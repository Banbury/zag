package org.p2c2e.zing;

import java.awt.*;
import javax.swing.*;
import java.io.*;

public class Fileref implements Comparable
{
  static final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));

  File f;
  int u;

  Fileref(File file, int usage)
  {
    f = file;
    u = usage;
  }

  public int compareTo(Object o)
  {
    return hashCode() - o.hashCode();
  }

  public static Fileref createTemp(int usage) throws IOException
  {
    File tf = File.createTempFile("zing", null);
    tf.deleteOnExit();
    return new Fileref(tf, usage);
  }

  public static Fileref createByPrompt(int usage, int mode)
  {
    int ret;
    File f;
    
    if ((mode & Glk.FILEMODE_WRITE) != 0)
      ret = fc.showSaveDialog((Component) Window.FRAME);
    else 
      ret = fc.showOpenDialog((Component) Window.FRAME);

    if (ret == JFileChooser.APPROVE_OPTION)
    {
      f = fc.getSelectedFile();
      if (f != null)
        return new Fileref(fc.getSelectedFile(), usage);
      else
        return null;
    }
    return null;
  }

  public static Fileref createByName(int usage, String name)
  {
    return new Fileref(new File(name), usage);
  }

  public static Fileref createFromFileref(int usage, Fileref ref)
  {
    return new Fileref(ref.f, usage);
  }

  public static void deleteFile(Fileref ref)
  {
    ref.f.delete();
  }

  public void destroy()
  {
    f = null;
  }

  public boolean fileExists()
  {
    return f.exists();
  }
}
