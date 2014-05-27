package org.p2c2e.zing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LameFocusManager
{
  static Window FOCUSED_WINDOW = null;
  static KeyListener KEY_LISTENER = new LameKeyListener();
  static StatusPane STATUS = new StatusPane();

  static synchronized void registerFrame(Container c, boolean statusOn)
  {
    c.addKeyListener(KEY_LISTENER);
    c.addComponentListener(new ResizeListener());

    RootPaneContainer rpc = (RootPaneContainer) c;
    rpc.getContentPane().setLayout(new BorderLayout());

    if (statusOn)
      rpc.getContentPane().add(STATUS, BorderLayout.SOUTH);

    rpc.getContentPane().repaint();
  }

  static void rootRearrange()
  {
    if (Window.root != null)
    {
      Window.FRAME.getContentPane().validate();
      Dimension d = Window.FRAME.getContentPane().getSize();
      Insets insets = Window.FRAME.getContentPane().getInsets();
      Window.root.rearrange(new Rectangle(0, 0, 
                                          d.width - insets.left - insets.right,
                                          d.height - insets.top - insets.bottom - STATUS.getHeight()));
    }
  }

  static synchronized boolean requestFocus(Window win)
  {
    if (FOCUSED_WINDOW == win)
    {
      return true;
    }
    else if (FOCUSED_WINDOW == null)
    {
      win.focusHighlight();
      FOCUSED_WINDOW = win;      
      return true;
    }
    else if (FOCUSED_WINDOW.isFocusStealable())
    {
      FOCUSED_WINDOW.unfocusHighlight();
      win.focusHighlight();
      FOCUSED_WINDOW = win;
      return true;
    }
    else
    {
      return false;
    }
  }

  static synchronized void grabFocus(Window win)
  {
    if (FOCUSED_WINDOW != null)
      FOCUSED_WINDOW.unfocusHighlight();
    win.focusHighlight();
    FOCUSED_WINDOW = win;
  }

  static class LameKeyListener implements KeyListener
  {
    public void keyPressed(KeyEvent e)
    {
      determineFocus();
      if (FOCUSED_WINDOW != null && e.getKeyChar() == KeyEvent.CHAR_UNDEFINED)
        FOCUSED_WINDOW.handleKey(e);
    }

    public void keyReleased(KeyEvent e)
    {

    }

    public void keyTyped(KeyEvent e)
    {
      determineFocus();
      if (FOCUSED_WINDOW != null)
        FOCUSED_WINDOW.handleKey(e);
    }

    static void determineFocus()
    {
      if (FOCUSED_WINDOW != null && FOCUSED_WINDOW.isRequestingKeyboardInput())
        return;

      Window w = pickWindow(Window.root);
      if (w != null)
        grabFocus(w);
    }

    static Window pickWindow(Window w)
    {
      if (w == null)
      {
        return null;
      }
      else if (w.isRequestingKeyboardInput())
      {
        return w;
      }
      else if (w instanceof PairWindow)
      {
        PairWindow pw = (PairWindow) w;
        Window nw = pickWindow(pw.first);
        if (nw == null)
          nw = pickWindow(pw.second);
        return nw;
      }
      else
      {
        return null;
      }
    }
  }
}
