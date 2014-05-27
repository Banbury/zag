package org.p2c2e.blorb;

import org.p2c2e.util.*;
import java.io.*;
import java.util.*;

public class Resolution
{
  int _px, _py;
  int _minx, _miny;
  int _maxx, _maxy;
  TreeMap _m = new TreeMap();

  Resolution(BlorbFile.Chunk c) throws IOException
  {
    init(c.getDataSize(), c.getData());
  }

  public int getStdWindowWidth() { return _px; }
  public int getStdWindowHeight() { return _py; }
  public int getMinWindowWidth() { return _minx; }
  public int getMinWindowHeight() { return _miny; }
  public int getMaxWindowWidth() { return _maxx; }
  public int getMaxWindowHeight() { return _maxy; }

  public Resolution.ImgData getImgData(int iId)
  {
    return (ImgData) _m.get(new Integer(iId));
  }

  void init(int iChunkSize, InputStream f) throws IOException
  {
    int iLeft;
    int id;
    int iNum, iDem;
    ImgData e;

    _px = Bytes.readInt(f);
    _py = Bytes.readInt(f);
    _minx = Bytes.readInt(f);
    _miny = Bytes.readInt(f);
    _maxx = Bytes.readInt(f);
    _maxy = Bytes.readInt(f);

    iLeft = iChunkSize - 24;
    while (iLeft > 0)
    {
      id = Bytes.readInt(f);
      e = new ImgData();
      e._stdNum = Bytes.readInt(f);
      e._stdDem = Bytes.readInt(f);
      e._minNum = Bytes.readInt(f);
      e._minDem = Bytes.readInt(f);
      e._maxNum = Bytes.readInt(f);
      e._maxDem = Bytes.readInt(f);
      _m.put(new Integer(id), e);
      iLeft -= 28;
    }

    f.close();
  }



  public static class ImgData
  {
    int _stdNum, _stdDem;
    int _minNum, _minDem;
    int _maxNum, _maxDem;

    public int getStdNumerator() { return _stdNum; }
    public int getStdDenominator() { return _stdDem; }
    public int getMinNumerator() { return _minNum; }
    public int getMinDenominator() { return _minDem; }
    public int getMaxNumerator() { return _maxNum; }
    public int getMaxDenominator() { return _maxDem; }
  }
}
