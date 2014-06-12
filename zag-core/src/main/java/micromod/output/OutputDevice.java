
package micromod.output;

/**
	An OutputDevice represents the basic functionality of an audio device.

	The way in which the device is configured and opened, and the way in which audio
	is written to the device is not defined at this stage.
*/
public interface OutputDevice {
	/**
		Begin playing audio.
		You should call this if you want any output!
		You must also call this after pause() or stop() to resume playback..
	*/
	public void start();

	/**
		Stop playback.
		All audio written to the device should be played before this method returns.
	*/
	public void stop();

	/**
		Close the output device and release any resources.
		Once this method is called, the device must be reopened to allow playback.
		Reopening could involve reinstantiation or calling a device specific open() method.
	*/
	public void close();
}
