package org.p2c2e.zing.swing;

import org.p2c2e.zing.IWindow;

public class OutWindow
{
  public IWindow window;

  public OutWindow()
  {
    this(null);
  }

  public OutWindow(IWindow w)
  {
    window = w;
  }
}
