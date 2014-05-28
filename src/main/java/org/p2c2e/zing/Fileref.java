package org.p2c2e.zing;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

public class Fileref implements Comparable {
	static final JFileChooser fc = new JFileChooser(
			System.getProperty("user.dir"));

	File f;
	int u;

	Fileref(File file, int usage) {
		f = file;
		u = usage;
	}

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
		return new Fileref(ref.f, usage);
	}

	public static void deleteFile(Fileref ref) {
		ref.f.delete();
	}

	public void destroy() {
		f = null;
	}

	public boolean fileExists() {
		return f.exists();
	}
}
