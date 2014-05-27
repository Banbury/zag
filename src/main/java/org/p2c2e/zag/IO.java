package org.p2c2e.zag;

import org.p2c2e.zing.*;
import org.p2c2e.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;

final class IO
{
  static final int IFhd = (((byte) 'I') << 24) | (((byte) 'F') << 16) |
    (((byte) 'h') << 8) | (byte) 'd';
  static final int CMem = (((byte) 'C') << 24) | (((byte) 'M') << 16) |
    (((byte) 'e') << 8) | (byte) 'm';
  static final int UMem = (((byte) 'U') << 24) | (((byte) 'M') << 16) |
    (((byte) 'e') << 8) | (byte) 'm';
  static final int Stks = (((byte) 'S') << 24) | (((byte) 't') << 16) |
    (((byte) 'k') << 8) | (byte) 's';
  static final int MAll = (((byte) 'M') << 24) | (((byte) 'A') << 16) |
    (((byte) 'l') << 8) | (byte) 'l';

  static final int NULL = 0;
  static final int FILTER = 1;
  static final int GLK = 2;

  static final int PUT_CHAR = 0x0080;
  static final int PUT_CHAR_STREAM = 0x0081;
  static final int PUT_STRING = 0x0082;
  static final int PUT_STRING_STREAM = 0x0083;
  static final int PUT_BUFFER = 0x0084;
  static final int PUT_BUFFER_STREAM = 0x0085;
  static final int CHAR_TO_LOWER = 0x00a0;
  static final int CHAR_TO_UPPER = 0x00a1;
  static final int WINDOW_OPEN = 0x0023;
  static final int WINDOW_CLEAR = 0x002a;
  static final int WINDOW_MOVE_CURSOR = 0x002b;
  static final int BUFFER_TO_LOWER_CASE_UNI = 0x0120;
  static final int BUFFER_TO_UPPER_CASE_UNI = 0x0121;
  static final int BUFFER_TO_TITLE_CASE_UNI = 0x0122;
  static final int PUT_CHAR_UNI = 0x0128;
  static final int PUT_STRING_UNI = 0x0129;
  static final int PUT_BUFFER_UNI = 0x012A;
  static final int PUT_CHAR_STREAM_UNI = 0x012B;
  static final int PUT_STRING_STREAM_UNI = 0x012C;
  static final int PUT_BUFFER_STREAM_UNI = 0x012D;


  static final Object[] P0 = new Object[0];
  static final Object[] P1 = new Object[1];
  static final Object[] P2 = new Object[2];
  static final Object[] P3 = new Object[3];
  static final Object[] P4 = new Object[4];
  static final Object[] P5 = new Object[5];
  static final Object[] P6 = new Object[6];
  static final Object[] P7 = new Object[7];
  static final Object[] P8 = new Object[8];
  static final Object[] P9 = new Object[9];
  static final Object[][] P = {P0, P1, P2, P3, P4, P5, P6, P7, P8, P9};

  int sys;
  int rock;

  HuffmanTree htree;

  LinkedList undoData = new LinkedList();
  Fileref undoFile;
  Stream undoStream;

  HashMap ors;

  IO(Zag z)
  {
    ors = new HashMap();
    init(z);
  }

  void init(Zag z)
  {
    int stringtbl = z.memory.getInt(28);
    if (stringtbl > 0)
      htree = new HuffmanTree(z, stringtbl);
    /*
    if (undoStream != null)
    {
      Glk.streamClose(undoStream, null);
      undoStream = null;
    }
    if (undoFile != null)
    {
      Glk.filerefDeleteFile(undoFile);
      Glk.filerefDestroy(undoFile);
      undoFile = null;
    }
    undoData = new LinkedList();

    */
    sys = 0;
    rock = 0;

    Glk.setCreationCallback(new CreationCallback());
    Glk.setDestructionCallback(new DestructionCallback());
  }

  void wrongNumArgs()
  {
    Zag.fatal("Wrong number of arguments to GLK function.");
  }

  void noSuchMethod(int s)
  {
    Zag.fatal("Attempt to call nonexistent (or unimplemented) GLK function: " + s);
  }

  String getStringFromMemory(FastByteBuffer mem, int addr)
  {
    StringBuffer sb = new StringBuffer();
    int t = mem.get(addr++);
    if (t == (byte)0xe0) {
        while ((t = mem.get(addr++)) != 0)
            sb.append((char) t);
    } else if (t == (byte)0xe2) {
        addr -= 1;
        while ((t = mem.getInt(addr+=4)) != 0)
            sb.appendCodePoint(t);
    } else {
      Zag.fatal("Cannot send compressed string to GLK function: " + t);
    }
    return sb.toString();
  }

  String getUnicodeStringFromMemory(FastByteBuffer mem, int addr, int len) {
      StringBuffer sb = new StringBuffer();
      int end = addr + len*4;
      while (addr < end) {
          int t = mem.getInt(addr+=4);
          sb.appendCodePoint(t);
      }
      return sb.toString();
  }

  void writeUnicodeStringToMemory(String string, FastByteBuffer mem, int addr) {
      for (int i = 0; i < string.length(); ++i) {
          int cp = string.codePointAt(i);
          if (cp > 0xffff) {
              ++i;
          }
          mem.putInt(addr+=4, cp);
      }
  }

  String truncate(String str, int len) {
      StringBuffer sb = new StringBuffer();
      int newOutLen = 0;
      int index = 0;
      while (index < str.length()) {
          newOutLen ++;
          if (newOutLen > len) {
              break;
          }
          int cp = str.codePointAt(index++);
          if (cp > 0xffff) {
              index ++;
          }
          sb.appendCodePoint(cp);
      }
      return sb.toString();
  }

  int glk(Zag z, int selector, int numargs, int[] args)
  {
    FastByteBuffer mem = z.memory;
    int ret = 0;

    switch(selector)
    {
    case PUT_CHAR:
      if (numargs != 1)
        wrongNumArgs();
      Glk.putChar((char) (args[0] & 0xff));
      break;
    case PUT_CHAR_UNI:
      if (numargs != 1)
        wrongNumArgs();
      Glk.putCharUni(args[0]);
      break;
    case PUT_CHAR_STREAM:
      if (numargs != 2)
        wrongNumArgs();
      Glk.putCharStream((Stream) ors.get(new Integer(args[0])), 
                        (char) (args[1] & 0xff));
      break;
    case PUT_CHAR_STREAM_UNI:
      if (numargs != 2)
        wrongNumArgs();
      Glk.putCharStream((Stream) ors.get(new Integer(args[0])), 
                        args[1]);
      break;
    case PUT_STRING: 
      if (numargs != 1)
        wrongNumArgs();
      Glk.putString(getStringFromMemory(mem, args[0]));
      break;
    case PUT_STRING_UNI:
      if (numargs != 1)
        wrongNumArgs();
      Glk.putString(getStringFromMemory(mem, args[0]));
      break;

    case PUT_STRING_STREAM:
    case PUT_STRING_STREAM_UNI:
      if (numargs != 2)
        wrongNumArgs();
      Glk.putStringStream((Stream) ors.get(new Integer(args[0])),
                          getStringFromMemory(mem, args[1]));
      break;
    case PUT_BUFFER:
      if (numargs != 2)
        wrongNumArgs();
      mem.position(args[0]);      
      Glk.putBuffer(new InByteBuffer(mem.slice()), args[1]);
      break;
    case PUT_BUFFER_UNI:
      if (numargs != 2)
        wrongNumArgs();
      mem.position(args[0]);      
      Glk.putBufferUni(new InByteBuffer(mem.slice()), args[1]);
      break;
    case PUT_BUFFER_STREAM:
      if (numargs != 3)
        wrongNumArgs();
      mem.position(args[1]);      
      Glk.putBufferStream((Stream) ors.get(new Integer(args[0])),
                          new InByteBuffer(mem.slice()), args[2]);
      break;
    case PUT_BUFFER_STREAM_UNI:
      if (numargs != 3)
        wrongNumArgs();
      mem.position(args[1]);      
      Glk.putBufferStreamUni((Stream) ors.get(new Integer(args[0])),
                             new InByteBuffer(mem.slice()), args[2]);
      break;
    case CHAR_TO_LOWER:
      if (numargs != 1)
        wrongNumArgs();
      ret = (int) Character.toLowerCase((char) (args[0] & 0xff));
      break;
    case CHAR_TO_UPPER:
      if (numargs != 1)
        wrongNumArgs();
      ret = (int) Character.toUpperCase((char) (args[0] & 0xff));
      break;
    case WINDOW_OPEN:
      if (numargs != 5)
        wrongNumArgs();
      ret = Glk.windowOpen((Window) ors.get(new Integer(args[0])),
                           args[1], args[2], args[3], args[4]).hashCode();
      break;
    case WINDOW_CLEAR:
      if (numargs != 1)
        wrongNumArgs();
      Glk.windowClear((Window) ors.get(new Integer(args[0])));
      break;
    case WINDOW_MOVE_CURSOR:
      if (numargs != 3)
        wrongNumArgs();
      Glk.windowMoveCursor((Window) ors.get(new Integer(args[0])),
                           args[1], args[2]);
      break;
    case BUFFER_TO_LOWER_CASE_UNI:
        {
            if (numargs != 3)
                wrongNumArgs();
            String unistr = getUnicodeStringFromMemory(mem, args[0] - 4, args[2]);
            unistr = unistr.toLowerCase();
            int len = args[1];
            int outLen = unistr.codePointCount(0, unistr.length());
            if (outLen > len) {
                unistr = truncate(unistr, len);
            }
            writeUnicodeStringToMemory(unistr, mem, args[0] - 4);
            ret = outLen;
        }
      break;
    case BUFFER_TO_UPPER_CASE_UNI:
        {
            if (numargs != 3)
                wrongNumArgs();
            String unistr = getUnicodeStringFromMemory(mem, args[0] - 4, args[2] + 4);
            unistr = unistr.toUpperCase();
            int len = args[1];
            int outLen = unistr.codePointCount(0, unistr.length());
            if (outLen > len) {
                unistr = truncate(unistr, len);
            }
            writeUnicodeStringToMemory(unistr, mem, args[0] - 4);
            ret = outLen;
        }
      break;
    case BUFFER_TO_TITLE_CASE_UNI:
        /*
          Glk does not actually give enough information to correctly
          handle title casing -- in particular, locale is not
          specified.  So we're just going to punt on this and upcase
          the first letter.  This avoids the need for many megabytes
          of ICU libraries which wouldn't actually help anyway.
          Sorry, Dutch and Turkish IF authors -- blame Zarf for not
          providing proper localization.
         */
        {
            if (numargs != 4)
                wrongNumArgs();
            String unistr = getUnicodeStringFromMemory(mem, args[0] - 4, args[2]);
            boolean lowerrest = args[3] != 0;
            if (lowerrest) {
                unistr = unistr.toLowerCase();
            }
            StringBuffer sb = new StringBuffer();
            int firstChar = unistr.codePointAt(0);
            sb.appendCodePoint(Character.toUpperCase(firstChar));
            sb.append(unistr.substring(firstChar > 0xffff ? 2 : 1));
            unistr = sb.toString();
            int len = args[1];
            int outLen = unistr.codePointCount(0, unistr.length());
            if (outLen > len) {
                unistr = truncate(unistr, len);
            }
            writeUnicodeStringToMemory(unistr, mem, args[0] - 4);
            ret = outLen;
        }
      break;
    default:
      int addr;
      Class c;
      Method m = Dispatch.getMethod(selector);
      if (m == null)
        noSuchMethod(selector);
      Class[] f = m.getParameterTypes();
      if (f.length != numargs)
        wrongNumArgs();

      Object[] p = P[numargs];
      for (int i = 0; i < numargs; i++)
      {
        c = f[i];
        if (c == Window.class || c == Stream.class || 
            c == Fileref.class || c == SoundChannel.class)
        {
          p[i] = ors.get(new Integer(args[i]));
        }
        else if (c == OutWindow.class)
        {
          p[i] = new OutWindow();
        }
        else if (c == int.class)
        {
          p[i] = new Integer(args[i]);
        }
        else if (c == char.class)
        {
          p[i] = new Character((char) args[i]);
        }
        else if (c == String.class)
        {
          p[i] = getStringFromMemory(mem, args[i]);
        }
        else if (c == OutInt.class)
        {
          p[i] = new OutInt();
        }
        else if (c == InByteBuffer.class)
        {
          mem.position(args[i]);
          p[i] = new InByteBuffer(mem.slice());
        }
        else if (c == OutByteBuffer.class)
        {
          mem.position(args[i]);
          p[i] = new OutByteBuffer(mem.slice());
        }
        else if (c == InOutByteBuffer.class)
        {
          mem.position(args[i]);
          p[i] = new InOutByteBuffer(mem.slice());
        }
        else if (c == InOutIntBuffer.class)
        {
          mem.position(args[i]);
          p[i] = new InOutIntBuffer(mem.slice().asIntBuffer());
        }
        else if (c == Glk.GlkEvent.class)
        {
          p[i] = new Glk.GlkEvent();
        }
        else if (c == Stream.Result.class)
        {
          p[i] = new Stream.Result();
        }
        else if (c == java.awt.Color.class)
        {
          p[i] = new java.awt.Color((args[i] >>> 16) & 0xff, 
                                    (args[i] >>> 8) & 0xff,
                                    args[i] & 0xff);
        }
        else
        {
          Zag.fatal("Unimplemented parameter type: " + c.getName());
        }
      }

      Object oRet = null;
      try {
        oRet = m.invoke(null, p);
      } catch (Exception eAccess) {
        String stError = "Could not dispatch call [" +
          Integer.toHexString(selector) + "] to GLK: " + eAccess;
        if (eAccess instanceof InvocationTargetException)
        {
          stError += "\n" + eAccess.getCause();
          eAccess.getCause().printStackTrace();
        }
        else
        {
          eAccess.printStackTrace();
        }
        z.fatal(stError);
      }

      Class retType = (oRet == null) ? null : m.getReturnType();

      for (int i = 0; i < numargs; i++)
      {
        c = f[i];
        addr = args[i];
        if (c == Glk.GlkEvent.class)
        {
          Glk.GlkEvent e = (Glk.GlkEvent) p[i];
          if (addr == -1)
          {
            z.stack.putInt(z.sp, e.type);
            z.sp += 4;
            z.stack.putInt(z.sp, ((e.win == null) ? 0 : e.win.hashCode()));
            z.sp += 4;
            z.stack.putInt(z.sp, e.val1);
            z.sp += 4;
            z.stack.putInt(z.sp, e.val2);
            z.sp += 4;
          }
          else if (addr != 0)
          {
            mem.putInt(addr, e.type);
            addr += 4;
            mem.putInt(addr, ((e.win == null) ? 0 : e.win.hashCode()));
            addr += 4;
            mem.putInt(addr, e.val1);
            addr += 4;
            mem.putInt(addr, e.val2);
            addr += 4;
          }
        }
        else if (c == Stream.Result.class)
        {
          Stream.Result r = (Stream.Result) p[i];
          if (addr == -1)
          {
            z.stack.putInt(z.sp, r.readcount);
            z.sp += 4;
            z.stack.putInt(z.sp, r.writecount);
            z.sp += 4;
          }
          else if (addr != 0)
          {
            mem.putInt(addr, r.readcount);
            addr += 4;
            mem.putInt(addr, r.writecount);
            addr += 4;
          }
        }
        else if (c == OutWindow.class)
        {
          if (addr == -1)
          {
            z.stack.putInt(z.sp, ((OutWindow) p[i]).window.hashCode());
            z.sp += 4;
          }
          else if (addr != 0)
          {
            mem.putInt(addr, ((OutWindow) p[i]).window.hashCode());
            addr += 4;
          }
        }
        else if (c == OutInt.class)
        {
          if (addr == -1)
          {
            z.stack.putInt(z.sp, ((Int) p[i]).val);
            z.sp += 4;
          }
          else if (addr != 0)
          {
            mem.putInt(addr, ((Int) p[i]).val);
            addr += 4;
          }
        }
      }

      if (retType != null)
      {
        if (retType == Window.class || retType == Stream.class ||
            retType == Fileref.class || retType == SoundChannel.class)
        {
          ret = oRet.hashCode();
        }
        else if (retType == char.class)
        {
          ret = (int) ((Character) oRet).charValue();
        }
        else if (retType == int.class)
        {
          ret = ((Integer) oRet).intValue();
        }
        else if (retType == boolean.class)
        {
          ret = ((Boolean) oRet).booleanValue() ? 1 : 0;
        }
        else
        {
          Zag.fatal("Unimplemented return type for GLK function.");
        }
      }
    }
    return ret;
  }

  void setSys(int newsys, int newrock)
  {
    switch(newsys)
    {
    case FILTER:
      sys = newsys;
      rock = newrock;
      break;
    case GLK:
      sys = newsys;
      rock = 0;
      break;
    default:
      sys = 0;
      rock = 0;
    }
  }

  void streamNum(Zag z, int n, boolean started, int position)
  {
    Zag.StringCallResult r;
    int ival;
    String s = String.valueOf(n);
    int len = s.length();

    switch(sys)
    {
    case GLK:
      for (int i = position; i < len; i++)
        Glk.putChar(s.charAt(i));
      if (started) {
        r = z.popCallstubString();
        if (r.pc != 0)
          z.fatal("String-on-string call stub while printing number.");
      }
      break;
    case FILTER:
      if (!started) {
        z.pushCallstub(0x11, 0);
      }
      if (position >= len)
      {
        r = z.popCallstubString();
        if (r.pc != 0)
          z.fatal("String-on-string call stub while printing number.");
      }
      else
      {
        int tmp = z.pc;
        z.pc = n;
        z.pushCallstub(0x12, position + 1);
        z.pc = tmp;
        z.enterFunction(z.memory, rock, 1, new int[] {(int) s.charAt(position)});
      }
      break;
    default:
    }
  }

  void streamChar(Zag z, int c)
  {
    switch(sys)
    {
    case FILTER:
      z.pushCallstub(0, 0);
      z.enterFunction(z.memory, rock, 1, new int[] {c});
      break;
    case GLK:
      Glk.putChar((char) c);
      break;
    default:
    }
  }

 void streamUniChar(Zag z, int c)
  {
    switch(sys)
    {
    case FILTER:
      z.pushCallstub(0, 0);
      z.enterFunction(z.memory, rock, 1, new int[] {c});
      break;
    case GLK:
      Glk.putCharUni((char) c);
      break;
    default:
    }
  }

  void streamString(Zag z, int addr, int inmiddle, int bit)
  {
    byte ch;
    int oaddr;
    int type;
    boolean alldone = false;
    boolean started = (inmiddle != 0);

    while (!alldone)
    {
      if (inmiddle == 0)
      {
        type = (int) z.memory.get(addr++) & 0xff;
        bit = 0;
      }
      else
      {
        if (inmiddle == 1)
          type = 0xe0;
        else if (inmiddle == 3) 
          type = 0xe2;
        else 
          type = 0xe1;
      }

      if (type == 0xe1)
      {
        if (htree == null)
          z.fatal("Attempt to stream a compressed string with no Huffman table.");
        HuffmanTree.Node troot = htree.root;
        HuffmanTree.Node n;
        int done = 0;

        if (troot == null)
          troot = htree.readTree(z, z.memory.getInt(htree.startaddr + 8), 
                                 false);

        n = troot;

        while (done == 0)
        {
          switch(n.type)
          {
          case 0x00:
            boolean on;
            byte b = z.memory.get(addr);

            if (bit > 0)
              b >>= bit;
            on = ((b & 1) != 0);
      
            if (++bit > 7)
            {
              bit = 0;
              addr++;
            }

            if (on)
              n = (n.right == null) 
                ? (n.right = htree.readTree(z, n.rightaddr, false))
                : n.right;
            else
              n = (n.left == null)
                ? (n.left = htree.readTree(z, n.leftaddr, false))
                : n.left;
            break;
          case 0x01:
            done = 1;
            break;
          case 0x02:
            switch(sys)
            {
            case GLK:
              Glk.putChar((char) (((int) n.c) & 0xff));
              break;
            case FILTER:
              if (!started)
              {
                z.pushCallstub(0x11, 0);
                started = true;
              }
              z.pc = addr;
              z.pushCallstub(0x10, bit);
              z.enterFunction(z.memory, rock, 1, new int[] {(int) n.c & 0xff});
              return;
            default:
              break;
            }
            n = troot;
            break;
          case 0x03:
            switch(sys)
            {
            case GLK:
              z.memory.position(n.addr);
              Glk.putBuffer(new InByteBuffer(z.memory.slice()), n.numargs);
              n = troot;
              break;
            case FILTER:
              if (!started)
              {
                z.pushCallstub(0x11, 0);
                started = true;
              }
              z.pc = addr;
              z.pushCallstub(0x10, bit);
              inmiddle = 1;
              addr = n.addr;
              done = 2;
              break;
            default:
              n = troot;
            }
            break;
          case 0x04:
            switch(sys)
            {
            case GLK:
              Glk.putCharUni(n.u);
              break;
            case FILTER:
              if (!started)
              {
                z.pushCallstub(0x11, 0);
                started = true;
              }
              z.pc = addr;
              z.pushCallstub(0x10, bit);
              z.enterFunction(z.memory, rock, 1, new int[] { n.u });
              return;
            default:
              break;
            }
            n = troot;
            break;
          case 0x05:
            switch(sys)
            {
            case GLK:
              z.memory.position(n.addr);
              Glk.putBufferUni(new InByteBuffer(z.memory.slice()), n.numargs);
              n = troot;
              break;
            case FILTER:
                if (!started)
              {
                z.pushCallstub(0x11, 0);
                started = true;
              }
              z.pc = addr;
              z.pushCallstub(0x10, bit);
              inmiddle = 3;
              addr = n.addr - 3;
              done = 2;
              break;
            default:
              n = troot;
            }
            break;
          case 0x08:
          case 0x09:
          case 0x0a:
          case 0x0b:
            int otype;
            oaddr = n.addr;
            if (n.type == 0x09 || n.type == 0x0b)
              oaddr = z.memory.getInt(oaddr);
            otype = (int) z.memory.get(oaddr) & 0xff;
            
            if (!started)
            {
              z.pushCallstub(0x11, 0);
              started = true;
            }
            if (otype >= 0xe0 && otype <= 0xff)
            {
              z.pc = addr;
              z.pushCallstub(0x10, bit);
              inmiddle = 0;
              addr = oaddr;
              done = 2;
            }
            else if (otype >= 0xc0 && otype <= 0xdf)
            {
              z.pc = addr;
              z.pushCallstub(0x10, bit);
              z.enterFunction(z.memory, oaddr, n.numargs, n.args);
              return;
            }
            else
            {
              z.fatal("Attempting indirect reference to unknown object while " +
                      "decoding string.");
            }
            break;
          default:
            z.fatal("Unknown node type in cached Huffman tree.");
          }
        }

        if (done > 1)
          continue;

      }
      else if (type == 0xe0)
      {
        switch(sys)
        {
        case GLK:
          while ((ch = z.memory.get(addr++)) != 0)
            Glk.putChar((char) (((int) ch) & 0xff));
          break;
        case FILTER:
          if (!started)
          {
            z.pushCallstub(0x11, 0);
            started = true;
          }
          ch = z.memory.get(addr++);
          if (ch != 0)
          {
            z.pc = addr;
            z.pushCallstub(0x13, 0);
            z.enterFunction(z.memory, rock, 1, new int[] {(int) ch & 0xff});
            return;
          }
          break;
        default:
        }
      }
      else if (type == 0xe2)
      {
        int uch;
        addr -= 1;
        switch(sys)
        {
        case GLK:
          while ((uch = z.memory.getInt(addr+=4)) != 0)
            Glk.putCharUni(uch);
          break;
        case FILTER:
          if (!started)
          {
            z.pushCallstub(0x11, 0);
            started = true;
          }
          uch = z.memory.getInt(addr+=4);
          if (uch != 0)
          {
            z.pc = addr+1;
            z.pushCallstub(0x14, 0);
            z.enterFunction(z.memory, rock, 1, new int[] {uch});
            return;
          }
          break;
        default:
        }
      }
      else if (type >= 0xe0 && type <= 0xff)
      {
        z.fatal("Attempt to print unknown type of string.");
      }
      else
      {
        z.fatal("Attempt to print non-string.");
      }

      if (!started)
      {
        alldone = true;
      }
      else
      {
        Zag.StringCallResult r = z.popCallstubString();
        if (r.pc == 0)
        {
          alldone = true;
        }
        else
        {
          addr = r.pc;
          bit = r.bitnum;
          inmiddle = 2;
        }
      }
    }
  }

  int saveUndo(Zag z)
  {
    if (undoFile == null)
    {
      undoFile = Glk.filerefCreateTemp(Glk.FILEUSAGE_DATA | 
                                       Glk.FILEUSAGE_BINARY_MODE, 0);
      undoStream = Glk.streamOpenFile(undoFile, Glk.FILEMODE_READ_WRITE, 0);
    }
    int pos = undoStream.getPosition();
    undoData.addFirst(new Integer(pos));

    int res = saveGame(z, undoStream.hashCode());
    return res;
  }


  int saveGame(Zag z, int streamId)
  {
    SaveSize savesize;
    Stream s;
    int pos;
    int end;
    int val;

    s = (Stream) ors.get(new Integer(streamId));
    if (s == null)
      return 1;

    try {
      pos = s.getPosition();
      savesize = saveState(z, s);
      end = s.getPosition();
    
      val = savesize.size;
      s.setPosition(pos + 4, Glk.SEEKMODE_START);
      s.putInt(val);

      s.setPosition(pos + 152, Glk.SEEKMODE_START);
      s.putInt(savesize.memSize);
    
      s.setPosition(s.getPosition() + savesize.memSize +
                    (((savesize.memSize & 1) == 0) ? 4 : 5), Glk.SEEKMODE_START);
      s.putInt(savesize.stackSize);
      s.setPosition(end, Glk.SEEKMODE_START);

      return 0;
    } catch (IOException e) {
      e.printStackTrace();
      return 1;
    }
  }

  int restoreGame(Zag z, int streamId)
  {
    Stream in = (Stream) ors.get(new Integer(streamId));
    boolean success;

    if (in == null)
        return 1;
    try {
      success = restoreState(z, in);
      return (success) ? 0 : 1;
    } catch (IOException e) {
      e.printStackTrace();
      return 1;
    }
  }

  int restoreUndo(Zag z)
  {
    int res;

    if (undoData.isEmpty())
      return 1;

    try {
      int pos = ((Integer) undoData.removeFirst()).intValue();
      undoStream.setPosition(pos, Glk.SEEKMODE_START);
      res = restoreState(z, undoStream) ? 0 : 1;
      undoStream.setPosition(pos, Glk.SEEKMODE_START);
    } catch(Exception e) {
      e.printStackTrace();
      res = 1;
    }
    return res;
  }

  boolean restoreState(Zag z, Stream in) throws IOException
  {
    int chunkSize;
    int iType;
    int iPos;
    int ch;
    int iChunkEnd;
    int bData;
    int bLen;
    RandomAccessFile f;
    DataInputStream din;
    FastByteBuffer memBuf = null;
    FastByteBuffer stackBuf = null;
    FastByteBuffer mallBuf = null;
    int memSize = 0;
    int fileLen; 
    ByteBuffer headBuf = ByteBuffer.allocate(128);
    byte[] gameHeadArr = new byte[128];
    boolean okay = true;
    boolean checkedHeader = false;
    
    okay &= (((char) in.getChar()) == 'F');
    okay &= (((char) in.getChar()) == 'O');
    okay &= (((char) in.getChar()) == 'R');
    okay &= (((char) in.getChar()) == 'M');
    chunkSize = in.getInt();

    okay &= (((char) in.getChar()) == 'I');
    okay &= (((char) in.getChar()) == 'F');
    okay &= (((char) in.getChar()) == 'Z');
    okay &= (((char) in.getChar()) == 'S');

    iType = in.getInt();
    while (okay)
    {
      switch(iType)
      {
      case IFhd:
        okay &= ((chunkSize = in.getInt()) == 128);

        in.getBuffer(headBuf, 128);
        f = new RandomAccessFile(z.gamefile, "r");
        f.seek(z.fileStartPos);
        din = new DataInputStream(new BufferedInputStream(new FileInputStream(f.getFD())));
        din.readFully(gameHeadArr);
        din.close();
        f.close();

        for (int i = 0; okay && i < 128; i++)
          okay &= (headBuf.get(i) == gameHeadArr[i]);

        checkedHeader = true;
        break;
      case UMem:
        chunkSize = in.getInt();
        memSize = in.getInt();
        memBuf = new FastByteBuffer(memSize - z.ramstart);
        in.getBuffer(memBuf.asByteBuffer(), memSize - z.ramstart);
        break;
      case CMem:
        chunkSize = in.getInt();
        iChunkEnd = in.getPosition() + chunkSize;
        memSize = in.getInt();
        memBuf = new FastByteBuffer(memSize - z.ramstart);
        f = new RandomAccessFile(z.gamefile, "r");
        fileLen = (int) f.length();
        f.seek(z.fileStartPos + z.ramstart);
        din = new DataInputStream(new BufferedInputStream(new FileInputStream(f.getFD())));
        bLen = 0;

        for (iPos = z.ramstart; iPos < memSize; iPos++)
        {
          if (iPos + z.fileStartPos < fileLen)
            bData = (din.read() & 0xff);
          else
            bData = 0;
          
          if (in.getPosition() >= iChunkEnd)
          {
            // NOOP
          }
          else if (bLen > 0)
          {
            bLen--;
          }
          else
          {
            ch = in.getChar();
            if (ch == 0)
              bLen = (in.getChar() & 0xff);
            else
              bData ^= ch;
          }

          memBuf.put(iPos - z.ramstart, (byte) bData);
        }
        din.close();
        f.close();
        break;
        
      case Stks:
        chunkSize = in.getInt();
        stackBuf = new FastByteBuffer(chunkSize);
        in.getBuffer(stackBuf.asByteBuffer(), chunkSize);
        break;

      case MAll:
        chunkSize = in.getInt();
        mallBuf = new FastByteBuffer(chunkSize);
        in.getBuffer(mallBuf.asByteBuffer(), chunkSize);
        break;

      default:
        chunkSize = in.getInt();
        in.setPosition(chunkSize, Glk.SEEKMODE_CURRENT);
      }

      //remoced - DMT
      //done = (checkedHeader && stackBuf != null && memBuf != null);
      //      if (!done)
      //{
        if ((chunkSize & 1) != 0)
          in.getChar();
        iType = in.getInt();
        if (iType == -1) {
            break;
        }
        //}
    }

    if (okay)
    {
      z.setMemSize(memSize);
      for (int i = z.ramstart; i < z.endmem; i++)
      {
        if (i >= z.protectend || i < z.protectstart)
          z.memory.put(i, memBuf.get(i - z.ramstart));
      }

      z.sp = stackBuf.capacity();
      for (int i = 0; i < z.sp; i++)
        z.stack.put(i, stackBuf.get(i));

      if (mallBuf != null) {
          mallBuf.position(0);

          int heapStart = mallBuf.getInt();
          int nblocks = mallBuf.getInt();
          int end = 0;
          Heap heap = new Heap(z, heapStart);
          for (int i = 0; i < nblocks; ++i) {
              int blockStart = mallBuf.getInt();
              int blockSize = mallBuf.getInt();
              int blockEnd = blockStart + blockSize;
              if (blockEnd > end) {
                  end = blockEnd;
                  heap.reserveUpTo(end);
              }
              heap.mallocAt(blockStart, blockSize);
          }
          z.heap = heap;
      }

    }
    
    return okay;
  }

  SaveSize saveState(Zag z, Stream out) throws IOException
  {
    int i;
    int pos;
    int memChunkSize, stackChunkSize, heapChunkSize = 0;
    SaveSize savesize;
    int startPos = out.getPosition();

    out.putChar((int) 'F');
    out.putChar((int) 'O');
    out.putChar((int) 'R');
    out.putChar((int) 'M');

    out.putInt(0);

    out.putChar((int) 'I');
    out.putChar((int) 'F');
    out.putChar((int) 'Z');
    out.putChar((int) 'S');

    out.putChar((int) 'I');
    out.putChar((int) 'F');
    out.putChar((int) 'h');
    out.putChar((int) 'd');

    out.putInt(128);

    z.memory.position(0);
    out.putBuffer(z.memory.asByteBuffer(), 128);

    out.putChar((int) 'C');
    out.putChar((int) 'M');
    out.putChar((int) 'e');
    out.putChar((int) 'm');

    out.putInt(0); 

    pos = out.getPosition();
    saveCmem(z, out);
    memChunkSize = out.getPosition() - pos;
    if ((memChunkSize & 1) != 0)
      out.putChar((int) 0);

    out.putChar((int) 'S');
    out.putChar((int) 't');
    out.putChar((int) 'k');
    out.putChar((int) 's');

    out.putInt(0); 

    pos = out.getPosition();
    saveStks(z, out);
    stackChunkSize = out.getPosition() - pos;
    if ((stackChunkSize & 1) != 0)
      out.putChar((int) 0);

    if (z.heap != null && z.heap.isActive()) {
        out.putChar((int) 'M');
        out.putChar((int) 'A');
        out.putChar((int) 'l');
        out.putChar((int) 'l');

        int [] blocks = z.heap.getUsedBlocks();

        heapChunkSize = 8 + blocks.length * 4;
        out.putInt(heapChunkSize); 

        out.putInt(z.heap.getHeapStart());
        out.putInt(blocks.length / 2);

        for (i = 0; i < blocks.length; i ++) {
            out.putInt(blocks[i]);
        }
    }


    savesize = new SaveSize();
    savesize.size = out.getPosition() - startPos - 4;
    savesize.memSize = memChunkSize;
    savesize.stackSize = stackChunkSize;
    savesize.heapSize = heapChunkSize;
    return savesize;
  }

  void saveStks(Zag z, Stream out)
  {
    z.stack.position(0);
    out.putBuffer(z.stack.asByteBuffer(), z.sp);
  }

  void saveCmem(Zag z, Stream out) throws IOException
  {
    BufferedInputStream in;
    int oval, xval;
    int runlen = 0;
    RandomAccessFile f = new RandomAccessFile(z.gamefile, "r");

    out.putInt(z.endmem);
    
    f.seek(z.fileStartPos + z.ramstart);
    in = new BufferedInputStream(new FileInputStream(f.getFD()));

    for (int i = z.ramstart; i < z.endmem; i++)
    {
      xval = ((int) z.memory.get(i)) & 0xff;
      if (i < z.extstart)
        xval ^= in.read();
      
      if (xval == 0)
      {
        runlen++;
      }
      else
      {
        while (runlen > 0)
        {
          oval = (runlen >= 256) ? 256 : runlen;
          out.putChar(0);
          out.putChar(oval - 1);
          runlen -= oval;
        }

        out.putChar((int) xval);
      }
    }
    in.close();
    f.close();
  }
  
  static final class SaveSize
  {
    int memSize;
    int stackSize;
    int size;
    int heapSize;
  }

  final class CreationCallback implements ObjectCallback
  {
    public void callback(Object o)
    {
      ors.put(new Integer(o.hashCode()), o);
    }
  }

  final class DestructionCallback implements ObjectCallback
  {
    public void callback(Object o)
    {
      if (o instanceof SoundChannel)
        try {
          ((SoundChannel) o).stop();
        } catch (Exception e) {
          System.err.println("could not stop sound channel: " + e);
        }

      ors.remove(new Integer(o.hashCode()));
    }
  }
}
