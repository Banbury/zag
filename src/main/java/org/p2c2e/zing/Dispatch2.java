package org.p2c2e.zing;

import java.lang.reflect.Method;
import java.util.HashMap;

public final class Dispatch2 {
	private static HashMap<Integer, Method> METHODS = new HashMap<Integer, Method>(
			512);

	public static Method getMethod(int selector) {
		return METHODS.get(selector);
	}

	static {
		try {
			createMethodList();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void createMethodList() throws NoSuchMethodException {
		METHODS.put(3, AbstractGlk.class.getMethod("tick"));
		METHODS.put(2, AbstractGlk.class.getMethod("setInterruptHandler",
				java.lang.Object.class));
		METHODS.put(1, AbstractGlk.class.getMethod("exit"));
		METHODS.put(32, AbstractGlk.class
				.getMethod("windowIterate", org.p2c2e.zing.IWindow.class,
						org.p2c2e.zing.types.OutInt.class));
		METHODS.put(64, AbstractGlk.class.getMethod("streamIterate",
				org.p2c2e.zing.Stream.class, org.p2c2e.zing.types.OutInt.class));
		METHODS.put(100, AbstractGlk.class
				.getMethod("filerefIterate", org.p2c2e.zing.Fileref.class,
						org.p2c2e.zing.types.OutInt.class));
		METHODS.put(240, AbstractGlk.class.getMethod("schannelIterate",
				org.p2c2e.zing.SoundChannel.class,
				org.p2c2e.zing.types.OutInt.class));
		METHODS.put(160, AbstractGlk.class.getMethod("charToLower", char.class));
		METHODS.put(161, AbstractGlk.class.getMethod("charToUpper", char.class));
		METHODS.put(39, AbstractGlk.class.getMethod("windowGetArrangement",
				org.p2c2e.zing.IWindow.class,
				org.p2c2e.zing.types.OutInt.class,
				org.p2c2e.zing.types.OutInt.class,
				org.p2c2e.zing.types.OutWindow.class));
		METHODS.put(38, AbstractGlk.class.getMethod("windowSetArrangement",
				org.p2c2e.zing.IWindow.class, int.class, int.class,
				org.p2c2e.zing.IWindow.class));
		METHODS.put(37, AbstractGlk.class.getMethod("windowGetSize",
				org.p2c2e.zing.IWindow.class,
				org.p2c2e.zing.types.OutInt.class,
				org.p2c2e.zing.types.OutInt.class));
		METHODS.put(48, AbstractGlk.class.getMethod("windowGetSibling",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(41, AbstractGlk.class.getMethod("windowGetParent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(40, AbstractGlk.class.getMethod("windowGetType",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(33, AbstractGlk.class.getMethod("windowGetRock",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(42, AbstractGlk.class.getMethod("windowClear",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(35, AbstractGlk.class.getMethod("windowOpen",
				org.p2c2e.zing.IWindow.class, int.class, int.class, int.class,
				int.class));
		METHODS.put(36, AbstractGlk.class.getMethod("windowClose",
				org.p2c2e.zing.IWindow.class,
				org.p2c2e.zing.types.StreamResult.class));
		METHODS.put(45, AbstractGlk.class.getMethod("windowSetEchoStream",
				org.p2c2e.zing.IWindow.class, org.p2c2e.zing.Stream.class));
		METHODS.put(46, AbstractGlk.class.getMethod("windowGetEchoStream",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(44, AbstractGlk.class.getMethod("windowGetStream",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(47, AbstractGlk.class.getMethod("setWindow",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(71, AbstractGlk.class.getMethod("streamSetCurrent",
				org.p2c2e.zing.Stream.class));
		METHODS.put(72, AbstractGlk.class.getMethod("streamGetCurrent"));
		METHODS.put(128, AbstractGlk.class.getMethod("putChar", char.class));
		METHODS.put(296, AbstractGlk.class.getMethod("putCharUni", int.class));
		METHODS.put(130, AbstractGlk.class.getMethod("putString",
				java.lang.String.class));
		METHODS.put(297, AbstractGlk.class.getMethod("putStringUni",
				java.lang.String.class));
		METHODS.put(132, AbstractGlk.class.getMethod("putBuffer",
				org.p2c2e.zing.types.InByteBuffer.class, int.class));
		METHODS.put(298, AbstractGlk.class.getMethod("putBufferUni",
				org.p2c2e.zing.types.InByteBuffer.class, int.class));
		METHODS.put(129, AbstractGlk.class.getMethod("putCharStream",
				org.p2c2e.zing.Stream.class, int.class));
		METHODS.put(299, AbstractGlk.class.getMethod("putCharStreamUni",
				org.p2c2e.zing.Stream.class, int.class));
		METHODS.put(131, AbstractGlk.class.getMethod("putStringStream",
				org.p2c2e.zing.Stream.class, java.lang.String.class));
		METHODS.put(300, AbstractGlk.class.getMethod("putStringStreamUni",
				org.p2c2e.zing.Stream.class, java.lang.String.class));
		METHODS.put(133, AbstractGlk.class.getMethod("putBufferStream",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.InByteBuffer.class, int.class));
		METHODS.put(301, AbstractGlk.class.getMethod("putBufferStreamUni",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.InByteBuffer.class, int.class));
		METHODS.put(288, AbstractGlk.class.getMethod("bufferToLowerCaseUni",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
		METHODS.put(289, AbstractGlk.class.getMethod("bufferToUpperCaseUni",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
		METHODS.put(290, AbstractGlk.class.getMethod("bufferToTitleCaseUni",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
		METHODS.put(144, AbstractGlk.class.getMethod("getCharStream",
				org.p2c2e.zing.Stream.class));
		METHODS.put(304, AbstractGlk.class.getMethod("getCharStreamUni",
				org.p2c2e.zing.Stream.class));
		METHODS.put(146, AbstractGlk.class.getMethod("getBufferStream",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.OutByteBuffer.class, int.class));
		METHODS.put(305, AbstractGlk.class.getMethod("getBufferStreamUni",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.OutByteBuffer.class, int.class));
		METHODS.put(145, AbstractGlk.class.getMethod("getLineStream",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.OutByteBuffer.class, int.class));
		METHODS.put(306, AbstractGlk.class.getMethod("getLineStreamUni",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.OutByteBuffer.class, int.class));
		METHODS.put(68, AbstractGlk.class.getMethod("streamClose",
				org.p2c2e.zing.Stream.class,
				org.p2c2e.zing.types.StreamResult.class));
		METHODS.put(70, AbstractGlk.class.getMethod("streamGetPosition",
				org.p2c2e.zing.Stream.class));
		METHODS.put(69, AbstractGlk.class.getMethod("streamSetPosition",
				org.p2c2e.zing.Stream.class, int.class, int.class));
		METHODS.put(67, AbstractGlk.class.getMethod("streamOpenMemory",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class, int.class));
		METHODS.put(313, AbstractGlk.class.getMethod("streamOpenMemoryUni",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class, int.class));
		METHODS.put(66, AbstractGlk.class.getMethod("streamOpenFile",
				org.p2c2e.zing.Fileref.class, int.class, int.class));
		METHODS.put(312, AbstractGlk.class.getMethod("streamOpenFileUni",
				org.p2c2e.zing.Fileref.class, int.class, int.class));
		METHODS.put(65, AbstractGlk.class.getMethod("streamGetRock",
				org.p2c2e.zing.Stream.class));
		METHODS.put(135, AbstractGlk.class.getMethod("setStyleStream",
				org.p2c2e.zing.Stream.class, int.class));
		METHODS.put(134, AbstractGlk.class.getMethod("setStyle", int.class));
		METHODS.put(176, AbstractGlk.class.getMethod("stylehintSet", int.class,
				int.class, int.class, int.class));
		METHODS.put(177, AbstractGlk.class.getMethod("stylehintClear",
				int.class, int.class, int.class));
		METHODS.put(178, AbstractGlk.class.getMethod("styleDistinguish",
				org.p2c2e.zing.IWindow.class, int.class, int.class));
		METHODS.put(179, AbstractGlk.class.getMethod("styleMeasure",
				org.p2c2e.zing.IWindow.class, int.class, int.class,
				org.p2c2e.zing.types.OutInt.class));
		METHODS.put(96, AbstractGlk.class.getMethod("filerefCreateTemp",
				int.class, int.class));
		METHODS.put(97, AbstractGlk.class.getMethod("filerefCreateByName",
				int.class, java.lang.String.class, int.class));
		METHODS.put(98, AbstractGlk.class.getMethod("filerefCreateByPrompt",
				int.class, int.class, int.class));
		METHODS.put(104, AbstractGlk.class.getMethod(
				"filerefCreateFromFileref", int.class,
				org.p2c2e.zing.Fileref.class, int.class));
		METHODS.put(99, AbstractGlk.class.getMethod("filerefDestroy",
				org.p2c2e.zing.Fileref.class));
		METHODS.put(102, AbstractGlk.class.getMethod("filerefDeleteFile",
				org.p2c2e.zing.Fileref.class));
		METHODS.put(103, AbstractGlk.class.getMethod("filerefDoesFileExist",
				org.p2c2e.zing.Fileref.class));
		METHODS.put(101, AbstractGlk.class.getMethod("filerefGetRock",
				org.p2c2e.zing.Fileref.class));
		METHODS.put(241, AbstractGlk.class.getMethod("schannelGetRock",
				org.p2c2e.zing.SoundChannel.class));
		METHODS.put(242,
				AbstractGlk.class.getMethod("schannelCreate", int.class));
		METHODS.put(243, AbstractGlk.class.getMethod("schannelDestroy",
				org.p2c2e.zing.SoundChannel.class));
		METHODS.put(249, AbstractGlk.class.getMethod("schannelPlayExt",
				org.p2c2e.zing.SoundChannel.class, int.class, int.class,
				int.class));
		METHODS.put(248, AbstractGlk.class.getMethod("schannelPlay",
				org.p2c2e.zing.SoundChannel.class, int.class));
		METHODS.put(250, AbstractGlk.class.getMethod("schannelStop",
				org.p2c2e.zing.SoundChannel.class));
		METHODS.put(251, AbstractGlk.class.getMethod("schannelSetVolume",
				org.p2c2e.zing.SoundChannel.class, int.class));
		METHODS.put(252, AbstractGlk.class.getMethod("soundLoadHint",
				int.class, int.class));
		METHODS.put(4,
				AbstractGlk.class.getMethod("gestalt", int.class, int.class));
		METHODS.put(5, AbstractGlk.class
				.getMethod("gestaltExt", int.class, int.class,
						org.p2c2e.zing.types.InOutIntBuffer.class, int.class));
		METHODS.put(210, AbstractGlk.class.getMethod("requestCharEvent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(320, AbstractGlk.class.getMethod("requestCharEventUni",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(211, AbstractGlk.class.getMethod("cancelCharEvent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(208, AbstractGlk.class.getMethod("requestLineEvent",
				org.p2c2e.zing.IWindow.class,
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
		METHODS.put(321, AbstractGlk.class.getMethod("requestLineEventUni",
				org.p2c2e.zing.IWindow.class,
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
		METHODS.put(209, AbstractGlk.class.getMethod("cancelLineEvent",
				org.p2c2e.zing.IWindow.class,
				org.p2c2e.zing.types.GlkEvent.class));
		METHODS.put(212, AbstractGlk.class.getMethod("requestMouseEvent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(213, AbstractGlk.class.getMethod("cancelMouseEvent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(214,
				AbstractGlk.class.getMethod("requestTimerEvents", int.class));
		METHODS.put(225, AbstractGlk.class.getMethod("imageDraw",
				org.p2c2e.zing.IWindow.class, int.class, int.class, int.class));
		METHODS.put(226, AbstractGlk.class.getMethod("imageDrawScaled",
				org.p2c2e.zing.IWindow.class, int.class, int.class, int.class,
				int.class, int.class));
		METHODS.put(224, AbstractGlk.class.getMethod("imageGetInfo", int.class,
				org.p2c2e.zing.types.OutInt.class,
				org.p2c2e.zing.types.OutInt.class));
		METHODS.put(235, AbstractGlk.class.getMethod(
				"windowSetBackgroundColor", org.p2c2e.zing.IWindow.class,
				java.awt.Color.class));
		METHODS.put(234, AbstractGlk.class.getMethod("windowFillRect",
				org.p2c2e.zing.IWindow.class, java.awt.Color.class, int.class,
				int.class, int.class, int.class));
		METHODS.put(233, AbstractGlk.class.getMethod("windowEraseRect",
				org.p2c2e.zing.IWindow.class, int.class, int.class, int.class,
				int.class));
		METHODS.put(232, AbstractGlk.class.getMethod("windowFlowBreak",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(43, AbstractGlk.class.getMethod("windowMoveCursor",
				org.p2c2e.zing.IWindow.class, int.class, int.class));
		METHODS.put(256, AbstractGlk.class.getMethod("setHyperlink", int.class));
		METHODS.put(257, AbstractGlk.class.getMethod("setHyperlinkStream",
				org.p2c2e.zing.Stream.class, int.class));
		METHODS.put(258, AbstractGlk.class.getMethod("requestHyperlinkEvent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(259, AbstractGlk.class.getMethod("cancelHyperlinkEvent",
				org.p2c2e.zing.IWindow.class));
		METHODS.put(352, AbstractGlk.class.getMethod("getCurrentTime",
				org.p2c2e.zing.types.GlkTimeval.class));
		METHODS.put(353,
				AbstractGlk.class.getMethod("getCurrentSimpleTime", int.class));
		METHODS.put(360, AbstractGlk.class.getMethod("convertTimeToDateUtc",
				org.p2c2e.zing.types.GlkTimeval.class,
				org.p2c2e.zing.types.GlkDate.class));
		METHODS.put(361, AbstractGlk.class.getMethod("convertTimeToDateLocal",
				org.p2c2e.zing.types.GlkTimeval.class,
				org.p2c2e.zing.types.GlkDate.class));
		METHODS.put(363, AbstractGlk.class.getMethod(
				"convertSimpleTimeToDateLocal", int.class, int.class,
				org.p2c2e.zing.types.GlkDate.class));
		METHODS.put(362, AbstractGlk.class.getMethod(
				"convertSimpleTimeToDateUtc", int.class, int.class,
				org.p2c2e.zing.types.GlkDate.class));
		METHODS.put(364, AbstractGlk.class.getMethod("convertDateToTimeUtc",
				org.p2c2e.zing.types.GlkDate.class,
				org.p2c2e.zing.types.GlkTimeval.class));
		METHODS.put(365, AbstractGlk.class.getMethod("convertDateToTimeLocal",
				org.p2c2e.zing.types.GlkDate.class,
				org.p2c2e.zing.types.GlkTimeval.class));
		METHODS.put(366, AbstractGlk.class.getMethod(
				"convertDateToSimpleTimeUtc",
				org.p2c2e.zing.types.GlkDate.class, int.class));
		METHODS.put(367, AbstractGlk.class.getMethod(
				"convertDateToSimpleTimeLocal",
				org.p2c2e.zing.types.GlkDate.class, int.class));
		METHODS.put(34, AbstractGlk.class.getMethod("windowGetRoot"));
		METHODS.put(192, AbstractGlk.class.getMethod("select",
				org.p2c2e.zing.types.GlkEvent.class));
		METHODS.put(193, AbstractGlk.class.getMethod("selectPoll",
				org.p2c2e.zing.types.GlkEvent.class));
		METHODS.put(291, AbstractGlk.class.getMethod("decomposeBufferCanon",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
		METHODS.put(292, AbstractGlk.class.getMethod("normalizeBufferCanon",
				org.p2c2e.zing.types.InOutByteBuffer.class, int.class,
				int.class));
	}
}
