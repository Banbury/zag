package org.p2c2e.zing;

import java.nio.ByteBuffer;

import org.p2c2e.zing.types.GlkEvent;

public class GlkLineConsumer implements LineInputConsumer {
	private IGlk glk;
	IWindow w;
	ByteBuffer b;
	boolean unicode;

	GlkLineConsumer(IGlk glk, IWindow win, ByteBuffer buf, boolean unicode) {
		this.glk = glk;
		w = win;
		b = buf;
		this.unicode = unicode;
	}

	@Override
	public void consume(String s) {
		GlkEvent ev = new GlkEvent();
		cancel(s);
		ev.type = AbstractGlk.EVTYPE_LINE_INPUT;
		ev.win = w;
		ev.val1 = s.length();
		ev.val2 = 0;
		glk.addEvent(ev);
	}

	@Override
	public void cancel(String s) {
		int l = s.length();
		if (unicode) {
			// fixme does not handle astral plane
			for (int i = 0; i < l; i++)
				b.putInt(i * 4, s.charAt(i));
		} else {
			for (int i = 0; i < l; i++)
				b.put(i, (byte) s.charAt(i));
		}
	}
}