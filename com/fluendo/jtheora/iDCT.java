/********************************************************************
 *                                                                  *
 * THIS FILE IS PART OF THE OggTheora SOFTWARE CODEC SOURCE CODE.   *
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
 *                                                                  *
 * THE Theora SOURCE CODE IS COPYRIGHT (C) 2002-2003                *
 * by the Xiph.Org Foundation http://www.xiph.org/                  *
 *                                                                  *
 ********************************************************************

  function:
  last mod: $Id: idct.c,v 1.6 2003/12/03 08:59:41 arc Exp $

 ********************************************************************/

package com.fluendo.jtheora;

public final class iDCT 
{
  private static final int IdctAdjustBeforeShift = 8;
  private static final int xC1S7 = 64277;
  private static final int xC2S6 = 60547;
  private static final int xC3S5 = 54491;
  private static final int xC4S4 = 46341;
  private static final int xC5S3 = 36410;
  private static final int xC6S2 = 25080;
  private static final int xC7S1 = 12785;

  private  int[] ip = new int[64];

  private final void dequant_slow(short[] dequant_coeffs,
                   short[] quantized_list,
                   int[] DCT_block) 
  {
    for(int i = 0; i < 64; i++) {
      DCT_block[Constants.dequant_index[i]] = quantized_list[i] * dequant_coeffs[i];
      /*
      if (i%8 ==0)
        System.out.print(": ");
      System.out.print("("+DCT_block[Constants.dequant_index[i]]+" ");
      System.out.print(dequant_coeffs[i]+" ");
      System.out.print(quantized_list[i]+")");
      if (i%8 ==7)
        System.out.print("\n");
	*/
    }
  }

  public final void IDctSlow (short[] InputData, short[]QuantMatrix, short[] OutputData) 
  {
    short[] op = OutputData;

    int _A, _B, _C, _D, _Ad, _Bd, _Cd, _Dd, _E, _F, _G, _H;
    int _Ed, _Gd, _Add, _Bdd, _Fd, _Hd;
    int t1, t2;

    dequant_slow (QuantMatrix, InputData, ip);

    /* Inverse DCT on the rows now */
    for (int loop = 0, off=0; loop < 8; loop++, off+=8){
      /* Check for non-zero values */
      if ((ip[0 + off] | ip[1 + off] | ip[2 + off] | ip[3 + off] | 
           ip[4 + off] | ip[5 + off] | ip[6 + off] | ip[7 + off]) != 0 ) 
      {
        t1 = (xC1S7 * ip[1 + off]) >> 16;
        t2 = (xC7S1 * ip[7 + off]) >> 16;
        _A = t1 + t2;

        t1 = (xC7S1 * ip[1 + off]) >> 16;
        t2 = (xC1S7 * ip[7 + off]) >> 16;
        _B = t1 - t2;

        t1 = (xC3S5 * ip[3 + off]) >> 16;
        t2 = (xC5S3 * ip[5 + off]) >> 16;
        _C = t1 + t2;

        t1 = (xC3S5 * ip[5 + off]) >> 16;
        t2 = (xC5S3 * ip[3 + off]) >> 16;
        _D = t1 - t2;

        _Ad = (xC4S4 * (_A - _C)) >> 16;
        _Bd = (xC4S4 * (_B - _D)) >> 16;

        _Cd = _A + _C;
        _Dd = _B + _D;

        _E = (xC4S4 * (ip[0 + off] + ip[4 + off])) >> 16;
        _F = (xC4S4 * (ip[0 + off] - ip[4 + off])) >> 16;

        t1 = (xC2S6 * ip[2 + off]) >> 16;
        t2 = (xC6S2 * ip[6 + off]) >> 16;
        _G = t1 + t2;

        t1 = (xC6S2 * ip[2 + off]) >> 16;
        t2 = (xC2S6 * ip[6 + off]) >> 16;
        _H = t1 - t2;


        _Ed = _E - _G;
        _Gd = _E + _G;

        _Add = _F + _Ad;
        _Bdd = _Bd - _H;

        _Fd = _F - _Ad;
        _Hd = _Bd + _H;

        /* Final sequence of operations over-write original inputs. */
        ip[0 + off] = (short)((_Gd + _Cd )   >> 0);
        ip[7 + off] = (short)((_Gd - _Cd )   >> 0);

        ip[1 + off] = (short)((_Add + _Hd )  >> 0);
        ip[2 + off] = (short)((_Add - _Hd )  >> 0);

        ip[3 + off] = (short)((_Ed + _Dd )   >> 0);
        ip[4 + off] = (short)((_Ed - _Dd )   >> 0);

        ip[5 + off] = (short)((_Fd + _Bdd )  >> 0);
        ip[6 + off] = (short)((_Fd - _Bdd )  >> 0);
      }
    }

    for (int loop = 0, off=0; loop < 8; loop++, off++){
      /* Check for non-zero values (bitwise or faster than ||) */
      if ((ip[0 * 8 + off] | ip[1 * 8 + off] | ip[2 * 8 + off] | ip[3 * 8 + off] |
           ip[4 * 8 + off] | ip[5 * 8 + off] | ip[6 * 8 + off] | ip[7 * 8 + off]) != 0) 
      {
        t1 = (xC1S7 * ip[1*8 + off]) >> 16;
        t2 = (xC7S1 * ip[7*8 + off]) >> 16;
        _A = t1 + t2;

        t1 = (xC7S1 * ip[1*8 + off]) >> 16;
        t2 = (xC1S7 * ip[7*8 + off]) >> 16;
        _B = t1 - t2;

        t1 = (xC3S5 * ip[3*8 + off]) >> 16;
        t2 = (xC5S3 * ip[5*8 + off]) >> 16;
        _C = t1 + t2;

        t1 = (xC3S5 * ip[5*8 + off]) >> 16;
        t2 = (xC5S3 * ip[3*8 + off]) >> 16;
        _D = t1 - t2;

        _Ad = (xC4S4 * (_A - _C)) >> 16;
        _Bd = (xC4S4 * (_B - _D)) >> 16;

        _Cd = _A + _C;
        _Dd = _B + _D;

        _E = (xC4S4 * (ip[0*8 + off] + ip[4*8 + off])) >> 16;
        _F = (xC4S4 * (ip[0*8 + off] - ip[4*8 + off])) >> 16;

        t1 = (xC2S6 * ip[2*8 + off]) >> 16;
        t2 = (xC6S2 * ip[6*8 + off]) >> 16;
        _G = t1 + t2;

        t1 = (xC6S2 * ip[2*8 + off]) >> 16;
        t2 = (xC2S6 * ip[6*8 + off]) >> 16;
        _H = t1 - t2;

        _Ed = _E - _G;
        _Gd = _E + _G;

        _Add = _F + _Ad;
        _Bdd = _Bd - _H;

        _Fd = _F - _Ad;
        _Hd = _Bd + _H;

        _Gd += IdctAdjustBeforeShift;
        _Add += IdctAdjustBeforeShift;
        _Ed += IdctAdjustBeforeShift;
        _Fd += IdctAdjustBeforeShift;

        /* Final sequence of operations over-write original inputs. */
        op[0*8 + off] = (short)((_Gd + _Cd )   >> 4);
        op[7*8 + off] = (short)((_Gd - _Cd )   >> 4);

        op[1*8 + off] = (short)((_Add + _Hd )  >> 4);
        op[2*8 + off] = (short)((_Add - _Hd )  >> 4);

        op[3*8 + off] = (short)((_Ed + _Dd )   >> 4);
        op[4*8 + off] = (short)((_Ed - _Dd )   >> 4);

        op[5*8 + off] = (short)((_Fd + _Bdd )  >> 4);
        op[6*8 + off] = (short)((_Fd - _Bdd )  >> 4);
      }else{
        op[0*8 + off] = 0;
        op[7*8 + off] = 0;
        op[1*8 + off] = 0;
        op[2*8 + off] = 0;
        op[3*8 + off] = 0;
        op[4*8 + off] = 0;
        op[5*8 + off] = 0;
        op[6*8 + off] = 0;
      }
    }
  }

  /************************
    x  x  x  x  0  0  0  0
    x  x  x  0  0  0  0  0
    x  x  0  0  0  0  0  0
    x  0  0  0  0  0  0  0
    0  0  0  0  0  0  0  0
    0  0  0  0  0  0  0  0
    0  0  0  0  0  0  0  0
    0  0  0  0  0  0  0  0
  *************************/

  private final void dequant_slow10 (short[] dequant_coeffs,
                     short[] quantized_list,
                     int[] DCT_block)
  {
    for(int i=0; i<32; i++)
      DCT_block[i] = 0;

    for(int i=0; i<10; i++)
      DCT_block[Constants.dequant_index[i]] = quantized_list[i] * dequant_coeffs[i];
  }

  public final void IDct10 (short[] InputData, short[]QuantMatrix, short[] OutputData)
  {
    short[] op = OutputData;

    int _A, _B, _C, _D, _Ad, _Bd, _Cd, _Dd, _E, _F, _G, _H;
    int _Ed, _Gd, _Add, _Bdd, _Fd, _Hd;
    int t1, t2;

    dequant_slow10 (QuantMatrix, InputData, ip);

    /* Inverse DCT on the rows now */
    for (int loop = 0, off = 0; loop < 4; loop++, off += 8){
      /* Check for non-zero values */
      if ((ip[0 + off] | ip[1 + off] | ip[2 + off] | ip[3 + off]) != 0 ){
        _A = (xC1S7 * ip[1 + off]) >> 16;
        _B = (xC7S1 * ip[1 + off]) >> 16;
        _C = (xC3S5 * ip[3 + off]) >> 16;
        _D = (xC5S3 * ip[3 + off]) >> 16;

        _Ad = (xC4S4 * (_A - _C)) >> 16;
        _Bd = (xC4S4 * (_B - _D)) >> 16;

        _Cd = _A + _C;
        _Dd = _B + _D;

        _E = (xC4S4 * ip[0 + off] ) >> 16;
        _F = _E;
        _G = (xC2S6 * ip[2 + off]) >> 16;
        _H = (xC6S2 * ip[2 + off]) >> 16;

        _Ed = _E - _G;
        _Gd = _E + _G;

        _Add = _F + _Ad;
        _Bdd = _Bd - _H;

        _Fd = _F - _Ad;
        _Hd = _Bd + _H;

        /* Final sequence of operations over-write original inputs. */
        ip[0 + off] = (short)((_Gd + _Cd )   >> 0);
        ip[7 + off] = (short)((_Gd - _Cd )   >> 0);

        ip[1 + off] = (short)((_Add + _Hd )  >> 0);
        ip[2 + off] = (short)((_Add - _Hd )  >> 0);

        ip[3 + off] = (short)((_Ed + _Dd )   >> 0);
        ip[4 + off] = (short)((_Ed - _Dd )   >> 0);

        ip[5 + off] = (short)((_Fd + _Bdd )  >> 0);
        ip[6 + off] = (short)((_Fd - _Bdd )  >> 0);

      }
    }

    for (int loop = 0, off = 0; loop < 8; loop++, off++) {
      /* Check for non-zero values (bitwise or faster than ||) */
      if ((ip[0*8 + off] | ip[1*8 + off] | ip[2*8 + off] | ip[3*8 + off]) != 0) {

        _A = (xC1S7 * ip[1*8 + off]) >> 16;
        _B = (xC7S1 * ip[1*8 + off]) >> 16;
        _C = (xC3S5 * ip[3*8 + off]) >> 16;
        _D = (xC5S3 * ip[3*8 + off]) >> 16;

        _Ad = (xC4S4 * (_A - _C)) >> 16;
        _Bd = (xC4S4 * (_B - _D)) >> 16;

        _Cd = _A + _C;
        _Dd = _B + _D;

        _E = (xC4S4 * ip[0*8 + off]) >> 16;
        _F = _E;
        _G = (xC2S6 * ip[2*8 + off]) >> 16;
        _H = (xC6S2 * ip[2*8 + off]) >> 16;

        _Ed = _E - _G;
        _Gd = _E + _G;

        _Add = _F + _Ad;
        _Bdd = _Bd - _H;

        _Fd = _F - _Ad;
        _Hd = _Bd + _H;

        _Gd += IdctAdjustBeforeShift;
        _Add += IdctAdjustBeforeShift;
        _Ed += IdctAdjustBeforeShift;
        _Fd += IdctAdjustBeforeShift;

        /* Final sequence of operations over-write original inputs. */
        op[0*8 + off] = (short)((_Gd + _Cd )   >> 4);
        op[7*8 + off] = (short)((_Gd - _Cd )   >> 4);

        op[1*8 + off] = (short)((_Add + _Hd )  >> 4);
        op[2*8 + off] = (short)((_Add - _Hd )  >> 4);

        op[3*8 + off] = (short)((_Ed + _Dd )   >> 4);
        op[4*8 + off] = (short)((_Ed - _Dd )   >> 4);

        op[5*8 + off] = (short)((_Fd + _Bdd )  >> 4);
        op[6*8 + off] = (short)((_Fd - _Bdd )  >> 4);
      }else{
        op[0*8 + off] = 0;
        op[7*8 + off] = 0;
        op[1*8 + off] = 0;
        op[2*8 + off] = 0;
        op[3*8 + off] = 0;
        op[4*8 + off] = 0;
        op[5*8 + off] = 0;
        op[6*8 + off] = 0;
      }
    }
  }

  /***************************
    x   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
    0   0   0  0  0  0  0  0
  **************************/

  public final void IDct1( short[] InputData,
              short[]QuantMatrix,
              short[] OutputData ){
    short OutD=(short) ((int)(InputData[0]*QuantMatrix[0]+15)>>5);

    for (int loop=0; loop<64; loop++)
      OutputData[loop]=OutD;
  }
}
