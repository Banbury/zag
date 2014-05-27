
package micromod;

/**
	Compressor is an implementation of WaveShaper that utilises
	an efficient algorithm I found on www.smartelectronix.com/musicdsp
	
	A compressor improves the sound of distortion and so allows you to
	crank up the gain without things sounding too horrible. This algorithm
	will work with input amplitudes up to 8x the output.

	The use of 32 bit arithmetic has meant that the input must be
	quantized to 13 bits. The distortion produced by the compressor
	is probably louder than the quantization noise anyway.
*/
public class Compressor extends WaveShaper {
	public void shapeWaves( int[] inputBuffer, short[] outputBuffer, int length ) {
		// I have managed to avoid using branches, so this should
		// improve performance on machines with deep cpu pipelines etc.
		int x, x2;
		for( int n=0; n<length; n++ ) {
			x = inputBuffer[n] >> 3;
			x2 = x*x >> 12;
			outputBuffer[n]=(short)((((((x&0x80000000)>>30)+1)*x2+x<<12)/(x2+4096))*26870>>12);			
		}
	}

	/** @return -1 if x negative, 1 if zero or positive */
	protected static int sgn( int x ) {
		return (((x&0x80000000)>>30)+1);
	}

	/** @return the magnitude of x */
	protected static int abs( int x ) {
		return x * sgn(x);
	}
}
