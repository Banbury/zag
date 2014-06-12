package org.p2c2e.util;

import java.io.*;

public abstract class Bytes
{
  static final byte[] BUFFER = new byte[8192];

  public static final int readInt(InputStream s) throws IOException
  {
    return (s.read() << 24) | (s.read() << 16) | (s.read() << 8) | s.read();
  }

  public static final byte[] getBytes(InputStream in) throws IOException
  {
    synchronized (BUFFER)
    {
      int i = 0;
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      while (i >= 0)
      {
        i = in.read(BUFFER);
        if (i > 0)
          out.write(BUFFER, 0, i);
      }

      out.flush();
      return out.toByteArray();
    }
  }
}
