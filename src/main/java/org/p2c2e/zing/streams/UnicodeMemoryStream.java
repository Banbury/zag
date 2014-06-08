package org.p2c2e.zing.streams;

import java.nio.ByteBuffer;

import org.p2c2e.zing.IGlk;

public class UnicodeMemoryStream extends Stream {
	ByteBuffer buf;
	int len;

	public UnicodeMemoryStream(ByteBuffer buffer, int buflen, int mode) {
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
		buf.position(pos * 4);
	}

	@Override
	public int getChar() {
		if (canRead && buf != null && pos < len) {
			rcount++;
			pos++;
			int result = buf.getInt();
			if (result > 0xff) {
				return 0x3f;
			}
			return result;
		} else {
			return -1;
		}
	}

	@Override
	public int getCharUni() {
		if (canRead && buf != null && pos < len) {
			rcount++;
			pos++;
			return buf.getInt();
		} else {
			return -1;
		}
	}

	@Override
	public int getBuffer(ByteBuffer b, int l) {
		int num = Math.min(l, len - pos);

		if (canRead && buf != null) {
			if (num > 0) {
				for (int i = 0; i < num; ++i) {
					int c = getChar();
					b.put((byte) c);
				}
			}
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
				buf.putInt(c);
				pos++;
			}
		}
	}

	@Override
	public void putCharUni(int c) {
		if (canWrite) {
			wcount++;

			if (buf != null && pos < len) {
				buf.putInt(c);
				pos++;
			}
		}
	}

	@Override
	public void putBuffer(ByteBuffer b, int l) {
		int num = Math.min(l, len - pos);

		if (canWrite) {
			wcount += l;

			if (num > 0) {
				if (buf != null) {
					for (int i = 0; i < num; ++i) {
						buf.putInt(b.get());
					}
					pos += num;
				}
			}
		}
	}

	@Override
	public void putBufferUni(ByteBuffer b, int l) {
		byte[] arr;
		int num = Math.min(l, len - pos);

		if (canWrite) {
			wcount += l;

			if (num > 0) {
				if (buf != null) {
					arr = new byte[num * 4];
					b.get(arr);
					buf.put(arr);
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
				// fixme: astral
				for (int i = 0; i < num; i++)
					buf.putInt(s.charAt(i));
				pos += num;
			}
		}
	}
}