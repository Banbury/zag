package org.p2c2e.zag;

import org.p2c2e.zing.*;
import org.p2c2e.blorb.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.font.*;
import java.util.*;
import java.util.prefs.*;
import java.io.*;
import java.net.*;

public class Main
{
  static JFrame frame;
  static JFrame prefFrame;
  static PreferencePane.CloseCallback cc;
  static JMenuItem openitem;
  static JMenuItem openurlitem;
  static JMenuItem closeitem;
  static JMenuItem quititem;
  static JMenuItem prefitem;
  static JCheckBoxMenuItem hintitem;
  static ZagActionListener al;
  static JFileChooser chooser;
  static Object o;
  static File f;
  static Zag z;
  static boolean specialConfig = false;

  public static void main(String[] argv)
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", 
                       "Zag");

    o = new Object();
    frame = new JFrame("Zag");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.getRootPane().setGlassPane(new GlassPane());

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
    prefitem = new JMenuItem("Style preferences...");
    al = new ZagActionListener();
    frame.addWindowListener(al);

    if (stOS.indexOf("mac") != -1)
    {
      openitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.META_MASK));
      quititem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.META_MASK));
    }
    else
    {
      openitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
    }

    filemenu.add(openitem);
    filemenu.add(openurlitem);
    filemenu.add(closeitem);
    filemenu.add(quititem);
    editmenu.add(hintitem);
    editmenu.add(prefitem);

    menubar.add(filemenu);
    menubar.add(editmenu);

    openitem.setActionCommand("file-open");
    openurlitem.setActionCommand("url-open");
    closeitem.setActionCommand("file-close");
    quititem.setActionCommand("file-quit");
    prefitem.setActionCommand("edit-preferences");
    hintitem.setActionCommand("edit-accept-hints");

    openitem.addActionListener(al);
    openurlitem.addActionListener(al);
    closeitem.addActionListener(al);
    quititem.addActionListener(al);
    prefitem.addActionListener(al);
    hintitem.addActionListener(al);

    closeitem.setEnabled(false);

    Config config;
    Rectangle rect = getDefaultFrameRect();
    String filename = (argv.length > 0) ? argv[0] : null;
    String conf = (argv.length > 1) ? argv[1] : null;
    StatusPane status = Glk.getStatusPane();

    config = getConfig(conf, rect);

    frame.setSize(new Dimension(config.width, config.height));

    if (config.center)
      frame.setLocationRelativeTo(null);
    else
      frame.setLocation(rect.x, rect.y);

    if (!config.decorate)
    {
      frame.setUndecorated(true);
      JPanel fooPanel = new JPanel(new BorderLayout());
      fooPanel.setPreferredSize(new Dimension(frame.getWidth(), 25));
      fooPanel.add(status.getProgressBar(), BorderLayout.EAST);
      frame.getContentPane().add(fooPanel, BorderLayout.SOUTH);
    }
    else
    {
      frame.getRootPane().setBorder(BorderFactory.createEtchedBorder());
      frame.setJMenuBar(menubar);
    }
    frame.show();
    Glk.setFrame(frame, config.decorate, config.borders, 
                 config.propFont, config.fixedFont,
                 config.pFontSize, config.fFontSize);
    Glk.setMorePromptCallback(new StatusMoreCallback((GlassPane) frame.getRootPane().getGlassPane(), status, config.decorate));

    hintitem.setState(Style.usingHints());

    if (filename != null)
    {
      if (filename.startsWith("http://") || filename.startsWith("file:///") ||
          filename.startsWith("ftp://"))
      {
        try {
          f = loadURL(filename);
        } catch(IOException eIO) {
          f = null;
        }
      }
      else
      {
        f = new File(filename);
      }

      if (f == null || !f.exists())
      {
        JOptionPane.showMessageDialog(frame, "Could not find a valid " +
                                      "resource at " + filename + ".", 
                                      "Could not open file", 
                                      JOptionPane.ERROR_MESSAGE);
        f = null;
      }

    }


    while (true)
    {
      if (f != null)
      {
        openitem.setEnabled(false);
        openurlitem.setEnabled(false);
        closeitem.setEnabled(true);

        if (config.decorate)
          status.show(StatusPane.BLANK);

        Glk.reset();
        open(f, config);
        Glk.flush();
        if (specialConfig)
          Glk.exit();
        else
          status.show(StatusPane.EXIT);

        f = null;
        openitem.setEnabled(true);
        openurlitem.setEnabled(true);
        closeitem.setEnabled(false);
      }
      else
      {
        synchronized(o)
        {
          try {
            o.wait();
          } catch(InterruptedException ex) { }
        }
      }
    }
  }

  static boolean open(File fi, Config config)
  {
    int iStart = 0;

    try
    {
      BlorbFile.Chunk chunk;
      BlorbFile bf = new BlorbFile(fi);
      Glk.setBlorbFile(bf);
      
      if (!config.decorate && config.mask >= 0)
        ((GlassPane) frame.getRootPane().getGlassPane()).setMask(config.mask);

      if (!config.decorate)
      {
        frame.getContentPane().removeAll();
        frame.getContentPane().invalidate();
      }

      Iterator it = bf.iterateByType(BlorbFile.GLUL);
      if (it.hasNext())
      {
        chunk = (BlorbFile.Chunk) it.next();
        iStart = chunk.getDataPosition();
      }
      else
      {
        JOptionPane.showMessageDialog(frame, 
                                      "The Blorb file you specified does " +
                                      "not contain an executable chunk of " +
                                      "type GLUL.  (In other words, this is " +
                                      "not a Glulx program, and Zag cannot " +
                                      "execute it.)", "Not a Glulx program", 
                                      JOptionPane.ERROR_MESSAGE);
        return false;

      }
    }
    catch(NotBlorbException eblorb)
    {
      // NOP
    }
    catch(Exception e)
    {
      JOptionPane.showMessageDialog(frame, 
                                    "The file you selected could not " +
                                    "be opened.", "Could not open file",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return false;
    }

    try
    {
      z = new Zag(fi, iStart);
      z.start();
      return true;
    }
    catch(GlulxException eG)
    {
      JOptionPane.showMessageDialog(frame, 
                                    "The Glulx virtual machine encountered a " +
                                    "fatal error in the program and has exited.",
                                    "Glulx fatal error",
                                    JOptionPane.ERROR_MESSAGE);      
      eG.printStackTrace();
      return false;
    }
    catch(Exception e)
    {
      JOptionPane.showMessageDialog(frame, 
                                    "The file you selected could not " +
                                    "be opened.", "Could not open file",
                                    JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return false;
    }
  }


  static class ZagActionListener extends WindowAdapter implements ActionListener
  {
    public void windowClosed(WindowEvent e)
    {
      actionPerformed(new ActionEvent(e.getWindow(), ActionEvent.ACTION_LAST, "file-quit"));
    }

    public void actionPerformed(ActionEvent e)
    {
      String st = e.getActionCommand();

      if ("file-open".equals(st))
      {
        synchronized(o)
        {
          File fi;
          int ret = chooser.showOpenDialog(frame);

          if (ret == JFileChooser.APPROVE_OPTION)
          {
            fi = chooser.getSelectedFile();

            openitem.setEnabled(false);
            openurlitem.setEnabled(false);
            closeitem.setEnabled(true);
            
            f = fi;
            o.notify();
          }
        }
      }
      else if ("url-open".equals(st))
      {
        String stURL = JOptionPane.showInputDialog(frame, "Load URL:");

        if (stURL != null)
        {
          synchronized (o)
          {
            try {
              f = loadURL(stURL);
              o.notify();
            } catch (IOException eIO) {
              f = null;
            }
          }
        }
      }
      else if ("file-close".equals(st))
      {
        if (specialConfig)
        {
          Glk.exit();
        }
        else if (z != null)
        {
          z.running = false;
          Glk.addEvent(new Glk.GlkEvent());
        }
      }
      else if ("file-quit".equals(st))
      {
        try
        {
          Point p = frame.getLocation();
          Dimension d = frame.getSize();
          Preferences prefs = Preferences.userRoot().node("/org/p2c2e/zag");

          prefs.putInt("frame-loc-x", p.x);
          prefs.putInt("frame-loc-y", p.y);
          prefs.putInt("frame-width", d.width);
          prefs.putInt("frame-height", d.height);
          prefs.flush();
        }
        catch(BackingStoreException eBack)
        {
          eBack.printStackTrace();
        }

        Glk.exit();
      }
      else if ("edit-preferences".equals(st))
      {
        prefitem.setEnabled(false);
        prefFrame.getContentPane().add(new PreferencePane(prefFrame, cc));
        prefFrame.pack();
        prefFrame.setLocationRelativeTo(frame);
        prefFrame.setVisible(true);
      }
      else if ("edit-accept-hints".equals(st))
      {
        Style.useHints(hintitem.getState());
      }
    }
  }

  static File loadURL(String stURL) throws IOException
  {
    int total = 0;
    URL url = new URL(stURL);
    URLConnection urlcon = url.openConnection();
    int len = urlcon.getContentLength();
    BufferedInputStream in = new BufferedInputStream(urlcon.getInputStream());
    File tmp = File.createTempFile("zag", null);
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
    byte[] buf = new byte[8192];
    int iRead = 0;

    while (iRead != -1)
    {
      iRead = in.read(buf, 0, buf.length);
      if (iRead > 0)
      {
        out.write(buf, 0, iRead);
        total += iRead;
        if (len > 0)
          Glk.progress("Downloading...", 0, len, total);
      }
    }
    Glk.progress(null, 0, 0, 0);
    out.flush();
    out.close();
    in.close();

    return tmp;
  }

  static Rectangle getDefaultFrameRect()
  {
    Rectangle r = new Rectangle(70, 70, 600, 700);
    Preferences prefs = Preferences.userRoot().node("/org/p2c2e/zag");
    r.x = prefs.getInt("frame-loc-x", r.x);
    r.y = prefs.getInt("frame-loc-y", r.y);
    r.width = prefs.getInt("frame-width", r.width);
    r.height = prefs.getInt("frame-height", r.height);

    return r;
  }

  static boolean getProp(Properties props, String name, boolean def)
  {
    String s = props.getProperty(name);
    if (s != null)
      return s.equalsIgnoreCase("yes");
    else
      return def;
  }
  static String getProp(Properties props, String name, String def)
  {
    String s = props.getProperty(name);
    if (s != null)
      return s;
    else
      return def;
  }

  static int getProp(Properties props, String name, int def)
  {
    String s = props.getProperty(name);
    if (s != null)
    {
      try {
        return Integer.parseInt(s);
      } catch (NumberFormatException e) { }
    }
    return def;
  }
  
  static Config getConfig(String conf, Rectangle r)
  {
    Config config = null;

    if (conf != null)
    {
      try {
        String p;
        Properties props = new Properties();
        File confFile = 
          (conf.startsWith("http://") || conf.startsWith("file:///") ||
           conf.startsWith("ftp://"))
          ? loadURL(conf)
          : new File(conf);
        FileInputStream confIn = new FileInputStream(confFile);
        
        props.load(confIn);
        confIn.close();
        
        config = new Config();
        config.center = true;
        config.width = getProp(props, "WindowWidth", r.width);
        config.height = getProp(props, "WindowHeight", r.height);
        config.decorate = getProp(props, "WindowFrame", true);
        config.borders = getProp(props, "WindowBorders", true);
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

    if (config == null)
    {
      config = new Config();
      config.center = false;
      config.width = r.width;
      config.height = r.height;
      config.decorate = true;
      config.borders = true;
      config.propFont = null;
      config.fixedFont = null;
      config.pFontSize = 14;
      config.fFontSize = 14;
      config.mask = -1;
    }

    return config;
  }

  static class ZagCC implements PreferencePane.CloseCallback
  {
    public void close()
    {
      prefFrame.setVisible(false);
      prefitem.setEnabled(true);
    }
  }

  static class Config
  {
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

  static class GlassPane extends JComponent
  {
    static final Font GFONT = new Font("SansSerif", Font.PLAIN, 24);

    boolean more;
    int ib;
    int iw;
    Color bl;
    Color wh;
    javax.swing.Timer t;
    String stText;

    Area mask;

    void setMask(int i) throws Exception
    {
      Image img = Glk.getImage(i, -1, -1);
      if (img != null)
      {
        Dimension d = getSize();
        if (img.getWidth(null) == d.getWidth() &&
            img.getHeight(null) == d.getHeight())
        {
          mask = new Area(getBounds());
          mask.subtract(new MaskArea(img));
          setVisible(true);
          revalidate();
          repaint();
        }
      }
    }

    synchronized void display(String s)
    {
      stText = s;
      more = (s != null);
      if (more)
      {
        if (t != null)
        {
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
            public void actionPerformed(ActionEvent e) {
              bl = new Color(0, 0, 0, ib);
              wh = new Color(0, 0, 0, iw);
              
              iw -= 10;
              ib -= 5;

              if (iw < 0 || ib < 0)
              {
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
      }
      else if (t != null)
      {
        if (mask == null)
          setVisible(false);
        t.stop();
        t = null;
        repaint();
      }
    }

    public void paint(Graphics g)
    {
      if (more)
      {
        int x, y;
        Graphics2D g2d = (Graphics2D) g;
        FontRenderContext frc = g2d.getFontRenderContext();
        Rectangle2D r = GFONT.getStringBounds(stText, frc);
        LineMetrics m = GFONT.getLineMetrics(stText, frc);
        int h = getHeight();

        g.setFont(GFONT);
        g.setColor(new Color(0, 0, 0, ib));
        g.fillRoundRect(10, h - 60, ((int) r.getWidth()) + 40, 50, 20, 20);
        g.setColor(new Color(255, 255, 255, iw));
        x = 30;
        y = (h - 35) + ((int) (r.getHeight() / 2d)) - (int) m.getDescent();
        g.drawString(stText, x, y);

      }

      if (mask != null)
      {
        g.setColor(Color.black);
        ((Graphics2D) g).fill(mask);
      }
    }
  }

  static class StatusMoreCallback implements ObjectCallback
  {
    boolean decorated;
    StatusPane p;
    GlassPane g;

    StatusMoreCallback(GlassPane g, StatusPane p, boolean decorated)
    {
      this.g = g;
      this.p = p;
      this.decorated = decorated;
    }

    public void callback(Object o)
    {
      if (decorated)
        p.show((Component) o);
      else
        g.display((o == StatusPane.BLANK) ? null : ((JLabel) o).getText());
    }
  }

  static class GlulxFileFilter extends javax.swing.filechooser.FileFilter
  {
    public boolean accept(File f)
    {
      String s = f.getName().toLowerCase();
      return f.isDirectory() || s.endsWith(".ulx") || s.endsWith(".blb");
    }

    public String getDescription()
    {
      return "Glulx (.ulx) & Blorb (.blb)";
    }
  }
}
