
package micromod;

/**
	A class representing a ProTracker/NoiseTracker/FTK module,
*/
public class Module {
	protected String name, type;
	protected boolean allowsPanning, pal;
	protected Sequence sequence;
	protected Instrument[] instruments;

	public Module( String name, String type, boolean allowsPanning, boolean pal, Sequence sequence, Instrument[] instruments ) {
		this.name = name;
		this.type = type;
		this.allowsPanning = allowsPanning;
		this.pal = pal;
		this.sequence = sequence;
		this.instruments = instruments;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public Instrument getInstrument( int num ) {
		if( num<0 || num>31 ) return null;
		return instruments[num]; 
	}

	public int getDefaultGain() {
		// Give all Protracker-type mods a gain of 2.0
		// No clipping possible with 4 channel mods, since they can't
		// pan all their channels to one side. Extended mods could cause
		// clipping, but it's easy to give the user the ability to adjust
		// things.
		return 65536 << 1;
	}

	public boolean allowsPanning() {
		return allowsPanning;
	}

	public boolean isPAL() {
		return pal;
	}
}
