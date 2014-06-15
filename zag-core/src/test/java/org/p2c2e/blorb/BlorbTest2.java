package org.p2c2e.blorb;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class BlorbTest2 extends JPanel
{
  public static void main(String[] argv) throws Exception
  {
    BlorbFile f = new BlorbFile(new File("/Users/jaz/sensory.blb"));
    f.dumpBlorb();
    
    Toolkit t = Toolkit.getDefaultToolkit();
    BlorbFile.Chunk c = f.getByUsage(BlorbFile.PICT, 11);
    InputStream in = c.getData();
    byte[] ar = new byte[2048];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int i = in.read(ar);
    while (i != -1)
    {
	out.write(ar, 0, i);
	i = in.read(ar);
    }
    byte[] imgarr = out.toByteArray();
    Image img = t.createImage(imgarr);

    JFrame jf = new JFrame();
    BlorbTest2 p = new BlorbTest2(img);

    jf.getContentPane().add(p);
    jf.pack();
    jf.show();

    System.out.println("here");
    BlorbFile.Chunk snd = f.getByUsage(BlorbFile.SND, 10);
    InputStream ain = new BufferedInputStream(snd.getRawData());
    p.playAiff(ain);
    System.out.println("there");
  }

    Image _img;

    BlorbTest2(Image img)
    {
	super();
	_img = img;
	System.out.println("width: " + _img.getWidth(this));
	System.out.println("height: " + _img.getHeight(this));
	setPreferredSize(new Dimension(_img.getWidth(this), _img.getHeight(this)));
    }

    protected void paintChildren(Graphics g)
    {
	g.drawImage(_img, 0, 0, _img.getWidth(this), _img.getHeight(this), this);
    }

    void playAiff(InputStream in) throws Exception
    {
	AudioInputStream ain = AudioSystem.getAudioInputStream(in);
	AudioFormat format = ain.getFormat();
	DataLine.Info info = new DataLine.Info(Clip.class, format, ((int) ain.getFrameLength() * format.getFrameSize()));
	Clip line = (Clip) AudioSystem.getLine(info);
	line.open(ain);
	line.loop(1);
	line.start();
	try { Thread.sleep(99); } catch (Exception e) {}
	while (line.isActive())
	    try { Thread.sleep(99); } catch (Exception e) {}
	line.drain();
	line.stop();
	line.close();
    }

}

