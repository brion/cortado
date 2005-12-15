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

import java.io.*;
import java.net.*;
import com.fluendo.jst.*;
import com.fluendo.utils.*;

public class HTTPSrc extends Element
{
  private Thread thread;
  private boolean stopping;
  private String userId;
  private String password;
  private String urlString;
  private InputStream input;
  private long contentLength;

  private static final int DEFAULT_READSIZE = 4096;

  private int readSize = DEFAULT_READSIZE;

  private Pad srcpad = new Pad(Pad.SRC, "src") {
    private boolean doSeek (Event event) {
      boolean result;
      int format;
      long position;

      format = event.getSeekFormat();
      position = event.getSeekPosition();

      if (format == Format.PERCENT) {
        position = position * contentLength / Format.PERCENT_MAX;
      }
      else if (format != Format.BYTES) {
        Debug.log (Debug.WARNING, "can only seek in bytes");
        return false;
      }

      pushEvent (Event.newFlushStart());

      synchronized (streamLock) {
        try {
          input = getInputStream (position);
          result = true;
        }
        catch (Exception e) {
	  e.printStackTrace ();
          result = false;
        }
        pushEvent (Event.newFlushStop());

        pushEvent (Event.newNewsegment(Format.BYTES, position));

        if (result)
	  result = startTask();
      }
      return result;
    }

    protected boolean eventFunc (Event event)
    {
      boolean res;

      switch (event.getType()) {
        case Event.SEEK:
	  res = doSeek(event);
	  break;
        default:
          res = super.eventFunc (event);
          break;
      }
      return res;
    }

    protected void taskFunc()
    {
      int ret;

      Buffer data = Buffer.create();
      data.ensureSize (readSize);
      data.offset = 0;
      try {
        data.length = input.read (data.data, 0, readSize);
      }
      catch (Exception e) {
	e.printStackTrace();
        data.length = 0;
      }
      if (data.length <= 0) {
	/* EOS */
	data.free();
        Debug.log(Debug.INFO, this+" reached EOS");
	pushEvent (Event.newEOS());
	pauseTask();
      }
      else {
        if ((ret = push(data)) != OK) {
	  if (isFlowFatal (ret)) {
	    postMessage (Message.newStreamStatus (this, "reason: "+getFlowName (ret)));
	  }
	  pauseTask();
        }
      }
    }
    
    protected boolean activateFunc (int mode)
    {
      boolean res = true;

      switch (mode) {
        case MODE_NONE:
	  res = stopTask();
	  break;
        case MODE_PUSH:
	  try {
	    input = getInputStream(0); 
	  }
	  catch (Exception e) {
	    res = false;
	  }
	  if (res)
	    res = startTask();
	  break;
	default:
	  res = false;
	  break;
      }
      return res;
    }
  };

  private InputStream getInputStream (long offset) throws Exception
  {
    InputStream dis = null;

    try {
      postMessage(Message.newResource (this, "Opening "+urlString));
      Debug.log(Debug.INFO, "reading from url "+urlString);
      URL url = new URL(urlString);
      Debug.log(Debug.INFO, "trying to open "+url);
      URLConnection uc = url.openConnection();
      if (userId != null && password != null) {
        String userPassword = userId + ":" + password;
        String encoding = Base64Converter.encode (userPassword.getBytes());
        uc.setRequestProperty ("Authorization", "Basic " + encoding);
      }
      uc.setRequestProperty ("Range", "bytes=" + offset+"-");
      /* FIXME, do typefind ? */
      dis = uc.getInputStream();
      contentLength = uc.getHeaderFieldInt ("Content-Length", 0) + offset;
      Debug.log(Debug.INFO, "opened "+url);
      Debug.log(Debug.INFO, "contentLength: "+contentLength);
    }
    catch (SecurityException e) {
      e.printStackTrace();
      postMessage(Message.newError (this, "Not allowed "+urlString+"..."));
    }
    catch (Exception e) {
      e.printStackTrace();
      postMessage(Message.newError (this, "Failed opening "+urlString+"..."));
    }

    return dis;
  }

  public String getName () {
    return "httpsrc";
  }

  public HTTPSrc () {
    super ();
    addPad (srcpad);
  }

  public synchronized boolean setProperty(String name, java.lang.Object value) {
    boolean res = true;

    if (name.equals("url")) {
      urlString = String.valueOf(value);
    }
    else if (name.equals("userId")) {
      userId = String.valueOf(value);
    }
    else if (name.equals("password")) {
      password = String.valueOf(value);
    }
    else if (name.equals("readSize")) {
      readSize = Integer.parseInt((String)value);
    }
    else {
      res = false;
    }
    return res;
  }
}
