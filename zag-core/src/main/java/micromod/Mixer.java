
package micromod;

import micromod.resamplers.Resampler;

/** Mixer mixes the output of the individual channels together. */
public class Mixer {
	protected WaveShaper waveShaper;
	protected WaveScaler waveScaler;
	protected Synthesizer synthesizer;
	protected int[] leftMixBuffer, rightMixBuffer;
	protected int gain;

	/**
		Constructor.
		@param synth The Synthesizer object that maintains the Channels.
		@param shaper This may be null. It is the compressor/limiter/clipping algorithm to use.
		@param scaler This may be null. It is the decimation algorithm to use. Eg dithering.
	*/
	public Mixer( Synthesizer synth, WaveShaper shaper, WaveScaler scaler ) {
		setWaveShaper(shaper);
		setWaveScaler(scaler);
		synthesizer = synth;
		leftMixBuffer  = new int[MicroMod.MAX_SAMPLES_PER_TICK];
		rightMixBuffer = new int[MicroMod.MAX_SAMPLES_PER_TICK];
		gain = 1 << 13;
	}

	public void setWaveShaper( WaveShaper shaper ) {
		if(shaper==null) shaper = new WaveShaper();
		waveShaper = shaper;
	}

	public void setWaveScaler( WaveScaler scaler ) {
		if(scaler==null) scaler = new WaveScaler();
		waveScaler = scaler;
	}

	/** 
		@return the current gain value. 4096 represents a gain of 1.0
	*/
	public int getGain() {
		return gain;
	}

	/**
		Set the current gain value.
		A parameter of 4096(1.0) guarantees the output never goes above the maximum.
		The maximum gain is 8.0
	*/
	public void setGain( int value ) {
		if( value < 0 ) value = 0;
		if( value > (1<<15)-1 ) value = (1<<15)-1;
		gain = value;
	}

	/**
		Mix the specified number of samples from each of the channels into the specified buffers.
		@param leftBuffer The buffer containing the left channel's audio.
		@param length The number of samples to mix. Should be > 0.
		@param snapBack If true, the audio will be "rewound" after mixing, so a subsequent identical
			call to output() will yield the same output. Used for the volume ramping.
	*/
	public void output( short[] leftBuffer, short[] rightBuffer, int length, Resampler resampler, boolean snapBack ) {
		Channel channel;
		int sample, leftAmp, rightAmp;
		int numChan = synthesizer.getNumberOfChannels();
                // 30 July 2003 (Jon Zeppieri) commented out the code below,
                     // which forces the number of channels to be even
                //		numChan = numChan + (numChan%2);

		// Do first channel seperately to clear out mix buffers
		channel = synthesizer.getChannel(0);
		if( channel.isSilent() ) {
			// Clear mix buffers
			for( int n=0; n<length; n++ ) {
				leftMixBuffer[n] = 0;
				rightMixBuffer[n] = 0;
			}
		} else {
			// Overwrite mix buffers with audio
			leftAmp  = ( channel.getLeftAmplitude() * gain / numChan ) >> 15;
			rightAmp = ( channel.getRightAmplitude() * gain / numChan ) >> 15;
			channel.getAudio( leftBuffer, length, resampler, snapBack );
			waveScaler.scaleWaves( leftBuffer, leftMixBuffer, length, leftAmp );
			waveScaler.scaleWaves( leftBuffer, rightMixBuffer, length, rightAmp );
		}
		// Accumulate other channels
		for( int n=1; n<numChan; n++ ) {
			channel = synthesizer.getChannel(n);
			if( !channel.isSilent() ) {
				leftAmp  = ( channel.getLeftAmplitude() * gain / numChan ) >> 15;
				rightAmp = ( channel.getRightAmplitude() * gain / numChan ) >> 15;
				channel.getAudio( leftBuffer, length, resampler, snapBack );
				waveScaler.scaleWavesAccumulate( leftBuffer, leftMixBuffer, length, leftAmp );
				waveScaler.scaleWavesAccumulate( leftBuffer, rightMixBuffer, length, rightAmp );
			}
		}
		// Clip or shape the final audio to 16 bit
		waveShaper.shapeWaves( leftMixBuffer, leftBuffer, length );
		waveShaper.shapeWaves( rightMixBuffer, rightBuffer, length );
	}
}


