package org.p2c2e.zing.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.util.ListIterator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.p2c2e.blorb.BlorbFile;
import org.p2c2e.util.Bytes;
import org.p2c2e.zing.AbstractGlk;
import org.p2c2e.zing.Fileref;
import org.p2c2e.zing.IWindow;
import org.p2c2e.zing.ObjectCallback;
import org.p2c2e.zing.Style;
import org.p2c2e.zing.streams.Stream;
import org.p2c2e.zing.types.OutInt;
import org.p2c2e.zing.types.OutWindow;
import org.p2c2e.zing.types.StreamResult;

import com.sixlegs.image.png.PngImage;

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
		Style.setupStyles(Glk.getInstance(), Window.getFrame());
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
		JProgressBar prog = LameFocusManager.STATUS.prog;

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
	public void windowGetSize(IWindow win, OutInt b1, OutInt b2) {
		if (win != null) {
			if (b1 != null)
				b1.val = win.getWindowWidth();
			if (b2 != null)
				b2.val = win.getWindowHeight();
		} else {
			nullRef("Glk.windowGetSize");
		}
	}

	@Override
	public IWindow windowGetSibling(IWindow win) {
		if (win != null)
			return win.getSibling();

		nullRef("Glk.windowGetSibling");
		return null;
	}

	@Override
	public IWindow windowGetParent(IWindow win) {
		if (win != null)
			return win.getParent();

		nullRef("Glk.widowGetParent");
		return null;
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

		return ((Integer) WINDOWS.get(w)).intValue();
	}

	@Override
	public void windowClear(IWindow win) {
		if (win == null)
			nullRef("Glk.windowClear");
		else
			win.clear();
	}

	@Override
	public IWindow windowOpen(IWindow w, int method, int size, int wintype,
			int rock) {
		Window win = Window
				.split((Window) w, method, size, BORDERS_ON, wintype);

		WINDOWS.put(win, new Integer(rock));
		STREAMS.put(win.stream, new Integer(0));
		if (w != null) {
			WINDOWS.put(win.getParent(), new Integer(0));
			STREAMS.put(win.getParent().stream, new Integer(0));
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

		WINDOWS.remove(wnd);
		STREAMS.remove(wnd.getStream());
		if (DESTROY_CALLBACK != null) {
			DESTROY_CALLBACK.callback(w);
			DESTROY_CALLBACK.callback(wnd.getStream());
		}
	}

	@Override
	public void windowSetEchoStream(IWindow win, Stream s) {
		if (win == null)
			nullRef("Glk.windowSetEchoStream");
		else
			win.setEchoStream(s);
	}

	@Override
	public Stream windowGetEchoStream(IWindow win) {
		if (win == null) {
			nullRef("Glk.windowGetEchoStream");
			return null;
		} else {
			return win.getEchoStream();
		}
	}

	@Override
	public Stream windowGetStream(IWindow win) {
		if (win == null) {
			nullRef("Glk.windowGetStream");
			return null;
		} else {
			return win.getStream();
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
		int nodes = IMAGE_CACHE.size();
		ListIterator it = IMAGE_CACHE.listIterator(nodes);

		if (it.hasPrevious()) {
			n = (ImageCacheNode) it.previous();

			while (n.id != id && it.hasPrevious())
				n = (ImageCacheNode) it.previous();
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

			if (BlorbFile.PNG.equals(chunk.getDataType())) {
				img = Toolkit.getDefaultToolkit().createImage(
						new PngImage(chunk.getData()));
			} else {
				byte[] arr = Bytes.getBytes(chunk.getData());
				img = Toolkit.getDefaultToolkit().createImage(arr);
			}

			TRACKER.addImage(img, id);
			TRACKER.waitForID(id);
			TRACKER.removeImage(img);

			n = new ImageCacheNode();
			n.id = id;
			n.normal = img;
			if (nodes == 20)
				IMAGE_CACHE.removeFirst();
			IMAGE_CACHE.add(n);

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
}
