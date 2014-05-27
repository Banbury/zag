
package micromod.resamplers;

/**
	The Resampler interface specifies a pure resampling algorithm.
	The algorithm need not maintain any state between invocations,
	making implementations of resamplers far more straight-forward.
	As an added advantage, the resampling loop (where most of the
	cpu time is spent) can be made tighter. Since an implementation
	need not have to maintain a state, a single instance of a Resampler
	can be used for all channels, and this may be switched on the fly.
*/
public interface Resampler {
	public static final int FIXED_POINT_SHIFT=16;
	public static final int FIXED_POINT_ONE=1<<FIXED_POINT_SHIFT;
	public static final int FIXED_POINT_BITMASK=FIXED_POINT_ONE-1;

	/**
		Resample the audio in inputBuf to outputBuf.
		The resampler should be able to deal correctly with a length of at least 1.

		@param samplePos The index of the sample in inputBuf
		@param subSamplePos The fractional part of the index, in 16 bit fixed point.
		@param step The integer part of the number of input samples per output sample.
		@param subStep The fractional part of the number of input samples per output sample.
			The value is in 16 bit fixed point.
		@param position The index of the output buffer to start writing to.
		@param length The number of samples of output to produce.
	*/
	public void resample( short[] inputBuf, int samplePos, int subSamplePos, int step, int subStep,
	                      short[] outputBuf, int position, int length );
	/**
		Return minimum number of samples required by the resampler before and after the audio
		to be processed. A cubic resampler might return 3, for example.
	*/
	public int getCushionSize();
}


