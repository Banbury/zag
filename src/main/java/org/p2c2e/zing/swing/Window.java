package org.p2c2e.zing.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.p2c2e.zing.CharInputConsumer;
import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.IWindow;
import org.p2c2e.zing.Int;
import org.p2c2e.zing.LineInputConsumer;
import org.p2c2e.zing.MouseInputConsumer;
import org.p2c2e.zing.Stream;
import org.p2c2e.zing.Style;
import org.p2c2e.zing.StyleHints;

public abstract class Window implements MouseListener, Comparable, IWindow {
	public static String DEFAULT_PROPORTIONAL_FONT = "Serif";
	public static String DEFAULT_FIXED_FONT = "Monospaced";
	public static int DEFAULT_PROP_FONT_SIZE = 14;
	public static int DEFAULT_FIXED_FONT_SIZE = 14;
	public static boolean OVERRIDE_PROPORTIONAL_FONT = false;
	public static boolean OVERRIDE_FIXED_FONT = false;
	public static boolean OVERRIDE_PROP_FONT_SIZE = false;
	public static boolean OVERRIDE_FIXED_FONT_SIZE = false;

	public static final Color HIGHLIGHT_COLOR = new Color(0x99, 0x99, 0xcc);
	public static final Color HIGHLIGHT_SHADOW = new Color(0x66, 0x66, 0x99);

	private static RootPaneContainer FRAME;
	private static Window root;

	FontRenderContext frc;
	TreeMap hintedStyles;
	TreeMap mHints;
	Style curStyle;
	PairWindow parent;
	Rectangle bbox;
	JComponent panel;

	Stream stream;
	Stream echo;

	public Window(FontRenderContext context) {
		frc = context;
		bbox = new Rectangle();
		mHints = StyleHints.getHints(getWindowType());
		hintedStyles = new TreeMap();
		createHintedStyles(getStyleMap(), Style.USE_HINTS);
		curStyle = (Style) hintedStyles.get("normal");
		stream = new Stream.WindowStream(this);
	}

	protected int getWindowType() {
		return 0;
	}

	protected void restyle(boolean useHints) {
		createHintedStyles(getStyleMap(), useHints);
		if (curStyle != null)
			curStyle = (Style) hintedStyles.get(curStyle.name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#getStream()
	 */
	public Stream getStream() {
		return stream;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#getEchoStream()
	 */
	public Stream getEchoStream() {
		return echo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#setEchoStream(org.p2c2e.zing.Stream)
	 */
	public void setEchoStream(Stream s) {
		if (s != stream)
			echo = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#getWindowWidth()
	 */
	public int getWindowWidth() {
		if (panel != null)
			return panel.getWidth();
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#getWindowHeight()
	 */
	public int getWindowHeight() {
		if (panel != null)
			return panel.getHeight();
		return 0;
	}

	public void doLayout() {
		panel.repaint();
	}

	protected boolean isRequestingKeyboardInput() {
		return false;
	}

	public void setStyle(Style style) {
		if (style != null)
			curStyle = style;
	}

	public TreeMap getHintedStyles() {
		return hintedStyles;
	}

	public void putChar(int c) {
		// NOOP
	}

	public void putCharUni(int c) {
		// NOOP
	}

	public void putString(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++)
			putChar(s.charAt(i));
	}

	public void clear() {

	}

	public boolean measureStyle(String stName, int hint, Int b) {
		return false;
	}

	protected void createHintedStyles(Map styles, boolean useHints) {
		Style s;
		Style hs;
		Iterator it = styles.values().iterator();

		while (it.hasNext()) {
			s = (Style) it.next();
			hs = (useHints) ? createHintedStyle(s) : s;
			hintedStyles.put(hs.name, hs);
		}
	}

	// Each subclass of Window should implement this. The idea is that Grid
	// windows, for example, should not honor all hints (e.g. proportional
	// fonts)
	// that Buffer windows should.
	protected Style createHintedStyle(Style style) {
		return style;
	}

	protected Map getStyleMap() {
		return Collections.EMPTY_MAP;
	}

	public static Window getRoot() {
		return root;
	}

	public static RootPaneContainer getFrame() {
		return FRAME;
	}

	public static void setFrame(RootPaneContainer frame) {
		FRAME = frame;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#getParent()
	 */
	public PairWindow getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#getSibling()
	 */
	public IWindow getSibling() {
		if (parent == null)
			return null;
		if (parent.first == this)
			return parent.second;
		else
			return parent.first;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#closeStream()
	 */
	public Stream.Result closeStream() {
		return stream.close();
	}

	public static Stream.Result close(IWindow w) {
		PairWindow grand;
		Window sibling;
		PairWindow p = w.getParent();

		if (p == null) {
			FRAME.getContentPane().remove(root.panel);
			root = null;
		} else {
			grand = p.getParent();
			sibling = (Window) w.getSibling();

			if (grand == null) {
				FRAME.getContentPane().remove(root.panel);
				FRAME.getContentPane().add(sibling.panel, BorderLayout.CENTER);
				root = sibling;
				sibling.parent = null;
			} else {
				if (grand.first == p)
					grand.set(sibling, grand.second);
				else
					grand.set(grand.first, sibling);

				if (grand.key == w)
					grand.key = null;
			}
		}

		if (root != null) {
			LameFocusManager.rootRearrange();
			root.panel.repaint();
		}

		return w.closeStream();
	}

	public static Window split(Window src, int method, int size,
			boolean border, int winType) {
		PairWindow oldPair;
		PairWindow newPair = null;
		Window w;
		FontRenderContext context = ((Graphics2D) FRAME.getContentPane()
				.getGraphics()).getFontRenderContext();

		switch (winType) {
		case BLANK:
			w = new BlankWindow(context);
			break;
		case PAIR:
			throw new RuntimeException("Pair windows cannot be leaf nodes.");
		case TEXT_BUFFER:
			w = new TextBufferWindow(context);
			break;
		case TEXT_GRID:
			w = new TextGridWindow(context);
			break;
		case GRAPHICS:
			w = new GraphicsWindow(context);
			break;
		default:
			throw new RuntimeException("Attempt to create unknown type ("
					+ winType + ") of window.");
		}

		if (border)
			w.panel.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));

		if (root == null) {
			root = w;
			w.panel.setPreferredSize(FRAME.getContentPane().getSize());
			FRAME.getContentPane().add(root.panel, BorderLayout.CENTER);
		} else {

			newPair = new PairWindow(context, method & 0x0f);
			oldPair = src.parent;
			newPair.parent = oldPair;
			newPair.key = w;
			newPair.keySize = size;
			// newPair.borderWidth = border;

			// are we splitting the root window?
			if (oldPair == null) {
				root = newPair;
				FRAME.getContentPane().remove(src.panel);
				FRAME.getContentPane().add(newPair.panel, BorderLayout.CENTER);
			} else {
				oldPair.replace(src, newPair);
			}

			if ((method & 0x0f) == LEFT || (method & 0x0f) == ABOVE)
				newPair.set(w, src);
			else
				newPair.set(src, w);

			if ((method & FIXED) != 0)
				newPair.keySizeType = FIXED;
			else
				newPair.keySizeType = PROPORTIONAL;
		}

		LameFocusManager.rootRearrange();
		root.panel.repaint();
		LameFocusManager.requestFocus(w);

		return w;
	}

	public abstract void rearrange(Rectangle r);

	protected boolean isFocusStealable() {
		return true;
	}

	protected void focusHighlight() {
		Border b = panel.getBorder();
		if (b != null)
			panel.setBorder(BorderFactory.createBevelBorder(
					BevelBorder.LOWERED, HIGHLIGHT_COLOR, HIGHLIGHT_SHADOW));
	}

	protected void unfocusHighlight() {
		Border b = panel.getBorder();
		if (b != null)
			panel.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
	}

	// the rectangle acts as a constraint on the size of the window
	protected int getSplit(int size, int axis) {
		return 0;
	}

	public boolean requestMouseInput(MouseInputConsumer c) {
		return false;
	}

	public boolean requestCharacterInput(CharInputConsumer c) {
		return false;
	}

	public boolean requestLineInput(LineInputConsumer c, String init, int max) {
		return false;
	}

	public void cancelMouseInput() {

	}

	public void cancelCharacterInput() {

	}

	public String cancelLineInput() {
		return null;
	}

	public void mouseClicked(MouseEvent e) {
		if (LameFocusManager.FOCUSED_WINDOW != this) {
			LameFocusManager.grabFocus(this);
		}
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	protected void handleKey(KeyEvent e) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.p2c2e.zing.IWindow#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return hashCode() - o.hashCode();
	}

	public static void useHints(boolean useHints) {
		if (useHints != Style.USE_HINTS) {
			Style.USE_HINTS = useHints;

			if (Window.root != null) {
				Window.getRoot().restyle(Style.USE_HINTS);
				LameFocusManager.rootRearrange();
				Window.root.panel.repaint();
			}

			try {
				Preferences stylep = Preferences.userRoot().node(
						"/org/p2c2e/zing/style");
				stylep.putBoolean("use-hints", Style.USE_HINTS);
				stylep.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}
	}

	public static void applyStyle() {
		if (Window.root != null) {
			try {
				Preferences gridp = Preferences.userRoot().node(
						"/org/p2c2e/zing/style/grid");
				Preferences bufp = Preferences.userRoot().node(
						"/org/p2c2e/zing/style/buffer");

				for (int i = 0; i < IGlk.STYLE_NUMSTYLES; i++) {
					Style s = Style.getStyle(IGlk.STYLES[i],
							IGlk.WINTYPE_TEXT_BUFFER);
					Style.saveStyle(Glk.getInstance(), bufp, s);

					s = Style.getStyle(IGlk.STYLES[i], IGlk.WINTYPE_TEXT_GRID);

					Style.saveStyle(Glk.getInstance(), gridp, s);
				}
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
			}

			Window.root.restyle(Style.USE_HINTS);
			LameFocusManager.rootRearrange();
			Window.root.panel.repaint();
		}
	}

}
