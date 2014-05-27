package org.p2c2e.zing;

import java.util.*;

public class StyleHints implements Cloneable
{
  static TreeMap BUFFER_HINTS = new TreeMap();
  static TreeMap GRID_HINTS = new TreeMap();

  public static final int LEFT_INDENT = 0;
  public static final int RIGHT_INDENT = 10;
  public static final int PAR_INDENT = 1;
  public static final int JUSTIFICATION = 2;
  public static final int SIZE = 3;
  public static final int WEIGHT = 4;
  public static final int OBLIQUE = 5;
  public static final int PROPORTIONAL = 6;
  public static final int TEXT_COLOR = 7;
  public static final int BACK_COLOR = 8;
  public static final int REVERSE_COLOR = 9;
  public static final int NUM_HINTS = 11;


  public Integer[] data = new Integer[NUM_HINTS];

  StyleHints()
  {
    // NOOP: we'll set the attributes independently
  }

  public Object clone()
  {
    StyleHints cl = null;

    try
    {
      cl = (StyleHints) super.clone();
      cl.data = new Integer[NUM_HINTS];
      System.arraycopy(data, 0, cl.data, 0, NUM_HINTS);
    }
    catch(CloneNotSupportedException e)
    {

    }

    return cl;
  }

  public static void clearAll()
  {
    BUFFER_HINTS.clear();
    GRID_HINTS.clear();
  }

  public static TreeMap getHints(int wintype)
  {
    Map.Entry e;
    StyleHints h;
    Iterator it;
    TreeMap mNew;
    TreeMap m = null;

    if (wintype == Window.TEXT_BUFFER)
      m = BUFFER_HINTS;
    else if (wintype == Window.TEXT_GRID)
      m = GRID_HINTS;

    if (m == null)
      return null;

    mNew = new TreeMap();
    it = m.entrySet().iterator();

    while (it.hasNext())
    {
      e = (Map.Entry) it.next();
      h = (StyleHints) e.getValue();
      mNew.put(e.getKey(), h.clone());
    }

    return mNew;
  }

  public static void setHint(int winType, Style style, int hint, int val)
  {
    StyleHints hints = getOrCreateHints(winType, style);
 
    hints.data[hint] = new Integer(val);
  }

  public static void clearHint(int winType, Style style, int hint)
  {
    StyleHints hints = getOrCreateHints(winType, style);
    
    hints.data[hint] = null;
  }

  private static StyleHints getOrCreateHints(int winType, Style style)
  {
    StyleHints hints = null;

    if (winType == Window.TEXT_BUFFER || winType == Glk.WINTYPE_ALL_TYPES)
    {
      hints = (StyleHints) BUFFER_HINTS.get(style.name);

      if (hints == null)
      {
        hints = new StyleHints();
        BUFFER_HINTS.put(style.name, hints);
      }
    }
    if (winType == Window.TEXT_GRID || winType == Glk.WINTYPE_ALL_TYPES)
    {
      hints = (StyleHints) GRID_HINTS.get(style.name);

      if (hints == null)
      {
        hints = new StyleHints();
        GRID_HINTS.put(style.name, hints);
      }
    }

    return hints;
  }
}
