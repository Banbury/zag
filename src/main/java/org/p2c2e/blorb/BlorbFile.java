package org.p2c2e.blorb;

import org.p2c2e.util.*;
import java.util.*;
import java.io.*;

public class BlorbFile
{
  static final char[] FORM_ARR = {'F', 'O', 'R', 'M'};
  static final char[] IFRS_ARR = {'I', 'F', 'R', 'S'};
  static final char[] RIDX_ARR = {'R', 'I', 'd', 'x'};
  static final char[] PICT_ARR = {'P', 'i', 'c', 't'};
  static final char[] SND_ARR = {'S', 'n', 'd', ' '};
  static final char[] EXEC_ARR = {'E', 'x', 'e', 'c'};
  static final char[] RELN_ARR = {'R', 'e', 'l', 'N'};
  static final char[] IFHD_ARR = {'I', 'F', 'h', 'd'};
  static final char[] PLTE_ARR = {'P', 'l', 't', 'e'};
  static final char[] RESO_ARR = {'R', 'e', 's', 'o'};
  static final char[] LOOP_ARR = {'L', 'o', 'o', 'p'};
  static final char[] AUTH_ARR = {'A', 'U', 'T', 'H'};
  static final char[] COPY_ARR = {'(', 'c', ')', ' '};
  static final char[] ANNO_ARR = {'A', 'N', 'N', 'O'};

  public static final int PICT = 0;
  public static final int SND = 1;
  public static final int EXEC = 2;

  public static final String ZCOD = "ZCOD";
  public static final String GLUL = "GLUL";
  public static final String FORM = "FORM";
  public static final String RELN = "RelN";
  public static final String IFHD = "IFhd";
  public static final String PLTE = "Plte";
  public static final String RESO = "Reso";
  public static final String LOOP = "Loop";
  public static final String AUTH = "AUTH";
  public static final String COPY = "(c) ";
  public static final String ANNO = "ANNO";
  public static final String AIFF = "AIFF";
  public static final String MOD = "MOD ";
  public static final String SONG = "SONG";
  public static final String JPEG = "JPEG";
  public static final String PNG = "PNG ";

  Looping _loop;
  Resolution _res;
  File _f;
  TreeMap _mOpt = new TreeMap();
  TreeMap[] _arMaps = new TreeMap[3];

  public BlorbFile(File f) throws FileNotFoundException, IOException
  {
    int iRecords;
    int iIndexLen;
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
    byte[] arId = new byte[4];

    if (readMatch(in, arId, FORM_ARR) && (Bytes.readInt(in) > 0) && 
        readMatch(in, arId, IFRS_ARR) && readMatch(in, arId, RIDX_ARR))
    {
      // At least it looks like a blorb file...
      _f = f;
      iIndexLen = Bytes.readInt(in);
      // fill the resource map
      iRecords = Bytes.readInt(in);

      for (int i = 0; i < iRecords; i++)
      {
        if (readArray(in, arId) < 0)
          throw new IOException("Blorb file corrupted.");

        if (match(arId, PICT_ARR))
          mapResource(in, PICT);
        else if (match(arId, SND_ARR))
          mapResource(in, SND);
        else if (match(arId, EXEC_ARR))
          mapResource(in, EXEC);
        else
          in.skip(8);
      }

      // Now we should search through the file for chunks
      // yeah, the +20 thing is nasty, I know...
      mapChunks(in, arId, iIndexLen + 20);
      in.close();
    }
    else
    {
      throw new NotBlorbException("Not a blorb file");
    }
  }


  void mapResource(InputStream in, int iType) 
    throws IOException
  {
    Integer oiNum = new Integer(Bytes.readInt(in));
    Integer oiPos = new Integer(Bytes.readInt(in));
    TreeMap m = _arMaps[iType];

    if (m == null)
      m = _arMaps[iType] = new TreeMap();

    m.put(oiNum, oiPos);
  }

  void mapChunks(InputStream in, byte[] arId, int iPos) 
    throws IOException
  {
    String stId;
    int iSkip;
    int iSkipped;
    Integer oiPos;
    LinkedList li;
    boolean boEOF = (readArray(in, arId) < 0);

    while (!boEOF)
    {
      oiPos = new Integer(iPos);
      stId = new String(arId);
      li = (LinkedList) _mOpt.get(stId);

      if (li == null)
      {
        li = new LinkedList();
        _mOpt.put(stId, li);
      }
      li.add(oiPos);

      iPos += 4;
      iSkip = Bytes.readInt(in);
      if (iSkip % 2 != 0)
        iSkip++;
      iPos += 4;
      iSkipped = 0;
      while (iSkipped < iSkip)
        iSkipped += (int) in.skip(iSkip - iSkipped);
      iPos += iSkip;

      boEOF = readArray(in, arId) < arId.length;
    }
  }

  public void dumpBlorb()
  {
    Iterator it, it2;
    Map.Entry entry;

    dumpResource(PICT, "Pict");
    dumpResource(SND, "Snd");
    dumpResource(EXEC, "Exec");

    System.out.println("Chunks by type:");
    it = _mOpt.entrySet().iterator();
    while (it.hasNext())
    {
      entry = (Map.Entry) it.next();
      System.out.println(entry.getKey() + ":");
      it2 = ((LinkedList) entry.getValue()).iterator();
      while (it2.hasNext())
        System.out.println(" " + Integer.toHexString(((Integer) it2.next()).intValue()));
    }
  }

  void dumpResource(int iType, String stType)
  {
    Iterator it;
    Map.Entry entry;

    if (_arMaps[iType] != null)
    {
      System.out.println(stType + " index:");
      it = _arMaps[iType].entrySet().iterator();
      while (it.hasNext())
      {
        entry = (Map.Entry) it.next();
        System.out.println(entry.getKey() + ": " + Integer.toHexString(((Integer) entry.getValue()).intValue()));
      }
    }
    System.out.println();    
  }

  public Iterator iterateByType(String stType) throws IOException
  {
    Iterator it;
    ArrayList copy;
    int iSize;
    List li = (List) _mOpt.get(stType);
    if (li == null) li = Collections.EMPTY_LIST;

    iSize = li.size();
    copy = new ArrayList(iSize);
    it = li.iterator();
    for (int i = 0; i < iSize; i++)
      copy.add(new Chunk(((Integer) it.next()).intValue(), stType));

    return copy.iterator();
  }

  public BlorbFile.Chunk getByUsage(int iUsage, int iNumber) throws IOException
  {
    Integer oiPos;
    TreeMap m = _arMaps[iUsage];

    if (m != null)
    {
      oiPos = (Integer) m.get(new Integer(iNumber));
      if (oiPos != null)
        return new Chunk(oiPos.intValue(), null);
      else
        return null;
    }
    else
    {
      return null;
    }
  }

  public Looping getLooping() throws IOException
  {
    Chunk c;
    Iterator it;

    if (_loop == null)
    {
      it = iterateByType(LOOP);
      if (it.hasNext())
      {
        c = (Chunk) it.next();
        _loop = new Looping(c);
        return _loop;
      }
      else
      {
        return null;
      }
    }
    else
    {
      return _loop;
    }
  }

  public Resolution getResolution() throws IOException
  {
    Chunk c;
    Iterator it;

    if (_res == null)
    {
      it = iterateByType(RESO);
      if (it.hasNext())
      {
        c = (Chunk) it.next();
        _res = new Resolution(c);
        return _res;
      }
      else
      {
        return null;
      }
    }
    else
    {
      return _res;
    }
  }

  public Sound getSound(int id) throws IOException
  {
    return new Sound(getByUsage(SND, id), getLooping().getLoops(id));
  }

  public Pict getPict(int id) throws IOException
  {
    return new Pict(getByUsage(PICT, id), getResolution().getImgData(id));
  }

  public Palette getPalette() throws IOException
  {
    Chunk c;
    Iterator it = iterateByType(PLTE);

    if (it.hasNext())
    {
      c = (Chunk) it.next();
      if (c.getDataSize() == 1)
        return new DepthPalette(c);
      else
        return new ListPalette(c);
    }
    else
    {
      return null;
    }
  }
      
  static boolean match(byte[] ar, char[] test)
  {
    for (int i = 0; i < ar.length; i++)
    {
      if (ar[i] != (byte) test[i])
        return false;
    }
    return true;
  }

  static int readArray(InputStream in, byte[] ar) throws IOException
  {
    int iTotal = 0;
    int iRead;
    
    while (iTotal < ar.length)
    {
      iRead = in.read(ar, iTotal, ar.length - iTotal);
      if (iRead == -1)
        return -1;
      iTotal += iRead;
    }
    return ar.length;
  }

  static boolean readMatch(InputStream in, byte[] ar, char[] test)
    throws IOException
  {
    int i = readArray(in, ar);
    if (i == -1)
      return false;
    else
      return match(ar, test);
  }



  public class Chunk
  {
    int _iSize;
    int _iDataPos;
    String _stType;

    Chunk(int iChunkPos, String stType) throws IOException
    {
      RandomAccessFile f = new RandomAccessFile(BlorbFile.this._f, "r");
      if (stType == null)
      {
        discoverType(f, iChunkPos);
      }
      else
      {
        _stType = stType;
        f.seek(iChunkPos + 4);
      }

      _iSize = (f.read() << 24) | (f.read() << 16) 
        | (f.read() << 8) | f.read();
      _iDataPos = (int) f.getFilePointer();
      f.close();
    }

    private void discoverType(RandomAccessFile f, int iChunkPos)
      throws IOException
    {
      byte[] b = new byte[4];
      f.seek(iChunkPos);
      b[0] = (byte) f.read();
      b[1] = (byte) f.read();
      b[2] = (byte) f.read();
      b[3] = (byte) f.read();
      _stType = new String(b, 0, b.length);
    }

    public int getDataSize()
    {
      return _iSize;
    }

    public int getDataPosition()
    {
      return _iDataPos;
    }

    public int getRawSize()
    {
      return _iSize + 8;
    }

    public String getDataType()
    {
      return _stType;
    }

    public InputStream getData() throws IOException
    {
      return new BlorbInputStream(BlorbFile.this._f, _iDataPos, _iSize);
    }

    public InputStream getRawData() throws IOException
    {
      return new BlorbInputStream(BlorbFile.this._f, 
                                  _iDataPos - 8, _iSize + 8);
    }
  }

  public static class BlorbInputStream extends FilterInputStream
  {
    int _iSize;
    int _iPos;
    int _iStart;

    BlorbInputStream(File f, int iDataPos, int iSize) throws IOException
    {
      super(new FileInputStream(f));
      _iSize = iSize;
      _iStart = _iPos = iDataPos;

      int iSkipped = 0;
      while (iSkipped < _iPos)
        iSkipped += in.skip(_iPos - iSkipped);
    }

    public int available() throws IOException
    {
      return Math.min(in.available(), (_iStart + _iSize) - _iPos); 
    }

    public int read() throws IOException
    {
      if (_iPos < _iStart + _iSize)
      {
        int i = super.read();
        if (i != -1)
          _iPos++;
        return i;
      }
      return -1;
    }
    
    public int read(byte[] b) throws IOException
    {
      return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException
    {
      int iLeft = (_iStart + _iSize) - _iPos;
      int reallen = Math.min(len, iLeft);

      if (reallen > 0)
      {
        int i = super.read(b, off, reallen);
        if (i != -1)
          _iPos += i;
        return i;
      }
      else
      {
        return -1;
      }
    }

    public long skip(long n) throws IOException
    {
      long l = super.skip(n);

      if (_iPos + (int) l >= _iStart + _iSize)
      {
        _iPos = _iStart + _iSize;
        return (_iStart + _iSize) - _iPos;
      }
      else
      {
        _iPos += (int) l;
        return l;
      }
    }
  }
}

