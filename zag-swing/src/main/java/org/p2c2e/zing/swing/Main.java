package org.p2c2e.zing.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

import org.p2c2e.blorb.BlorbFile;
import org.p2c2e.blorb.NotBlorbException;
import org.p2c2e.zag.GlulxException;
import org.p2c2e.zag.Zag;
import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.ObjectCallback;
import org.p2c2e.zing.Style;
import org.p2c2e.zing.types.GlkEvent;

public class Main {
	private JFrame frame;
	private JFrame prefFrame;
	private PreferencePane.CloseCallback cc;
	private JMenuItem openitem;
	private JMenuItem openurlitem;
	private JMenuItem closeitem;
	private JMenuItem quititem;
	private JMenuItem prefitem;
	private JCheckBoxMenuItem hintitem;
	private ZagActionListener al;
	private JFileChooser chooser;
	private Object o;
	private File f;
	private Zag z;
	private Glk glk;
	private boolean specialConfig = false;
	private Config config;

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Main m = new Main();
				m.run(args);
			}
		});
	}

	public void run(String[] argv) {
		try {
			UIManager.setLookAndFeel(org.p2c2e.zing.swing.Properties
					.getInstance().getPlafName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name",
				"Zag");

		o = new Object();
		frame = new JFrame("Zag");
		if (isMaximized()) {
			frame.setExtendedState(frame.getExtendedState()
					| JFrame.MAXIMIZED_BOTH);
		}
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getRootPane().setGlassPane(new GlassPane());
		frame.setIconImage(new ImageIcon(Main.class
				.getResource("/images/zag_icon.png")).getImage());

		prefFrame = new JFrame("Zag Preferences");
		chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setFileFilter(new GlulxFileFilter());
		cc = new ZagCC();

		String stOS = System.getProperty("os.name").toLowerCase();
		JMenuBar menubar = new JMenuBar();
		JMenu filemenu = new JMenu("File");
		JMenu editmenu = new JMenu("Edit");
		openitem = new JMenuItem("Open file...");
		openurlitem = new JMenuItem("Open URL...");
		closeitem = new JMenuItem("End session");
		quititem = new JMenuItem("Quit");
		hintitem = new JCheckBoxMenuItem("Accept style hints");
		final JCheckBoxMenuItem antialiasItem = new JCheckBoxMenuItem(
				"Anti-aliasing");
		final Preferences stylep = Preferences.userRoot().node(
				"/org/p2c2e/zing/style");
		antialiasItem.setState(stylep.getBoolean("use-antialias", true));
		antialiasItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame,
						"This option will take effect on restart.");
				try {
					stylep.putBoolean("use-antialias", antialiasItem.getState());
					stylep.flush();
				} catch (BackingStoreException ex) {
					ex.printStackTrace();
				}
			}
		});
		prefitem = new JMenuItem("Style preferences...");
		al = new ZagActionListener();
		frame.addWindowListener(al);

		if (stOS.indexOf("mac") != -1) {
			openitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
					Event.META_MASK));
			quititem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
					Event.META_MASK));
		} else {
			openitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
					Event.CTRL_MASK));
		}

		filemenu.add(openitem);
		filemenu.add(openurlitem);
		filemenu.add(closeitem);
		filemenu.add(quititem);
		editmenu.add(hintitem);
		editmenu.add(antialiasItem);
		editmenu.add(prefitem);

		menubar.add(filemenu);
		menubar.add(editmenu);

		openitem.setAction(new FileOpenAction());
		openurlitem.setAction(new FileOpenUrlAction());
		closeitem.setAction(new FileCloseAction());
		quititem.setAction(new QuitAction());
		prefitem.setAction(new EditPreferencesAction());
		hintitem.setAction(new UseHintsAction());

		closeitem.setEnabled(false);

		Rectangle rect = getDefaultFrameRect();
		String filename = (argv.length > 0) ? argv[0] : null;
		String conf = (argv.length > 1) ? argv[1] : null;
		glk = Glk.getInstance();
		StatusPane status = glk.getStatusPane();

		config = getConfig(conf, rect);

		frame.setSize(new Dimension(config.width, config.height));

		if (config.center)
			frame.setLocationRelativeTo(null);
		else
			frame.setLocation(rect.x, rect.y);

		if (!config.decorate) {
			frame.setUndecorated(true);
			JPanel fooPanel = new JPanel(new BorderLayout());
			fooPanel.setPreferredSize(new Dimension(frame.getWidth(), 25));
			fooPanel.add(status.getProgressBar(), BorderLayout.EAST);
			frame.getContentPane().add(fooPanel, BorderLayout.SOUTH);
		} else {
			frame.getRootPane().setBorder(BorderFactory.createEtchedBorder());
			frame.setJMenuBar(menubar);
		}
		
//		frame.getContentPane().setPreferredSize(new Dimension(640, 480));
//		frame.pack();
		
		frame.setVisible(true);
		glk.setFrame(frame, config.decorate, config.borders, config.propFont,
				config.fixedFont, config.pFontSize, config.fFontSize);
		glk.setMorePromptCallback(new StatusMoreCallback((GlassPane) frame
				.getRootPane().getGlassPane(), status, config.decorate));

		hintitem.setState(Style.usingHints());

		frame.setTransferHandler(new TransferHandler() {

			@Override
			public boolean importData(TransferSupport support) {
				try {
					List<File> files = (List<File>) support.getTransferable()
							.getTransferData(DataFlavor.javaFileListFlavor);
					f = files.get(0);
					execute();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

			@Override
			public boolean canImport(TransferSupport support) {
				if (f == null
						&& support
								.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					support.setDropAction(LINK);
					return true;
				}
				return false;
			}
		});

		if (filename != null) {
			if (filename.startsWith("http://")
					|| filename.startsWith("file:///")
					|| filename.startsWith("ftp://")) {
				try {
					f = loadURL(filename);
				} catch (IOException eIO) {
					f = null;
				}
			} else {
				f = new File(filename);
			}

			if (f == null || !f.exists()) {
				JOptionPane.showMessageDialog(frame, "Could not find a valid "
						+ "resource at " + filename + ".",
						"Could not open file", JOptionPane.ERROR_MESSAGE);
				f = null;
			}

			execute();
		}

	}

	private void execute() {
		if (f != null) {
			SwingWorker<String, Object> sw = new SwingWorker<String, Object>() {
				@Override
				protected String doInBackground() throws Exception {
					openitem.setEnabled(false);
					openurlitem.setEnabled(false);
					closeitem.setEnabled(true);

					if (config.decorate)
						glk.getStatusPane().show(StatusPane.BLANK);

					glk.reset();
					open(f, config);
					return null;
				}

				@Override
				protected void done() {
					glk.flush();
					if (specialConfig)
						glk.exit();
					else
						glk.getStatusPane().show(StatusPane.EXIT);

					f = null;
					openitem.setEnabled(true);
					openurlitem.setEnabled(true);
					closeitem.setEnabled(false);
				}
			};

			sw.execute();
		}
	}

	private boolean open(final File file, Config config) {
		int iStart = 0;

		try {
			BlorbFile.Chunk chunk;
			BlorbFile bf = new BlorbFile(file);
			Glk.getInstance().setBlorbFile(bf);

			if (!config.decorate && config.mask >= 0)
				((GlassPane) frame.getRootPane().getGlassPane())
						.setMask(config.mask);

			if (!config.decorate) {
				frame.getContentPane().removeAll();
				frame.getContentPane().invalidate();
			}

			Iterator<BlorbFile.Chunk> it = bf.iterateByType(BlorbFile.GLUL);
			if (it.hasNext()) {
				chunk = it.next();
				iStart = chunk.getDataPosition();
			} else {
				JOptionPane.showMessageDialog(frame,
						"The Blorb file you specified does "
								+ "not contain an executable chunk of "
								+ "type GLUL.  (In other words, this is "
								+ "not a Glulx program, and Zag cannot "
								+ "execute it.)", "Not a Glulx program",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (NotBlorbException eblorb) {
			// NOP
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame,
					"The file you selected could not " + "be opened.",
					"Could not open file", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}

		try {
			z = new Zag(Glk.getInstance(), file, iStart,
					org.p2c2e.zing.swing.Properties.getInstance()
							.getSaveMemory());
			z.start();
		} catch (GlulxException eG) {
			JOptionPane.showMessageDialog(frame,
					"The Glulx virtual machine encountered a "
							+ "fatal error in the program and has exited.",
					"Glulx fatal error", JOptionPane.ERROR_MESSAGE);
			eG.printStackTrace();
			return false;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame,
					"The file you selected could not " + "be opened.",
					"Could not open file", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private class ZagActionListener extends WindowAdapter {
		@Override
		public void windowClosed(WindowEvent e) {
			new QuitAction().actionPerformed(new ActionEvent(e.getWindow(),
					ActionEvent.ACTION_LAST, "file-quit"));
		}
	}

	private class FileOpenAction extends AbstractAction {
		public FileOpenAction() {
			super("Open file...");
			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			File fi;
			int ret = chooser.showOpenDialog(frame);

			if (ret == JFileChooser.APPROVE_OPTION) {
				fi = chooser.getSelectedFile();
				f = fi;
				execute();
			}
		}
	}

	private class FileOpenUrlAction extends AbstractAction {
		public FileOpenUrlAction() {
			super("Open URL...");
			putValue(MNEMONIC_KEY, KeyEvent.VK_U);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String stURL = JOptionPane.showInputDialog(frame, "Load URL:");

			if (stURL != null) {
				try {
					f = loadURL(stURL);
					execute();
				} catch (IOException eIO) {
					f = null;
				}
			}
		}
	}

	private class FileCloseAction extends AbstractAction {
		public FileCloseAction() {
			super("Close file");
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (specialConfig) {
				glk.exit();
			} else if (z != null) {
				z.setRunning(false);
				Glk.getInstance().addEvent(new GlkEvent());
			}
		}
	}

	private class QuitAction extends AbstractAction {
		public QuitAction() {
			super("Quit");
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4,
					ActionEvent.ALT_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Point p = frame.getLocation();
				Dimension d = frame.getSize();
				Preferences prefs = Preferences.userRoot().node(
						"/org/p2c2e/zag");

				if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
					prefs.putInt("frame-loc-x", p.x);
					prefs.putInt("frame-loc-y", p.y);
					prefs.putInt("frame-width", d.width);
					prefs.putInt("frame-height", d.height);
					prefs.putBoolean("frame-maximized", false);
				} else {
					prefs.putBoolean("frame-maximized", true);
				}
				prefs.flush();
			} catch (BackingStoreException eBack) {
				eBack.printStackTrace();
			}

			glk.exit();
		}
	}

	private class EditPreferencesAction extends AbstractAction {
		public EditPreferencesAction() {
			super("Preferences...");
			putValue(MNEMONIC_KEY, KeyEvent.VK_P);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O,
					ActionEvent.CTRL_MASK));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			prefitem.setEnabled(false);
			prefFrame.getContentPane().add(new PreferencePane(prefFrame, cc));
			prefFrame.pack();
			prefFrame.setLocationRelativeTo(frame);
			prefFrame.setVisible(true);
		}
	}

	private class UseHintsAction extends AbstractAction {
		public UseHintsAction() {
			super("Use hints");
			putValue(MNEMONIC_KEY, KeyEvent.VK_U);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Window.useHints(hintitem.getState());
		}
	}

	private File loadURL(String stURL) throws IOException {
		IGlk glk = Glk.getInstance();
		int total = 0;
		URL url = new URL(stURL);
		URLConnection urlcon = url.openConnection();
		int len = urlcon.getContentLength();
		BufferedInputStream in = new BufferedInputStream(
				urlcon.getInputStream());
		File tmp = File.createTempFile("zag", null);
		BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(tmp));
		byte[] buf = new byte[8192];
		int iRead = 0;

		while (iRead != -1) {
			iRead = in.read(buf, 0, buf.length);
			if (iRead > 0) {
				out.write(buf, 0, iRead);
				total += iRead;
				if (len > 0)
					glk.progress("Downloading...", 0, len, total);
			}
		}
		glk.progress(null, 0, 0, 0);
		out.flush();
		out.close();
		in.close();

		return tmp;
	}

	private Rectangle getDefaultFrameRect() {
		Rectangle r = new Rectangle(70, 70, 600, 700);
		Preferences prefs = Preferences.userRoot().node("/org/p2c2e/zag");
		r.x = prefs.getInt("frame-loc-x", r.x);
		r.y = prefs.getInt("frame-loc-y", r.y);
		r.width = prefs.getInt("frame-width", r.width);
		r.height = prefs.getInt("frame-height", r.height);

		return r;
	}

	private boolean isMaximized() {
		Preferences prefs = Preferences.userRoot().node("/org/p2c2e/zag");
		return prefs.getBoolean("frame-maximized", false);
	}

	private boolean getProp(Properties props, String name, boolean def) {
		String s = props.getProperty(name);
		if (s != null)
			return s.equalsIgnoreCase("yes");
		else
			return def;
	}

	private String getProp(Properties props, String name, String def) {
		String s = props.getProperty(name);
		if (s != null)
			return s;
		else
			return def;
	}

	private int getProp(Properties props, String name, int def) {
		String s = props.getProperty(name);
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
			}
		}
		return def;
	}

	private Config getConfig(String conf, Rectangle r) {
		Config config = null;

		if (conf != null) {
			try {
				String p;
				Properties props = new Properties();
				File confFile = (conf.startsWith("http://")
						|| conf.startsWith("file:///") || conf
						.startsWith("ftp://")) ? loadURL(conf) : new File(conf);
				FileInputStream confIn = new FileInputStream(confFile);

				props.load(confIn);
				confIn.close();

				config = new Config();
				config.center = true;
				config.width = getProp(props, "WindowWidth", r.width);
				config.height = getProp(props, "WindowHeight", r.height);
				config.decorate = getProp(props, "WindowFrame", true);
				config.borders = getProp(props, "WindowBorders", false);
				config.propFont = getProp(props, "FontName", null);
				config.fixedFont = getProp(props, "FixedFontName", null);
				config.pFontSize = getProp(props, "FontSize", 14);
				config.fFontSize = getProp(props, "FixedFontSize", 14);
				config.mask = getProp(props, "WindowMask", -1);

				specialConfig = true;
			} catch (IOException confEx) {
				System.err.println("Could not load configuration file: ");
				confEx.printStackTrace();
			}
		}

		if (config == null) {
			config = new Config();
			config.center = false;
			config.width = r.width;
			config.height = r.height;
			config.decorate = true;
			config.borders = false;
			config.propFont = null;
			config.fixedFont = null;
			config.pFontSize = 14;
			config.fFontSize = 14;
			config.mask = -1;
		}

		return config;
	}

	private class ZagCC implements PreferencePane.CloseCallback {
		@Override
		public void close() {
			prefFrame.setVisible(false);
			prefitem.setEnabled(true);
		}
	}

	static class Config {
		int width;
		int height;
		boolean decorate;
		boolean borders;
		String propFont;
		String fixedFont;
		int pFontSize;
		int fFontSize;
		boolean center;
		int mask;
	}

	private class GlassPane extends JComponent {
		private final Font GFONT = new Font("SansSerif", Font.PLAIN, 24);

		boolean more;
		int ib;
		int iw;
		Color bl;
		Color wh;
		javax.swing.Timer t;
		String stText;

		Area mask;

		void setMask(int i) throws Exception {
			Image img = Glk.getInstance().getImage(i, -1, -1);
			if (img != null) {
				Dimension d = getSize();
				if (img.getWidth(null) == d.getWidth()
						&& img.getHeight(null) == d.getHeight()) {
					mask = new Area(getBounds());
					mask.subtract(new MaskArea(img));
					setVisible(true);
					revalidate();
					repaint();
				}
			}
		}

		synchronized void display(String s) {
			stText = s;
			more = (s != null);
			if (more) {
				if (t != null) {
					t.stop();
					t = null;
				}

				ActionListener fader;
				t = new javax.swing.Timer(50, null);

				if (mask == null)
					setVisible(true);
				ib = 50;
				iw = 255;

				fader = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						bl = new Color(0, 0, 0, ib);
						wh = new Color(0, 0, 0, iw);

						iw -= 10;
						ib -= 5;

						if (iw < 0 || ib < 0) {
							iw = 0;
							ib = 0;
							t.stop();
							more = false;
						}

						repaint();
					}
				};

				t.addActionListener(fader);
				t.setInitialDelay(5000);
				t.start();
				repaint();
			} else if (t != null) {
				if (mask == null)
					setVisible(false);
				t.stop();
				t = null;
				repaint();
			}
		}

		@Override
		public void paint(Graphics g) {
			if (more) {
				int x, y;
				Graphics2D g2d = (Graphics2D) g;
				// FontRenderContext frc = g2d.getFontRenderContext();
				FontRenderContext frc = new FontRenderContext(null,
						Style.use_antialiasing, true);
				Rectangle2D r = GFONT.getStringBounds(stText, frc);
				LineMetrics m = GFONT.getLineMetrics(stText, frc);
				int h = getHeight();

				g.setFont(GFONT);
				g.setColor(new Color(0, 0, 0, ib));
				g.fillRoundRect(10, h - 60, ((int) r.getWidth()) + 40, 50, 20,
						20);
				g.setColor(new Color(255, 255, 255, iw));
				x = 30;
				y = (h - 35) + ((int) (r.getHeight() / 2d))
						- (int) m.getDescent();
				g.drawString(stText, x, y);

			}

			if (mask != null) {
				g.setColor(Color.black);
				((Graphics2D) g).fill(mask);
			}
		}
	}

	private class StatusMoreCallback implements ObjectCallback {
		boolean decorated;
		StatusPane p;
		GlassPane g;

		StatusMoreCallback(GlassPane g, StatusPane p, boolean decorated) {
			this.g = g;
			this.p = p;
			this.decorated = decorated;
		}

		@Override
		public void callback(Object o) {
			if (decorated)
				p.show((Component) o);
			else
				g.display((o == StatusPane.BLANK) ? null : ((JLabel) o)
						.getText());
		}
	}

	private class GlulxFileFilter extends javax.swing.filechooser.FileFilter {
		@Override
		public boolean accept(File f) {
			String s = f.getName().toLowerCase();
			return f.isDirectory() || s.endsWith(".ulx") || s.endsWith(".blb")
					|| s.endsWith(".gblorb");
		}

		@Override
		public String getDescription() {
			return "Glulx (*.ulx,*.blb,*.gblorb)";
		}
	}
}
