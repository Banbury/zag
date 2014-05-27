package org.p2c2e.zing;

import java.lang.reflect.*;
import java.util.*;

public abstract class Dispatch
{
  static Method[] METHODS = new Method[322];

  public static Method getMethod(int selector)
  {
    return METHODS[selector];
  }

  static
  {
    String[] NAMES = new String[322];

    NAMES[0x0001] = "exit";
    NAMES[0x0002] = "setInterruptHandler";
    NAMES[0x0003] = "tick";
    NAMES[0x0004] = "gestalt";
    NAMES[0x0005] = "gestaltExt";
    NAMES[0x0020] = "windowIterate";
    NAMES[0x0021] = "windowGetRock";
    NAMES[0x0022] = "windowGetRoot";
    NAMES[0x0023] = "windowOpen";
    NAMES[0x0024] = "windowClose";
    NAMES[0x0025] = "windowGetSize";
    NAMES[0x0026] = "windowSetArrangement";
    NAMES[0x0027] = "windowGetArrangement";
    NAMES[0x0028] = "windowGetType";
    NAMES[0x0029] = "windowGetParent";
    NAMES[0x002A] = "windowClear";
    NAMES[0x002B] = "windowMoveCursor";
    NAMES[0x002C] = "windowGetStream";
    NAMES[0x002D] = "windowSetEchoStream";
    NAMES[0x002E] = "windowGetEchoStream";
    NAMES[0x002F] = "setWindow";
    NAMES[0x0030] = "windowGetSibling";
    NAMES[0x0040] = "streamIterate";
    NAMES[0x0041] = "streamGetRock";
    NAMES[0x0042] = "streamOpenFile";
    NAMES[0x0043] = "streamOpenMemory";
    NAMES[0x0044] = "streamClose";
    NAMES[0x0045] = "streamSetPosition";
    NAMES[0x0046] = "streamGetPosition";
    NAMES[0x0047] = "streamSetCurrent";
    NAMES[0x0048] = "streamGetCurrent";
    NAMES[0x0060] = "filerefCreateTemp";
    NAMES[0x0061] = "filerefCreateByName";
    NAMES[0x0062] = "filerefCreateByPrompt";
    NAMES[0x0063] = "filerefDestroy";
    NAMES[0x0064] = "filerefIterate";
    NAMES[0x0065] = "filerefGetRock";
    NAMES[0x0066] = "filerefDeleteFile";
    NAMES[0x0067] = "filerefDoesFileExist";
    NAMES[0x0068] = "filerefCreateFromFileref";
    NAMES[0x0080] = "putChar";
    NAMES[0x0081] = "putCharStream";
    NAMES[0x0082] = "putString";
    NAMES[0x0083] = "putStringStream";
    NAMES[0x0084] = "putBuffer";
    NAMES[0x0085] = "putBufferStream";
    NAMES[0x0086] = "setStyle";
    NAMES[0x0087] = "setStyleStream";
    NAMES[0x0090] = "getCharStream";
    NAMES[0x0091] = "getLineStream";
    NAMES[0x0092] = "getBufferStream";
    NAMES[0x00A0] = "charToLower";
    NAMES[0x00A1] = "charToUpper";
    NAMES[0x00B0] = "stylehintSet";
    NAMES[0x00B1] = "stylehintClear";
    NAMES[0x00B2] = "styleDistinguish";
    NAMES[0x00B3] = "styleMeasure";
    NAMES[0x00C0] = "select";
    NAMES[0x00C1] = "selectPoll";
    NAMES[0x00D0] = "requestLineEvent";
    NAMES[0x00D1] = "cancelLineEvent";
    NAMES[0x00D2] = "requestCharEvent";
    NAMES[0x00D3] = "cancelCharEvent";
    NAMES[0x00D4] = "requestMouseEvent";
    NAMES[0x00D5] = "cancelMouseEvent";
    NAMES[0x00D6] = "requestTimerEvents";
    NAMES[0x00E0] = "imageGetInfo";
    NAMES[0x00E1] = "imageDraw";
    NAMES[0x00E2] = "imageDrawScaled";
    NAMES[0x00E8] = "windowFlowBreak";
    NAMES[0x00E9] = "windowEraseRect";
    NAMES[0x00EA] = "windowFillRect";
    NAMES[0x00EB] = "windowSetBackgroundColor";
    NAMES[0x00F0] = "schannelIterate";
    NAMES[0x00F1] = "schannelGetRock";
    NAMES[0x00F2] = "schannelCreate";
    NAMES[0x00F3] = "schannelDestroy";
    NAMES[0x00F8] = "schannelPlay";
    NAMES[0x00F9] = "schannelPlayExt";
    NAMES[0x00FA] = "schannelStop";
    NAMES[0x00FB] = "schannelSetVolume";
    NAMES[0x00FC] = "soundLoadHint";
    NAMES[0x0100] = "setHyperlink";
    NAMES[0x0101] = "setHyperlinkStream";
    NAMES[0x0102] = "requestHyperlinkEvent";
    NAMES[0x0103] = "cancelHyperlinkEvent";

    NAMES[0x0120] = "bufferToLowerCaseUni";
    NAMES[0x0121] = "bufferToUpperCaseUni";
    NAMES[0x0122] = "bufferToTitleCaseUni";
    NAMES[0x0128] = "putCharUni";
    NAMES[0x0129] = "putStringUni";
    NAMES[0x012A] = "putBufferUni";
    NAMES[0x012B] = "putCharStreamUni";
    NAMES[0x012C] = "putStringStreamUni";
    NAMES[0x012D] = "putBufferStreamUni";
    NAMES[0x0130] = "getCharStreamUni";
    NAMES[0x0131] = "getBufferStreamUni";
    NAMES[0x0132] = "getLineStreamUni";
    NAMES[0x0138] = "streamOpenFileUni";
    NAMES[0x0139] = "streamOpenMemoryUni";
    NAMES[0x0140] = "requestCharEventUni";
    NAMES[0x0141] = "requestLineEventUni";




    Method[] members = Glk.class.getMethods();
    Comparator methodComp = new MethodNameComparator();
    int len = NAMES.length;
    int index;

    Arrays.sort(members, methodComp);

    for (int i = 0; i < len; i++)
    {
      if (NAMES[i] != null)
      {
        index = Arrays.binarySearch(members, NAMES[i], methodComp);

        if (index >= 0)
          METHODS[i] = members[index];
      }
    }
  }

  static class MethodNameComparator implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      String s1, s2;

      if (o1 instanceof Method)
        s1 = ((Method) o1).getName();
      else
        s1 = (String) o1;

      if (o2 instanceof Method)
        s2 = ((Method) o2).getName();
      else
        s2 = (String) o2;

      return s1.compareTo(s2);
    }

    public boolean equals(Object o)
    {
      return (this == o);
    }
  }

}
