package org.p2c2e.zing.streams;

import java.io.EOFException;
import java.nio.ByteBuffer;

import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.types.StreamResult;

public abstract class Stream implements Comparable {
	protected int filemode;
	protected int rcount;
	protected int wcount;
	protected int pos;
	protected boolean canRead;
	protected boolean canWrite;

	Stream(int mode) {
		filemode = mode;
		rcount = wcount = pos = 0;
		canRead = (filemode == IGlk.FILEMODE_READ || filemode == IGlk.FILEMODE_READ_WRITE);
		canWrite = (filemode == IGlk.FILEMODE_WRITE
				|| filemode == IGlk.FILEMODE_READ_WRITE || filemode == IGlk.FILEMODE_WRITE_APPEND);
	}

	public void setHyperlink(int val) {

	}

	@Override
	public int compareTo(Object o) {
		return hashCode() - o.hashCode();
	}

	public int getPosition() {
		return pos;
	}

	public abstract void setPosition(int p, int seekmode);

	public void setStyle(String sname) {

	}

	public boolean canWrite() {
		return this.canWrite;
	}

	public boolean canRead() {
		return this.canRead;
	}

	public abstract void putChar(int c);

	public abstract void putCharUni(int c);

	public void putInt(int i) {
		putChar((i >>> 24) & 0xff);
		putChar((i >>> 16) & 0xff);
		putChar((i >>> 8) & 0xff);
		putChar(i & 0xff);
	}

	public void putString(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++)
			putChar((s.charAt(i)));
	}

	public void putStringUni(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++)
			putCharUni(s.charAt(i));
	}

	public void putBuffer(ByteBuffer b, int len) {
		for (int i = 0; i < len; i++)
			putChar((b.get(i)));
	}

	public void putBufferUni(ByteBuffer b, int len) {
		for (int i = 0; i < len; i++)
			putCharUni(b.getInt(i * 4));
	}

	public int getChar() {
		return -1;
	}

	public int getCharUni() {
		return -1;
	}

	public int getInt() throws EOFException {
		try {
			return (((getChar() & 0xff) << 24) | ((getChar() & 0xff) << 16)
					| ((getChar() & 0xff) << 8) | (getChar() & 0xff));
		} catch (Exception e) {
			e.printStackTrace();
			throw new EOFException();
		}
	}

	public int getBuffer(ByteBuffer b, int len) {
		int i = 0;
		int val = getChar();

		while (i < len && val != -1) {
			i++;
			b.put((byte) val);
			val = getChar();
		}
		return i;
	}

	public int getBufferUni(ByteBuffer b, int len) {
		int i = 0;
		int val = getCharUni();

		while (i < len && val != -1) {
			i++;
			b.putInt(val);
			val = getCharUni();
		}
		return i;
	}

	public int getLine(ByteBuffer b, int len) {
		int i = 0;
		int val = getChar();

		while (i < len - 1 && val != -1) {
			if ((char) val != '\r') {
				b.put((byte) val);
				i++;
			} else {
				rcount--;
			}
			if ((char) val == '\n') {
				break;
			}
			val = getChar();
		}
		if (len > 0)
			b.put((byte) 0);

		return i;
	}

	public int getLineUni(ByteBuffer b, int len) {
		int i = 0;
		int val = getCharUni();

		while (i < len - 1 && val != -1) {
			i++;
			b.putInt(val);
			if ((char) val == '\n')
				break;
			val = getCharUni();
		}
		if (len > 0)
			b.putInt(0);

		return i;
	}

	public StreamResult close() {
		StreamResult r = new StreamResult();
		r.readcount = rcount;
		r.writecount = wcount;

		return r;
	}
}
