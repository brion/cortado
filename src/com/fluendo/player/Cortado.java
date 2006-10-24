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
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import com.fluendo.utils.*;
import com.fluendo.jst.*;

public class Cortado extends Applet implements Runnable, MouseMotionListener,
        MouseListener, BusHandler, StatusListener, ActionListener {
    private static final long serialVersionUID = 1L;

    private static Cortado cortado;
    private static CortadoPipeline pipeline;

    private String urlString;
    private boolean seekable;
    private boolean audio;
    private boolean video;
    private boolean keepAspect;
    private boolean autoPlay;
    private int bufferSize;
    private String userId;
    private String password;
    private int bufferLow;
    private int bufferHigh;
    private int debug;
    private double duration;

    private boolean statusRunning;
    private Thread statusThread;
    private Status status;
    private int statusHeight = 20;
    private boolean inStatus;
    private boolean isBuffering;
    private int desiredState;

    private boolean isEOS;
    private boolean isError;

    private int showStatus;
    private static final String[] showStatusVals = { "auto", "show", "hide" };
    private static final int STATUS_AUTO = 0;
    private static final int STATUS_SHOW = 1;
    private static final int STATUS_HIDE = 2;
    private int hideTimeout;
    private int hideCounter;
    private boolean mayHide;

    private PopupMenu menu;

    private Hashtable params = new Hashtable();

    private Configure configure;

    private Dimension appletDimension;

    public String getAppletInfo() {
        return "Title: Fluendo media player \nAuthor: Wim Taymans \nA Java based network multimedia player.";
    }

    public String[][] getParameterInfo() {
        String[][] info = {
                { "url", "URL", "The media file to play" },
                { "seekable", "boolean",
                        "Can you seek in this file (default false)" },
                { "duration", "float",
                        "Total duration of the file in seconds (default unknown)" },
                { "audio", "boolean", "Enable audio playback (default true)" },
                { "video", "boolean", "Enable video playback (default true)" },
                { "statusHeight", "int", "The height of the status area (default 12)" },
                { "autoPlay", "boolean", "Automatically start playback (default true)" },
                { "showStatus", "enum", "Show status area (auto|show|hide) (default auto)" },
                { "hideTimeout", "int", "Timeout in seconds to hide the status area when " +
			"showStatus is auto (default 0)" },
                { "keepAspect", "boolean",
                        "Use aspect ratio of video (default true)" },
                { "bufferSize", "int",
                        "The size of the prebuffer in Kbytes (default 100)" },
                { "bufferLow", "int", "Percent of empty buffer (default 10)" },
                { "bufferHigh", "int", "Percent of full buffer (default 70)" },
                { "userId", "string",
                        "userId for basic authentication (default null)" },
                { "password", "string",
                        "password for basic authentication (default null)" },
                { "debug", "int", "Debug level 0 - 4 (default = 3)" }, };
        return info;
    }

    public void setParam(String name, String value) {
        params.put(name, value);
    }

    public void restart() {
        stop();
        init();
        start();
    }

    public String getParam(String name, String def) {
        String result;

        result = (String) params.get(name);

        if (result == null) {
            try {
                result = getParameter(name);
            } catch (Exception e) {
            }
        }
        if (result == null) {
            result = def;
        }
        return result;
    }
    public int getEnum(String name, String[] vals, String def) {
      String val = getParam (name, def);
      for (int i=0; i<vals.length;i++) {
	 if (vals[i].equals (val))
           return i;
      }
      return 0;
    }

    public static void shutDown(Throwable error) {
        Debug.log(Debug.INFO, "shutting down: reason: " + error.getMessage());
        error.printStackTrace();
        cortado.stop();
    }

    public synchronized void init() {
        cortado = this;

	System.out.println("init()");

	if (pipeline != null)
          stop();

        pipeline = new CortadoPipeline();
        configure = new Configure();

        urlString = getParam("url", null);
        seekable = String.valueOf(getParam("seekable", "false")).equals("true");
        duration = Double.valueOf(getParam("duration", "-1.0")).doubleValue();
        audio = String.valueOf(getParam("audio", "true")).equals("true");
        video = String.valueOf(getParam("video", "true")).equals("true");
        statusHeight = Integer.valueOf(getParam("statusHeight", "12")).intValue();
        autoPlay = String.valueOf(getParam("autoPlay", "true")).equals( "true");
        showStatus = getEnum("showStatus", showStatusVals, "auto");
        hideTimeout = Integer.valueOf(getParam("hideTimeout", "0")).intValue();
        keepAspect = String.valueOf(getParam("keepAspect", "true")).equals("true");
        bufferSize = Integer.valueOf(getParam("bufferSize", "200")).intValue();
        bufferLow = Integer.valueOf(getParam("bufferLow", "10")).intValue();
        bufferHigh = Integer.valueOf(getParam("bufferHigh", "70")).intValue();
        debug = Integer.valueOf(getParam("debug", "3")).intValue();
        userId = getParam("userId", null);
        password = getParam("password", null);

        Debug.level = debug;
        Debug.log(Debug.INFO, "build info: " + configure.buildInfo);

        pipeline.setUrl(urlString);
        pipeline.setUserId(userId);
        pipeline.setPassword(password);
        pipeline.enableAudio(audio);
        pipeline.enableVideo(video);
        pipeline.setBufferSize(bufferSize);
        pipeline.setBufferLow(bufferLow);
        pipeline.setBufferHigh(bufferHigh);

	URL documentBase;
	try {
	  documentBase = getDocumentBase();
          Debug.log(Debug.INFO, "Document base: " + documentBase);
	}
	catch (Throwable t) {
          documentBase = null;
	}
        pipeline.setDocumentBase(documentBase);
        pipeline.setComponent(this);
        pipeline.getBus().addHandler(this);

        setBackground(Color.black);
        setForeground(Color.white);

        status = new Status(this);
        status.setHaveAudio(audio);
        status.setHavePercent(true);
        status.setSeekable(seekable);
        status.setDuration(duration);
        inStatus = false;
        mayHide = (hideTimeout == 0);
	hideCounter = 0;
	if (showStatus != STATUS_HIDE) {
          status.setVisible(true);
	}
	else {
          status.setVisible(false);
	}

        menu = new PopupMenu();
        menu.add("About...");
        menu.addActionListener(this);
        this.add(menu);
    }

    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();

        if (command.equals("About...")) {
            AboutFrame about = new AboutFrame(pipeline);
            about.d.setVisible(true);
        }
    }

    public Graphics getGraphics() {
        Graphics g = super.getGraphics();

        if (status != null && status.isVisible()) {
            g.setClip(0, 0, getSize().width, getSize().height - statusHeight);
        } else {
            g.setClip(0, 0, getSize().width, getSize().height);
        }
        return g;
    }

    public Dimension getSize() {
        if (appletDimension == null)
            appletDimension = super.getSize();

        return appletDimension;
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void run() {
        try {
            realRun();
        } catch (Throwable t) {
            Cortado.shutDown(t);
        }
    }

    private void realRun() {
        Debug.log(Debug.INFO, "entering status thread");
        while (statusRunning) {
            try {
		long now = pipeline.getPosition() / Clock.SECOND;
                status.setTime(now);

                Thread.sleep(1000);

		if (hideCounter > 0) {
	          hideCounter--;
		  if (hideCounter == 0) {
	            mayHide = true;
                    setStatusVisible(false, false);
		  }
		}
            } catch (Exception e) {
                if (statusRunning)
                    e.printStackTrace();
            }
        }
        Debug.log(Debug.INFO, "exit status thread");
    }

    public void paint(Graphics g) {
        int dwidth = getSize().width;
        int dheight = getSize().height;

        /* sometimes dimension is wrong */
        if (dwidth <= 0 || dheight <= statusHeight) {
	  appletDimension = null;
	  return;
	}
	
        if (status != null && status.isVisible()) {
            status.setBounds(0, dheight - statusHeight, dwidth, statusHeight);
            status.paint(g);
        }
    }

    private void setStatusVisible(boolean b, boolean force) {
        /* no change, do nothing */
        if (status.isVisible() == b)
            return;

	/* refuse to hide when hideTimeout did not expire */
	if (!b && !mayHide)
            return;

	if (!force) {
	  if (showStatus == STATUS_SHOW && !b)
              return;
	  if (showStatus == STATUS_HIDE && b)
              return;
	}
	/* never hide when we are in error */
	if (isError && !b)
            return;
          
        /* don't make invisible when the mouse pointer is inside status area */
        if (inStatus && !b)
            return;

        status.setVisible(b);
        repaint();
    }

    private boolean intersectStatus(MouseEvent e) {
        inStatus = e.getY() > getSize().height - statusHeight;
        return inStatus;
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        setStatusVisible(false, false);
    }

    public void mousePressed(MouseEvent e) {
        if (intersectStatus(e)) {
            int y = getSize().height - statusHeight;
            e.translatePoint(0, -y);
            ((MouseListener) status).mousePressed(e);
        } else {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                menu.show(this, e.getX(), e.getY());
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (intersectStatus(e)) {
            int y = getSize().height - statusHeight;
            e.translatePoint(0, -y);
            ((MouseListener) status).mouseReleased(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (intersectStatus(e)) {
            int y = getSize().height - statusHeight;
            setStatusVisible(true, false);
            e.translatePoint(0, -y);
            ((MouseMotionListener) status).mouseDragged(e);
        } else {
            setStatusVisible(false, false);
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (intersectStatus(e)) {
            int y = getSize().height - statusHeight;
            setStatusVisible(true, false);
            e.translatePoint(0, -y);
            ((MouseMotionListener) status).mouseMoved(e);
        } else {
            setStatusVisible(false, false);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.getType()) {
        case Message.WARNING:
        case Message.ERROR:
            System.out.println(msg.toString());
	    if (!isError) {
              status.setMessage(msg.parseErrorString());
              status.setState(Status.STATE_STOPPED);
              pipeline.setState(Element.STOP);
              setStatusVisible(true, true);
	      isError = true;
	    }
            break;
        case Message.EOS:
            Debug.log(Debug.INFO, "EOS: playback ended");
	    if (!isError) {
              status.setState(Status.STATE_STOPPED);
              status.setMessage("Playback ended");
	      isEOS = true;
              pipeline.setState(Element.STOP);
              setStatusVisible(true, false);
	    }
            break;
        case Message.STREAM_STATUS:
            System.out.println(msg.toString());
            break;
        case Message.RESOURCE:
	    if (!isError) {
              status.setMessage(msg.parseResourceString());
              setStatusVisible(true, false);
	    }
            break;
        case Message.BUFFERING:
	    boolean busy;
	    int percent;

	    if (isError)
	      break;

	    busy = msg.parseBufferingBusy();
	    percent = msg.parseBufferingPercent();

	    if (busy) {
	      if (!isBuffering) {
                Debug.log(Debug.INFO, "PAUSE: we are buffering");
		if (desiredState == Element.PLAY)
	          pipeline.setState(Element.PAUSE);
		isBuffering = true;
                setStatusVisible(true, false);
	      }
              status.setBufferPercent(busy, percent);
	    }
	    else {
	      if (isBuffering) {
                Debug.log(Debug.INFO, "PLAY: we finished buffering");
		if (desiredState == Element.PLAY)
	          pipeline.setState(Element.PLAY);
		isBuffering = false;
                setStatusVisible(false, false);
	      }
              status.setBufferPercent(busy, percent);
	    }
            break;
        case Message.STATE_CHANGED:
            if (msg.getSrc() == pipeline) {
                int old, next;

                old = msg.parseStateChangedOld();
                next = msg.parseStateChangedNext();

                switch (next) {
                case Element.PAUSE:
		    if (!isError && !isEOS) {
                      status.setMessage("Paused");
		    }
                    status.setState(Status.STATE_PAUSED);
                    break;
                case Element.PLAY:
		    if (!isError && !isEOS) {
                      status.setMessage("Playing");
                      setStatusVisible(false, false);
		      if (!mayHide)
		        hideCounter = hideTimeout;
		    }
                    status.setState(Status.STATE_PLAYING);
                    break;
                case Element.STOP:
		    if (!isError && !isEOS) {
                      status.setMessage("Stopped");
                      setStatusVisible(true, false);
		    }
                    status.setState(Status.STATE_STOPPED);
                    break;
                }
            }
            break;
        default:
            break;
        }
    }

    public void doPause()
    {
      isError = false;
      isEOS = false;
      status.setMessage("Pause");
      desiredState = Element.PAUSE;
      pipeline.setState(desiredState);
    }
    public void doPlay()
    {
      isError = false;
      isEOS = false;
      status.setMessage("Play");
      desiredState = Element.PLAY;
      pipeline.setState(desiredState);
    }
    public void doStop() {
      status.setMessage("Stop");
      desiredState = Element.STOP;
      pipeline.setState(desiredState);
    }

    public void doSeek(double aPos) {
      boolean res;
      com.fluendo.jst.Event event;

      /* get value, convert to PERCENT and construct seek event */
      event = com.fluendo.jst.Event.newSeek(Format.PERCENT,
              (int) (aPos * 100.0 * Format.PERCENT_SCALE));

      /* send event to pipeline */
      res = pipeline.sendEvent(event);
      if (!res) {
          Debug.log(Debug.WARNING, "seek failed");
      }
    }

    public void newState(int aState) {
        int ret;
        switch (aState) {
        case Status.STATE_PAUSED:
            doPause();
            break;
        case Status.STATE_PLAYING:
            doPlay();
            break;
        case Status.STATE_STOPPED:
	    doStop();
            break;
        default:
            break;
        }
    }
    public void newSeek(double aPos) {
      doSeek (aPos);
    }

    public synchronized void start() {
        int res;

	System.out.println("start()");

        addMouseListener(this);
        addMouseMotionListener(this);
        status.addStatusListener(this);

	if (autoPlay) 
          desiredState = Element.PLAY;
	else
          desiredState = Element.PAUSE;

        res = pipeline.setState(desiredState);

	if (statusThread != null)
          throw new RuntimeException ("invalid state");

        statusThread = new Thread(this, "cortado-StatusThread-"+Debug.genId());
	statusRunning = true;
        statusThread.start();
    }

    public synchronized void stop() {
	System.out.println("stop()");
	statusRunning = false;
        desiredState = Element.STOP;
	if (pipeline != null) {
	  System.out.println("pipeline stop");
          pipeline.setState(desiredState);
	  System.out.println("pipeline shutdown");
          pipeline.shutDown();
	  System.out.println("pipeline stopped");
	  pipeline = null;
	}
        if (statusThread != null) {
          try {
            statusThread.interrupt();
          } catch (Exception e) {
          }
          try {
            statusThread.join();
          } catch (Exception e) {
          }
          statusThread = null;
	}
    }
}

/* dialog box */

class AppFrame extends Frame 
    implements WindowListener { 
  public AppFrame(String title) { 
    super(title); 
    addWindowListener(this); 
  } 
  public void windowClosing(WindowEvent e) { 
    setVisible(false); 
    dispose(); 
    System.exit(0); 
  } 
  public void windowClosed(WindowEvent e) {} 
  public void windowDeactivated(WindowEvent e) {} 
  public void windowActivated(WindowEvent e) {} 
  public void windowDeiconified(WindowEvent e) {} 
  public void windowIconified(WindowEvent e) {} 
  public void windowOpened(WindowEvent e) {} 
} 

class AboutFrame extends AppFrame { 
  Dialog d; 
 
  public AboutFrame(CortadoPipeline pipeline) { 
    super("AboutFrame"); 

    Configure configure = new Configure();

    setSize(200, 100); 
    Button dbtn; 
    d = new Dialog(this, "About Cortado", false); 
    d.setVisible(true);

    TextArea ta = new TextArea("", 8, 40, TextArea.SCROLLBARS_NONE);
    d.add(ta);
    ta.appendText("This is Cortado " + configure.buildVersion + ".\n");
    ta.appendText("Brought to you by Wim Taymans.\n");
    ta.appendText("(C) Copyright 2004,2005,2006 Fluendo\n\n");
    ta.appendText("Built on " + configure.buildDate + "\n");
    ta.appendText("Built in " + configure.buildType + " mode.\n");
    ta.appendText("Running on Java VM " + System.getProperty("java.version")
                  + " from " + System.getProperty("java.vendor") + "\n");

    if (pipeline.isAudioEnabled()) {
      if (pipeline.usingJavaX) {
        ta.appendText("Using the javax.sound backend.");
      } else {
        ta.appendText("Using the sun.audio backend.\n\n");
        ta.appendText("NOTE: you should install the Java(TM) from Sun for better audio quality.");
      }
    }

    d.add(dbtn = new Button("OK"), 
      BorderLayout.SOUTH); 

    Dimension dim = d.getPreferredSize();
    d.setSize(dim);
    dbtn.addActionListener(new ActionListener() { 
      public void actionPerformed(ActionEvent e) { 
        d.setVisible(false); 
      } 
    }); 
    d.addWindowListener(new WindowAdapter() { 
      public void windowClosing(WindowEvent e) { 
        d.setVisible(false); 
      } 
    }); 
  } 
}
