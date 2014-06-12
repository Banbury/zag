package org.p2c2e.blorb;

public class Pict
{
  BlorbFile.Chunk _c;
  Resolution.ImgData _imgData;

  Pict(BlorbFile.Chunk c, Resolution.ImgData imgData)
  {
    _c = c;
    _imgData = imgData;
  }

  public BlorbFile.Chunk getChunk()
  {
    return _c;
  }

  public Resolution.ImgData getImgData()
  {
    return _imgData;
  }
}
