
package micromod;

/**
	LoopDecoder streams the samples of an instrument in the correct order.
	Because looping is done independently of anything else, it makes bidi loops
	etc. easy to do, and fast, because the loop can be made tight.

	This implementation is rather strange, because in order to emulate a widely
	used quirk of ProTracker, an Instrument can be assigned to the decoder while
	another one is playing. The assigned instrument will be triggered automatically
	by the decoder depending upon certain conditions.
*/
public class LoopDecoder {
	protected Instrument instrument, assigned;
	protected int samplePos=0;

	/** Constructor */
	public LoopDecoder(){
		// Set up a silent "null" Instrument.
		instrument=assigned=new Instrument();
	}

	/** Assign an Instrument to the decoder. */
	public void assignInstrument( Instrument inst ) {
		assigned=inst;
	}

	/** Switch to the assigned instrument */
	public void setAssigned() {
		instrument=assigned;
		setSamplePosition(samplePos);
	}

	/** @return The Instrument currently being decoded */
	public Instrument getInstrument() {
		return instrument;
	}

	/** @return true if the Instrument has finished playback. */
	public boolean finishedPlaying() {
		// A looped instrument never falls silent.
		return !instrument.looped && samplePos>=instrument.sampleEnd;
	}

	/** Set the sample position of playback. If out of range, a sensible position will be chosen.*/
	public void setSamplePosition( int samplePosition ) {
		if( samplePosition < 0 ) samplePosition = 0;
		if( samplePosition >= instrument.sampleEnd )
			if(instrument.looped)
				samplePosition = instrument.loopStart;
			else
				samplePosition = 0;
		samplePos = samplePosition;
	}

	/** @return The current sample position.*/
	public int getSamplePosition() {
		return samplePos;
	}

	/**
		Output looped(or not) audio into buffer.
		@param snapBack if true, the sample position will not be updated.
	*/
	public void output( short[] buffer, int offset, int length, boolean snapBack ) {
		int samPos=samplePos, end=offset+length;
		Instrument inst=instrument;
		for( int n=offset; n<end; n++ ) {
			if( samPos>=inst.sampleEnd ) {
				if( inst!=assigned && assigned.looped ) {
					// The quirk
					inst=assigned;
					samPos=inst.loopStart;
				}
				else if( inst.looped ) {
					// Current instrument is looped
					samPos=inst.loopStart;
				}
				else {
					// Not looped. Fall silent.
					zero( buffer, n, end );
					break;
				}
			}
                        if (inst.sampleSize == 8)
                        {
                          buffer[n] = (short)( inst.data[samPos] << 8 );
                          samPos++;
                        }
                        else
                        {
                          buffer[n] = 
                            (short) ((inst.data[2 * samPos] << 8) |
                                     (inst.data[(2 * samPos) + 1] & 0xff));
                          samPos++;
                        }
		}
		if(!snapBack){
			samplePos=samPos;
			instrument=inst;
		}
	}

	/*
		Fill the specified buffer with zeros from start to end-1.
	*/
	protected static void zero( short[] buffer, int start, int end ) {
		for( int n=start; n<end; n++ ) buffer[n] = 0;
	}
}
