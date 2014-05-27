
package micromod;

/**
	A ProTracker style instrument, supporting 8 bit mono samples, and forward looping only.
*/
public class Instrument {
	public String name;
	public int volume;
	public int fineTune;

	// Loop related members.
	public boolean looped;
	public int loopStart;
	// The index after the last sample.
	public int sampleEnd;

	// The actual length of the instrument data.
	public int sampleLength;

  // 12 August 2003 (Jon Zeppieri)
    // added sampleSize, to distinguish normal 8-bit data from enhanced 16-bit
    public int sampleSize;

	// Signed 8 (or 16) bit Sample data
	public byte[] data;

	public Instrument() {
		name="nullinst";
		// Not necessary in Java, but other languages need it.
		volume=0;
		fineTune=0;
		looped=false;
		loopStart=0;
                sampleSize = 8;
		sampleEnd=0;
	}

	public String toString() {
		return
			name+" Len("+sampleEnd+")"+" LoopStart("+loopStart+
			") Volume("+volume+") FineTune("+fineTune+") Looped("+looped+")";
	}
}


