package org.p2c2e.zing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import org.p2c2e.blorb.*;
import org.p2c2e.util.*;
import com.sixlegs.image.png.PngImage;

public abstract class Glk
{
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

  public static final int EVTYPE_NONE = 0;
  public static final int EVTYPE_TIMER = 1;
  public static final int EVTYPE_CHAR_INPUT = 2;
  public static final int EVTYPE_LINE_INPUT = 3;
  public static final int EVTYPE_MOUSE_INPUT = 4;
  public static final int EVTYPE_ARRANGE = 5;
  public static final int EVTYPE_REDRAW = 6;
  public static final int EVTYPE_SOUND_NOTIFY = 7;
  public static final int EVTYPE_HYPERLINK = 8;

  public static class GlkEvent
  {
    public int type;
    public Window win;
    public int val1, val2;
  }


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

  public static final String[] STYLES = 
  {"normal", "emphasized", "preformatted", "header", "subheader", "alert",
   "note", "blockquote", "input", "user1", "user2"};


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


  static final Comparator HC_COMP = new HashCodeComparator();
  static TreeMap WINDOWS;
  static TreeMap STREAMS;
  static TreeMap FILE_REFS;
  static TreeMap SOUND_CHANNELS;

  static Stream CURRENT_STREAM;

  static LinkedList EVENT_QUEUE;

  static int TIMER = 0;
  static long TIMESTAMP;

  static BlorbFile BLORB_FILE;
  static MediaTracker TRACKER;
  static LinkedList IMAGE_CACHE;

  static ObjectCallback CREATE_CALLBACK;
  static ObjectCallback DESTROY_CALLBACK;

  static boolean BORDERS_ON = true;

  public static final int STRICTNESS_IGNORE = 0;
  public static final int STRICTNESS_WARN = 1;
  public static final int STRICTNESS_DIE = 2;
  static int STRICTNESS = STRICTNESS_WARN;


  public static void setFrame(JFrame frame)
  {
    setFrame(frame, true, true, null, null, -1, -1);
  }

  public static void setFrame(JFrame frame,
                              boolean statusOn,
                              boolean bordersOn, 
                              String proportionalFont, 
                              String fixedFont,
                              int propFontSize,
                              int fixedFontSize)
  {
    reset();

    Window.FRAME = (RootPaneContainer) frame;

    BORDERS_ON = bordersOn;

    // a hack to get around abysmally poor font substitution performance
    if (proportionalFont != null &&
        (new Font(proportionalFont, Font.PLAIN, 1)).getName().toLowerCase().equals(proportionalFont.toLowerCase()))
    {
      Window.DEFAULT_PROPORTIONAL_FONT = proportionalFont;
      Window.OVERRIDE_PROPORTIONAL_FONT = true;
    }

    if (fixedFont != null && 
        (new Font(fixedFont, Font.PLAIN, 1)).getName().toLowerCase().equals(fixedFont.toLowerCase()))
    {
      Window.DEFAULT_FIXED_FONT = fixedFont;
      Window.OVERRIDE_FIXED_FONT = true;
    }

    if (propFontSize > 0)
    {
      Window.DEFAULT_PROP_FONT_SIZE = propFontSize;
      Window.OVERRIDE_PROP_FONT_SIZE = true;
    }
    if (fixedFontSize > 0)
    {
      Window.DEFAULT_FIXED_FONT_SIZE = fixedFontSize;
      Window.OVERRIDE_FIXED_FONT_SIZE = true;
    }

    LameFocusManager.registerFrame(frame, statusOn);
    
    TRACKER = new MediaTracker(frame);
    Style.setupStyles(Window.FRAME);
  }

  public static void flush()
  {
    if (Window.root != null)
      Window.root.doLayout();
    
    try {
      Iterator it = SOUND_CHANNELS.keySet().iterator();
      while (it.hasNext())
        ((SoundChannel) it.next()).stop();
      it = STREAMS.keySet().iterator();
      while (it.hasNext())
        ((Stream) it.next()).close();
    } catch(Exception e) {
      System.err.println("problem while attempting to stop sound channel: " + e);
    }
  }

  public static void reset()
  {
    if (Window.root != null)
    {
      Window.close(Window.root);
      Window.FRAME.getContentPane().validate();
      Window.FRAME.getContentPane().repaint();
    }

    WINDOWS = new TreeMap(HC_COMP);
    STREAMS = new TreeMap(HC_COMP);
    FILE_REFS = new TreeMap(HC_COMP);
    SOUND_CHANNELS = new TreeMap(HC_COMP);

    CURRENT_STREAM = null;
    EVENT_QUEUE = new LinkedList();    
    TIMER = 0;
    TIMESTAMP = 0L;
    BLORB_FILE = null;
    IMAGE_CACHE = new LinkedList();

    StyleHints.clearAll();
  }

  public static StatusPane getStatusPane()
  {
    return LameFocusManager.STATUS;
  }

  public static void progress(String stJob, int min, int max, int cur)
  {
    JProgressBar prog = LameFocusManager.STATUS.prog;
    
    if (min == max && prog.isVisible())
    {
      prog.setVisible(false);
      prog.setIndeterminate(false);
    }
    else 
    {
      if (!prog.isVisible())
      {
        prog.setVisible(true);
        prog.revalidate();
      }
        
      if (stJob != null)
      {
        prog.setStringPainted(true);
        prog.setString(stJob);
      }
      else
      {
        prog.setStringPainted(false);
      }
      
      if (min < max)
      {
        if (min >= 0)
          prog.setMinimum(min);
        if (max >= 0)
          prog.setMaximum(max);
      
        prog.setValue(cur);
      }
      else
      {
        prog.setIndeterminate(true);
      }
    }
  }

  public static void setBlorbFile(BlorbFile f)
  {
    BLORB_FILE = f;
    IMAGE_CACHE.clear();

  }

  public static void setMorePromptCallback(ObjectCallback c)
  {
    TextBufferWindow.MORE_CALLBACK = c;
  }

  public static void setCreationCallback(ObjectCallback c)
  {
    CREATE_CALLBACK = c;
  }

  public static void setDestructionCallback(ObjectCallback c)
  {
    DESTROY_CALLBACK = c;
  }

  public static void tick()
  {

  }

  // why does *this* function have a glk selector?!
  public static void setInterruptHandler(Object o)
  {

  }

  public static void exit()
  {
    System.exit(0);
  }

  static Object objIterate(SortedMap mainMap, Object o, Int rock)
  {
    SortedMap m;
    Object next;

    if (o == null)
      m = mainMap;
    else
      m = mainMap.tailMap(new Integer(o.hashCode() + 1));
    
    if (m.isEmpty())
    {
      if (rock != null)
        rock.val = 0;
      return null;
    }
    else
    {
      next = m.firstKey();
      if (rock != null)
        rock.val =((Integer) mainMap.get(next)).intValue(); 
      return next;
    }
  }

  public static Window windowIterate(Window win, OutInt rock)
  {
    return (Window) objIterate(WINDOWS, win, rock);
  }

  public static Stream streamIterate(Stream s, OutInt rock)
  {
    return (Stream) objIterate(STREAMS, s, rock);
  }

  public static Fileref filerefIterate(Fileref f, OutInt rock)
  {
    return (Fileref) objIterate(FILE_REFS, f, rock);
  }

  public static SoundChannel schannelIterate(SoundChannel s, OutInt rock)
  {
    return (SoundChannel) objIterate(SOUND_CHANNELS, s, rock);
  }

  public static char charToLower(char ch)
  {
    return Character.toLowerCase(ch);
  }

  public static char charToUpper(char ch)
  {
    return Character.toUpperCase(ch);
  }

  public static Window windowGetRoot()
  {
    return Window.root;
  }

  public static void windowGetArrangement(Window win, OutInt method, OutInt size,
                                          OutWindow key)
  {
    if (win == null)
    {
      nullRef("Glk.windowGetArrangement");
    }
    else
    {
      PairWindow w = (PairWindow) win;
      if (method != null)
        method.val = w.getSplitMethod();
      if (size != null)
        size.val = w.getKeyWindowSize();
      if (key != null)
        key.window = w.key;
    }
  }

  public static void windowSetArrangement(Window win, int method, int size,
                                          Window newKey)
  {
    if (win == null)
      nullRef("Glk.windowSetArrangement");
    else
      ((PairWindow) win).setArrangement(method, size, newKey);
  }

  public static void windowGetSize(Window win, OutInt b1, OutInt b2)
  {
    if (win != null)
    {
      if (b1 != null)
        b1.val = win.getWindowWidth();
      if (b2 != null)
        b2.val = win.getWindowHeight();
    }
    else
    {
      nullRef("Glk.windowGetSize");
    }
  }

  public static Window windowGetSibling(Window win)
  {
    if (win != null)
      return win.getSibling();

    nullRef("Glk.windowGetSibling");
    return null;
  }

  public static Window windowGetParent(Window win)
  {
    if (win != null)
      return win.getParent();

    nullRef("Glk.widowGetParent");
    return null;
  }

  public static int windowGetType(Window win)
  {
    if (win instanceof TextBufferWindow)
      return WINTYPE_TEXT_BUFFER;
    if (win instanceof TextGridWindow)
      return WINTYPE_TEXT_GRID;
    if (win instanceof PairWindow)
      return WINTYPE_PAIR;
    if (win instanceof GraphicsWindow)
      return WINTYPE_GRAPHICS;
    if (win instanceof BlankWindow)
      return WINTYPE_BLANK;
    
    if (win == null)
      nullRef("Glk.windowGetType");

    return -1;
  }

  public static int windowGetRock(Window w)
  {
    if (w == null)
    {
      nullRef("Glk.windowGetRock");
      return 0;
    }

    if (w instanceof PairWindow)
      return 0;

    return ((Integer) WINDOWS.get(w)).intValue();
  }

  public static void windowClear(Window win)
  {
    if (win == null)
      nullRef("Glk.windowClear");
    else
      win.clear();
  }

  public static Window windowOpen(Window w, int method, int size, int wintype,
                                  int rock)
  {
    Window win = Window.split(w, method, size, BORDERS_ON, wintype);

    WINDOWS.put(win, new Integer(rock));
    STREAMS.put(win.stream, new Integer(0));
    if (w != null)
    {
      WINDOWS.put(win.getParent(), new Integer(0));
      STREAMS.put(win.getParent().stream, new Integer(0));
    }
    if (CREATE_CALLBACK != null)
    {
      CREATE_CALLBACK.callback(win);
      CREATE_CALLBACK.callback(win.stream);
      if (w != null)
      {
        CREATE_CALLBACK.callback(win.getParent());
        CREATE_CALLBACK.callback(win.getParent().stream);
      }
    }
    return win;
  }

  public static void windowClose(Window w, Stream.Result streamresult)
  {
    if (w == null)
    {
      nullRef("Glk.windowClose");
      return;
    }

    Stream.Result r = Window.close(w);
    if (streamresult != null)
    {
      streamresult.readcount = r.readcount;
      streamresult.writecount = r.writecount;
    }
    windowCloseRecurse(w);
  }

  private static void windowCloseRecurse(Window w)
  {
    if (w instanceof PairWindow)
    {
      PairWindow pw = (PairWindow) w;
      windowCloseRecurse(pw.first);
      windowCloseRecurse(pw.second);
    }

    WINDOWS.remove(w);
    STREAMS.remove(w.stream);
    if (DESTROY_CALLBACK != null)
    {
      DESTROY_CALLBACK.callback(w);
      DESTROY_CALLBACK.callback(w.stream);
    }
  }

  public static void windowSetEchoStream(Window win, Stream s)
  {
    if (win == null)
      nullRef("Glk.windowSetEchoStream");
    else
      win.setEchoStream(s);
  }

  public static Stream windowGetEchoStream(Window win)
  {
    if (win == null)
    {
      nullRef("Glk.windowGetEchoStream");
      return null;
    }
    else
    {
      return win.getEchoStream();
    }
  }

  public static Stream windowGetStream(Window win)
  {
    if (win == null)
    {
      nullRef("Glk.windowGetStream");
      return null;
    }
    else
    {
      return win.getStream();
    }
  }

  public static void setWindow(Window win)
  {
    CURRENT_STREAM = (win == null) ? null : win.getStream();
  }

  public static void streamSetCurrent(Stream s)
  {
    if (s == null || s.canWrite)
      CURRENT_STREAM = s;
  }

  public static Stream streamGetCurrent()
  {
    return CURRENT_STREAM;
  }

  public static void putChar(char ch)
  {
    if (CURRENT_STREAM == null)
      nullRef("Glk.putChar");
    else
      CURRENT_STREAM.putChar(ch);
  }

  public static void putCharUni(int ch)
  {
    if (CURRENT_STREAM == null)
      nullRef("Glk.putCharUni");
    else
        CURRENT_STREAM.putCharUni(ch);
  }

  public static void putString(String s)
  {
    if (CURRENT_STREAM == null)
      nullRef("Glk.putString");
    else
      CURRENT_STREAM.putString(s);
  }

  public static void putStringUni(String s)
  {
    if (CURRENT_STREAM == null)
      nullRef("Glk.putStringUni");
    else
      CURRENT_STREAM.putStringUni(s);
  }

  public static void putBuffer(InByteBuffer b, int len)
  {
    if (CURRENT_STREAM == null)
      nullRef("Glk.putBuffer");
    else
      CURRENT_STREAM.putBuffer(b.buffer, len);
  }

  public static void putBufferUni(InByteBuffer b, int len)
  {
    if (CURRENT_STREAM == null)
      nullRef("Glk.putBufferUni");
    else
      CURRENT_STREAM.putBufferUni(b.buffer, len);
  }

  public static void putCharStream(Stream s, int ch)
  {
    if (s == null)
      nullRef("Glk.putCharStream");
    else
      s.putChar(ch);
  }
  public static void putCharStreamUni(Stream s, int ch)
  {
    if (s == null)
      nullRef("Glk.putCharStreamUni");
    else
      s.putCharUni(ch);
  }

  public static void putStringStream(Stream stm, String s)
  {
    if (stm == null)
      nullRef("Glk.putStringStream");
    else
      stm.putString(s);
  }

  public static void putStringStreamUni(Stream stm, String s)
  {
    if (stm == null)
      nullRef("Glk.putStringStreamUni");
    else
      stm.putStringUni(s);
  }

  public static void putBufferStream(Stream s, InByteBuffer b, int len)
  {
    if (s == null)
      nullRef("Glk.putBufferStream");
    else
      s.putBuffer(b.buffer, len);
  }

  public static void putBufferStreamUni(Stream s, InByteBuffer b, int len)
  {
    if (s == null)
      nullRef("Glk.putBufferStreamUni");
    else
      s.putBufferUni(b.buffer, len);
  }

  public static int getCharStream(Stream s)
  {
    if (s != null)
      return s.getChar();

    nullRef("Glk.getCharStream");
    return -1;
  }

  public static int getCharStreamUni(Stream s)
  {
    if (s != null)
      return s.getCharUni();

    nullRef("Glk.getCharStreamUni");
    return -1;
  }

  public static int getBufferStream(Stream s, OutByteBuffer b, int len)
  {
    if (s != null)
      return s.getBuffer(b.buffer, len);

    nullRef("Glk.getBufferStream");
    return -1;
  }

  public static int getBufferStreamUni(Stream s, OutByteBuffer b, int len)
  {
    if (s != null)
      return s.getBufferUni(b.buffer, len);

    nullRef("Glk.getBufferStreamUni");
    return -1;
  }

  public static int getLineStream(Stream s, OutByteBuffer b, int len)
  {
    if (s != null)
      return s.getLine(b.buffer, len);

    nullRef("Glk.getLineStream");
    return -1;
  }

  public static int getLineStreamUni(Stream s, OutByteBuffer b, int len)
  {
    if (s != null)
      return s.getLineUni(b.buffer, len);

    nullRef("Glk.getLineStreamUni");
    return -1;
  }

  public static void streamClose(Stream s, Stream.Result b)
  {
    if (s == null)
    {
      nullRef("Glk.streamClose");
      return;
    }

    Stream.Result res = s.close();
    if (b != null)
    {
      b.readcount = res.readcount;
      b.writecount = res.writecount;
    }

    STREAMS.remove(s);
    if (DESTROY_CALLBACK != null)
      DESTROY_CALLBACK.callback(s);
  }

  public static int streamGetPosition(Stream s)
  {
    if (s == null)
    {
      nullRef("Glk.streamGetPosition");
      return -1;
    }

    return s.getPosition();
  }

  public static void streamSetPosition(Stream s, int pos, int seekmode)
  {
    if (s == null)
    {
      nullRef("Glk.streamSetPosition");
      return;
    }

    s.setPosition(pos, seekmode);
  }

  public static Stream streamOpenMemory(InOutByteBuffer b, 
                                        int len, int mode, int rock)
  {
    Stream s = new Stream.MemoryStream(b.buffer, len, mode);

    STREAMS.put(s, new Integer(rock));
    if (CREATE_CALLBACK != null)
      CREATE_CALLBACK.callback(s);
    return s;
  }

  public static Stream streamOpenMemoryUni(InOutByteBuffer b, 
                                        int len, int mode, int rock)
  {
    Stream s = new Stream.UnicodeMemoryStream(b.buffer, len, mode);

    STREAMS.put(s, new Integer(rock));
    if (CREATE_CALLBACK != null)
      CREATE_CALLBACK.callback(s);
    return s;
  }

  public static Stream streamOpenFile(Fileref ref, int mode, int rock)
  {
    if (ref == null)
    {
      nullRef("Glk.streamOpenFile");
      return null;
    }

    Stream s = new Stream.FileStream(ref, mode, false);

    STREAMS.put(s, new Integer(rock));
    if (CREATE_CALLBACK != null)
      CREATE_CALLBACK.callback(s);
    return s;
  }

  public static Stream streamOpenFileUni(Fileref ref, int mode, int rock)
  {
    if (ref == null)
    {
      nullRef("Glk.streamOpenFileUni");
      return null;
    }

    Stream s = new Stream.FileStream(ref, mode, true);

    STREAMS.put(s, new Integer(rock));
    if (CREATE_CALLBACK != null)
      CREATE_CALLBACK.callback(s);
    return s;
  }


  public static int streamGetRock(Stream s)
  {
    if (s == null)
    {
      nullRef("Glk.streamGetRock");
      return 0;
    }

    return ((Integer) STREAMS.get(s)).intValue();
  }

  public static void setStyleStream(Stream s, int style)
  {
    if (s == null)
    {
      nullRef("Glk.setStyleStream");
      return;
    }

    if (style < STYLE_NUMSTYLES)
      s.setStyle(STYLES[style]);
    else
      s.setStyle(STYLES[STYLE_NORMAL]);
  }

  public static void setStyle(int style)
  {
    if (CURRENT_STREAM != null)
      setStyleStream(CURRENT_STREAM, style);
  }

  public static void stylehintSet(int wintype, int style, int hint, int val)
  {
    StyleHints.setHint(wintype, Style.getStyle(STYLES[style], wintype), hint, val);
  }

  public static void stylehintClear(int wintype, int style, int hint)
  {
    StyleHints.clearHint(wintype, Style.getStyle(STYLES[style], wintype), hint);
  }

  public static boolean styleDistinguish(Window win, int s1, int s2)
  {
    if (win == null)
    {
      nullRef("Glk.styleDistinguish");
      return false;
    }

    Style first = (Style) win.hintedStyles.get(STYLES[s1]);
    Style second = (Style) win.hintedStyles.get(STYLES[s2]);

    if (!first.family.equals(second.family))
      return true;
    if ((first.isOblique || second.isOblique) && 
        !(first.isOblique && second.isOblique))
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

  public static boolean styleMeasure(Window win, int style, int hint, OutInt result)
  {
    if (win == null)
    {
      nullRef("Glk.styleMeasure");
      return false;
    }

    return win.measureStyle(STYLES[style], hint, result);
  }

  public static Fileref filerefCreateTemp(int usage, int rock)
  {
    try
    {
      Fileref ref = Fileref.createTemp(usage);
      if (ref != null)
      {
        FILE_REFS.put(ref, new Integer(rock));
        if (CREATE_CALLBACK != null)
          CREATE_CALLBACK.callback(ref);
        return ref;
      }
      else
      {
        return null;
      }
    }
    catch(IOException e)
    {
      return null;
    }
  }

  public static Fileref filerefCreateByPrompt(int usage, int mode, int rock)
  {
    Fileref ref = Fileref.createByPrompt(usage, mode);
    if (ref != null)
    {
      FILE_REFS.put(ref, new Integer(rock));
      if (CREATE_CALLBACK != null)
        CREATE_CALLBACK.callback(ref);
      return ref;
    }
    else
    {
      return null;
    }
  }

  public static Fileref filerefCreateByName(int usage, String name, int rock)
  {
    Fileref ref = Fileref.createByName(usage, name);
    if (ref != null)
    {
      FILE_REFS.put(ref, new Integer(rock));
      if (CREATE_CALLBACK != null)
        CREATE_CALLBACK.callback(ref);
      return ref;
    }
    else
    {
      return null;
    }
  }

  public static Fileref filerefCreateFromFileref(int usage, Fileref r, int rock)
  {
    if (r == null)
    {
      nullRef("Glk.filerefCreateFromFileref");
      return null;
    }

    Fileref ref = Fileref.createFromFileref(usage, r);
    FILE_REFS.put(ref, new Integer(rock));
    if (CREATE_CALLBACK != null)
      CREATE_CALLBACK.callback(ref);
    return ref;
  }

  public static void filerefDestroy(Fileref ref)
  {
    if (ref == null)
    {
      nullRef("Glk.filerefDestroy");
      return;
    }

    ref.destroy();

    FILE_REFS.remove(ref);
    if (DESTROY_CALLBACK != null)
      DESTROY_CALLBACK.callback(ref);
  }

  public static void filerefDeleteFile(Fileref ref)
  {
    if (ref == null)
    {
      nullRef("Glk.filerefDeleteFile");
      return;
    }

    Fileref.deleteFile(ref);
  }

  public static boolean filerefDoesFileExist(Fileref ref)
  {
    if (ref == null)
    {
      nullRef("Glk.filerefDoesFileExist");
      return false;
    }

    return ref.fileExists();
  }

  public static int filerefGetRock(Fileref ref)
  {
    if (ref == null)
    {
      nullRef("Glk.filerefGetRock");
      return 0;
    }

    return ((Integer) FILE_REFS.get(ref)).intValue();
  }

  public static int schannelGetRock(SoundChannel c)
  {
    if (c == null)
    {
      nullRef("Glk.schannelGetRock");
      return 0;
    }

    return ((Integer) SOUND_CHANNELS.get(c)).intValue();
  }

  public static SoundChannel schannelCreate(int rock)
  {
    SoundChannel c = new SoundChannel();
    SOUND_CHANNELS.put(c, new Integer(rock));
    if (CREATE_CALLBACK != null)
      CREATE_CALLBACK.callback(c);

    return c;
  }

  public static void schannelDestroy(SoundChannel c)
  {
    if (c == null)
    {
      nullRef("Glk.schannelDestroy");
      return;
    }

    try
    {
      c.destroy();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
    SOUND_CHANNELS.remove(c);
    if (DESTROY_CALLBACK != null)
      DESTROY_CALLBACK.callback(c);
  }

  public static boolean schannelPlayExt(SoundChannel c, int soundId, 
                                 int repeat, int notify)
  {
    if (c == null)
    {
      nullRef("Glk.schannelPlayExt");
      return false;
    }

    try
    {
      return c.play(soundId, repeat, notify);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean schannelPlay(SoundChannel c, int soundId)
  {
    return schannelPlayExt(c, soundId, 1, 0);
  }

  public static void schannelStop(SoundChannel c)
  {
    if (c == null)
    {
      nullRef("Glk.schannelStop");
      return;
    }

    try
    {
      c.stop();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void schannelSetVolume(SoundChannel c, int vol)
  {
    if (c == null)
      nullRef("Glk.schannelSetVolume");
    else
      c.setVolume(vol);
  }

  public static void soundLoadHint(int soundId, int val)
  {

  }

  

  public static int gestalt(int sel, int val)
  {
    return gestaltExt(sel, val, null, 0);
  }

  public static int gestaltExt(int sel, int val, InOutIntBuffer arr, int len)
  {
    switch(sel)
    {
    case GESTALT_VERSION:
      return 0x00000601;
    case GESTALT_CHAR_OUTPUT:
      if (val == 10 || (val >=32 && val < 127) || (val >= 160 && val < 256))
      {
        if (arr != null && len > 0)
          arr.buffer.put(1);
        return GESTALT_CHAR_OUTPUT_EXACT_PRINT;
      }
      else
      {
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
      if (val == KEYCODE_LEFT || val == KEYCODE_RIGHT || 
          val == KEYCODE_UP || val == KEYCODE_DOWN)
        return 1;
      return 0;
    case GESTALT_MOUSE_INPUT:
      if (val == WINTYPE_TEXT_GRID || val == WINTYPE_GRAPHICS)
        return 1;
      else return 0;
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
    default:
      return 0;
    }    
  }

  public static void requestCharEvent(Window win)
  {
    if (win == null)
      nullRef("Glk.requestCharEvent");
    else
      win.requestCharacterInput(new GlkCharConsumer(win));
  }

  /* TODO: test this */
  public static void requestCharEventUni(Window win)
  {
    if (win == null)
      nullRef("Glk.requestCharEvent");
    else
      win.requestCharacterInput(new GlkCharConsumer(win));
  }

  public static void cancelCharEvent(Window win)
  {
    if (win == null)
      nullRef("Glk.cancelCharEvent");
    else
      win.cancelCharacterInput();
  }

  public static void requestLineEvent(Window win, InOutByteBuffer b, 
                                      int maxlen, int initlen)
  {
    if (win == null)
    {
      nullRef("Glk.requestLineEvent");
      return;
    }

    StringBuffer sb;
    String s = null;
    if (initlen > 0)
    {
      sb = new StringBuffer();
      for (int i = 0; i < initlen; i++)
        sb.append((char) b.buffer.get(i));
      s = sb.toString();
    }
    win.requestLineInput(new GlkLineConsumer(win, b.buffer, false), s, maxlen);
  }

  public static void requestLineEventUni(Window win, InOutByteBuffer b, 
                                         int maxlen, int initlen)
  {
    if (win == null)
    {
      nullRef("Glk.requestLineEventUni");
      return;
    }

    StringBuffer sb;
    String s = null;
    if (initlen > 0)
    {
      sb = new StringBuffer();
      for (int i = 0; i < initlen; i ++) {
          int t = b.buffer.getInt(i * 4);
          sb.appendCodePoint(t);
      }
      s = sb.toString();
    }
    win.requestLineInput(new GlkLineConsumer(win, b.buffer, true), s, maxlen);
  }

  public static void cancelLineEvent(Window win, GlkEvent e)
  {
    if (win == null)
    {
      nullRef("Glk.cancelLineEvent");
      return;
    }

    String s = win.cancelLineInput();

    if (e != null)
    {
      e.type = EVTYPE_LINE_INPUT;
      e.win = win;
      e.val1 = s.length();
      e.val2 = 0;
    }
  }

  public static void requestMouseEvent(Window win)
  {
    if (win == null)
      nullRef("Glk.requestMouseEvent");
    else
      win.requestMouseInput(new GlkMouseConsumer(win));
  }

  public static void cancelMouseEvent(Window win)
  {
    if (win == null)
      nullRef("Glk.cancelMouseEvent");
    else
      win.cancelMouseInput();
  }

  public static void requestTimerEvents(int delta)
  {
    TIMESTAMP = System.currentTimeMillis();
    TIMER = delta;
  }

  public static boolean imageDraw(Window win, int imgid, int val1, int val2)
  {
    Image img = getImage(imgid, -1, -1);
    if (img == null)
      return false;

    if (win instanceof TextBufferWindow)
    {
      ((TextBufferWindow) win).drawImage(img, val1);
      return true;
    }
    if (win instanceof GraphicsWindow)
    {
      ((GraphicsWindow) win).drawImage(img, val1, val2);
      return true;
    }
    return false;
  }

  public static boolean imageDrawScaled(Window win, int imgid, int val1, 
                                        int val2, int width, int height)
  {
    Image img = getImage(imgid, width, height);
    if (img == null)
      return false;

    if (win instanceof TextBufferWindow)
    {
      ((TextBufferWindow) win).drawImage(img, val1);
      return true;
    }
    if (win instanceof GraphicsWindow)
    {
      ((GraphicsWindow) win).drawImage(img, val1, val2);
      return true;
    }
    return false;
  }

  public static boolean imageGetInfo(int imgid, OutInt width, OutInt height)
  {
    if (BLORB_FILE != null)
    {
      Image img = getImage(imgid, -1, -1);
      if (img != null)
      {
        if (width != null)
          width.val = img.getWidth((Component) Window.FRAME);
        if (height != null)
          height.val = img.getHeight((Component) Window.FRAME);
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return false;
    }
  }

  public static void windowSetBackgroundColor(Window win, Color c)
  {
    if (win instanceof GraphicsWindow)
      ((GraphicsWindow) win).setBackgroundColor(c);
  }

  public static void windowFillRect(Window win, Color c, int left, int top,
                                    int width, int height)
  {
    if (win instanceof GraphicsWindow)
      ((GraphicsWindow) win).fillRect(c, left, top, width, height);
  }

  public static void windowEraseRect(Window win, int left, int top,
                                     int width, int height)
  {
    if (win instanceof GraphicsWindow)
      ((GraphicsWindow) win).eraseRect(left, top, width, height);
  }

  public static void windowFlowBreak(Window win)
  {
    if (win instanceof TextBufferWindow)
      ((TextBufferWindow) win).flowBreak();
  }

  public static void windowMoveCursor(Window win, int x, int y)
  {
    if (win == null)
      nullRef("Glk.windowMoveCursor");
    else
      ((TextGridWindow) win).setCursor(x, y);
  }

  public static void setHyperlink(int val)
  {
    setHyperlinkStream(CURRENT_STREAM, val);
  }

  public static void setHyperlinkStream(Stream s, int val)
  {
    if (s == null)
      nullRef("Glk.setHyperlinkStream");
    else
      s.setHyperlink(val);
  }

  public static void requestHyperlinkEvent(Window w)
  {
    if (w instanceof TextBufferWindow)
      ((TextBufferWindow) w).requestHyperlinkInput(new GlkHyperConsumer(w));
    else if (w instanceof TextGridWindow)
      ((TextGridWindow) w).requestHyperlinkInput(new GlkHyperConsumer(w));
  }

  public static void cancelHyperlinkEvent(Window w)
  {
    if (w instanceof TextBufferWindow)
      ((TextBufferWindow) w).cancelHyperlinkInput();
    else if (w instanceof TextGridWindow)
      ((TextGridWindow) w).cancelHyperlinkInput();
  }

  public static void select(GlkEvent e)
  {
    long cur = 0l;
    GlkEvent ev = null;
    boolean done = false;

    synchronized(EVENT_QUEUE)
    {
      if (Window.root != null)
        Window.root.doLayout();

      while (!done)
      {
        if (!EVENT_QUEUE.isEmpty())
        {
          ev = (GlkEvent) EVENT_QUEUE.removeFirst();
          if (ev != null)
            done = true;
        }
        else if (TIMER > 0 && 
                 (cur = System.currentTimeMillis()) - TIMESTAMP >= TIMER)
        {
          e.type = EVTYPE_TIMER;
          e.win = null;
          e.val1 = 0;
          e.val2 = 0;
          TIMESTAMP = cur;
          done = true;
        }
        else
        {
          try {
            if (TIMER > 0)
              EVENT_QUEUE.wait(TIMER - (cur - TIMESTAMP));
            else
              EVENT_QUEUE.wait();
          } catch(InterruptedException ex) {}
        }
      }
      if (ev != null)
      {
        e.type = ev.type;
        e.win = ev.win;
        e.val1 = ev.val1;
        e.val2 = ev.val2;
      }
    }
  }

  public static void selectPoll(GlkEvent e)
  {
    long cur;
    GlkEvent ev = null;
    ListIterator li;

    synchronized(EVENT_QUEUE)
    {
      if (Window.root != null)
        Window.root.doLayout();

      li = EVENT_QUEUE.listIterator();
      while (li.hasNext())
      {
        ev = (GlkEvent) li.next();
        if (ev.type == EVTYPE_TIMER || ev.type == EVTYPE_ARRANGE || 
            ev.type == EVTYPE_SOUND_NOTIFY)
        {
          li.remove();
          e.type = ev.type;
          e.win = ev.win;
          e.val1 = ev.val1;
          e.val2 = ev.val2;
          break;
        }
      }
      if (TIMER > 0)
      {
        cur = System.currentTimeMillis();
        if ((cur - TIMESTAMP) >= TIMER)
        {
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

  public static void addEvent(GlkEvent e)
  {
    synchronized(EVENT_QUEUE)
    {
      EVENT_QUEUE.addLast(e);
      EVENT_QUEUE.notifyAll();
    }
  }

  public static Image getImage(int id, int xscale, int yscale)
  {
    ImageCacheNode n = null;
    Image img = null;
    int nodes = IMAGE_CACHE.size();
    ListIterator it = IMAGE_CACHE.listIterator(nodes);

    if (it.hasPrevious())
    {
      n = (ImageCacheNode) it.previous();

      while (n.id != id && it.hasPrevious())
        n = (ImageCacheNode) it.previous();
    }

    if (n != null && n.id == id)
    {
      if (xscale >= 0)
      {
        boolean found = false;

        if (n.scaled != null)
        {
          img = n.scaled;
          found |= (img.getWidth(Window.root.panel) == xscale &&
                    img.getHeight(Window.root.panel) == yscale);
        }
        
        if (!found)
        {
          img = n.normal.getScaledInstance(xscale, yscale, Image.SCALE_SMOOTH);
          try {
            TRACKER.addImage(img, id);
            TRACKER.waitForID(id);
            TRACKER.removeImage(img);
          }
          catch (InterruptedException eI)
          {
            eI.printStackTrace();
          }
          n.scaled = img;
        }
        return img;
      }
      else
      {
        return n.normal;
      }
    }

    try {
      BlorbFile.Chunk chunk = BLORB_FILE.getByUsage(BlorbFile.PICT, id);
      if (chunk == null)
        return null;

      if (BlorbFile.PNG.equals(chunk.getDataType()))
      {
        img = Toolkit.getDefaultToolkit().createImage(new PngImage(chunk.getData()));
      }
      else
      {
        byte[] arr = Bytes.getBytes(chunk.getData());
        img = Toolkit.getDefaultToolkit().createImage(arr);
      }

      TRACKER.addImage(img, id);
      TRACKER.waitForID(id);
      TRACKER.removeImage(img);

      n = new ImageCacheNode();
      n.id = id;
      n.normal = img;
      if (nodes == 20)
        IMAGE_CACHE.removeFirst();
      IMAGE_CACHE.add(n);

      if (xscale >= 0)
      {
        img = img.getScaledInstance(xscale, yscale, Image.SCALE_SMOOTH);
        TRACKER.addImage(img, id);
        TRACKER.waitForID(id);
        TRACKER.removeImage(img);
        n.scaled = img;
      }

      return img;
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  public static int colorToInt(Color c)
  {
    return ((c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue());
  }

  public static Color intToColor(int i)
  {
    int iRed = (i >>> 16) & 0xff;
    int iGreen = (i >>> 8) & 0xff;
    int iBlue = i & 0xff;
    return new Color(iRed, iGreen, iBlue);
  }

  static void nullRef(String func)
  {
    switch (STRICTNESS)
    {
    case STRICTNESS_DIE:
      throw new NullPointerException("Invalid object reference: " + func);
    case STRICTNESS_WARN:
      if (TextBufferWindow.MORE_CALLBACK != null)
        TextBufferWindow.MORE_CALLBACK.callback(new JLabel("Illegal obj ref: " + func, SwingConstants.LEFT));
      else
        JOptionPane.showMessageDialog((Frame) Window.FRAME, 
                                      "Warning: the program has illegally " +
                                      "referenced a null object in the " +
                                      "function '" + func + "'.", 
                                      "Null object reference",
                                      JOptionPane.ERROR_MESSAGE);
      break;
    default:
      // NOOP
    }
  }

  static class ImageCacheNode
  {
    int id;
    Image normal;
    Image scaled;
  }


  static class GlkHyperConsumer implements HyperlinkInputConsumer
  {
    Window w;

    GlkHyperConsumer(Window win)
    {
      w = win;
    }

    public void consume(int val)
    {
      GlkEvent e = new GlkEvent();
      e.type = EVTYPE_HYPERLINK;
      e.win = w;
      e.val1 = val;
      e.val2 = 0;

      addEvent(e);
    }
  }

  static class GlkCharConsumer implements CharInputConsumer
  {
    Window w;

    GlkCharConsumer(Window win)
    {
      w = win;
    }

    public void consume(java.awt.event.KeyEvent e)
    {
      GlkEvent ev = new GlkEvent();
      ev.type = EVTYPE_CHAR_INPUT;
      ev.win = w;
      ev.val1 = e.getKeyChar();
      ev.val2 = 0;

      switch (ev.val1)
      {
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
        switch (e.getKeyCode())
        {
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

      addEvent(ev);
    }
  }

  static class GlkLineConsumer implements LineInputConsumer
  {
    Window w;
    ByteBuffer b;
    boolean unicode;

    GlkLineConsumer(Window win, ByteBuffer buf, boolean unicode)
    {
      w = win;
      b = buf;
      this.unicode = unicode;
    }

    public void consume(String s)
    {
      GlkEvent ev = new GlkEvent();
      cancel(s);
      ev.type = EVTYPE_LINE_INPUT;
      ev.win = w;
      ev.val1 = s.length();
      ev.val2 = 0;
      addEvent(ev);
    }

    public void cancel(String s)
    {
      int l = s.length();
      if (unicode) {
          //fixme does not handle astral plane
          for (int i = 0; i < l; i++)
              b.putInt(i*4, s.charAt(i));
      } else {
          for (int i = 0; i < l; i++)
              b.put(i, (byte) s.charAt(i));
      }
    }
  }

  static class GlkMouseConsumer implements MouseInputConsumer
  {
    Window w;

    GlkMouseConsumer(Window win)
    {
      w = win;
    }

    public void consume(int x, int y)
    {
      GlkEvent e = new GlkEvent();
      e.type = EVTYPE_MOUSE_INPUT;
      e.win = w;
      e.val1 = x;
      e.val2 = y;
      addEvent(e);
    }
  }

  static final class HashCodeComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      return o1.hashCode() - o2.hashCode();
    }

    public boolean equals(Object o)
    {
      return o == this;
    }
  }
}
