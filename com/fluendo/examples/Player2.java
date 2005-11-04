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

package com.fluendo.examples;

import com.fluendo.plugin.*;
import com.fluendo.jst.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class PlayPipeline extends Pipeline implements PadListener {
  private Element httpsrc;
  private Element oggdemux;
  private Element theoradec;
  private Element vorbisdec;
  private Element videosink;
  private Element audiosink;
  private Element v_queue, a_queue;

  public void padAdded(Pad pad) {
    if (pad.getName().equals("serial_31273")) {
      pad.link(a_queue.getPad("sink"));
    }
    if (pad.getName().equals("serial_31272")) {
      pad.link(v_queue.getPad("sink"));
    }
  }
  
  public void padRemoved(Pad pad) {
    System.out.println ("pad removed "+pad);
  }

  public void noMorePads() {
    System.out.println ("no more pads");
  }

  public PlayPipeline ()
  {
    httpsrc = ElementFactory.makeByName("httpsrc");
    //httpsrc.setProperty("url", "http://localhost/fluendo/src/fluendo/psvn/cortado/trunk/test8.ogg");
    httpsrc.setProperty("url", "http://localhost/fluendo/src/fluendo/psvn/cortado/trunk/sanju.ogg");

    oggdemux = ElementFactory.makeByName("oggdemux");
    theoradec = ElementFactory.makeByName("theoradec");
    vorbisdec = ElementFactory.makeByName("vorbisdec");
    audiosink = ElementFactory.makeByName("audiosinkj2");
    videosink = ElementFactory.makeByName("videosink");
    v_queue = new Queue();
    a_queue = new Queue();

    oggdemux.addPadListener (this);

    add(httpsrc);
    add(oggdemux);
    add(theoradec);
    add(vorbisdec);
    add(videosink);
    add(audiosink);
    add(v_queue);
    add(a_queue);

    httpsrc.getPad("src").link(oggdemux.getPad("sink"));

    v_queue.getPad("src").link(theoradec.getPad("sink"));
    theoradec.getPad("src").link(videosink.getPad("sink"));

    a_queue.getPad("src").link(vorbisdec.getPad("sink"));
    vorbisdec.getPad("src").link(audiosink.getPad("sink"));
  }

  protected boolean doSendEvent(com.fluendo.jst.Event event) {
    if (event.getType() != com.fluendo.jst.Event.SEEK)
      return false;

    httpsrc.getPad("src").sendEvent (event);
    getState(null, null, 0);

    long t1 = ((Sink)videosink).getPrerollTime();
    long t2 = ((Sink)audiosink).getPrerollTime();

    streamTime = Math.min (t1, t2);
    System.out.println ("stream time "+streamTime);

    return true;
  }
}


class PlayFrame extends Frame implements AdjustmentListener {
  private Scrollbar sb;
  private PlayPipeline pipeline;
  
  public PlayFrame(PlayPipeline p) {
    pipeline = p;
    sb = new Scrollbar(Scrollbar.HORIZONTAL, 0, 2, 0, 100);  
    sb.setSize(200, 16);
    sb.addAdjustmentListener (this);
    setSize(200,32);
    add(sb);
    pack (); 
  }
  
  public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
    if (!e.getValueIsAdjusting()) {
      int val;
      com.fluendo.jst.Event event;

      /* get value, convert to PERCENT and construct seek event */
      val = e.getValue();
      event = com.fluendo.jst.Event.newSeek (Format.PERCENT, val * Format.PERCENT_SCALE);

      /* send event to pipeline */
      pipeline.sendEvent(event);
    }
  }
}

public class Player2 implements BusHandler {
  private PlayPipeline pipeline;
  private Bus bus;
  private Frame frame;

  public Player2 ()
  {
    pipeline = new PlayPipeline();
    bus = pipeline.getBus();
    bus.addHandler (this);

    frame = new PlayFrame(pipeline);
    frame.show();

    pipeline.setState (Element.PLAY);
  }

  public void handleMessage (Message msg)
  {
    switch (msg.getType()) {
      case Message.WARNING:
      case Message.ERROR:
        System.out.println ("got ERROR from "+msg.getSrc()+ ": "+msg);
	break;
      case Message.EOS:
        System.out.println ("got EOS from "+msg.getSrc()+ ": "+msg);
	break;
      default:	
	break;
    }
  }

  public static void main(String args[]) {
    Player2 p2 = new Player2();

    synchronized (p2) {
      try {
        p2.wait ();
      }
      catch (InterruptedException ie) {}
    }
  }
}
