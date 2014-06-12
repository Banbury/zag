
package micromod.output.converters;

/**
	The AudioFormatConverter interface defines a class that can convert
	blocks of PCM audio into different formats.
*/
public interface AudioFormatConverter {
	/** @return the number of bytes output per stereo sample of input. */
	public int getBytesPerFrame();

	/** @return 2 if this converter produces stereo output, 1 if mono */
	public int getNumberOfChannels();

	/** @return true if the converter outputs signed samples */
	public boolean isSigned();

	/** @return true if the converter outputs big endian samples */
	public boolean isBigEndian();

	/**
		Perform conversion of audio.
		length*getBytesPerFrame() bytes are written to the start of the output array.

		@param left the left buffer
		@param right the right buffer
		@param position the start position in the input buffers.
		@param length the number of "frames" or stereo samples to convert
	*/
	public void convert( short[] left, short[] right, int position, byte[] output, int length );
}

