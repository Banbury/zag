package org.p2c2e.zing;

import java.io.File;
import java.io.IOException;

public class Fileref implements Comparable {
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
		return new Fileref(new File(name), usage);
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
