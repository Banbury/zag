package org.p2c2e.blorb;

import java.io.*;

public class DepthPalette implements Palette
{
  int _iDepth;

  public DepthPalette(BlorbFile.Chunk c) throws IOException
  {
    InputStream in = c.getData();
    _iDepth = in.read();
    in.close();
  }

  public boolean isColorList()
  {
    return false;
  }

  public int getColorDepth()
  {
    return _iDepth;
  }
}
