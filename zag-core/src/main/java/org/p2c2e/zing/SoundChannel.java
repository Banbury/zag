package org.p2c2e.zing;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;

import micromod.Instrument;
import micromod.MicroMod;
import micromod.Module;
import micromod.ModuleLoader;
import micromod.output.JavaSoundOutputDevice;
import micromod.output.converters.SS16LEAudioFormatConverter;
import micromod.resamplers.FIRResampler;

import org.newdawn.easyogg.OggClip;
import org.p2c2e.blorb.Aiff;
import org.p2c2e.blorb.BlorbFile;
import org.p2c2e.zing.types.GlkEvent;

public class SoundChannel {
	private PlayerThread playerThread;
	private int volume;
	private IGlk glk;

	public SoundChannel(IGlk glk) {
		this.glk = glk;
		volume = 0x10000;
		playerThread = null;
	}

	public void destroy() throws Exception {
		stop();
		playerThread = null;
	}

	public boolean play(BlorbFile blorbFile, int soundId) throws Exception {
		return play(blorbFile, soundId, 1, 0);
	}

	public boolean play(BlorbFile blorbFile, int soundId, int iRepeat,
			int iNotify) throws Exception {
		if (blorbFile == null)
			return false;

		if (playerThread != null)
			stop();

		String stType;
		BlorbFile.Chunk c = blorbFile.getByUsage(BlorbFile.SND, soundId);

		if (c == null)
			return false;

		stType = c.getDataType();

		if (stType.equals("FORM"))
			return playAIFF(c, iRepeat, soundId, iNotify);
		else if (stType.equals("MOD "))
			return playMOD(c, iRepeat, soundId, iNotify);
		else if (stType.equals("SONG"))
			return playSONG(blorbFile, c, iRepeat, soundId, iNotify);
		else if (stType.equals("OGGV"))
			return playOGG(c, iRepeat, soundId, iNotify);
		else
			return false;
	}

	public void stop() throws Exception {
		if (playerThread != null)
			playerThread.stopPlaying();
	}

	public void setVolume(int iVol) {
		this.volume = iVol;
		if (playerThread != null)
			playerThread.setVolume(iVol);
	}

	private synchronized void donePlaying() {
		playerThread = null;
	}

	private boolean playAIFF(BlorbFile.Chunk c, int iRepeat, int soundId,
			int iNotify) throws Exception {
		AudioInputStream in = AudioSystem
				.getAudioInputStream(new BufferedInputStream(c.getRawData()));
		AudioFormat audioFormat = in.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		SourceDataLine l = (SourceDataLine) AudioSystem.getLine(info);

		l.open(audioFormat);

		AIFFThread at = new AIFFThread(l, c, in, iRepeat, soundId, iNotify);
		playerThread = at;
		playerThread.setVolume(volume);
		at.start();
		return true;
	}

	private boolean playSONG(BlorbFile bf, BlorbFile.Chunk c, int iRepeat,
			int soundId, int iNotify) throws Exception {
		MODThread mt;
		MicroMod microMod;
		JavaSoundOutputDevice out = new JavaSoundOutputDevice(
				new SS16LEAudioFormatConverter(), 44100, 1000);
		Module module = ModuleLoader.read(new DataInputStream(c.getData()));

		// for some reason, the first two samples are always blanked out in mods
		for (int i = 2; i < 32; i++) {
			Instrument inst = module.getInstrument(i);
			if (inst != null) {
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

				if (numChannels == 1 && sampleSize <= 8) {
					for (int j = 0; j < numSampleFrames; j++)
						newData.putShort((short) (data[j + offset] << 8));
				} else {
					int k = offset;
					for (int ix = 0; ix < numSampleFrames; ix++) {
						for (int jx = 0; jx < numChannels; jx++) {
							int sample;
							if (sampleSize <= 8)
								sample = data[k++] << 24;
							else if (sampleSize <= 16)
								sample = ((data[k++] << 8) | (data[k++] & 0xff)) << 16;
							else if (sampleSize <= 24)
								sample = ((data[k++] << 16)
										| ((data[k++] << 8) & 0xff) | (data[k++] & 0xff)) << 8;
							else
								sample = (data[k++] << 24)
										| ((data[k++] << 16) & 0xff)
										| ((data[k++] << 8) & 0xff)
										| (data[k++] & 0xff);
							if (jx == 0)
								newData.putShort((short) (sample >>> 16));
						}
					}
				}
				inst.data = newData.array();
				if (inst.looped) {
					inst.loopStart = aiff.getMarkerPos(aiff
							.getSustainLoopBegin());
					inst.sampleEnd = inst.loopStart
							+ (aiff.getMarkerPos(aiff.getSustainLoopEnd()) - aiff
									.getMarkerPos(aiff.getSustainLoopBegin()));
				} else {
					inst.loopStart = 0;
					inst.sampleEnd = numSampleFrames;
				}
			}
		}
		microMod = new MicroMod(module, out, new FIRResampler(16));

		mt = new MODThread(microMod, out, iRepeat, soundId, iNotify);
		playerThread = mt;
		playerThread.setVolume(volume);

		mt.start();
		return true;
	}

	private boolean playMOD(BlorbFile.Chunk c, int iRepeat, int soundId,
			int iNotify) throws Exception {
		MODThread mt;
		JavaSoundOutputDevice out = new JavaSoundOutputDevice(
				new SS16LEAudioFormatConverter(), 44100, 1000);
		Module module = ModuleLoader.read(new DataInputStream(c.getData()));
		MicroMod microMod = new MicroMod(module, out, new FIRResampler(16));

		mt = new MODThread(microMod, out, iRepeat, soundId, iNotify);
		playerThread = mt;
		playerThread.setVolume(volume);

		mt.start();
		return true;
	}

	private boolean playOGG(BlorbFile.Chunk c, int iRepeat, int soundId,
			int iNotify) {
		try {
			OGGThread oggt = new OGGThread(c.getData(), iRepeat, soundId,
					iNotify);
			playerThread = oggt;
			playerThread.setVolume(volume);

			oggt.start();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private interface PlayerThread extends Runnable {
		public abstract void setVolume(int vol);

		public abstract void stopPlaying() throws Exception;
	}

	private class AIFFThread extends Thread implements PlayerThread,
			LineListener {
		private static final int BUFFER_SIZE = 128000;

		int iRepeat;
		int soundId;
		int iNotify;
		boolean stopped;
		BlorbFile.Chunk c;
		SourceDataLine l;
		AudioInputStream in;

		AIFFThread(SourceDataLine l, BlorbFile.Chunk c, AudioInputStream in,
				int iRepeat, int soundId, int iNotify) {
			this.l = l;
			this.in = in;
			this.c = c;
			this.iRepeat = iRepeat;
			this.soundId = soundId;
			this.iNotify = iNotify;

			l.addLineListener(this);
			stopped = false;
		}

		@Override
		public void setVolume(int vol) {
			FloatControl ctl = (FloatControl) l
					.getControl(FloatControl.Type.MASTER_GAIN);
			double gain = (double) vol / (double) 0x10000;
			float dB = (float) (Math.log(gain) / Math.log(10.0) * 20);

			if (ctl != null)
				ctl.setValue(dB);
		}

		@Override
		public synchronized void stopPlaying() throws Exception {
			if (!stopped) {
				l.flush();
				l.drain();
				l.stop();
				l.close();
				stopped = true;
				donePlaying();
			}
		}

		@Override
		public void update(LineEvent e) {
			try {
				LineEvent.Type t = e.getType();
				if (t == LineEvent.Type.STOP && !stopped) {
					l.close();
					donePlaying();

					if (iNotify != 0) {
						GlkEvent ev = new GlkEvent();
						ev.type = IGlk.EVTYPE_SOUND_NOTIFY;
						ev.win = null;
						ev.val1 = soundId;
						ev.val2 = iNotify;
						glk.addEvent(ev);
					}
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void run() {
			int read = 0;
			byte[] data = new byte[BUFFER_SIZE];

			l.start();

			try {
				while (iRepeat-- != 0) {
					while (in.available() > 0) {
						read = in.read(data, 0, in.available());
						l.write(data, 0, read);
					}
					if (iRepeat != 0) {
						in = AudioSystem
								.getAudioInputStream(new BufferedInputStream(c
										.getRawData()));
						read = 0;
					}
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private class MODThread extends Thread implements PlayerThread {
		int soundId;
		int iNotify;
		int iRepeat;
		boolean running;
		boolean stopped;
		JavaSoundOutputDevice out;
		MicroMod mm;

		MODThread(MicroMod mm, JavaSoundOutputDevice out, int iRepeat,
				int soundId, int iNotify) {
			this.mm = mm;
			this.out = out;
			this.soundId = soundId;
			this.iNotify = iNotify;
			this.iRepeat = iRepeat;

			running = false;
			stopped = false;
		}

		@Override
		public synchronized void setVolume(int vol) {
			Line l = out.getLine();
			FloatControl ctl = (FloatControl) l
					.getControl(FloatControl.Type.MASTER_GAIN);
			double gain = (double) vol / (double) 0x10000;
			float dB = (float) (Math.log(gain) / Math.log(10.0) * 20);

			if (ctl != null)
				ctl.setValue(dB);
		}

		@Override
		public synchronized void stopPlaying() {
			if (!stopped) {
				running = false;
				stopped = true;
				out.stop();
				out.close();
				donePlaying();
			}
		}

		@Override
		public void run() {
			out.start();

			for (int i = 0; !stopped && (iRepeat == -1 || i < iRepeat); i++) {
				running = true;
				mm.setCurrentPatternPos(0);

				while (running && mm.getSequenceLoopCount() == 0) {
					mm.doRealTimePlayback();
					Thread.yield();
				}
			}

			synchronized (this) {
				if (!stopped) {
					running = false;
					out.stop();
					out.close();
					donePlaying();

					if (iNotify != 0) {
						GlkEvent e = new GlkEvent();
						e.type = IGlk.EVTYPE_SOUND_NOTIFY;
						e.win = null;
						e.val1 = soundId;
						e.val2 = iNotify;

						glk.addEvent(e);
					}
				}
			}
		}
	}

	private class OGGThread extends Thread implements PlayerThread {
		private OggClip ogg;

		private int repeat;
		private int soundId;
		private int notify;

		public OGGThread(InputStream in, int repeat, int soundId, int notify)
				throws IOException {
			this.repeat = repeat;
			this.soundId = soundId;
			this.notify = notify;

			ogg = new OggClip(new BufferedInputStream(in));
		}

		@Override
		public void run() {
			int n = repeat;
			if (n == -1) {
				ogg.loop();
				while (!ogg.stopped()) {
					Thread.yield();
				}
			} else {
				while (n >= repeat) {
					if (ogg.stopped()) {
						ogg.play();
						n--;
					}
					while (!ogg.stopped()) {
						Thread.yield();
					}
				}
			}

			synchronized (this) {
				if (ogg != null && !ogg.stopped()) {
					ogg.stop();
					donePlaying();

					if (notify != 0) {
						GlkEvent e = new GlkEvent();
						e.type = IGlk.EVTYPE_SOUND_NOTIFY;
						e.win = null;
						e.val1 = soundId;
						e.val2 = notify;

						glk.addEvent(e);
					}
				}
			}
		}

		@Override
		public void setVolume(int vol) {
			if (ogg != null) {
				ogg.setGain((float) vol / 0x10000);
			}
		}

		@Override
		public void stopPlaying() throws Exception {
			if (ogg != null && !ogg.stopped()) {
				ogg.stop();
				donePlaying();
			}
		}

	}
}
