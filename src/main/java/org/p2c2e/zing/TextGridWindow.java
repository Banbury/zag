package org.p2c2e.zing;

import java.nio.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.event.*;
import java.text.*;

public class TextGridWindow extends Window
{
  private static final int MARGIN = 5;

  int rows, cols, x, y;
  int colSize;
  int lineHeight;
  int ascending;
  Square[][] grid;
  boolean[] dirty;
  boolean drawBackground;
  Style nonHyper;
  Style oldStyle;
  HyperlinkInputConsumer hyperConsumer;
  CharInputConsumer charConsumer;
  LineInputConsumer lineConsumer;
  MouseInputConsumer mouseConsumer;
  int iLineInputStartX, iLineInputStartY, maxLineInput, hyperVal;
  BufferedImage bi;
  Graphics2D g2d;

  public TextGridWindow(FontRenderContext context)
  {
    super(context);
    panel = new TextGridPanel();
    panel.addMouseListener(this);
    grid = new Square[0][0];
    dirty = new boolean[0];
    drawBackground = true;
  }

  protected void doLayout()
  {
    if (drawBackground)
    {
      panel.repaint();
    }
    else
    {
      for (int i = 0; i < dirty.length; i++)
      {
        if (dirty[i])
          panel.repaint();
      }
    }
  }

  protected int getWindowType()
  {
    return TEXT_GRID;
  }

  protected void restyle(boolean useHints)
  {
    super.restyle(useHints);

    if (nonHyper != null)
      nonHyper = (Style) hintedStyles.get(nonHyper.name);
    if (oldStyle != null)
    {
      Style oldRep = (Style) hintedStyles.get(oldStyle.name);
      oldStyle = (oldStyle.isHyperlinked) ? oldRep.getHyperlinked() : oldRep;
    }

    for (int i = 0; i < rows; i++)
    {
      for (int j = 0; j < cols; j++)
      {
        if (grid[i][j].s != null)
        {
          Style rep = (Style) hintedStyles.get(grid[i][j].s.name);
          grid[i][j].s = (grid[i][j].s.isHyperlinked) 
            ? rep.getHyperlinked() : rep;
        }
      }
    }

    Arrays.fill(dirty, true);
    drawBackground = true;
  }

  protected boolean isRequestingKeyboardInput()
  {
    return lineConsumer != null || charConsumer != null;
  }

  public void requestHyperlinkInput(HyperlinkInputConsumer hic)
  {
    if (hyperConsumer == null)
      hyperConsumer = hic;
  }

  public void cancelHyperlinkInput()
  {
    hyperConsumer = null;
  }

  public void setHyperlink(int val)
  {
    hyperVal = val;

    if (val != 0)
    {
      nonHyper = curStyle;
      curStyle = curStyle.getHyperlinked();
    }
    else if (nonHyper != null)
    {
      curStyle = nonHyper;
      nonHyper = null;
    }
  }

  public void setStyle(Style style)
  {
    if (nonHyper != null)
    {
      nonHyper = style;
      style = style.getHyperlinked();
    }

    super.setStyle(style);
  }

  public int getWindowWidth()
  {
    return cols;
  }

  public int getWindowHeight()
  {
    return rows;
  }

  protected void clear()
  {
    Square sq = new Square(' ', curStyle, 0);

    synchronized(grid)
    {
      for (int i = 0; i < rows; i++)
      {
        for (int j = 0; j < cols; j++)
        {
          grid[i][j] = sq;
        }
      }

      Arrays.fill(dirty, true);
      drawBackground = true;
      x = y = 0;
    }
  }

  public void setCursor(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  // This will be called by the window stream
  protected void putChar(int c)
  {
    if (c == '\n' && y < rows)
    {
      dirty[y] = true;
      x = 0;
      y++;
    }
    else if (x < cols && y < rows)
    {
      synchronized (grid)
      {
        Square sq = new Square(c, curStyle, hyperVal);
        grid[y][x++] = sq;
        dirty[y] = true;

        if (x >= cols)
        {
          x = 0;
          y++;
        }
      }
    }
  }

  public void putCharUni(int c) {
      putChar(c);
  }

  // If this is being called, then size represents a fixed size (i.e., 
  // the number of lines or columns that this component should fill)
  protected int getSplit(int size, int axis)
  {
    Style normal = (Style) hintedStyles.get("normal");

    return TextSplitMeasurer.getSplit(panel, size, axis, frc, normal, 
                                      Style.MONO_TEST_ARRAY) + 
      (2 * MARGIN);
  }

  // we need to set up the grid
  protected void rearrange(Rectangle r)
  {
    int width, height, rowSize, newRows, newCols, i, j;
    Square[][] newGrid;
    LineMetrics m;
    Style normal = (Style) hintedStyles.get("normal");
    Font normalFont = new Font(normal.getMap());
    Insets insets = panel.getInsets();

    bbox.x = r.x;
    bbox.y = r.y;
    bbox.width = r.width;
    bbox.height = r.height;

    // how many rows and cols can we fit in our rectangle?  we must remember 
    // to compensate for INSET, as well
    width = r.width - (insets.right + insets.left) - (2 * MARGIN);
    height = r.height - (insets.top + insets.bottom) - (2 * MARGIN);

    m = normalFont.getLineMetrics("Hag", frc);
    ascending = (int) m.getAscent();
    rowSize = (int) m.getHeight() + 2;
    colSize = (int) normalFont.getStringBounds(Style.MONO_TEST_ARRAY, 0, 1, frc).getWidth();
    //    colSize = (int) normalFont.createGlyphVector(frc, Style.MONO_TEST_ARRAY).getGlyphMetrics(0).getAdvance();

    newCols = width / colSize;
    newRows = height / rowSize;
    lineHeight = rowSize;
    newGrid = new Square[newRows][newCols];
    dirty = new boolean[newRows];

    synchronized(grid)
    {
      BufferedImage nbi = 
        new BufferedImage(Math.max(1, r.width), Math.max(1, r.height), BufferedImage.TYPE_INT_ARGB_PRE);
      Graphics2D ng2 = nbi.createGraphics();
      if (g2d != null)
        g2d.dispose();
      bi = nbi;
      g2d = ng2;

      /*
      Arrays.fill(dirty, true);
      drawBackground = true;
      */

      Square sq = new Square(' ', normal, 0);
      // fill the grid with spaces
      for (i = 0; i < newRows; i++)
        Arrays.fill(newGrid[i], sq);

      // now put the old info in newGrid
      for (i = 0; i < rows && i < newRows; i++)
      {
        for (j = 0; j < cols && j < newCols; j++)
          newGrid[i][j] = grid[i][j];
      }

      grid = newGrid;
      rows = newRows;
      cols = newCols;
    }
      
    panel.revalidate();
  }

  protected boolean measureStyle(String stName, int hint, Int b)
  {
    int result;
    Style unhinted = Style.getStyle(stName, TEXT_GRID);
    Style s = (Style) hintedStyles.get(stName);

    if (s != null)
    {
      switch(hint)
      {
      case Glk.STYLEHINT_INDENTATION:
        result = 0;
        break;
      case Glk.STYLEHINT_PARA_INDENTATION:
        result = 0;
        break;
      case Glk.STYLEHINT_JUSTIFICATION:
        result = 0;
        break;
      case Glk.STYLEHINT_SIZE:
        result = s.size;
        break;
      case Glk.STYLEHINT_WEIGHT:
        if (s.weight == TextAttribute.WEIGHT_BOLD ||
            s.weight == TextAttribute.WEIGHT_DEMIBOLD ||
            s.weight == TextAttribute.WEIGHT_EXTRABOLD ||
            s.weight == TextAttribute.WEIGHT_HEAVY)
          result = 1;
        else if (s.weight == TextAttribute.WEIGHT_REGULAR ||
                 s.weight == TextAttribute.WEIGHT_SEMIBOLD ||
                 s.weight == TextAttribute.WEIGHT_MEDIUM)
          result = 0;
        else
          result = -1;
        break;
      case Glk.STYLEHINT_OBLIQUE:
        result = (s.isOblique ? 1 : 0);
        break;
      case Glk.STYLEHINT_PROPORTIONAL:
        result = (s.isMonospace ? 0 : 1);
        break;
      case Glk.STYLEHINT_TEXT_COLOR:
        result = Glk.colorToInt(s.textColor);
        break;
      case Glk.STYLEHINT_BACK_COLOR:
        result = Glk.colorToInt(s.backColor);
        break;
      case Glk.STYLEHINT_REVERSE_COLOR:
        result = 
          ((unhinted.textColor == s.backColor && unhinted.backColor == s.textColor)
           ? 1 : 0);
        break;
      default:
        return false;
      }
      b.val = result;
      return true;
    }
    return false;
  }

  protected Map getStyleMap()
  {
    return Style.GRID_STYLES;
  }

  protected Style createHintedStyle(Style style)
  {
    Color tmp;
    int val;
    Style hintedStyle;
    StyleHints hints = (StyleHints) mHints.get(style.name);

    if (hints == null)
      return style;

    hintedStyle = (Style) style.clone();

    if (hints.data[StyleHints.SIZE] != null)
      hintedStyle.size += hints.data[StyleHints.SIZE].intValue();

    if (hints.data[StyleHints.WEIGHT] != null)
    {
      val = hints.data[StyleHints.WEIGHT].intValue();
      if (val == 0)
        hintedStyle.weight = TextAttribute.WEIGHT_REGULAR;
      else if (val < 0)
        hintedStyle.weight = TextAttribute.WEIGHT_LIGHT;
      else
        hintedStyle.weight = TextAttribute.WEIGHT_BOLD;
    }

    if (hints.data[StyleHints.OBLIQUE] != null)
    {
      val = hints.data[StyleHints.OBLIQUE].intValue();
      hintedStyle.isOblique = (val == 1);
    }

    if (hints.data[StyleHints.TEXT_COLOR] != null)
    {
      val = hints.data[StyleHints.TEXT_COLOR].intValue();
      hintedStyle.textColor = 
        new Color((val >> 16) & 0xff,
                  (val >> 8) & 0xff,
                  val & 0xff);
    }
    if (hints.data[StyleHints.BACK_COLOR] != null)
    {
      val = hints.data[StyleHints.BACK_COLOR].intValue();
      hintedStyle.backColor =
        new Color((val >> 16) & 0xff,
                  (val >> 8) & 0xff,
                  val & 0xff);
    }
    if (hints.data[StyleHints.REVERSE_COLOR] != null &&
        hints.data[StyleHints.REVERSE_COLOR].intValue() == 1)
    {
      tmp = hintedStyle.textColor;
      hintedStyle.textColor = hintedStyle.backColor;
      hintedStyle.backColor = tmp;
    }

    return hintedStyle;
  }

  protected synchronized boolean requestMouseInput(MouseInputConsumer mic)
  {
    if (mouseConsumer == null)
    {
      mouseConsumer = mic;
      return true;
    }
    return false;
  }

  public synchronized void cancelMouseInput()
  {
    mouseConsumer = null;
  }

  protected synchronized boolean requestCharacterInput(CharInputConsumer cic)
  {
    if (charConsumer != null || lineConsumer != null)
      return false;
      
    charConsumer = cic;
    LameFocusManager.requestFocus(this);
    return true;
  }

  public synchronized void cancelCharacterInput()
  {
    charConsumer = null;
  }

  protected synchronized boolean requestLineInput(LineInputConsumer lic, 
                                                  String initContents, int max)
  {
    if (charConsumer != null || lineConsumer != null)
      return false;

    lineConsumer = lic;
    iLineInputStartX = x;
    iLineInputStartY = y;
    maxLineInput = max;
    oldStyle = curStyle;
    curStyle = (Style) hintedStyles.get("input");
    LameFocusManager.requestFocus(this);
    if (initContents != null)
    {
      for (int i = 0; i < initContents.length() && 
             i < max && 
             i + iLineInputStartX < cols; i++)
        putChar(initContents.charAt(i));
    }
    if (y < rows)
      dirty[y] = true;

    return true;    
  }

  public synchronized String cancelLineInput()
  {
    if (lineConsumer == null)
      return "";

    int end;
    String s;
    StringBuffer sb = new StringBuffer();
    LineInputConsumer lic = lineConsumer;
    lineConsumer = null;
    curStyle = oldStyle;
    panel.repaint();

    end = (y == iLineInputStartY) ? x : cols;
    for (int i = iLineInputStartX; i < end; i++)
      sb.appendCodePoint(grid[iLineInputStartY][i].c);

    s = sb.toString();
    lic.cancel(s);
    return s;
  }

  protected synchronized void handleKey(KeyEvent e)
  {
    if (charConsumer != null)
    {
      CharInputConsumer cs = charConsumer;
      charConsumer = null;
      cs.consume(e);
    }
    else if (lineConsumer != null)
    {
      int iChar = (int) e.getKeyChar();
      if ((iChar >=32 && iChar < 127) || (iChar >= 160 && iChar < 256))
      {
        if (y == iLineInputStartY && (x - iLineInputStartX) < maxLineInput)
        {
          putChar((char) iChar);
        }
      }
      else if (iChar == 8 || iChar == 127)
      {
        if (y == iLineInputStartY && x > iLineInputStartX)
        {
          grid[y][x - 1].c = ' ';
          dirty[y] = true;
          setCursor(x - 1, y);
        }
        else if (y > iLineInputStartY)
        {
          grid[y - 1][cols - 1].c = ' ';
          dirty[y] = true;
          setCursor(cols - 1, y - 1);
        }
      }
      else if (iChar == 10 || iChar == 13)
      {
        int end;
        StringBuffer sb = new StringBuffer();
        LineInputConsumer ls = lineConsumer;
        lineConsumer = null;

        end = (y == iLineInputStartY) ? x : cols;
        putChar('\n');

        for (int i = iLineInputStartX; i < end; i++)
          sb.appendCodePoint(grid[iLineInputStartY][i].c);

        ls.consume(sb.toString());
        if (echo != null)
        {
          echo.putString(sb.toString());
          echo.putChar('\n');
        }
      }
      panel.repaint();
    }
  }

  public void mouseClicked(MouseEvent e)
  {
    Insets insets;
    int x, y;
    boolean consumed = false;

    if (LameFocusManager.FOCUSED_WINDOW != this)
    {
      LameFocusManager.grabFocus(this);
    }
    
    if (hyperConsumer != null || mouseConsumer != null)
    {
      insets = panel.getInsets();
      x = (e.getX() - insets.left) / colSize;
      y = (e.getY() - insets.top) / lineHeight;

      if (x < 0) x = 0;
      if (y < 0) y = 0;
      if (x >= cols) x = cols - 1;
      if (y >= rows) y = rows - 1;

      if (hyperConsumer != null && colSize > 0 && lineHeight > 0)
      {
        if (grid[y][x].hyper != 0)
        {
          HyperlinkInputConsumer hic = hyperConsumer;
          hyperConsumer = null;
          curStyle = nonHyper;
          nonHyper = null;
          hic.consume(grid[y][x].hyper);
          consumed = true;
        }
      }
        
      if (!consumed && mouseConsumer != null && colSize > 0 && lineHeight > 0)
      {
        mouseConsumer.consume(x, y);
      }
    }
  }

  class TextGridPanel extends JPanel
  {
    public TextGridPanel()
    {
      super();
      setDoubleBuffered(false);
    }
    
    public void invalidate()
    {
      synchronized(grid)
      {
        Arrays.fill(dirty, true);
        drawBackground = true;
        super.invalidate();
      }
    }

    public void paintComponent(Graphics g)
    {
      int x, y;
      int i, j, mark;
      AttributedString as;
      TextLayout layout;
      Style s;
      boolean wasDirty = false;
      StringBuffer sb = new StringBuffer();
      Graphics2D g2 = (Graphics2D) g;
      Style input = (Style) hintedStyles.get("input");
      Insets insets = panel.getInsets();

      if (drawBackground && bi != null)
      {
        Style normal = (Style) hintedStyles.get("normal");
        g2d.setColor(normal.backColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        drawBackground = false;
      }

      x = insets.left + MARGIN;
      y = insets.top + MARGIN + ascending;
      
      synchronized (grid)
      {
        for (i = 0; i < rows; i++)
        {
          if (dirty[i] && cols > 0)
          {
            wasDirty = true;
            sb.setLength(0);
            for (int k = 0; k < cols; k++)
              sb.appendCodePoint(grid[i][k].c);

            as = new AttributedString(sb.substring(0, sb.length()));
            j = mark = 0;
            s = grid[i][j++].s;
            while (j < cols)
            {
              if (grid[i][j].s != s)
              {
                as.addAttributes(s.getMap(), mark, j);
                mark = j;
                s = grid[i][j].s;
              }
              j++;
            }
            as.addAttributes(s.getMap(), mark, cols);

            layout = new TextLayout(as.getIterator(null), frc);
            layout.draw(g2d, (float) x, (float) y);
          }
          y += lineHeight;
        }

        Arrays.fill(dirty, false);

        if (lineConsumer != null && wasDirty)
        {
          g2d.setColor(input.textColor);
          g2d.fillRect(insets.left + MARGIN + 
                       (colSize * TextGridWindow.this.x),
                       insets.top + MARGIN + 
                       (lineHeight * TextGridWindow.this.y),
                       colSize, ascending);
        }
      }
      if (bi != null)
        g.drawImage(bi, 0, 0, this);
    }
  }

  static class Square
  {
    int c;
    Style s;
    int hyper;
    
    Square(int c, Style s, int h)
    {
      this.c = c;
      this.s = s;
      this.hyper = h;
    }
  }
  }
