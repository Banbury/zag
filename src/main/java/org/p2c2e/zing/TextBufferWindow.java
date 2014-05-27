package org.p2c2e.zing;

import java.nio.*;
import java.util.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;

public final class TextBufferWindow extends Window
{
  static ObjectCallback MORE_CALLBACK;

  static final int H_MARGIN = 15;
  static final int TOP_MARGIN = 5;
  static final int HISTORY_SIZE_LIMIT = 20;

  static final char[] MEASURE_ARR = new char[] {'0'};
  static final char INLINE_IMAGE = (char) 1;
  static final char FLOW_BREAK = (char) 2;

  public static final int INLINE_UP = 1;
  public static final int INLINE_DOWN = 2;
  public static final int INLINE_CENTER = 3;
  public static final int MARGIN_LEFT = 4;
  public static final int MARGIN_RIGHT = 5;
  

  // Since it is illegal to print control characters to the output stream, 
  // we can use those codes to represent special cases:  graphics, flow
  // breaks, and so forth.
  StringBuffer buffer;
  boolean lastLineDirty = false;

  // This maps positions in the buffer to style changes.
  TreeMap mStyles;
  boolean restyled;

  // This maps positions to graphics
  TreeMap mMarginGraphics;
  TreeMap mInlineGraphics;
  TreeMap mHyperlinks;

  TextBufferPanel view;

  int viewWidth = 0;

  LinkedList paragraphs;
  Line lastLineSeen = null;

  MediaTracker tracker;
  int imageNum = 0;

  CharInputConsumer charConsumer;
  LineInputConsumer lineConsumer;
  HyperlinkInputConsumer hyperConsumer;
  int iLineStartPos;
  int iLineCursorPos;
  int maxLineInput;
  int inputHistoryIndex;
  Style oldStyle;
  Style nonHyper;
  LinkedList inputHistory;

  public TextBufferWindow(FontRenderContext context)
  {
    super(context);
    buffer = new StringBuffer("\n");
    mStyles = new TreeMap();
    mMarginGraphics = new TreeMap();
    mInlineGraphics = new TreeMap();
    mHyperlinks = new TreeMap();
    paragraphs = new LinkedList();
    view = new TextBufferPanel();
    tracker = new MediaTracker(view);
    oldStyle = curStyle;
    mHyperlinks.put(new Integer(0), new Integer(0));
    // having the scrollbar always present makes calculating size constraints
    // much easier
    panel = new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    ((JScrollPane) panel).getVerticalScrollBar().setFocusable(false);
    ((JScrollPane) panel).setBorder(null);
    view.addMouseListener(this);
    inputHistory = new LinkedList();
  }

  protected int getWindowType()
  {
    return TEXT_BUFFER;
  }

  protected void restyle(boolean useHints)
  {
    super.restyle(useHints);

    if (nonHyper != null)
      nonHyper = (Style) hintedStyles.get(nonHyper.name);
    if (oldStyle != null)
    {
      Style oldRep = (Style) hintedStyles.get(oldStyle.name);
      if (oldStyle.isHyperlinked)
        oldStyle = oldRep.getHyperlinked();
      else
        oldStyle = oldRep;
    }

    Map.Entry e;
    TreeMap mNewStyles = new TreeMap();
    Iterator it = mStyles.entrySet().iterator();

    while (it.hasNext())
    {
      e = (Map.Entry) it.next();
      Integer pos = (Integer) e.getKey();
      Style s = (Style) e.getValue();
      Style rep = (Style) hintedStyles.get(s.name);

      mNewStyles.put(pos, ((s.isHyperlinked) ? rep.getHyperlinked() : rep));
    }
    
    mStyles = mNewStyles;
    restyled = true;
  }

  protected boolean isRequestingKeyboardInput()
  {
    return lineConsumer != null || charConsumer != null;
  }

  public int getWindowWidth()
  {
    int pixWid = viewWidth - (2 * H_MARGIN);
    Style n = (Style) hintedStyles.get("normal");
    int w = (int) 
      (new Font(n.getMap())).getStringBounds(MEASURE_ARR, 0, 1, frc).getWidth();
    return (pixWid / w);
  }

  public int getWindowHeight()
  {
    int pixHeight = ((JScrollPane) panel).getViewport().getHeight();
    Style n = (Style) hintedStyles.get("normal");    
    LineMetrics m = (new Font(n.getMap())).getLineMetrics("Hag", frc);
    return pixHeight / (int) m.getHeight();
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
    if (val != 0)
    {
      if (nonHyper == null)
      {
        Style nhs = curStyle;
        setStyle(curStyle.getHyperlinked());
        nonHyper = nhs;
      }
      mHyperlinks.put(new Integer(buffer.length()), new Integer(val));
    }
    else if (nonHyper != null)
    {
      Style nhs = nonHyper;
      nonHyper = null;
      setStyle(nhs);
      mHyperlinks.put(new Integer(buffer.length()), new Integer(val));
    }
  }

  protected void doLayout()
  {
      if (lastLineDirty)
      {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              doPaint(layoutLastLine(buffer.length()));
            }
          });
      }
      else
      {
        doPaint(lastLineSeen);
      }
  }

  private final void doPaint(Line l)
  {
    if (l != null)
      view.repaint(0L, 0, l.top - 3, view.getWidth(), 
                   view.getHeight() - (l.top - 3));
    else
      view.repaint();
  }

  protected void putChar(int c)
  {
      buffer.appendCodePoint(c);
      lastLineDirty = true;
  }

  protected void putCharUni(int c)
  {
      buffer.appendCodePoint(c);
      lastLineDirty = true;
  }

  protected void putString(String s)
  {
      if (s.length() > 0)
      {
        buffer.append(s);
        lastLineDirty = true;        
      }
  }

  protected void clear()
  {
      buffer = new StringBuffer("\n");
      lastLineSeen = null;
      mMarginGraphics.clear();
      mInlineGraphics.clear();
      mHyperlinks.clear();
      mHyperlinks.put(new Integer(0), new Integer(0));
      synchronized (paragraphs)
      {
        paragraphs.clear();
      }
      mStyles.clear();
      setStyle(curStyle);
      view.setPreferredSize(view.preferredViewportSize);
      layout(null, null);
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
        result = (int) (((float) s.leftIndent * 0.03) * 
                        (float) (viewWidth - (2 * H_MARGIN)));
        break;
      case Glk.STYLEHINT_PARA_INDENTATION:
        result = (int) (((float) s.parIndent * 0.03) * 
                        (float) (viewWidth - (2 * H_MARGIN)));
        break;
      case Glk.STYLEHINT_JUSTIFICATION:
        result = s.justification;
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

  protected void setStyle(Style style)
  {
    if (nonHyper != null)
    {
      nonHyper = style;
      style = style.getHyperlinked();
    }

    super.setStyle(style);
    mStyles.put(new Integer(buffer.length()), style);
  }

  public boolean drawImage(Image img, int align)
  {
    char last;

    tracker.addImage(img, imageNum++);
    try {
      tracker.waitForAll();
    } catch (InterruptedException eInterrupt) 
    { 
      System.err.println("media tracker was interrupted.");
    }
    tracker.removeImage(img);
    
    if (align > INLINE_CENTER)
    {
      int pos = buffer.length();

      // this is a margin image
      MarginImgStruct s = 
           (MarginImgStruct) mMarginGraphics.get(new Integer(pos));
      if (s == null)
      {
        s = new MarginImgStruct();
        mMarginGraphics.put(new Integer(pos), s);
      }
      s.add(img, align);
    }
    else
    {
      InlineImgStruct s = new InlineImgStruct();
      Integer oiPos = new Integer(buffer.length());
      s.img = img;
      s.align = align;
      mInlineGraphics.put(oiPos, s);

      if (nonHyper != null)
        mStyles.put(oiPos, nonHyper);

      buffer.append(INLINE_IMAGE);

      if (nonHyper != null)
        mStyles.put(new Integer(buffer.length()), curStyle);
    }
    lastLineDirty = true;
    return true;
  }

  public void flowBreak()
  {
    if (buffer.length() != 0 && buffer.charAt(buffer.length() - 1) != FLOW_BREAK)
      buffer.append(FLOW_BREAK);
  }

  protected Map getStyleMap()
  {
    return Style.BUFFER_STYLES;
  }

  // This is a bit more complex than the grid window's version, since
  // here we actually have to worry about, for instance, indentation.
  protected Style createHintedStyle(Style style)
  {
    boolean isBold, isItalic;
    int val;
    Color tmp;
    Font newface;
    Style hintedStyle;
    StyleHints hints = (StyleHints) mHints.get(style.name);
    
    if (hints == null)
      return style;

    hintedStyle = (Style) style.clone();
    
    if (hints.data[StyleHints.LEFT_INDENT] != null)
      hintedStyle.leftIndent += hints.data[StyleHints.LEFT_INDENT].intValue();
    if (hints.data[StyleHints.RIGHT_INDENT] != null)
      hintedStyle.rightIndent += 
        hints.data[StyleHints.RIGHT_INDENT].intValue();
    if (hints.data[StyleHints.PAR_INDENT] != null)
      hintedStyle.parIndent += hints.data[StyleHints.PAR_INDENT].intValue();

    // The final result of indentation may not be negative, so we have to
    // check for this...
    if (hintedStyle.leftIndent < 0) hintedStyle.leftIndent = 0;
    if (hintedStyle.rightIndent < 0) hintedStyle.rightIndent = 0;
    if (hintedStyle.parIndent < 0) hintedStyle.parIndent = 0;

    if (hints.data[StyleHints.JUSTIFICATION] != null)
      hintedStyle.justification = 
        hints.data[StyleHints.JUSTIFICATION].intValue();

    if (hintedStyle.justification < Style.LEFT_FLUSH || 
        hintedStyle.justification > Style.RIGHT_FLUSH)
      hintedStyle.justification = Style.LEFT_FLUSH;


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

    if (hints.data[StyleHints.PROPORTIONAL] != null)
    {
      val = hints.data[StyleHints.PROPORTIONAL].intValue();
      if (val == 1 && hintedStyle.isMonospace())
        hintedStyle.family = DEFAULT_PROPORTIONAL_FONT;
      else if (val == 0 && !hintedStyle.isMonospace())
        hintedStyle.family = DEFAULT_FIXED_FONT;
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

  protected int getSplit(int numLines, int axis)
  {
    int split;
    Dimension d;
    Insets i;
    Style normal = (Style) hintedStyles.get("normal");

    split = TextSplitMeasurer.getSplit(panel, numLines, axis, 
                                       frc, normal, MEASURE_ARR);
    
    if (axis == BoxLayout.X_AXIS)
    {
      d = ((JScrollPane) panel).getVerticalScrollBar().getPreferredSize();
      split += d.width;
    }
    else
    {
      split += TOP_MARGIN;
    }

    return split;
  }

  protected void rearrange(Rectangle r)
  {
    Dimension d;
    Insets i;
    int iNewWidth;
    
    // I *think* that basically two things need to get done here.
      // First: we have to set the preferred size.  This is pretty trivial.
      // Second: we have to do a *complete* layout of the buffer.  This is 
      // not trivial (and is better handled in a different method).
                                      
      // The rectangle will have space for the view *and* the scroll pane.
                                               
                                               
    i = ((JScrollPane) panel).getInsets();
    d = ((JScrollPane) panel).getVerticalScrollBar().getPreferredSize();
    view.preferredViewportSize.width = r.width - (i.left + i.right + d.width);
    view.preferredViewportSize.height = r.height - (i.top + i.bottom);
    ((JScrollPane) panel).getViewport().setPreferredSize(view.preferredViewportSize);
    view.setPreferredSize(view.preferredViewportSize);
    view.revalidate();

    if (view.preferredViewportSize.width != viewWidth || restyled)
    {
      restyled = false;
      viewWidth = view.preferredViewportSize.width;
      synchronized (paragraphs)
      {
        paragraphs.clear();
      }
      layout(null, null);
    }
  }

  protected synchronized void layout(Line curLine, Line lastLine)
  {
    char c;
    boolean isParaEnd;
    int iParaStart;
    int iStyleLen;
    int iFlowBreakPos;
    ArrayList imgPos;
    ArrayList breakPos;
    StringBuffer paraBuf;
    AttributedString stStyle;
    int iBuf;
    Paragraph para;

    lastLineDirty = false;

    if (curLine == null)
    {
      para = new Paragraph();
      iBuf = 0;
    }
    else
    {
      para = curLine.para;
      iBuf = curLine.start;
    }

    while (iBuf < buffer.length())
    {
      isParaEnd = false;
      iParaStart = iBuf;
      imgPos = null;
      breakPos = null;
      iFlowBreakPos = -1;
      paraBuf = new StringBuffer();
      c = buffer.charAt(iBuf);

      // layout a paragraph at a time
      while (!isParaEnd && iBuf < buffer.length())
      {
        switch (c)
        {
        case '\n':
          if (iBuf != iParaStart)
            isParaEnd = true;
          else
            paraBuf.append('?');
          break;
        case FLOW_BREAK:
          if (breakPos == null)
            breakPos = new ArrayList();

          breakPos.add(new Integer(iBuf));
          if (iFlowBreakPos < 0)
            iFlowBreakPos = iBuf;
          paraBuf.append('?');
          break;
        case INLINE_IMAGE:
          if (imgPos == null)
            imgPos = new ArrayList();

          imgPos.add(new Integer(iBuf));
          paraBuf.append('?');
          break;
        default:
          paraBuf.append(c);
        }

        if (!isParaEnd)
        {
          iBuf++;
          if (iBuf < buffer.length())
            c = buffer.charAt(iBuf);
        }
      }


      if (paraBuf.length() > 0)
      {
        // apply styles
        stStyle = new AttributedString(paraBuf.toString());
        iStyleLen = paraBuf.length();
        applyStyles(stStyle, iParaStart, iStyleLen);

        // replace images
        if (imgPos != null)
          replaceImages(stStyle, iParaStart, imgPos);

        // replace flow breaks
        if (breakPos != null)
          replaceBreaks(stStyle, iParaStart, breakPos);

        // create lines
        createLines(para, stStyle, iParaStart, iStyleLen, curLine, lastLine);
           
        // now we have to correct lines interrupted by a FLOW_BREAK
        if (iFlowBreakPos >= 0)
        {
          if (correctParagraph(para, stStyle, paraBuf, iParaStart, 
                               iStyleLen, iFlowBreakPos))
            iBuf--;
        }
      
        synchronized (paragraphs) 
        {
          paragraphs.add(para);
        }
        lastLine = (Line) para.lines.getLast();
        para = new Paragraph();
        curLine = new Line();
        curLine.para = para;
        curLine.start = lastLine.end;
        curLine.top = lastLine.bottom;
      }
    }
    resizeView();
  }

  void resizeView()
  {
    final JViewport v;
    Dimension d;
    Paragraph p;
    final int viewportHeight;
    final Line l;
    int y;

    // must set the size of this panel to be large enough
    // FIXME:  margin images may extend further than text, but we have no
    // way of determining this right now
    if (!paragraphs.isEmpty())
    {
      p = (Paragraph) paragraphs.getLast();
      l = (Line) p.lines.getLast();
      v = ((JScrollPane) panel).getViewport();
      //      viewportHeight = v.getHeight();
      viewportHeight = view.preferredViewportSize.height;
      y = Math.max(l.bottom + 5, viewportHeight);
      if (view.getHeight() != y)
      {
        d = new Dimension(viewWidth, y);
        view.setPreferredSize(d);
        view.setSize(d);
        view.revalidate();
      }

      scrollDown(v, l.bottom + 5, viewportHeight);
    }
  }
 
  void scrollDown(JViewport v, int viewHeight, int portHeight)
  {
    if (viewHeight == 0)
      viewHeight = view.getHeight();

    JScrollPane sp = (JScrollPane) panel;
    boolean found = false;

    Line l = (lastLineSeen == null) 
      ? (Line) ((Paragraph) paragraphs.getFirst()).lines.getFirst() 
      : lastLineSeen;
    int rectBottom = Math.min(l.top + portHeight, viewHeight);
    Point viewPos = v.getViewPosition();
    int newY = Math.max(0, rectBottom - portHeight);

    if (lastLineSeen == null || viewPos.y != newY)
    {
      viewPos.y = newY;
      v.setViewPosition(viewPos);
    }

    if (MORE_CALLBACK != null)
    {
      if (rectBottom != viewHeight)
        MORE_CALLBACK.callback(StatusPane.MORE);
      else
        MORE_CALLBACK.callback(StatusPane.BLANK);
    }

    Line l2 = l;
    synchronized (paragraphs)
    {
      int np = paragraphs.size();
      ListIterator pit = paragraphs.listIterator(np);
      while (!found && pit.hasPrevious())
      {
        Paragraph p = (Paragraph) pit.previous();
        int nl = p.lines.size();
        ListIterator lit = p.lines.listIterator(nl);

        while (!found && lit.hasPrevious())
        {
          l2 = l;
          l = (Line) lit.previous();
          if (l.bottom < rectBottom)
            found = true;
        }
      }
    }
    lastLineSeen = (lastLineSeen == l) ? l2 : l;
  }

  // return true if the very last char of para is a FLOW_BREAK
  boolean correctParagraph(Paragraph para, AttributedString s, StringBuffer buf, 
                           int start, int iLen, int iPos)
  {
    int i;
    int iNewStart;
    Line curLine = null;
    Line l = null;
    LineBreakMeasurer lbm = new LineBreakMeasurer(s.getIterator(), frc);
    int nl = para.lines.size();
    ListIterator lit = para.lines.listIterator();
    
    for (i = 0; i < nl; i++)
    {
      l = (Line) lit.next();
      if (iPos >= l.start && iPos < l.end)
        break;
    }

    if (iPos == l.end)
    {
      if (i == nl - 1)
      {
        l.end--;
        return true;
      }
      else
      {
        l = (Line) lit.next();
        i++;
      }
    }

    // is l indented because of margin images?
    if (l.leftImg != null || l.rightImg != null)
    {
      // these lines are now invalid
      if (i + 1 < nl)
        para.lines.subList(i + 1, nl).clear();

      // is there anything on the line before the break?
      if (iPos > l.start)
      {
        lbm.setPosition(l.start - start);
        layoutLine(para, start, l, lbm, iPos - start);
      }

      // the position past the flow break (FIXME: do we really want to pass it?)
      iNewStart = iPos + 1; 

      while (iNewStart - start < iLen)
      {
        curLine = new Line();
        curLine.start = iNewStart;
        curLine.top = l.getNextLineTop();

        lbm.setPosition(curLine.start - start);
        layoutLine(para, start, curLine, lbm, -1);

        l = curLine;
        curLine.para = para;
        para.lines.add(curLine);
        iNewStart = start + lbm.getPosition();
      }
    }

    return false;
  }

  void createLines(Paragraph para, AttributedString s, int start, int iLen, 
                   Line curLine, Line lastLine)
  {
    SortedMap head;
    Style cur;
    int indentTotal;
    int availWidth;
    int widthBase;
    TextLayout layout;
    MarginImgStruct struct;
    MarginImage mimg;
    LineBreakMeasurer lbm = new LineBreakMeasurer(s.getIterator(), frc);


    if (curLine == null)
    {
      curLine = new Line();
      if (lastLine == null)
      {
        curLine.start = 0;
        curLine.top = TOP_MARGIN;
      }
      else
      {
        curLine.start = lastLine.end;
        curLine.top = lastLine.bottom;
      }
    }

    if (lastLine != null)
    {
      if (lastLine.leftImg != null)
        curLine.leftImg = lastLine.leftImg.getInheritance(curLine.top);
      if (lastLine.rightImg != null)
        curLine.rightImg = lastLine.rightImg.getInheritance(curLine.top);
    }

    while (lbm.getPosition() < iLen)
    {
      if (para.lines.isEmpty())
      {
        // find the indentation and justification parameters for this paragraph
        cur = (Style) mStyles.get(new Integer(start + 1));
        if (cur == null)
        {
          head = mStyles.headMap(new Integer(start + 1));
          if (!head.isEmpty())
            cur = (Style) mStyles.get(head.lastKey());
          else
            cur = (Style) hintedStyles.get("normal");
        }
        para.style = cur;

        // Line may have its own margin images since it starts a paragraph.
        // Why the "+1"?  Because the first line of a paragraph always
        // starts with a newline character.
        struct = (MarginImgStruct) mMarginGraphics.get(new Integer(curLine.start + 1));
      
        if (struct != null && struct.left != null)
        {
          for (int i = 0; i < struct.left.size(); i++)
          {
            mimg = new MarginImage();
            mimg.img = (Image) struct.left.get(i);
            mimg.inherit = curLine.leftImg;
            mimg.myTop = curLine.top;
            curLine.leftImg = mimg;
          }
        }

        if (struct != null && struct.right != null)
        {
          for (int i = 0; i < struct.right.size(); i++)
          {
            mimg = new MarginImage();
            mimg.img = (Image) struct.right.get(i);
            mimg.inherit = curLine.rightImg;
            mimg.myTop = curLine.top;
            curLine.rightImg = mimg;
          }
        }
      }
      layoutLine(para, start, curLine, lbm, -1);
      
      lastLine = curLine;
      curLine.para = para;
      para.lines.add(curLine);

      if (lbm.getPosition() < iLen)
      {
        curLine = new Line();
        curLine.start = lastLine.end;
        curLine.top = lastLine.bottom;

        if (lastLine.leftImg != null)
          curLine.leftImg = lastLine.leftImg.getInheritance(curLine.top);
        if (lastLine.rightImg != null)
          curLine.rightImg = lastLine.rightImg.getInheritance(curLine.top);
      }
    }
  }

  void layoutLine(Paragraph para, int start, Line curLine, LineBreakMeasurer lbm, 
                  int offsetLimit)
  {
    TextLayout layout;
    int indentTotal;
    int parWidthBase;
    int availWidth;

    availWidth = viewWidth - (2 * H_MARGIN);
    if (curLine.leftImg != null)
      availWidth -= (curLine.leftImg.getWidth() + 5);
    if (curLine.rightImg != null)
      availWidth -= (curLine.rightImg.getWidth() + 5);
    parWidthBase = availWidth;

    para.leftBase = para.getLeftIndent(viewWidth);
    para.rightBase = para.getRightIndent(viewWidth);

    indentTotal = para.leftBase + para.rightBase;
    if (para.lines.isEmpty())
      indentTotal += para.getParIndent(parWidthBase);

    availWidth -= indentTotal;

    // this is lame, but apparently necessary
    if (availWidth < 10)
      availWidth = 10;

    if (offsetLimit < 0)
      layout = lbm.nextLayout((float) availWidth);
    else
      layout = lbm.nextLayout((float) availWidth, offsetLimit, false);

    curLine.widthBase = availWidth;
    //    if (para.style.justification == Style.LEFT_RIGHT_FLUSH)
      //      layout = layout.getJustifiedLayout((float) availWidth);

    curLine.layout = layout;        

    if (para.style.justification == Style.LEFT_FLUSH ||
        para.style.justification == Style.LEFT_RIGHT_FLUSH)
    {
      curLine.left = 
        ((curLine.leftImg == null) ? 0 : curLine.leftImg.getWidth() + 5) +
        para.leftBase;
      if (para.lines.isEmpty())
        curLine.left += para.getParIndent(viewWidth);
    }
    else if (para.style.justification == Style.CENTERED)
    {
      curLine.left = ((viewWidth - (2 * H_MARGIN)) / 2) - (int) (layout.getAdvance() / 2);
    }
    else
    {
      curLine.left = viewWidth - (2 * H_MARGIN) -
        (((curLine.rightImg == null) ? 0 : curLine.rightImg.getWidth() + 5) +
         (int) layout.getAdvance());
    }
      
    curLine.end = start + lbm.getPosition();
    curLine.bottom = curLine.top + 
      (int) (layout.getAscent() + layout.getDescent() + layout.getLeading() + 2);
  }

  void replaceImages(AttributedString s, int start, ArrayList imgPos)
  {
    ImageGraphicAttribute iga;
    InlineImgStruct struct;
    Integer oi;
    Iterator it = imgPos.iterator();

    while (it.hasNext())
    {
      oi = (Integer) it.next();
      struct = (InlineImgStruct) mInlineGraphics.get(oi);

      switch (struct.align)
      {
      case INLINE_UP:
        iga = new ImageGraphicAttribute(struct.img, 
                                        GraphicAttribute.ROMAN_BASELINE,
                                        0.0f,
                                        (float) struct.img.getHeight(panel));
        break;
      case INLINE_DOWN:
        iga = new ImageGraphicAttribute(struct.img, 
                                        GraphicAttribute.TOP_ALIGNMENT);
        break;
      default:
        // FIXME: probably will not work
        iga = new ImageGraphicAttribute(struct.img, 
                                        GraphicAttribute.CENTER_BASELINE,
                                        0.0f, 
                                        (float) (struct.img.getHeight(panel) / 2));
      }

      s.addAttribute(TextAttribute.CHAR_REPLACEMENT, iga, oi.intValue() - start, 
                     (oi.intValue() + 1) - start);
    }
  }

  void replaceBreaks(AttributedString s, int start, ArrayList breakPos)
  {
    Integer oi;
    Iterator it = breakPos.iterator();

    while (it.hasNext())
    {
      oi = (Integer) it.next();
      s.addAttribute(TextAttribute.CHAR_REPLACEMENT, FlowBreakAttribute.SINGLETON,
                     oi.intValue() - start, (oi.intValue() + 1) - start);
    }

  }

  void applyStyles(AttributedString s, int start, int iLen)
  {
    Map.Entry e;
    Iterator it;
    int iLast = 0;
    Integer oi = null;
    int iKey;
    SortedMap head = mStyles.headMap(new Integer(start));
    SortedMap sub = mStyles.subMap(new Integer(start), new Integer(start + iLen));
    Style cur = (Style) hintedStyles.get("normal");
    
    if (!head.isEmpty())
      oi = (Integer) head.lastKey();
    
    if (oi != null)
      cur = (Style) mStyles.get(oi);

    // replace initial newline
    s.addAttribute(TextAttribute.CHAR_REPLACEMENT, 
                   new NewlineAttribute(cur.getMap(), frc),
                   0, 1);

    it = sub.entrySet().iterator();
    while (it.hasNext())
    {
      e = (Map.Entry) it.next();
      oi = (Integer) e.getKey();
      iKey = oi.intValue();

      if (iKey > start && iKey < start + iLen)
      {
        s.addAttributes(cur.getMap(), iLast, iKey - start);
      }

      iLast = iKey - start;
      cur = (Style) e.getValue();
    }

    s.addAttributes(cur.getMap(), iLast, iLen);
  }


  public boolean isFocusStealable()
  {
    return (charConsumer == null && lineConsumer == null);
  }

  public void mouseClicked(MouseEvent e)
  {
    if (hyperConsumer != null)
    {
      int x = e.getX();
      int y = e.getY();
      Line l = getHit(x, y);

      if (l != null)
      {
        TextLayout lay = (l.jlay == null) ? l.layout : l.jlay;
        TextHitInfo thi = 
          lay.hitTestChar((float) (x - l.left - H_MARGIN), 
                          (float) (y - l.top));
        int pos = thi.getCharIndex() + l.start;
        Integer oiPos = new Integer(pos);
        Integer oiVal = (Integer) mHyperlinks.get(oiPos);
        
        if (oiVal == null)
        {
          SortedMap m = mHyperlinks.headMap(oiPos);
          if (!m.isEmpty())
          {
            oiPos = (Integer) m.lastKey();
            oiVal = (Integer) m.get(oiPos);
          }
        }

        if (oiVal != null && oiVal.intValue() != 0)
        {
          HyperlinkInputConsumer hic = hyperConsumer;
          Style nhs = nonHyper;
          nonHyper = null;
          setStyle(nhs);
          hyperConsumer = null;
          hic.consume(oiVal.intValue());
        }
      }
    }

    if (LameFocusManager.FOCUSED_WINDOW != this)
      LameFocusManager.grabFocus(this);
  }

  Line getHit(int x, int y)
  {
    int np = paragraphs.size();
    ListIterator pit = paragraphs.listIterator(np);

    while (pit.hasPrevious())
    {
      Paragraph p = (Paragraph) pit.previous();
      int pstart = ((Line) p.lines.getFirst()).top;

      if (pstart <= y)
      {
        int nl = p.lines.size();
        ListIterator lit = p.lines.listIterator(nl);

        while (lit.hasPrevious())
        {
          Line l = (Line) lit.previous();
          if (l.top <= y)
          {
            int ax = l.left + H_MARGIN;
            TextLayout lay = (l.jlay == null) ? l.layout : l.jlay;
            int bx = ax + (int) lay.getVisibleAdvance();
            if (l.rightImg != null)
              bx += (l.rightImg.getWidth() + 5);

            if ((x >= ax && x < bx) || 
                (l.leftImg != null && x >= H_MARGIN && 
                 x < H_MARGIN + l.leftImg.getWidth()))
              return l;
            else
              return null;
          }
        }
      }
    }
    return null;
  }

  public boolean requestCharacterInput(CharInputConsumer cic)
  {
    if (charConsumer != null || lineConsumer != null)
      return false;
    charConsumer = cic;
    LameFocusManager.requestFocus(this);
    iLineStartPos = iLineCursorPos = buffer.length();

    return true;
  }

  public boolean requestLineInput(LineInputConsumer lic, String initContents, 
                                  int max)
  {
    if (charConsumer != null || lineConsumer != null)
      return false;
    lineConsumer = lic;
    iLineStartPos = buffer.length();
    iLineCursorPos = iLineStartPos;
    maxLineInput = max;
    LameFocusManager.requestFocus(this);
    if (!mStyles.isEmpty())
      oldStyle = (Style) mStyles.get(mStyles.lastKey());
    setStyle((Style) hintedStyles.get("input"));
    if (initContents != null)
    {
      if (initContents.length() > max)
        putString(initContents.substring(0, max));
      else
        putString(initContents);
      iLineCursorPos = buffer.length();
      layoutLastLine(buffer.length());
    }

    return true;
  }

  public void cancelCharacterInput()
  {
    charConsumer = null;
  }

  public String cancelLineInput()
  {
    if (lineConsumer == null)
      return "";

    String s;
    LineInputConsumer lic = lineConsumer;
    lineConsumer = null;
    inputHistoryIndex = 0;
    setStyle(oldStyle);

    s = buffer.substring(iLineStartPos, buffer.length());
    lic.cancel(s);
    return s;
  }

  protected void handleKey(KeyEvent e)
  {
    boolean foundNonWhite;
    JViewport v = ((JScrollPane) panel).getViewport();
    Rectangle portRect = v.getViewRect();
    int viewHeight = view.getHeight();

    if (portRect.height > 5 && viewHeight > portRect.y + portRect.height)
    {
      scrollDown(v, viewHeight, portRect.height);
    }
    else if (charConsumer != null)
    {
      CharInputConsumer cs = charConsumer;
      charConsumer = null;

      doPaint(lastLineSeen);

      cs.consume(e);
    }
    else if (lineConsumer != null)
    {
      boolean needLayout = false;

      if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
      {
        switch(e.getKeyCode())
        {
        case KeyEvent.VK_LEFT:
          if (e.getModifiers() == InputEvent.CTRL_MASK)
          {
            int i;
            foundNonWhite = false;
            for (i = iLineCursorPos; i > iLineStartPos; i--)
            {
              if (!foundNonWhite && i != 0 && buffer.charAt(i - 1) != ' ')
                foundNonWhite = true;

              if (foundNonWhite && i != 0 && buffer.charAt(i - 1) == ' ')
                break;
            }
            iLineCursorPos = i;
          }
          else if (iLineCursorPos > iLineStartPos)
          {
            iLineCursorPos--;
          }
          break;
        case KeyEvent.VK_RIGHT:
          if (e.getModifiers() == InputEvent.CTRL_MASK)
          {
            int i;
            foundNonWhite = false;
            for (i = iLineCursorPos; i < buffer.length(); i++)
            {
              if (!foundNonWhite && buffer.charAt(i) != ' ')
                foundNonWhite = true;

              if (foundNonWhite && buffer.charAt(i) == ' ')
                break;
            }
            iLineCursorPos = i;
          }
          else if (iLineCursorPos < buffer.length() && 
                   iLineCursorPos < iLineStartPos + maxLineInput)
          {
            iLineCursorPos++;
          }
          break;
        case KeyEvent.VK_UP:
          if (inputHistoryIndex < inputHistory.size())
          {
            String oldReply;

            buffer.delete(iLineStartPos, buffer.length());
            iLineCursorPos = iLineStartPos;
            oldReply = (String) inputHistory.get(inputHistoryIndex);
            inputHistoryIndex++;
            buffer.append(oldReply);
            iLineCursorPos += oldReply.length();
            needLayout = true;
          }
          break;
        case KeyEvent.VK_DOWN:
          if (inputHistoryIndex > 0)
          {
            String newReply;

            buffer.delete(iLineStartPos, buffer.length());
            iLineCursorPos = iLineStartPos;
            inputHistoryIndex--;

            if (inputHistoryIndex > 0)
            {
              newReply = (String) inputHistory.get(inputHistoryIndex - 1);
              buffer.append(newReply);
              iLineCursorPos += newReply.length();
            }

            needLayout = true;
          }
          break;
        default:
          // NOOP
        }
      }
      else
      {
        char c = e.getKeyChar();
        switch(c)
        {
        case 1:
          iLineCursorPos = iLineStartPos;
          break;
        case 5:
          iLineCursorPos = buffer.length();
          break;
        case 8:
        case 127:
          if (e.getModifiersEx() == InputEvent.META_DOWN_MASK ||
              e.getModifiersEx() == InputEvent.ALT_DOWN_MASK)

          {
            int i;
            foundNonWhite = false;
            for (i = iLineCursorPos; i > iLineStartPos; i--)
            {
              if (!foundNonWhite && i != 0 && buffer.charAt(i - 1) != ' ')
                foundNonWhite = true;
              
              if (foundNonWhite && i != 0 && buffer.charAt(i - 1) == ' ')
                break;
            }
            if (iLineCursorPos != i)
            {
              buffer.delete(i, iLineCursorPos);
              iLineCursorPos = i;
              needLayout = true;
            }
          }
          else if (iLineCursorPos > iLineStartPos)
          {
            iLineCursorPos--;
            buffer.deleteCharAt(iLineCursorPos);
            needLayout = true;
          }
          break;
        case 10:
        case 13:
          String stReply;
          LineInputConsumer lic = lineConsumer;
          lineConsumer = null;
          buffer.append('\n');
          setStyle(oldStyle);

          // do the layout right HERE, before consume()
          lastLineSeen = layoutLastLine(iLineStartPos);

          stReply = buffer.substring(iLineStartPos, buffer.length() - 1);

          if (stReply.length() > 0)
          {
            inputHistory.addFirst(stReply);
            if (inputHistory.size() >= HISTORY_SIZE_LIMIT)
              inputHistory.removeLast();
          }
          inputHistoryIndex = 0;
          lic.consume(stReply);

          if (echo != null)
          {
            echo.putString(stReply);
            echo.putChar('\n');
          }
          break;
        default:
          if (buffer.length() < (iLineStartPos + maxLineInput) &&
              (c >= 32 && c < 127) || (c >= 160 && c < 256))
          {
            buffer.insert(iLineCursorPos++, c);

            needLayout = true;
          }
        }
      }
      
      Line fromLine = null;
      if (needLayout)
        fromLine = layoutLastLine(iLineStartPos);

      if (fromLine == null)
        fromLine = lastLineSeen;

      doPaint(fromLine);
    }
  }


  Line layoutLastLine(int startPos)
  {
    Paragraph p1 = null;
    Paragraph p2 = null;
    Line l1 = null;
    Line l2 = null;
    int np = 0;
    int nl = 0;
    int i = 0;
    int j = 0;
    boolean done = false;

    synchronized (paragraphs)
    {
      if (!paragraphs.isEmpty())
      {
        np = paragraphs.size();
        ListIterator pit = paragraphs.listIterator(np);

        for (i = np - 1; !done && i >= 0; i--)
        {
          p1 = (Paragraph) pit.previous();
          nl = p1.lines.size();
          ListIterator lit = p1.lines.listIterator(nl);

          for (j = nl - 1; !done && j >= 0; j--)
          {
            l1 = (Line) lit.previous();
            done = (l1.start < startPos);
          }
        }
        if (l1 != p1.lines.getFirst())
        {
          l2 = (Line) p1.lines.get(j);
        }
        else if (p1 != paragraphs.getFirst())
        {
          p2 = (Paragraph) paragraphs.get(i);
          l2 = (Line) p2.lines.getLast();
        }
      }
    }

    if (l1 != null && l2 != null)
    {
      synchronized (paragraphs)
      {
        p1.lines.subList(j + 1, nl).clear();
        paragraphs.subList(i + 1, np).clear();
      }
      layout(l1, l2);
      return l1;
    }
    else
    {
      synchronized (paragraphs)
      {
        paragraphs.clear();
      }
      layout(null, null);
      return null;
    }
  }

  class TextBufferPanel extends JPanel implements Scrollable
  {
    Dimension preferredViewportSize = new Dimension(0, 0);
    int lSize = ((Style) hintedStyles.get("normal")).size + 2;


    public Dimension getPreferredScrollableViewportSize()
    {
      return preferredViewportSize;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, 
                                           int orientation,
                                           int direction)
    {
      return preferredViewportSize.height - lSize;
    }

    public boolean getScrollableTracksViewportHeight()
    {
      return false;
    }

    public boolean getScrollableTracksViewportWidth()
    {
      return true;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction)
    {
      return lSize;
    }

    public synchronized void paintComponent(Graphics g)
    {
      int np, nl, i, j;
      int x, y;
      MarginImage mi;
      Rectangle r;
      Rectangle clip;
      Paragraph p;
      Shape[] carets = null;
      int translateX = 0;
      int translateY = 0;
      Line l1 = null;
      Line l2 = null;
      TextLayout layout = null;
      Graphics2D g2 = (Graphics2D) g;
      Style lastStyle = (Style) hintedStyles.get("normal");
      Style inputStyle = (Style) hintedStyles.get("input");

      clip = g2.getClipBounds();
      g2.setColor(lastStyle.backColor);
      g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        
      r = new Rectangle();
      r.x = clip.x;
      r.width = clip.width;

      synchronized (paragraphs)
      {
        np = paragraphs.size();
        ListIterator pit = paragraphs.listIterator();

        for (i = 0; i < np; i++)
        {
          p = (Paragraph) pit.next();
          l1 = (Line) p.lines.getFirst();
          l2 = (Line) p.lines.getLast();

          r.y = l1.top;
          r.height = l2.bottom - l1.top;
          
          if (r.intersects(clip))
          {
            l1 = (Line) p.lines.getFirst();
            
            mi = l1.leftImg;
            while (mi != null)
            {
              r.y = mi.myTop;
              r.height = mi.img.getHeight(this);
            
              if (r.intersects(clip))
              {
                x = H_MARGIN + p.leftBase + 
                  ((mi.inherit == null) ? 0 : mi.inherit.getWidth());
                g2.drawImage(mi.img, x, r.y, this);
              }
              mi = mi.inherit;
            }
          
            mi = l1.rightImg;
            while (mi != null)
            {
              r.y = mi.myTop;
              r.height = mi.img.getHeight(this);
              
              if (r.intersects(clip))
              {
                x = getWidth() - (H_MARGIN + p.rightBase + mi.getWidth());
                g2.drawImage(mi.img, x, r.y, this);
              }
              mi = mi.inherit;
            }
          
            nl = p.lines.size();
            ListIterator lit = p.lines.listIterator();
              
            for (j = 0; j < nl; j++)
            {
              l1 = (Line) lit.next();
              r.y = l1.top;
              r.height = l1.bottom - l1.top;
            
              if (r.intersects(clip) && l1.layout != null)
              {
                lastStyle = p.style;
                if (p.style.justification == Style.LEFT_RIGHT_FLUSH &&
                    j != nl - 1)
                {
                  if (l1.jlay == null)
                    l1.jlay = 
                      l1.layout.getJustifiedLayout((float) l1.widthBase);
                  layout = l1.jlay;
                }
                else
                {
                  layout = l1.layout;
                }

                x = l1.left + H_MARGIN;
                y = l1.top + (int) layout.getAscent();
                layout.draw(g2, (float) x, (float) y);
              }
            
              if ((charConsumer != null || lineConsumer != null) && 
                  ((l1.start <= iLineCursorPos && l1.end > iLineCursorPos) ||
                   (iLineCursorPos == l1.end && l1.end == buffer.length())))
              {
                translateX = l1.left + H_MARGIN;
                translateY = (int) ((double) l1.top + layout.getAscent());
                
                int curPos;
                if (charConsumer != null)
                  curPos = layout.getCharacterCount();
                else
                  curPos = iLineCursorPos - l1.start ;

                carets = layout.getCaretShapes(curPos);
              }
            }
          }
        }
      }
      
      if (carets != null)
      {
        g2.setColor(inputStyle.textColor);
        g2.translate(translateX, translateY);
        g2.draw(carets[0]);
      }
    }
  }


  static class Paragraph
  {
    Style style;
    int leftBase = 0;
    int rightBase = 0;
    LinkedList lines = new LinkedList();

    Line getLastLine()
    {
      return (lines.isEmpty() ? null : (Line) lines.getLast());
    }

    int getLeftIndent(int availWidth)
    {
      return ((int) ((float) availWidth * 0.03)) * style.leftIndent;
    }

    int getRightIndent(int availWidth)
    {
      return ((int) ((float) availWidth * 0.03)) * style.rightIndent;
    }

    int getParIndent(int availWidth)
    {
      return ((int) ((float) availWidth * 0.03)) * style.parIndent;
    }
  }

  static class Line 
  {
    Paragraph para;
    TextLayout layout;
    TextLayout jlay;
    int widthBase;

    int top;
    int bottom;
    int start;
    int end;
    int left;

    // lazily instantiated, since most lines will not reference any margin images
    MarginImage leftImg;
    MarginImage rightImg;

    int getNextLineTop()
    {
      int left, right;
      if (leftImg != null || rightImg != null)
      {
        left = (leftImg == null) ? bottom : leftImg.getFlowBreakPos();
        right = (rightImg == null) ? bottom : rightImg.getFlowBreakPos();
        return Math.max(bottom, Math.max(left, right));
      }
      else
      {
        return bottom;
      }
    }
  }

  class MarginImage
  {
    MarginImage inherit;
    Image img;
    int myTop;

    int getWidth()
    {
      return ((inherit == null) ? 0 : inherit.getWidth()) + img.getWidth(view);
    }

    int getXLimit(int y)
    {
      if (myTop + img.getHeight(view) >= y)
        return getWidth();
      else
        return (inherit == null) ? 0 : inherit.getXLimit(y);
    }

    MarginImage getInheritance(int y)
    {
      if (myTop + img.getHeight(view) >= y)
        return this;
      else
        return (inherit == null) ? null : inherit.getInheritance(y);
    }

    int getFlowBreakPos()
    {
      // FIXME:  again the +5 for a margin
      return Math.max(myTop + img.getHeight(view) + 5,
                      ((inherit == null) ? 0 : inherit.getFlowBreakPos()));
    }
  }

  static class InlineImgStruct
  {
    Image img;
    int align;
  }

  static class MarginImgStruct
  {
    ArrayList left;
    ArrayList right;

    void add(Image img, int align)
    {
      if (align == MARGIN_LEFT)
      {
        if (left == null)
          left = new ArrayList(1);
        left.add(img);
      }
      else
      {
        if (right == null)
          right = new ArrayList(1);
        right.add(img);
      }
    }
  }

  static class FlowBreakAttribute extends GraphicAttribute
  {
    static final FlowBreakAttribute SINGLETON = new FlowBreakAttribute();

    public FlowBreakAttribute()
    {
      super(ROMAN_BASELINE);
    }

    public void draw(Graphics2D g, float x, float y)
    {
      // is invisible
    }

    public float getAdvance()
    {
      return 0.0f;
    }

    public float getAscent()
    {
      return 0.0f;
    }

    public float getDescent()
    {
      return 0.0f;
    }
  }

  static class NewlineAttribute extends GraphicAttribute
  {
    TextLayout l;

    public NewlineAttribute(Map m, FontRenderContext frc)
    {
      super(ROMAN_BASELINE);
      l = new TextLayout("Mfgylpq", m, frc);
    }

    public void draw(Graphics2D g, float x, float y)
    {

    }

    public float getAdvance()
    {
      return 0f;
    }
    
    public float getAscent()
    {
      return l.getAscent();
    }

    public float getDescent()
    {
      return l.getDescent();
    }
  }
}

