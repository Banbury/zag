
package micromod.output;

/**
	A HasAvailableOutputDevice has an internal buffer whose
	status can be determined at any time.

	All stream devices have such a buffer, but some may not support
	the ability to request the amount of data within it.
*/
public abstract class HasAvailableOutputDevice extends PCM16StreamOutputDevice {
	/** @return The number of bytes available in the buffer. */
	protected abstract int bytesAvailable();

	/**
		A frame of PCM audio is one sample.
		Eg) 1 frame is 4 bytes with 16 bit stereo audio.
		
		@return the number of frames of audio that can be written
		directly to the buffer without "blocking"
		(having to wait until the space becomes available)
	*/
	public int framesAvailable() {
		return bytesAvailable() / bytesPerFrame;
	}
}

