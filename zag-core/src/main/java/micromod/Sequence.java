
package micromod;

/**
	This class represents the sequence information contained in a
	ProTracker style music module. S3M and XM all have ProTracker-style
	sequences.
*/
public class Sequence {
	public int[] patternOrder = new int[128];
	public Pattern[] patterns = new Pattern[128];
	public int numberOfChannels, songLengthPatterns;
	public int restartPosition, numberOfPatterns;
	public int defaultBPM, defaultTempo;

	/** Get the number of channels, so the Mixer can calibrate itself */
	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	/** Get the song length, in patterns. */
	public int getSongLength() {
		return songLengthPatterns;
	}

	public static class Pattern {
		public int[][] instrument;
		public int[][] period;
		public int[][] volColumn;
		public int[][] effectCommand;
		public int[][] effectValue;

		public Pattern( int numChannels ) {
			instrument = new int[64][numChannels];
			period = new int[64][numChannels];
			volColumn = new int[64][numChannels];
			effectCommand = new int[64][numChannels];
			effectValue = new int[64][numChannels];
		}
	}
}
