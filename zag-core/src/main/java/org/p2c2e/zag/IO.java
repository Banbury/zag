package org.p2c2e.zag;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import org.p2c2e.blorb.Color;
import org.p2c2e.util.FastByteBuffer;
import org.p2c2e.zing.Dispatch2;
import org.p2c2e.zing.Fileref;
import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.IWindow;
import org.p2c2e.zing.ObjectCallback;
import org.p2c2e.zing.SoundChannel;
import org.p2c2e.zing.streams.Stream;
import org.p2c2e.zing.types.GlkDate;
import org.p2c2e.zing.types.GlkEvent;
import org.p2c2e.zing.types.GlkTimeval;
import org.p2c2e.zing.types.GlkType;
import org.p2c2e.zing.types.InByteBuffer;
import org.p2c2e.zing.types.InOutByteBuffer;
import org.p2c2e.zing.types.InOutIntBuffer;
import org.p2c2e.zing.types.OutByteBuffer;
import org.p2c2e.zing.types.OutInt;
import org.p2c2e.zing.types.OutWindow;
import org.p2c2e.zing.types.StreamResult;

public final class IO {
	static final int IFhd = (((byte) 'I') << 24) | (((byte) 'F') << 16)
			| (((byte) 'h') << 8) | (byte) 'd';
	static final int CMem = (((byte) 'C') << 24) | (((byte) 'M') << 16)
			| (((byte) 'e') << 8) | (byte) 'm';
	static final int UMem = (((byte) 'U') << 24) | (((byte) 'M') << 16)
			| (((byte) 'e') << 8) | (byte) 'm';
	static final int Stks = (((byte) 'S') << 24) | (((byte) 't') << 16)
			| (((byte) 'k') << 8) | (byte) 's';
	static final int MAll = (((byte) 'M') << 24) | (((byte) 'A') << 16)
			| (((byte) 'l') << 8) | (byte) 'l';

	static final int NULL = 0;
	static final int FILTER = 1;
	static final int GLK = 2;

	static final int PUT_CHAR = 0x0080;
	static final int PUT_CHAR_STREAM = 0x0081;
	static final int PUT_STRING = 0x0082;
	static final int PUT_STRING_STREAM = 0x0083;
	static final int PUT_BUFFER = 0x0084;
	static final int PUT_BUFFER_STREAM = 0x0085;
	static final int CHAR_TO_LOWER = 0x00a0;
	static final int CHAR_TO_UPPER = 0x00a1;
	static final int WINDOW_OPEN = 0x0023;
	static final int WINDOW_CLEAR = 0x002a;
	static final int WINDOW_MOVE_CURSOR = 0x002b;
	static final int BUFFER_TO_LOWER_CASE_UNI = 0x0120;
	static final int BUFFER_TO_UPPER_CASE_UNI = 0x0121;
	static final int BUFFER_TO_TITLE_CASE_UNI = 0x0122;
	static final int PUT_CHAR_UNI = 0x0128;
	static final int PUT_STRING_UNI = 0x0129;
	static final int PUT_BUFFER_UNI = 0x012A;
	static final int PUT_CHAR_STREAM_UNI = 0x012B;
	static final int PUT_STRING_STREAM_UNI = 0x012C;
	static final int PUT_BUFFER_STREAM_UNI = 0x012D;

	static final Object[] P0 = new Object[0];
	static final Object[] P1 = new Object[1];
	static final Object[] P2 = new Object[2];
	static final Object[] P3 = new Object[3];
	static final Object[] P4 = new Object[4];
	static final Object[] P5 = new Object[5];
	static final Object[] P6 = new Object[6];
	static final Object[] P7 = new Object[7];
	static final Object[] P8 = new Object[8];
	static final Object[] P9 = new Object[9];
	static final Object[][] P = { P0, P1, P2, P3, P4, P5, P6, P7, P8, P9 };

	private IGlk glk;

	int sys;
	int rock;

	HuffmanTree htree;

	LinkedList<Integer> undoData = new LinkedList<Integer>();
	Fileref undoFile;
	Stream undoStream;

	HashMap<Integer, Object> ors;

	public IO(IGlk glk, Zag z) {
		this.glk = glk;
		ors = new HashMap<Integer, Object>();
		init(z);
	}

	void init(Zag z) {
		int stringtbl = z.memory.getInt(28);
		if (stringtbl > 0)
			htree = new HuffmanTree(z, stringtbl);
		/*
		 * if (undoStream != null) { glk.streamClose(undoStream, null);
		 * undoStream = null; } if (undoFile != null) {
		 * glk.filerefDeleteFile(undoFile); glk.filerefDestroy(undoFile);
		 * undoFile = null; } undoData = new LinkedList();
		 */
		sys = 0;
		rock = 0;

		glk.setCreationCallback(new CreationCallback());
		glk.setDestructionCallback(new DestructionCallback());
	}

	void wrongNumArgs() {
		Zag.fatal("Wrong number of arguments to GLK function.");
	}

	void noSuchMethod(int s) {
		Zag.fatal("Attempt to call nonexistent (or unimplemented) GLK function: "
				+ s);
	}

	String getStringFromMemory(FastByteBuffer mem, int addr) {
		StringBuffer sb = new StringBuffer();
		int t = mem.get(addr++);
		if (t == (byte) 0xe0) {
			while ((t = mem.get(addr++)) != 0)
				sb.append((char) t);
		} else if (t == (byte) 0xe2) {
			addr -= 1;
			while ((t = mem.getInt(addr += 4)) != 0)
				sb.appendCodePoint(t);
		} else {
			Zag.fatal("Cannot send compressed string to GLK function: " + t);
		}
		return sb.toString();
	}

	String getUnicodeStringFromMemory(FastByteBuffer mem, int addr, int len) {
		StringBuffer sb = new StringBuffer();
		int end = addr + len * 4;
		while (addr < end) {
			int t = mem.getInt(addr += 4);
			sb.appendCodePoint(t);
		}
		return sb.toString();
	}

	void writeUnicodeStringToMemory(String string, FastByteBuffer mem, int addr) {
		for (int i = 0; i < string.length(); ++i) {
			int cp = string.codePointAt(i);
			if (cp > 0xffff) {
				++i;
			}
			mem.putInt(addr += 4, cp);
		}
	}

	String truncate(String str, int len) {
		StringBuffer sb = new StringBuffer();
		int newOutLen = 0;
		int index = 0;
		while (index < str.length()) {
			newOutLen++;
			if (newOutLen > len) {
				break;
			}
			int cp = str.codePointAt(index++);
			if (cp > 0xffff) {
				index++;
			}
			sb.appendCodePoint(cp);
		}
		return sb.toString();
	}

	int glk(Zag z, int selector, int numargs, int[] args) {
		FastByteBuffer mem = z.memory;
		int ret = 0;

		switch (selector) {
		case PUT_CHAR:
			if (numargs != 1)
				wrongNumArgs();
			glk.putChar((char) (args[0] & 0xff));
			break;
		case PUT_CHAR_UNI:
			if (numargs != 1)
				wrongNumArgs();
			glk.putCharUni(args[0]);
			break;
		case PUT_CHAR_STREAM:
			if (numargs != 2)
				wrongNumArgs();
			glk.putCharStream((Stream) ors.get(new Integer(args[0])),
					(char) (args[1] & 0xff));
			break;
		case PUT_CHAR_STREAM_UNI:
			if (numargs != 2)
				wrongNumArgs();
			glk.putCharStream((Stream) ors.get(new Integer(args[0])), args[1]);
			break;
		case PUT_STRING:
			if (numargs != 1)
				wrongNumArgs();
			glk.putString(getStringFromMemory(mem, args[0]));
			break;
		case PUT_STRING_UNI:
			if (numargs != 1)
				wrongNumArgs();
			glk.putString(getStringFromMemory(mem, args[0]));
			break;

		case PUT_STRING_STREAM:
		case PUT_STRING_STREAM_UNI:
			if (numargs != 2)
				wrongNumArgs();
			glk.putStringStream((Stream) ors.get(new Integer(args[0])),
					getStringFromMemory(mem, args[1]));
			break;
		case PUT_BUFFER:
			if (numargs != 2)
				wrongNumArgs();
			mem.position(args[0]);
			glk.putBuffer(new InByteBuffer(mem.slice()), args[1]);
			break;
		case PUT_BUFFER_UNI:
			if (numargs != 2)
				wrongNumArgs();
			mem.position(args[0]);
			glk.putBufferUni(new InByteBuffer(mem.slice()), args[1]);
			break;
		case PUT_BUFFER_STREAM:
			if (numargs != 3)
				wrongNumArgs();
			mem.position(args[1]);
			glk.putBufferStream((Stream) ors.get(new Integer(args[0])),
					new InByteBuffer(mem.slice()), args[2]);
			break;
		case PUT_BUFFER_STREAM_UNI:
			if (numargs != 3)
				wrongNumArgs();
			mem.position(args[1]);
			glk.putBufferStreamUni((Stream) ors.get(new Integer(args[0])),
					new InByteBuffer(mem.slice()), args[2]);
			break;
		case CHAR_TO_LOWER:
			if (numargs != 1)
				wrongNumArgs();
			ret = Character.toLowerCase((char) (args[0] & 0xff));
			break;
		case CHAR_TO_UPPER:
			if (numargs != 1)
				wrongNumArgs();
			ret = Character.toUpperCase((char) (args[0] & 0xff));
			break;
		case WINDOW_OPEN:
			if (numargs != 5)
				wrongNumArgs();
			ret = glk.windowOpen((IWindow) ors.get(new Integer(args[0])),
					args[1], args[2], args[3], args[4]).hashCode();
			break;
		case WINDOW_CLEAR:
			if (numargs != 1)
				wrongNumArgs();
			glk.windowClear((IWindow) ors.get(new Integer(args[0])));
			break;
		case WINDOW_MOVE_CURSOR:
			if (numargs != 3)
				wrongNumArgs();
			glk.windowMoveCursor((IWindow) ors.get(new Integer(args[0])),
					args[1], args[2]);
			break;
		case BUFFER_TO_LOWER_CASE_UNI: {
			if (numargs != 3)
				wrongNumArgs();
			String unistr = getUnicodeStringFromMemory(mem, args[0] - 4,
					args[2]);
			unistr = unistr.toLowerCase();
			int len = args[1];
			int outLen = unistr.codePointCount(0, unistr.length());
			if (outLen > len) {
				unistr = truncate(unistr, len);
			}
			writeUnicodeStringToMemory(unistr, mem, args[0] - 4);
			ret = outLen;
		}
			break;
		case BUFFER_TO_UPPER_CASE_UNI: {
			if (numargs != 3)
				wrongNumArgs();
			String unistr = getUnicodeStringFromMemory(mem, args[0] - 4,
					args[2] + 4);
			unistr = unistr.toUpperCase();
			int len = args[1];
			int outLen = unistr.codePointCount(0, unistr.length());
			if (outLen > len) {
				unistr = truncate(unistr, len);
			}
			writeUnicodeStringToMemory(unistr, mem, args[0] - 4);
			ret = outLen;
		}
			break;
		case BUFFER_TO_TITLE_CASE_UNI:
		/*
		 * Glk does not actually give enough information to correctly handle
		 * title casing -- in particular, locale is not specified. So we're just
		 * going to punt on this and upcase the first letter. This avoids the
		 * need for many megabytes of ICU libraries which wouldn't actually help
		 * anyway. Sorry, Dutch and Turkish IF authors -- blame Zarf for not
		 * providing proper localization.
		 */
		{
			if (numargs != 4)
				wrongNumArgs();
			String unistr = getUnicodeStringFromMemory(mem, args[0] - 4,
					args[2]);
			boolean lowerrest = args[3] != 0;
			if (lowerrest) {
				unistr = unistr.toLowerCase();
			}
			StringBuffer sb = new StringBuffer();
			int firstChar = unistr.codePointAt(0);
			sb.appendCodePoint(Character.toUpperCase(firstChar));
			sb.append(unistr.substring(firstChar > 0xffff ? 2 : 1));
			unistr = sb.toString();
			int len = args[1];
			int outLen = unistr.codePointCount(0, unistr.length());
			if (outLen > len) {
				unistr = truncate(unistr, len);
			}
			writeUnicodeStringToMemory(unistr, mem, args[0] - 4);
			ret = outLen;
		}
			break;
		default:
			int addr;
			Class<?> c;
			Method m = Dispatch2.getMethod(selector);
			if (m == null)
				noSuchMethod(selector);
			Class<?>[] f = m.getParameterTypes();
			if (f.length != numargs)
				wrongNumArgs();

			Object[] p = P[numargs];
			for (int i = 0; i < numargs; i++) {
				c = f[i];
				if (c == IWindow.class || c == Stream.class
						|| c == Fileref.class || c == SoundChannel.class) {
					p[i] = ors.get(new Integer(args[i]));
				} else if (c == OutWindow.class) {
					p[i] = new OutWindow();
				} else if (c == int.class) {
					p[i] = new Integer(args[i]);
				} else if (c == char.class) {
					p[i] = new Character((char) args[i]);
				} else if (c == String.class) {
					p[i] = getStringFromMemory(mem, args[i]);
				} else if (c == OutInt.class) {
					p[i] = new OutInt();
				} else if (c == InByteBuffer.class) {
					mem.position(args[i]);
					p[i] = new InByteBuffer(mem.slice());
				} else if (c == OutByteBuffer.class) {
					mem.position(args[i]);
					p[i] = new OutByteBuffer(mem.slice());
				} else if (c == InOutByteBuffer.class) {
					mem.position(args[i]);
					p[i] = new InOutByteBuffer(mem.slice());
				} else if (c == InOutIntBuffer.class) {
					mem.position(args[i]);
					p[i] = new InOutIntBuffer(mem.slice().asIntBuffer());
				} else if (c == GlkEvent.class) {
					p[i] = new GlkEvent();
				} else if (c == StreamResult.class) {
					p[i] = new StreamResult();
				} else if (c == Color.class) {
					p[i] = new Color((args[i] >>> 16), (args[i] >>> 8), args[i]);
				} else if (c == GlkTimeval.class) {
					mem.position(args[i]);
					p[i] = new GlkTimeval(mem.getInt(), mem.getInt(),
							mem.getInt());
				} else if (c == GlkDate.class) {
					mem.position(args[i]);
					p[i] = new GlkDate(mem.getInt(), mem.getInt(),
							mem.getInt(), mem.getInt(), mem.getInt(),
							mem.getInt(), mem.getInt(), mem.getInt());
				} else {
					Zag.fatal("Unimplemented parameter type: " + c.getName());
				}
			}

			Object oRet = null;
			try {
				oRet = m.invoke(glk, p);
			} catch (Exception eAccess) {
				String stError = "Could not dispatch call ["
						+ Integer.toHexString(selector) + "] to GLK: "
						+ eAccess;
				if (eAccess instanceof InvocationTargetException) {
					stError += "\n" + eAccess.getCause();
					eAccess.getCause().printStackTrace();
				} else {
					eAccess.printStackTrace();
				}
				Zag.fatal(stError);
			}

			Class<?> retType = (oRet == null) ? null : m.getReturnType();

			for (int i = 0; i < numargs; i++) {
				c = f[i];
				addr = args[i];
				if (GlkType.class.isAssignableFrom(c)) {
					GlkType t = (GlkType) p[i];
					if (t.isOut()) {
						if (addr == -1) {
							z.sp = t.pushToBuffer(z.sp, z.stack);
						} else if (addr != 0) {
							addr = t.pushToBuffer(addr, mem);
						}
					}
				}
			}

			if (retType != null) {
				if (retType == IWindow.class || retType == Stream.class
						|| retType == Fileref.class
						|| retType == SoundChannel.class) {
					ret = oRet.hashCode();
				} else if (retType == char.class) {
					ret = ((Character) oRet).charValue();
				} else if (retType == int.class) {
					ret = ((Integer) oRet).intValue();
				} else if (retType == boolean.class) {
					ret = ((Boolean) oRet).booleanValue() ? 1 : 0;
				} else {
					Zag.fatal("Unimplemented return type for GLK function.");
				}
			}
		}
		return ret;
	}

	void setSys(int newsys, int newrock) {
		switch (newsys) {
		case FILTER:
			sys = newsys;
			rock = newrock;
			break;
		case GLK:
			sys = newsys;
			rock = 0;
			break;
		default:
			sys = 0;
			rock = 0;
		}
	}

	void streamNum(Zag z, int n, boolean started, int position) {
		Zag.StringCallResult r;
		String s = String.valueOf(n);
		int len = s.length();

		switch (sys) {
		case GLK:
			for (int i = position; i < len; i++)
				glk.putChar(s.charAt(i));
			if (started) {
				r = z.popCallstubString();
				if (r.pc != 0)
					Zag.fatal("String-on-string call stub while printing number.");
			}
			break;
		case FILTER:
			if (!started) {
				z.pushCallstub(0x11, 0);
			}
			if (position >= len) {
				r = z.popCallstubString();
				if (r.pc != 0)
					Zag.fatal("String-on-string call stub while printing number.");
			} else {
				int tmp = z.pc;
				z.pc = n;
				z.pushCallstub(0x12, position + 1);
				z.pc = tmp;
				z.enterFunction(z.memory, rock, 1,
						new int[] { s.charAt(position) });
			}
			break;
		default:
		}
	}

	void streamChar(Zag z, int c) {
		switch (sys) {
		case FILTER:
			z.pushCallstub(0, 0);
			z.enterFunction(z.memory, rock, 1, new int[] { c });
			break;
		case GLK:
			glk.putChar((char) c);
			break;
		default:
		}
	}

	void streamUniChar(Zag z, int c) {
		switch (sys) {
		case FILTER:
			z.pushCallstub(0, 0);
			z.enterFunction(z.memory, rock, 1, new int[] { c });
			break;
		case GLK:
			glk.putCharUni((char) c);
			break;
		default:
		}
	}

	void streamString(Zag z, int addr, int inmiddle, int bit) {
		byte ch;
		int oaddr;
		int type;
		boolean alldone = false;
		boolean started = (inmiddle != 0);

		while (!alldone) {
			if (inmiddle == 0) {
				type = z.memory.get(addr++) & 0xff;
				bit = 0;
			} else {
				if (inmiddle == 1)
					type = 0xe0;
				else if (inmiddle == 3)
					type = 0xe2;
				else
					type = 0xe1;
			}

			if (type == 0xe1) {
				if (htree == null)
					Zag.fatal("Attempt to stream a compressed string with no Huffman table.");
				HuffmanTree.Node troot = htree.root;
				HuffmanTree.Node n;
				int done = 0;

				if (troot == null)
					troot = htree.readTree(z,
							z.memory.getInt(htree.startaddr + 8), false);

				n = troot;

				while (done == 0) {
					switch (n.type) {
					case 0x00:
						boolean on;
						byte b = z.memory.get(addr);

						if (bit > 0)
							b >>= bit;
						on = ((b & 1) != 0);

						if (++bit > 7) {
							bit = 0;
							addr++;
						}

						if (on)
							n = (n.right == null) ? (n.right = htree.readTree(
									z, n.rightaddr, false)) : n.right;
						else
							n = (n.left == null) ? (n.left = htree.readTree(z,
									n.leftaddr, false)) : n.left;
						break;
					case 0x01:
						done = 1;
						break;
					case 0x02:
						switch (sys) {
						case GLK:
							glk.putChar((char) ((n.c) & 0xff));
							break;
						case FILTER:
							if (!started) {
								z.pushCallstub(0x11, 0);
								started = true;
							}
							z.pc = addr;
							z.pushCallstub(0x10, bit);
							z.enterFunction(z.memory, rock, 1,
									new int[] { n.c & 0xff });
							return;
						default:
							break;
						}
						n = troot;
						break;
					case 0x03:
						switch (sys) {
						case GLK:
							z.memory.position(n.addr);
							glk.putBuffer(new InByteBuffer(z.memory.slice()),
									n.numargs);
							n = troot;
							break;
						case FILTER:
							if (!started) {
								z.pushCallstub(0x11, 0);
								started = true;
							}
							z.pc = addr;
							z.pushCallstub(0x10, bit);
							inmiddle = 1;
							addr = n.addr;
							done = 2;
							break;
						default:
							n = troot;
						}
						break;
					case 0x04:
						switch (sys) {
						case GLK:
							glk.putCharUni(n.u);
							break;
						case FILTER:
							if (!started) {
								z.pushCallstub(0x11, 0);
								started = true;
							}
							z.pc = addr;
							z.pushCallstub(0x10, bit);
							z.enterFunction(z.memory, rock, 1,
									new int[] { n.u });
							return;
						default:
							break;
						}
						n = troot;
						break;
					case 0x05:
						switch (sys) {
						case GLK:
							z.memory.position(n.addr);
							glk.putBufferUni(
									new InByteBuffer(z.memory.slice()),
									n.numargs);
							n = troot;
							break;
						case FILTER:
							if (!started) {
								z.pushCallstub(0x11, 0);
								started = true;
							}
							z.pc = addr;
							z.pushCallstub(0x10, bit);
							inmiddle = 3;
							addr = n.addr - 3;
							done = 2;
							break;
						default:
							n = troot;
						}
						break;
					case 0x08:
					case 0x09:
					case 0x0a:
					case 0x0b:
						int otype;
						oaddr = n.addr;
						if (n.type == 0x09 || n.type == 0x0b)
							oaddr = z.memory.getInt(oaddr);
						otype = z.memory.get(oaddr) & 0xff;

						if (!started) {
							z.pushCallstub(0x11, 0);
							started = true;
						}
						if (otype >= 0xe0 && otype <= 0xff) {
							z.pc = addr;
							z.pushCallstub(0x10, bit);
							inmiddle = 0;
							addr = oaddr;
							done = 2;
						} else if (otype >= 0xc0 && otype <= 0xdf) {
							z.pc = addr;
							z.pushCallstub(0x10, bit);
							z.enterFunction(z.memory, oaddr, n.numargs, n.args);
							return;
						} else {
							Zag.fatal("Attempting indirect reference to unknown object while "
									+ "decoding string.");
						}
						break;
					default:
						Zag.fatal("Unknown node type in cached Huffman tree.");
					}
				}

				if (done > 1)
					continue;

			} else if (type == 0xe0) {
				switch (sys) {
				case GLK:
					while ((ch = z.memory.get(addr++)) != 0)
						glk.putChar((char) ((ch) & 0xff));
					break;
				case FILTER:
					if (!started) {
						z.pushCallstub(0x11, 0);
						started = true;
					}
					ch = z.memory.get(addr++);
					if (ch != 0) {
						z.pc = addr;
						z.pushCallstub(0x13, 0);
						z.enterFunction(z.memory, rock, 1,
								new int[] { ch & 0xff });
						return;
					}
					break;
				default:
				}
			} else if (type == 0xe2) {
				int uch;
				addr -= 1;
				switch (sys) {
				case GLK:
					while ((uch = z.memory.getInt(addr += 4)) != 0)
						glk.putCharUni(uch);
					break;
				case FILTER:
					if (!started) {
						z.pushCallstub(0x11, 0);
						started = true;
					}
					uch = z.memory.getInt(addr += 4);
					if (uch != 0) {
						z.pc = addr + 1;
						z.pushCallstub(0x14, 0);
						z.enterFunction(z.memory, rock, 1, new int[] { uch });
						return;
					}
					break;
				default:
				}
			} else if (type >= 0xe0 && type <= 0xff) {
				Zag.fatal("Attempt to print unknown type of string.");
			} else {
				Zag.fatal("Attempt to print non-string.");
			}

			if (!started) {
				alldone = true;
			} else {
				Zag.StringCallResult r = z.popCallstubString();
				if (r.pc == 0) {
					alldone = true;
				} else {
					addr = r.pc;
					bit = r.bitnum;
					inmiddle = 2;
				}
			}
		}
	}

	int saveUndo(Zag z) {
		if (undoFile == null) {
			undoFile = glk.filerefCreateTemp(IGlk.FILEUSAGE_DATA
					| IGlk.FILEUSAGE_BINARY_MODE, 0);
			undoStream = glk.streamOpenFile(undoFile, IGlk.FILEMODE_READ_WRITE,
					0);
		}
		int pos = undoStream.getPosition();
		undoData.addFirst(new Integer(pos));

		int res = saveGame(z, undoStream.hashCode());
		return res;
	}

	int saveGame(Zag z, int streamId) {
		SaveSize savesize;
		Stream s;
		int pos;
		int end;
		int val;

		s = (Stream) ors.get(new Integer(streamId));
		if (s == null)
			return 1;

		try {
			pos = s.getPosition();
			savesize = saveState(z, s);
			end = s.getPosition();

			val = savesize.size;
			s.setPosition(pos + 4, IGlk.SEEKMODE_START);
			s.putInt(val);

			s.setPosition(pos + 152, IGlk.SEEKMODE_START);
			s.putInt(savesize.memSize);

			s.setPosition(s.getPosition() + savesize.memSize
					+ (((savesize.memSize & 1) == 0) ? 4 : 5),
					IGlk.SEEKMODE_START);
			s.putInt(savesize.stackSize);
			s.setPosition(end, IGlk.SEEKMODE_START);

			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
	}

	int restoreGame(Zag z, int streamId) {
		Stream in = (Stream) ors.get(new Integer(streamId));
		boolean success;

		if (in == null)
			return 1;
		try {
			success = restoreState(z, in);
			return (success) ? 0 : 1;
		} catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
	}

	int restoreUndo(Zag z) {
		int res;

		if (undoData.isEmpty())
			return 1;

		try {
			int pos = undoData.removeFirst().intValue();
			undoStream.setPosition(pos, IGlk.SEEKMODE_START);
			res = restoreState(z, undoStream) ? 0 : 1;
			undoStream.setPosition(pos, IGlk.SEEKMODE_START);
		} catch (Exception e) {
			e.printStackTrace();
			res = 1;
		}
		return res;
	}

	boolean restoreState(Zag z, Stream in) throws IOException {
		int chunkSize;
		int iType;
		int iPos;
		int ch;
		int iChunkEnd;
		int bData;
		int bLen;
		RandomAccessFile f;
		DataInputStream din;
		FastByteBuffer memBuf = null;
		FastByteBuffer stackBuf = null;
		FastByteBuffer mallBuf = null;
		int memSize = 0;
		int fileLen;
		ByteBuffer headBuf = ByteBuffer.allocate(128);
		byte[] gameHeadArr = new byte[128];
		boolean okay = true;
		boolean checkedHeader = false;

		okay &= (((char) in.getChar()) == 'F');
		okay &= (((char) in.getChar()) == 'O');
		okay &= (((char) in.getChar()) == 'R');
		okay &= (((char) in.getChar()) == 'M');
		chunkSize = in.getInt();

		okay &= (((char) in.getChar()) == 'I');
		okay &= (((char) in.getChar()) == 'F');
		okay &= (((char) in.getChar()) == 'Z');
		okay &= (((char) in.getChar()) == 'S');

		iType = in.getInt();
		while (okay) {
			switch (iType) {
			case IFhd:
				okay &= ((chunkSize = in.getInt()) == 128);

				in.getBuffer(headBuf, 128);
				f = new RandomAccessFile(z.gamefile, "r");
				f.seek(z.fileStartPos);
				din = new DataInputStream(new BufferedInputStream(
						new FileInputStream(f.getFD())));
				din.readFully(gameHeadArr);
				din.close();
				f.close();

				for (int i = 0; okay && i < 128; i++)
					okay &= (headBuf.get(i) == gameHeadArr[i]);

				checkedHeader = true;
				break;
			case UMem:
				chunkSize = in.getInt();
				memSize = in.getInt();
				memBuf = new FastByteBuffer(memSize - z.ramstart);
				in.getBuffer(memBuf.asByteBuffer(), memSize - z.ramstart);
				break;
			case CMem:
				chunkSize = in.getInt();
				iChunkEnd = in.getPosition() + chunkSize;
				memSize = in.getInt();
				memBuf = new FastByteBuffer(memSize - z.ramstart);
				f = new RandomAccessFile(z.gamefile, "r");
				fileLen = (int) f.length();
				f.seek(z.fileStartPos + z.ramstart);
				din = new DataInputStream(new BufferedInputStream(
						new FileInputStream(f.getFD())));
				bLen = 0;

				for (iPos = z.ramstart; iPos < memSize; iPos++) {
					if (iPos + z.fileStartPos < fileLen)
						bData = (din.read() & 0xff);
					else
						bData = 0;

					if (in.getPosition() >= iChunkEnd) {
						// NOOP
					} else if (bLen > 0) {
						bLen--;
					} else {
						ch = in.getChar();
						if (ch == 0)
							bLen = (in.getChar() & 0xff);
						else
							bData ^= ch;
					}

					memBuf.put(iPos - z.ramstart, (byte) bData);
				}
				din.close();
				f.close();
				break;

			case Stks:
				chunkSize = in.getInt();
				stackBuf = new FastByteBuffer(chunkSize);
				in.getBuffer(stackBuf.asByteBuffer(), chunkSize);
				break;

			case MAll:
				chunkSize = in.getInt();
				mallBuf = new FastByteBuffer(chunkSize);
				in.getBuffer(mallBuf.asByteBuffer(), chunkSize);
				break;

			default:
				chunkSize = in.getInt();
				in.setPosition(chunkSize, IGlk.SEEKMODE_CURRENT);
			}

			// remoced - DMT
			// done = (checkedHeader && stackBuf != null && memBuf != null);
			// if (!done)
			// {
			if ((chunkSize & 1) != 0)
				in.getChar();
			iType = in.getInt();
			if (iType == -1) {
				break;
			}
			// }
		}

		if (okay) {
			z.setMemSize(memSize);
			for (int i = z.ramstart; i < z.endmem; i++) {
				if (i >= z.protectend || i < z.protectstart)
					z.memory.put(i, memBuf.get(i - z.ramstart));
			}

			z.sp = stackBuf.capacity();
			for (int i = 0; i < z.sp; i++)
				z.stack.put(i, stackBuf.get(i));

			if (mallBuf != null) {
				mallBuf.position(0);

				int heapStart = mallBuf.getInt();
				int nblocks = mallBuf.getInt();
				int end = 0;
				Heap heap = new Heap(z, heapStart);
				for (int i = 0; i < nblocks; ++i) {
					int blockStart = mallBuf.getInt();
					int blockSize = mallBuf.getInt();
					int blockEnd = blockStart + blockSize;
					if (blockEnd > end) {
						end = blockEnd;
						heap.reserveUpTo(end);
					}
					heap.mallocAt(blockStart, blockSize);
				}
				z.heap = heap;
			}

		}

		return okay;
	}

	SaveSize saveState(Zag z, Stream out) throws IOException {
		int i;
		int pos;
		int memChunkSize, stackChunkSize, heapChunkSize = 0;
		SaveSize savesize;
		int startPos = out.getPosition();

		out.putChar('F');
		out.putChar('O');
		out.putChar('R');
		out.putChar('M');

		out.putInt(0);

		out.putChar('I');
		out.putChar('F');
		out.putChar('Z');
		out.putChar('S');

		out.putChar('I');
		out.putChar('F');
		out.putChar('h');
		out.putChar('d');

		out.putInt(128);

		z.memory.position(0);
		out.putBuffer(z.memory.asByteBuffer(), 128);

		out.putChar('C');
		out.putChar('M');
		out.putChar('e');
		out.putChar('m');

		out.putInt(0);

		pos = out.getPosition();
		saveCmem(z, out);
		memChunkSize = out.getPosition() - pos;
		if ((memChunkSize & 1) != 0)
			out.putChar(0);

		out.putChar('S');
		out.putChar('t');
		out.putChar('k');
		out.putChar('s');

		out.putInt(0);

		pos = out.getPosition();
		saveStks(z, out);
		stackChunkSize = out.getPosition() - pos;
		if ((stackChunkSize & 1) != 0)
			out.putChar(0);

		if (z.heap != null && z.heap.isActive()) {
			out.putChar('M');
			out.putChar('A');
			out.putChar('l');
			out.putChar('l');

			int[] blocks = z.heap.getUsedBlocks();

			heapChunkSize = 8 + blocks.length * 4;
			out.putInt(heapChunkSize);

			out.putInt(z.heap.getHeapStart());
			out.putInt(blocks.length / 2);

			for (i = 0; i < blocks.length; i++) {
				out.putInt(blocks[i]);
			}
		}

		savesize = new SaveSize();
		savesize.size = out.getPosition() - startPos - 4;
		savesize.memSize = memChunkSize;
		savesize.stackSize = stackChunkSize;
		savesize.heapSize = heapChunkSize;
		return savesize;
	}

	void saveStks(Zag z, Stream out) {
		z.stack.position(0);
		out.putBuffer(z.stack.asByteBuffer(), z.sp);
	}

	void saveCmem(Zag z, Stream out) throws IOException {
		BufferedInputStream in;
		int oval, xval;
		int runlen = 0;
		RandomAccessFile f = new RandomAccessFile(z.gamefile, "r");

		out.putInt(z.endmem);

		f.seek(z.fileStartPos + z.ramstart);
		in = new BufferedInputStream(new FileInputStream(f.getFD()));

		for (int i = z.ramstart; i < z.endmem; i++) {
			xval = (z.memory.get(i)) & 0xff;
			if (i < z.extstart)
				xval ^= in.read();

			if (xval == 0) {
				runlen++;
			} else {
				while (runlen > 0) {
					oval = (runlen >= 256) ? 256 : runlen;
					out.putChar(0);
					out.putChar(oval - 1);
					runlen -= oval;
				}

				out.putChar(xval);
			}
		}
		in.close();
		f.close();
	}

	static final class SaveSize {
		int memSize;
		int stackSize;
		int size;
		int heapSize;
	}

	final class CreationCallback implements ObjectCallback {
		@Override
		public void callback(Object o) {
			ors.put(new Integer(o.hashCode()), o);
		}
	}

	final class DestructionCallback implements ObjectCallback {
		@Override
		public void callback(Object o) {
			if (o instanceof SoundChannel)
				try {
					((SoundChannel) o).stop();
				} catch (Exception e) {
					System.err.println("could not stop sound channel: " + e);
				}

			ors.remove(new Integer(o.hashCode()));
		}
	}
}
