package org.p2c2e.zag;

public interface OpConstants
{
  public static final int NOP = 0x00;
  public static final int ADD = 0x10;
  public static final int SUB = 0x11;
  public static final int MUL = 0x12;
  public static final int DIV = 0x13;
  public static final int MOD = 0x14;
  public static final int NEG = 0x15;
  public static final int BITAND = 0x18;
  public static final int BITOR = 0x19;
  public static final int BITXOR = 0x1a;
  public static final int BITNOT = 0x1b;
  public static final int SHIFTL = 0x1c;
  public static final int SSHIFTR = 0x1d;
  public static final int USHIFTR = 0x1e;
  public static final int JUMP = 0x20;
  public static final int JZ = 0x22;
  public static final int JNZ = 0x23;
  public static final int JEQ = 0x24;
  public static final int JNE = 0x25;
  public static final int JLT = 0x26;
  public static final int JGE = 0x27;
  public static final int JGT = 0x28;
  public static final int JLE = 0x29;
  public static final int JLTU = 0x2a;
  public static final int JGEU = 0x2b;
  public static final int JGTU = 0x2c;
  public static final int JLEU = 0x2d;
  public static final int CALL = 0x30;
  public static final int RETURN = 0x31;
  public static final int CATCH = 0x32;
  public static final int THROW = 0x33;
  public static final int TAILCALL = 0x34;
  public static final int COPY = 0x40;
  public static final int COPYS = 0x41;
  public static final int COPYB = 0x42;
  public static final int SEXS = 0x44;
  public static final int SEXB = 0x45;
  public static final int ALOAD = 0x48;
  public static final int ALOADS = 0x49;
  public static final int ALOADB = 0x4a;
  public static final int ALOADBIT = 0x4b;
  public static final int ASTORE = 0x4c;
  public static final int ASTORES = 0x4d;
  public static final int ASTOREB = 0x4e;
  public static final int ASTOREBIT = 0x4f;
  public static final int STKCOUNT = 0x50;
  public static final int STKPEEK = 0x51;
  public static final int STKSWAP = 0x52;
  public static final int STKROLL = 0x53;
  public static final int STKCOPY = 0x54;
  public static final int STREAMCHAR = 0x70;
  public static final int STREAMNUM = 0x71;
  public static final int STREAMSTR = 0x72;
  public static final int STREAMUNICHAR = 0x73;
  public static final int GESTALT = 0x100;
  public static final int DEBUGTRAP = 0x101;
  public static final int GETMEMSIZE = 0x102;
  public static final int SETMEMSIZE = 0x103;
  public static final int JUMPABS = 0x104;
  public static final int RANDOM = 0x110;
  public static final int SETRANDOM = 0x111;
  public static final int QUIT = 0x120;
  public static final int VERIFY = 0x121;
  public static final int RESTART = 0x122;
  public static final int SAVE = 0x123;
  public static final int RESTORE = 0x124;
  public static final int SAVEUNDO = 0x125;
  public static final int RESTOREUNDO = 0x126;
  public static final int PROTECT = 0x127;
  public static final int GLK = 0x130;
  public static final int GETSTRINGTBL = 0x140;
  public static final int SETSTRINGTBL = 0x141;
  public static final int GETIOSYS = 0x148;
  public static final int SETIOSYS = 0x149;
  public static final int LINEARSEARCH = 0x150;
  public static final int BINARYSEARCH = 0x151;
  public static final int LINKEDSEARCH = 0x152;
  public static final int CALLF = 0x160;
  public static final int CALLFI = 0x161;
  public static final int CALLFII = 0x162;
  public static final int CALLFIII = 0x163;
  public static final int MZERO = 0x170;
  public static final int MCOPY = 0x171;
  public static final int MALLOC = 0x178;
  public static final int MFREE = 0x179;

  public static final int ACCELFUNC = 0x180;
  public static final int ACCELPARAM = 0x181;

  public static final int NUMTOF = 0x190;
  public static final int FTONUMZ = 0x191;
  public static final int FTONUMN = 0x192;

  public static final int CEIL = 0x198;
  public static final int FLOOR = 0x199;

  public static final int FADD = 0x1A0;
  public static final int FSUB = 0x1A1;
  public static final int FMUL = 0x1A2;
  public static final int FDIV = 0x1A3;
  public static final int FMOD = 0x1A4;

  public static final int SQRT = 0x1A8;
  public static final int EXP = 0x1A9;
  public static final int LOG = 0x1AA;
  public static final int POW = 0x1AB;

  public static final int SIN = 0x1B0;
  public static final int COS = 0x1B1;
  public static final int TAN = 0x1B2;
  public static final int ASIN = 0x1B3;
  public static final int ACOS = 0x1B4;
  public static final int ATAN = 0x1B5;
  public static final int ATAN2 = 0x1B6;

  public static final int JFEQ = 0x1C0;
  public static final int JFNE = 0x1C1;
  public static final int JFLT = 0x1C2;
  public static final int JFLE = 0x1C3;
  public static final int JFGT = 0x1C4;
  public static final int JFGE = 0x1C5;

  public static final int JISNAN = 0x1C8;
  public static final int JISINF = 0x1C9;

  public static final int LOAD = 0;
  public static final int STORE = 1;

  public static final int[] L = {LOAD};
  public static final int[] LL = {LOAD, LOAD};
  public static final int[] LLL = {LOAD, LOAD, LOAD};
  public static final int[] LLLL = {LOAD, LOAD, LOAD, LOAD};
  public static final int[] LLLS = {LOAD, LOAD, LOAD, STORE};
  public static final int[] LLLLS = {LOAD, LOAD, LOAD, LOAD, STORE};
  public static final int[] LLLLLLS = 
  {LOAD, LOAD, LOAD, LOAD, LOAD, LOAD, STORE};
  public static final int[] LLLLLLLS = 
  {LOAD, LOAD, LOAD, LOAD, LOAD, LOAD, LOAD, STORE};
  public static final int[] LS = {LOAD, STORE};
  public static final int[] S = {STORE};
  public static final int[] SS = {STORE, STORE};
  public static final int[] SL = {STORE, LOAD};
  public static final int[] LLS = {LOAD, LOAD, STORE};
  public static final int[] LLSS = {LOAD, LOAD, STORE, STORE};
}
