/* Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

package com.fluendo.player;

import java.applet.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import com.jcraft.jogg.*;
import com.fluendo.codecs.*;
import com.fluendo.jheora.*;
import com.fluendo.utils.*;

public class VideoConsumer implements DataConsumer, Runnable
{
  private ImageTarget target;
  private Component component;
  private Toolkit toolkit;
  private MediaTracker mt;
  private int queueid;
  private int framenr;
  private Clock clock;
  private boolean ready;
  private static final int MAX_BUFFER = 1;
  private double framerate;
  private double frameperiod;
  private double aspect = 1.;
  private boolean stopping = false;
  private double avgratio;
  private SmokeCodec smoke;
  private String currentType;

  private Info ti;
  private Comment tc;
  private State ts;
  private Packet op = new Packet();
  private int packet = 0;
  private YUVBuffer yuv;
  
  public VideoConsumer(Clock newClock, ImageTarget target, double framerate) {
    this.target = target;
    component = target.getComponent();
    toolkit = component.getToolkit();
    mt = new MediaTracker(component);
    queueid = QueueManager.registerQueue(MAX_BUFFER);
    clock = newClock;
    frameperiod = 1000.0 / framerate;
    smoke = new SmokeCodec(component, mt);

    ti = new Info();
    tc = new Comment();
    ts = new State();
    yuv = new YUVBuffer();
  }

  public void setFramerate (double framerate) {
    this.framerate = framerate;
    frameperiod = 1000.0 / framerate;
    System.out.println("frameperiod: "+frameperiod);
  }

  public void setType(String type) {
    currentType = type;;
  }

  public boolean isReady() {
    return ready;
  }

  public void consume(byte[] data, int offset, int length) {
    Image newImage = null;
    
    try {
      if (currentType.equals("image/x-smoke")) {
        newImage = smoke.decode (data, offset, length);

        setFramerate(smoke.fps_num/(double)smoke.fps_denom);
        aspect = 1.0;
      }
      else if (currentType.equals("image/jpeg")) {
        newImage = toolkit.createImage(data, offset, length);
        mt.addImage(newImage, 0);
        mt.waitForID(0);
        mt.removeImage(newImage, 0);
      }
      else if (currentType.equals("video/x-theora")) {
        //System.out.println ("creating packet");
        op.packet_base = data;
        op.packet = offset;
        op.bytes = length;
        op.b_o_s = (packet == 0 ? 1 : 0);
        op.e_o_s = 0;
        op.packetno = packet;
        
        if (packet < 3) {
          //System.out.println ("decoding header");
          if(ti.decodeHeader(tc, op) < 0){
            // error case; not a theora header
            System.err.println("does not contain Theora video data.");
            return;
          }
          if (packet == 2) {
            ts.decodeInit(ti);

            System.out.println("theora dimension: "+ti.width+"x"+ti.height);
	    if (ti.aspect_denominator == 0) {
	      ti.aspect_numerator = 1;
	      ti.aspect_denominator = 1;
	    }
            System.out.println("theora offset: "+ti.offset_x+","+ti.offset_y);
            System.out.println("theora frame: "+ti.frame_width+","+ti.frame_height);
            System.out.println("theora aspect: "+ti.aspect_numerator+"/"+ti.aspect_denominator);
            System.out.println("theora framerate: "+ti.fps_numerator+"/"+ti.fps_denominator);
            setFramerate(ti.fps_numerator/(double)ti.fps_denominator);
            aspect = ti.aspect_numerator/(double)ti.aspect_denominator;
          }
        }
        else {
	  if (ts.decodePacketin(op) != 0) {
            System.err.println("Error Decoding Theora.");
	    return;
	  }
	  if (ts.decodeYUVout(yuv) != 0) {
            System.err.println("Error getting the picture.");
	    return;
	  }
	  newImage = yuv.getAsImage(toolkit, ti.offset_x, ti.offset_y, ti.frame_width, ti.frame_height);
	}
	packet++;
      }
      if (newImage != null) {
        QueueManager.enqueue(queueid, newImage);
      }
    }
    catch (Exception e) { e.printStackTrace();}
  }

  public void stop() {
    stopping = true;
  }

  public void run() {
    System.out.println("entering video thread");
    while (!stopping) {
      //System.out.println("dequeue image");
      Image image = (Image) QueueManager.dequeue(queueid);
      //System.out.println("dequeued image");

      try {
	if (framenr == 0) {
	  // first frame, wait for signal
	  synchronized (clock) {
	    ready = true;
	    System.out.println("video preroll wait");
	    clock.wait();
	    System.out.println("video preroll go!");
	  }
	}
	else {
	  clock.waitForMediaTime((long) (framenr * frameperiod));
	}
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      target.setImage(image, framerate, aspect);
      framenr++;
    }
  }
}
