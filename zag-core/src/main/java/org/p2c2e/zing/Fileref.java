package org.p2c2e.zing;

import java.io.File;
import java.io.IOException;

public class Fileref implements Comparable<Object> {
	public final static int FILEMODE_WRITE = 0x01;
	public final static int FILEMODE_READ = 0x02;
	public final static int FILEMODE_READWRITE = 0x03;
	public final static int FILEMODE_WRITEAPPEND = 0x05;

	private File file;
	private int usage;

	Fileref(File file, int usage) {
		this.file = file;
		this.usage = usage;
	}

	public File getFile() {
		return file;
	}

	public int getUsage() {
		return usage;
	}

	@Override
	public int compareTo(Object o) {
		return hashCode() - o.hashCode();
	}

	public static Fileref createTemp(int usage) throws IOException {
		File tf = File.createTempFile("zing", null);
		tf.deleteOnExit();
		return new Fileref(tf, usage);
	}

	public static Fileref createByName(int usage, String name) {
		String fname = name.replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
		fname = fname.split("\\.")[0];

		if (fname.isEmpty())
			fname = "null";

		if ((usage & IGlk.FILEUSAGE_DATA) == IGlk.FILEUSAGE_DATA) {
			fname += ".glkdata";
		} else if ((usage & IGlk.FILEUSAGE_SAVED_GAME) == IGlk.FILEUSAGE_SAVED_GAME) {
			fname += ".glksave";
		} else if ((usage & IGlk.FILEUSAGE_TRANSCRIPT) == IGlk.FILEUSAGE_TRANSCRIPT) {
			fname += ".txt";
		} else if ((usage & IGlk.FILEUSAGE_INPUT_RECORD) == IGlk.FILEUSAGE_INPUT_RECORD) {
			fname += ".txt";
		}

		return new Fileref(new File(fname), usage);
	}

	public static Fileref createFromFileref(int usage, Fileref ref) {
		return new Fileref(ref.file, usage);
	}

	public static void deleteFile(Fileref ref) {
		ref.file.delete();
	}

	public void destroy() {
		file = null;
	}

	public boolean fileExists() {
		return file.exists();
	}
}
