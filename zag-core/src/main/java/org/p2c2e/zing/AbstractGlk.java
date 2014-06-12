package org.p2c2e.zing;

import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.p2c2e.blorb.BlorbFile;
import org.p2c2e.util.GlkMethod;
import org.p2c2e.zing.streams.FileStream;
import org.p2c2e.zing.streams.MemoryStream;
import org.p2c2e.zing.streams.Stream;
import org.p2c2e.zing.streams.UnicodeMemoryStream;
import org.p2c2e.zing.types.GlkDate;
import org.p2c2e.zing.types.GlkEvent;
import org.p2c2e.zing.types.GlkTimeval;
import org.p2c2e.zing.types.InByteBuffer;
import org.p2c2e.zing.types.InOutByteBuffer;
import org.p2c2e.zing.types.InOutIntBuffer;
import org.p2c2e.zing.types.Int;
import org.p2c2e.zing.types.OutByteBuffer;
import org.p2c2e.zing.types.OutInt;
import org.p2c2e.zing.types.OutWindow;
import org.p2c2e.zing.types.StreamResult;

public abstract class AbstractGlk implements IGlk {
	public Comparator HC_COMP = new HashCodeComparator();
	public TreeMap WINDOWS;
	public TreeMap STREAMS;
	public TreeMap FILE_REFS;
	public TreeMap SOUND_CHANNELS;
	public Stream CURRENT_STREAM;
	public static LinkedList EVENT_QUEUE;
	public int TIMER = 0;
	public long TIMESTAMP;
	protected BlorbFile blorbFile;

	public MediaTracker TRACKER;
	public LinkedList IMAGE_CACHE;
	public ObjectCallback CREATE_CALLBACK;
	public ObjectCallback DESTROY_CALLBACK;
	public boolean BORDERS_ON = true;

	public static class ImageCacheNode {
		public int id;
		public Image normal;
		public Image scaled;
	}

	@Override
	public void flush() {
		try {
			Iterator it = SOUND_CHANNELS.keySet().iterator();
			while (it.hasNext())
				((SoundChannel) it.next()).stop();
			it = STREAMS.keySet().iterator();
			while (it.hasNext())
				((Stream) it.next()).close();
		} catch (Exception e) {
			System.err
					.println("problem while attempting to stop sound channel: "
							+ e);
		}
	}

	@Override
	public void reset() {
		WINDOWS = new TreeMap(HC_COMP);
		STREAMS = new TreeMap(HC_COMP);
		FILE_REFS = new TreeMap(HC_COMP);
		SOUND_CHANNELS = new TreeMap(HC_COMP);

		CURRENT_STREAM = null;
		EVENT_QUEUE = new LinkedList();
		TIMER = 0;
		TIMESTAMP = 0L;
		blorbFile = null;
		IMAGE_CACHE = new LinkedList();

		StyleHints.clearAll();
	}

	@Override
	public abstract void progress(String stJob, int min, int max, int cur);

	public BlorbFile getBlorbFile() {
		return blorbFile;
	}

	@Override
	public void setBlorbFile(BlorbFile f) {
		blorbFile = f;
		IMAGE_CACHE.clear();

	}

	@Override
	public abstract void setMorePromptCallback(ObjectCallback c);

	@Override
	public void setCreationCallback(ObjectCallback c) {
		CREATE_CALLBACK = c;
	}

	@Override
	public void setDestructionCallback(ObjectCallback c) {
		DESTROY_CALLBACK = c;
	}

	@Override
	@GlkMethod(3)
	public void tick() {

	}

	@Override
	@GlkMethod(2)
	public void setInterruptHandler(Object o) {

	}

	@Override
	@GlkMethod(1)
	public void exit() {
		System.exit(0);
	}

	private Object objIterate(SortedMap mainMap, Object o, Int rock) {
		SortedMap m;
		Object next;

		if (o == null)
			m = mainMap;
		else
			m = mainMap.tailMap(new Integer(o.hashCode() + 1));

		if (m.isEmpty()) {
			if (rock != null)
				rock.val = 0;
			return null;
		} else {
			next = m.firstKey();
			if (rock != null)
				rock.val = ((Integer) mainMap.get(next)).intValue();
			return next;
		}
	}

	@Override
	@GlkMethod(0x20)
	public IWindow windowIterate(IWindow win, OutInt rock) {
		return (IWindow) objIterate(WINDOWS, win, rock);
	}

	@Override
	@GlkMethod(0x40)
	public Stream streamIterate(Stream s, OutInt rock) {
		return (Stream) objIterate(STREAMS, s, rock);
	}

	@Override
	@GlkMethod(0x64)
	public Fileref filerefIterate(Fileref f, OutInt rock) {
		return (Fileref) objIterate(FILE_REFS, f, rock);
	}

	@Override
	@GlkMethod(0xF0)
	public SoundChannel schannelIterate(SoundChannel s, OutInt rock) {
		return (SoundChannel) objIterate(SOUND_CHANNELS, s, rock);
	}

	@Override
	@GlkMethod(0xA0)
	public char charToLower(char ch) {
		return Character.toLowerCase(ch);
	}

	@Override
	@GlkMethod(0xA1)
	public char charToUpper(char ch) {
		return Character.toUpperCase(ch);
	}

	@Override
	@GlkMethod(0x27)
	public abstract void windowGetArrangement(IWindow win, OutInt method,
			OutInt size, OutWindow key);

	@Override
	@GlkMethod(0x26)
	public abstract void windowSetArrangement(IWindow win, int method,
			int size, IWindow newKey);

	@Override
	@GlkMethod(0x25)
	public abstract void windowGetSize(IWindow win, OutInt b1, OutInt b2);

	@Override
	@GlkMethod(0x30)
	public abstract IWindow windowGetSibling(IWindow win);

	@Override
	@GlkMethod(0x29)
	public abstract IWindow windowGetParent(IWindow win);

	@Override
	@GlkMethod(0x28)
	public abstract int windowGetType(IWindow win);

	@Override
	@GlkMethod(0x21)
	public abstract int windowGetRock(IWindow w);

	@Override
	@GlkMethod(0x2A)
	public abstract void windowClear(IWindow win);

	@Override
	@GlkMethod(0x23)
	public abstract IWindow windowOpen(IWindow w, int method, int size,
			int wintype, int rock);

	@Override
	@GlkMethod(0x24)
	public abstract void windowClose(IWindow w, StreamResult streamresult);

	@Override
	@GlkMethod(0x2D)
	public abstract void windowSetEchoStream(IWindow win, Stream s);

	@Override
	@GlkMethod(0x2E)
	public abstract Stream windowGetEchoStream(IWindow win);

	@Override
	@GlkMethod(0x2C)
	public abstract Stream windowGetStream(IWindow win);

	@Override
	@GlkMethod(0x2F)
	public void setWindow(IWindow win) {
		CURRENT_STREAM = (win == null) ? null : win.getStream();
	}

	@Override
	@GlkMethod(0x47)
	public void streamSetCurrent(Stream s) {
		if (s == null || s.canWrite())
			CURRENT_STREAM = s;
	}

	@Override
	@GlkMethod(0x48)
	public Stream streamGetCurrent() {
		return CURRENT_STREAM;
	}

	@Override
	@GlkMethod(0x80)
	public void putChar(char ch) {
		if (CURRENT_STREAM == null)
			nullRef("Glk.putChar");
		else
			CURRENT_STREAM.putChar(ch);
	}

	@Override
	@GlkMethod(0x128)
	public void putCharUni(int ch) {
		if (CURRENT_STREAM == null)
			nullRef("Glk.putCharUni");
		else
			CURRENT_STREAM.putCharUni(ch);
	}

	@Override
	@GlkMethod(0x82)
	public void putString(String s) {
		if (CURRENT_STREAM == null)
			nullRef("Glk.putString");
		else
			CURRENT_STREAM.putString(s);
	}

	@Override
	@GlkMethod(0x129)
	public void putStringUni(String s) {
		if (CURRENT_STREAM == null)
			nullRef("Glk.putStringUni");
		else
			CURRENT_STREAM.putStringUni(s);
	}

	@Override
	@GlkMethod(0x84)
	public void putBuffer(InByteBuffer b, int len) {
		if (CURRENT_STREAM == null)
			nullRef("Glk.putBuffer");
		else
			CURRENT_STREAM.putBuffer(b.buffer, len);
	}

	@Override
	@GlkMethod(0x12A)
	public void putBufferUni(InByteBuffer b, int len) {
		if (CURRENT_STREAM == null)
			nullRef("Glk.putBufferUni");
		else
			CURRENT_STREAM.putBufferUni(b.buffer, len);
	}

	@Override
	@GlkMethod(0x81)
	public void putCharStream(Stream s, int ch) {
		if (s == null)
			nullRef("Glk.putCharStream");
		else
			s.putChar(ch);
	}

	@Override
	@GlkMethod(0x12B)
	public void putCharStreamUni(Stream s, int ch) {
		if (s == null)
			nullRef("Glk.putCharStreamUni");
		else
			s.putCharUni(ch);
	}

	@Override
	@GlkMethod(0x83)
	public void putStringStream(Stream stm, String s) {
		if (stm == null)
			nullRef("Glk.putStringStream");
		else
			stm.putString(s);
	}

	@Override
	@GlkMethod(0x12C)
	public void putStringStreamUni(Stream stm, String s) {
		if (stm == null)
			nullRef("Glk.putStringStreamUni");
		else
			stm.putStringUni(s);
	}

	@Override
	@GlkMethod(0x85)
	public void putBufferStream(Stream s, InByteBuffer b, int len) {
		if (s == null)
			nullRef("Glk.putBufferStream");
		else
			s.putBuffer(b.buffer, len);
	}

	@Override
	@GlkMethod(0x12D)
	public void putBufferStreamUni(Stream s, InByteBuffer b, int len) {
		if (s == null)
			nullRef("Glk.putBufferStreamUni");
		else
			s.putBufferUni(b.buffer, len);
	}

	@Override
	@GlkMethod(0x120)
	public int bufferToLowerCaseUni(InOutByteBuffer buf, int len, int numchars) {
		// TODO Implement glk_buffer_to_lower_case_uni
		return 0;
	}

	@Override
	@GlkMethod(0x121)
	public int bufferToUpperCaseUni(InOutByteBuffer buf, int len, int numchars) {
		// TODO implement glk_buffer_to_upper_case_uni
		return 0;
	}

	@Override
	@GlkMethod(0x122)
	public int bufferToTitleCaseUni(InOutByteBuffer buf, int len, int numchars) {
		// TODO Implement glk_buffer_to_title_case_uni
		return 0;
	}

	@Override
	@GlkMethod(0x90)
	public int getCharStream(Stream s) {
		if (s != null)
			return s.getChar();

		nullRef("Glk.getCharStream");
		return -1;
	}

	@Override
	@GlkMethod(0x130)
	public int getCharStreamUni(Stream s) {
		if (s != null)
			return s.getCharUni();

		nullRef("Glk.getCharStreamUni");
		return -1;
	}

	@Override
	@GlkMethod(0x92)
	public int getBufferStream(Stream s, OutByteBuffer b, int len) {
		if (s != null)
			return s.getBuffer(b.buffer, len);

		nullRef("Glk.getBufferStream");
		return -1;
	}

	@Override
	@GlkMethod(0x131)
	public int getBufferStreamUni(Stream s, OutByteBuffer b, int len) {
		if (s != null)
			return s.getBufferUni(b.buffer, len);

		nullRef("Glk.getBufferStreamUni");
		return -1;
	}

	@Override
	@GlkMethod(0x91)
	public int getLineStream(Stream s, OutByteBuffer b, int len) {
		if (s != null)
			return s.getLine(b.buffer, len);

		nullRef("Glk.getLineStream");
		return -1;
	}

	@Override
	@GlkMethod(0x132)
	public int getLineStreamUni(Stream s, OutByteBuffer b, int len) {
		if (s != null)
			return s.getLineUni(b.buffer, len);

		nullRef("Glk.getLineStreamUni");
		return -1;
	}

	@Override
	@GlkMethod(0x44)
	public void streamClose(Stream s, StreamResult b) {
		if (s == null) {
			nullRef("Glk.streamClose");
			return;
		}

		StreamResult res = s.close();
		if (b != null) {
			b.readcount = res.readcount;
			b.writecount = res.writecount;
		}

		STREAMS.remove(s);
		if (DESTROY_CALLBACK != null)
			DESTROY_CALLBACK.callback(s);
	}

	@Override
	@GlkMethod(0x46)
	public int streamGetPosition(Stream s) {
		if (s == null) {
			nullRef("Glk.streamGetPosition");
			return -1;
		}

		return s.getPosition();
	}

	@Override
	@GlkMethod(0x45)
	public void streamSetPosition(Stream s, int pos, int seekmode) {
		if (s == null) {
			nullRef("Glk.streamSetPosition");
			return;
		}

		s.setPosition(pos, seekmode);
	}

	@Override
	@GlkMethod(0x43)
	public Stream streamOpenMemory(InOutByteBuffer b, int len, int mode,
			int rock) {
		Stream s = new MemoryStream(b.buffer, len, mode);

		STREAMS.put(s, new Integer(rock));
		if (CREATE_CALLBACK != null)
			CREATE_CALLBACK.callback(s);
		return s;
	}

	@Override
	@GlkMethod(0x139)
	public Stream streamOpenMemoryUni(InOutByteBuffer b, int len, int mode,
			int rock) {
		Stream s = new UnicodeMemoryStream(b.buffer, len, mode);

		STREAMS.put(s, new Integer(rock));
		if (CREATE_CALLBACK != null)
			CREATE_CALLBACK.callback(s);
		return s;
	}

	@Override
	@GlkMethod(0x42)
	public Stream streamOpenFile(Fileref ref, int mode, int rock) {
		if (ref == null) {
			nullRef("Glk.streamOpenFile");
			return null;
		}

		Stream s = new FileStream(ref, mode, false);

		STREAMS.put(s, new Integer(rock));
		if (CREATE_CALLBACK != null)
			CREATE_CALLBACK.callback(s);
		return s;
	}

	@Override
	@GlkMethod(0x138)
	public Stream streamOpenFileUni(Fileref ref, int mode, int rock) {
		if (ref == null) {
			nullRef("Glk.streamOpenFileUni");
			return null;
		}

		Stream s = new FileStream(ref, mode, true);

		STREAMS.put(s, new Integer(rock));
		if (CREATE_CALLBACK != null)
			CREATE_CALLBACK.callback(s);
		return s;
	}

	@Override
	@GlkMethod(0x41)
	public int streamGetRock(Stream s) {
		if (s == null) {
			nullRef("Glk.streamGetRock");
			return 0;
		}

		return ((Integer) STREAMS.get(s)).intValue();
	}

	@Override
	@GlkMethod(0x87)
	public void setStyleStream(Stream s, int style) {
		if (s == null) {
			nullRef("Glk.setStyleStream");
			return;
		}

		if (style < STYLE_NUMSTYLES)
			s.setStyle(STYLES[style]);
		else
			s.setStyle(STYLES[STYLE_NORMAL]);
	}

	@Override
	@GlkMethod(0x86)
	public void setStyle(int style) {
		if (CURRENT_STREAM != null)
			setStyleStream(CURRENT_STREAM, style);
	}

	@Override
	@GlkMethod(0xB0)
	public void stylehintSet(int wintype, int style, int hint, int val) {
		StyleHints.setHint(wintype, Style.getStyle(STYLES[style], wintype),
				hint, val);
	}

	@Override
	@GlkMethod(0xB1)
	public void stylehintClear(int wintype, int style, int hint) {
		StyleHints.clearHint(wintype, Style.getStyle(STYLES[style], wintype),
				hint);
	}

	@Override
	@GlkMethod(0xB2)
	public boolean styleDistinguish(IWindow win, int s1, int s2) {
		if (win == null) {
			nullRef("Glk.styleDistinguish");
			return false;
		}

		Style first = (Style) win.getHintedStyles().get(STYLES[s1]);
		Style second = (Style) win.getHintedStyles().get(STYLES[s2]);

		if (!first.family.equals(second.family))
			return true;
		if ((first.isOblique || second.isOblique)
				&& !(first.isOblique && second.isOblique))
			return true;
		if (first.size != second.size)
			return true;
		if (!first.weight.equals(second.weight))
			return true;
		if (first.leftIndent != second.leftIndent)
			return true;
		if (first.rightIndent != second.rightIndent)
			return true;
		if (first.parIndent != second.parIndent)
			return true;
		if (first.justification != second.justification)
			return true;
		if (!first.textColor.equals(second.textColor))
			return true;
		if (!first.backColor.equals(second.backColor))
			return true;

		return false;
	}

	@Override
	@GlkMethod(0xB3)
	public boolean styleMeasure(IWindow win, int style, int hint, OutInt result) {
		if (win == null) {
			nullRef("Glk.styleMeasure");
			return false;
		}

		return win.measureStyle(STYLES[style], hint, result);
	}

	@Override
	@GlkMethod(0x60)
	public Fileref filerefCreateTemp(int usage, int rock) {
		try {
			Fileref ref = Fileref.createTemp(usage);
			if (ref != null) {
				FILE_REFS.put(ref, new Integer(rock));
				if (CREATE_CALLBACK != null)
					CREATE_CALLBACK.callback(ref);
				return ref;
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	@GlkMethod(0x61)
	public Fileref filerefCreateByName(int usage, String name, int rock) {
		Fileref ref = Fileref.createByName(usage, name);
		if (ref != null) {
			FILE_REFS.put(ref, new Integer(rock));
			if (CREATE_CALLBACK != null)
				CREATE_CALLBACK.callback(ref);
			return ref;
		} else {
			return null;
		}
	}

	@Override
	@GlkMethod(0x62)
	public abstract Fileref filerefCreateByPrompt(int usage, int fmode, int rock);

	@Override
	@GlkMethod(0x68)
	public Fileref filerefCreateFromFileref(int usage, Fileref r, int rock) {
		if (r == null) {
			nullRef("Glk.filerefCreateFromFileref");
			return null;
		}

		Fileref ref = Fileref.createFromFileref(usage, r);
		FILE_REFS.put(ref, new Integer(rock));
		if (CREATE_CALLBACK != null)
			CREATE_CALLBACK.callback(ref);
		return ref;
	}

	@Override
	@GlkMethod(0x63)
	public void filerefDestroy(Fileref ref) {
		if (ref == null) {
			nullRef("Glk.filerefDestroy");
			return;
		}

		ref.destroy();

		FILE_REFS.remove(ref);
		if (DESTROY_CALLBACK != null)
			DESTROY_CALLBACK.callback(ref);
	}

	@Override
	@GlkMethod(0x66)
	public void filerefDeleteFile(Fileref ref) {
		if (ref == null) {
			nullRef("Glk.filerefDeleteFile");
			return;
		}

		Fileref.deleteFile(ref);
	}

	@Override
	@GlkMethod(0x67)
	public boolean filerefDoesFileExist(Fileref ref) {
		if (ref == null) {
			nullRef("Glk.filerefDoesFileExist");
			return false;
		}

		return ref.fileExists();
	}

	@Override
	@GlkMethod(0x65)
	public int filerefGetRock(Fileref ref) {
		if (ref == null) {
			nullRef("Glk.filerefGetRock");
			return 0;
		}

		return ((Integer) FILE_REFS.get(ref)).intValue();
	}

	@Override
	@GlkMethod(0xF1)
	public int schannelGetRock(SoundChannel c) {
		if (c == null) {
			nullRef("Glk.schannelGetRock");
			return 0;
		}

		return ((Integer) SOUND_CHANNELS.get(c)).intValue();
	}

	@Override
	@GlkMethod(0xF2)
	public SoundChannel schannelCreate(int rock) {
		SoundChannel c = new SoundChannel(this);
		SOUND_CHANNELS.put(c, new Integer(rock));
		if (CREATE_CALLBACK != null)
			CREATE_CALLBACK.callback(c);

		return c;
	}

	@Override
	@GlkMethod(0xF3)
	public void schannelDestroy(SoundChannel c) {
		if (c == null) {
			nullRef("Glk.schannelDestroy");
			return;
		}

		try {
			c.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}

		SOUND_CHANNELS.remove(c);
		if (DESTROY_CALLBACK != null)
			DESTROY_CALLBACK.callback(c);
	}

	@Override
	@GlkMethod(0xF9)
	public boolean schannelPlayExt(SoundChannel c, int soundId, int repeat,
			int notify) {
		if (c == null) {
			nullRef("Glk.schannelPlayExt");
			return false;
		}

		try {
			return c.play(blorbFile, soundId, repeat, notify);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	@GlkMethod(0xF8)
	public boolean schannelPlay(SoundChannel c, int soundId) {
		return schannelPlayExt(c, soundId, 1, 0);
	}

	@Override
	@GlkMethod(0xFA)
	public void schannelStop(SoundChannel c) {
		if (c == null) {
			nullRef("Glk.schannelStop");
			return;
		}

		try {
			c.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@GlkMethod(0xFB)
	public void schannelSetVolume(SoundChannel c, int vol) {
		if (c == null)
			nullRef("Glk.schannelSetVolume");
		else
			c.setVolume(vol);
	}

	@Override
	@GlkMethod(0xFC)
	public void soundLoadHint(int soundId, int val) {
		// TODO Implement preloading of sounds.
	}

	@Override
	@GlkMethod(4)
	public int gestalt(int sel, int val) {
		return gestaltExt(sel, val, null, 0);
	}

	@Override
	@GlkMethod(5)
	public int gestaltExt(int sel, int val, InOutIntBuffer arr, int len) {
		switch (sel) {
		case GESTALT_VERSION:
			return 0x00000601;
		case GESTALT_CHAR_OUTPUT:
			if (val == 10 || (val >= 32 && val < 127)
					|| (val >= 160 && val < 256)) {
				if (arr != null && len > 0)
					arr.buffer.put(1);
				return GESTALT_CHAR_OUTPUT_EXACT_PRINT;
			} else {
				if (arr != null && len > 0)
					arr.buffer.put(0);
				return GESTALT_CHAR_OUTPUT_CANNOT_PRINT;
			}
		case GESTALT_LINE_INPUT:
			if ((val >= 32 && val < 127) || (val >= 160 && val < 256))
				return 1;
			else
				return 0;
		case GESTALT_CHAR_INPUT:
			if (val >= 0 && val < 256)
				return 1;
			if (val == KEYCODE_LEFT || val == KEYCODE_RIGHT
					|| val == KEYCODE_UP || val == KEYCODE_DOWN)
				return 1;
			return 0;
		case GESTALT_MOUSE_INPUT:
			if (val == WINTYPE_TEXT_GRID || val == WINTYPE_GRAPHICS)
				return 1;
			else
				return 0;
		case GESTALT_TIMER:
			return 1;
		case GESTALT_GRAPHICS:
			return 1;
		case GESTALT_DRAW_IMAGE:
			if (val == WINTYPE_TEXT_BUFFER || val == WINTYPE_GRAPHICS)
				return 1;
			else
				return 0;
		case GESTALT_GRAPHICS_TRANSPARENCY:
			return 1;
		case GESTALT_SOUND:
			return 1;
		case GESTALT_SOUND_MUSIC:
			return 1;
		case GESTALT_SOUND_VOLUME:
			return 1;
		case GESTALT_SOUND_NOTIFY:
			return 1;
		case GESTALT_HYPERLINKS:
			return 1;
		case GESTALT_UNICODE:
			return 1;
		case GESTALT_UNICODENORM:
			return 1;
		case GESTALT_DATETIME:
			return 1;
		default:
			return 0;
		}
	}

	@Override
	@GlkMethod(0xD2)
	public void requestCharEvent(IWindow win) {
		if (win == null)
			nullRef("Glk.requestCharEvent");
		else
			win.requestCharacterInput(new GlkCharConsumer(this, win));
	}

	@Override
	@GlkMethod(0x140)
	public void requestCharEventUni(IWindow win) {
		if (win == null)
			nullRef("Glk.requestCharEvent");
		else
			win.requestCharacterInput(new GlkCharConsumer(this, win));
	}

	@Override
	@GlkMethod(0xD3)
	public void cancelCharEvent(IWindow win) {
		if (win == null)
			nullRef("Glk.cancelCharEvent");
		else
			win.cancelCharacterInput();
	}

	@Override
	@GlkMethod(0xD0)
	public void requestLineEvent(IWindow win, InOutByteBuffer b, int maxlen,
			int initlen) {
		if (win == null) {
			nullRef("Glk.requestLineEvent");
			return;
		}

		StringBuffer sb;
		String s = null;
		if (initlen > 0) {
			sb = new StringBuffer();
			for (int i = 0; i < initlen; i++)
				sb.append((char) b.buffer.get(i));
			s = sb.toString();
		}
		win.requestLineInput(new GlkLineConsumer(this, win, b.buffer, false),
				s, maxlen);
	}

	@Override
	@GlkMethod(0x141)
	public void requestLineEventUni(IWindow win, InOutByteBuffer b, int maxlen,
			int initlen) {
		if (win == null) {
			nullRef("Glk.requestLineEventUni");
			return;
		}

		StringBuffer sb;
		String s = null;
		if (initlen > 0) {
			sb = new StringBuffer();
			for (int i = 0; i < initlen; i++) {
				int t = b.buffer.getInt(i * 4);
				sb.appendCodePoint(t);
			}
			s = sb.toString();
		}
		win.requestLineInput(new GlkLineConsumer(this, win, b.buffer, true), s,
				maxlen);
	}

	@Override
	@GlkMethod(0xD1)
	public void cancelLineEvent(IWindow win, GlkEvent e) {
		if (win == null) {
			nullRef("Glk.cancelLineEvent");
			return;
		}

		String s = win.cancelLineInput();

		if (e != null) {
			e.type = EVTYPE_LINE_INPUT;
			e.win = win;
			e.val1 = s.length();
			e.val2 = 0;
		}
	}

	@Override
	@GlkMethod(0xD4)
	public void requestMouseEvent(IWindow win) {
		if (win == null)
			nullRef("Glk.requestMouseEvent");
		else
			win.requestMouseInput(new GlkMouseConsumer(this, win));
	}

	@Override
	@GlkMethod(0xD5)
	public void cancelMouseEvent(IWindow win) {
		if (win == null)
			nullRef("Glk.cancelMouseEvent");
		else
			win.cancelMouseInput();
	}

	@Override
	@GlkMethod(0xD6)
	public void requestTimerEvents(int delta) {
		TIMESTAMP = System.currentTimeMillis();
		TIMER = delta;
	}

	@Override
	@GlkMethod(0xE1)
	public abstract boolean imageDraw(IWindow win, int imgid, int val1, int val2);

	@Override
	@GlkMethod(0xE2)
	public abstract boolean imageDrawScaled(IWindow win, int imgid, int val1,
			int val2, int width, int height);

	@Override
	@GlkMethod(0xE0)
	public abstract boolean imageGetInfo(int imgid, OutInt width, OutInt height);

	@Override
	@GlkMethod(0xEB)
	public void windowSetBackgroundColor(IWindow win, Color c) {
		if (win instanceof IGraphicsWindow)
			((IGraphicsWindow) win).setBackgroundColor(c);
	}

	@Override
	@GlkMethod(0xEA)
	public void windowFillRect(IWindow win, Color c, int left, int top,
			int width, int height) {
		if (win instanceof IGraphicsWindow)
			((IGraphicsWindow) win).fillRect(c, left, top, width, height);
	}

	@Override
	@GlkMethod(0xE9)
	public void windowEraseRect(IWindow win, int left, int top, int width,
			int height) {
		if (win instanceof IGraphicsWindow)
			((IGraphicsWindow) win).eraseRect(left, top, width, height);
	}

	@Override
	@GlkMethod(0xE8)
	public void windowFlowBreak(IWindow win) {
		if (win instanceof ITextBufferWindow)
			((ITextBufferWindow) win).flowBreak();
	}

	@Override
	@GlkMethod(0x2B)
	public void windowMoveCursor(IWindow win, int x, int y) {
		if (win == null)
			nullRef("Glk.windowMoveCursor");
		else
			((ITextGridWindow) win).setCursor(x, y);
	}

	@Override
	@GlkMethod(0x100)
	public void setHyperlink(int val) {
		setHyperlinkStream(CURRENT_STREAM, val);
	}

	@Override
	@GlkMethod(0x101)
	public void setHyperlinkStream(Stream s, int val) {
		if (s == null)
			nullRef("Glk.setHyperlinkStream");
		else
			s.setHyperlink(val);
	}

	@Override
	@GlkMethod(0x102)
	public void requestHyperlinkEvent(IWindow w) {
		w.requestHyperlinkInput(new GlkHyperConsumer(this, w));
	}

	@Override
	@GlkMethod(0x103)
	public void cancelHyperlinkEvent(IWindow w) {
		w.cancelHyperlinkInput();
	}

	@Override
	public void addEvent(GlkEvent e) {
		synchronized (EVENT_QUEUE) {
			EVENT_QUEUE.addLast(e);
			EVENT_QUEUE.notifyAll();
		}
	}

	@Override
	@GlkMethod(0x160)
	public void getCurrentTime(GlkTimeval timeval) {
		long t = System.currentTimeMillis();
		long ut = t / 1000L;

		timeval.setOut(true);
		timeval.setHighsec((int) (ut >> 32));
		timeval.setLowsec((int) ut);
		timeval.setMicrosec((int) (t & 1000));
	}

	@Override
	@GlkMethod(0x161)
	public int getCurrentSimpleTime(int factor) {
		return Math.round(System.currentTimeMillis() / 1000L / factor);
	}

	@Override
	@GlkMethod(0x168)
	public void convertTimeToDateUtc(GlkTimeval time, GlkDate date) {
		DateTime dt = new DateTime(time.getUnixTime() * 1000L)
				.toDateTime(DateTimeZone.UTC);
		date.setOut(true);
		date.setYear(dt.getYear());
		date.setMonth(dt.getMonthOfYear());
		date.setDay(dt.getDayOfMonth());
		date.setWeekday(dt.getDayOfWeek());
		date.setHour(dt.getHourOfDay());
		date.setMinute(dt.getMinuteOfHour());
		date.setSecond(dt.getSecondOfMinute());
		date.setMicrosec(time.getMicrosec());
	}

	@Override
	@GlkMethod(0x169)
	public void convertTimeToDateLocal(GlkTimeval time, GlkDate date) {
		DateTime udt = new DateTime(time.getUnixTime() * 1000L);
		date.setOut(true);
		LocalDateTime dt = udt.toLocalDateTime();
		date.setYear(dt.getYear());
		date.setMonth(dt.getMonthOfYear());
		date.setDay(dt.getDayOfMonth());
		date.setWeekday(dt.getDayOfWeek());
		date.setHour(dt.getHourOfDay());
		date.setMinute(dt.getMinuteOfHour());
		date.setSecond(dt.getSecondOfMinute());
		date.setMicrosec(time.getMicrosec());
	}

	@Override
	@GlkMethod(0x16B)
	public void convertSimpleTimeToDateLocal(int time, int factor, GlkDate date) {
		DateTime udt = new DateTime(time * 1000L * factor);
		date.setOut(true);
		LocalDateTime dt = udt.toLocalDateTime();
		date.setYear(dt.getYear());
		date.setMonth(dt.getMonthOfYear());
		date.setDay(dt.getDayOfMonth());
		date.setWeekday(dt.getDayOfWeek());
		date.setHour(dt.getHourOfDay());
		date.setMinute(dt.getMinuteOfHour());
		date.setSecond(dt.getSecondOfMinute());
		date.setMicrosec(0);
	}

	@Override
	@GlkMethod(0x16A)
	public void convertSimpleTimeToDateUtc(int time, int factor, GlkDate date) {
		DateTime udt = new DateTime(time * 1000L * factor)
				.toDateTime(DateTimeZone.UTC);
		date.setOut(true);
		LocalDateTime dt = udt.toLocalDateTime();
		date.setYear(dt.getYear());
		date.setMonth(dt.getMonthOfYear());
		date.setDay(dt.getDayOfMonth());
		date.setWeekday(dt.getDayOfWeek());
		date.setHour(dt.getHourOfDay());
		date.setMinute(dt.getMinuteOfHour());
		date.setSecond(dt.getSecondOfMinute());
		date.setMicrosec(0);
	}

	@Override
	@GlkMethod(0x16C)
	public void convertDateToTimeUtc(GlkDate date, GlkTimeval time) {
		DateTime udt = new DateTime(date.getYear(), date.getMonth(),
				date.getDay(), date.getHour(), date.getMinute(),
				date.getSecond(), DateTimeZone.UTC);
		long t = udt.getMillis() / 1000L;
		time.setOut(true);
		time.setHighsec((int) (t >> 32));
		time.setLowsec((int) t);
		time.setMicrosec(0);
	}

	@Override
	@GlkMethod(0x16D)
	public void convertDateToTimeLocal(GlkDate date, GlkTimeval time) {
		DateTime udt = new DateTime(date.getYear(), date.getMonth(),
				date.getDay(), date.getHour(), date.getMinute(),
				date.getSecond());
		long t = udt.getMillis() / 1000;
		time.setOut(true);
		time.setHighsec((int) (t >> 32));
		time.setLowsec((int) t);
		time.setMicrosec(0);
	}

	@Override
	@GlkMethod(0x16E)
	public int convertDateToSimpleTimeUtc(GlkDate date, int factor) {
		DateTime udt = new DateTime(date.getYear(), date.getMonth(),
				date.getDay(), date.getHour(), date.getMinute(),
				date.getSecond(), DateTimeZone.UTC);
		return Math.round(udt.getMillis() / 1000L / factor);
	}

	@Override
	@GlkMethod(0x16F)
	public int convertDateToSimpleTimeLocal(GlkDate date, int factor) {
		DateTime udt = new DateTime(date.getYear(), date.getMonth(),
				date.getDay(), date.getHour(), date.getMinute(),
				date.getSecond());
		return Math.round(udt.getMillis() / 1000L / factor);
	}

	@Override
	public abstract Image getImage(int id, int xscale, int yscale);

	@Override
	public int colorToInt(Color c) {
		return ((c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue());
	}

	@Override
	public Color intToColor(int i) {
		int iRed = (i >>> 16) & 0xff;
		int iGreen = (i >>> 8) & 0xff;
		int iBlue = i & 0xff;
		return new Color(iRed, iGreen, iBlue);
	}

	@Override
	public abstract void nullRef(String func);

	@Override
	@GlkMethod(0x22)
	public abstract IWindow windowGetRoot();

	@Override
	@GlkMethod(0xC0)
	public void select(GlkEvent e) {
		long cur = 0l;
		GlkEvent ev = null;
		boolean done = false;

		synchronized (EVENT_QUEUE) {
			while (!done) {
				if (!EVENT_QUEUE.isEmpty()) {
					ev = (GlkEvent) EVENT_QUEUE.removeFirst();
					if (ev != null)
						done = true;
				} else if (TIMER > 0
						&& (cur = System.currentTimeMillis()) - TIMESTAMP >= TIMER) {
					e.type = EVTYPE_TIMER;
					e.win = null;
					e.val1 = 0;
					e.val2 = 0;
					TIMESTAMP = cur;
					done = true;
				} else {
					try {
						if (TIMER > 0)
							EVENT_QUEUE.wait(TIMER - (cur - TIMESTAMP));
						else
							EVENT_QUEUE.wait();
					} catch (InterruptedException ex) {
					}
				}
			}
			if (ev != null) {
				e.type = ev.type;
				e.win = ev.win;
				e.val1 = ev.val1;
				e.val2 = ev.val2;
			}
		}
	}

	@Override
	@GlkMethod(0xC1)
	public void selectPoll(GlkEvent e) {
		long cur;
		GlkEvent ev = null;
		ListIterator li;

		synchronized (EVENT_QUEUE) {
			li = EVENT_QUEUE.listIterator();
			while (li.hasNext()) {
				ev = (GlkEvent) li.next();
				if (ev.type == EVTYPE_TIMER || ev.type == EVTYPE_ARRANGE
						|| ev.type == EVTYPE_SOUND_NOTIFY) {
					li.remove();
					e.type = ev.type;
					e.win = ev.win;
					e.val1 = ev.val1;
					e.val2 = ev.val2;
					break;
				}
			}
			if (TIMER > 0) {
				cur = System.currentTimeMillis();
				if ((cur - TIMESTAMP) >= TIMER) {
					e.type = EVTYPE_TIMER;
					e.win = null;
					e.val1 = 0;
					e.val2 = 0;
					TIMESTAMP = cur;
					return;
				}
			}
			e.type = EVTYPE_NONE;
		}
	}

	@Override
	@GlkMethod(0x123)
	public int decomposeBufferCanon(InOutByteBuffer buf, int len, int numchars) {
		String s = convertBufferToString(buf, numchars);

		if (s == null)
			return 0;

		String s1 = Normalizer.normalize(s, Normalizer.Form.NFD);
		writeStringToBuffer(buf, s1);
		return s1.length();
	}

	@Override
	@GlkMethod(0x124)
	public int normalizeBufferCanon(InOutByteBuffer buf, int len, int numchars) {
		String s = convertBufferToString(buf, numchars);

		if (s == null)
			return 0;

		String s1 = Normalizer.normalize(s, Normalizer.Form.NFC);
		writeStringToBuffer(buf, s1);
		return s1.length();
	}

	private void writeStringToBuffer(InOutByteBuffer buf, String s) {
		buf.buffer.clear();

		for (int i = 0; i < s.length(); i++) {
			buf.buffer.putInt(s.codePointAt(i));
		}
	}

	private String convertBufferToString(InOutByteBuffer buf, int numchars) {
		String s = null;
		if (numchars > 0) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < numchars; i++) {
				int t = buf.buffer.getInt(i * 4);
				sb.appendCodePoint(t);
			}
			s = sb.toString();
		}
		return s;
	}

	protected static final class HashCodeComparator implements Comparator {
		@Override
		public int compare(Object o1, Object o2) {
			return o1.hashCode() - o2.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o == this;
		}
	}

	protected static class GlkHyperConsumer implements HyperlinkInputConsumer {
		private IGlk glk;
		private IWindow w;

		GlkHyperConsumer(IGlk glk, IWindow win) {
			this.glk = glk;
			w = win;
		}

		@Override
		public void consume(int val) {
			GlkEvent e = new GlkEvent();
			e.type = EVTYPE_HYPERLINK;
			e.win = w;
			e.val1 = val;
			e.val2 = 0;

			glk.addEvent(e);
		}
	}

	protected static class GlkCharConsumer implements CharInputConsumer {
		private IGlk glk;
		private IWindow w;

		GlkCharConsumer(IGlk glk, IWindow win) {
			this.glk = glk;
			w = win;
		}

		@Override
		public void consume(java.awt.event.KeyEvent e) {
			GlkEvent ev = new GlkEvent();
			ev.type = EVTYPE_CHAR_INPUT;
			ev.win = w;
			ev.val1 = e.getKeyChar();
			ev.val2 = 0;

			switch (ev.val1) {
			case 9:
				ev.val1 = KEYCODE_TAB;
				break;
			case 10:
			case 13:
				ev.val1 = KEYCODE_RETURN;
				break;
			case 27:
				ev.val1 = KEYCODE_ESCAPE;
				break;
			case 127:
				ev.val1 = KEYCODE_DELETE;
				break;
			case KeyEvent.CHAR_UNDEFINED:
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					ev.val1 = KEYCODE_LEFT;
					break;
				case KeyEvent.VK_RIGHT:
					ev.val1 = KEYCODE_RIGHT;
					break;
				case KeyEvent.VK_UP:
					ev.val1 = KEYCODE_UP;
					break;
				case KeyEvent.VK_DOWN:
					ev.val1 = KEYCODE_DOWN;
					break;
				case KeyEvent.VK_ENTER:
					ev.val1 = KEYCODE_RETURN;
					break;
				case KeyEvent.VK_DELETE:
					ev.val1 = KEYCODE_DELETE;
					break;
				case KeyEvent.VK_ESCAPE:
					ev.val1 = KEYCODE_ESCAPE;
					break;
				case KeyEvent.VK_TAB:
					ev.val1 = KEYCODE_TAB;
					break;
				case KeyEvent.VK_PAGE_UP:
					ev.val1 = KEYCODE_PAGE_UP;
					break;
				case KeyEvent.VK_PAGE_DOWN:
					ev.val1 = KEYCODE_PAGE_DOWN;
					break;
				case KeyEvent.VK_HOME:
					ev.val1 = KEYCODE_HOME;
					break;
				case KeyEvent.VK_END:
					ev.val1 = KEYCODE_END;
					break;
				case KeyEvent.VK_F1:
					ev.val1 = KEYCODE_FUNC1;
					break;
				case KeyEvent.VK_F2:
					ev.val1 = KEYCODE_FUNC2;
					break;
				case KeyEvent.VK_F3:
					ev.val1 = KEYCODE_FUNC3;
					break;
				case KeyEvent.VK_F4:
					ev.val1 = KEYCODE_FUNC4;
					break;
				case KeyEvent.VK_F5:
					ev.val1 = KEYCODE_FUNC5;
					break;
				case KeyEvent.VK_F6:
					ev.val1 = KEYCODE_FUNC6;
					break;
				case KeyEvent.VK_F7:
					ev.val1 = KEYCODE_FUNC7;
					break;
				case KeyEvent.VK_F8:
					ev.val1 = KEYCODE_FUNC8;
					break;
				case KeyEvent.VK_F9:
					ev.val1 = KEYCODE_FUNC9;
					break;
				case KeyEvent.VK_F10:
					ev.val1 = KEYCODE_FUNC10;
					break;
				case KeyEvent.VK_F11:
					ev.val1 = KEYCODE_FUNC11;
					break;
				case KeyEvent.VK_F12:
					ev.val1 = KEYCODE_FUNC12;
					break;
				default:
					ev.val1 = KEYCODE_UNKNOWN;
				}
				break;
			default:
			}

			glk.addEvent(ev);
		}
	}

	protected static class GlkLineConsumer implements LineInputConsumer {
		private IGlk glk;
		IWindow w;
		ByteBuffer b;
		boolean unicode;

		GlkLineConsumer(IGlk glk, IWindow win, ByteBuffer buf, boolean unicode) {
			this.glk = glk;
			w = win;
			b = buf;
			this.unicode = unicode;
		}

		@Override
		public void consume(String s) {
			GlkEvent ev = new GlkEvent();
			cancel(s);
			ev.type = EVTYPE_LINE_INPUT;
			ev.win = w;
			ev.val1 = s.length();
			ev.val2 = 0;
			glk.addEvent(ev);
		}

		@Override
		public void cancel(String s) {
			int l = s.length();
			if (unicode) {
				// fixme does not handle astral plane
				for (int i = 0; i < l; i++)
					b.putInt(i * 4, s.charAt(i));
			} else {
				for (int i = 0; i < l; i++)
					b.put(i, (byte) s.charAt(i));
			}
		}
	}

	protected static class GlkMouseConsumer implements MouseInputConsumer {
		IGlk glk;
		IWindow w;

		GlkMouseConsumer(IGlk glk, IWindow win) {
			this.glk = glk;
			w = win;
		}

		@Override
		public void consume(int x, int y) {
			GlkEvent e = new GlkEvent();
			e.type = EVTYPE_MOUSE_INPUT;
			e.win = w;
			e.val1 = x;
			e.val2 = y;
			glk.addEvent(e);
		}
	}
}
