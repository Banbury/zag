package org.p2c2e.zing.streams;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.p2c2e.zing.Fileref;
import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.types.StreamResult;

import ucar.unidata.io.RandomAccessFile;

public class FileStream extends Stream {
	static final String TERMINATOR = System.getProperty("line.separator");
	static final int TERM_LEN = TERMINATOR.length();
	static final Charset UTF32 = Charset.forName("UTF-32BE");

	private RandomAccessFile rfile;

	private Fileref fileref;
	private boolean isText;
	private boolean unicode;

	public FileStream(Fileref ref, int fmode, boolean unicode) {
		super(fmode);
		fileref = ref;
		this.unicode = unicode;

		try {
			switch (fmode) {
			case IGlk.FILEMODE_READ:
				rfile = new RandomAccessFile(fileref.getFile()
						.getAbsolutePath(), "r");
				break;
			case IGlk.FILEMODE_WRITE:
				fileref.getFile().createNewFile();
				rfile = new RandomAccessFile(fileref.getFile()
						.getAbsolutePath(), "rw");
				break;
			case IGlk.FILEMODE_READ_WRITE:
				if (!fileref.getFile().exists())
					fileref.getFile().createNewFile();
				rfile = new RandomAccessFile(fileref.getFile()
						.getAbsolutePath(), "rw");
				break;
			case IGlk.FILEMODE_WRITE_APPEND:
				if (!fileref.getFile().exists())
					fileref.getFile().createNewFile();
				rfile = new RandomAccessFile(fileref.getFile()
						.getAbsolutePath(), "rw");
				int nPos = (int) rfile.length();
				rfile.seek(nPos);
				break;
			default:
				System.err
						.println("Attempt to open file stream with bad mode.");
			}
			rfile.order(RandomAccessFile.BIG_ENDIAN);
		} catch (IOException eio) {
			eio.printStackTrace();
		}

		isText = ((fileref.getUsage() & IGlk.FILEUSAGE_TEXT_MODE) != 0);
	}

	@Override
	public int getChar() {
		try {
			if (canRead && !isEOF()) {
				rcount++;

				if (!isText) {
					return rfile.readByte();
				} else {
					char c = readNextCharUnicode();

					if (!unicode)
						return (c < 0x80) ? c : 0x3F;
					else
						return c;
				}
			} else {
				return -1;
			}
		} catch (IOException eio) {
			eio.printStackTrace();
			return -1;
		}
	}

	@Override
	public int getCharUni() {
		return getChar();
	}

	@Override
	public int getBuffer(ByteBuffer b, int len) {
		try {
			int i = 0;
			while (!isEOF() && i < len) {
				if (unicode) {
					int c = getCharUni();
					b.putInt(c);
				} else {
					int c = getChar();
					b.put((byte) c);
				}
				i++;
			}
			return i;
		} catch (IOException eio) {
			eio.printStackTrace();
			return -1;
		}
	}

	@Override
	public int getLine(ByteBuffer b, int len) {
		try {
			int i = 0;

			byte[] buf = new byte[len];
			while (i < len && !isEOF()) {
				byte bt = rfile.readByte();
				if (bt != '\r') {
					buf[i] = bt;
					i++;
					if (bt == '\n')
						break;
				}
			}

			String s = decodeUtf8(buf);

			b.put(s.getBytes(Charset.forName("US-ASCII")));
			rcount += i;
			return i;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public int getLineUni(ByteBuffer b, int len) {
		try {
			int i = 0;

			while (i < len && !isEOF()) {
				int c = getCharUni();

				if (c != '\r') {
					b.putInt(c);
					i++;
					if (c == '\n')
						break;
				} else {
					rcount--;
				}
			}

			return i;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public void putChar(int c) {
		try {
			if (isText) {
				if (c == '\n') {
					rfile.writeBytes(TERMINATOR);
				} else {
					rfile.write(encodeUtf8("" + c));
				}
			} else {
				rfile.writeByte(c);
			}
			wcount++;
		} catch (IOException eio) {
			eio.printStackTrace();
		}
	}

	@Override
	public void putCharUni(int c) {
		putChar(c);
	}

	@Override
	public void putBuffer(ByteBuffer b, int len) {
		for (int i = 0; i < len; i++) {
			putChar(b.get());
		}
	}

	@Override
	public void putBufferUni(ByteBuffer b, int len) {
		for (int i = 0; i < len; i++) {
			putCharUni(b.getInt());
		}
	}

	@Override
	public void putString(String s) {
		for (int i = 0; i < s.length(); i++) {
			putChar(s.charAt(i));
		}
	}

	@Override
	public void putStringUni(String s) {
		putString(s);
	}

	@Override
	public StreamResult close() {
		try {
			rfile.flush();
			rfile.close();
			return super.close();
		} catch (IOException eio) {
			eio.printStackTrace();
			return null;
		}
	}

	@Override
	public int getPosition() {
		try {
			return (int) rfile.getFilePointer();
		} catch (IOException e) {
			return 0;
		}
	}

	public boolean isEOF() throws IOException {
		return getPosition() == rfile.length();
	}

	@Override
	public void setPosition(int p, int seekmode) {
		try {
			switch (seekmode) {
			case IGlk.SEEKMODE_START:
				rfile.seek(p);
				break;
			case IGlk.SEEKMODE_CURRENT:
				rfile.seek(getPosition() + p);
				break;
			case IGlk.SEEKMODE_END:
				rfile.seek(rfile.length() - p);
				break;
			default:
				System.err
						.println("setting position of file stream: unknown seek mode");
			}
		} catch (IOException eio) {
			System.err.println(eio);
			eio.printStackTrace();
		}
	}

	private byte[] encodeUtf8(String s) {
		return s.getBytes(Charset.forName("UTF-8"));
	}

	private int noBytesUtf8(byte b) {
		if ((0xff & b) < 0x80) {
			return 1;
		} else if ((b & 0xC0) == 0xC0) {
			return 2;
		} else if ((b & 0xE0) == 0xE0) {
			return 3;
		} else if ((b & 0xF0) == 0xF0) {
			return 4;
		}
		return 1;
	}

	private String decodeUtf8(byte[] buf) {
		return new String(buf, Charset.forName("UTF-8"));
	}

	private char readNextCharUnicode() throws IOException {
		byte b = rfile.readByte();
		rfile.unread();
		int n = noBytesUtf8(b);

		byte[] buf = rfile.readBytes(n);
		return decodeUtf8(buf).charAt(0);
	}
}