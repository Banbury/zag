
package micromod;

import micromod.output.*;
import micromod.resamplers.Resampler;

/**
	MicroMod 0.9xxx!

	Copyright 2002 Martin Cameron. This is Free Software subject to the
	terms of the Gnu Lesser General Public Licence(LGPL). Please see the
	included file "LGPL-2.1"

	This player was designed for the "standard" Amiga ProTracker format
	with extensions for more than four channels and channel panning.
	The player should work well with most clone formats such as FTK
	and older formats such as NoiseTracker, SoundTracker2.X and StarTrekker.

	Problems to address for 0.99:
	 Missing glissando fx.
	 The original Amiga period limits are enforced, so some pc mods may not work.
	Future plans:
	 Needs a complete functional comparison with Amiga ProTracker.
	 Rewrite planned. Lots of design ideas (will implement in MuXM first).

	Inherent Limitations:
	 The original and rare Ultimate SoundTracker module format is not supported.
	 The pattern loop command is ignored, for both design and compatibility reasons.
*/
public class MicroMod {
	public static final String MICROMOD_VERSION = "0.98kX!";

	protected OutputDevice output;
	protected Sequencer sequencer;
	protected Synthesizer synthesizer;
	protected Resampler resampler;
	protected Mixer mixer;

	/* This buffer size allows mixing up to 104khz. */
	public static final int MAX_SAMPLES_PER_TICK = 8192;

	/* Volume ramping to reduce clicks when volume/instrument changes. */
	protected static final int LOG2_VOL_RAMP_SAMPLES = 4;
	protected static final int VOL_RAMP_SAMPLES = 1 << LOG2_VOL_RAMP_SAMPLES;
	protected short[] volRampLeftBuffer, volRampRightBuffer;
	protected short[] leftOutBuffer, rightOutBuffer;

	/* Counters for the realtime output feature. */
	protected int tickLength, currentTick;

	/**
		@param module The module to be played.
		@param output An open output device to send the audio to.
	*/
	public MicroMod( Module module, PCM16StreamOutputDevice output, Resampler resampler ) {
		System.out.println("  _   _            _   _");
		System.out.println(" / \\_/ \\() __ _ _ / \\_/ \\ _  _|");
		System.out.println(" \\_\\ /_/|||_ | |_|\\_\\ /_/|_||_|");
		System.out.println("  =MuMart2002=========="+MICROMOD_VERSION+"=");
		System.out.println();	
		System.out.println(" MicroMod ProTracker replay (C)2002 Martin Cameron.");
		System.out.println(" Free Software licenced under the GNU LGPL.");
		System.out.println();

		// Check for compliance
		if( !(output instanceof PCM16StreamOutputDevice) )
			throw new IllegalArgumentException("The specified output device is not supported!");

		this.output = output;
		setResampler(resampler);
		synthesizer = new Synthesizer( module, ((PCM16)output).getSamplingRate() );
		mixer = new Mixer( synthesizer, null, null );
		sequencer = new Sequencer( module, synthesizer );
		currentTick=tickLength=synthesizer.getCiaTickSamples();

		volRampLeftBuffer  = new short[VOL_RAMP_SAMPLES];
		volRampRightBuffer = new short[VOL_RAMP_SAMPLES];
		leftOutBuffer = new short[MAX_SAMPLES_PER_TICK];
		rightOutBuffer = new short[MAX_SAMPLES_PER_TICK];
	}

	/** Assign another module to the player to play from the beginning. */
	public synchronized void setModule( Module module ) {
		synthesizer.setModule(module);
		sequencer.setModule(module);
		currentTick=tickLength=synthesizer.getCiaTickSamples();
	}

	/** Assign another open output device to the player. */
	public synchronized void setOutputDevice( PCM16StreamOutputDevice output ) {
		this.output = output;
		synthesizer.setSamplingRate(output.getSamplingRate());
	}

	/** Switch to the specified resampling algorithm. */
	public synchronized void setResampler( Resampler resampler ) {
		this.resampler=resampler;
	}

	/** Switch to the specified WaveShaper (compressor/expander/limiter etc) */
	public synchronized void setWaveShaper( WaveShaper waveShaper ) {
		mixer.setWaveShaper(waveShaper);
	}

	/** Switch to the specified WaveScaler (dithering algorithm). */
	public synchronized void setWaveScaler( WaveScaler waveScaler ) {
		mixer.setWaveScaler(waveScaler);
	}

	/** Set whether to use PAL or NTSC decoding (default PAL for ProTracker/NoiseTracker) */
	public synchronized void usePAL( boolean pal ) {
		synthesizer.usePAL(pal);
	}

	/** @return true if currently decoding in PAL, else NTSC */
	public synchronized boolean isPAL() {
		return synthesizer.isPAL();
	}

	/**
		Set the gain value. 4096 represents a gain of 1.0 that allows for all channels to
		be panned to one side without clipping. The default is 2.0, which prevents clipping
		using Amiga 4 channel mods, but can potentially cause clipping in multichannel mods
		which use the panning commands. The maximum gain is 8.0.
	*/
	public synchronized void setGain( int gain ) {
		mixer.setGain(gain);
	}

	/** @return The length in patterns of the current song. */
	public synchronized int getSongLengthPatterns() {
		return sequencer.getSongLengthPatterns();
	}

	/** @return The pattern being played. */
	public synchronized int getCurrentPatternPos() {
		return sequencer.getSequencePosition();
	}

	/**
		Set the pattern in the sequence to play from.
		To restart playback of the current module from scratch, set pos=0.
	*/
	public synchronized void setCurrentPatternPos( int pos ) {
		if(pos==0) {
			// Reset the player
			sequencer.reset();
			synthesizer.reset();
			currentTick=tickLength=synthesizer.getCiaTickSamples();
		} else {
			sequencer.setSequencePosition(pos);
		}
	}

	/**
		@return the number of times the current pattern in the sequence has been
		played. You can use this to end if the song begins to loop around.
	*/
	public synchronized int getSequenceLoopCount() {
		return sequencer.getSequenceCounter();
	}

	/*
		Perform realtime (non blocking) audio output.

		A call to this method will calculate just enough audio
		to top up the buffer in the output device and return. You should do this before
		calling start() on the output device to ensure there is audio in the buffer before
		playback. If you call doPlayback(true) in evenly spaced intervals, you should get
		roughly the same cpu usage per call. If you call this for every frame of animation
		that you calculate you will be able to maintain some graphical smoothness.

		This method requires a HasAvailableOutputDevice that correctly reports the amount of
		space to near sample accuracy to work in non-blocking mode.
	*/
	public synchronized void doRealTimePlayback() {
		// Getting this one right was a job and a half :)
		// I have done some very sick things when testing this method, and I reckon it's pretty stable.

		// Ensure we have a compliant output device.
		if(!(output instanceof HasAvailableOutputDevice))
			throw new UnsupportedOperationException("The specified OutputDevice does not support real-time operation!");

		// Get the number of bytes to write
		int bufferAvailable = ((HasAvailableOutputDevice)output).framesAvailable();
		if( bufferAvailable < VOL_RAMP_SAMPLES ) return;

		// ASSERTION: We have to write no less than VOL_RAMP_SAMPLES
		// Write up to end of first tick.
		if( currentTick < tickLength ) {
			// ASSERTION: We are in the middle of a tick
			if( bufferAvailable < currentTick ) {
				if( currentTick-bufferAvailable >= VOL_RAMP_SAMPLES ) {
					writeAudio( bufferAvailable, false, false );
					currentTick-=bufferAvailable;
					bufferAvailable=0;
					// ASSERTION: currentTick is no less than VOL_RAMP_SAMPLES
				}
			}
			else {
				// Write to end of tick and update
				writeAudio( currentTick, false, true );
				bufferAvailable-=currentTick;
				sequencer.update();
				currentTick=tickLength=synthesizer.getCiaTickSamples();
			}
		}
		// Write whole ticks and update.
		while( bufferAvailable>=tickLength ) {
			writeAudio( tickLength, true, true );
			bufferAvailable-=tickLength;
			sequencer.update();
			currentTick=tickLength=synthesizer.getCiaTickSamples();
		}
		// Write end tick fragment.
		if( bufferAvailable>=VOL_RAMP_SAMPLES && currentTick-bufferAvailable>=VOL_RAMP_SAMPLES) {
			writeAudio( bufferAvailable, true, false );
			currentTick -= bufferAvailable;
			// ASSERT: currentTick never less than VOL_RAMP_SAMPLES
		}	
	}

	/**
		Call this method in an infinite loop to render a "tick" of audio at a time.
		The playing thread will block while writing to the output device.

		If you want to use this player in a game or demo, you should use the
		doRealTimePlayback() method instead, as this will ensure even cpu usage.
	*/
	public synchronized void doPlayback() {
		// The easy bit - Blocking IO.
		// write to end of current tick if any (this bit bridges blocking and non blocking calls)
		if( currentTick < tickLength ) {
			writeAudio( currentTick, false, true );
			sequencer.update();
			currentTick=tickLength=synthesizer.getCiaTickSamples();
		}
		// write a whole tick and volume ramp.
		writeAudio( tickLength, true, true );
		sequencer.update();
		currentTick=tickLength=synthesizer.getCiaTickSamples();
	}

	/**
		Render length samples of audio and write them to the output device.
		This method is just common code used by doPlayback().
		@param ramp Crossfade the volume ramp buffer with the start of the audio.
		@param updateRamp Render a few more samples into the volume ramp buffer.
	*/
	protected void writeAudio( int length, boolean ramp, boolean updateRamp ) {
		// BadNWrong test: if(length < VOL_RAMP_SAMPLES) System.out.println("*"+length); 
		mixer.output( leftOutBuffer, rightOutBuffer, length, resampler, false );
		if(ramp) volumeRamp( leftOutBuffer, rightOutBuffer );
		((PCM16StreamOutputDevice)output).write( leftOutBuffer, rightOutBuffer, length );
		if(updateRamp) mixer.output( volRampLeftBuffer, volRampRightBuffer, VOL_RAMP_SAMPLES, resampler, true );
	}

	/**
		Crossfade the volume ramping buffers with the start of the specified buffers.
		If the values are the same in each buffer, the result will be unchanged.
		This is important to maintain sound quality.
	*/
	protected void volumeRamp( short[] leftBuffer, short[] rightBuffer ) {
		// Crossfade
		int x,y;
		for( x=0; x<VOL_RAMP_SAMPLES; x++ ) {
			y=VOL_RAMP_SAMPLES-x;
			leftBuffer[x]  = (short)( ( leftBuffer[x]*x  + volRampLeftBuffer[x]*y )  >> LOG2_VOL_RAMP_SAMPLES );
			rightBuffer[x] = (short)( ( rightBuffer[x]*x + volRampRightBuffer[x]*y ) >> LOG2_VOL_RAMP_SAMPLES );
		}
	}
}


