package org.p2c2e.zing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ResizeListener extends ComponentAdapter 
  implements HierarchyListener
{
  public void componentResized(ComponentEvent e)
  {
    if (Window.root != null)
      LameFocusManager.rootRearrange();

    Glk.GlkEvent ev = new Glk.GlkEvent();
    ev.type = Glk.EVTYPE_ARRANGE;
    ev.win = null;
    ev.val1 = 0;
    ev.val2 = 0;
    Glk.addEvent(ev);
  }

  public void hierarchyChanged(HierarchyEvent e)
  {
    if ((e.getChangeFlags() & HierarchyEvent.ANCESTOR_RESIZED) != 0)
    {
      //    if (Window.root != null)
        //      LameFocusManager.rootRearrange();

    Glk.GlkEvent ev = new Glk.GlkEvent();
    ev.type = Glk.EVTYPE_ARRANGE;
    ev.win = null;
    ev.val1 = 0;
    ev.val2 = 0;
    Glk.addEvent(ev);
    }
  }
}
