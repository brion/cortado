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
  last mod: $Id: hufftables.h,v 1.6 2003/12/03 08:59:42 arc Exp $

 ********************************************************************/

package com.fluendo.jtheora;

public class HuffTables
{
  //byte[] ExtraBitLengths_VP31[Constants.MAX_ENTROPY_TOKENS] = {
  static final byte[] ExtraBitLengths_VP31 = {
    0, 0, 0, 2, 3, 4, 12,3, 6,                 /* EOB and Zero-run tokens. */
    0, 0, 0, 0,                                /* Very low value tokens. */
    1, 1, 1, 1, 2, 3, 4, 5, 6, 10,             /* Other value tokens */
    1, 1, 1, 1, 1, 3, 4,                       /* Category 1 runs. */
    2, 3,                                      /* Category 2 runs. */
  };


  static final int[][] FrequencyCounts_VP3 = {
  /* DC Intra bias */
  {  198,    62,    22,    31,    14,     6,     6,   205,     3,
     843,   843,   415,   516,
     660,   509,   412,   347,   560,   779,   941,   930,   661,   377,
     170,   155,    39,     2,     9,    15,    11,
     128,    86,
  },
  {  299,    92,    34,    39,    15,     6,     6,   132,     1,
     851,   851,   484,   485,
     666,   514,   416,   351,   567,   788,   953,   943,   670,   383,
     117,   119,    26,     4,    17,     7,     1,
      93,    56,
  },
  {  367,   115,   42,   47,   16,    6,    6,   105,    1,
     896,   896,   492,   493,
     667,   510,   408,   342,   547,   760,   932,   927,   656,   379,
     114,   103,   10,    3,    6,    2,    1,
     88,   49,
  },
  {  462,   158,   63,   76,   28,    9,    8,   145,    1,
     1140,  1140,   573,   574,
     754,   562,   435,   357,   555,   742,   793,   588,   274,   81,
     154,   117,   13,    6,   12,    2,    1,
     104,   62,
  },
  {  558,   196,   81,   99,   36,   11,    9,   135,    1,
     1300,  1301,   606,   607,
     779,   560,   429,   349,   536,   680,   644,   405,   153,   30,
     171,   120,   12,    5,   14,    3,    1,
     104,   53,
  },
  {  635,   233,   100,   122,   46,   14,   12,   113,    1,
     1414,  1415,   631,   631,
     785,   555,   432,   335,   513,   611,   521,   284,   89,   13,
     170,   113,   10,    5,   14,    3,    1,
     102,   62,
  },
  {  720,   276,   119,   154,   62,   20,   16,   101,    1,
     1583,  1583,   661,   661,
     794,   556,   407,   318,   447,   472,   343,   153,   35,    1,
     172,   115,   11,    7,   14,    3,    1,
     112,   70,
  },
  {  853,   326,   144,   184,   80,   27,   19,   52,    1,
     1739,  1740,   684,   685,
     800,   540,   381,   277,   364,   352,   218,   78,   13,    1,
     139,   109,    9,    6,   20,    2,    1,
     94,   50,
  },

  /* DC Inter Bias */
  {  490,   154,   57,   53,   10,    2,    1,   238,   160,
     1391,  1390,   579,   578,
     491,   273,   172,   118,   152,   156,   127,   79,   41,   39,
     712,   547,   316,   125,   183,   306,   237,
     451,   358,
  },
  {  566,   184,   70,   65,   11,    2,    1,   235,   51,
     1414,  1414,   599,   598,
     510,   285,   180,   124,   157,   161,   131,   82,   42,   40,
     738,   551,   322,   138,   195,   188,   93,
     473,   365,
  },
  {  711,   261,   111,   126,   27,    4,    1,   137,   52,
     1506,  1505,   645,   645,
     567,   316,   199,   136,   172,   175,   142,   88,   45,   48,
     548,   449,   255,   145,   184,   174,   121,
     260,   227,
  },
  {  823,   319,   144,   175,   43,    7,    1,   53,   42,
     1648,  1648,   653,   652,
     583,   329,   205,   139,   175,   176,   139,   84,   44,   34,
     467,   389,   211,   137,   181,   186,   107,
     106,   85,
  },
  {  948,   411,   201,   276,   85,   16,    2,   39,   33,
     1778,  1777,   584,   583,
     489,   265,   162,   111,   140,   140,   108,   64,   38,   23,
     428,   356,   201,   139,   186,   165,   94,
     78,   63,
  },
  {  1002,   470,   248,   386,   153,   39,    6,   23,   23,
     1866,  1866,   573,   573,
     467,   249,   155,   103,   130,   128,   94,   60,   38,   14,
     323,   263,   159,   111,   156,   153,   74,
     46,   34,
  },
  {  1020,   518,   291,   504,   242,   78,   18,   14,   14,
     1980,  1979,   527,   526,
     408,   219,   132,   87,   110,   104,   79,   55,   31,    7,
     265,   213,   129,   91,   131,   111,   50,
     31,   20,
  },
  {  1018,   544,   320,   591,   338,   139,   47,    5,    2,
     2123,  2123,   548,   547,
     414,   212,   126,   83,   101,   96,   79,   60,   23,    1,
     120,   97,   55,   39,   60,   38,   15,
     11,    8,
  },

  /* AC INTRA Tables  */
  /* AC Intra bias group 1 tables */
  {  242,   62,   22,   20,    4,    1,    1,   438,    1,
     593,   593,   489,   490,
     657,   580,   471,   374,   599,   783,   869,   770,   491,   279,
     358,   144,   82,   54,   49,   70,    5,
     289,   107,
  },
  {  317,   95,   38,   41,    8,    1,    1,   479,    1,
     653,   654,   500,   501,
     682,   611,   473,   376,   582,   762,   806,   656,   358,   155,
     419,   162,   86,   58,   36,   34,    1,
     315,   126,
  },
  {  382,   121,   49,   59,   15,    3,    1,   496,    1,
     674,   674,   553,   554,
     755,   636,   487,   391,   576,   718,   701,   488,   221,   72,
     448,   161,   107,   56,   37,   29,    1,
     362,   156,
  },
  {  415,   138,   57,   73,   21,    5,    1,   528,    1,
     742,   741,   562,   563,
     753,   669,   492,   388,   563,   664,   589,   340,   129,   26,
     496,   184,   139,   71,   48,   33,    2,
     387,   166,
  },
  {  496,   170,   73,   94,   31,    8,    2,   513,    1,
     855,   855,   604,   604,
     769,   662,   477,   356,   486,   526,   381,   183,   51,    5,
     590,   214,   160,   85,   60,   39,    3,
     427,   203,
  },
  {  589,   207,   89,   116,   40,   13,    3,   491,    1,
     919,   919,   631,   631,
     769,   633,   432,   308,   408,   378,   247,   94,   17,    1,
     659,   247,   201,   105,   73,   51,    3,
     466,   242,
  },
  {  727,   266,   115,   151,   49,   17,    6,   439,    1,
     977,   977,   642,   642,
     718,   572,   379,   243,   285,   251,   133,   40,    1,    1,
     756,   287,   253,   126,   94,   66,    4,
     492,   280,
  },
  {  940,   392,   180,   247,   82,   30,   14,   343,    1,
     1064,  1064,   615,   616,
     596,   414,   235,   146,   149,   108,   41,    1,    1,    1,
     882,   314,   346,   172,   125,   83,    6,
     489,   291,
  },
  /* AC Inter bias group 1 tables */
  {  440,   102,   33,   23,    2,    1,    1,   465,   85,
     852,   852,   744,   743,
     701,   496,   297,   193,   225,   200,   129,   58,   18,    2,
     798,   450,   269,   202,   145,   308,   154,
     646,   389,
  },
  {  592,   151,   53,   43,    6,    1,    1,   409,   34,
     875,   875,   748,   747,
     723,   510,   305,   196,   229,   201,   130,   59,   18,    2,
     800,   436,   253,   185,   115,   194,   88,
     642,   368,
  },
  {  759,   222,   86,   85,   17,    2,    1,   376,   46,
     888,   888,   689,   688,
     578,   408,   228,   143,   165,   141,   84,   35,    7,    1,
     878,   488,   321,   244,   147,   266,   124,
     612,   367,
  },
  {  912,   298,   122,   133,   34,    7,    1,   261,   44,
     1092,  1091,   496,   496,
     409,   269,   150,   95,   106,   87,   49,   16,    1,    1,
     1102,   602,   428,   335,   193,   323,   157,
     423,   253,
  },
  {  1072,   400,   180,   210,   60,   16,    3,   210,   40,
     1063,  1063,   451,   451,
     345,   221,   121,   73,   79,   64,   31,    6,    1,    1,
     1105,   608,   462,   358,   202,   330,   155,
     377,   228,
  },
  {  1164,   503,   254,   330,   109,   34,    9,   167,   35,
     1038,  1037,   390,   390,
     278,   170,   89,   54,   56,   40,   13,    1,    1,    1,
     1110,   607,   492,   401,   218,   343,   141,
     323,   192,
  },
  {  1173,   583,   321,   486,   196,   68,   23,   124,   23,
     1037,  1037,   347,   346,
     232,   139,   69,   40,   37,   20,    2,    1,    1,    1,
     1128,   584,   506,   410,   199,   301,   113,
     283,   159,
  },
  {  1023,   591,   366,   699,   441,   228,   113,   79,    5,
     1056,  1056,   291,   291,
     173,   96,   38,   19,    8,    1,    1,    1,    1,    1,
     1187,   527,   498,   409,   147,   210,   56,
     263,   117,
  },

  /* AC Intra bias group 2 tables */
  {  311,   74,   27,   27,    5,    1,    1,   470,   24,
     665,   667,   637,   638,
     806,   687,   524,   402,   585,   679,   609,   364,   127,   20,
     448,   210,   131,   76,   52,   111,   19,
     393,   195,
  },
  {  416,   104,   39,   38,    8,    1,    1,   545,   33,
     730,   731,   692,   692,
     866,   705,   501,   365,   495,   512,   387,   168,   39,    2,
     517,   240,   154,   86,   64,   127,   19,
     461,   247,
  },
  {  474,   117,   43,   42,    9,    1,    1,   560,   40,
     783,   783,   759,   760,
     883,   698,   466,   318,   404,   377,   215,   66,    7,    1,
     559,   259,   176,   110,   87,   170,   22,
     520,   278,
  },
  {  582,   149,   53,   53,   12,    2,    1,   473,   39,
     992,   993,   712,   713,
     792,   593,   373,   257,   299,   237,   114,   25,    1,    1,
     710,   329,   221,   143,   116,   226,   26,
     490,   259,
  },
  {  744,   210,   78,   77,   16,    2,    1,   417,   37,
     1034,  1035,   728,   728,
     718,   509,   296,   175,   184,   122,   42,    3,    1,    1,
     791,   363,   255,   168,   145,   311,   35,
     492,   272,
  },
  {  913,   291,   121,   128,   28,    4,    1,   334,   40,
     1083,  1084,   711,   712,
     624,   378,   191,   107,   95,   50,    7,    1,    1,    1,
     876,   414,   288,   180,   164,   382,   39,
     469,   275,
  },
  {  1065,   405,   184,   216,   53,    8,    1,   236,   36,
     1134,  1134,   685,   686,
     465,   253,   113,   48,   41,    9,    1,    1,    1,    1,
     965,   451,   309,   179,   166,   429,   53,
     414,   249,
  },
  {  1148,   548,   301,   438,   160,   42,    6,   84,   17,
     1222,  1223,   574,   575,
     272,   111,   23,    6,    2,    1,    1,    1,    1,    1,
     1060,   502,   328,   159,   144,   501,   54,
     302,   183,
  },
  /* AC Inter bias group 2 tables */
  {  403,   80,   24,   17,    1,    1,    1,   480,   90,
     899,   899,   820,   819,
     667,   413,   228,   133,   139,   98,   42,   10,    1,    1,
     865,   470,   316,   222,   171,   419,   213,
     645,   400,
  },
  {  698,   169,   59,   49,    6,    1,    1,   414,   101,
     894,   893,   761,   761,
     561,   338,   171,   96,   97,   64,   26,    6,    1,    1,
     896,   494,   343,   239,   192,   493,   215,
     583,   366,
  },
  {  914,   255,   94,   80,   10,    1,    1,   345,   128,
     935,   935,   670,   671,
     415,   222,   105,   55,   51,   30,   10,    1,    1,    1,
     954,   530,   377,   274,   232,   641,   295,
     456,   298,
  },
  {  1103,   359,   146,   135,   20,    1,    1,   235,   119,
     1042,  1042,   508,   507,
     293,   146,   65,   33,   30,   16,    4,    1,    1,    1,
     1031,   561,   407,   296,   265,   813,   317,
     301,   192,
  },
  {  1255,   504,   238,   265,   51,    5,    1,   185,   113,
     1013,  1013,   437,   438,
     212,   92,   41,   18,   15,    6,    1,    1,    1,    1,
     976,   530,   386,   276,   260,   927,   357,
     224,   148,
  },
  {  1292,   610,   332,   460,   127,   16,    1,   136,   99,
     1014,  1015,   384,   384,
     153,   65,   25,   11,    6,    1,    1,    1,    1,    1,
     942,   487,   343,   241,   238,   970,   358,
     174,   103,
  },
  {  1219,   655,   407,   700,   280,   55,    2,   100,   60,
     1029,  1029,   337,   336,
     119,   43,   11,    3,    2,    1,    1,    1,    1,    1,
     894,   448,   305,   199,   213,  1005,   320,
     136,   77,
  },
  {  1099,   675,   435,   971,   581,   168,   12,   37,   16,
     1181,  1081,   319,   318,
     66,   11,    6,    1,    1,    1,    1,    1,    1,    1,
     914,   370,   235,   138,   145,   949,   128,
     94,   41,
  },

  /* AC Intra bias group 3 tables */
  {  486,   112,   39,   34,    6,    1,    1,   541,   67,
     819,   818,   762,   763,
     813,   643,   403,   280,   332,   295,   164,   53,    6,    1,
     632,   294,   180,   131,   105,   208,   109,
     594,   295,
  },
  {  723,   191,   69,   65,   12,    1,    1,   445,   79,
     865,   865,   816,   816,
     750,   515,   290,   172,   184,   122,   46,    5,    1,    1,
     740,   340,   213,   165,   129,   270,   168,
     603,   326,
  },
  {  884,   264,   102,   103,   21,    3,    1,   382,   68,
     897,   897,   836,   836,
     684,   427,   227,   119,   119,   70,   16,    1,    1,    1,
     771,   367,   234,   184,   143,   272,   178,
     555,   326,
  },
  {  1028,   347,   153,   161,   36,    8,    1,   251,   44,
     1083,  1084,   735,   735,
     541,   289,   144,   77,   57,   23,    3,    1,    1,    1,
     926,   422,   270,   215,   176,   301,   183,
     443,   248,
  },
  {  1155,   465,   224,   264,   71,   14,    3,   174,   27,
     1110,  1111,   730,   731,
     429,   206,   79,   30,   19,    4,    1,    1,    1,    1,
     929,   443,   279,   225,   194,   298,   196,
     354,   223,
  },
  {  1191,   576,   296,   415,   144,   36,    8,   114,   16,
     1162,  1162,   749,   749,
     338,   108,   29,    8,    5,    1,    1,    1,    1,    1,
     947,   458,   273,   207,   194,   248,   145,
     258,   152,
  },
  {  1169,   619,   366,   603,   247,   92,   23,   46,    1,
     1236,  1236,   774,   775,
     191,   35,   14,    1,    1,    1,    1,    1,    1,    1,
     913,   449,   260,   214,   194,   180,   82,
     174,   98,
  },
  {  1006,   537,   381,   897,   504,   266,   101,   39,    1,
     1307,  1307,   668,   667,
     116,    3,    1,    1,    1,    1,    1,    1,    1,    1,
     1175,   261,   295,   70,   164,   107,   31,
     10,   76,
  },
  /* AC Inter bias group 3 tables */
  {  652,   156,   53,   43,    5,    1,    1,   368,   128,
     983,   984,   825,   825,
     583,   331,   163,   88,   84,   48,   15,    1,    1,    1,
     870,   480,   316,   228,   179,   421,   244,
     562,   349,
  },
  {  988,   280,   104,   87,   12,    1,    1,   282,   194,
     980,   981,   738,   739,
     395,   189,   80,   37,   31,   12,    2,    1,    1,    1,
     862,   489,   333,   262,   214,   600,   446,
     390,   260,
  },
  {  1176,   399,   165,   154,   24,    2,    1,   218,   224,
     1017,  1018,   651,   651,
     280,   111,   42,   16,    9,    3,    1,    1,    1,    1,
     787,   469,   324,   269,   229,   686,   603,
     267,   194,
  },
  {  1319,   530,   255,   268,   47,    4,    1,   113,   183,
     1149,  1150,   461,   461,
     173,   58,   17,    5,    3,    1,    1,    1,    1,    1,
     768,   450,   305,   261,   221,   716,   835,
     136,   97,
  },
  {  1362,   669,   355,   465,   104,    9,    1,   76,   153,
     1253,  1253,   398,   397,
     102,   21,    5,    1,    1,    1,    1,    1,    1,    1,
     596,   371,   238,   228,   196,   660,   954,
     68,   53,
  },
  {  1354,   741,   446,   702,   174,   15,    1,   38,   87,
     1498,  1498,   294,   294,
     43,    7,    1,    1,    1,    1,    1,    1,    1,    1,
     381,   283,   165,   181,   155,   544,  1039,
     25,   21,
  },
  {  1262,   885,   546,   947,   263,   18,    1,   18,   27,
     1908,  1908,   163,   162,
     14,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     195,   152,   83,   125,   109,   361,   827,
     7,    5,
  },
  {  2539,   951,   369,   554,   212,   18,    1,    1,    1,
     2290,  2289,   64,   64,
     1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     18,   18,    9,   55,   36,   184,   323,
     1,    1,
  },


  /* AC Intra bias group 4 tables */
  {  921,   264,   101,   100,   19,    2,    1,   331,   98,
     1015,  1016,   799,   799,
     512,   269,   119,   60,   50,   17,    1,    1,    1,    1,
     841,   442,   307,   222,   182,   493,   256,
     438,   310,
  },
  {  1147,   412,   184,   206,   50,    6,    1,   242,   141,
     977,   976,   808,   807,
     377,   135,   40,   10,    7,    1,    1,    1,    1,    1,
     788,   402,   308,   223,   205,   584,   406,
     316,   227,
  },
  {  1243,   504,   238,   310,   79,   11,    1,   184,   150,
     983,   984,   814,   813,
     285,   56,   10,    1,    1,    1,    1,    1,    1,    1,
     713,   377,   287,   217,   180,   615,   558,
     208,   164,
  },
  {  1266,   606,   329,   484,   161,   27,    1,   79,   92,
     1187,  1188,   589,   588,
     103,   10,    1,    1,    1,    1,    1,    1,    1,    1,
     680,   371,   278,   221,   244,   614,   728,
     80,   62,
  },
  {  1126,   828,   435,   705,   443,   90,    8,   10,   55,
     1220,  1219,   350,   350,
     28,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     602,   330,   222,   168,   158,   612,   919,
     104,    5,
  },
  {  1210,   506,  1014,   926,   474,   240,    4,    1,    44,
     1801,  1801,   171,   171,
     1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     900,   132,    36,    11,    47,   191,   316,
     2,    1,
  },
  {  1210,   506,  1014,   926,   474,   240,    4,    1,    44,
     1801,  1801,   171,   171,
     1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     900,   132,    36,    11,    47,   191,   316,
     2,    1,
  },
  {  1210,   506,  1014,   926,   474,   240,    4,    1,    44,
     1801,  1801,   171,   171,
     1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     900,   132,    36,    11,    47,   191,   316,
     2,    1,
  },
  /* AC Inter bias group 4 tables */
  {  1064,   325,   129,   117,    20,    2,    1,   266,   121,
     1000,  1000,   706,   706,
     348,   162,    67,    32,    25,    11,    1,    1,    1,    1,
     876,   513,   363,   274,   225,   627,   384,
     370,   251,
  },
  {  1311,   517,   238,   254,    45,    3,    1,   188,   160,
     1070,  1070,   635,   635,
     239,    85,    30,    11,    6,    1,    1,    1,    1,    1,
     744,   420,   313,   239,   206,   649,   541,
     221,   155,
  },
  {  1394,   632,   322,   385,    78,    7,    1,   134,   152,
     1163,  1164,   607,   607,
     185,    51,    12,    3,    1,    1,    1,    1,    1,    1,
     631,   331,   275,   203,   182,   604,   620,
     146,    98,
  },
  {  1410,   727,   407,   546,   146,    19,    1,    67,    88,
     1485,  1486,   419,   418,
     103,    18,    3,    1,    1,    1,    1,    1,    1,    1,
     555,   261,   234,   164,   148,   522,   654,
      67,    39,
  },
  {  1423,   822,   492,   719,   216,    22,    1,    28,    59,
     1793,  1793,   323,   324,
     37,    2,    1,    1,    1,    1,    1,    1,    1,    1,
     376,   138,   158,   102,   119,   400,   604,
     28,    9,
  },
  {  1585,   923,   563,   918,   207,    25,    1,    5,    20,
     2229,  2230,   172,   172,
     7,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     191,    40,    56,    22,    65,   243,   312,
     2,    1,
  },
  {  2225,  1100,   408,   608,   133,    8,    1,    1,    1,
     2658,  2658,    25,    24,
     1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     8,    1,    1,    1,    1,   125,    16,
     1,    1,
  },
  {  2539,   951,   369,   554,   212,    18,    1,    1,    1,
     2290,  2289,    64,    64,
     1,    1,    1,    1,    1,    1,    1,    1,    1,    1,
     18,    18,    9,    55,    36,   184,   323,
     1,    1,
  },
 };
}

