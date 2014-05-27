package org.p2c2e.zing;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class MaskArea extends Area
{
  static final int TRANS = 0xffffffff;

  BufferedImage bi;

  public MaskArea(Image img)
  {
    super();

    int c;
    int w = img.getWidth(null);
    int h = img.getHeight(null);

    bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = bi.createGraphics();
    g.drawImage(img, 0, 0, null);

    int[] pixels = bi.getRGB(0, 0, w, h, null, 0, w);

    // algorithm thanks to David Kinder
    for (int y = 0; y < h; y++)
    {
      Glk.progress("Preparing mask...", 0, h, y);

      int left = -1;
      for (int x = 0; x < w + 1; x++)
      {
        c = (x < w) ? pixels[(y * w) + x] : TRANS;

        if (left >= 0)
        {
          if (c == TRANS)
          {
            Rectangle rect = new Rectangle(left, y, x - left, 1);
            add(new Area(rect));
            left = -1;
          }
        }
        else
        {
          if (c != TRANS)
            left = x;
        }
      }
      Glk.progress(null, 0, 0, 0);
    }
  }
}
