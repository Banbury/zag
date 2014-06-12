
package micromod.output.converters;

/** Signed, Stereo, 16 bit, Big Endian */
public class SS16BEAudioFormatConverter implements AudioFormatConverter {
	public int getBytesPerFrame()    { return 4; }
	public int getNumberOfChannels() { return 2; }
	public boolean isSigned()        { return true; }
	public boolean isBigEndian()     { return true; }

	public void convert( short[] left, short[] right, int position, byte[] output, int length ) {
		int leftSample, rightSample, counter=0;
		length+=position;
		for ( int i=position; i<length; i++ ) {
			leftSample = left[i];
			rightSample = right[i];
			output[counter++] = (byte)(leftSample >> 8 );
			output[counter++] = (byte)(leftSample & 0xFF );
			output[counter++] = (byte)(rightSample >> 8);
			output[counter++] = (byte)(rightSample & 0xFF);
		}
	}
}

