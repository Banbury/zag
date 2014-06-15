package org.p2c2e.zing;

import org.p2c2e.zing.types.GlkEvent;

public class GlkCharConsumer implements CharInputConsumer {
	private IGlk glk;
	private IWindow w;

	GlkCharConsumer(IGlk glk, IWindow win) {
		this.glk = glk;
		w = win;
	}

	@Override
	public void consume(int c) {
		GlkEvent ev = new GlkEvent();
		ev.type = AbstractGlk.EVTYPE_CHAR_INPUT;
		ev.win = w;
		ev.val1 = c;
		ev.val2 = 0;

		glk.addEvent(ev);
	}
}