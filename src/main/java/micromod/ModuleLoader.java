
package micromod;

import java.io.*;

public class ModuleLoader {
	/**
		@return true if stream contains a valid MOD file.
	*/
	public static boolean identify( DataInput modStream ) throws IOException {
		// Get to the identifier
		modStream.skipBytes(1080);
		String info = DataReader.readText(modStream, 4);
		System.out.println(" Checking MOD type : "+info);
		// Check the identifier
		try{ getFormatInfo(info); } catch (IllegalArgumentException e) { return false; }
		return true;
	}

	public static Module read( DataInput dataInput ) throws IOException, IllegalArgumentException {
		String name = DataReader.readText( dataInput, 20 );

		// Read the instruments
		Instrument[] instruments = new Instrument[32];
		for( int n=1; n<32; n++ )
			instruments[n] = readInstrument( dataInput );

		// Set up the sequence
		Sequence sequence = new Sequence();
		sequence.songLengthPatterns = DataReader.readUnsigned7Bit(dataInput);
		sequence.restartPosition = DataReader.readUnsigned7Bit(dataInput);
		sequence.defaultBPM = 125;
		sequence.defaultTempo = 6;

		// Read in the actual pattern order.
		for( int n=0; n<128; n++ ) {
			sequence.patternOrder[n] = DataReader.readUnsigned7Bit(dataInput);
			if( sequence.numberOfPatterns < sequence.patternOrder[n] ) sequence.numberOfPatterns = sequence.patternOrder[n];
		}
		sequence.numberOfPatterns++;

		// Check module type
		String id = DataReader.readText( dataInput, 4 );
		ModFormatInfo fInfo = getFormatInfo( id );
		sequence.numberOfChannels = fInfo.numChan;

		// Read the patterns
		for( int n=0; n<sequence.numberOfPatterns; n++ )
			sequence.patterns[n] = readPattern( dataInput, sequence.numberOfChannels );

		// Read instrumentdata. The module will still play even if some samples
		// are missing, so only warn about this.
		try {
			for( int n=1; n<32; n++ )
				if( instruments[n]!=null ) readSampleData( instruments[n], dataInput );
		}
		catch (EOFException e) {
			System.out.println("Warning : Module is truncated!");
		}

		return new Module( name, id, fInfo.supportsPan, fInfo.pal, sequence, instruments );
	}

	/**
		@return Bits 0-11 gives number of channels, bit 12 is set if format uses the panning
			commands. Returns zero if mod is unrecognized.
	*/
	protected static ModFormatInfo getFormatInfo( String id ) throws IllegalArgumentException {
		ModFormatInfo mfi = new ModFormatInfo();
		if( id.equals("M.K.")||id.equals("M!K!")||id.equals("FLT4")||id.equals("N.T.")||id.equals("M&K!") ) {
			// 4-Channel Karsten/Mahoney&Kaktus/Freelancers style MOD.
			mfi.numChan=4;
			mfi.pal=true;
			mfi.supportsPan=false;
			return mfi;
		}
		if( id.equals("CD81")||id.equals("OKTA")||id.equals("FLT8") ) {
			// Oktalyzer-type module. Not so sure about CD81.
			mfi.numChan=8;
			mfi.pal=true;
			mfi.supportsPan=false;
			return mfi;
		}
		if( id.regionMatches( false, 1, "CHN", 0, 3 ) ) {
			// FTK. Normally 6/8 channel.
			mfi.numChan = Integer.parseInt( id.substring(0,1) );
			mfi.pal=false;
			mfi.supportsPan=true;
			return mfi;
		}
		if( id.regionMatches( false, 0, "TDZ", 0, 3 ) ) {
			// TDZ - No idea! 4-8 channels. Probably an Amiga/ST mod format.
			mfi.numChan = Integer.parseInt( id.substring(3,4) );
			mfi.pal=false;
			mfi.supportsPan=false;
			return mfi;
		}
		if( id.regionMatches( false, 2, "CH", 0, 2 )||id.regionMatches( false, 2, "CN", 0, 2 ) ) {
			// Generic extended mod format. ModPlug saves multichannel mods with the "xxCH" id.
			mfi.numChan = Integer.parseInt( id.substring(0,2) );
			mfi.pal=false;
			mfi.supportsPan=true;
			return mfi;
		}
		throw new IllegalArgumentException("Module format \""+id+"\" unrecognised!");
	}

	/**
		Read the data for a Pattern into a new Pattern object and return it.
	*/
	protected static Sequence.Pattern readPattern( DataInput dataInput, int numChannels ) throws IOException {
		int note;
		Sequence.Pattern patt = new Sequence.Pattern(numChannels);
		for( int m=0; m<64; m++ ) {
			for( int c=0; c<numChannels; c++ ) {
				note = DataReader.readInt32(dataInput);
				patt.instrument[m][c] = ((note&0xF000)>>12)|((note&0x10000000)>>24);
				patt.period[m][c] = (note&0xFFF0000)>>16;
				patt.effectCommand[m][c] = (note&0xF00)>>8;
				if( patt.effectCommand[m][c] == 0xE ) {
					patt.effectCommand[m][c] = 0xE0 | ((note&0xF0)>>4);
					patt.effectValue[m][c] = note&0xF;
				}
				else {
					patt.effectValue[m][c] = note&0xFF;
					if( patt.effectCommand[m][c] == 0xD ) patt.effectValue[m][c] = nibbleDecimal2Bin((byte)patt.effectValue[m][c]);
				}
			}
		}
		return patt;
	}

	/**
		Read in the data for an Instrument, and return a new Instrument containing that data.
	*/
	protected static Instrument readInstrument( DataInput dataInput ) throws IOException {
		Instrument inst   = new Instrument();
		inst.name         = DataReader.readText( dataInput, 22 );
		inst.sampleLength = DataReader.readUnsigned16Bit(dataInput) << 1;
		int fineTuneByte  = DataReader.readUnsigned8Bit(dataInput) & 0x0F;
		inst.fineTune     = (fineTuneByte&0x07)-(fineTuneByte&0x08);
		inst.volume       = DataReader.readSigned8Bit(dataInput);
		inst.loopStart    = DataReader.readUnsigned16Bit(dataInput) << 1;
		int loopLength    = DataReader.readUnsigned16Bit(dataInput) << 1;
		inst.data         = new byte[inst.sampleLength + 3];

		// Validate and configure the loop
		inst.looped = true;
		if(inst.loopStart+loopLength > inst.sampleLength)
			loopLength=inst.sampleLength-inst.loopStart;
		if(inst.loopStart >= inst.sampleLength) loopLength=0;
		if( loopLength < 3 ) {
			inst.looped = false;
			inst.loopStart = 0;
			inst.sampleEnd = inst.sampleLength;
		} else {
			inst.sampleEnd = inst.loopStart+loopLength;
		}

		return inst;
	}

	/**
		Read the number of bytes specified by the Instrument's sampleLength field into the
		Instrument's data array, and zero the first 2 samples, as ProTracker is supposed to
		do (According to Lars Hamre's modspec.txt)
	*/ 
	protected static void readSampleData( Instrument instrument, DataInput dataInput ) throws IOException {
		if( instrument.sampleEnd > 0 )
			DataReader.readSigned8BitArray( dataInput, instrument.data, 0, instrument.sampleLength );
		instrument.data[0] = 0;
		instrument.data[1] = 0;
	}

	/**
		Return a 2-digit decimal value written as hex as a binary number.
	*/
	protected static byte nibbleDecimal2Bin( byte decValue ) {
		return (byte) ( ((decValue&0xf0)>>4)*10 + (decValue&0xf) );
	}

	/**
		A type for the module format information.
	*/
	protected static class ModFormatInfo {
		public int numChan;
		public boolean pal, supportsPan;
	}
}
