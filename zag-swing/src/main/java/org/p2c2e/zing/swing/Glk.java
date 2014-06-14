package org.p2c2e.zing.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.util.ListIterator;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;

import org.p2c2e.blorb.BlorbFile;
import org.p2c2e.blorb.Color;
import org.p2c2e.util.Bytes;
import org.p2c2e.zing.AbstractGlk;
import org.p2c2e.zing.Fileref;
import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.IWindow;
import org.p2c2e.zing.ObjectCallback;
import org.p2c2e.zing.Style;
import org.p2c2e.zing.types.GlkEvent;
import org.p2c2e.zing.types.OutInt;
import org.p2c2e.zing.types.OutWindow;
import org.p2c2e.zing.types.StreamResult;

public class Glk extends AbstractGlk {
	private static Glk instance;

	public static Glk getInstance() {
		if (instance == null)
			instance = new Glk();

		return instance;
	}

	public void setFrame(JFrame frame) {
		setFrame(frame, true, true, null, null, -1, -1);
	}

	public void setFrame(JFrame frame, boolean statusOn, boolean bordersOn,
			String proportionalFont, String fixedFont, int propFontSize,
			int fixedFontSize) {
		reset();

		Window.setFrame(frame);

		BORDERS_ON = bordersOn;

		// a hack to get around abysmally poor font substitution performance
		if (proportionalFont != null
				&& (new Font(proportionalFont, Font.PLAIN, 1)).getName()
						.toLowerCase().equals(proportionalFont.toLowerCase())) {
			Window.DEFAULT_PROPORTIONAL_FONT = proportionalFont;
			Window.OVERRIDE_PROPORTIONAL_FONT = true;
		}

		if (fixedFont != null
				&& (new Font(fixedFont, Font.PLAIN, 1)).getName().toLowerCase()
						.equals(fixedFont.toLowerCase())) {
			Window.DEFAULT_FIXED_FONT = fixedFont;
			Window.OVERRIDE_FIXED_FONT = true;
		}

		if (propFontSize > 0) {
			Window.DEFAULT_PROP_FONT_SIZE = propFontSize;
			Window.OVERRIDE_PROP_FONT_SIZE = true;
		}
		if (fixedFontSize > 0) {
			Window.DEFAULT_FIXED_FONT_SIZE = fixedFontSize;
			Window.OVERRIDE_FIXED_FONT_SIZE = true;
		}

		LameFocusManager.registerFrame(frame, statusOn);

		TRACKER = new MediaTracker(frame);
		setupStyles(Glk.getInstance(), Window.getFrame());
	}

	@Override
	public void flush() {
		super.flush();

		if (Window.getRoot() != null)
			Window.getRoot().doLayout();
	}

	@Override
	public void reset() {
		super.reset();
		if (Window.getRoot() != null) {
			Window.close(Window.getRoot());
			Window.getFrame().getContentPane().validate();
			Window.getFrame().getContentPane().repaint();
		}
	}

	public StatusPane getStatusPane() {
		return LameFocusManager.STATUS;
	}

	@Override
	public void progress(String stJob, int min, int max, int cur) {
		JProgressBar prog = LameFocusManager.STATUS.getProgressBar();

		if (min == max && prog.isVisible()) {
			prog.setVisible(false);
			prog.setIndeterminate(false);
		} else {
			if (!prog.isVisible()) {
				prog.setVisible(true);
				prog.revalidate();
			}

			if (stJob != null) {
				prog.setStringPainted(true);
				prog.setString(stJob);
			} else {
				prog.setStringPainted(false);
			}

			if (min < max) {
				if (min >= 0)
					prog.setMinimum(min);
				if (max >= 0)
					prog.setMaximum(max);

				prog.setValue(cur);
			} else {
				prog.setIndeterminate(true);
			}
		}
	}

	@Override
	public void setMorePromptCallback(ObjectCallback c) {
		TextBufferWindow.MORE_CALLBACK = c;
	}

	@Override
	public IWindow windowGetRoot() {
		return Window.getRoot();
	}

	@Override
	public void windowGetArrangement(IWindow win, OutInt method, OutInt size,
			OutWindow key) {
		if (win == null) {
			nullRef("Glk.windowGetArrangement");
		} else {
			PairWindow w = (PairWindow) win;
			if (method != null)
				method.val = w.getSplitMethod();
			if (size != null)
				size.val = w.getKeyWindowSize();
			if (key != null)
				key.window = w.key;
		}
	}

	@Override
	public void windowSetArrangement(IWindow win, int method, int size,
			IWindow newKey) {
		if (win == null)
			nullRef("Glk.windowSetArrangement");
		else
			((PairWindow) win).setArrangement(method, size, newKey);
	}

	@Override
	public int windowGetType(IWindow win) {
		if (win instanceof TextBufferWindow)
			return WINTYPE_TEXT_BUFFER;
		if (win instanceof TextGridWindow)
			return WINTYPE_TEXT_GRID;
		if (win instanceof PairWindow)
			return WINTYPE_PAIR;
		if (win instanceof GraphicsWindow)
			return WINTYPE_GRAPHICS;
		if (win instanceof BlankWindow)
			return WINTYPE_BLANK;

		if (win == null)
			nullRef("Glk.windowGetType");

		return -1;
	}

	@Override
	public int windowGetRock(IWindow w) {
		if (w == null) {
			nullRef("Glk.windowGetRock");
			return 0;
		}

		if (w instanceof PairWindow)
			return 0;

		return windows.get(w).intValue();
	}

	@Override
	public IWindow windowOpen(IWindow w, int method, int size, int wintype,
			int rock) {
		Window win = Window
				.split((Window) w, method, size, BORDERS_ON, wintype);

		windows.put(win, new Integer(rock));
		streams.put(win.stream, new Integer(0));
		if (w != null) {
			windows.put(win.getParent(), new Integer(0));
			streams.put(win.getParent().stream, new Integer(0));
		}
		if (CREATE_CALLBACK != null) {
			CREATE_CALLBACK.callback(win);
			CREATE_CALLBACK.callback(win.stream);
			if (w != null) {
				CREATE_CALLBACK.callback(win.getParent());
				CREATE_CALLBACK.callback(win.getParent().stream);
			}
		}
		return win;
	}

	@Override
	public void windowClose(IWindow w, StreamResult streamresult) {
		if (w == null) {
			nullRef("Glk.windowClose");
			return;
		}

		StreamResult r = Window.close(w);
		if (streamresult != null) {
			streamresult.readcount = r.readcount;
			streamresult.writecount = r.writecount;
		}
		windowCloseRecurse(w);
	}

	protected void windowCloseRecurse(IWindow w) {
		IWindow wnd = w;

		if (wnd instanceof PairWindow) {
			PairWindow pw = (PairWindow) wnd;
			windowCloseRecurse(pw.first);
			windowCloseRecurse(pw.second);
		}

		windows.remove(wnd);
		streams.remove(wnd.getStream());
		if (DESTROY_CALLBACK != null) {
			DESTROY_CALLBACK.callback(w);
			DESTROY_CALLBACK.callback(wnd.getStream());
		}
	}

	@Override
	public boolean imageDraw(IWindow win, int imgid, int val1, int val2) {
		Image img = getImage(imgid, -1, -1);
		if (img == null)
			return false;

		if (win instanceof TextBufferWindow) {
			((TextBufferWindow) win).drawImage(img, val1);
			return true;
		}
		if (win instanceof GraphicsWindow) {
			((GraphicsWindow) win).drawImage(img, val1, val2);
			return true;
		}
		return false;
	}

	@Override
	public boolean imageDrawScaled(IWindow win, int imgid, int val1, int val2,
			int width, int height) {
		Image img = getImage(imgid, width, height);
		if (img == null)
			return false;

		if (win instanceof TextBufferWindow) {
			((TextBufferWindow) win).drawImage(img, val1);
			return true;
		}
		if (win instanceof GraphicsWindow) {
			((GraphicsWindow) win).drawImage(img, val1, val2);
			return true;
		}
		return false;
	}

	@Override
	public boolean imageGetInfo(int imgid, OutInt width, OutInt height) {
		if (blorbFile != null) {
			Image img = getImage(imgid, -1, -1);
			if (img != null) {
				if (width != null)
					width.val = img.getWidth((Component) Window.getFrame());
				if (height != null)
					height.val = img.getHeight((Component) Window.getFrame());
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public Image getImage(int id, int xscale, int yscale) {
		ImageCacheNode n = null;
		Image img = null;
		int nodes = imageCache.size();
		ListIterator<ImageCacheNode> it = imageCache.listIterator(nodes);

		if (it.hasPrevious()) {
			n = it.previous();

			while (n.id != id && it.hasPrevious())
				n = it.previous();
		}

		if (n != null && n.id == id) {
			if (xscale >= 0) {
				boolean found = false;

				if (n.scaled != null) {
					img = n.scaled;
					found |= (img.getWidth(Window.getRoot().panel) == xscale && img
							.getHeight(Window.getRoot().panel) == yscale);
				}

				if (!found) {
					img = n.normal.getScaledInstance(xscale, yscale,
							Image.SCALE_SMOOTH);
					try {
						TRACKER.addImage(img, id);
						TRACKER.waitForID(id);
						TRACKER.removeImage(img);
					} catch (InterruptedException eI) {
						eI.printStackTrace();
					}
					n.scaled = img;
				}
				return img;
			} else {
				return n.normal;
			}
		}

		try {
			BlorbFile.Chunk chunk = blorbFile.getByUsage(BlorbFile.PICT, id);
			if (chunk == null)
				return null;

			byte[] arr = Bytes.getBytes(chunk.getData());
			img = Toolkit.getDefaultToolkit().createImage(arr);

			TRACKER.addImage(img, id);
			TRACKER.waitForID(id);
			TRACKER.removeImage(img);

			n = new ImageCacheNode();
			n.id = id;
			n.normal = img;
			if (nodes == 20)
				imageCache.removeFirst();
			imageCache.add(n);

			if (xscale >= 0) {
				img = img.getScaledInstance(xscale, yscale, Image.SCALE_SMOOTH);
				TRACKER.addImage(img, id);
				TRACKER.waitForID(id);
				TRACKER.removeImage(img);
				n.scaled = img;
			}

			return img;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void nullRef(String func) {
		switch (STRICTNESS) {
		case STRICTNESS_DIE:
			throw new NullPointerException("Invalid object reference: " + func);
		case STRICTNESS_WARN:
			if (TextBufferWindow.MORE_CALLBACK != null)
				TextBufferWindow.MORE_CALLBACK.callback(new JLabel(
						"Illegal obj ref: " + func, SwingConstants.LEFT));
			else
				JOptionPane.showMessageDialog((Frame) Window.getFrame(),
						"Warning: the program has illegally "
								+ "referenced a null object in the "
								+ "function '" + func + "'.",
						"Null object reference", JOptionPane.ERROR_MESSAGE);
			break;
		default:
			// NOOP
		}
	}

	@Override
	public Fileref filerefCreateByPrompt(int usage, int fmode, int rock) {
		String name = null;

		JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

		if (fmode == Fileref.FILEMODE_WRITE
				|| fmode == Fileref.FILEMODE_WRITEAPPEND) {
			if (fc.showSaveDialog(Window.getFrame().getRootPane().getParent()) == JFileChooser.APPROVE_OPTION) {
				name = fc.getName();
			}
		} else {
			if (fc.showOpenDialog(Window.getFrame().getRootPane().getParent()) == JFileChooser.APPROVE_OPTION) {
				name = fc.getName();
			}
		}
		return filerefCreateByName(usage, name, rock);
	}

	@Override
	public void select(GlkEvent e) {
		if (Window.getRoot() != null)
			Window.getRoot().doLayout();
		super.select(e);
	}

	@Override
	public void selectPoll(GlkEvent e) {
		if (Window.getRoot() != null)
			Window.getRoot().doLayout();
		super.selectPoll(e);
	}

	private static void setupStyles(IGlk glk, RootPaneContainer f) {
		FontRenderContext frc = ((Graphics2D) f.getContentPane().getGraphics())
				.getFontRenderContext();
		frc = new FontRenderContext(null, true, true);

		Preferences stylep = Preferences.userRoot().node(
				"/org/p2c2e/zing/style");
		Preferences gridp = stylep.node("grid");
		Preferences bufp = stylep.node("buffer");

		Style.USE_HINTS = stylep.getBoolean("use-hints", true);

		for (int i = 0; i < IGlk.STYLE_NUMSTYLES; i++) {
			Style.addStyle(
					constructStyle(glk, frc, gridp, i, IGlk.WINTYPE_TEXT_GRID),
					IGlk.WINTYPE_TEXT_GRID);
			Style.addStyle(
					constructStyle(glk, frc, bufp, i, IGlk.WINTYPE_TEXT_BUFFER),
					IGlk.WINTYPE_TEXT_BUFFER);
		}
	}

	private static Style constructStyle(IGlk glk, FontRenderContext frc,
			Preferences p, int i, int type) {
		String stName = IGlk.STYLES[i];
		p = p.node(stName);

		String stFam;
		int iSize;

		if (Window.OVERRIDE_PROPORTIONAL_FONT
				&& type == IGlk.WINTYPE_TEXT_BUFFER
				&& i != IGlk.STYLE_PREFORMATTED)
			stFam = Window.DEFAULT_PROPORTIONAL_FONT;
		else if (Window.OVERRIDE_FIXED_FONT
				&& (type == IGlk.WINTYPE_TEXT_GRID || i == IGlk.STYLE_PREFORMATTED))
			stFam = Window.DEFAULT_FIXED_FONT;
		else
			stFam = p
					.get("typeface",
							(type == IGlk.WINTYPE_TEXT_GRID || i == IGlk.STYLE_PREFORMATTED) ? Window.DEFAULT_FIXED_FONT
									: Window.DEFAULT_PROPORTIONAL_FONT);

		if (Window.OVERRIDE_PROP_FONT_SIZE && type == IGlk.WINTYPE_TEXT_BUFFER
				&& i != IGlk.STYLE_PREFORMATTED)
			iSize = Window.DEFAULT_PROP_FONT_SIZE;
		else if (Window.OVERRIDE_FIXED_FONT_SIZE
				&& (type == IGlk.WINTYPE_TEXT_GRID || i == IGlk.STYLE_PREFORMATTED))
			iSize = Window.DEFAULT_FIXED_FONT_SIZE;
		else
			iSize = p
					.getInt("font-size",
							(type == IGlk.WINTYPE_TEXT_GRID) ? Window.DEFAULT_FIXED_FONT_SIZE
									: Window.DEFAULT_PROP_FONT_SIZE);
		Float ofWeight = new Float(
				p.getFloat(
						"font-weight",
						(i == IGlk.STYLE_INPUT || i == IGlk.STYLE_SUBHEADER) ? TextAttribute.WEIGHT_BOLD
								.floatValue() : TextAttribute.WEIGHT_REGULAR
								.floatValue()));

		boolean bItalic = p
				.getBoolean(
						"font-italic",
						(i == IGlk.STYLE_EMPHASIZED || (type == IGlk.WINTYPE_TEXT_GRID && (i == IGlk.STYLE_ALERT || i == IGlk.STYLE_NOTE))));
		boolean bUnderlined = p.getBoolean("font-underline", false);
		int iLeft = p
				.getInt("left-indent",
						(type == IGlk.WINTYPE_TEXT_BUFFER && i == IGlk.STYLE_BLOCKQUOTE) ? 2
								: 0);
		int iRight = p
				.getInt("right-indent",
						(type == IGlk.WINTYPE_TEXT_BUFFER && i == IGlk.STYLE_BLOCKQUOTE) ? 2
								: 0);
		int iPar = p
				.getInt("paragraph-indent",
						(type == IGlk.WINTYPE_TEXT_BUFFER && (i != IGlk.STYLE_HEADER
								&& i != IGlk.STYLE_SUBHEADER
								&& i != IGlk.STYLE_PREFORMATTED
								&& i != IGlk.STYLE_BLOCKQUOTE && i != IGlk.STYLE_INPUT)) ? 1
								: 0);
		int iJust = p
				.getInt("justification",
						(type == IGlk.WINTYPE_TEXT_BUFFER && (i != IGlk.STYLE_HEADER
								&& i != IGlk.STYLE_SUBHEADER
								&& i != IGlk.STYLE_PREFORMATTED && i != IGlk.STYLE_INPUT)) ? Style.LEFT_RIGHT_FLUSH
								: Style.LEFT_FLUSH);

		Color cText;
		int iText = glk.colorToInt(new Color(0, 0, 0));
		if (type == IGlk.WINTYPE_TEXT_GRID && i == IGlk.STYLE_ALERT) {
			iText = glk.colorToInt(new Color(255, 0, 0));
		} else if (type == IGlk.WINTYPE_TEXT_BUFFER) {
			if (i == IGlk.STYLE_ALERT)
				iText = glk.colorToInt(new Color(255, 0, 0));
			else if (i == IGlk.STYLE_NOTE)
				iText = glk.colorToInt(new Color(255, 0, 255));
		}

		cText = glk.intToColor(p.getInt("text-color", iText));

		Color cBack = glk.intToColor(p.getInt("back-color", 0x00ffffff));

		Style style = new Style(stName, stFam, iSize, ofWeight, bItalic,
				bUnderlined, iLeft, iRight, iPar, iJust, cText, cBack);

		Font testfont = new Font(style.getMap());
		double w1 = testfont.getStringBounds("m", frc).getWidth();
		double w2 = testfont.getStringBounds("i", frc).getWidth();
		style.setMonospace((w1 == w2));

		return style;
	}
}
