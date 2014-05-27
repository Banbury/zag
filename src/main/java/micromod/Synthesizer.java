
package micromod;

/**
	The Synthesizer represents a bunch of Modulators, each with an
	associated Channel. Methods are provided for setting up/updating
	the Modulators, and for extracting audio from the channels.
*/
public class Synthesizer {
	public static final int CBM_AMIGA_PAL_CLOCK  = 7093789;
	public static final int CBM_AMIGA_NTSC_CLOCK = 7159090;

	protected Module module;
	protected Modulator[] modulators;
	protected Channel[] channels;

	protected int numberOfChannels, samplingRate, ciaTickSamples, ciaPreCalc;
	protected boolean pal;

	/**
		Constructor.
		@param mod The module to use initially.
		@param mixingRate The sampling rate.
		@param resamp The Resampler to use.
	*/
	public Synthesizer( Module mod, int mixingRate ) {
		samplingRate=mixingRate;
		setModule(mod);
	}

	/**
		Reconfigure the Synthesizer for a new module.
	*/
	public void setModule( Module mod ) {
		module = mod;
		numberOfChannels = mod.getSequence().getNumberOfChannels();
		modulators = new Modulator[numberOfChannels];
		channels = new Channel[numberOfChannels];
		for( int n=0; n<numberOfChannels; n++ ) {
			channels[n] = new Channel( 8000, false );
			modulators[n] = new Modulator( channels[n], mod.allowsPanning() );
		}
		usePAL(mod.isPAL());
		setSamplingRate(samplingRate);
		reset();
	}

	/**
		Set the Synthesizer to produce audio at the specified sampling rate.
	*/
	public void setSamplingRate( int rate ) {
		samplingRate=rate;
		for( int n=0; n<numberOfChannels; n++ ) {
			channels[n].configure(rate,pal);
		}
	}

	/**
		Set whether to decode audio in PAL or NTSC mode. 
		@param p If true, use PAL pitch, else use NTSC pitch and tempo.
	*/
	public void usePAL( boolean p ) {
		pal=p;
		for( int n=0; n<numberOfChannels; n++ ) {
			channels[n].configure(samplingRate, pal);
		}
		// Set up the CIA timer constant.
		int ciaClock = CBM_AMIGA_NTSC_CLOCK/10;
		if( pal ) ciaClock = CBM_AMIGA_PAL_CLOCK/10;
		ciaPreCalc = (1773447*1200)/ciaClock;
	}

	/** @return true if the Synthesizer is configured for PAL, otherwise NTSC */
	public boolean isPAL() {
		return pal;
	}

	/**
		Silence the channels and reset panning to the default values.
	*/
	public void reset() {
		// Configure the default panning.
		int low=0, high=65536, decider=0;
		if( !module.allowsPanning() ){
			// Reduce the stereo separation a bit.
			low=13107;
			high=52428;
		}
		for( int n=0; n<numberOfChannels; n++ ) {
			// Reset the channel
			modulators[n].reset();
			channels[n].reset();
			// Set panning.
			decider = n&3;
			if(decider==0||decider==3) channels[n].setPanning( high, low );
			else channels[n].setPanning( low, high );
		}
	}

	/**
		Set the BPM of the playback (which directly affects the number of
		samples produced per tick of the CIA clock)
	*/
	public void setBPM( int bpm ) {
		ciaTickSamples = (ciaPreCalc*samplingRate)/(bpm*1200);
	}

	/** @return The current number of samples of output per tick. */
	public int getCiaTickSamples() {
		return ciaTickSamples;
	}

	/** @return the number of channels the Synthesizer is configured for */
	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	/** @return The specified Channel object */
	public Channel getChannel( int channel ){
		return channels[channel];
	}

	/** Call this method at the start of every row to assign instruments and initialise effects */
	public void initialiseFX( int channel, int period, int instrument, int effectCommand, int effectValue ) {
		modulators[channel].initialiseFX( period, module.getInstrument(instrument), effectCommand, effectValue );
	}

	/**
		This method is called every tick except the first in a row to update the
		effect currently active on each channel.
	*/
	public void updateFX() {
		for( int n=0; n<numberOfChannels; n++ ) {
			modulators[n].updateFX();
		}
	}
}
