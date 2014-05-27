package org.p2c2e.zing;

import java.util.*;
import java.awt.*;
import java.awt.font.*;
import javax.swing.*;
import java.util.prefs.*;


public class Style implements Cloneable
{
  static boolean USE_HINTS = true;
  static TreeMap GRID_STYLES = new TreeMap();
  static TreeMap BUFFER_STYLES = new TreeMap();
  static char[] MONO_TEST_ARRAY = {'m', 'i'};


  public static final int LEFT_FLUSH = 0;
  public static final int RIGHT_FLUSH = 3;
  public static final int CENTERED = 2;
  public static final int LEFT_RIGHT_FLUSH = 1;

  public String name;
  public String family;
  public boolean isOblique;
  public boolean isUnderlined;
  public int size;
  public Float weight;
  public int leftIndent;
  public int rightIndent;
  public int parIndent;
  public int justification;
  public Color textColor;
  public Color backColor;

  HashMap map;

  boolean isMonospace;
  boolean isHyperlinked;

  public Style(FontRenderContext frc, String name,
               String fam, int s, Float w, boolean oblique, boolean underline,
               int l, int r, int p, int just, Color t, Color b)
  {
    this.name = name;
    family = fam;
    size = s;
    weight = w;
    isOblique = oblique;
    isUnderlined = underline;
    leftIndent = l;
    rightIndent = r;
    parIndent = p;
    justification = just;
    textColor = t;
    backColor = b;

    Font testfont = new Font(getMap());
    double w1 = testfont.getStringBounds("m", frc).getWidth();
    double w2 = testfont.getStringBounds("i", frc).getWidth();
    isMonospace = (w1 == w2);

    isHyperlinked = false;
  }

  public Object clone()
  {
    Style c;
    try 
    {
      c = (Style) super.clone();
      c.name = name;
      c.family = family;
      c.size = size;
      c.weight = weight;
      c.isOblique = isOblique;
      c.isUnderlined = isUnderlined;
      c.leftIndent = leftIndent;
      c.rightIndent = rightIndent;
      c.parIndent = parIndent;
      c.justification = justification;
      c.textColor = textColor;
      c.backColor = backColor;
      c.isMonospace = isMonospace;

      // don't copy the map, since hinting may change it
      c.map = null;
      // don't copy hyperlink flag
      c.isHyperlinked = false;

      return c;
    }
    catch (CloneNotSupportedException e)
    {
      return null;
    }
  }

  public boolean isMonospace()
  {
    return isMonospace;
  }

  public Map getMap()
  {
    if (map == null)
      createMap();

    return map;
  }

  public Style getHyperlinked()
  {
    Style s = (Style) clone();
    s.textColor = Color.blue;
    s.isUnderlined = true;
    s.isHyperlinked = true;
    return s;
  }

  public static boolean usingHints()
  {
    return USE_HINTS;
  }

  public static void addStyle(Style s, int winType)
  {
    if (winType == Window.TEXT_GRID)
      GRID_STYLES.put(s.name, s);
    else
      BUFFER_STYLES.put(s.name, s);
  }

  public static Style getStyle(String name, int winType)
  {
    if (winType == Window.TEXT_GRID || winType == Glk.WINTYPE_ALL_TYPES)
      return (Style) GRID_STYLES.get(name);
    else
      return (Style) BUFFER_STYLES.get(name);
  }

  public static void useHints(boolean useHints)
  {
    if (useHints != USE_HINTS)
    {
      USE_HINTS = useHints;

      if (Window.root != null)
      {
        Window.root.restyle(USE_HINTS);
        LameFocusManager.rootRearrange();
        Window.root.panel.repaint();
      }

      try {
        Preferences stylep = 
          Preferences.userRoot().node("/org/p2c2e/zing/style");
        stylep.putBoolean("use-hints", USE_HINTS);
        stylep.flush();
      }
      catch (BackingStoreException e) {
        e.printStackTrace();
      }
    }
  }

  public static void apply()
  {
    if (Window.root != null)
    {
      try
      {
        Preferences gridp = 
          Preferences.userRoot().node("/org/p2c2e/zing/style/grid");
        Preferences bufp = 
          Preferences.userRoot().node("/org/p2c2e/zing/style/buffer");      
        
        for (int i = 0; i < Glk.STYLE_NUMSTYLES; i++)
        {
          Style s = getStyle(Glk.STYLES[i], Glk.WINTYPE_TEXT_BUFFER);
          saveStyle(bufp, s);
        
          s = getStyle(Glk.STYLES[i], Glk.WINTYPE_TEXT_GRID);

          saveStyle(gridp, s);
        }
      }
      catch(BackingStoreException ex)
      {
        ex.printStackTrace();
      }

      Window.root.restyle(USE_HINTS);
      LameFocusManager.rootRearrange();
      Window.root.panel.repaint();
    }
  }

  private static void saveStyle(Preferences p, Style s) 
    throws BackingStoreException
  {
    p = p.node(s.name);
    p.put("typeface", s.family);
    p.putInt("font-size", s.size);
    p.putFloat("font-weight", s.weight.floatValue());
    p.putBoolean("font-italic", s.isOblique);
    p.putBoolean("font-underline", s.isUnderlined);
    p.putInt("left-indent", s.leftIndent);
    p.putInt("right-indent", s.rightIndent);
    p.putInt("paragraph-indent", s.parIndent);
    p.putInt("justification", s.justification);
    p.putInt("text-color", Glk.colorToInt(s.textColor));
    p.putInt("back-color", Glk.colorToInt(s.backColor));
    p.flush();
  }

  private void createMap()
  {
    map = new HashMap();

    map.put(TextAttribute.FAMILY, family);
    map.put(TextAttribute.POSTURE, (isOblique) 
            ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
    if (isUnderlined)
      map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
    map.put(TextAttribute.SIZE, new Float((float) size));
    map.put(TextAttribute.WEIGHT, weight);
    map.put(TextAttribute.BACKGROUND, backColor);
    map.put(TextAttribute.FOREGROUND, textColor);
  }

  static void setupStyles(RootPaneContainer f)
  {
    FontRenderContext frc = ((Graphics2D) f.getContentPane().getGraphics()).getFontRenderContext();

    Preferences stylep = Preferences.userRoot().node("/org/p2c2e/zing/style");
    Preferences gridp = stylep.node("grid");
    Preferences bufp = stylep.node("buffer");

    USE_HINTS = stylep.getBoolean("use-hints", true);

    for (int i = 0; i < Glk.STYLE_NUMSTYLES; i++)
    {
      Style.addStyle(constructStyle(frc, gridp, i, Glk.WINTYPE_TEXT_GRID), 
                      Glk.WINTYPE_TEXT_GRID); 
      Style.addStyle(constructStyle(frc, bufp, i, Glk.WINTYPE_TEXT_BUFFER), 
                      Glk.WINTYPE_TEXT_BUFFER); 
    }
  }

  static Style constructStyle(FontRenderContext frc, 
                              Preferences p, int i, int type)
  {
    String stName = Glk.STYLES[i];
    p = p.node(stName);

    String stFam;
    int iSize;

    if (Window.OVERRIDE_PROPORTIONAL_FONT &&
        type == Glk.WINTYPE_TEXT_BUFFER &&
        i != Glk.STYLE_PREFORMATTED)
      stFam = Window.DEFAULT_PROPORTIONAL_FONT;
    else if (Window.OVERRIDE_FIXED_FONT &&
             (type == Glk.WINTYPE_TEXT_GRID || i == Glk.STYLE_PREFORMATTED))
      stFam = Window.DEFAULT_FIXED_FONT;
    else
      stFam = p.get("typeface", 
                    (type == Glk.WINTYPE_TEXT_GRID || 
                     i == Glk.STYLE_PREFORMATTED) 
                    ? Window.DEFAULT_FIXED_FONT
                    : Window.DEFAULT_PROPORTIONAL_FONT);

    if (Window.OVERRIDE_PROP_FONT_SIZE && 
        type == Glk.WINTYPE_TEXT_BUFFER && i != Glk.STYLE_PREFORMATTED)
      iSize = Window.DEFAULT_PROP_FONT_SIZE;
    else if (Window.OVERRIDE_FIXED_FONT_SIZE &&
             (type == Glk.WINTYPE_TEXT_GRID || i == Glk.STYLE_PREFORMATTED))
      iSize = Window.DEFAULT_FIXED_FONT_SIZE;
    else
      iSize = p.getInt("font-size", 
                       (type == Glk.WINTYPE_TEXT_GRID)
                       ? Window.DEFAULT_FIXED_FONT_SIZE
                       : Window.DEFAULT_PROP_FONT_SIZE);
    Float ofWeight = 
      new Float(p.getFloat("font-weight", 
                           (i == Glk.STYLE_INPUT || i == Glk.STYLE_SUBHEADER)
                           ? TextAttribute.WEIGHT_BOLD.floatValue()
                           : TextAttribute.WEIGHT_REGULAR.floatValue()));
    
    boolean bItalic = p.getBoolean("font-italic", 
                                   (i == Glk.STYLE_EMPHASIZED ||
                                    (type == Glk.WINTYPE_TEXT_GRID &&
                                     (i == Glk.STYLE_ALERT || 
                                      i == Glk.STYLE_NOTE))));
    boolean bUnderlined = p.getBoolean("font-underline", false);
    int iLeft = p.getInt("left-indent", 
                         (type == Glk.WINTYPE_TEXT_BUFFER &&
                          i == Glk.STYLE_BLOCKQUOTE) ? 2 : 0);
    int iRight = p.getInt("right-indent", 
                          (type == Glk.WINTYPE_TEXT_BUFFER &&
                           i == Glk.STYLE_BLOCKQUOTE) ? 2 : 0);
    int iPar = p.getInt("paragraph-indent", 
                        (type == Glk.WINTYPE_TEXT_BUFFER &&
                         (i != Glk.STYLE_HEADER && i != Glk.STYLE_SUBHEADER &&
                          i != Glk.STYLE_PREFORMATTED &&
                          i != Glk.STYLE_BLOCKQUOTE &&
                          i != Glk.STYLE_INPUT)) ? 1 : 0);
    int iJust = p.getInt("justification", 
                         (type == Glk.WINTYPE_TEXT_BUFFER &&
                          (i != Glk.STYLE_HEADER && i != Glk.STYLE_SUBHEADER &&
                           i != Glk.STYLE_PREFORMATTED &&
                           i != Glk.STYLE_INPUT)) 
                         ? Style.LEFT_RIGHT_FLUSH : Style.LEFT_FLUSH);

    Color cText;
    int iText = Glk.colorToInt(Color.black);
    if (type == Glk.WINTYPE_TEXT_GRID && i == Glk.STYLE_ALERT)
    {
      iText = Glk.colorToInt(Color.red);
    }
    else if (type == Glk.WINTYPE_TEXT_BUFFER)
    {
      if (i == Glk.STYLE_ALERT)
        iText = Glk.colorToInt(Color.red);
      else if (i == Glk.STYLE_NOTE)
        iText = Glk.colorToInt(Color.cyan);
    }

    cText = Glk.intToColor(p.getInt("text-color", iText));

    Color cBack = Glk.intToColor(p.getInt("back-color", 0x00ffffff));
    
    return new Style(frc, stName, stFam, iSize, ofWeight, bItalic, bUnderlined,
                     iLeft, iRight, iPar, iJust, cText, cBack);
  }

}
