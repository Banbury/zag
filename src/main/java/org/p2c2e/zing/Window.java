package org.p2c2e.zing;

import java.nio.*;
import java.util.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class Window implements MouseListener, Comparable
{
  static String DEFAULT_PROPORTIONAL_FONT = "Serif";
  static String DEFAULT_FIXED_FONT = "Monospaced";
  static int DEFAULT_PROP_FONT_SIZE = 14;
  static int DEFAULT_FIXED_FONT_SIZE = 14;
  static boolean OVERRIDE_PROPORTIONAL_FONT = false;
  static boolean OVERRIDE_FIXED_FONT = false;
  static boolean OVERRIDE_PROP_FONT_SIZE = false;
  static boolean OVERRIDE_FIXED_FONT_SIZE = false;

  static final Color HIGHLIGHT_COLOR = new Color(0x99, 0x99, 0xcc);
  static final Color HIGHLIGHT_SHADOW = new Color(0x66, 0x66, 0x99);

  public static final int BLANK = 2;
  public static final int PAIR = 1;
  public static final int TEXT_BUFFER = 3;
  public static final int TEXT_GRID = 4;
  public static final int GRAPHICS = 5;

  public static final int LEFT = 0;
  public static final int RIGHT = 1;
  public static final int ABOVE = 2;
  public static final int BELOW = 3;
  
  public static final int FIXED = 0x10;
  public static final int PROPORTIONAL = 0x20;

  static RootPaneContainer FRAME;
  public static Window root;

  FontRenderContext frc;
  TreeMap hintedStyles;
  TreeMap mHints;
  Style curStyle;
  PairWindow parent;
  Rectangle bbox;
  JComponent panel;

  Stream stream;
  Stream echo;

  public Window(FontRenderContext context)
  {
    frc = context;
    bbox = new Rectangle();
    mHints = StyleHints.getHints(getWindowType());
    hintedStyles = new TreeMap();
    createHintedStyles(getStyleMap(), Style.USE_HINTS);
    curStyle = (Style) hintedStyles.get("normal");
    stream = new Stream.WindowStream(this);
  }

  protected int getWindowType()
  {
    return 0;
  }

  protected void restyle(boolean useHints)
  {
    createHintedStyles(getStyleMap(), useHints);
    if (curStyle != null)
      curStyle = (Style) hintedStyles.get(curStyle.name);
  }

  public Stream getStream()
  {
    return stream;
  }

  public Stream getEchoStream()
  {
    return echo;
  }

  public void setEchoStream(Stream s)
  {
    if (s != stream)
      echo = s;
  }

  public int getWindowWidth()
  {
    if (panel != null)
      return panel.getWidth();
    return 0;
  }

  public int getWindowHeight()
  {
    if (panel != null)
      return panel.getHeight();
    return 0;
  }

  protected void doLayout()
  {
    panel.repaint();
  }

  protected boolean isRequestingKeyboardInput()
  {
    return false;
  }

  protected void setStyle(Style style)
  {
    if (style != null)
      curStyle = style;
  }

  protected void putChar(int c)
  {
    // NOOP
  }

  protected void putCharUni(int c)
  {
    // NOOP
  }

  protected void putString(String s)
  {
    int len = s.length();
    for (int i = 0; i < len; i++)
      putChar(s.charAt(i));
  }

  protected void clear()
  {

  }

  protected boolean measureStyle(String stName, int hint, Int b)
  {
    return false;
  }
  
  protected void createHintedStyles(Map styles, boolean useHints)
  {
    Style s;
    Style hs;
    Iterator it = styles.values().iterator();

    while (it.hasNext())
    {
      s = (Style) it.next();
      hs = (useHints) ? createHintedStyle(s) : s;
      hintedStyles.put(hs.name, hs);
    }
  }

  // Each subclass of Window should implement this.  The idea is that Grid 
  // windows, for example, should not honor all hints (e.g. proportional fonts)
  // that Buffer windows should.
  protected Style createHintedStyle(Style style)
  {
    return style;
  }

  protected Map getStyleMap()
  {
    return Collections.EMPTY_MAP;
  }

  public PairWindow getParent()
  {
    return parent;
  }

  public Window getSibling()
  {
    if (parent == null)
      return null;
    if (parent.first == this)
      return parent.second;
    else
      return parent.first;
  }

  public Stream.Result closeStream()
  {
    return stream.close();
  }

  public static Stream.Result close(Window w)
  {
    PairWindow grand;
    Window sibling;
    PairWindow p = w.getParent();

    if (p == null)
    {
      FRAME.getContentPane().remove(root.panel);
      root = null;
    }
    else
    {
      grand = p.getParent();
      sibling = w.getSibling();

      if (grand == null)
      {
        FRAME.getContentPane().remove(root.panel);
        FRAME.getContentPane().add(sibling.panel, BorderLayout.CENTER);
        root = sibling;
        sibling.parent = null;
      }
      else
      {
        if (grand.first == p)
          grand.set(sibling, grand.second);
        else
          grand.set(grand.first, sibling);

        if (grand.key == w)
          grand.key = null;
      }
    }

    if (root != null)
    {
      LameFocusManager.rootRearrange();
      root.panel.repaint();
    }

    return w.closeStream();
  }

  public static Window split(Window src, int method, int size, 
                             boolean border, int winType)
  {
    PairWindow oldPair;
    PairWindow newPair = null;
    Window w;
    FontRenderContext context = 
      ((Graphics2D) FRAME.getContentPane().getGraphics()).getFontRenderContext();

    switch(winType)
    {
    case BLANK:
      w = new BlankWindow(context);
      break;
    case PAIR:
      throw new RuntimeException("Pair windows cannot be leaf nodes.");
    case TEXT_BUFFER:
      w = new TextBufferWindow(context);
      break;
    case TEXT_GRID:
      w = new TextGridWindow(context);
      break;
    case GRAPHICS:
      w = new GraphicsWindow(context);
      break;
    default:
      throw new RuntimeException("Attempt to create unknown type (" +
                                 winType + ") of window.");
    }

    if (border)
      w.panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    if (root == null)
    {
      root = w;
      w.panel.setPreferredSize(FRAME.getContentPane().getSize());
      FRAME.getContentPane().add(root.panel, BorderLayout.CENTER);
    }
    else
    {
      
      newPair = new PairWindow(context, method & 0x0f);
      oldPair = src.parent;
      newPair.parent = oldPair;
      newPair.key = w;
      newPair.keySize = size;
      //      newPair.borderWidth = border;
      
      // are we splitting the root window?
      if (oldPair == null)
      {
        root = newPair;
        FRAME.getContentPane().remove(src.panel);
        FRAME.getContentPane().add(newPair.panel, BorderLayout.CENTER);
      }
      else
      {
        oldPair.replace(src, newPair);
      }

      if ((method & 0x0f) == LEFT || (method & 0x0f) == ABOVE)
        newPair.set(w, src);
      else
        newPair.set(src, w);

      if ((method & FIXED) != 0)
        newPair.keySizeType = FIXED;
      else
        newPair.keySizeType = PROPORTIONAL;
    }

    LameFocusManager.rootRearrange();
    root.panel.repaint();
    LameFocusManager.requestFocus(w);

    return w;
  }

  protected abstract void rearrange(Rectangle r);

  protected boolean isFocusStealable()
  {
    return true;
  }

  protected void focusHighlight()
  {
    Border b = panel.getBorder();
    if (b != null)
      panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, 
                                                      HIGHLIGHT_COLOR,
                                                      HIGHLIGHT_SHADOW));
  }

  protected void unfocusHighlight()
  {
    Border b = panel.getBorder();
    if (b != null)
      panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
  }

  // the rectangle acts as a constraint on the size of the window
  protected int getSplit(int size, int axis)
  {
    return 0;
  }


  protected boolean requestMouseInput(MouseInputConsumer c)
  {
    return false;
  }

  protected boolean requestCharacterInput(CharInputConsumer c)
  {
    return false;
  }

  protected boolean requestLineInput(LineInputConsumer c, String init, int max)
  {
    return false;
  }

  protected void cancelMouseInput()
  {

  }

  protected void cancelCharacterInput()
  {

  }

  protected String cancelLineInput()
  {
    return null;
  }


  public void mouseClicked(MouseEvent e)
  {
    if (LameFocusManager.FOCUSED_WINDOW != this)
    {
      LameFocusManager.grabFocus(this);
    }
  }

  public void mouseEntered(MouseEvent e)
  {

  }

  public void mouseExited(MouseEvent e)
  {

  }

  public void mousePressed(MouseEvent e)
  {

  }

  public void mouseReleased(MouseEvent e)
  {

  }

  protected void handleKey(KeyEvent e)
  {

  }

  public int compareTo(Object o)
  {
    return hashCode() - o.hashCode();
  }
}
