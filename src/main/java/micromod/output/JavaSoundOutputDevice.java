
package micromod.output;
import micromod.output.converters.AudioFormatConverter;
import javax.sound.sampled.*;

/**
	An OutputDevice for the Java1.3 audio system.
	The one Java should have had 3 years ago ...

	Because of the clunky way JavaSound drains it's buffer,
	this class implements a workaround that alters the behaviour of
	framesAvailable() to return more fine-grained values.

          30 July 2003 (Jon Zeppieri):  added getLine(), so we can
          control volume on the line

          18 March 2005 (Jon Zeppieri):  inserted a sourceDataLine.flush() 
          into the close() method, to eliminate a delay on the Macintosh
*/
public class JavaSoundOutputDevice extends HasAvailableOutputDevice {
	protected int samplingRate, bufferFrames, available;
	protected long timeMillis, lastMillis;
	protected SourceDataLine sourceDataLine;

	/**
		@param format An instance of micromod.output.AudioFormatConverter
		@param samplingRate Try 44100
		@param bufferTimeMillis The number of milliseconds of audio to buffer.
	*/
	public JavaSoundOutputDevice( AudioFormatConverter converter, int samplingRate, int bufferTimeMillis ) throws OutputDeviceException {
		this.samplingRate = samplingRate;
		int bitsPerSample = converter.getBytesPerFrame()/converter.getNumberOfChannels()*8;
		initialise(converter);
		AudioFormat outFormat = new AudioFormat( samplingRate, bitsPerSample,
		                                         converter.getNumberOfChannels(),
		                                         converter.isSigned(),
		                                         converter.isBigEndian() );
		try {
			DataLine.Info lineInfo = new DataLine.Info( SourceDataLine.class, outFormat );
			sourceDataLine = (SourceDataLine)AudioSystem.getLine(lineInfo);
			sourceDataLine.open( outFormat, (bufferTimeMillis*samplingRate/1000)*bytesPerFrame );
		}
		catch( LineUnavailableException e ) {
			//e.printStackTrace();
			throw new OutputDeviceException(" JavaSoundOutputDevice: Can't get a valid audio line!");
		}
		bufferFrames=sourceDataLine.getBufferSize()/bytesPerFrame;
		System.out.println("\n JavaSound Realtime Output Device 0.8 initialised.");
	}

  // Added 30 July 2003 (Jon Zeppieri)
  public SourceDataLine getLine()
  {
    return sourceDataLine;
  }

	public int framesAvailable() {
		// A bit cleaner than the last version, eh?
		timeMillis=System.currentTimeMillis();
		available += (int)(timeMillis-lastMillis)*samplingRate/1000;
		int realAvailable=super.framesAvailable();
		if(available>realAvailable||realAvailable>=bufferFrames) available=realAvailable;
		lastMillis=timeMillis;
		return available;
	}

	public void write( byte[] buffer, int length ) {
		sourceDataLine.write( buffer, 0, length );
		available-=length/bytesPerFrame;
	}

	public int getSamplingRate() {
		return samplingRate;
	}

	public void start() {
		sourceDataLine.start();
		available=super.framesAvailable();
		lastMillis=System.currentTimeMillis();
	}

	public void pause() {
		sourceDataLine.stop();
	}

	public void stop() {
          // 18 March 2005, Jon Zeppieri: added the flush() call, to eliminate 
          // delays on the Mac
		sourceDataLine.flush();
		sourceDataLine.drain();
		// Assume hardware buffer is max. 50% JS buffersize (as it is on my linux system)
		try{Thread.sleep(bufferFrames*500/samplingRate);}catch(InterruptedException ie){}
		sourceDataLine.stop();
	}

	public void close() {
		sourceDataLine.close();
	}

	protected int bytesAvailable() {
		return sourceDataLine.available();
	}
}
