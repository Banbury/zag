package org.p2c2e.zing;

import java.awt.geom.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.*;

public class Test
{
  public static void main(String[] argv) throws Exception
  {
    JFrame f = new JFrame();

    f.setSize(new Dimension(600, 700));
    f.getRootPane().setBorder(BorderFactory.createEtchedBorder());
    f.show();
    Glk.setFrame(f);


    Window w = Glk.windowOpen(null, 0, 0, Glk.WINTYPE_TEXT_GRID, 0);
    Stream s = w.getStream();
    Glk.streamSetCurrent(s);

    Glk.putChar('H');
    Glk.putChar('e');
    Glk.putChar('l');
    Glk.putChar('l');
    Glk.putChar('o');
    Glk.putChar(' ');
    Glk.setStyle(Glk.STYLE_EMPHASIZED);
    Glk.putChar('W');
    Glk.putChar('o');
    Glk.putChar('r');
    Glk.putChar('l');
    Glk.putChar('d');
    Glk.putChar('!');
    Glk.setStyle(Glk.STYLE_NORMAL);
    Glk.windowMoveCursor(w, 2, 1);
    Glk.putChar('J');
    Glk.putChar('o');
    Glk.putChar('n');
    Glk.windowMoveCursor(w, 18, 7);
    Glk.putChar('B');
    Glk.putChar('o');
    Glk.putChar('t');
    Glk.putChar('t');
    Glk.putChar('o');
    Glk.putChar('m');

    Window w2 = 
      Glk.windowOpen(w, Glk.WINMETHOD_PROPORTIONAL | Glk.WINMETHOD_ABOVE, 
                     30, Glk.WINTYPE_TEXT_GRID, 0);

    Glk.setWindow(w2);
    Glk.windowMoveCursor(w, 2, 1);
    Glk.putChar('F');
    Glk.putChar('o');
    Glk.putChar('o');


    Window w3 =
      Glk.windowOpen(w, Glk.WINMETHOD_FIXED | Glk.WINMETHOD_RIGHT, 5, 
                     Glk.WINTYPE_TEXT_GRID, 0);

    Glk.setWindow(w3);
    Glk.putChar('M');
    Glk.putChar('M');
    Glk.putChar('M');
    Glk.putChar('M');
    Glk.putChar('M');
    Glk.putChar('o');
    Glk.putChar('o');
    Glk.putChar('o');
    Glk.putChar('o');
    Glk.putChar('o');

    Window w4 =
      Glk.windowOpen(w2, Glk.WINMETHOD_PROPORTIONAL | Glk.WINMETHOD_LEFT, 50, 
                     Glk.WINTYPE_TEXT_BUFFER, 0);
    //    Image img = Toolkit.getDefaultToolkit().getImage("/Users/jaz/random/tpf.gif");
    //    Image img2 = Toolkit.getDefaultToolkit().getImage("/Users/jaz/random/wqxr-mkplace.gif");
    Glk.setWindow(w4);
    Glk.setStyle(Glk.STYLE_NORMAL);
    //    w4.drawImage(img, TextBufferWindow.MARGIN_LEFT);
    //    w4.drawImage(img2, TextBufferWindow.MARGIN_LEFT);
    //    w4.drawImage(img, TextBufferWindow.MARGIN_RIGHT);

    Glk.putString("This is the very first test of");
    Glk.putString(" putting text in a ");
    Glk.setHyperlink(10);
    Glk.putString("TextBufferWindow");
    Glk.setHyperlink(0);
    Glk.putString(". ");
    Glk.putString("We do hope it works, since otherwise much time--");
    Glk.setStyle(Glk.STYLE_EMPHASIZED);
    Glk.putString("very important time");
    Glk.setStyle(Glk.STYLE_NORMAL);
    Glk.putString("--was spent for nought.\n");
    Glk.putString("\nThis is the very first test of putting text in a ");
    Glk.setHyperlink(15);
    Glk.putString("TextBufferWindow");
    Glk.setHyperlink(0);

    Glk.putString(".  We do hope it works, since otherwise much time--");
    Glk.setStyle(Glk.STYLE_EMPHASIZED);
    Glk.putString("very important time");
    Glk.setStyle(Glk.STYLE_NORMAL);
    Glk.putString("--was spent for nought.\n");      

    Glk.setStyle(Glk.STYLE_BLOCKQUOTE);
    Glk.putString("This is the very first test of putting text in a TextBufferWindow.  We do hope it works, since otherwise much time--");
    Glk.setStyle(Glk.STYLE_EMPHASIZED);
    Glk.putString("very important time");
    Glk.setStyle(Glk.STYLE_BLOCKQUOTE);
    Glk.putString("--was spent for nought.\n");

    Glk.setStyle(Glk.STYLE_NORMAL);
    Glk.putString("This is the very first test of putting text in a TextBufferWindow.  We do hope it works, since otherwise much time--");
    Glk.setStyle(Glk.STYLE_EMPHASIZED);
    Glk.putString("very important time");
    Glk.setStyle(Glk.STYLE_NORMAL);
    Glk.windowFlowBreak(w4);
    Glk.putString("--was spent for nought.\n");
    //    w4.setStyle(ib);
    //    w4.putString("\n>");

//      GraphicsWindow w5 = (GraphicsWindow) Window.split(w, Window.PROPORTIONAL | Window.RIGHT, 50, true, Window.GRAPHICS);
//      w5.drawImage(img, 20, 20);
//      w5.drawImage(img2, -40, 60);
//      w5.setBackgroundColor(Color.red);
//      w5.eraseRect(200, 100, 100, 50);
//      w5.fillRect(Color.black, 250, 125, 100, 50);

    //    MyCharConsumer con = new MyCharConsumer(w4);
    //    MyLineConsumer lc = new MyLineConsumer(w4);
    //    w.setCursor(0, 3);
    //    w.requestLineInput(lc, "initial");
    //w.putChar('>');
    ByteBuffer b = ByteBuffer.allocate(64);
    b.put(0, (byte) 'w');
    b.put(1, (byte) 'e');
    b.put(2, (byte) 'l');
    b.put(3, (byte) 'l');
    b.put(4, (byte) ',');
    b.put(5, (byte) ' ');
    b.put(6, (byte) 'w');
    b.put(7, (byte) 'h');
    b.put(8, (byte) 'y');
    b.put(9, (byte) ' ');
    b.put(10, (byte) 'n');
    b.put(11, (byte) 'o');
    b.put(12, (byte) 't');
    b.put(13, (byte) '?');
    b.put(14, (byte) ' ');
    Glk.requestHyperlinkEvent(w4);
    Glk.requestLineEvent(w4, new InOutByteBuffer(b), 64, 15);
    //    Glk.requestTimerEvents(10000);
    
    Glk.GlkEvent ev = new Glk.GlkEvent();
    while (true)
    {
      Glk.select(ev);
      if (ev.type == Glk.EVTYPE_HYPERLINK)
        System.err.println("hyperlink: " + ev.val1);
      if (ev.type == Glk.EVTYPE_LINE_INPUT)
        break;
      if (ev.type == Glk.EVTYPE_TIMER)
      {
        Glk.cancelLineEvent(w4, null);
        Glk.putString("Time!\n");
        Glk.requestLineEvent(w4, new InOutByteBuffer(b), 64, 0);
      }        
    }

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < ev.val1; i++)
      sb.append((char) b.get(i));

    Glk.putString(sb.toString());

    Glk.windowClose(w.getParent(), null);


    Glk.requestLineEvent(w4, new InOutByteBuffer(ByteBuffer.allocate(64)), 64, 15);
    //    Glk.requestTimerEvents(10000);
    

    while (true)
    {
      Glk.select(ev);
      if (ev.type == Glk.EVTYPE_LINE_INPUT)
        break;
      if (ev.type == Glk.EVTYPE_TIMER)
      {
        Glk.cancelLineEvent(w4, null);
        Glk.putString("Time!\n");
        Glk.requestLineEvent(w4, new InOutByteBuffer(b), 64, 0);
      }        
    }

    Glk.windowClose(w2, null);

    //    Glk.exit();

    Class.forName("org.p2c2e.zing.Dispatch");
  }
}
