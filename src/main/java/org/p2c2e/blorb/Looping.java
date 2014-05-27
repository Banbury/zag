package org.p2c2e.blorb;

import org.p2c2e.util.*;
import java.util.*;
import java.io.*;

public class Looping
{
  TreeMap _m = new TreeMap();

  Looping(BlorbFile.Chunk c) throws IOException
  {
    int left = c.getDataSize();
    InputStream in = c.getData();

    while (left > 0)
    {
      _m.put(new Integer(Bytes.readInt(in)), new Integer(Bytes.readInt(in)));
      left -= 8;
    }

    in.close();
  }

  public int getLoops(int iId)
  {
    Integer oi = (Integer) _m.get(new Integer(iId));
    return (oi == null) ? -1 : oi.intValue();
  }
}
