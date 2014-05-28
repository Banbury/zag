package org.p2c2e.zing.swing;

import java.awt.Rectangle;
import java.awt.font.FontRenderContext;

import javax.swing.JPanel;

public class BlankWindow extends Window {
	public BlankWindow(FontRenderContext c) {
		super(c);
		panel = new JPanel();
	}

	@Override
	protected int getWindowType() {
		return BLANK;
	}

	@Override
	public void rearrange(Rectangle r) {

	}
}
