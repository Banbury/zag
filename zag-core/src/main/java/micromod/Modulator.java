
package micromod;

/**
	The Modulator is intended to "modulate" the pitch/volume of a Channel
	over time. It can be configured to perform arpeggios, vibratos, tremolos, portamentos etc.
*/
public class Modulator {
	public static final int
		FX_ARPEGGIO=0, FX_SLIDEUP=1, FX_SLIDEDOWN=2, FX_TONEPORTA=3,
		FX_VIBRATO=4, FX_TONEPORTAVOLSLIDE=5, FX_VIBRATOVOLSLIDE=6,
		FX_TREMOLO=7, FX_PANNING=8, FX_SETSAMPLEOFFSET=9, FX_VOLSLIDE=0xA,
		FX_SETVOLUME=0xC, FX_LOWPASS=0xE0, FX_FINESLIDEUP=0xE1,
		FX_FINESLIDEDOWN=0xE2, FX_SETGLISSANDO=0xE3, FX_SETVIBRATOWAVE=0xE4,
		FX_SETFINETUNE=0xE5, FX_SETTREMOLOWAVE=0xE7, FX_EXTPAN=0xE8, FX_RETRIG=0xE9,
		FX_FINEVOLUP=0xEA,FX_FINEVOLDOWN=0xEB, FX_NOTECUT=0xEC, FX_NOTEDELAY=0xED,
		FX_INVERTLOOP=0xEF;

	protected Channel channel;
	protected boolean supportsPanning;
	protected ProTrackerLFO vibratoLFO, tremoloLFO;
	protected int currentFXCommand, currentFXValue, fxSubValue1, fxSubValue2;
	protected int currentFXPeriod, currentFXCounter;
	protected int tonePortaDestination, tonePortaSpeed, sampleOffset;
	protected int vibSpeed, vibDepth, tremSpeed, tremDepth;
	protected int[] arpeggio = new int[3];

	protected static int[] sinTable = new int[] {
		  0, 24 ,  49,  74,  97, 120, 141, 161, 180, 197, 212, 224, 235, 244, 250, 253,
		255, 253, 250, 244, 235, 224, 212, 197, 180, 161, 141, 120,  97,  74,  49,  24 };

	// TODO: Find out the period limits of FTK style mods. Have found some using periods as low as 45.
	protected static int[] periodTable = new int[] {
		856, 808, 762, 720, 678, 640, 604, 570, 538, 508, 480, 453,
		428, 404, 381, 360, 339, 320, 302, 285, 269, 254, 240, 226,
		214, 202, 190, 180, 170, 160, 151, 143, 135, 127, 120, 113 };

	/**
		@param chan the channel to be associated with this Modulator
		@param supportsPanning If false, will ignore extended panning commands.
			Old Protracker modules do not have panning, but may still issue the
			panning commands.
	*/
	public Modulator( Channel chan, boolean supportsPanning ) {
		channel = chan;
		vibratoLFO = new ProTrackerLFO();
		tremoloLFO = new ProTrackerLFO();
		this.supportsPanning = supportsPanning;
	}	

	/**
		Reset the modulator to "power on" defaults.
	*/
	public void reset() {
		currentFXCommand=currentFXValue=currentFXPeriod=0;
		tonePortaDestination=tonePortaSpeed=sampleOffset=0;
		vibSpeed=vibDepth=0;
		tremSpeed=tremDepth=0;
		vibratoLFO.reset();
		tremoloLFO.reset();
	}

	/**
		Initialise both this object and the channel associated with it with the data from a row
		in a ProTracker-style sequence.
	*/
	public void initialiseFX( int fxPeriod, Instrument fxInstrument, int fxCommand, int fxValue ) {
		currentFXPeriod=fxPeriod;
		currentFXCommand=fxCommand;
		currentFXValue=fxValue;

		// Set up the porta destination if not zero.
		if( fxPeriod != 0 ) tonePortaDestination = fxPeriod;

		// Trigger the instrument(if any) on the channel.
		if( fxCommand == FX_TONEPORTA || fxCommand == FX_TONEPORTAVOLSLIDE ) {
			// If it's a tone porta, instruct the channel to switch to it.
			channel.trigger( fxInstrument, -1 );
		}
		else if ( fxCommand == FX_NOTEDELAY ) {
			/// Assign the instrument but defer trigger
			channel.trigger( fxInstrument, 0 );
		}
		else {
			// Do a normal trigger.
			channel.trigger( fxInstrument, fxPeriod );
		}

		switch(fxCommand) {
			case FX_ARPEGGIO:
				if( fxValue != 0 ) initialiseArpeggio();
				break;
			case FX_TONEPORTA:
				if( fxValue !=0 ) tonePortaSpeed = fxValue;
				break;
			case FX_VIBRATO:
				initialiseVibrato(fxPeriod!=0);
				break;
			case FX_TONEPORTAVOLSLIDE:
				getFXSubValues();
				break;
			case FX_VIBRATOVOLSLIDE:
				getFXSubValues();
				break;
			case FX_TREMOLO:
				initialiseTremolo(fxPeriod!=0);
				break;
			case FX_PANNING:
				if( !supportsPanning ) break;
				if( fxValue<=128 )
					channel.setPanning( (128-fxValue)<<9, fxValue<<9 );
				else if( fxValue==0xA4 )
					channel.setPanning( -32768, 32768 );
				break;
			case FX_SETSAMPLEOFFSET:
				if( currentFXValue != 0 ) sampleOffset = currentFXValue;
				channel.setSamplePosition( sampleOffset << 8 );
				break;
			case FX_VOLSLIDE:
				getFXSubValues();
				break;
			case FX_SETVOLUME:
				channel.setVolume( fxValue, true );
				break;
			case FX_FINESLIDEUP:
				// Research needed. Docs say add fxValue. Mods say add fxValue*2
				channel.setPeriod( channel.getPeriod()-(fxValue<<1), true );
				break;
			case FX_FINESLIDEDOWN:
				channel.setPeriod( channel.getPeriod()+(fxValue<<1), true );
				break;
			case FX_SETVIBRATOWAVE:
				setVibratoWave(fxValue);
				break;
			case FX_SETFINETUNE:
				adjustFineTune();
				break;
			case FX_SETTREMOLOWAVE:
				setTremoloWave(fxValue);
				break;
			case FX_EXTPAN:
				if( !supportsPanning ) break;
				channel.setPanning( (15-fxValue)*4369, fxValue*4369 );
				break;
			case FX_RETRIG:
				currentFXCounter = fxValue;
				break;
			case FX_NOTECUT:
				currentFXCounter = fxValue;
				updateNoteCut();
				break;
			case FX_NOTEDELAY:
				currentFXCounter = fxValue;
				updateNoteDelay();
				break;
			case FX_FINEVOLUP:
				channel.setVolume( channel.getVolume()+fxValue, true );
				break;
			case FX_FINEVOLDOWN:
				channel.setVolume( channel.getVolume()-fxValue, true );
				break;
		}
	}

	/**
		Update the pitch/volume for a CIA tick of the tracker.
	*/
	public void updateFX() {
		switch( currentFXCommand ) {
			case FX_ARPEGGIO:
				if( currentFXValue != 0 )
					updateArpeggio();
				break;
			case FX_SLIDEUP:
				channel.setPeriod( channel.getPeriod()-currentFXValue, true );
				break;
			case FX_SLIDEDOWN:
				channel.setPeriod( channel.getPeriod()+currentFXValue, true );
				break;
			case FX_TONEPORTA:
				updateTonePorta();
				break;
			case FX_VIBRATO:
				updateVibrato(false);
				break;
			case FX_TONEPORTAVOLSLIDE:
				updateTonePorta();
				updateVolSlide();
				break;
			case FX_VIBRATOVOLSLIDE:
				updateVibrato(false);
				updateVolSlide();
				break;
			case FX_TREMOLO:
				updateTremolo(false);
				break;
			case FX_VOLSLIDE:
				updateVolSlide();
				break;
			case FX_RETRIG:
				currentFXCounter--;
				if( currentFXCounter==0 ) {
					channel.trigger( null, channel.getPeriod() );
					currentFXCounter = currentFXValue;
				}
				break;
			case FX_NOTECUT:
				updateNoteCut();
				break;
			case FX_NOTEDELAY:
				updateNoteDelay();
				break;
		}
	}

	protected void getFXSubValues() {
		fxSubValue1 = (currentFXValue&0xF0) >> 4;
		fxSubValue2 = currentFXValue&0xF;
	}

	protected void initialiseArpeggio() {
		currentFXCounter=0;
		int period = channel.getPeriod();
		int firstAdd = (currentFXValue&0xF0) >> 4;
		int secondAdd = currentFXValue&0x0F;
		for(int n=0; n<36; n++ )
			if( periodTable[n] <= period ) {
				arpeggio[0]=periodTable[n];
				if( n+firstAdd > 35 ) firstAdd = 35-n;
				if( n+secondAdd > 35 ) secondAdd = 35-n;
				arpeggio[1] = periodTable[n+firstAdd];
				arpeggio[2] = periodTable[n+secondAdd];
				break;
			}
	}

	protected void updateArpeggio() {
		currentFXCounter++;
		if( currentFXCounter > 2 ) currentFXCounter=0;
		channel.setPeriod(arpeggio[currentFXCounter], false);
	}

	protected void updateTonePorta() {
		int source = channel.getPeriod();
		if( source==0 ) return;
		if( source > tonePortaDestination ) {
			if( (source-=tonePortaSpeed) < tonePortaDestination )
				source=tonePortaDestination;
		}
		else {
			if( (source+=tonePortaSpeed) > tonePortaDestination )
				source=tonePortaDestination;
		}
		channel.setPeriod( source, true );
	}

	protected void updateVolSlide() {
		channel.setVolume( channel.getVolume()+fxSubValue1-fxSubValue2, true );
	}

	protected void initialiseVibrato( boolean newNote ) {
		getFXSubValues();
		if( fxSubValue1 != 0 ) vibSpeed = fxSubValue1;
		if( fxSubValue2 != 0 ) vibDepth = fxSubValue2;
		updateVibrato(newNote);
	}

	protected void updateVibrato( boolean trig ){
		int pdelta = vibratoLFO.update(vibSpeed,trig)*vibDepth >> 7;
		channel.setPeriod( pdelta+channel.getPeriod(), false );
	}

	protected void setVibratoWave( int waveform ) {
		boolean retrig=true;
		if(waveform>3){
			waveform-=4;
			retrig=false;
		}
		switch(waveform){
			case 0:
				vibratoLFO.setWaveform(ProTrackerLFO.WF_SINUS,retrig);
			break;
			case 1:
				vibratoLFO.setWaveform(ProTrackerLFO.WF_SAWDN,retrig);
			break;
			case 2:
				vibratoLFO.setWaveform(ProTrackerLFO.WF_SQUARE,retrig);
			break;
			case 3:
				vibratoLFO.setWaveform(ProTrackerLFO.WF_RANDOM,retrig);
			break;
		}
	}

	protected void initialiseTremolo( boolean newNote ) {
		getFXSubValues();
		if( fxSubValue1 != 0 ) tremSpeed = fxSubValue1;
		if( fxSubValue2 != 0 ) tremDepth = fxSubValue2;
		updateTremolo(newNote);
	}

	protected void updateTremolo( boolean trig ) {
		int vdelta = tremoloLFO.update(tremSpeed,trig)*tremDepth >> 7;
		channel.setVolume( vdelta+channel.getVolume(), false );
	}

	protected void setTremoloWave( int waveform ) {
		boolean retrig=true;
		if(waveform>3){
			waveform-=4;
			retrig=false;
		}
		switch(waveform){
			case 0:
				tremoloLFO.setWaveform(ProTrackerLFO.WF_SINUS,retrig);
			break;
			case 1:
				tremoloLFO.setWaveform(ProTrackerLFO.WF_SAWDN,retrig);
			break;
			case 2:
				tremoloLFO.setWaveform(ProTrackerLFO.WF_SQUARE,retrig);
			break;
			case 3:
				tremoloLFO.setWaveform(ProTrackerLFO.WF_RANDOM,retrig);
			break;
		}
	}

	protected void updateNoteCut() {
		if( currentFXCounter==0 ) channel.setVolume(0, true);
		currentFXCounter--;
	}

	protected void updateNoteDelay() {
		if( currentFXCounter==0 ) channel.trigger( null, currentFXPeriod );
		currentFXCounter--;
	}

	protected void adjustFineTune() {
		int fine;
		if( currentFXValue < 8 )
			fine = currentFXValue;
		else
			fine = -16 + currentFXValue;
		channel.setFineTune( fine );
	}
}
