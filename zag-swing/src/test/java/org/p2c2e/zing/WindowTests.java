package org.p2c2e.zing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.p2c2e.zing.swing.Glk;
import org.p2c2e.zing.swing.GraphicsWindow;
import org.p2c2e.zing.swing.PairWindow;
import org.p2c2e.zing.swing.TextBufferWindow;
import org.p2c2e.zing.swing.TextGridWindow;

public class WindowTests {
	private Glk glk;

	@Before
	public void setup() {
		Dimension dim = new Dimension(600, 700);

		JFrame f = mock(JFrame.class);
		Container c = mock(Container.class);
		when(f.getContentPane()).thenReturn(c);
		when(c.getSize()).thenReturn(dim);
		when(c.getInsets()).thenReturn(new Insets(5, 5, 5, 5));

		glk = Glk.getInstance();
		glk.setFrame(f);
	}

	@After
	public void shutdown() {
		// glk.exit();
	}

	@Test
	public void testTextBufferWindowOpen() {
		IWindow w = glk.windowOpen(null, 0, 0, IGlk.WINTYPE_TEXT_BUFFER, 0);
		assertTrue(w instanceof TextBufferWindow);
		assertNotNull(w);
	}

	@Test
	public void testTextGridWindowOpen() {
		IWindow w = glk.windowOpen(null, 0, 0, IGlk.WINTYPE_TEXT_GRID, 0);
		assertTrue(w instanceof TextGridWindow);
		assertNotNull(w);
	}

	@Test
	public void testGraphicsWindowOpen() {
		IWindow w = glk.windowOpen(null, 0, 0, IGlk.WINTYPE_GRAPHICS, 0);
		assertTrue(w instanceof GraphicsWindow);
		assertNotNull(w);
	}

	@Test
	public void testSplitWindow() {
		IWindow w = glk.windowOpen(null, 0, 0, IGlk.WINTYPE_TEXT_BUFFER, 0);
		IWindow w1 = glk.windowOpen(w, IGlk.WINMETHOD_ABOVE
				| IGlk.WINMETHOD_PROPORTIONAL, 50, IGlk.WINTYPE_TEXT_BUFFER, 0);

		assertTrue(w1.getParent() instanceof PairWindow);
		assertEquals(w1.getParent(), w.getParent());
		assertNotNull(w1);
	}
}
