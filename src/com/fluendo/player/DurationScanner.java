/* Copyright (C) <2008> Maik Merten <maikmerten@googlemail.com>
 * Copyright (C) <2004> Wim Taymans <wim@fluendo.com> (HTTPSrc.java parts)
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
package com.fluendo.player;

import com.fluendo.utils.Base64Converter;
import com.fluendo.utils.Debug;
import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;

/**
 *
 * @author maik
 */
public class DurationScanner {

    final static int NOTDETECTED = -1;
    final static int UNKNOWN = 0;
    final static int VORBIS = 1;
    final static int THEORA = 2;
    private long contentLength = -1;
    private long responseOffset;
    private Hashtable streaminfo = new Hashtable();
    private SyncState oy = new SyncState();
    private Page og = new Page();
    private Packet op = new Packet();

    public class TimingInfo {
      public float startTime = -1;
      public float duration = -1;
      TimingInfo(float st, float d) { startTime = st; duration = d; }
      TimingInfo() { startTime = -1; duration = -1; }
    };

    public DurationScanner() {
        oy.init();
    }

    private InputStream openWithConnection(URL url, String userId, String password, long offset) throws IOException {
        // lifted from HTTPSrc.java
        InputStream dis = null;
        String userAgent = "Cortado";

        URLConnection uc = url.openConnection();

        uc.setRequestProperty("Connection", "Keep-Alive");

        String range;
        if (offset != 0 && contentLength != -1) {
            range = "bytes=" + offset + "-" + (contentLength - 1);
        } else if (offset != 0) {
            range = "bytes=" + offset + "-";
        } else {
            range = null;
        }
        if (range != null) {
            Debug.info("doing range: " + range);
            uc.setRequestProperty("Range", range);
        }

        uc.setRequestProperty("User-Agent", userAgent);
        if (userId != null && password != null) {
            String userPassword = userId + ":" + password;
            String encoding = Base64Converter.encode(userPassword.getBytes());
            uc.setRequestProperty("Authorization", "Basic " + encoding);
        }
        uc.setRequestProperty("Content-Type", "application/octet-stream");

        /* This will send the request. */
        dis = uc.getInputStream();

        String responseRange = uc.getHeaderField("Content-Range");
        if (responseRange == null) {
            Debug.info("Response contained no Content-Range field, assuming offset=0");
            responseOffset = 0;
        } else {
            try {
                MessageFormat format = new MessageFormat("bytes {0,number}-{1,number}");
                format.setLocale(Locale.US);
                java.lang.Object parts[] = format.parse(responseRange);
                responseOffset = ((Number) parts[0]).longValue();
                if (responseOffset < 0) {
                    responseOffset = 0;
                }
                Debug.debug("Stream successfully with offset " + responseOffset);
            } catch (Exception e) {
                Debug.info("Error parsing Content-Range header");
                responseOffset = 0;
            }
        }

        contentLength = uc.getHeaderFieldInt("Content-Length", -1) + responseOffset;

        return dis;
    }

    private void determineType(Packet packet, StreamInfo info) {

        int ret;
        Class c;

        if (info.decoder != null) {
          ret = info.decoder.takeHeader(packet);
          if (ret > 0) {
            info.ready = true;
          }
          return;
        }

        // try theora
        try {
          c = Class.forName("com.fluendo.plugin.TheoraDec");
          com.fluendo.plugin.OggPayload pl = (com.fluendo.plugin.OggPayload)c.newInstance();
          ret = pl.takeHeader(packet);
          if (ret >= 0) {
              info.decoder = pl;
              info.type = THEORA;
              return;
          }
        }
        catch (Throwable e) {
        }

        // try vorbis
        try {
          c = Class.forName("com.fluendo.plugin.VorbisDec");
          com.fluendo.plugin.OggPayload pl = (com.fluendo.plugin.OggPayload)c.newInstance();
          ret = pl.takeHeader(packet);
          if (ret >= 0) {
              info.decoder = pl;
              info.type = VORBIS;
              return;
          }
        }
        catch (Throwable e) {
        }

        info.type = UNKNOWN;
    }

    public TimingInfo scanBuffer(byte[] buffer, int bufbytes) {
        long start = -1;
        long time = -1;

        int offset = oy.buffer(bufbytes);
        java.lang.System.arraycopy(buffer, 0, oy.data, offset, bufbytes);
        oy.wrote(bufbytes);

        while (oy.pageout(og) == 1) {

            Integer serialno = new Integer(og.serialno());
            StreamInfo info = (StreamInfo) streaminfo.get(serialno);
            if (info == null) {
                info = new StreamInfo();
                info.streamstate = new StreamState();
                info.streamstate.init(og.serialno());
                streaminfo.put(serialno, info);
                Debug.info("DurationScanner: created StreamState for stream no. " + serialno);
            }

            info.streamstate.pagein(og);

            while (info.streamstate.packetout(op) == 1) {

                int type = info.type;
                if (type == NOTDETECTED || !info.ready) {
                    determineType(op, info);
                }
                else if (type != NOTDETECTED && type != UNKNOWN && info.ready && info.startgranule < 0) {
                    info.startgranule = og.granulepos();
                    long thisStartTime = info.decoder.granuleToTime(info.startgranule);
                    if (start < 0 || thisStartTime < start) {
                      start = thisStartTime;
                    }
                    Debug.info("start granule for stream "+og.serialno()+": "+info.startgranule);
                }

                if (info.ready) switch (type) {
                    case VORBIS:
                         {
                            com.fluendo.plugin.OggPayload pl = info.decoder;
                            long t = pl.granuleToTime(og.granulepos()) - pl.granuleToTime(info.startgranule);
                            if (t > time) {
                                time = t;
                            }
                        }
                        break;
                    case THEORA:
                         {
                            com.fluendo.plugin.OggPayload pl = info.decoder;
                            long t = pl.granuleToTime(og.granulepos()) - pl.granuleToTime(info.startgranule);
                            if (t > time) {
                                time = t;
                            }
                        }
                        break;
                }
            }
        }

        return new TimingInfo(start / (float)com.fluendo.jst.Clock.SECOND,
                              time / (float)com.fluendo.jst.Clock.SECOND);
    }

    public TimingInfo scanURL(URL url, String user, String password) {
        try {
            int headbytes = 64 * 1024;
            int tailbytes = 128 * 1024;

            float start = -1;
            float time = 0;

            byte[] buffer = new byte[1024];
            InputStream is = openWithConnection(url, user, password, 0);

            int read = 0;
            long totalbytes = 0;
            read = is.read(buffer);
            // read beginning of the stream
            while (totalbytes < headbytes && read > 0) {
                totalbytes += read;
                TimingInfo tinfo = scanBuffer(buffer, read);
                if (tinfo.duration >= 0) {
                  float t = tinfo.duration;
                  time = t > time ? t : time;
                }
                if (tinfo.startTime >= 0 && start < 0) {
                  start = tinfo.startTime;
                }
                read = is.read(buffer);
            }
            is.close();
            is = openWithConnection(url, user, password, Math.max(0, contentLength - tailbytes));
            if(responseOffset == 0 && tailbytes<contentLength) {
                Debug.warning("DurationScanner: Couldn't complete duration scan due to failing range requests!");
                return new TimingInfo();
            }

            read = is.read(buffer);
            // read tail until eos, also abort if way too many bytes have been read
            while (read > 0 && totalbytes < (headbytes + tailbytes) * 2) {
                totalbytes += read;
                TimingInfo tinfo = scanBuffer(buffer, read);
                if (tinfo.duration >= 0) {
                  time = tinfo.duration > time ? tinfo.duration : time;
                }
                read = is.read(buffer);
            }

            return new TimingInfo(start, time);
        } catch (IOException e) {
            Debug.error(e.toString());
            return new TimingInfo();
        }
    }

    private class StreamInfo {

        public com.fluendo.plugin.OggPayload decoder = null;
        public int type = NOTDETECTED;
        public long startgranule = -1;
        public StreamState streamstate;
        public boolean ready = false;
    }

    public static void main(String[] args) throws IOException {

        URL url;
        url = new URL(args[0]);

        DurationScanner ds = new DurationScanner();
        TimingInfo tinfo = ds.scanURL(url, null, null);
        System.out.println(tinfo.duration);
    }
}
