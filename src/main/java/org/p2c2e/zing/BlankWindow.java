package org.p2c2e.zing;

import java.awt.font.*;
import java.awt.*;
import javax.swing.JPanel;

public class BlankWindow extends Window
{
  public BlankWindow(FontRenderContext c)
  {
    super(c);
    panel = new JPanel();
  }

  protected int getWindowType()
  {
    return BLANK;
  }

  protected void rearrange(Rectangle r)
  {

  }
}

