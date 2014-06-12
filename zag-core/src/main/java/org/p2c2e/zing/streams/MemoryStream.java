package org.p2c2e.zing.streams;

import java.nio.ByteBuffer;

import org.p2c2e.zing.IGlk;

public class MemoryStream extends Stream {
	ByteBuffer buf;
	int len;

	public MemoryStream(ByteBuffer buffer, int buflen, int mode) {
		super(mode);
		buf = buffer;
		len = buflen;

		if (mode == IGlk.FILEMODE_WRITE_APPEND)
			System.err
					.println("Attempt to open memory stream with mode WriteAppend.");
	}

	@Override
	public void setPosition(int p, int seekmode) {
		switch (seekmode) {
		case IGlk.SEEKMODE_START:
			pos = p;
			break;
		case IGlk.SEEKMODE_CURRENT:
			pos += p;
			break;
		case IGlk.SEEKMODE_END:
			pos = len + p;
			break;
		default:
			System.err
					.println("setting position of memory stream: unknown seek mode");
		}
		if (pos < 0)
			pos = 0;
		if (pos > len)
			pos = len;
		buf.position(pos);
	}

	@Override
	public int getChar() {
		if (canRead && buf != null && pos < len) {
			rcount++;
			pos++;
			return buf.get() & 0xff;
		} else {
			return -1;
		}
	}

	@Override
	public int getCharUni() {
		return getChar();
	}

	@Override
	public int getBuffer(ByteBuffer b, int l) {
		byte[] arr;
		int num = Math.min(l, len - pos);

		if (canRead && buf != null) {
			if (num > 0) {
				arr = new byte[num];
				buf.get(arr);
				b.put(arr);
			}
			rcount += num;
			pos += num;
			return num;
		} else {
			return 0;
		}
	}

	@Override
	public void putChar(int c) {
		if (canWrite) {
			wcount++;

			if (buf != null && pos < len) {
				buf.put((byte) c);
				pos++;
			}
		}
	}

	@Override
	public void putCharUni(int c) {
		if (canWrite) {
			wcount++;

			if (buf != null && pos < len) {
				if (c > 0xff) {
					c = 0x3f;
				}
				buf.put((byte) c);
				pos++;
			}
		}
	}

	@Override
	public void putBuffer(ByteBuffer b, int l) {
		byte[] arr;
		int num = Math.min(l, len - pos);

		if (canWrite) {
			wcount += l;
			if (num > 0) {
				if (buf != null) {
					arr = new byte[num];
					b.get(arr);
					buf.put(arr);
					pos += num;
				}
			}
		}
	}

	@Override
	public void putBufferUni(ByteBuffer b, int l) {
		int num = Math.min(l, len - pos);

		if (canWrite) {
			wcount += l;

			if (num > 0) {
				if (buf != null) {
					for (int i = 0; i < num; ++i) {
						int c = b.getInt();
						if (c > 0xff)
							c = 0x3f;
						buf.put((byte) c);
					}
					pos += num;
				}
			}
		}
	}

	@Override
	public void putString(String s) {
		int l = s.length();
		int num = Math.min(l, len - pos);

		if (canWrite) {
			wcount += l;

			if (buf != null) {
				for (int i = 0; i < num; i++)
					buf.put((byte) s.charAt(i));
				pos += num;
			}
		}
	}
}