
package micromod;

/**
	The Sequencer interprets a Sequence a "tick" at
	a time. There is usually more than one tick per row.
*/
public class Sequencer {
	public static final int
		SEQFX_POSJUMP=0xB, SEQFX_PATBREAK=0xD, SEQFX_SETSPEED=0xF,
		SEQFX_PATLOOP=0xE6, SEQFX_PATDELAY=0xEE;

	protected Sequence sequence;
	protected Synthesizer synthesizer;
	protected int[] sequenceCounter;
	protected int sequencePosition, currentRow, tempo, tempoCounter;
	protected int patternBreak, positionJump, rowJump;
	protected int[] patternLoopRow;
	protected int patternLoopChannel, patternLoopCounter;
	protected boolean patternLoopEngaged;

	/** @param sequence the ProTrackerSequence to use */
	public Sequencer( Module module, Synthesizer synthesizer ) {
		sequenceCounter = new int[128];
		this.synthesizer = synthesizer;
		setModule(module);
	}

	/**
		Initialise the Sequencer to play a new module.
	*/
	public void setModule( Module module ) {
		sequence = module.getSequence();
		patternLoopRow = new int[ sequence.numberOfChannels ];
		reset();
	}

	/**
		Reset the Sequencer and Synthesizer to their initial states.
		This method sets up the first tick, so you should extract audio from the
		Synthesizer before calling update().
	*/
	public void reset() {
		resetSequenceCounter();
		synthesizer.reset();
		synthesizer.setBPM(sequence.defaultBPM);
		tempo = tempoCounter = sequence.defaultTempo;
		sequencePosition = 0;
		currentRow = 0;
		resetPatternLoop();
		initializeRowFX();
	}

	/**
		Update the Sequencer for a single tick.
	*/
	public void update() {
		tempoCounter--;
		if( tempoCounter==0 ) {
			nextRow();
			tempoCounter = tempo;
			initializeRowFX();
		}
		else {
			synthesizer.updateFX();
		}
	}

	/**
		@return the number of times the current sequence position has been played. This can
		be used to determine whether the song is looping.
	*/
	public int getSequenceCounter() {
		return sequenceCounter[sequencePosition];
	}

	/**
		Set the current sequence position to be played. This can be used to skip parts of a
		song, or to find "hidden" patterns. This method resets the "sequence counter".
	*/
	public void setSequencePosition( int pos ) {
		resetSequenceCounter();
		updateSequencePosition(pos);
		updateRow(0);
		tempoCounter=tempo;
		initializeRowFX();
	}

	/**
		@return The length, in patterns, of the current Sequence.
	*/
	public int getSongLengthPatterns() {
		return sequence.songLengthPatterns;
	}

	/**
		@return the sequence position currently being played.
	*/
	public int getSequencePosition() {
		return sequencePosition;
	}

	/**
		Zero the sequence counter.
	*/
	protected void resetSequenceCounter() {
		for( int n=0; n<128; n++ ) sequenceCounter[n]=0;
	}

	/**
		Move to the next row, or perform a jump if one is requested.
	*/
	protected void nextRow() {
		if( patternBreak != -1 ) {
			// Do a pattern break
			nextSequencePosition();
			updateRow(patternBreak);
			return;
		}
		if( positionJump != -1 ) {
			// Do a position jump
			updateSequencePosition(positionJump);
			updateRow(0);
			return;
		}
		if( rowJump != -1 ) {
			// Do a row jump
			updateRow(rowJump);
			return;
		}
		// Otherwise, do a normal step.	
		if( currentRow < 63 )
			updateRow(currentRow+1);
		else {
			nextSequencePosition();
			updateRow(0);
		}
	}

	protected void nextSequencePosition() {
		int seqpos = sequencePosition+1;
		if( seqpos == sequence.songLengthPatterns ) {
			if( sequence.restartPosition==127 || sequence.restartPosition>=sequence.songLengthPatterns )
				seqpos = 0 ;
			else
				seqpos = sequence.restartPosition;
		}
		updateSequencePosition(seqpos);
	}

	/**
		Process any sequencer commands on the current row and set flags to
		indicate what nextRow() should do next.
	*/
	protected void initializeRowFX() {
		int fxCmd, fxVal;
		Sequence.Pattern currentPattern = sequence.patterns[sequence.patternOrder[sequencePosition]];

		// Defensive-coding
		patternBreak = -1;
		positionJump = -1;
		rowJump = -1;

		// Update the Synthesizer
		for(int n=0; n<sequence.numberOfChannels; n++) {
			synthesizer.initialiseFX( n,
				currentPattern.period[currentRow][n],
				currentPattern.instrument[currentRow][n],
				currentPattern.effectCommand[currentRow][n],
				currentPattern.effectValue[currentRow][n] );
		}

		// Collect sequencer commands from all channels. If two or more jumps are present
		// on the same row, the last one will be obeyed only. Is this correct behaviour?
		for( int n=0; n<sequence.numberOfChannels; n++ ) {
			fxCmd = currentPattern.effectCommand[currentRow][n];
			fxVal = currentPattern.effectValue[currentRow][n];
			switch( fxCmd ) {
				case SEQFX_POSJUMP :
					if(fxVal < sequence.songLengthPatterns) positionJump = fxVal;
					break;
				case SEQFX_PATBREAK :
					patternBreak = fxVal;
					break;
				case SEQFX_SETSPEED :
					// Ignore 0 parameter.
					if( fxVal==0 ) break;
					if( fxVal<32 ) {
						// Set speed, not BPM
						tempo = fxVal;
						tempoCounter = fxVal;
						break;
					}
					// Set BPM
					synthesizer.setBPM(fxVal);
					break;
				case SEQFX_PATLOOP :
					if( handlePatternLoop( n, fxVal ) )
						rowJump = patternLoopRow[n];
					break;
				case SEQFX_PATDELAY :
					tempoCounter = tempo + tempo*fxVal;
					break;
			}
		}
	}

	/**
		Set the current sequence position, and handle any necessary state changes.
	*/
	protected void updateSequencePosition( int pos ) {
		if( pos < 0 || pos >= sequence.songLengthPatterns ) pos=0;
		sequenceCounter[sequencePosition]++;
		sequencePosition = pos;
		resetPatternLoop();
	}

	/**
		Set the current row, and handle any necessary state changes.
	*/
	protected void updateRow( int row ) {
		if( row<0 || row>63 ) row=0;
		currentRow = row;
		// Reset the pattern loop after one may have finished.
		if( !patternLoopEngaged ) 
			patternLoopChannel=-1;
	}

	/**
		Some modules do some very sick things with pattern loops, so it must be implemented correctly.
		While I'm not 100% sure, this algorithm works with all the patternloop-bending I know of
		(including the sick BETA-3, which uses an arrangement that causes a pattern to play through with
		lots of stuttering, then the same pattern plays through without stuttering. ModPlug can play this
		tune correctly, the version of MikIT I have can't, but to be fair it doesn't sound any worse.)

		Pattern loop works by having a loop marker for each channel. Once a
			pattern loop is started, loop commands on other channels are ignored.
		Issuing a pattern loop without setting a marker loops from row zero.
			The loop markers are reset for each pattern.
		Loops on the leftmost channels take precedence, so on a row with a pattern
			loop on each channel, the pattern is looped using the channel 0 markers, then
			it is looped with the channel 1 markers and so on.

		@return true if a jump is to be made to the marker set in patternLoopRow[channel].
	*/
	protected boolean handlePatternLoop( int channel, int parameter ) {
		// Set the marker.
		if( parameter==0 ) {
			patternLoopRow[channel] = currentRow;
			return false;
		}
		/*
			Implementation details:
			At new pattern, reset all markers to row zero.
			At new row, before handling fx, if no pattern loop is engaged, reset patternLoopChannel
			to -1, so any fresh loops on channel 0 will be engaged.
			If a patternloop jump cmd is encountered
				If a loop is engaged on this channel
					update counter, jump if necessary, break out of loop if necessary.
				Else
					If we are on a higher channel than the one just looped (or no loops have yet taken place on this row)
						engage this channel as the one to be looped, set counter, perform first jump.
		*/
		if( patternLoopEngaged ) { 
			if( patternLoopChannel == channel ) {
				patternLoopCounter--;
				if( patternLoopCounter > 0 )
					return true;
				else {
					patternLoopEngaged = false;
					return false;
				}
			}
		}
		else { 
			if( channel > patternLoopChannel ) {
				patternLoopEngaged = true;
				patternLoopChannel = channel;
				patternLoopCounter = parameter;
				return true;
			}
		}
		return false;
	}

	/**
		Reset all loop markers to row 0, as would happen when a new sequence position is entered.
	*/
	protected void resetPatternLoop() {
		patternLoopEngaged=false;
		patternLoopChannel=-1;
		patternLoopCounter=0;
		for( int n=0; n<sequence.numberOfChannels; n++ ) patternLoopRow[n] = 0;
	}
}

