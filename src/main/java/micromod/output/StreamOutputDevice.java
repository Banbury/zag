
package micromod.output;

/**
	A StreamOutputDevice is fed an 8 bit raw audio stream
	in the audio format native to the device. The data is written
	to a buffer, and then on to the output hardware.
*/
public interface StreamOutputDevice extends OutputDevice {
	/**
		Write length bytes of audio to the output device

		This method should return only when all of the audio has been
		written to the device, except if the device is stopped or
		paused, in which case the method should return immediately.
	*/
	public void write( byte[] buffer, int length );

	/**
		Pause playback immediately.
		Calling start() will continue where you left off.
	*/
	public void pause();

}
