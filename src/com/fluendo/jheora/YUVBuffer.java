/* Jheora
 * Copyright (C) 2004 Fluendo S.L.
 *  
 * Written by: 2004 Wim Taymans <wim@fluendo.com>
 *   
 * Many thanks to 
 *   The Xiph.Org Foundation http://www.xiph.org/
 * Jheora was based on their Theora reference decoder.
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.fluendo.jheora;

import java.awt.image.*;
//import java.util.Random;

public class YUVBuffer implements ImageProducer {

    public int y_width;
    public int y_height;
    public int y_stride;
    public int uv_width;
    public int uv_height;
    public int uv_stride;
    public short[] data;
    public int y_offset;
    public int u_offset;
    public int v_offset;
    private int[] pixels;
    private int pix_size;
    private boolean newPixels = true;
    private ColorModel colorModel = ColorModel.getRGBdefault();
    private ImageProducer filteredThis;
    private int crop_x;
    private int crop_y;
    private int crop_w;
    private int crop_h;

    public void addConsumer(ImageConsumer ic) {
    }

    public boolean isConsumer(ImageConsumer ic) {
        return false;
    }

    public void removeConsumer(ImageConsumer ic) {
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    public void startProduction(ImageConsumer ic) {
        ic.setColorModel(colorModel);
        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
                ImageConsumer.COMPLETESCANLINES |
                ImageConsumer.SINGLEFRAME |
                ImageConsumer.SINGLEPASS);
        ic.setDimensions(y_width, y_height);
        prepareRGBData(0, 0, y_width, y_height);
        ic.setPixels(0, 0, y_width, y_height, colorModel, pixels, 0, y_width);
        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }

    private synchronized void prepareRGBData(int x, int y, int width, int height) {
        if (!newPixels) {
            return;
        }

        int size = width * height;

        try {
            if (size != pix_size) {
                pixels = new int[size];
                pix_size = size;
            }
            YUVtoRGB(x, y, width, height);
        } catch (Throwable t) {
            /* ignore */
        }
        newPixels = false;
    }

    public synchronized void newPixels() {
        newPixels = true;
    }

    // cropping code provided by Benjamin Schwartz
    public Object getObject(int x, int y, int width, int height) {
        if (x == 0 && y == 0 && width == y_width && height == y_height) {
            return this;
        } else {
            if (x != crop_x || y != crop_y || width != crop_w || height != crop_h) {
                crop_x = x;
                crop_y = y;
                crop_w = width;
                crop_h = height;
                CropImageFilter cropFilter = new CropImageFilter(crop_x, crop_y, crop_w, crop_h);
                filteredThis = new FilteredImageSource(this, cropFilter);
            }
            return filteredThis;
        }
    }

   
    private void YUVtoRGB(int x, int y, int width, int height) {

        /*
         * this modified version of the original YUVtoRGB was
         * provided by Ilan and Yaniv Ben Hagai.
         *
         * additional thanks to Gumboot for helping with making this
         * code perform better.
         */

        // Set up starting values for YUV pointers
        int YPtr = y_offset + x + y * (y_stride);
        int YPtr2 = YPtr + y_stride;
        int UPtr = u_offset + x / 2 + (y / 2) * (uv_stride);
        int VPtr = v_offset + x / 2 + (y / 2) * (uv_stride);
        int RGBPtr = 0;
        int RGBPtr2 = width;
        int width2 = width / 2;
        int height2 = height / 2;

        // Set the line step for the Y and UV planes and YPtr2
        int YStep = y_stride * 2 - (width2) * 2;
        int UVStep = uv_stride - (width2);
        int RGBStep = width;

        for (int i = 0; i < height2; i++) {
            for (int j = 0; j < width2; j++) {
                int D, E, r, g, b, t1, t2, t3, t4;

                D = data[UPtr++];
                E = data[VPtr++];

                t1 = 298 * (data[YPtr] - 16);
                t2 = 409 * E - 409*128 + 128;
                t3 = (100 * D) + (208 * E) - 100*128 - 208*128 - 128;
                t4 = 516 * D - 516*128 + 128;

                r = (t1 + t2);
                g = (t1 - t3);
                b = (t1 + t4);

                // retrieve data for next pixel now, hide latency?
                t1 = 298 * (data[YPtr + 1] - 16);

                // pack pixel
                pixels[RGBPtr] =
                        (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b)>>8) | 0xff000000;

                r = (t1 + t2);
                g = (t1 - t3);
                b = (t1 + t4);

                // retrieve data for next pixel now, hide latency?
                t1 = 298 * (data[YPtr2] - 16);

                // pack pixel
                pixels[RGBPtr + 1] =
                        (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b)>>8) | 0xff000000;


                r = (t1 + t2);
                g = (t1 - t3);
                b = (t1 + t4);

                // retrieve data for next pixel now, hide latency?
                t1 = 298 * (data[YPtr2 + 1] - 16);

                // pack pixel
                pixels[RGBPtr2] =
                        (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b)>>8) | 0xff000000;


                r = (t1 + t2);
                g = (t1 - t3);
                b = (t1 + t4);

                // pack pixel
                pixels[RGBPtr2 + 1] =
                        (clamp65280(r) << 8) | clamp65280(g) | (clamp65280(b)>>8) | 0xff000000;
                YPtr += 2;
                YPtr2 += 2;
                RGBPtr += 2;
                RGBPtr2 += 2;
            }

            // Increment the various pointers
            YPtr += YStep;
            YPtr2 += YStep;
            UPtr += UVStep;
            VPtr += UVStep;
            RGBPtr += RGBStep;
            RGBPtr2 += RGBStep;
        }
    }

    // kept for reference
    /*private static final int clamp255(int val) {
        return ((~(val>>31)) & 255 & (val | ((255-val)>>31)));
    }*/
    
    private static final int clamp65280(int val) {
        /* 65280 == 255 << 8 == 0x0000FF00 */
        /* This function is just like clamp255, but only acting on the top
        24 bits (bottom 8 are zero'd).  This allows val, initially scaled
        to 65536, to be clamped without shifting, thereby saving one shift.
        (RGB packing must be aware that the info is in the second-lowest
        byte.) */
        return ((~(val>>31)) & 65280 & (val | ((65280-val)>>31)));
    }

    // some benchmarking stuff, uncomment if you need it
    /*public static void main(String[] args) {
        YUVBuffer yuvbuf = new YUVBuffer();

        // let's create a 1280x720 picture with noise

        int x = 1280;
        int y = 720;

        int size = (x * y) + (x * y) / 2;
        short[] picdata = new short[size];

        Random r = new Random();
        for (int i = 0; i < picdata.length; ++i) {
            picdata[i] = (short) (r.nextInt(255) | 0xFF);
        }

        System.out.println("bench...");

        yuvbuf.data = picdata;
        yuvbuf.y_height = y;
        yuvbuf.y_width = x;
        yuvbuf.y_stride = x;
        yuvbuf.uv_height = y / 2;
        yuvbuf.uv_width = x / 2;
        yuvbuf.uv_stride = x / 2;
        yuvbuf.u_offset = x / 2;
        yuvbuf.v_offset = x + x / 2;

        int times = 5000;

        long start = System.currentTimeMillis();
        for (int i = 0; i < times; ++i) {
            yuvbuf.newPixels();
            yuvbuf.prepareRGBData(0, 0, x, y);
        }
        long end = System.currentTimeMillis();

        System.out.println("average conversion time per frame: " + ((double) (end - start)) / (times * 1f) + " ms.");

    }*/
}
