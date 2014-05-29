package org.p2c2e.zing;

import java.util.TreeMap;

public interface IWindow {

	public static final int BLANK = 2;
	public static final int PAIR = 1;
	public static final int TEXT_BUFFER = 3;
	public static final int TEXT_GRID = 4;
	public static final int GRAPHICS = 5;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int ABOVE = 2;
	public static final int BELOW = 3;
	public static final int FIXED = 0x10;
	public static final int PROPORTIONAL = 0x20;

	public Stream getStream();

	public Stream getEchoStream();

	public void setEchoStream(Stream s);

	public int getWindowWidth();

	public int getWindowHeight();

	public IWindow getParent();

	public IWindow getSibling();

	public Stream.Result closeStream();

	public int compareTo(Object o);

	public TreeMap getHintedStyles();

	public void putString(String s);

	public void putCharUni(int c);

	public void putChar(int c);

	public void clear();

	public boolean measureStyle(String stName, int hint, Int b);

	public boolean requestLineInput(LineInputConsumer c, String init, int max);

	public boolean requestCharacterInput(CharInputConsumer c);

	public boolean requestMouseInput(MouseInputConsumer c);

	public void cancelCharacterInput();

	public String cancelLineInput();

	public void cancelMouseInput();

	public void cancelHyperlinkInput();

	public void requestHyperlinkInput(HyperlinkInputConsumer hic);

}