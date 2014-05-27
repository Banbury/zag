package org.p2c2e.zing;

import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;

public class GraphicsWindow extends Window
{
  static GraphicsConfiguration graphConfig = 
    GraphicsEnvironment
    .getLocalGraphicsEnvironment()
    .getDefaultScreenDevice()
    .getDefaultConfiguration();

  Color bg;
  MouseInputConsumer mouseConsumer;
  BufferedImage bi;
  Graphics2D g2d;
  boolean change;

  public GraphicsWindow(FontRenderContext context)
  {
    super(context);

    panel = new GraphicsWindowPanel();
    panel.addMouseListener(this);
    bg = Color.white;
    change = true;
  }

  protected void doLayout()
  {
    if (change)
    {
      panel.repaint();
      change = false;
    }
  }

  protected int getWindowType()
  {
    return GRAPHICS;
  }

  public int getWindowHeight()
  {
    return (bi == null) ? 0 : bi.getHeight();
  }

  public int getWindowWidth()
  {
    return (bi == null) ? 0 : bi.getWidth();
  }

  protected void clear()
  {
    fillRect(bg, 0, 0, bi.getWidth(), bi.getHeight());
    change = true;
  }

  protected int getSplit(int size, int axis)
  {
    // no insets on a graphics window; graphics should go to the edges
    return size;
  }

  protected synchronized void rearrange(Rectangle r)
  {
    BufferedImage nbi = 
      graphConfig.createCompatibleImage(r.width, r.height,
                                        Transparency.TRANSLUCENT);
    Graphics2D ng2d = nbi.createGraphics();

    ng2d.setClip(0, 0, r.width, r.height);
    ng2d.setColor(bg);
    ng2d.fillRect(0, 0, r.width, r.height);

    if (bi != null)
    {
      ng2d.drawImage(bi, 0, 0, panel);

      if (r.width > bi.getWidth())
        ng2d.fillRect(bi.getWidth(), 0, r.width - bi.getWidth(), r.height);

      if (r.height > bi.getHeight())
        ng2d.fillRect(0, bi.getHeight(), r.width, r.height - bi.getHeight());

      g2d.dispose();
    }

    bi = nbi;
    g2d = ng2d;

    change = true;
    panel.revalidate();
  }

  public void setBackgroundColor(Color c)
  {
    bg = c;
    panel.setBackground(c);
    change = true;
  }

  public void fillRect(final Color c, final int left, final int top, 
                       final int width, final int height)
  {
    if (bi != null)
    {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              g2d.setColor(c);
              g2d.fillRect(left, top, width, height);
            }
          });
      } catch (Exception e) {
        System.err.println("Could not fillRect(): " + e);
      }
      change = true;
    }
  }

  public void eraseRect(final int left, final int top, 
                        final int width, final int height)
  {
    if (bi != null)
    {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              g2d.setColor(bg);
              g2d.fillRect(left, top, width, height);
            }
          });
      } catch (Exception e) {
        System.err.println("Could not eraseRect(): " + e);
      }
      change = true;
    }
  }

  public void drawImage(final Image img, final int x, final int y)
  {
    if (bi != null)
    {
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              g2d.drawImage(img, x, y, panel);
            }
          });
      } catch (Exception e) {
        System.err.println("Could not drawImage(): " + e);
      }
      change = true;
    }
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

  public void mouseClicked(MouseEvent e)
  {
    if (LameFocusManager.FOCUSED_WINDOW != this)
    {
      LameFocusManager.grabFocus(this);
    }

    if (mouseConsumer != null)
    {
      mouseConsumer.consume(e.getX(), e.getY());
    }
  }

  class GraphicsWindowPanel extends JPanel
  {
    public GraphicsWindowPanel()
    {
      super();
    }

    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (bi != null)
          g.drawImage(bi, 0, 0, this);

        change = false;
    }
  }
}
