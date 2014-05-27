package org.p2c2e.zag;

import java.io.*;
import java.lang.Math;
import java.lang.System;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import org.p2c2e.util.FastByteBuffer;
import org.p2c2e.zing.Glk;

public final class Zag implements OpConstants
{
  File gamefile;
  int fileStartPos;

  // memory
  FastByteBuffer memory;

  // stack
  FastByteBuffer stack;

  IO io;

  boolean running;

  int ramstart;
  int extstart;
  int endmem;
  int stacksize;

  int protectstart;
  int protectend;

  int pc;

  int sp;
  int fp;
  int lp;
  int vp;

  int[] funargs = new int[32];

  Random rand = new Random();

  Heap heap;

  HashMap accelTable = new HashMap();

    /* accel params  */
    int classes_table;
    int indiv_prop_start;
    int class_metaclass;
    int object_metaclass;
    int routine_metaclass;
    int string_metaclass;
    int self;
    int num_attr_bytes;
    int cpv_start;

  public Zag(File gf, int iStart) throws IOException
  {
    gamefile = gf;
    fileStartPos = iStart;
    if (verify(true) != 0)
      throw new IOException("Gamefile failed checksum.");
    init();
  }

  void start()
  {
    running = true;
    enterFunction(memory, pc, 0, null);
    exec();
  }

  void init() throws IOException
  {
    int b;
    FastByteBuffer buf;
    DataInputStream in;
    RandomAccessFile f = new RandomAccessFile(gamefile, "r");
    f.seek(fileStartPos);

    if (f.read() == 'G' && f.read() == 'l' && f.read() == 'u' && f.read() == 'l')
    {
      f.seek(fileStartPos + 12);
      extstart = f.readInt();
      endmem = f.readInt();
      buf = new FastByteBuffer(endmem);
      buf.setMinSize(endmem);

      f.seek(fileStartPos);
      buf.limit(extstart);
      f.getChannel().read(buf.asByteBuffer());
      buf.clear();
      f.close();

      for (int j = extstart; j < endmem; j++)
        buf.put(j, (byte) 0);

      if (memory != null)
      {
        for (int i = protectstart; i < protectend; i++)
          buf.put(i, memory.get(i));
      }

      memory = buf;
      heap = new Heap(this);

      ramstart = buf.getInt(8);
      stacksize = buf.getInt(20);
      stack = new FastByteBuffer(stacksize);
      sp = fp = lp = vp = 0;

      pc = buf.getInt(24);

      if (io == null)
        io = new IO(this);
      else
        io.init(this);
    }
    else
    {
      throw new IOException("Not a glulx file.");
    }
  }

  private final void storeOperand(FastByteBuffer mem, int mode, 
                                int addr, float val) {

      storeOperand(mem, mode, addr, Float.floatToRawIntBits(val));
  }

  private final void storeOperand(FastByteBuffer mem, int mode, 
                                int addr, double val) {

      storeOperand(mem, mode, addr, Float.floatToRawIntBits((float)val));
  }

  private final void storeOperand(FastByteBuffer mem, int mode, 
                                  int addr, int val)
  {
    switch(mode)
    {
    case 0:
      break;
    case 1:
      mem.putInt(addr, val);
      break;
    case 2:
      stack.putInt(addr + lp, val);
      break;
    case 3:
      stack.putInt(sp, val);
      sp += 4;
      break;
    default:
      fatal("storing illegal operand");
    }
  }

  private final void storeShortOperand(FastByteBuffer mem, int mode, 
                                       int addr, int val)
  {
    short s = (short) val;

    switch(mode)
    {
    case 0:
      break;
    case 1:
      mem.putShort(addr, s);
      break;
    case 2:
      stack.putShort(addr + lp, s);
      break;
    case 3:
      stack.putInt(sp, ((int) s) & 0xffff);
      sp += 4;
      break;
    default:
      fatal("storing illegal operand");
    }
  }

  private final void storeByteOperand(FastByteBuffer mem, int mode, 
                                      int addr, int val)
  {
    byte b = (byte) val;

    switch(mode)
    {
    case 0:
      break;
    case 1:
      mem.put(addr, b);
      break;
    case 2:
      stack.put(addr + lp, b);
      break;
    case 3:
      stack.putInt(sp, ((int) b) & 0xff);
      sp += 4;
      break;
    default:
      fatal("storing illegal operand");
    }
  }

  private final void parseOperands(Op op, FastByteBuffer mem, int[] modes, 
                                   int[] values)
  {
    int i;
    int rawmode = 0;
    int arity = op.arity;
    int[] format = op.format;
    int modeaddr = mem.position();

    mem.position(modeaddr + ((arity + 1) / 2));

    for (i = 0; i < arity; i++)
    {
      if ((i & 1) == 0)
      {
        rawmode = (int) mem.get(modeaddr);
        modes[i] = rawmode & 0x0f;
      }
      else
      {
        modes[i] = (rawmode >> 4) & 0x0f;
        modeaddr++;
      }

      if (format[i] == LOAD)
      {
        switch(modes[i])
        {
        case 0:
          values[i] = 0;
          break;
        case 1:
          values[i] = (int) mem.get();
          break;
        case 2:
          values[i] = (int) mem.getShort();
          break;
        case 3:
          values[i] = mem.getInt();
          break;
        case 5:
          values[i] = mem.getInt(((int) mem.get()) & 0xff);
          break;
        case 6:
          values[i] = mem.getInt(((int) mem.getShort()) & 0xffff);
          break;
        case 7:
          values[i] = mem.getInt(mem.getInt());
          break;
        case 8:
          sp = sp - 4;
          values[i] = stack.getInt(sp);
          break;
        case 9:
          values[i] = stack.getInt(lp + (0xff & ((int) mem.get())));
          break;
        case 10:
          values[i] = stack.getInt(lp + (0xffff & ((int) mem.getShort())));
          break;
        case 11:
          values[i] = stack.getInt(lp + mem.getInt());
          break;
        case 13:
          values[i] = mem.getInt((((int) mem.get()) & 0xff) + ramstart);
          break;
        case 14:
          values[i] = mem.getInt((((int) mem.getShort()) & 0xffff) + ramstart);
          break;
        case 15:
          values[i] = mem.getInt(mem.getInt() + ramstart);
          break;
        default:
          fatal("Non-existent addressing mode: " + modes[i]);
        }
      }
      else
      {
        switch(modes[i])
        {
        case 0:
          values[i] = 0;
          break;
        case 8:
          modes[i] = 3;
          values[i] = 0;
          break;
        case 5:
          modes[i] = 1;
          values[i] = ((int) mem.get()) & 0xff;
          break;
        case 9:
          modes[i] = 2;
          values[i] = ((int) mem.get()) & 0xff;
          break;
        case 13:
          modes[i] = 1;
          values[i] = ramstart + (((int) mem.get()) & 0xff);
          break;
        case 6:
          modes[i] = 1;
          values[i] = ((int) mem.getShort()) & 0xffff;
          break;
        case 10:
          modes[i] = 2;
          values[i] = ((int) mem.getShort()) & 0xffff;
          break;
        case 14:
          modes[i] = 1;
          values[i] = ramstart + (((int) mem.getShort()) & 0xffff);
          break;
        case 7:
          modes[i] = 1;
          values[i] = mem.getInt();
          break;
        case 11:
          modes[i] = 2;
          values[i] = mem.getInt();
          break;
        case 15:
          modes[i] = 1;
          values[i] = ramstart + mem.getInt();
          break;
        default:
          fatal("Non-existent addressing mode (store): " + modes[i]);
        }
      }
    }
  }

  private final void exec()
  {
    int opcode = 0;
    int val;
    int addr;
    int i;
    FastByteBuffer mem;
    FastByteBuffer stack;
    int[] values = new int[10];
    int[] modes = new int[10];

    float f, fa, fb, fc;

    while(running)
    {
      mem = this.memory;
      stack = this.stack;
      mem.position(pc);
      opcode = ((int)mem.get()) & 0xff;
      if ((opcode & 0x80) != 0)
      {
        if ((opcode & 0x40) != 0)
        {
          opcode &= 0x3f;
          opcode = (opcode << 8) | ((int)mem.get()) & 0xff;
          opcode = (opcode << 8) | ((int)mem.get()) & 0xff;
          opcode = (opcode << 8) | ((int)mem.get()) & 0xff;
        }
        else
        {
          opcode &= 0x7f;
          opcode = (opcode << 8) | ((int)mem.get()) & 0xff;
        }
      }

      if (Op.OPS[opcode] == null) {
          System.out.println("bad op: " + opcode);
      }
      if (Op.OPS[opcode].format != null)
        parseOperands(Op.OPS[opcode], mem, modes, values);
      pc = mem.position();

      switch(opcode)
      {
      case NOP:
        break;
      case ADD:
        storeOperand(mem, modes[2], values[2], values[0] + values[1]);
        break;
      case SUB:
        storeOperand(mem, modes[2], values[2], values[0] - values[1]);
        break;
      case MUL:
        storeOperand(mem, modes[2], values[2], values[0] * values[1]);
        break;
      case DIV:
        if (values[1] == 0)
          fatal("Division by zero.");
        storeOperand(mem, modes[2], values[2], values[0] / values[1]);
        break;
      case MOD:
        if (values[1] == 0)
          fatal("Mod by zero.");
        storeOperand(mem, modes[2], values[2], values[0] % values[1]);
        break;
      case NEG:
        storeOperand(mem, modes[1], values[1], 0 - values[0]);
        break;
      case BITAND:
        storeOperand(mem, modes[2], values[2], values[0] & values[1]);
        break;
      case BITOR:
        storeOperand(mem, modes[2], values[2], values[0] | values[1]);
        break;
      case BITXOR:
        storeOperand(mem, modes[2], values[2], values[0] ^ values[1]);
        break;
      case BITNOT:
        storeOperand(mem, modes[1], values[1], ~values[0]);
        break;
      case SHIFTL:
        val = ((values[1] & 0xff) > 31) ? 0 : values[0] << (values[1] & 0xff);
        storeOperand(mem, modes[2], values[2], val);
        break;
      case USHIFTR:
        val = ((values[1] & 0xff) > 31) ? 0 : values[0] >>> (values[1] & 0xff);
        storeOperand(mem, modes[2], values[2], val);
        break;
      case SSHIFTR:
        if (values[1] >= 32) {
            val = (values[0] & 0x80000000) == 0 ? 0 : 0xffffffff;
        } else {
            val = values[0] >> (values[1] & 0xff);
        }
        storeOperand(mem, modes[2], values[2], val);
        break;
      case JUMP:
        handleRelativeJump(values[0]);
        break;
      case JZ:
        if (values[0] == 0)
          handleRelativeJump(values[1]);
        break;
      case JNZ:
        if (values[0] != 0)
          handleRelativeJump(values[1]);
        break;
      case JEQ:
        if (values[0] == values[1])
          handleRelativeJump(values[2]);
        break;
      case JNE:
        if (values[0] != values[1])
          handleRelativeJump(values[2]);
        break;
      case JLT:
        if (values[0] < values[1])
          handleRelativeJump(values[2]);
        break;
      case JLE:
        if (values[0] <= values[1])
          handleRelativeJump(values[2]);
        break;
      case JGT:
        if (values[0] > values[1])
          handleRelativeJump(values[2]);
        break;
      case JGE:
        if (values[0] >= values[1])
          handleRelativeJump(values[2]);
        break;
      case JLTU:
        if (((long) values[0] & 0xffffffffl) < ((long) values[1] & 0xffffffffl))
          handleRelativeJump(values[2]);
        break;
      case JLEU:
        if (((long) values[0] & 0xffffffffl) <= ((long) values[1] & 0xffffffffl))
          handleRelativeJump(values[2]);
        break;
      case JGTU:
        if (((long) values[0] & 0xffffffffl) > ((long) values[1] & 0xffffffffl))
          handleRelativeJump(values[2]);
        break;
      case JGEU:
        if (((long) values[0] & 0xffffffffl) >= ((long) values[1] & 0xffffffffl))
          handleRelativeJump(values[2]);
        break;
      case JUMPABS:
        pc = values[0];
        break;
      case COPY:
        storeOperand(mem, modes[1], values[1], values[0]);
        break;
      case COPYS:
          switch(modes[0]) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 8:
              val = values[0];
              break;
          case 5:
          case 6:
          case 7:
          case 13:
          case 14:
          case 15:
              val = values[0] >> 16;
              break;
          //these are unsupported; we'll just do something
          case 9:
          case 10:
          case 11:
          default:

              val = values[0];

          }
        storeShortOperand(mem, modes[1], values[1], val);
        break;
      case COPYB:
          switch(modes[0]) {
          case 0:
          case 1:
          case 2:
          case 3:
          case 8:
              val = values[0];
              break;
          case 5:
          case 6:
          case 7:
          case 13:
          case 14:
          case 15:
              val = values[0] >> 24;
              break;
          //these are unsupported; we'll just do something
          case 9:
          case 10:
          case 11:
          default:

              val = values[0];

          }
        storeByteOperand(mem, modes[1], values[1], val);
        break;
      case SEXS:
        val = ((values[0] & 0x8000) != 0)
          ? (values[0] | 0xffff0000) : (values[0] & 0x0000ffff);
        storeOperand(mem, modes[1], values[1], val);
        break;
      case SEXB:
        val = ((values[0] & 0x80) != 0)
          ? (values[0] | 0xffffff00) : (values[0] & 0x000000ff);
        storeOperand(mem, modes[1], values[1], val);
        break;
      case ALOAD:
        storeOperand(mem, modes[2], values[2], 
                     mem.getInt(values[0] + (4 * values[1])));
        break;
      case ALOADS:
        storeOperand(mem, modes[2], values[2],
                     ((int) mem.getShort(values[0] + (2 * values[1]))) & 0xffff);
        break;
      case ALOADB:
        storeOperand(mem, modes[2], values[2], 
                     ((int) mem.get(values[0] + values[1])) & 0xff);
        break;
      case ALOADBIT:
        addr = values[0] + (values[1] / 8);
        val = values[1] % 8;

        if (val < 0)
        {
          addr--;
          val += 8;
        }

        storeOperand(mem, modes[2], values[2], 
                     ((mem.get(addr) & (byte) (1 << val)) != 0) ? 1 : 0);
        break;
      case ASTORE:
        storeOperand(mem, 1, values[0] + (4 * values[1]), values[2]);
        break;
      case ASTORES:
        storeShortOperand(mem, 1, values[0] + (2 * values[1]), values[2]);
        break;
      case ASTOREB:
        storeByteOperand(mem, 1, values[0] + values[1], values[2]);
        break;
      case ASTOREBIT:
        addr = values[0] + (values[1] / 8);
        val = values[1] % 8;

        if (val < 0)
        {
          addr--;
          val += 8;
        }

        if (values[2] == 0)
          mem.put(addr,(byte) (mem.get(addr) & ((byte) ~(1 << val))));
        else
          mem.put(addr, (byte) (mem.get(addr) | (byte) (1 << val)));
        break;
      case STKCOUNT:
        storeOperand(mem, modes[0], values[0], (sp - vp) / 4);
        break;
      case STKPEEK:
        if (values[0] < 0 || values[0] >= ((sp - vp) / 4))
          fatal("stkpeek: outside valid stack range");

        storeOperand(mem, modes[1], values[1], 
                     stack.getInt(sp - (4 * (values[0] + 1))));
        break;
      case STKSWAP:
        if (sp - vp < 8)
          fatal("Must be at least two values on the stack to execute stkswap.");

        val = stack.getInt(sp - 4);
        addr = stack.getInt(sp - 8);
        stack.putInt(sp - 8, val);
        stack.putInt(sp - 4, addr);
        break;
      case STKCOPY:
        if (sp - vp < 4 * values[0])
          fatal("Cannot copy " + values[0] + " stack items.  Stack too small.");
        for (i = 0; i < values[0]; i++)
          stack.putInt(sp + (4 * i), stack.getInt(sp - (4 * (values[0] - i))));
        sp += 4 * values[0];
        break;
      case STKROLL:
        if (values[0] < 0)
          fatal("Cannot roll negative number of stack entries.");
        if (((sp - vp) / 4) < values[0])
          fatal("Cannot roll more stack values than there are on the stack.");

        /* Algorithm thanks to Andrew Plotkin... */

        if (values[0] == 0)
          break;

        if (values[1] > 0)
          val = values[0] - (values[1] % values[0]);
        else
          val = (-values[1]) % values[0];

        if (val == 0)
          break;

        addr = sp - (4 * values[0]);
        for (i = 0; i < val; i++)
          stack.putInt(sp + (4 * i), stack.getInt(addr + (4 * i)));
        for (i = 0; i < values[0]; i++)
          stack.putInt(addr + (4 * i), stack.getInt(addr + (4 * (val + i))));
        break;
      case CALL:
        popArguments(values[1]);
        pushCallstub(modes[2], values[2]);
        enterFunction(mem, values[0], values[1], funargs);
        break;
      case RETURN:
        leaveFunction();
        if (sp == 0)
          running = false;
        else
          popCallstub(values[0]);
        break;
      case TAILCALL:
        popArguments(values[1]);
        leaveFunction();
        enterFunction(mem, values[0], values[1], funargs);
        break;
      case CATCH:
        pushCallstub(modes[0], values[0]);
        storeOperand(mem, modes[0], values[0], sp);
        handleRelativeJump(values[1]);
        break;
      case THROW:
        sp = values[1];
        popCallstub(values[0]);
        break;
      case STREAMCHAR:
        io.streamChar(this, values[0] & 0xff);
        break;
      case STREAMNUM:
        io.streamNum(this, values[0], false, 0);
        break;
      case STREAMSTR:
        io.streamString(this, values[0], 0, 0);
        break;
      case STREAMUNICHAR:
        io.streamUniChar(this, values[0]);
        break;
      case GESTALT:
        storeOperand(mem, modes[2], values[2], gestalt(values[0], values[1]));
        break;
      case DEBUGTRAP:
        fatal("debugtrap executed.");
        break;
      case CALLF:
        pushCallstub(modes[1], values[1]);
        enterFunction(mem, values[0], 0, funargs);
        break;
      case CALLFI:
        funargs[0] = values[1];
        pushCallstub(modes[2], values[2]);
        enterFunction(mem, values[0], 1, funargs);
        break;
      case CALLFII:
        funargs[0] = values[1];
        funargs[1] = values[2];
        pushCallstub(modes[3], values[3]);
        enterFunction(mem, values[0], 2, funargs);
        break;
      case CALLFIII:
        funargs[0] = values[1];
        funargs[1] = values[2];
        funargs[2] = values[3];
        pushCallstub(modes[4], values[4]);
        enterFunction(mem, values[0], 3, funargs);
        break;
      case GETMEMSIZE:
        storeOperand(mem, modes[0], values[0], getMemSize());
        break;
      case SETMEMSIZE:
        if (heap.isActive()) {
          System.out.println("Unable to set mem size while heap is active");
          return;
        }
        storeOperand(mem, modes[1], values[1], setMemSize(values[0]));
        break;
      case GETSTRINGTBL:
        if (io.htree != null)
          storeOperand(mem, modes[0], values[0], io.htree.startaddr);
        else
          storeOperand(mem, modes[0], values[0], 0);
        break;
      case SETSTRINGTBL:
        io.htree = new HuffmanTree(this, values[0]);
        break;
      case GETIOSYS:
        storeOperand(mem, modes[0], values[0], io.sys);
        storeOperand(mem, modes[1], values[1], io.rock);
        break;
      case SETIOSYS:
        io.setSys(values[0], values[1]);
        break;
      case RANDOM:
        if (values[0] == 0)
          storeOperand(mem, modes[1], values[1], rand.nextInt());
        else if (values[0] < 0)
          storeOperand(mem, modes[1], values[1], 0 - rand.nextInt(0 - values[0]));
        else
          storeOperand(mem, modes[1], values[1], rand.nextInt(values[0]));
        break;
      case SETRANDOM:
        rand.setSeed((long) values[0]);
        break;
      case LINEARSEARCH:
        val = linearSearch(values[0], values[1], values[2], values[3], values[4],
                           values[5], values[6]);
        storeOperand(mem, modes[7], values[7], val);
        break;
      case BINARYSEARCH:
        val = binarySearch(values[0], values[1], values[2], values[3], values[4],
                           values[5], values[6]);
        storeOperand(mem, modes[7], values[7], val);
        break;
      case LINKEDSEARCH:
        val = linkedSearch(values[0], values[1], values[2], values[3], values[4],
                           values[5]);
        storeOperand(mem, modes[6], values[6], val);
        break;
      case QUIT:
        running = false;
        break;
      case VERIFY:
        storeOperand(mem, modes[0], values[0], verify(false));
        break;
      case RESTART:
        try {
          init();
        } catch (IOException eRestart) {
          System.err.println(eRestart);
          fatal("Could not restart!");
        }
        enterFunction(memory, pc, 0, null);
        break;
      case SAVE:
        pushCallstub(modes[1], values[1]);
        val = io.saveGame(this, values[0]);
        popCallstub(val);
        break;
      case RESTORE:
        val = io.restoreGame(this, values[0]);
        if (val == 0)
          popCallstub(-1);
        else
          storeOperand(mem, modes[1], values[1], val);
        break;
      case SAVEUNDO:
        pushCallstub(modes[0], values[0]);
        val = io.saveUndo(this);
        popCallstub(val);
        break;
      case RESTOREUNDO:
        val = io.restoreUndo(this);
        if (val == 0)
          popCallstub(-1);
        else
          storeOperand(mem, modes[0], values[0], val);
        break;
      case PROTECT:
        protectstart = values[0];
        protectend = values[0] + values[1];
        break;
      case GLK:
        popArguments(values[1]);
        val = io.glk(this, values[0], values[1], funargs);
        storeOperand(mem, modes[2], values[2], val);
        break;
      case MZERO:
          Arrays.fill(mem.array(), values[1], values[0] + values[1], (byte)0);
          break;
      case MCOPY:
          System.arraycopy(mem.array(), values[1], mem.array(), values[2], values[0]);
          break;
      case MALLOC:
          int allocated = heap.malloc(values[0]);
          storeOperand(mem, modes[1], values[1], allocated);
          break;
      case MFREE:
          heap.free(values[0]);
          break;
      case ACCELFUNC:
          accel(values[0], values[1]);
          break;
      case ACCELPARAM:
          int index = values[0];
          val = values[1];
          switch (index) {
          case 0: classes_table = val; break;
          case 1: indiv_prop_start = val; break;
          case 2: class_metaclass = val; break;
          case 3: object_metaclass = val; break;
          case 4: routine_metaclass = val; break;
          case 5: string_metaclass = val; break;
          case 6: self = val; break;
          case 7: num_attr_bytes = val; break;
          case 8: cpv_start = val; break;
          }
          break;
      case NUMTOF:
          f = (float)values[0];
          storeOperand(mem, modes[1], values[1], f);
          break;
      case FTONUMZ:
          f = Float.intBitsToFloat(values[0]);
          if (Float.isNaN(f)) {
              val = 0x7fffffff + (values[0] >>> 31);
          } else {
              val = (int) f;
          }
          storeOperand(mem, modes[1], values[1], val);
          break;
      case FTONUMN:
          f = Float.intBitsToFloat(values[0]);
          if (Float.isNaN(f)) {
              val = 0x7fffffff + (values[0] >>> 31);
          } else {
              val = Math.round(f);
          }
          storeOperand(mem, modes[1], values[1], val);
          break;
      case CEIL:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.ceil(f));
          break;
      case FLOOR:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.floor(f));
          break;
      case FADD:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          storeOperand(mem, modes[2], values[2], fa+fb);
          break;
      case FSUB:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          storeOperand(mem, modes[2], values[2], fa-fb);
          break;
      case FMUL:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          storeOperand(mem, modes[2], values[2], fa*fb);
          break;
      case FDIV:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          storeOperand(mem, modes[2], values[2], fa/fb);
          break;
      case FMOD:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);

          if (Float.isInfinite(fa) || fb == 0.0f) {
              storeOperand(mem, modes[2], values[2], Float.NaN);
              storeOperand(mem, modes[3], values[3], Float.NaN);
          } else {
              float remainder = fa%fb;
              int quotient = Float.floatToRawIntBits((fa-remainder) / fb);
              if (quotient == 0x0 || quotient == 0x80000000)
                  quotient = (values[0] ^ values[1]) & 0x80000000;
              storeOperand(mem, modes[2], values[2], remainder);
              storeOperand(mem, modes[3], values[3], quotient);
          }
          break;
      case SQRT:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.sqrt(f));
          break;
      case EXP:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.exp(f));
          break;
      case LOG:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.log(f));
          break;
      case POW:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          if ((fa == -1 && Float.isInfinite(fb)) || fa == 1) {
              storeOperand(mem, modes[2], values[2], 1.0f);
          } else { 
              storeOperand(mem, modes[2], values[2], Math.pow(fa,fb));
          }
          break;
      case SIN:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.sin(f));
          break;
      case COS:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.cos(f));
          break;
      case TAN:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.tan(f));
          break;
      case ASIN:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.asin(f));
          break;
      case ACOS:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.acos(f));
          break;
      case ATAN:
          f = Float.intBitsToFloat(values[0]);
          storeOperand(mem, modes[1], values[1], Math.atan(f));
          break;
      case ATAN2:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          storeOperand(mem, modes[2], values[2], Math.atan2(fa,fb));
          break;
      case JFEQ:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          fc = Float.intBitsToFloat(values[2]);

          if (!Float.isNaN(fc)) {
              if (Float.isInfinite(fa) && Float.isInfinite(fb)) {
                  if (fa == fb) {
                      handleRelativeJump(values[3]);
                  }
              } else {
                  if (Math.abs(fa-fb) <= Math.abs(fc)) {
                      handleRelativeJump(values[3]);
                  }
              }
          }
          break;
      case JFNE:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          fc = Float.intBitsToFloat(values[2]);

          if (Float.isNaN(fc)) {
              handleRelativeJump(values[3]);
          } else {
              if (Float.isInfinite(fa) && Float.isInfinite(fb)) {
                  if (fa != fb) {
                      handleRelativeJump(values[3]);
                  }
              } else {
                  float diff = fa-fb;
                  fc = Math.abs(fc);
                  if (!(diff <= fc && diff >= -fc)) {
                      handleRelativeJump(values[3]);
                  }
              }
          }
          break;
      case JFGT:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          if (fa > fb)
              handleRelativeJump(values[2]);
        break;
      case JFGE:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          if (fa >= fb)
              handleRelativeJump(values[2]);
        break;
      case JFLT:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          if (fa < fb)
              handleRelativeJump(values[2]);
        break;
      case JFLE:
          fa = Float.intBitsToFloat(values[0]);
          fb = Float.intBitsToFloat(values[1]);
          if (fa <= fb)
              handleRelativeJump(values[2]);
        break;
      case JISNAN:
          f = Float.intBitsToFloat(values[0]);
          if (Float.isNaN(f))
              handleRelativeJump(values[1]);
        break;
      case JISINF:
          f = Float.intBitsToFloat(values[0]);
          if (Float.isInfinite(f))
              handleRelativeJump(values[1]);
        break;
 
      }
    }
  }

  void accel(int number, int addr) {
      switch(number) {
      case 0:
          accelTable.remove(addr);
          break;
      case 1:
          accelTable.put(addr, new Func1ZRegion(this));
          break;
      case 2:
          accelTable.put(addr, new Func2CPTab(this));
          break;
      case 3:
          accelTable.put(addr, new Func3RAPr(this));
          break;
      case 4:
          accelTable.put(addr, new Func4RLPr(this));
          break;
      case 5:
          accelTable.put(addr, new Func5OCCl(this));
          break;
      }
  }

  int getMemSize() {
    return memory.limit();
  }

  int verify(boolean progress)
  {
    int len;
    int check;
    int sum = 0;
    int val;
    boolean okay = true;
    RandomAccessFile f;
    DataInputStream in;

    try
    {
      f = new RandomAccessFile(gamefile, "r");
      f.seek(fileStartPos);
      okay &= ((char) f.read()) == 'G';
      okay &= ((char) f.read()) == 'l';
      okay &= ((char) f.read()) == 'u';
      okay &= ((char) f.read()) == 'l';

      f.seek(fileStartPos + 12);
      len = f.readInt();
      okay &= f.length() >= (long) (fileStartPos + len);

      f.seek(fileStartPos + 32);
      check = f.readInt();

      f.seek(fileStartPos);
      in = new DataInputStream(new BufferedInputStream(new FileInputStream(f.getFD())));
      for (int i = 0; i < len / 4; i++)
      {
        if (progress && ((i & 0x100) != 0))
          Glk.progress("Verifying file...", 0, (len / 4), i);

        val = in.readInt();
        sum += (i == 8) ? 0 : val;
      }
      in.close();
      f.close();

      if (progress)
        Glk.progress(null, 0, 0, 0);

      okay &= (sum == check);

      return (okay) ? 0 : 1;
    }
    catch (Exception e)
    {
      return 1;
    }
  }

  private final int linkedSearch(int key, int keySize, int start, 
                                 int keyOffset, int nextOffset, int options)
  {
    byte curKeyByte;
    int curKey = 0;
    boolean found;
    boolean zeroKey;
    int nextAddr;
    FastByteBuffer b = memory;
    boolean keyIndirect = (options & 0x01) != 0;
    boolean zeroTerm = ((options & 0x02) != 0);

    if (keySize < 1 || start < 0)
      fatal("Illegal argument(s) to linkedsearch.");

    while (true)
    {
      found = true;
      zeroKey = true;

      if (keyIndirect)
      {
        for (int j = 0; j < keySize; j++)
        {
          curKeyByte = b.get(start + keyOffset + j);
          found &= (curKeyByte == b.get(key + j));
          if (curKeyByte != (byte) 0)
            zeroKey = false;
        }
      }
      else
      {
        switch(keySize)
        {
        case 1:
          curKey = ((int) b.get(start + keyOffset)) & 0xff;
          found = ((key & 0xff) == curKey);
          break;
        case 2:
          curKey = ((int) b.getShort(start + keyOffset)) & 0xffff;
          found = ((key & 0xffff) == curKey);
          break;
        case 3:
          curKey = (b.getInt(start + keyOffset) >>> 8);
          found = ((key & 0xffffff) == curKey);
        case 4:
          curKey = b.getInt(start + keyOffset);
          found = (key == curKey);
          break;
        default:
          fatal("Illegal key size for direct linkedsearch.");
        }
        zeroKey = (curKey == 0);
      }

      if (found)
        break;

      if (zeroTerm && zeroKey) {
          return 0;
      }

      nextAddr = b.getInt(start + nextOffset);
      if (nextAddr == 0)
        break;

      start = nextAddr;
    }

    if (found)
      return start;
    else
      return 0;
  }


  public final int binarySearch(int key, int keySize, int start, 
                                 int structSize, int numStructs, 
                                 int keyOffset, int options)
  {
    int curKeyByte;
    int curKey;
    int curStart = start;
    int diff = -1;
    int i = 0;
    int bottom = 0;
    int top = numStructs;
    FastByteBuffer b = memory;
    boolean keyIndirect = (options & 0x01) != 0;
    boolean returnIndex = (options & 0x04) != 0;

    if (keySize < 1 || start < 0 || structSize < 1 || numStructs < 0)
      fatal("Illegal argument(s) to binarysearch.");

    while ((top - bottom) > 0)
    {
      diff = 0;
      i = bottom + ((top - bottom) / 2);
      curStart = start + (i * structSize);

      if (keyIndirect)
      {
        for (int j = 0; j < keySize; j++)
        {
          curKeyByte = ((int) b.get(curStart + keyOffset + j)) & 0xff;
          diff = (((int) b.get(key + j)) & 0xff) - curKeyByte;
          if (diff != 0)
            break;
        }
      }
      else
      {
        switch(keySize)
        {
        case 1:
          curKey = ((int) b.get(curStart + keyOffset)) & 0xff;
          diff = (key & 0xff) - curKey;
          break;
        case 2:
          curKey = ((int) b.getShort(curStart + keyOffset)) & 0xffff;
          diff = (key & 0xffff) - curKey;
          break;
        case 3:
          //FIXME: illegal? -dmt
          curKey = (b.getInt(curStart + keyOffset) >>> 8);
          diff = (key & 0xffffff) - curKey;
          break;
        case 4:
          curKey = b.getInt(curStart + keyOffset);
          //fuck Java's lack of unsigned ints.
          /*
            a - b

            if both are positive, all is good
            if both are negative, likewise

            if a is positive and b is negative, wrapping is possible

            if a is negative and b is positive, wrapping is possible

           */
          int curSign = curKey & 0x80000000;
          int keySign = key & 0x80000000;

          if (curSign == keySign) {
              diff = key - curKey;
          } else {
              diff = (curSign >> 1) - (keySign >> 1);
          }
          break;
        default:
          fatal("Illegal key size for direct binarysearch.");
        }
      }

      if (diff == 0)
        break;

      if (diff < 0)
        top = i;
      else
        bottom = i + 1;
    }

    if (diff == 0)
    {
      if (returnIndex)
        return i;
      else
        return curStart;
    }
    else
    {
      if (returnIndex)
        return -1;
      else
        return 0;
    }
  }

  private final int linearSearch(int key, int keySize, int start, 
                                 int structSize, int numStructs, 
                                 int keyOffset, int options)
  {
    int i = 0;
    int curKey;
    byte curKeyByte;
    boolean found = false;
    boolean zeroKey = false;
    boolean keyIndirect = (options & 0x01) != 0;
    boolean zeroTerm = (options & 0x02) != 0;
    boolean returnIndex = (options & 0x04) != 0;
    FastByteBuffer b = memory;

    if (numStructs == 0xffffffff) {
        numStructs = 0x7fffffff; //make positive
    }

    if (keySize < 1 || start < 0 || structSize < 1)
      fatal("Illegal argument(s) to linearseach.");

    while (numStructs < 0 || i < numStructs)
    {
      found = true;
      zeroKey = true;
      if (keyIndirect)
      {
        for (int j = 0; j < keySize; j++)
        {
          curKeyByte = b.get(start + keyOffset + j);
          if (curKeyByte != (byte) 0)
            zeroKey = false;

          if (curKeyByte != b.get(key + j))
          {
            found = false;
            break;
          }
        }
      }
      else
      {
        switch(keySize)
        {
        case 1:
          curKey = ((int) b.get(start + keyOffset)) & 0xff;
          found = ((key & 0xff) == curKey);
          zeroKey = (curKey == 0);
          break;
        case 2:
          curKey = ((int) b.getShort(start + keyOffset)) & 0xffff;
          found = ((key & 0xffff) == curKey);
          zeroKey = (curKey == 0);
          break;
        case 3:
          //FIXME: illegal? - DMT
          curKey = (b.getInt(start + keyOffset) >>> 8);
          found = ((key & 0xffffff) == curKey);
          zeroKey = (curKey == 0);
          break; //added - dmt
        case 4:
          curKey = b.getInt(start + keyOffset);
          found = (key == curKey);
          break;
        default:
          fatal("Illegal key size for direct linearsearch.");
        }
      }
      
      if (found || (zeroTerm && zeroKey))
        break;

      i++;
      start += structSize;
    }

    if (found)
    {
      if (returnIndex)
        return i;
      else
        return start;
    }
    else if (zeroTerm && zeroKey)
    {
      if (returnIndex)
        return -1;
      else
        return 0;
    }
    else
    {
      if (returnIndex)
        return -1;
      else
        return 0;
    }
  }

  final int setMemSize(int newsize)
  {

      if (newsize == endmem)
          return 0;

      FastByteBuffer newmem = memory.resize(newsize);
      if (newmem == null) {
          return 1;
      }
      endmem = newsize;
      memory = newmem;
      return 0;
  }
  
  final int gestalt(int a, int b)
  {
    switch(a)
    {
    case 0:
      return 0x00020000;
    case 1:
      return 0x00001000;
    case 2:
      return 1;
    case 3:
      return 1;
    case 4:
      switch(b)
      {
      case 0:
      case 1:
      case 2:
        return 1;
      default:
        return 0;
      }
    case 5://unicode
        return 1;
    case 6://memcopy
        return 1;
    case 7://malloc
        return 1;
    case 8://mallocHeap
        return heap.getHeapStart();
    case 9:
        return 1;
    case 10:
        return (b >= 1 && b <= 7) ? 1 : 0;
    case 11:
        return 1;
    default:
      return 0;
    }
  }

  final void popArguments(int numargs)
  {
    if (sp < (vp + (4 * numargs)))
      fatal("Attempting to pop too many [" + numargs + 
            "] function arguments.  sp=" + sp + "; vp=" + vp);

    if (numargs > funargs.length)
      funargs = new int[numargs];

    for (int i = 0; i < numargs; i++)
    {
      sp -= 4;
      funargs[i] = stack.getInt(sp);
    }
  }

  final void pushCallstub(int mode, int addr)
  {
    stack.putInt(sp, mode);
    stack.putInt(sp + 4, addr);
    stack.putInt(sp + 8, pc);
    stack.putInt(sp + 12, fp);
    sp += 16;
  }

  final void popCallstub(int retval)
  {
    int dtype, daddr;

    sp -= 16;
    fp = stack.getInt(sp + 12);
    pc = stack.getInt(sp + 8);
    daddr = stack.getInt(sp + 4);
    dtype = stack.getInt(sp);

    vp = fp + stack.getInt(fp);
    lp = fp + stack.getInt(fp + 4);

    if (sp < vp)
      fatal("while popping callstub, sp=" + sp + "; vp=" + vp);


    switch(dtype)
    {
    case 0x10:
      io.streamString(this, pc, 2, daddr);
      break;
    case 0x11:
      fatal("String terminator callstub found at end of function call.");
      break;
    case 0x12:
      io.streamNum(this, pc, true, daddr);
      break;
    case 0x13:
      io.streamString(this, pc, 1, daddr);
      break;
    case 0x14:
      io.streamString(this, pc, 3, daddr);
      break;
    default:
      storeOperand(memory, dtype, daddr, retval);
    }
  }

  final StringCallResult popCallstubString()
  {
    StringCallResult r = new StringCallResult();
    int desttype, destaddr, newpc;

    sp -= 16;
    desttype = stack.getInt(sp);
    destaddr = stack.getInt(sp + 4);
    newpc = stack.getInt(sp + 8);

    pc = newpc;

    if (desttype == 0x11)
    {
      r.pc = 0;
      r.bitnum = 0;
      return r;
    }

    if (desttype == 0x10)
    {
      r.pc = pc;
      r.bitnum = destaddr;
      return r;
    }

    fatal("Function terminator call stub at the end of a string.");
    return null;
  }

  final void enterFunction(FastByteBuffer mem, int addr, 
                           int numargs, int[] args)
  {

    AcceleratedFunction func = (AcceleratedFunction) accelTable.get(addr);
    if (func != null) {
        int retval = func.enterFunction(numargs, args);
        popCallstub(retval);
        return;
    }

    int ltype, lnum;
    int format, local;
    int i, j;
    int len = 0;
    int funtype = ((int) mem.get(addr++)) & 0xff;

    if (funtype != 0xc0 && funtype != 0xc1)
    {
      if (funtype >= 0xc0 && funtype <= 0xdf)
        fatal("Unknown type of function.");
      else
        fatal("Attempt to call non-function.");
    }

    fp = sp;

    //figure out the length of the locals
    i = 0;
    while (true)
    {
      ltype = ((int) mem.get(addr++)) & 0xff;
      lnum = ((int) mem.get(addr++)) & 0xff;
      stack.put(fp + 8 + (2 * i), (byte) ltype);
      stack.put(fp + 8 + (2 * i) + 1, (byte) lnum);
      i++;

      if (ltype == 0)
      {
        if ((i & 1) != 0)
        {
          stack.put(fp + 8 + (2 * i), (byte) 0);
          stack.put(fp + 8 + (2 * i) + 1, (byte) 0);
          i++;
        }
        break;
      }

      if (ltype == 4)
      {
        while ((len & 3) != 0)
          len++;
      }
      else if (ltype == 2)
      {
        while ((len & 1) != 0)
          len++;
      }

      len += ltype * lnum;
    }

    while ((len & 3) != 0)
      len++;

    lp = fp + 8 + (2 * i);
    vp = lp + len;
    
    stack.putInt(fp, 8 + (2 * i) + len);
    stack.putInt(fp + 4, 8 + (2 * i));
    
    sp = vp;
    pc = addr;

    for (j = 0; j < len; j++)
      stack.put(lp + j, (byte) 0);
    
    //put locals on stack according to function's format
    if (funtype == 0xc0)
    {
      for (j = numargs - 1; j >=0; j--)
      {
        stack.putInt(sp, args[j]);
        sp += 4;
      }
      stack.putInt(sp, numargs);
      sp += 4;
    }
    else
    {
      format = fp + 8;
      local = lp;
      i = 0;
      while (i < numargs)
      {
        ltype = ((int) stack.get(format++)) & 0xff;
        lnum = ((int) stack.get(format++)) & 0xff;
        if (ltype == 0)
          break;
        if (ltype == 4)
        {
          while ((local & 3) != 0)
            local++;
          while (i < numargs && lnum != 0)
          {
            stack.putInt(local, args[i++]);
            local += 4;
            lnum--;
          }
        }
        else if (ltype == 2)
        {
          while ((local & 1) != 0)
            local++;
          while (i < numargs && lnum != 0)
          {
            stack.putShort(local, (short) (args[i++] & 0xffff));
            local += 2;
            lnum--;
          }
        }
        else
        {
          while (i < numargs && lnum != 0)
          {
            stack.put(local, (byte) (args[i++] & 0xff));
            local++;
            lnum--;
          }
        }
      }
    }
  }

  final void leaveFunction()
  {
    sp = fp;
  }

  private final void handleRelativeJump(int offset)
  {
    if (offset == 0 || offset == 1)
    {
      leaveFunction();
      if (sp == 0)
      {
        running = false;
        return;
      }
      popCallstub(offset);
    }
    else
    {
      pc = pc + offset - 2;
    }
  }

  static final void fatal(String s)
  {
    throw new GlulxException(s);
  }

  final static class StringCallResult
  {
    int pc;
    int bitnum;
  }
}


