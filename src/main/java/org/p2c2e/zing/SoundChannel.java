package org.p2c2e.zing;

import java.io.*;
import java.nio.*;
import javax.sound.sampled.*;
import micromod.*;
import micromod.resamplers.*;
import micromod.output.*;
import micromod.output.converters.*;
import org.p2c2e.blorb.*;

public class SoundChannel
{
  PlayerThread pt;
  int vol;
  
  public SoundChannel()
  {
    vol = 0x10000;
    pt = null;
  }

  public void destroy() throws Exception
  {
    stop();
    pt = null;
  }

  public boolean play(int soundId) throws Exception
  {
    return play(soundId, 1, 0);
  }

  public boolean play(int soundId, int iRepeat, int iNotify) throws Exception
  {
    if (Glk.BLORB_FILE == null)
      return false;

    if (pt != null)
      stop();

    String stType;
    BlorbFile.Chunk c =  Glk.BLORB_FILE.getByUsage(BlorbFile.SND, soundId);

    if (c == null)
      return false;
    
    stType = c.getDataType();

    if ("FORM".equals(stType))
      return playAIFF(c, iRepeat, soundId, iNotify);
    else if ("MOD ".equals(stType))
      return playMOD(c, iRepeat, soundId, iNotify);
    else if ("SONG".equals(stType))
      return playSONG(Glk.BLORB_FILE, c, iRepeat, soundId, iNotify);
    else
      return false;
  }

  public void stop() throws Exception
  {
    if (pt != null)
      pt.stopPlaying();
  }

  public void setVolume(int iVol)
  {
    this.vol = iVol;
    if (pt != null)
      pt.setVolume(iVol);
  }

  synchronized void donePlaying()
  {
    pt = null;
  }

  boolean playAIFF(BlorbFile.Chunk c, int iRepeat, int soundId, int iNotify) 
    throws Exception
  {
    AudioInputStream in = AudioSystem.getAudioInputStream(new BufferedInputStream(c.getRawData()));
    AudioFormat audioFormat = in.getFormat();
    DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
    SourceDataLine l = (SourceDataLine) AudioSystem.getLine(info);

    l.open(audioFormat);
    
    AIFFThread at = new AIFFThread(l, c, in, iRepeat, soundId, iNotify);
    pt = at;
    pt.setVolume(vol);
    at.start();
    return true;
  }

  boolean playSONG(BlorbFile bf,
                   BlorbFile.Chunk c, int iRepeat, int soundId, int iNotify)
    throws Exception
  {
    MODThread mt;
    MicroMod microMod;
    JavaSoundOutputDevice out =  
      new JavaSoundOutputDevice(new SS16LEAudioFormatConverter(), 44100, 1000);
    Module module = ModuleLoader.read(new DataInputStream(c.getData()));
    
    // for some reason, the first two samples are always blanked out in mods
    for (int i = 2; i < 32; i++)
    {
      Instrument inst = module.getInstrument(i);
      if (inst != null)
      {
        String name = inst.name.trim();
        if ("".equals(name))
          continue;

        if (name.length() < 4)
          return false;
        int id = Integer.parseInt(name.substring(3, name.length()));
        BlorbFile.Chunk ac = bf.getByUsage(BlorbFile.SND, id);
        Aiff aiff = new Aiff(ac);
        byte[] data = aiff.getSoundData();
        int numChannels = aiff.getNumChannels();
        int sampleSize = aiff.getSampleSize();
        int numSampleFrames = aiff.getNumSampleFrames();
        int offset = aiff.getOffset();
        ByteBuffer newData = ByteBuffer.allocate(numSampleFrames * 2);

        inst.looped = (aiff.getSustainLoopPlayMode() != 0);
        inst.sampleSize = 16;

        if (numChannels == 1 && sampleSize <= 8)
        {
          for (int j = 0; j < numSampleFrames; j++)
            newData.putShort((short) (data[j + offset] << 8));
        }
        else
        {
          int k = offset;
          for (int ix = 0; ix < numSampleFrames; ix++)
          {
            for (int jx = 0; jx < numChannels; jx++)
            {
              int sample;
              if (sampleSize <= 8)
                sample = data[k++] << 24;
              else if (sampleSize <= 16)
                sample = 
                  ((data[k++] << 8) | (data[k++] & 0xff)) << 16;
              else if (sampleSize <= 24)
                sample =
                  ((data[k++] << 16) | ((data[k++] << 8) & 0xff) |
                   (data[k++] & 0xff)) << 8;
              else
                sample = (data[k++] << 24) |
                  ((data[k++] << 16) & 0xff) |
                  ((data[k++] << 8) & 0xff) |
                  (data[k++] & 0xff);
              if (jx == 0)
                newData.putShort((short) (sample >>> 16));
            }
          }
        }
        inst.data = newData.array();
        if (inst.looped)
        {
          inst.loopStart = aiff.getMarkerPos(aiff.getSustainLoopBegin());
          inst.sampleEnd = inst.loopStart + 
            (aiff.getMarkerPos(aiff.getSustainLoopEnd()) - 
             aiff.getMarkerPos(aiff.getSustainLoopBegin()));
        }
        else
        {
          inst.loopStart = 0;
          inst.sampleEnd = numSampleFrames;
        }
      }
    }
    microMod = new MicroMod(module, out, new FIRResampler(16));
    
    mt = new MODThread(microMod, out, iRepeat, soundId, iNotify);
    pt = mt;
    pt.setVolume(vol);
    
    mt.start();
    return true;
  }
  
  boolean playMOD(BlorbFile.Chunk c, int iRepeat, int soundId, int iNotify) 
    throws Exception
  {
    MODThread mt;
    JavaSoundOutputDevice out =  
      new JavaSoundOutputDevice(new SS16LEAudioFormatConverter(), 44100, 1000);
    Module module = ModuleLoader.read(new DataInputStream(c.getData()));
    MicroMod microMod = new MicroMod(module, out, new FIRResampler(16));

    mt = new MODThread(microMod, out, iRepeat, soundId, iNotify);
    pt = mt;
    pt.setVolume(vol);

    mt.start();
    return true;
  }


  public interface PlayerThread extends Runnable
  {
    public abstract void setVolume(int vol);

    public abstract void stopPlaying() throws Exception;
  }

  class AIFFThread extends Thread implements PlayerThread, LineListener
  {
    private static final int BUFFER_SIZE = 128000;

    int iRepeat;
    int soundId;
    int iNotify;
    boolean stopped;
    BlorbFile.Chunk c;
    SourceDataLine l;
    AudioInputStream in;

    AIFFThread(SourceDataLine l, BlorbFile.Chunk c, AudioInputStream in, 
               int iRepeat, int soundId, int iNotify)
    {
      this.l = l;
      this.in = in;
      this.c = c;
      this.iRepeat = iRepeat;
      this.soundId = soundId;
      this.iNotify = iNotify;

      l.addLineListener(this);
      stopped = false;
    }

    public void setVolume(int vol)
    {
      FloatControl ctl = (FloatControl) l.getControl(FloatControl.Type.MASTER_GAIN);
      double gain = (double) vol / (double) 0x10000;
      float dB = (float) (Math.log(gain) / Math.log(10.0) * 20);

      if (ctl != null)
        ctl.setValue(dB);
    }

    public synchronized void stopPlaying() throws Exception
    {
      if (!stopped)
      {
        l.flush();
        l.drain();
        l.stop();
        l.close();
        stopped = true;
        donePlaying();
      }
    }

    public void update(LineEvent e)
    {
      try
      {
        LineEvent.Type t = e.getType();
        if (t == LineEvent.Type.STOP && !stopped)
        {
          l.close();
          donePlaying();
        
          if (iNotify != 0)
          {
            Glk.GlkEvent ev = new Glk.GlkEvent();
            ev.type = Glk.EVTYPE_SOUND_NOTIFY;
            ev.win = null;
            ev.val1 = soundId;
            ev.val2 = iNotify;
            Glk.addEvent(ev);
          }
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }

    public void run()
    {
      int read = 0;
      byte[] data = new byte[BUFFER_SIZE];

      l.start();

      try
      {
        while (iRepeat-- != 0)
        {
          while (read >= 0)
          {
            read = in.read(data, 0, data.length);
            l.write(data, 0, read);
          }
          if (iRepeat != 0)
          {
            in = AudioSystem.getAudioInputStream(new BufferedInputStream(c.getRawData()));
            read = 0;
          }
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
  }

  class MODThread extends Thread implements PlayerThread
  {
    int soundId;
    int iNotify;
    int iRepeat;
    boolean running;
    boolean stopped;
    JavaSoundOutputDevice out;
    MicroMod mm;

    MODThread(MicroMod mm, JavaSoundOutputDevice out, 
              int iRepeat, int soundId, int iNotify)
    {
      this.mm = mm;
      this.out = out;
      this.soundId = soundId;
      this.iNotify = iNotify;
      this.iRepeat = iRepeat;

      running = false;
      stopped = false;
    }

    public synchronized void setVolume(int vol)
    {
      Line l = out.getLine();
      FloatControl ctl = 
        (FloatControl) l.getControl(FloatControl.Type.MASTER_GAIN);
      double gain = (double) vol / (double) 0x10000;
      float dB = (float) (Math.log(gain) / Math.log(10.0) * 20);

      if (ctl != null)
        ctl.setValue(dB);
    }

    public synchronized void stopPlaying()
    {
      if (!stopped)
      {
        running = false;
        stopped = true;
        out.stop();
        out.close();
        donePlaying();
      }
    }

    public void run()
    {
      out.start();

      for (int i = 0; !stopped && (iRepeat == -1 || i < iRepeat); i++)
      {
        running = true;
        mm.setCurrentPatternPos(0);

        while (running && mm.getSequenceLoopCount() == 0)
        {     
          mm.doRealTimePlayback();
          Thread.yield();
        }
      }

      synchronized (this)
      {
        if (!stopped)
        {        
          running = false;
          out.stop();
          out.close();
          donePlaying();

          if (iNotify != 0)
          {
            Glk.GlkEvent e = new Glk.GlkEvent();
            e.type = Glk.EVTYPE_SOUND_NOTIFY;
            e.win = null;
            e.val1 = soundId;
            e.val2 = iNotify;
          
            Glk.addEvent(e);
          }
        }
      }
    }
  }
}
