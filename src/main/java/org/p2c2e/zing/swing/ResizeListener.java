package org.p2c2e.zing.swing;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.types.GlkEvent;


public class ResizeListener extends ComponentAdapter implements
		HierarchyListener {
	@Override
	public void componentResized(ComponentEvent e) {
		if (Window.getRoot() != null)
			LameFocusManager.rootRearrange();

		GlkEvent ev = new GlkEvent();
		ev.type = IGlk.EVTYPE_ARRANGE;
		ev.win = null;
		ev.val1 = 0;
		ev.val2 = 0;
		Glk.addEvent(ev);
	}

	public void hierarchyChanged(HierarchyEvent e) {
		if ((e.getChangeFlags() & HierarchyEvent.ANCESTOR_RESIZED) != 0) {
			// if (Window.root != null)
			// LameFocusManager.rootRearrange();

			GlkEvent ev = new GlkEvent();
			ev.type = IGlk.EVTYPE_ARRANGE;
			ev.win = null;
			ev.val1 = 0;
			ev.val2 = 0;
			Glk.addEvent(ev);
		}
	}
}
