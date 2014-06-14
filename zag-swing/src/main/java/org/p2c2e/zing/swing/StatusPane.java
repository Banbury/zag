package org.p2c2e.zing.swing;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class StatusPane extends JPanel {
	public static Component BLANK = Box.createVerticalStrut(25);
	public static JLabel MORE = new JLabel("[More]", SwingConstants.LEFT);
	public static JLabel EXIT = new JLabel("[*** End of session ***]",
			SwingConstants.LEFT);

	private JProgressBar prog;
	private Component current;

	StatusPane() {
		super();
		prog = new JProgressBar();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		show(BLANK);
	}

	public JProgressBar getProgressBar() {
		return prog;
	}

	public void show(Component l) {
		if (l == null)
			l = BLANK;

		if (l != current) {
			removeAll();
			add(l);
			add(BLANK);
			add(Box.createHorizontalGlue());
			add(prog);
			revalidate();
			repaint();
			current = l;
		}
	}
}
