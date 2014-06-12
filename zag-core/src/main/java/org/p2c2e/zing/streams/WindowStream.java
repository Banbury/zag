package org.p2c2e.zing.streams;

import java.nio.ByteBuffer;

import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.IWindow;
import org.p2c2e.zing.Style;

public class WindowStream extends Stream {
	IWindow w;

	public WindowStream(IWindow w) {
		super(IGlk.FILEMODE_WRITE);
		this.w = w;
	}

	@Override
	public void setHyperlink(int val) {
		w.setHyperlink(val);
	}

	@Override
	public int getChar() {
		// NOOP
		return -1;
	}

	@Override
	public void setPosition(int p, int seekmode) {
		// NOOP
	}

	@Override
	public void setStyle(String stylename) {
		Style s = (Style) w.getHintedStyles().get(stylename);
		if (s == null)
			s = (Style) w.getHintedStyles().get("normal");

		w.setStyle(s);
	}

	@Override
	public void putChar(int c) {
		wcount++;
		w.putChar((char) c);
		if (w.getEchoStream() != null)
			w.getEchoStream().putChar(c);
	}

	@Override
	public void putCharUni(int c) {
		wcount++;
		w.putCharUni(c);
		if (w.getEchoStream() != null)
			w.getEchoStream().putCharUni(c);
	}

	@Override
	public void putString(String s) {
		wcount += s.length();
		w.putString(s);
		if (w.getEchoStream() != null)
			w.getEchoStream().putString(s);
	}

	@Override
	public void putBuffer(ByteBuffer b, int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++)
			sb.append((char) b.get());

		wcount += len;
		w.putString(sb.toString());
		if (w.getEchoStream() != null)
			w.getEchoStream().putBuffer(b, len);
	}

	@Override
	public void putBufferUni(ByteBuffer b, int len) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++)
			sb.appendCodePoint(b.getInt());

		wcount += len;
		w.putString(sb.toString());
		if (w.getEchoStream() != null)
			w.getEchoStream().putBufferUni(b, len);
	}

}