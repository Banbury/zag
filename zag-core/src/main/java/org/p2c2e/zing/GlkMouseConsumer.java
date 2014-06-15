package org.p2c2e.zing;

import org.p2c2e.zing.types.GlkEvent;

public class GlkMouseConsumer implements MouseInputConsumer {
	IGlk glk;
	IWindow w;

	GlkMouseConsumer(IGlk glk, IWindow win) {
		this.glk = glk;
		w = win;
	}

	@Override
	public void consume(int x, int y) {
		GlkEvent e = new GlkEvent();
		e.type = AbstractGlk.EVTYPE_MOUSE_INPUT;
		e.win = w;
		e.val1 = x;
		e.val2 = y;
		glk.addEvent(e);
	}
}