package org.p2c2e.zing;

import java.awt.Dimension;
import java.nio.ByteBuffer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import org.p2c2e.zing.swing.Glk;

public class Test {
	public static void main(String[] argv) throws Exception {
		JFrame f = new JFrame();

		f.setSize(new Dimension(600, 700));
		f.getRootPane().setBorder(BorderFactory.createEtchedBorder());
		f.setVisible(true);
		Glk glk = Glk.getInstance();
		glk.setFrame(f);

		IWindow w = glk.windowOpen(null, 0, 0, glk.WINTYPE_TEXT_GRID, 0);
		Stream s = w.getStream();
		glk.streamSetCurrent(s);

		glk.putChar('H');
		glk.putChar('e');
		glk.putChar('l');
		glk.putChar('l');
		glk.putChar('o');
		glk.putChar(' ');
		glk.setStyle(glk.STYLE_EMPHASIZED);
		glk.putChar('W');
		glk.putChar('o');
		glk.putChar('r');
		glk.putChar('l');
		glk.putChar('d');
		glk.putChar('!');
		glk.setStyle(glk.STYLE_NORMAL);
		glk.windowMoveCursor(w, 2, 1);
		glk.putChar('J');
		glk.putChar('o');
		glk.putChar('n');
		glk.windowMoveCursor(w, 18, 7);
		glk.putChar('B');
		glk.putChar('o');
		glk.putChar('t');
		glk.putChar('t');
		glk.putChar('o');
		glk.putChar('m');

		IWindow w2 = glk.windowOpen(w, glk.WINMETHOD_PROPORTIONAL
				| glk.WINMETHOD_ABOVE, 30, glk.WINTYPE_TEXT_GRID, 0);

		glk.setWindow(w2);
		glk.windowMoveCursor(w, 2, 1);
		glk.putChar('F');
		glk.putChar('o');
		glk.putChar('o');

		IWindow w3 = glk.windowOpen(w, glk.WINMETHOD_FIXED
				| glk.WINMETHOD_RIGHT, 5, glk.WINTYPE_TEXT_GRID, 0);

		glk.setWindow(w3);
		glk.putChar('M');
		glk.putChar('M');
		glk.putChar('M');
		glk.putChar('M');
		glk.putChar('M');
		glk.putChar('o');
		glk.putChar('o');
		glk.putChar('o');
		glk.putChar('o');
		glk.putChar('o');

		IWindow w4 = glk.windowOpen(w2, glk.WINMETHOD_PROPORTIONAL
				| glk.WINMETHOD_LEFT, 50, glk.WINTYPE_TEXT_BUFFER, 0);
		// Image img =
		// Toolkit.getDefaultToolkit().getImage("/Users/jaz/random/tpf.gif");
		// Image img2 =
		// Toolkit.getDefaultToolkit().getImage("/Users/jaz/random/wqxr-mkplace.gif");
		glk.setWindow(w4);
		glk.setStyle(glk.STYLE_NORMAL);
		// w4.drawImage(img, TextBufferWindow.MARGIN_LEFT);
		// w4.drawImage(img2, TextBufferWindow.MARGIN_LEFT);
		// w4.drawImage(img, TextBufferWindow.MARGIN_RIGHT);

		glk.putString("This is the very first test of");
		glk.putString(" putting text in a ");
		glk.setHyperlink(10);
		glk.putString("TextBufferWindow");
		glk.setHyperlink(0);
		glk.putString(". ");
		glk.putString("We do hope it works, since otherwise much time--");
		glk.setStyle(glk.STYLE_EMPHASIZED);
		glk.putString("very important time");
		glk.setStyle(glk.STYLE_NORMAL);
		glk.putString("--was spent for nought.\n");
		glk.putString("\nThis is the very first test of putting text in a ");
		glk.setHyperlink(15);
		glk.putString("TextBufferWindow");
		glk.setHyperlink(0);

		glk.putString(".  We do hope it works, since otherwise much time--");
		glk.setStyle(glk.STYLE_EMPHASIZED);
		glk.putString("very important time");
		glk.setStyle(glk.STYLE_NORMAL);
		glk.putString("--was spent for nought.\n");

		glk.setStyle(glk.STYLE_BLOCKQUOTE);
		glk.putString("This is the very first test of putting text in a TextBufferWindow.  We do hope it works, since otherwise much time--");
		glk.setStyle(glk.STYLE_EMPHASIZED);
		glk.putString("very important time");
		glk.setStyle(glk.STYLE_BLOCKQUOTE);
		glk.putString("--was spent for nought.\n");

		glk.setStyle(glk.STYLE_NORMAL);
		glk.putString("This is the very first test of putting text in a TextBufferWindow.  We do hope it works, since otherwise much time--");
		glk.setStyle(glk.STYLE_EMPHASIZED);
		glk.putString("very important time");
		glk.setStyle(glk.STYLE_NORMAL);
		glk.windowFlowBreak(w4);
		glk.putString("--was spent for nought.\n");
		// w4.setStyle(ib);
		// w4.putString("\n>");

		// GraphicsWindow w5 = (GraphicsWindow) Window.split(w,
		// Window.PROPORTIONAL | Window.RIGHT, 50, true, Window.GRAPHICS);
		// w5.drawImage(img, 20, 20);
		// w5.drawImage(img2, -40, 60);
		// w5.setBackgroundColor(Color.red);
		// w5.eraseRect(200, 100, 100, 50);
		// w5.fillRect(Color.black, 250, 125, 100, 50);

		// MyCharConsumer con = new MyCharConsumer(w4);
		// MyLineConsumer lc = new MyLineConsumer(w4);
		// w.setCursor(0, 3);
		// w.requestLineInput(lc, "initial");
		// w.putChar('>');
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
		glk.requestHyperlinkEvent(w4);
		glk.requestLineEvent(w4, new InOutByteBuffer(b), 64, 15);
		// glk.requestTimerEvents(10000);

		Glk.GlkEvent ev = new Glk.GlkEvent();
		while (true) {
			glk.select(ev);
			if (ev.type == glk.EVTYPE_HYPERLINK)
				System.err.println("hyperlink: " + ev.val1);
			if (ev.type == glk.EVTYPE_LINE_INPUT)
				break;
			if (ev.type == glk.EVTYPE_TIMER) {
				glk.cancelLineEvent(w4, null);
				glk.putString("Time!\n");
				glk.requestLineEvent(w4, new InOutByteBuffer(b), 64, 0);
			}
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ev.val1; i++)
			sb.append((char) b.get(i));

		glk.putString(sb.toString());

		glk.windowClose(w.getParent(), null);

		glk.requestLineEvent(w4, new InOutByteBuffer(ByteBuffer.allocate(64)),
				64, 15);
		// glk.requestTimerEvents(10000);

		while (true) {
			glk.select(ev);
			if (ev.type == glk.EVTYPE_LINE_INPUT)
				break;
			if (ev.type == glk.EVTYPE_TIMER) {
				glk.cancelLineEvent(w4, null);
				glk.putString("Time!\n");
				glk.requestLineEvent(w4, new InOutByteBuffer(b), 64, 0);
			}
		}

		glk.windowClose(w2, null);

		// glk.exit();

		Class.forName("org.p2c2e.zing.Dispatch");
	}
}
