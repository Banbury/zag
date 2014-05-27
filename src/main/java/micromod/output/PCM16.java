
package micromod.output;

/**
	PCM (Pulse Code Modulation) audio consists of regularly
	spaced "impulses". The spacing of these impulses per second is
	determined by the sampling rate.
	A correct implementation of this interface should contain an
	appropriate representation of 16 bit PCM audio.
*/
public interface PCM16 {
	/** @return The PCM sampling rate */
	public int getSamplingRate();
}

