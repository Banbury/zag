
package micromod.resamplers;

/**
	The linear resampler implements linear interpolation resampling.
	Linear interpolation prevents nasty harmonics being added to the sound during
	resampling. Technically, linear interpolation is not very good - but sound
	quality is a very subjective thing, and this resampler certainly has a low end
	"kick" that the FIR resampler certainly lacks(probably cause it ain't supposed to
	be there :) As as extra bonus, it's pretty fast!
*/
public class LinearResampler implements Resampler {
	public LinearResampler() {
		System.out.println(" Linear Resampler 0.2 initialised.");
	}

	public void resample( short[] inputBuf, int samplePos, int subSamplePos, int step, int subStep,
	                            short[] outputBuf, int position, int length ) {
		int end=position+length-1, gradient, yintercept, temp;
		step = (step<<FIXED_POINT_SHIFT)+subStep;

		// Perform linear interploation
		gradient=inputBuf[samplePos+1]-inputBuf[samplePos];
		yintercept=inputBuf[samplePos] << FIXED_POINT_SHIFT;
		while( position <= end ) {
			outputBuf[position++] = (short) (gradient*subSamplePos+yintercept >> FIXED_POINT_SHIFT);
			subSamplePos += step;
			if( subSamplePos > FIXED_POINT_BITMASK ) {
				samplePos += subSamplePos >> FIXED_POINT_SHIFT;
				subSamplePos &= FIXED_POINT_BITMASK;
				temp = inputBuf[samplePos];
				gradient   = inputBuf[samplePos+1]-temp;
				yintercept = temp << FIXED_POINT_SHIFT;
			}
		}
	}

	public int getCushionSize() {
		// Need 1 sample.
		return 1;
	}
}
