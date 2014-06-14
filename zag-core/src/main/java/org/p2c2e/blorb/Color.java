package org.p2c2e.blorb;

public class Color {
	private byte _red;
	private byte _green;
	private byte _blue;

	public Color(int red, int green, int blue) {
		_red = (byte) red;
		_green = (byte) green;
		_blue = (byte) blue;
	}

	public int getRed() {
		return _red & 0xff;
	}

	public int getGreen() {
		return _green & 0xff;
	}

	public int getBlue() {
		return _blue & 0xff;
	}
}