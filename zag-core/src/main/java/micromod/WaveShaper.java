
package micromod;

/**
	The WaveShaper simply provides a method for distorting
	(usually compressing/limiting) audio.
*/
public class WaveShaper {
	/**
		Perform shaping of the samples in inputBuffer to outputBuffer from 0 to length-1.
	*/
	public void shapeWaves( int[] inputBuffer, short[] outputBuffer, int length ){
		for(int n=0; n<length; n++) outputBuffer[n] = (short)inputBuffer[n];
	}
}

