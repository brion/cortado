/* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package com.fluendo.plugin;

import java.awt.*;
import java.awt.image.*;
import com.jcraft.jogg.*;
import com.fluendo.jheora.*;
import com.fluendo.player.*;
import com.fluendo.utils.*;

public class TheoraPlugin extends Plugin 
{
  private Component component;
  private Toolkit toolkit;

  private Info ti;
  private Comment tc;
  private State ts;
  private Packet op;
  private int packet;
  private YUVBuffer yuv;

  public TheoraPlugin() {
    super(Plugin.TYPE_VIDEO);
  }

  public String getMime ()
  {
    return "video/x-theora";
  }
  public int typeFind (byte[] data, int offset, int length)
  {
    if (data[offset+1] == 0x74) {
      return Plugin.RANK_PRIMARY;
    }
    return Plugin.RANK_NONE;
  }

  public void initDecoder(Component comp) {
    component = comp;
    toolkit = component.getToolkit();

    ti = new Info();
    tc = new Comment();
    ts = new State();
    yuv = new YUVBuffer();
    op = new Packet();
    packet = 0;
  }

  public long offsetToTime (long offset) {
    return (long) (ts.granuleTime(offset) * 1000);
  }

  public MediaBuffer decode(MediaBuffer buf)
  {
    op.packet_base = buf.data;
    op.packet = buf.offset;
    op.bytes = buf.length;
    op.b_o_s = (packet == 0 ? 1 : 0);
    op.e_o_s = 0;
    op.packetno = packet;
    
    if (packet < 3) {
      //System.out.println ("decoding header");
      if (ti.decodeHeader(tc, op) < 0){
        buf.free();
        // error case; not a theora header
        Debug.log(Debug.ERROR, "does not contain Theora video data.");
        return null;
      }
      if (packet == 2) {
        ts.decodeInit(ti);
    
        Debug.log(Debug.INFO, "theora dimension: "+ti.width+"x"+ti.height);
        if (ti.aspect_denominator == 0) {
          ti.aspect_numerator = 1;
          ti.aspect_denominator = 1;
        }
        Debug.log(Debug.INFO, "theora offset: "+ti.offset_x+","+ti.offset_y);
        Debug.log(Debug.INFO, "theora frame: "+ti.frame_width+","+ti.frame_height);
        Debug.log(Debug.INFO, "theora aspect: "+ti.aspect_numerator+"/"+ti.aspect_denominator);
        Debug.log(Debug.INFO, "theora framerate: "+ti.fps_numerator+"/"+ti.fps_denominator);

        fps_numerator = ti.fps_numerator;
        fps_denominator = ti.fps_denominator;
        aspect_numerator = ti.aspect_numerator;
        aspect_denominator = ti.aspect_denominator;
      }
      buf.free();
      buf = null;
    }
    else {
      if (ts.decodePacketin(op) != 0) {
        buf.free();
        Debug.log(Debug.ERROR, "Error Decoding Theora.");
        return null;
      }
      if (ts.decodeYUVout(yuv) != 0) {
        buf.free();
        Debug.log(Debug.ERROR, "Error getting the picture.");
        return null;
      }
      buf.object = yuv.getObject(ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height);
    }
    packet++;

    return buf;
  }

  public void stop() {
  }
}
