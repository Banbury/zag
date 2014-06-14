package org.p2c2e.blorb;

public class Color {
	private int _red;
	private int _green;
	private int _blue;

	public Color(int red, int green, int blue) {
		_red = (red & 0xff);
		_green = (green & 0xff);
		_blue = (blue & 0xff);
	}

	public int getRed() {
		return _red;
	}

	public int getGreen() {
		return _green;
	}

	public int getBlue() {
		return _blue;
	}
}