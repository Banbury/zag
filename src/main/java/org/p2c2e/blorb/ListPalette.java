package org.p2c2e.blorb;

import java.util.*;
import java.io.*;

public class ListPalette implements Palette
{
  LinkedList _li = new LinkedList();
  
  ListPalette(BlorbFile.Chunk c) throws IOException
  {
    InputStream in = c.getData();
    int i = in.read();

    while (i != -1)
    {
      _li.add(new Palette.Color(i, in.read(), in.read()));
      i = in.read();
    }

    in.close();
  }

  public boolean isColorList()
  {
    return true;
  }

  public Iterator iterator()
  {
    return _li.iterator();
  }
}
