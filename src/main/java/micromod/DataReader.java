
package micromod;

import java.io.*;

/**
	The DataReader contains methods to convert data on an implementation
	of DataInput into a more useful format.
	DataInput is only used to read bytes and arrays of bytes to aid portability.
*/
public class DataReader {
	protected static final int CONVERT_BUFFER_SIZE = 16384;
	protected byte[] convertBuffer;
	protected DataInput input;

	/** Constructor */
	public DataReader( DataInput input ) {
		this.input = input;
		convertBuffer = new byte[CONVERT_BUFFER_SIZE];
	}

	/** Read length 16 bit signed, big endian words from the DataInput device into the specified array.*/
	public void readSigned16BitArray( short[] array, int offset, int length ) throws IOException {
// Need to be able to recover as many bytes as possible after eof.
System.out.println("DataReader : operation not implemented !");
	}

	/** Read a signed 32 bit integer. */
	public static int readInt32( DataInput input ) throws IOException {
		return input.readInt();
	}

	/** Read a positive number from a byte between 0 and 127. */
	public static byte readUnsigned7Bit( DataInput input ) throws IOException {
		return (byte)(input.readByte() & 0x7F);
	}

	/** Read a single signed byte. */
	public static byte readSigned8Bit( DataInput input ) throws IOException {
		return input.readByte();
	}

	/** Read a single unsigned byte. */
	public static int readUnsigned8Bit( DataInput input ) throws IOException {
		return input.readUnsignedByte();
	}

	/** Read length 8 bit signed words from the DataInput device into the specified array.*/
	public static void readSigned8BitArray( DataInput input, byte[] array, int offset, int length ) throws IOException {
		input.readFully( array, offset, length );
	}

	/** Read a single signed 16 bit word. */
	public static short readSigned16Bit( DataInput input ) throws IOException {
		return input.readShort();
	}

	/** Read a single unsigned 16 bit word. */
	public static int readUnsigned16Bit( DataInput input ) throws IOException {
		return input.readUnsignedShort();
	}

	/**
		Simple method to read ASCII text and convert nulls to spaces.
	*/
	public static String readText( DataInput input, int count ) throws IOException {
		byte[] bytes = new byte[count];
		input.readFully( bytes );
		for( int n=0; n<count; n++ )
			if( bytes[n] == 0 )
				bytes[n] = 32;
		return new String( bytes, "8859_1" );
	}
}


