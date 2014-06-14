package org.p2c2e.blorb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

public class ListPalette implements Palette {
	LinkedList<Color> _li = new LinkedList<Color>();

	ListPalette(BlorbFile.Chunk c) throws IOException {
		InputStream in = c.getData();
		int i = in.read();

		while (i != -1) {
			_li.add(new Color(i, in.read(), in.read()));
			i = in.read();
		}

		in.close();
	}

	@Override
	public boolean isColorList() {
		return true;
	}

	public Iterator<Color> iterator() {
		return _li.iterator();
	}
}
