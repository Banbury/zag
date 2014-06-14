package org.p2c2e.zing;

import org.p2c2e.blorb.Color;

public interface IGraphicsWindow {

	public void eraseRect(final int left, final int top, final int width,
			final int height);

	public void fillRect(final Color c, final int left, final int top,
			final int width, final int height);

	public void setBackgroundColor(Color c);

}
