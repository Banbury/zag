package org.p2c2e.zing;

import java.nio.charset.Charset;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;

public abstract class Stream implements Comparable
{
  int filemode;
  int rcount;
  int wcount;
  int pos;
  boolean canRead;
  boolean canWrite;

  Stream(int mode)
  {
    filemode = mode;
    rcount = wcount = pos = 0;
    canRead = (filemode == Glk.FILEMODE_READ || 
               filemode == Glk.FILEMODE_READ_WRITE);
    canWrite = (filemode == Glk.FILEMODE_WRITE || 
                filemode == Glk.FILEMODE_READ_WRITE || 
                filemode == Glk.FILEMODE_WRITE_APPEND);
  }

  public void setHyperlink(int val)
  {

  }

  public int compareTo(Object o)
  {
    return hashCode() - o.hashCode();
  }

  public int getPosition()
  {
    return pos;
  }

  public abstract void setPosition(int p, int seekmode);

  public void setStyle(String sname)
  {

  }

  public abstract void putChar(int c);
  public abstract void putCharUni(int c);

  public void putInt(int i)
  {
    putChar((i >>> 24) & 0xff);
    putChar((i >>> 16) & 0xff);
    putChar((i >>> 8) & 0xff);
    putChar(i & 0xff);
  }

  public void putString(String s)
  {
    int len = s.length();
    for (int i = 0; i < len; i++)
      putChar(((int) s.charAt(i)));
  }

  public void putStringUni(String s)
  {
    int len = s.length();
    for (int i = 0; i < len; i++)
      putCharUni(s.charAt(i));
  }

  public void putBuffer(ByteBuffer b, int len)
  {
    for (int i = 0; i < len; i++)
      putChar(((int) b.get(i)));
  }

  public void putBufferUni(ByteBuffer b, int len)
  {
    for (int i = 0; i < len; i++)
      putCharUni(b.getInt(i*4));
  }

  public int getChar()
  {
    return -1;
  }
  public int getCharUni()
  {
    return -1;
  }

  public int getInt() throws EOFException
  {
    try {
      return (((getChar() & 0xff) << 24) | ((getChar() & 0xff) << 16) | 
              ((getChar() & 0xff) << 8) | (getChar() & 0xff));
    }
    catch(Exception e)
    {
      e.printStackTrace();
      throw new EOFException();
    }
  }

  public int getBuffer(ByteBuffer b, int len)
  {
    int i = 0;
    int val = getChar();
    
    while (i < len && val != -1)
    {
      i++;
      b.put((byte) val);
      val = getChar();
    }
    return i;
  }

  public int getBufferUni(ByteBuffer b, int len)
  {
    int i = 0;
    int val = getCharUni();
    
    while (i < len && val != -1)
    {
      i++;
      b.putInt(val);
      val = getCharUni();
    }
    return i;
  }

  public int getLine(ByteBuffer b, int len)
  {
    int i = 0;
    int val = getChar();

    while (i < len - 1 && val != -1)
    {
      i++;
      b.put((byte) val);
      if ((char) val == '\n')
        break;
      val = getChar();
    }
    if (len > 0)
      b.put((byte) 0);

    return i;
  }


  public int getLineUni(ByteBuffer b, int len)
  {
    int i = 0;
    int val = getCharUni();

    while (i < len - 1 && val != -1)
    {
      i++;
      b.putInt(val);
      if ((char) val == '\n')
        break;
      val = getCharUni();
    }
    if (len > 0)
      b.putInt(0);

    return i;
  }

  public Result close()
  {
    Result r = new Result();
    r.readcount = rcount;
    r.writecount = wcount;

    return r;
  }



  public static class Result
  {
    public int readcount;
    public int writecount;
  }


  static class MemoryStream extends Stream
  {
    ByteBuffer buf;
    int len;

    MemoryStream(ByteBuffer buffer, int buflen, int mode)
    {
      super(mode);
      buf = buffer;
      len = buflen;

      if (mode == Glk.FILEMODE_WRITE_APPEND)
        System.err.println("Attempt to open memory stream with mode WriteAppend.");
    }

    public void setPosition(int p, int seekmode)
    {
      switch(seekmode)
      {
      case Glk.SEEKMODE_START:
        pos = p;
        break;
      case Glk.SEEKMODE_CURRENT:
        pos += p;
        break;
      case Glk.SEEKMODE_END:
        pos = len + p;
        break;
      default:
        System.err.println("setting position of memory stream: unknown seek mode");
      }
      if (pos < 0) pos = 0;
      if (pos > len) pos = len;
      buf.position(pos);
    }

    public int getChar()
    {
      if (canRead && buf != null && pos < len)
      {
        rcount++;
        pos ++;
        return buf.get() & 0xff;
      }
      else
      {
        return -1;
      }
    }
    public int getCharUni()
    {
      return getChar();
    }

    public int getBuffer(ByteBuffer b, int l)
    {
      byte[] arr;
      int num = Math.min(l, len - pos);

      if (canRead && buf != null)
      {
        if (num > 0)
        {
          arr = new byte[num];
          buf.get(arr);
          b.put(arr);
        }
        rcount += num;
        pos += num;
        return num;
      }
      else
      {
        return 0;
      }
    }

    public void putChar(int c)
    {
      if (canWrite)
      {
        wcount++;

        if (buf != null && pos < len)
        {
          buf.put((byte) c);
          pos++;
        }
      }
    }


    public void putCharUni(int c)
    {
      if (canWrite)
      {
        wcount++;

        if (buf != null && pos < len)
        {
          if (c > 0xff) {
              c = 0x3f;
          }
          buf.put((byte) c);
          pos++;
        }
      }
    }

    public void putBuffer(ByteBuffer b, int l)
    {
      byte[] arr;
      int num = Math.min(l, len - pos);

      if (canWrite)
      {
        wcount += l;
        if (num > 0)
        {
          if (buf != null)
          {
            arr = new byte[num];
            b.get(arr);
            buf.put(arr);
            pos += num;
          }
        }
      }
    }

    public void putBufferUni(ByteBuffer b, int l)
    {
      byte[] arr;
      int num = Math.min(l, len - pos);

      if (canWrite)
      {
        wcount += l;

        if (num > 0)
        {
          if (buf != null)
          {
            for (int i = 0; i < num; ++i) {
                int c = b.getInt();
                if (c > 0xff)
                    c = 0x3f;
                buf.put((byte)c);
            }
            pos += num;
          }
        }
      }
    }

    public void putString(String s)
    {
      int l = s.length();
      int num = Math.min(l, len - pos);

      if (canWrite)
      {
        wcount += l;

        if (buf != null)
        {
          for (int i = 0; i < num; i++)
            buf.put((byte) s.charAt(i));
          pos += num;
        }
      }
    }
  }


  static class WindowStream extends Stream
  {
    Window w;

    WindowStream(Window w)
    {
      super(Glk.FILEMODE_WRITE);
      this.w = w;
    }

    public void setHyperlink(int val)
    {
      if (w instanceof TextBufferWindow)
        ((TextBufferWindow) w).setHyperlink(val);
      else if (w instanceof TextGridWindow)
        ((TextGridWindow) w).setHyperlink(val);
    }

    public int getChar()
    {
      // NOOP
      return -1;
    }

    public void setPosition(int p, int seekmode)
    {
      // NOOP
    }

    public void setStyle(String stylename)
    {
      Style s = (Style) w.hintedStyles.get(stylename);
      if (s == null)
        s = (Style) w.hintedStyles.get("normal");

      w.setStyle(s);
    }

    public void putChar(int c)
    {
      wcount++;
      w.putChar((char) c);
      if (w.echo != null)
        w.echo.putChar(c);
    }

    public void putCharUni(int c)
    {
      wcount++;
      w.putCharUni(c);
      if (w.echo != null)
        w.echo.putCharUni(c);
    }

    public void putString(String s)
    {
      wcount += s.length();
      w.putString(s);
      if (w.echo != null)
        w.echo.putString(s);
    }

    public void putBuffer(ByteBuffer b, int len)
    {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < len; i++)
        sb.append((char) b.get());

      wcount += len;
      w.putString(sb.toString());
      if (w.echo != null)
        w.echo.putBuffer(b, len);
    }
    public void putBufferUni(ByteBuffer b, int len)
    {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < len; i++)
        sb.appendCodePoint(b.getInt());

      wcount += len;
      w.putString(sb.toString());
      if (w.echo != null)
        w.echo.putBufferUni(b, len);
    }

  }

  static class UnicodeMemoryStream extends Stream
  {
    ByteBuffer buf;
    int len;

    UnicodeMemoryStream(ByteBuffer buffer, int buflen, int mode)
    {
      super(mode);
      buf = buffer;
      len = buflen;

      if (mode == Glk.FILEMODE_WRITE_APPEND)
        System.err.println("Attempt to open memory stream with mode WriteAppend.");
    }

    public void setPosition(int p, int seekmode)
    {
      switch(seekmode)
      {
      case Glk.SEEKMODE_START:
        pos = p;
        break;
      case Glk.SEEKMODE_CURRENT:
        pos += p;
        break;
      case Glk.SEEKMODE_END:
        pos = len + p;
        break;
      default:
        System.err.println("setting position of memory stream: unknown seek mode");
      }
      if (pos < 0) pos = 0;
      if (pos > len) pos = len;
      buf.position(pos * 4);
    }

    public int getChar()
    {
      if (canRead && buf != null && pos < len)
      {
        rcount++;
        pos ++;
        int result = buf.getInt();
        if (result > 0xff) {
            return 0x3f;
        }
        return result;
      }
      else
      {
        return -1;
      }
    }

    public int getCharUni()
    {
      if (canRead && buf != null && pos < len)
      {
        rcount++;
        pos ++;
        return buf.getInt();
      }
      else
      {
        return -1;
      }
    }

    public int getBuffer(ByteBuffer b, int l)
    {
      int num = Math.min(l, len - pos);

      if (canRead && buf != null)
      {
        if (num > 0)
        {
          for (int i = 0; i < num; ++i) {
            int c = getChar();
            b.put((byte)c);
          }
        }
        return num;
      }
      else
      {
        return 0;
      }
    }

    public void putChar(int c)
    {
      if (canWrite)
      {
        wcount++;

        if (buf != null && pos < len)
        {
          buf.putInt(c);
          pos++;
        }
      }
    }


    public void putCharUni(int c)
    {
      if (canWrite)
      {
        wcount++;

        if (buf != null && pos < len)
        {
          buf.putInt(c);
          pos++;
        }
      }
    }

    public void putBuffer(ByteBuffer b, int l)
    {
      byte[] arr;
      int num = Math.min(l, len - pos);

      if (canWrite)
      {
        wcount += l;

        if (num > 0)
        {
          if (buf != null)
          {
            for (int i = 0; i < num; ++i) {
                buf.putInt(b.get());
            }
            pos += num;
          }
        }
      }
    }

    public void putBufferUni(ByteBuffer b, int l)
    {
      byte[] arr;
      int num = Math.min(l, len - pos);

      if (canWrite)
      {
        wcount += l;

        if (num > 0)
        {
          if (buf != null)
          {
            arr = new byte[num*4];
            b.get(arr);
            buf.put(arr);
            pos += num;
          }
        }
      }
    }

    public void putString(String s)
    {
      int l = s.length();
      int num = Math.min(l, len - pos);

      if (canWrite)
      {
        wcount += l;

        if (buf != null)
        {
          //fixme: astral
          for (int i = 0; i < num; i++)
            buf.putInt(s.charAt(i));
          pos += num;
        }
      }
    }
  }


  
  static class FileStream extends Stream
  {
    static final String TERMINATOR = System.getProperty("line.separator");
    static final int TERM_LEN = TERMINATOR.length();
    static final Charset UTF32 = Charset.forName("UTF-32BE");
    RandomAccessFile rf;
    FileChannel fc;
    Fileref f;
    boolean isText;
    boolean isEOF;
    boolean reading = false;
    ByteBuffer rbuf;
    ByteBuffer wbuf;
    boolean unicode;

    FileStream(Fileref ref, int fmode, boolean unicode)
    {
      super(fmode);
      this.unicode = unicode;
      f = ref;
      pos = 0;

      try
      {
        switch(fmode)
        {
        case Glk.FILEMODE_READ:
          f.f.createNewFile();
          rf = new RandomAccessFile(f.f, "r");
          rbuf = ByteBuffer.allocate(8192);
          break;
        case Glk.FILEMODE_WRITE:
          rf = new RandomAccessFile(f.f, "rw");
          rf.setLength(0L);
          wbuf = ByteBuffer.allocate(8192);
          break;
        case Glk.FILEMODE_READ_WRITE:
          if (!f.f.exists())
            f.f.createNewFile();
          rf = new RandomAccessFile(f.f, "rw");
          rbuf = ByteBuffer.allocate(8192);
          wbuf = ByteBuffer.allocate(8192);
          break;
        case Glk.FILEMODE_WRITE_APPEND:
          if (!f.f.exists())
            f.f.createNewFile();
          rf = new RandomAccessFile(f.f, "rw");
          pos = (int) rf.length();
          isEOF = true;
          rf.seek(pos);
          wbuf = ByteBuffer.allocate(8192);
          break;
        default:
          System.err.println("Attempt to open file stream with bad mode.");
        }
        if (rf != null)
          fc = rf.getChannel();
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
      }

      isText = ((f.u & Glk.FILEUSAGE_TEXT_MODE) != 0);
    }

    int fillReadBuf() throws IOException
    {
      int i;
      
      if (rbuf != null && !isEOF)
      {
        rbuf.clear();
        i = fc.read(rbuf);
        isEOF = (i == -1);
        rbuf.flip();
        return i;
      }
      return -1;
    }

    void commitWrite() throws IOException
    {
      if (wbuf != null && wbuf.position() != 0)
      {
        wbuf.flip();
        while(wbuf.hasRemaining())
          fc.write(wbuf);
        fc.force(false);
        wbuf.clear();
      }
    }

    void invalidateRead() throws IOException
    {
      if (rbuf != null)
      {
        if (rbuf.hasRemaining())
          rf.seek((long) pos);
        rbuf.position(rbuf.limit());
      }
    }

    public int getChar()
    {
      try
      {
        int i; 
        
        if (!reading)
        {
          commitWrite();
          reading = true;

          if (!isEOF)
          {
            i = 0;
            while (i == 0) {
              i = fillReadBuf();
            }
          }
        }

        if (!isEOF && !rbuf.hasRemaining())
        {
          i = 0;
          while (i == 0) {
            i = fillReadBuf();
          }
        }

        if (isEOF)
          return -1;

        int c;
        int b = rbuf.get();
        pos++;
        rcount++;
        
        if (isText)
        {
          if ((char) b == '\r')
          {
            b = '\n';

            if (!rbuf.hasRemaining())
            {
              i = 0;
              while (i == 0) {
                i = fillReadBuf();
              }
            }
            if (!isEOF)
            {
              c = rbuf.get();

              if ((char) c != '\n')
                rbuf.position(rbuf.position() - 1);
              else
                pos++;
            }
          }
        }
        return b;
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
        return -1;
      }
    }

    public int getBuffer(ByteBuffer b, int len)
    {
      int i;
      int iRead;
      int tot = len;

      try
      {
        if (isText)
        {
          int n = super.getBuffer(b, len);
          rcount += n;
          return n;
        }
        else
        {
          if (!reading)
          {
            commitWrite();
            reading = true;

            if (!isEOF)
            {
              i = 0;
              while (i == 0) {
                i = fillReadBuf();
              }
            }
          }

          if (isEOF)
            return -1;

          while (tot > 0)
          {
            iRead = rbuf.remaining();

            if (tot >= iRead)
            {
              b.put(rbuf);
              tot -= iRead;

              if (tot > 0)
              {
                i = 0;
                while (i == 0) {
                  i = fillReadBuf();
                } 
                
                if (isEOF)
                  break;
              }
            }
            else
            {
              iRead = rbuf.limit();
              rbuf.limit(rbuf.position() + tot);
              b.put(rbuf);
              rbuf.limit(iRead);
              tot = 0;
            }
          }
          pos += (len - tot);
          return len - tot;
        }
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
        return -1;
      }
    }

    public void putChar(int c)
    {
      try
      {
        if (reading)
        {
          invalidateRead();
          reading = false;
        }

        if (!wbuf.hasRemaining())
          commitWrite();

        if (isText && c == '\n')
        {
          for (int i = 0; i < TERM_LEN; i++)
          if (wbuf.remaining() < TERM_LEN)
            rf.write(TERMINATOR.charAt(i));
          else
            wbuf.put((byte) TERMINATOR.charAt(i));
          pos += TERM_LEN;
        }
        else
        {
          if (unicode) {
            wbuf.put((byte)((c & 0xff000000) >> 24));
            wbuf.put((byte)((c & 0xff0000) >> 16));
            wbuf.put((byte)((c & 0xff00) >> 8));
            wbuf.put((byte)(c & 0xff));
          } else {
            wbuf.put((byte) c);
          }
          pos++;
        }
        wcount++;
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
      }
    }

    public void putCharUni(int c) {
        putChar(c);
    }

    public void putBuffer(ByteBuffer b, int len)
    {
      int tot = len;
      int l;

      try
      {
        if (isText)
        {
          super.putBuffer(b, len);
        }
        else
        {
          if (reading)
          {
            invalidateRead();
            reading = false;
          }

          b.limit(b.position() + len);
          while (tot > 0)
          {
            if (!wbuf.hasRemaining())
              commitWrite();
            
            if (tot <= wbuf.remaining())
            {
              wbuf.put(b);
              tot = 0;
            }
            else
            {
              l = b.limit();
              tot -= wbuf.remaining();
              b.limit(b.position() + wbuf.remaining());
              wbuf.put(b);
              b.limit(l);
            }
          }
          b.clear();
        }
        wcount += len;
        pos += len;
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
      }
    }
    public void putBufferUni(ByteBuffer b, int len)
    {
      int tot = len;
      int l;

      try
      {
        if (isText)
        {
          super.putBufferUni(b, len);
        }
        else
        {
          if (reading)
          {
            invalidateRead();
            reading = false;
          }

          b.limit(b.position() + len * 4);
          while (tot > 0)
          {
            if (!wbuf.hasRemaining())
              commitWrite();
            
            if (tot <= wbuf.remaining())
            {
              wbuf.put(b);
              tot = 0;
            }
            else
            {
              l = b.limit();
              tot -= wbuf.remaining();
              b.limit(b.position() + wbuf.remaining());
              wbuf.put(b);
              b.limit(l);
            }
          }
          b.clear();
        }
        wcount += len;
        pos += len;
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
      }
    }

    public void putString(String s)
    {
      if (isText)
        super.putString(s);
      else
        putBuffer(ByteBuffer.wrap(s.getBytes()), s.length());
    }

    public void putStringUni(String s)
    {
      if (isText)
        super.putStringUni(s);
      else
        putBuffer(ByteBuffer.wrap(s.getBytes(UTF32)), s.length());
    }

    public Result close()
    {
      try
      {
        if (filemode != Glk.FILEMODE_READ)
        {
          if (!reading)
            commitWrite();
          else
            fc.force(true);
        }
        fc.close();
        rf.close();
        return super.close();
      }
      catch(IOException eio)
      {
        eio.printStackTrace();
        return null;
      }
    }

    public int getPosition()
    {
      return pos;
    }

    public void setPosition(int p, int seekmode)
    {
      try
      {
        if (!reading)
        {
          commitWrite();
          reading = true;
        }
        else
        {
          invalidateRead();
          reading = false;
        }

        switch(seekmode)
        {
        case Glk.SEEKMODE_START:
          pos = p;
          break;
        case Glk.SEEKMODE_CURRENT:
          pos += p;
          break;
        case Glk.SEEKMODE_END:
          pos = (int) rf.length() + p;
          break;
        default:
          System.err.println("setting position of file stream: unknown seek mode");
        }
        rf.seek((long) pos);
        isEOF = (pos == rf.length());
      }
      catch(IOException eio)
      {
        System.err.println(eio);
        eio.printStackTrace();
      }
    }
  }
}
