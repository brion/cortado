package com.fluendo.jtheora;

public final class BlockMapping
{
  private int[][][] blockMap; 

  private static final int[] mbOrderMap = { 0, 2, 3, 1 };
  private static final int[][] blockOrderMap1 = 
  { { 0, 1, 3, 2 },
    { 0, 2, 3, 1 },
    { 0, 2, 3, 1 },
    { 3, 2, 0, 1 }
  };

  public final int quadMapToIndex1 (int sb, int mb, int b)
  {
    return blockMap[sb][mbOrderMap[mb]][blockOrderMap1[mb][b]];
  }

  public final int quadMapToMBTopLeft (int sb, int mb)
  {
    return blockMap[sb][mbOrderMap[mb]][0];
  }

  private void CreateMapping (int firstSB,
                            int firstFrag, int hFrags,
                            int vFrags)
  {
    int i = 0, j = 0;
    int xpos;
    int ypos;
    int mb, B;

    int sb=firstSB;
    int fragIndex=firstFrag;

    /* Set Super-Block dimensions */
    int sBRows = (vFrags>>2) + ((vFrags & 0x3) != 0 ? 1 : 0 );
    int sBCols = (hFrags>>2) + ((hFrags & 0x3) != 0 ? 1 : 0 );

    /* Map each Super-Block */
    for (int sBRow=0; sBRow<sBRows; sBRow++ ){
      for (int sBCol=0; sBCol<sBCols; sBCol++ ){
        /* Y co-ordinate of Super-Block in Block units */
        ypos = sBRow<<2;

        /* Map Blocks within this Super-Block */
        for ( i=0; (i<4) && (ypos<vFrags); i++, ypos++ ){
          /* X co-ordinate of Super-Block in Block units */
          xpos = sBCol<<2;

          for ( j=0; (j<4) && (xpos<hFrags); j++, xpos++ ){
            mb = (i & 2) + ((j & 2) >> 1);
            B = ((i & 1) << 1) + (j & 1);

            /* Set mapping and move to next fragment */
            blockMap[sb][mb][B] = fragIndex++;
          }

          /* Move to first fragment in next row in Super-Block */
          fragIndex += hFrags-j;
        }
  
        /* Move on to next Super-Block */
        sb++;
        fragIndex -= i*hFrags-j;
      }

      /* Move to first Super-Block in next row */
      fragIndex += 3*hFrags;
    }
  }

  public BlockMapping (int ySuperBlocks, int uvSuperBlocks, int hFrags, int vFrags ) 
  {
    blockMap = new int[ySuperBlocks + uvSuperBlocks * 2][4][4];

    for (int i=0; i<ySuperBlocks + uvSuperBlocks * 2; i++ ){
      for (int j=0; j<4; j++ ) {
        blockMap[i][j][0] = -1;
        blockMap[i][j][1] = -1;
        blockMap[i][j][2] = -1;
        blockMap[i][j][3] = -1;
      }
    }

    CreateMapping (0, 0, hFrags, vFrags );
    CreateMapping (ySuperBlocks, hFrags*vFrags, hFrags/2, vFrags/2 );
    CreateMapping (ySuperBlocks + uvSuperBlocks, (hFrags*vFrags*5)/4,
                  hFrags/2, vFrags/2 );
  }

}
