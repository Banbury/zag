package org.p2c2e.zing;

import java.awt.*;
import java.awt.font.*;
import javax.swing.*;

public class PairWindow extends Window
{
  int axis;
  //  int borderWidth;
  int keySize;
  int keySizeType;
  boolean backward;

  Window key;
  public Window first;
  public Window second;
  
  Rectangle currentBounds;

  PairWindow(FontRenderContext context, int placement)
  {
    super(context);

    axis = ((placement == LEFT) || (placement == RIGHT))
      ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS;
    backward = (placement == LEFT) || (placement == ABOVE);

    panel = new JPanel();
    BoxLayout bl = new BoxLayout(panel, axis);
    panel.setLayout(bl);
    //    panel = (axis == BoxLayout.X_AXIS) ? Box.createHorizontalBox() :
    //      Box.createVerticalBox();
    this.axis = axis;
  }

  protected void restyle(boolean useHints)
  {
    first.restyle(useHints);
    second.restyle(useHints);
  }

  protected int getWindowType()
  {
    return PAIR;
  }

  public int getWindowWidth()
  {
    int i = 0;
    if (first != null) i += first.panel.getWidth();
    if (second != null) i+= second.panel.getWidth();
    return i;
  }

  public int getWindowHeight()
  {
    int i = 0;
    if (first != null) i += first.panel.getHeight();
    if (second != null) i+= second.panel.getHeight();
    return i;
  }

  protected void doLayout()
  {
    if (first != null)
      first.doLayout();
    if (second != null)
      second.doLayout();
  }

  void set(Window one, Window two)
  {
    first = one;
    second = two;
    first.parent = this;
    second.parent = this;

    panel.removeAll();
    panel.add(one.panel);

    //    if (borderWidth > 0)
    //    {
    //      border = Box.createRigidArea(new Dimension((axis == BoxLayout.X_AXIS) 
    //                                                 ? borderWidth : 1,
    //                                                 (axis == BoxLayout.X_AXIS)
    //                                                 ? 1 : borderWidth));
      //      border.setBackground(Color.blue);
    //      panel.add(border);
    //    }

    panel.add(two.panel);

  }
  
  void replace(Window w, PairWindow p)
  {
    panel.remove(first.panel);
    panel.remove(second.panel);
    //    if (border != null)
    //      panel.remove(border);

    
    if (w == first)
      first = p;
    else
      second = p;

    panel.add(first.panel);
    //    if (border != null)
    //      panel.add(border);
    panel.add(second.panel);
  }

  public int getSplitMethod()
  {
    int placement;

    if (axis == BoxLayout.X_AXIS)
      placement = (backward ? LEFT : RIGHT);
    else
      placement = (backward ? ABOVE : BELOW);

    return (placement | keySizeType);
  }

  public int getKeyWindowSize()
  {
    return keySize;
  }

  public synchronized void setArrangement(int method, int size, Window keywin)
  {
    int placement = (method & 0x0f);
    int splitType = ((method & FIXED) != 0) ? FIXED : PROPORTIONAL;

    if ((axis == BoxLayout.X_AXIS && 
         (placement == LEFT || placement == RIGHT)) ||
        (axis == BoxLayout.Y_AXIS && 
         (placement == ABOVE || placement == BELOW)))
    {
      backward = (placement == LEFT) || (placement == ABOVE);
      keySizeType = splitType;
      keySize = size;

      if (keywin != null)
      {
        PairWindow p = keywin.parent;

        while (p != null && p != this)
          p = p.parent;

        if (p == this)
          key = keywin;
      }

      rearrange(currentBounds);
    }
  }

  protected synchronized void rearrange(Rectangle r)
  {
    currentBounds = r;

    Rectangle box1 = new Rectangle();
    Rectangle box2 = new Rectangle();
    int split = 0;

    bbox.x = r.x;
    bbox.y = r.y;
    bbox.width = r.width;
    bbox.height = r.height;

    //    panel.setPreferredSize(r.width, r.height);

    //    if (axis == BoxLayout.X_AXIS)
    //    {
    //      if (border != null)
    //        border.setPreferredSize(new Dimension(borderWidth, r.height));
    //    }
    //    else
    //    {
    //      if (border != null)
    //        border.setPreferredSize(new Dimension(r.width, borderWidth));
    //    }

    if (keySizeType == PROPORTIONAL)
    {
      split = (axis == BoxLayout.X_AXIS) 
        ? ((r.width * keySize) / 100) : ((r.height * keySize) / 100);
    }
    else
    {
      // is the size is fixed, things are rather more complex;
      // we'll ask the window itself for its actual size
      if (key == null)
        split = 0;
      else
        split = key.getSplit(keySize, axis);
    }

    if (axis == BoxLayout.X_AXIS)
    {
      box1.x = r.x;
      box1.y = r.y;
      box1.width = split;
      box1.height = r.height;
      
      box2.x = r.x + split; // + borderWidth;        
      box2.y = r.y;
      box2.width = r.width - split; // - borderWidth;
      box2.height = r.height;
    }
    else
    {
      box1.x = r.x;
      box1.y = r.y;
      box1.width = r.width;
      box1.height = split;
      box2.x = r.x;
      box2.y = r.y + split; // + borderWidth;
      box2.width = r.width;
      box2.height = r.height - split; // - borderWidth;
    }

    if (backward)
    {
      first.panel.setPreferredSize(new Dimension(box1.width, box1.height));
      second.panel.setPreferredSize(new Dimension(box2.width, box2.height));
      first.rearrange(box1);
      second.rearrange(box2);
    }
    else
    {
      first.panel.setPreferredSize(new Dimension(box2.width, box2.height));
      second.panel.setPreferredSize(new Dimension(box1.width, box1.height));
      first.rearrange(box2);
      second.rearrange(box1);
    }

    panel.revalidate();
    panel.repaint();

    // Note that these boxes may have negative widths or heights, in which 
    // case they should be invisible (this is different from glk, but I
    // prefer it).
    //
    // This probably entails actually removing the elements from the Boxes...
  }

  public Stream.Result closeStream()
  {
    if (first != null)
      first.closeStream();
    if (second != null)
      second.closeStream();
    return super.closeStream();
  }
}
