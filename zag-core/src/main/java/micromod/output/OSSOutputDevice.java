
package micromod.output;
import micromod.output.converters.*;

/**
	The OSSOutputDevice provides audio output for pre-1.3
	Java VMs by outputing directly to the Linux native audio subsystem.
	The device will operate on any system that provides libMMOSS.so.
*/
public class OSSOutputDevice extends PCM16StreamOutputDevice {
	static{System.loadLibrary("MMOSS");}
	public OSSOutputDevice( int samplingRate ) throws OutputDeviceException{
		initialise(new SS16LEAudioFormatConverter());
		ossInit(samplingRate);
		System.out.println(" Simple OSS/Linux OutputDevice Version 0.3 Initialised.");
	}
	public native void start();
	public native void write( byte[] data, int length );
	public native void pause();
	public native void stop();
	public native void close();
	public native int getSamplingRate();
	protected native void ossInit( int samplingRate ) throws OutputDeviceException;
}
