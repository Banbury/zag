package org.p2c2e.blorb;

import java.io.*;

public class BlorbTest
{
  public static void main(String[] argv) throws Exception
  {
    BlorbFile f = new BlorbFile(new File("/home/jon/sensory.blb"));
    f.dumpBlorb();
  }
}
