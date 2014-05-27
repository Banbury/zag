
package micromod.resamplers;

/**
	Table based multipoint FIR resampler.
	This took many, many hours to debug! Note to self: No more 12-hour sleep ins.
*/
public class FIRResampler implements Resampler {
	protected static final int FIXED_POINT_SHIFT=12;
	protected static final int FIXED_POINT_ONE=1<<FIXED_POINT_SHIFT;
	protected static final int FIXED_POINT_BITMASK=FIXED_POINT_ONE-1;
	protected static final int FIXED_POINT_CONV=16-FIXED_POINT_SHIFT;
	protected int points;
	protected short[] sinc;

	/**
		Constructor.
		@param numPoints The higher the better but more cpu and memory intensive. Even number, minimum 2.
	*/
	public FIRResampler( int numPoints ) {
		points = numPoints>>1;
		if(points<1) points = 1;
		genSinc();
		System.out.println(" Multipoint FIR Resampler 0.3 Initialised. Using "+(points<<1)+" points.");
	}

	/**
		Do resampling.
	*/
	public void resample( short[] inputBuf, int samplePos, int subSamplePos, int step, int subStep,
		              short[] outputBuf, int position, int length ) {
		int issp, spos, amp, n, end=position+length;
		if(step==0) {
			// Convert 16 bit fixed point to 12 bit.
			subSamplePos>>=FIXED_POINT_CONV;
			subStep>>=FIXED_POINT_CONV;
			while( position < end ) {
				amp  = 0;
				issp = FIXED_POINT_ONE-subSamplePos;
				spos = samplePos-points+1;
				for( n=points-1; n>=0; n--,spos++ )
					amp += inputBuf[spos]*sinc[(n<<FIXED_POINT_SHIFT)+subSamplePos] >> 15;
				for( n=0; n<points; n++,spos++ )
					amp += inputBuf[spos]*sinc[(n<<FIXED_POINT_SHIFT)+issp] >> 15;
				outputBuf[position++] = (short)amp;
				samplePos += ( subSamplePos += subStep ) >> FIXED_POINT_SHIFT;
				subSamplePos &= FIXED_POINT_BITMASK;
			}
		} else {
			// A simple nearest algorithm. Should really implement some filtering beforehand.
			while( position < end ) {
				outputBuf[position++] = inputBuf[samplePos];
				samplePos+=step+((subSamplePos+=subStep) >> FIXED_POINT_SHIFT);
				subSamplePos&=FIXED_POINT_BITMASK;
			}
		}
	}

	/**
		@return the number of extra input samples required before and after the audio.
	*/
	public int getCushionSize(){
		return points;
	}

	/**
		Generate one wing of a Blackman windowed sinc equation.
		According to Shannon's sampling theory, a sample should be rendered according to the
		equation A Sin(PI*t)/(PI*t) where the sampling frequency is 1/t. This equation goes to
		+-infinity so it is multiplied by a window function to make it tail off at the ends.
		The more points you use to render each sample, the more accurate the result.
		Linear interpolation is actually a very, very course approximation of the above "sinc"
		equation, which sorta explains why it works.
	*/
	protected void genSinc() {
		double pit, wpit, t, gain=0.6;
		int len = (points<<FIXED_POINT_SHIFT)+1;
		sinc = new short[len];
		sinc[0] = (short)( 32767 * gain );
		for( int n=1; n<len; n++ ) {
			t = n/((double)FIXED_POINT_ONE);
			pit = Math.PI*t;
			wpit = pit/points;
			sinc[n] = (short)( (Math.sin(pit)/pit) * (0.42+0.5*Math.cos(wpit)+0.08*Math.cos(2*wpit)) * 32767 * gain );
		}
	}

	/*
		Fill the specified buffer with zeros from start to end-1.
	*/
	protected static void zero( short[] buffer, int start, int end ) {
		for( int n=start; n<end; n++ ) buffer[n] = 0;
	}
}

