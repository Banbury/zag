
package micromod.output;
import micromod.output.converters.AudioFormatConverter;

/**
	A PCM16StreamOutputDevice can be fed a 16 bit stereo audio stream,
	which is automatically converted to the native PCM format of the device.
*/
public abstract class PCM16StreamOutputDevice implements StreamOutputDevice, PCM16 {
	// Small buffer to store converted audio.
	protected static int CONVERT_BUFFER_FRAMES = 1024;
	protected byte[] convertBuffer;
	protected int bytesPerFrame;
	protected AudioFormatConverter converter;

	/**
		Configure the device for the specified format.
		This method must be called by any subclasses.
		@param converter An implementation of AudioFormatConverter.
	*/
	protected void initialise( AudioFormatConverter converter ) {
		this.converter = converter;
		bytesPerFrame = converter.getBytesPerFrame();
		convertBuffer = new byte[ CONVERT_BUFFER_FRAMES*bytesPerFrame ];
	}

	/**
		Write length frames of 16 bit stereo audio.
		A frame is one 16 bit stereo sample.
	*/
	public void write( short[] leftBuffer, short[] rightBuffer, int length ) {
		int position=0, outputLen, counter;
		int leftSample, rightSample;

		// Convert audio 1024 bytes at a time if necessary
		while( position<length ) {
			outputLen=length-position;
			if( outputLen>CONVERT_BUFFER_FRAMES ) outputLen=CONVERT_BUFFER_FRAMES;
			converter.convert( leftBuffer, rightBuffer, position, convertBuffer, outputLen );
			write( convertBuffer, outputLen*bytesPerFrame );
			position+=outputLen;
		}
	}
}

