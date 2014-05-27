package org.p2c2e.zing;

import java.awt.*;
import java.awt.font.*;
import javax.swing.*;

final class TextSplitMeasurer
{
  static final Insets insets = new Insets(0, 0, 0, 0);


  synchronized static final int getSplit(JComponent panel, int size, int axis, 
                                         FontRenderContext frc, Style normal,
                                         char[] testArray)
  {
    int val;
    int insetVal;

    panel.getInsets(insets);
    if (axis == BoxLayout.X_AXIS)
    {
      // we have a horizontal layout (i.e., a vertical split), and size
      // represents a number of columns
      val = (int) (new Font(normal.getMap())).getStringBounds(testArray, 0, 1, frc).getWidth();
      //      GlyphVector v = (new Font(normal.getMap())).createGlyphVector(frc, testArray);
    
      //      val = (int) v.getGlyphMetrics(0).getAdvance();
      insetVal = insets.left + insets.right;
    }
    else
    {
      // rows
      // "Hag" includes letters with ascenders and descenders
      LineMetrics m = (new Font(normal.getMap())).getLineMetrics("Hag", frc);
      val = (int) m.getHeight() + 2;
      insetVal = insets.top + insets.bottom;
    }
    
    return (val * size) + insetVal;
  }
}
