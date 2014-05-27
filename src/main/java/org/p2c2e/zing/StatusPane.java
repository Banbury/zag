package org.p2c2e.zing;

import javax.swing.*;
import java.awt.*;

public class StatusPane extends JPanel
{
  public static Component BLANK = Box.createVerticalStrut(25);
  public static JLabel MORE = new JLabel("[More]", SwingConstants.LEFT);
  public static JLabel EXIT = new JLabel("[*** End of session ***]", 
                                  SwingConstants.LEFT);

  JProgressBar prog;
  Component current;

  StatusPane()
  {
    super();
    prog = new JProgressBar();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    show(BLANK);
  }

  public JProgressBar getProgressBar()
  {
    return prog;
  }

  public void show(Component l)
  {
    if (l == null)
      l = BLANK;

    if (l != current)
    {
      removeAll();
      add(l);
      add(BLANK);
      add(Box.createHorizontalGlue());
      add(prog);
      revalidate();
      repaint();
    }
  }
}
