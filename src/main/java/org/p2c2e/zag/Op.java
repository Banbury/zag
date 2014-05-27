package org.p2c2e.zag;

final class Op implements OpConstants
{
  static final Op[] OPS = new Op[0x1CA];

  int arity;
  int[] format;

  Op(int a, int[] f)
  {
    arity = a;
    format = f;
  }

  static
  {
    OPS[NOP] = new Op(0, null);
    OPS[ADD] = new Op(3, LLS);
    OPS[SUB] = new Op(3, LLS);
    OPS[MUL] = new Op(3, LLS);
    OPS[DIV] = new Op(3, LLS);
    OPS[MOD] = new Op(3, LLS);
    OPS[NEG] = new Op(2, LS);
    OPS[BITAND] = new Op(3, LLS);
    OPS[BITOR] = new Op(3, LLS);
    OPS[BITXOR] = new Op(3, LLS);
    OPS[BITNOT] = new Op(2, LS);
    OPS[SHIFTL] = new Op(3, LLS);
    OPS[SSHIFTR] = new Op(3, LLS);
    OPS[USHIFTR] = new Op(3, LLS);
    OPS[JUMP] = new Op(1, L);
    OPS[JZ] = new Op(2, LL);
    OPS[JNZ] = new Op(2, LL);
    OPS[JEQ] = new Op(3, LLL);
    OPS[JNE] = new Op(3, LLL);
    OPS[JLT] = new Op(3, LLL);
    OPS[JGE] = new Op(3, LLL);
    OPS[JGT] = new Op(3, LLL);
    OPS[JLE] = new Op(3, LLL);
    OPS[JLTU] = new Op(3, LLL);
    OPS[JGEU] = new Op(3, LLL);
    OPS[JGTU] = new Op(3, LLL);
    OPS[JLEU] = new Op(3, LLL);
    OPS[CALL] = new Op(3, LLS);
    OPS[RETURN] = new Op(1, L);
    OPS[CATCH] = new Op(2, SL);
    OPS[THROW] = new Op(2, LL);
    OPS[TAILCALL] = new Op(2, LL);
    OPS[COPY] = new Op(2, LS);
    OPS[COPYS] = new Op(2, LS);
    OPS[COPYB] = new Op(2, LS);
    OPS[SEXS] = new Op(2, LS);
    OPS[SEXB] = new Op(2, LS);
    OPS[ALOAD] = new Op(3, LLS);
    OPS[ALOADS] = new Op(3, LLS);
    OPS[ALOADB] = new Op(3, LLS);
    OPS[ALOADBIT] = new Op(3, LLS);
    OPS[ASTORE] = new Op(3, LLL);
    OPS[ASTORES] = new Op(3, LLL);
    OPS[ASTOREB] = new Op(3, LLL);
    OPS[ASTOREBIT] = new Op(3, LLL);
    OPS[STKCOUNT] = new Op(1, S);
    OPS[STKPEEK] = new Op(2, LS);
    OPS[STKSWAP] = new Op(0, null);
    OPS[STKROLL] = new Op(2, LL);
    OPS[STKCOPY] = new Op(1, L);
    OPS[STREAMCHAR] = new Op(1, L);
    OPS[STREAMNUM] = new Op(1, L);
    OPS[STREAMSTR] = new Op(1, L);
    OPS[STREAMUNICHAR] = new Op(1, L);
    OPS[GESTALT] = new Op(3, LLS);
    OPS[DEBUGTRAP] = new Op(1, L);
    OPS[GETMEMSIZE] = new Op(1, S);
    OPS[SETMEMSIZE] = new Op(2, LS);
    OPS[JUMPABS] = new Op(1, L);
    OPS[RANDOM] = new Op(2, LS);
    OPS[SETRANDOM] = new Op(1, L);
    OPS[QUIT] = new Op(0, null);
    OPS[VERIFY] = new Op(1, S);
    OPS[RESTART] = new Op(0, null);
    OPS[SAVE] = new Op(2, LS);
    OPS[RESTORE] = new Op(2, LS);
    OPS[SAVEUNDO] = new Op(1, S);
    OPS[RESTOREUNDO] = new Op(1, S);
    OPS[PROTECT] = new Op(2, LL);
    OPS[GLK] = new Op(3, LLS);
    OPS[GETSTRINGTBL] = new Op(1, S);
    OPS[SETSTRINGTBL] = new Op(1, L);
    OPS[GETIOSYS] = new Op(2, SS);
    OPS[SETIOSYS] = new Op(2, LL);
    OPS[LINEARSEARCH] = new Op(8, LLLLLLLS);
    OPS[BINARYSEARCH] = new Op(8, LLLLLLLS);
    OPS[LINKEDSEARCH] = new Op(7, LLLLLLS);
    OPS[CALLF] = new Op(2, LS);
    OPS[CALLFI] = new Op(3, LLS);
    OPS[CALLFII] = new Op(4, LLLS);
    OPS[CALLFIII] = new Op(5, LLLLS);
    OPS[MZERO] = new Op(2, LL);
    OPS[MCOPY] = new Op(3, LLL);
    OPS[MALLOC] = new Op(2, LS);
    OPS[MFREE] = new Op(1, L);

    OPS[ACCELFUNC] = new Op(2, LL);
    OPS[ACCELPARAM] = new Op(2, LL);

    OPS[NUMTOF] = new Op(2, LS);
    OPS[FTONUMZ] = new Op(2, LS);
    OPS[FTONUMN] = new Op(2, LS);
    OPS[CEIL] = new Op(2, LS);
    OPS[FLOOR] = new Op(2, LS);

    OPS[FADD] = new Op(3, LLS);
    OPS[FSUB] = new Op(3, LLS);
    OPS[FMUL] = new Op(3, LLS);
    OPS[FDIV] = new Op(3, LLS);
    OPS[FMOD] = new Op(4, LLSS);

    OPS[SQRT] = new Op(2, LS);
    OPS[EXP] = new Op(2, LS);
    OPS[LOG] = new Op(2, LS);
    OPS[POW] = new Op(3, LLS);

    OPS[SIN] = new Op(2, LS);
    OPS[COS] = new Op(2, LS);
    OPS[TAN] = new Op(2, LS);
    OPS[ASIN] = new Op(2, LS);
    OPS[ACOS] = new Op(2, LS);
    OPS[ATAN] = new Op(2, LS);
    OPS[ATAN2] = new Op(3, LLS);

    OPS[JFEQ] = new Op(4, LLLL);
    OPS[JFNE] = new Op(4, LLLL);
    OPS[JFLT] = new Op(3, LLL);
    OPS[JFLE] = new Op(3, LLL);
    OPS[JFGT] = new Op(3, LLL);
    OPS[JFGE] = new Op(3, LLL);

    OPS[JISNAN] = new Op(2, LL);
    OPS[JISINF] = new Op(2, LL);

  }
}
