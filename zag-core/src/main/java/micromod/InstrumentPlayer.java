
package micromod;

import micromod.resamplers.Resampler;

/**
	InstrumentPlayer 0.5
	This class is designed to play a ProTracker style instrument at the specified pitch
	The output of an object of this class has any instrument-related effects applied.

	This class supports resamplers with "cushion" sizes up to 16 points.
*/
public class InstrumentPlayer {
	public static final int FIXED_POINT_SHIFT=16;
	protected static final int FIXED_POINT_BITMASK=(1<<FIXED_POINT_SHIFT)-1;

	/*
		The resample buffer is the input to the resampler.
		3100 allows for the maximum number of input samples that will be required
		for a protracker tick of audio, with the maximum supported cushion size
		added to that.
	*/
	protected static final int CUSHION_BUFFER_SAMPLES=16;
	protected static final int RESAMPLE_BUFFER_SAMPLES=3100+CUSHION_BUFFER_SAMPLES;

	protected int resampleFactor, subSamplePos;
	protected short[] resampleBuffer, cushionBuffer;
	protected LoopDecoder loopDecoder;

	public InstrumentPlayer() {
		loopDecoder = new LoopDecoder();
		resampleBuffer = new short[RESAMPLE_BUFFER_SAMPLES];
		cushionBuffer = new short[CUSHION_BUFFER_SAMPLES];
		blankCushionBuffer();
	}

	/**
		Assign an Instrument to be played.
		Under some circumstances the assigned Instrument will begin to play.
	*/
	public void assignInstrument( Instrument i ) {
		loopDecoder.assignInstrument(i);
	}

	/*
		Set the assigned instrument to begin playing from the current
		sample position. Use setSamplePosition(0) to trigger from the start.
	*/
	public void setAssigned() {
		loopDecoder.setAssigned();
	}

	/** Get the current instrument */
	public Instrument getInstrument() {
		return loopDecoder.getInstrument();
	}

	/**
		Explicitly set the sample position of playback. Protracker has an effect command
		to do this.
	*/
	public void setSamplePosition( int samplePosition ) {
		blankCushionBuffer();
		loopDecoder.setSamplePosition( samplePosition );
		subSamplePos = 0;
	}

	/**
		@return the current sample position.
	*/
	public int getSamplePosition() {
		return loopDecoder.getSamplePosition();
	}

	/**
		@return whether or not the Instrument has finished playing.
	*/
	public boolean hasFinished() {
		return loopDecoder.finishedPlaying();
	}

	/**
		Set the number of input samples per output sample produced in the getAudio()
		method. A higher value results in a higher pitch. The value is in 16 bit fixed
		point format, with 65536 representing 1 input sample per output sample.
	*/
	public void setResampleFactor( int resampleFactor ) {
		this.resampleFactor = resampleFactor;
	}

	/**
		Get the required amount of audio for resampling, and resample it into the
		specified buffer.
	*/
	public void getAudio( short buffer[], int length, Resampler resampler, boolean snapBack ) {
		int cushSize = resampler.getCushionSize();
		System.arraycopy( cushionBuffer, 0, resampleBuffer, 0, cushSize );
		// Get the looped audio
		int samplesRequired = getSamplesRequired( length );
		if( snapBack ) {
			loopDecoder.output( resampleBuffer, cushSize, samplesRequired+cushSize+1, true );
		} else {
			loopDecoder.output( resampleBuffer, cushSize, samplesRequired, false );
			loopDecoder.output( resampleBuffer, samplesRequired+cushSize, cushSize+1, true );
			// Update cushion buffer
			System.arraycopy( resampleBuffer, samplesRequired, cushionBuffer, 0, cushSize );
		}
		// Perform resampling
		int step = resampleFactor >> FIXED_POINT_SHIFT;
		int subStep = resampleFactor & FIXED_POINT_BITMASK;
		resampler.resample( resampleBuffer, cushSize, subSamplePos, step, subStep, buffer, 0, length );
		// Update subSamplePos if necessary
		if(!snapBack) subSamplePos = ( subSamplePos + resampleFactor*length ) & FIXED_POINT_BITMASK;
	}

	/**
		@return the integer number of input samples required to provied length samples
		of output, using the most basic "nearest" algorithm. Unless the resampler requires
		precisely an integer number of input samples, an extra sample will be required at
		the end. This sample will be needed by the resampler again when resampling the
		next segment, so you should always tack on an extra sample that is duplicated at
		the start of the next invocation. This is what snapBack is for.
	*/
	protected int getSamplesRequired( int length ) {
		return ( resampleFactor*length + subSamplePos ) >> FIXED_POINT_SHIFT;
	}

	/**
		Zero the contents of the cushion buffer.
		Use when the contents of the buffer are no longer valid.
	*/
	protected void blankCushionBuffer(){
		for(int n=0; n<CUSHION_BUFFER_SAMPLES; n++) cushionBuffer[n]=0;
	}
}

