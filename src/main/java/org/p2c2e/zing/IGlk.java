package org.p2c2e.zing;

import java.awt.Color;
import java.awt.Image;

import org.p2c2e.blorb.BlorbFile;
import org.p2c2e.zing.types.GlkDate;
import org.p2c2e.zing.types.GlkEvent;
import org.p2c2e.zing.types.GlkTimeval;
import org.p2c2e.zing.types.InByteBuffer;
import org.p2c2e.zing.types.InOutByteBuffer;
import org.p2c2e.zing.types.InOutIntBuffer;
import org.p2c2e.zing.types.OutByteBuffer;
import org.p2c2e.zing.types.OutInt;
import org.p2c2e.zing.types.OutWindow;
import org.p2c2e.zing.types.StreamResult;

public interface IGlk {

	public void nullRef(String func);

	public Color intToColor(int i);

	public int colorToInt(Color c);

	public Image getImage(int id, int xscale, int yscale);

	public void selectPoll(GlkEvent e);

	public void cancelHyperlinkEvent(IWindow w);

	public void requestHyperlinkEvent(IWindow w);

	public void setHyperlinkStream(Stream s, int val);

	public void setHyperlink(int val);

	public void windowMoveCursor(IWindow win, int x, int y);

	public void windowFlowBreak(IWindow win);

	public void windowEraseRect(IWindow win, int left, int top, int width,
			int height);

	public void windowFillRect(IWindow win, Color c, int left, int top,
			int width, int height);

	public void windowSetBackgroundColor(IWindow win, Color c);

	public boolean imageGetInfo(int imgid, OutInt width, OutInt height);

	public boolean imageDrawScaled(IWindow win, int imgid, int val1, int val2,
			int width, int height);

	public boolean imageDraw(IWindow win, int imgid, int val1, int val2);

	public void requestTimerEvents(int delta);

	public void cancelMouseEvent(IWindow win);

	public void requestMouseEvent(IWindow win);

	public void cancelLineEvent(IWindow win, GlkEvent e);

	public void requestLineEventUni(IWindow win, InOutByteBuffer b, int maxlen,
			int initlen);

	public void requestLineEvent(IWindow win, InOutByteBuffer b, int maxlen,
			int initlen);

	public void cancelCharEvent(IWindow win);

	public void requestCharEventUni(IWindow win);

	public void requestCharEvent(IWindow win);

	public int gestaltExt(int sel, int val, InOutIntBuffer arr, int len);

	public int gestalt(int sel, int val);

	public void soundLoadHint(int soundId, int val);

	public void schannelSetVolume(SoundChannel c, int vol);

	public void schannelStop(SoundChannel c);

	public boolean schannelPlay(SoundChannel c, int soundId);

	public boolean schannelPlayExt(SoundChannel c, int soundId, int repeat,
			int notify);

	public void schannelDestroy(SoundChannel c);

	public SoundChannel schannelCreate(int rock);

	public int schannelGetRock(SoundChannel c);

	public int filerefGetRock(Fileref ref);

	public boolean filerefDoesFileExist(Fileref ref);

	public void filerefDeleteFile(Fileref ref);

	public void filerefDestroy(Fileref ref);

	public Fileref filerefCreateFromFileref(int usage, Fileref r, int rock);

	public Fileref filerefCreateByName(int usage, String name, int rock);

	public Fileref filerefCreateTemp(int usage, int rock);

	public boolean styleMeasure(IWindow win, int style, int hint, OutInt result);

	public boolean styleDistinguish(IWindow win, int s1, int s2);

	public void stylehintClear(int wintype, int style, int hint);

	public void stylehintSet(int wintype, int style, int hint, int val);

	public void setStyle(int style);

	public void setStyleStream(Stream s, int style);

	public int streamGetRock(Stream s);

	public Stream streamOpenFileUni(Fileref ref, int mode, int rock);

	public Stream streamOpenFile(Fileref ref, int mode, int rock);

	public Stream streamOpenMemoryUni(InOutByteBuffer b, int len, int mode,
			int rock);

	public Stream streamOpenMemory(InOutByteBuffer b, int len, int mode,
			int rock);

	public void streamSetPosition(Stream s, int pos, int seekmode);

	public int streamGetPosition(Stream s);

	public void streamClose(Stream s, StreamResult b);

	public int getLineStreamUni(Stream s, OutByteBuffer b, int len);

	public int getLineStream(Stream s, OutByteBuffer b, int len);

	public int getBufferStreamUni(Stream s, OutByteBuffer b, int len);

	public int getBufferStream(Stream s, OutByteBuffer b, int len);

	public int getCharStreamUni(Stream s);

	public int getCharStream(Stream s);

	public void putBufferStreamUni(Stream s, InByteBuffer b, int len);

	public void putBufferStream(Stream s, InByteBuffer b, int len);

	public void putStringStreamUni(Stream stm, String s);

	public void putStringStream(Stream stm, String s);

	public void putCharStreamUni(Stream s, int ch);

	public void putCharStream(Stream s, int ch);

	public void putBufferUni(InByteBuffer b, int len);

	public void putBuffer(InByteBuffer b, int len);

	public void putStringUni(String s);

	public void putString(String s);

	public void putCharUni(int ch);

	public void putChar(char ch);

	public Stream streamGetCurrent();

	public void streamSetCurrent(Stream s);

	public void setWindow(IWindow win);

	public Stream windowGetStream(IWindow win);

	public Stream windowGetEchoStream(IWindow win);

	public void windowSetEchoStream(IWindow win, Stream s);

	public void windowClose(IWindow w, StreamResult streamresult);

	public IWindow windowOpen(IWindow w, int method, int size, int wintype,
			int rock);

	public void windowClear(IWindow win);

	public int windowGetRock(IWindow w);

	public int windowGetType(IWindow win);

	public IWindow windowGetParent(IWindow win);

	public IWindow windowGetSibling(IWindow win);

	public void windowGetSize(IWindow win, OutInt b1, OutInt b2);

	public void windowSetArrangement(IWindow win, int method, int size,
			IWindow newKey);

	public void windowGetArrangement(IWindow win, OutInt method, OutInt size,
			OutWindow key);

	public char charToUpper(char ch);

	public char charToLower(char ch);

	public SoundChannel schannelIterate(SoundChannel s, OutInt rock);

	public Fileref filerefIterate(Fileref f, OutInt rock);

	public Stream streamIterate(Stream s, OutInt rock);

	public IWindow windowIterate(IWindow win, OutInt rock);

	public void exit();

	public void setInterruptHandler(Object o);

	public void tick();

	public void setDestructionCallback(ObjectCallback c);

	public void setCreationCallback(ObjectCallback c);

	public void setMorePromptCallback(ObjectCallback c);

	public void setBlorbFile(BlorbFile f);

	public void progress(String stJob, int min, int max, int cur);

	public void reset();

	public void flush();

	public void getCurrentTime(GlkTimeval timeval);

	public int getCurrentSimpleTime(int factor);

	public void convertTimeToDateUtc(GlkTimeval time, GlkDate date);

	public void convertTimeToDateLocal(GlkTimeval time, GlkDate date);

	public void convertSimpleTimeToDateLocal(int time, int factor, GlkDate date);

	public void convertSimpleTimeToDateUtc(int time, int factor, GlkDate date);

	public void convertDateToTimeUtc(GlkDate date, GlkTimeval time);

	public void convertDateToTimeLocal(GlkDate date, GlkTimeval time);

	public int convertDateToSimpleTimeUtc(GlkDate date, int factor);

	public int convertDateToSimpleTimeLocal(GlkDate date, int factor);

	public IWindow windowGetRoot();

	public Fileref filerefCreateByPrompt(int usage, int fmode, int rock);

	public void select(GlkEvent e);

	public int bufferToLowerCaseUni(InOutByteBuffer buf, int len, int numchars);

	public int bufferToUpperCaseUni(InOutByteBuffer buf, int len, int numchars);

	public int bufferToTitleCaseUni(InOutByteBuffer buf, int len, int numchars);

	public static final int GESTALT_VERSION = 0;
	public static final int GESTALT_CHAR_INPUT = 1;
	public static final int GESTALT_LINE_INPUT = 2;
	public static final int GESTALT_CHAR_OUTPUT = 3;
	public static final int GESTALT_CHAR_OUTPUT_CANNOT_PRINT = 0;
	public static final int GESTALT_CHAR_OUTPUT_APPROX_PRINT = 1;
	public static final int GESTALT_CHAR_OUTPUT_EXACT_PRINT = 2;
	public static final int GESTALT_MOUSE_INPUT = 4;
	public static final int GESTALT_TIMER = 5;
	public static final int GESTALT_GRAPHICS = 6;
	public static final int GESTALT_DRAW_IMAGE = 7;
	public static final int GESTALT_SOUND = 8;
	public static final int GESTALT_SOUND_VOLUME = 9;
	public static final int GESTALT_SOUND_NOTIFY = 10;
	public static final int GESTALT_HYPERLINKS = 11;
	public static final int GESTALT_HYPERLINK_INPUT = 12;
	public static final int GESTALT_SOUND_MUSIC = 13;
	public static final int GESTALT_GRAPHICS_TRANSPARENCY = 14;
	public static final int GESTALT_UNICODE = 15;
	public static final int GESTALT_UNICODENORM = 16;
	public static final int GESTALT_LINEINPUTECHO = 17;
	public static final int GESTALT_LINETERMINATORS = 18;
	public static final int GESTALT_LINETERMINATORKEY = 19;
	public static final int GESTALT_DATETIME = 20;
	public static final int GESTALT_SOUND2 = 21;
	public static final int GESTALT_RESOURCESTREAM = 22;
	public static final int EVTYPE_NONE = 0;
	public static final int EVTYPE_TIMER = 1;
	public static final int EVTYPE_CHAR_INPUT = 2;
	public static final int EVTYPE_LINE_INPUT = 3;
	public static final int EVTYPE_MOUSE_INPUT = 4;
	public static final int EVTYPE_ARRANGE = 5;
	public static final int EVTYPE_REDRAW = 6;
	public static final int EVTYPE_SOUND_NOTIFY = 7;
	public static final int EVTYPE_HYPERLINK = 8;

	public static final int KEYCODE_UNKNOWN = 0xffffffff;
	public static final int KEYCODE_LEFT = 0xfffffffe;
	public static final int KEYCODE_RIGHT = 0xfffffffd;
	public static final int KEYCODE_UP = 0xfffffffc;
	public static final int KEYCODE_DOWN = 0xfffffffb;
	public static final int KEYCODE_RETURN = 0xfffffffa;
	public static final int KEYCODE_DELETE = 0xfffffff9;
	public static final int KEYCODE_ESCAPE = 0xfffffff8;
	public static final int KEYCODE_TAB = 0xfffffff7;
	public static final int KEYCODE_PAGE_UP = 0xfffffff6;
	public static final int KEYCODE_PAGE_DOWN = 0xfffffff5;
	public static final int KEYCODE_HOME = 0xfffffff4;
	public static final int KEYCODE_END = 0xfffffff3;
	public static final int KEYCODE_FUNC1 = 0xffffffef;
	public static final int KEYCODE_FUNC2 = 0xffffffee;
	public static final int KEYCODE_FUNC3 = 0xffffffed;
	public static final int KEYCODE_FUNC4 = 0xffffffec;
	public static final int KEYCODE_FUNC5 = 0xffffffeb;
	public static final int KEYCODE_FUNC6 = 0xffffffea;
	public static final int KEYCODE_FUNC7 = 0xffffffe9;
	public static final int KEYCODE_FUNC8 = 0xffffffe8;
	public static final int KEYCODE_FUNC9 = 0xffffffe7;
	public static final int KEYCODE_FUNC10 = 0xffffffe6;
	public static final int KEYCODE_FUNC11 = 0xffffffe5;
	public static final int KEYCODE_FUNC12 = 0xffffffe4;
	public static final int KEYCODE_MAXVAL = 28;
	public static final int STYLE_NORMAL = 0;
	public static final int STYLE_EMPHASIZED = 1;
	public static final int STYLE_PREFORMATTED = 2;
	public static final int STYLE_HEADER = 3;
	public static final int STYLE_SUBHEADER = 4;
	public static final int STYLE_ALERT = 5;
	public static final int STYLE_NOTE = 6;
	public static final int STYLE_BLOCKQUOTE = 7;
	public static final int STYLE_INPUT = 8;
	public static final int STYLE_USER1 = 9;
	public static final int STYLE_USER2 = 10;
	public static final int STYLE_NUMSTYLES = 11;
	public static final String[] STYLES = { "normal", "emphasized",
			"preformatted", "header", "subheader", "alert", "note",
			"blockquote", "input", "user1", "user2" };
	public static final int WINTYPE_ALL_TYPES = 0;
	public static final int WINTYPE_PAIR = 1;
	public static final int WINTYPE_BLANK = 2;
	public static final int WINTYPE_TEXT_BUFFER = 3;
	public static final int WINTYPE_TEXT_GRID = 4;
	public static final int WINTYPE_GRAPHICS = 5;
	public static final int WINMETHOD_LEFT = 0x00;
	public static final int WINMETHOD_RIGHT = 0x01;
	public static final int WINMETHOD_ABOVE = 0x02;
	public static final int WINMETHOD_BELOW = 0x03;
	public static final int WINMETHOD_DIRMASK = 0x0f;
	public static final int WINMETHOD_FIXED = 0x10;
	public static final int WINMETHOD_PROPORTIONAL = 0x20;
	public static final int WINMETHOD_DIVISION_MASK = 0xf0;
	public static final int FILEUSAGE_DATA = 0x00;
	public static final int FILEUSAGE_SAVED_GAME = 0x01;
	public static final int FILEUSAGE_TRANSCRIPT = 0x02;
	public static final int FILEUSAGE_INPUT_RECORD = 0x03;
	public static final int FILEUSAGE_TYPE_MASK = 0x0f;
	public static final int FILEUSAGE_TEXT_MODE = 0x100;
	public static final int FILEUSAGE_BINARY_MODE = 0x000;
	public static final int FILEMODE_WRITE = 0x01;
	public static final int FILEMODE_READ = 0x02;
	public static final int FILEMODE_READ_WRITE = 0x03;
	public static final int FILEMODE_WRITE_APPEND = 0x05;
	public static final int SEEKMODE_START = 0;
	public static final int SEEKMODE_CURRENT = 1;
	public static final int SEEKMODE_END = 2;
	public static final int STYLEHINT_INDENTATION = 0;
	public static final int STYLEHINT_PARA_INDENTATION = 1;
	public static final int STYLEHINT_JUSTIFICATION = 2;
	public static final int STYLEHINT_SIZE = 3;
	public static final int STYLEHINT_WEIGHT = 4;
	public static final int STYLEHINT_OBLIQUE = 5;
	public static final int STYLEHINT_PROPORTIONAL = 6;
	public static final int STYLEHINT_TEXT_COLOR = 7;
	public static final int STYLEHINT_BACK_COLOR = 8;
	public static final int STYLEHINT_REVERSE_COLOR = 9;
	public static final int STYLEHINT_NUMHINTS = 10;
	public static final int STYLEHINT_JUST_LEFT_FLUSH = 0;
	public static final int STYLEHINT_JUST_LEFT_RIGHT = 1;
	public static final int STYLEHINT_JUST_CENTERED = 2;
	public static final int STYLEHINT_JUST_RIGHT_FLUSH = 3;
	public static final int IMAGEALIGN_INLINE_UP = 0x01;
	public static final int IMAGEALIGN_INLINE_DOWN = 0x02;
	public static final int IMAGEALIGN_INLINE_CENTER = 0x03;
	public static final int IMAGEALIGN_MARGIN_LEFT = 0x04;
	public static final int IMAGEALIGN_MARGIN_RIGHT = 0x05;

	public static int STRICTNESS_IGNORE = 0;
	public static int STRICTNESS_WARN = 1;
	public static int STRICTNESS_DIE = 2;
	public static int STRICTNESS = STRICTNESS_WARN;

}
