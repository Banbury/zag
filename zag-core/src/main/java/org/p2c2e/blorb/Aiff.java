package org.p2c2e.blorb;

import java.io.*;

public class Aiff
{
  static final int AIFF = 0x41494646;
  static final int COMM = 0x434f4d4d;
  static final int SSND = 0x53534e44;
  static final int MARK = 0x4d41524b;
  static final int INST = 0x494e5354;

  int numChannels;
  int numSampleFrames;
  int sampleSize;
  double sampleRate;
  int offset;
  int blockSize;
  int sustainLoopPlayMode;
  int sustainLoopBegin;
  int sustainLoopEnd;
  byte[] soundData;
  Marker[] markers;

  public Aiff(BlorbFile.Chunk c) throws IOException
  {
    int len = c.getDataSize();
    DataInputStream in = 
      new DataInputStream(new BufferedInputStream(c.getData()));

    int ctype;
    boolean okay = true;
    okay &= len >= 4;
    if (okay) 
    {
      ctype = in.readInt();
      len -= 4;
      okay &= (ctype == AIFF);
    }

    if (!okay)
      throw new IOException("Not an AIFF");

    parseAiff(len, in);
    in.close();
  }

  public int getMarkerPos(int id)
  {
    for (int i = 0; i < markers.length; i++)
    {
      if (markers[i].id == id)
        return markers[i].pos;
    }
    return -1;
  }

  public int getNumChannels()
  {
    return numChannels;
  }

  public int getNumSampleFrames()
  {
    return numSampleFrames;
  }

  public int getSampleSize()
  {
    return sampleSize;
  }

  public double getSampleRate()
  {
    return sampleRate;
  }

  public int getOffset()
  {
    return offset;
  }

  public int getBlockSize()
  {
    return blockSize;
  }

  public byte[] getSoundData()
  {
    return soundData;
  }

  public Marker[] getMarkers()
  {
    return markers;
  }

  public int getSustainLoopPlayMode()
  {
    return sustainLoopPlayMode;
  }

  public int getSustainLoopBegin()
  {
    return sustainLoopBegin;
  }

  public int getSustainLoopEnd()
  {
    return sustainLoopEnd;
  }

  void parseAiff(int len, DataInputStream in) throws IOException
  {
    byte[] waste;
    int ctype;
    int clen;
    boolean done = len < 4;

    while (!done)
    {
      ctype = in.readInt();
      clen = in.readInt();
      len -= 8;

      if (len < clen)
        throw new IOException("Corrupted AIFF");

      switch(ctype)
      {
      case COMM:
        numChannels = in.readUnsignedShort();
        numSampleFrames = in.readInt();
        sampleSize = in.readUnsignedShort();
        int exponent = in.readUnsignedShort();
        boolean neg = (exponent < 0);
        exponent &= 0x7fff;
        long mantissa = in.readLong();

        // if this is an aiff-c, or something
        if (clen > 18)
        {
          waste = new byte[clen - 18];
          in.readFully(waste);
        }

        // thanks to Greg Guerin

        if (exponent == 0x7fff)
        {
          if ((mantissa & Long.MAX_VALUE) == 0L)
            sampleRate = 
              (neg) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
          else
            sampleRate = Double.NaN;
          break;
        }
        if (mantissa >= 0L)
        {
          sampleRate = 0.0D;
          break;
        }          

        exponent = exponent - 16383 + 1023;
        if (exponent <= 0)
        {
          sampleRate = 0.0D;
          break;
        }
        if (exponent >= 2047)
        {
          sampleRate = 
            (neg) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
          break;
        }
        
        int sign = (neg) ? 0xfffff800 : 0x00000000;
        long signexp = ((long) (sign | exponent)) << 52;
        mantissa = (mantissa & Long.MAX_VALUE) >> 11;
        sampleRate = Double.longBitsToDouble(signexp | mantissa);
        break;
      case SSND:
        offset = in.readInt();
        blockSize = in.readInt();
        soundData = new byte[clen - 8];
        in.readFully(soundData);
        break;
      case MARK:
        markers = new Marker[in.readUnsignedShort()];
        for (int i = 0; i < markers.length; i++)
        {
          Marker m = new Marker(in.readUnsignedShort(),
                                in.readInt());
          int slen = ((int) in.read()) & 0xff;
          if ((slen & 1) == 0)
            slen++;

          waste = new byte[slen];
          in.readFully(waste);
          markers[i] = m;
        }
        break;
      case INST:
        in.readLong();

        sustainLoopPlayMode = in.readUnsignedShort();
        sustainLoopBegin = in.readUnsignedShort();
        sustainLoopEnd = in.readUnsignedShort();

        in.readInt();
        in.readShort();

        break;
      default:
        waste = new byte[clen];
        in.readFully(waste);
      }

      len -= clen;

      if ((clen & 1) != 0)
      {
        in.read();
        len--;
      }
      done = len < 4;
    }
  }

  static class Marker
  {
    public int id;
    public int pos;

    Marker(int id, int pos)
    {
      this.id = id;
      this.pos = pos;
    }
  }
}
