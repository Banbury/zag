
package micromod;

import micromod.resamplers.Resampler;

/**
	A Channel attemps to do a bit of Amiga hardware emulation to play
	ProTracker modules a bit better than most. In my experience, this implementation
	can play some modules that even SurfSmurf can't manage. There are still one or
	two tunes that won't sound entirely correct, but I believe this is one of the
	better implementations.

	The justification for the design of this class is that it provides a protracker
	specific channel interface and allows the InstrumentPlayer,LoopDecoder,resampler
	interfaces to be reused in other mod players.
	In fact, InstrumentPlayer and LoopDecoder have been hacked to better emulate
	ProTracker. This is an advantage of using different codebases for each module type.
*/
public class Channel {
	protected static final int FIXED_POINT_SHIFT=16;

	protected int period, volume, fineTune;
	protected int amplitude, leftPan, rightPan, mixerStepPrecalc;
	protected InstrumentPlayer instrumentPlayer;

	/**
		A log table for calculating fine tuned periods.
	*/
	protected int[] fineTuneTable = new int[] {
		17358, 17233, 17109, 16986, 16864, 16743, 16622, 16503,
		16384, 16266, 16149, 16033, 15918, 15803, 15689, 15576 };

	/**
		Constructor
	*/
	public Channel( int samplingRate, boolean pal ) {
		instrumentPlayer = new InstrumentPlayer();
		configure(samplingRate, pal);
		reset();
	}

	/**
		Configure the Channel to use the specified sampling
		rate and PAL/NTSC mode.
	*/
	public void configure( int samplingRate, boolean pal ) {
		// Set up a precalculated pitch constant
		int cpuClock = Synthesizer.CBM_AMIGA_NTSC_CLOCK;
		if(pal) cpuClock = Synthesizer.CBM_AMIGA_PAL_CLOCK;
		mixerStepPrecalc = ((cpuClock<<8)/samplingRate) << 4;
	}

	/**
		Overrides reset() to reinitialise the default volume and period.
	*/
	public void reset() {
		instrumentPlayer.assignInstrument(new Instrument());
		instrumentPlayer.setAssigned();
		// Set default centre panning.
		amplitude = 0;
		leftPan = 65536;
		rightPan = 65536;
		// Set channel properties
		volume=0;
		period=0;
		fineTune=0;
	}

	/**
		Trigger an instrument on the channel to play at the specified pitch.
		Call this for every row, even if the row is empty, since it resets the
		volume and pitch modulations done by eg) vibrato/tremolo

		ProTracker behaviour is currently as follows:

		Valid instrument, period > 0 : Plays from instrument start as normal.
		Null instrument,  period > 0 : Assigned instrument is triggered at period.
		Valid instrument, period = 0 : Instrument is "assigned" but not triggered.
		Valid instrument, period =-1 : Instrument is "switched over" without retrig.

		Anything else is ignored. This behaviour will probably change after some
		testing, since I know it's not exactly correct.
	*/
	public void trigger( Instrument instrument, int period ) {
		// If the volume or pitch was being modulated, reset the volume and pitch.
		setVolume( this.volume, false );
		setPeriod( this.period, false );

		if( instrument!=null ) {
			// Assign the specified instrument
			instrumentPlayer.assignInstrument(instrument);
			setVolume(instrument.volume, true);

			if( period==-1 ) {
				// Tone porta switch
				if( this.period!=0 ) {
					switchInstrument(false);
					setPeriod( this.period, false );
				}
			}
			else if( period>0 ) {
				// A normal trigger.
				switchInstrument(true);
				setPeriod( period, true );
			}
		}
		else {
			// Trigger assigned
			if( period>0 ) {
				switchInstrument(true);
				setPeriod( period, true );
			}
		}
	}

	/**
		Get the current period of the channel.Used so the Modulator can, er, modulate
	*/
	public int getPeriod() {
		return period;
	}

	/**
		Get the current ProTracker volume of the channel, from 0 to 64.
	*/
	public int getVolume() {
		return volume;
	}

	/**
		Set the period using the conventional ProTracker period notation.
		@param permanent if false, the period change only affects one subsequent call to output()
	*/
	public void setPeriod( int period, boolean permanent ) {
		if( period == 0 ) return;
		if( period < 113 ) period = 113;
		if( period > 856 ) period = 856;
		calculateMixerStep( period, fineTune );
		if(permanent) this.period = period;
	}

	/**
		Set the volume from 0 to 64.
		@param permanent if false, the period change only affects one subsequent call to output()
	*/
	public void setVolume( int volume, boolean permanent ) {
		if(volume>64) volume=64;
		if(volume<0 ) volume=0;
		amplitude = volume << 10;
		if(permanent) this.volume = volume;
	}

	/**
		Set the current sample position
	*/
	public void setSamplePosition( int samplePosition ) {
		instrumentPlayer.setSamplePosition( samplePosition );
	}

	/**
		Set the finetune. The finetune of the channel is set automatically
		when an instrument is assigned. This method is used for the (useless)
		set finetune ProTracker command.
	*/
	public void setFineTune( int fineTune ) {
		this.fineTune = fineTune;
		calculateMixerStep( period, fineTune );
	}

	/**
		Set the leftAmp and rightAmp members to specify the panning. leftAmp and rightAmp
		are signed integers from -65536 to 65536 to specify the value a sample on either
		channel should be multiplied by. If one of these is negative, a "surround" effect can be achieved.
	*/
	public void setPanning( int left, int right ) {
		leftPan = left;
		rightPan = right;
	}

	/**
		@return the current left-speaker amplitude of the channel, in 16 bit fixed point format.
		This value can be negative, and if it is, the audio should be inverted for a surround effect.
	*/
	public int getLeftAmplitude() {
		return amplitude*(leftPan>>1) >> FIXED_POINT_SHIFT-1;
	}

	/**
		@return the current right-speaker amplitude of the channel, in 16 bit fixed point format.
		This value can be negative, and if it is, the audio should be inverted for a surround effect.
	*/
	public int getRightAmplitude() {
		return amplitude*(rightPan>>1) >> FIXED_POINT_SHIFT-1;
	}

	/**
		@return true if a call to getAudio() would result only in a blank buffer.
	*/
	public boolean isSilent() {
		return instrumentPlayer.hasFinished();
	}

	/**
		Get resampled audio into the specified buffers. Volume and panning are not applied. You must
		scale the amplitude according to getLeftAmplitude() and getRightAmplitude().

		@param snapBack Do not update the sample-pointers, so the same audio can be retrieved again.
	*/
	public void getAudio( short[] buffer, int length, Resampler resampler, boolean snapBack ) {
		// Dont attempt to generate audio when no mixer step calculated.
		if(period==0) return;
		// Obtain the resampled audio
		instrumentPlayer.getAudio( buffer, length, resampler, snapBack );
		// Idea for XM/IT player - update instrument envelopes here.
	}

	/**
		Play the assigned instrument.
		@param trig If true, will play instrument from start.
	*/
	protected void switchInstrument( boolean trig ) {
		instrumentPlayer.setAssigned();
		fineTune = instrumentPlayer.getInstrument().fineTune;
		if(trig) instrumentPlayer.setSamplePosition(0);
	}

	/**
		Calculate the step and subStep values for the resampler, from the
		specified period and fineTune value.
	*/
	protected void calculateMixerStep( int period, int fineTune ) {
		if( period == 0 ) return;
		int ftPeriod = (period*fineTuneTable[fineTune+8]) >> 7;
		instrumentPlayer.setResampleFactor( ((mixerStepPrecalc<<6)/ftPeriod) << 4 );
	}
}
