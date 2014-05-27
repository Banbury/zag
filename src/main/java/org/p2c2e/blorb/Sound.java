package org.p2c2e.blorb;

public class Sound
{
  BlorbFile.Chunk _c;
  int _loops;

  Sound(BlorbFile.Chunk c, int loops)
  {
    _c = c;
    _loops = loops;
  }

  public BlorbFile.Chunk getChunk()
  {
    return _c;
  }

  public int getLoops()
  {
    return _loops;
  }
}
