
package micromod;

/**
	An "LFO" so the Modulator can do vibrato, tremolo etc.
*/
public class ProTrackerLFO {
	public static final int WF_SINUS = 1, WF_SAWDN = 3,
	                        WF_SQUARE= 4, WF_RANDOM= 5;

	// The SoundTracker/NoiseTracker/ProTracker "sinus" table.
	protected static int[] sinTable = new int[] {
		  0, 24 ,  49,  74,  97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253,
		255, 253, 250, 244, 235, 224, 212, 197, 180, 161, 141, 120,  97,  74,  49,  24 };

	// The current position and rate.
	protected int waveform, position, value;
	protected boolean retrig;

	public ProTrackerLFO() {
		reset();
	}

	/**
		Set the LFO to default values.
	*/
	public void reset(){
		// ST/NT/PT default
		retrig=true;
		setWaveform(WF_SINUS,true);
	}

	/**
		Set the LFO waveform to that which is specified (amazingly)
		@param wave The waveform ID. See above.
		@param retrig If true, retrigger the waveform at new note.
	*/
	public void setWaveform( int wave, boolean retrig ) {
		waveform=wave;
		this.retrig=retrig;
	}

	/**
		Update the oscillator.
		@param speed The amount to advance the LFO.
		@param newNote If true, the waveform is retriggered if required.
		@return the current value between -255 and 255.
	*/
	public int update( int speed, boolean newNote ){
		if(newNote&&retrig)position=0;
		// Update value
		switch(waveform) {
			case WF_SINUS:
				value = sinTable[position&0x1F];
				if(position>31) value=-value;
			break;
			case WF_SAWDN:
				value = -8*position + 255;
			break;
			case WF_SQUARE:
				value = ((position&0x20)==0)?-255:255;
			break;
			case WF_RANDOM:
				value = (int)(Math.random()*511 - 255);
			break;
		}
		position = (position+speed)&0x3F;
		return value;
	}
}
