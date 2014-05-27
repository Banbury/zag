
package micromod;

/**
	The WaveScaler class reduces the amplitude of audio.
	The idea is that this could be extended to provide a dithering
	algorithm in the future. This class only does some simple rounding
	at the moment.
*/
public class WaveScaler {
	/**
		Scale the amplitude of signed 16 bit audio in buffer.
		Perform the operation on indexes 0 to length-1.

		@param inputBuffer The audio input buffer.
		@param outputBuffer The audio output buffer.
		@param length The number of samples to scale.
		@param scale The amount to scale the audio by. This is in fixed
			point format, with 8192 representing a scale factor of 1.0.
			The maximum scale factor is 8.0
	*/
	public void scaleWaves( short[] inputBuffer, int[] outputBuffer, int length, int scale ) {
		for( int n=0; n<length; n++ ) {
			outputBuffer[n] = (inputBuffer[n]*scale) >> 13;
		}
	}

	/**
		As scaleWaves(), except add the output values to those in outputBuffer, instead
		of overwriting them. This method is good for efficiency.
	*/
	public void scaleWavesAccumulate( short[] inputBuffer, int[] outputBuffer, int length, int scale ) {
		for( int n=0; n<length; n++ ) {
			outputBuffer[n] += (inputBuffer[n]*scale) >> 13;
		}
	}
}
