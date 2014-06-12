
package micromod.output.converters;

/**
	Unsigned mono 8 bit cheesy output.
	This should allow anyone with an 8 bit soundcard (or just a half-arsed driver)
	to get at least some audio out of most of the output devices.
*/
public class UM8AudioFormatConverter implements AudioFormatConverter {
	public int getBytesPerFrame()    { return 1; }
	public int getNumberOfChannels() { return 1; }
	public boolean isSigned()        { return false; }
	public boolean isBigEndian()     { return false; }

	public void convert( short[] left, short[] right, int position, byte[] output, int length ) {
		int counter=0;
		length+=position;
		for ( int i=position; i<length; i++ ) {
			output[counter++] = (byte)( (left[i]+right[i]>>9)+128 );
		}
	}
}

