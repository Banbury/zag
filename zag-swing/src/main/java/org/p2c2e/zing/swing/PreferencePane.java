package org.p2c2e.zing.swing;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.font.TextAttribute;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import org.p2c2e.blorb.Color;
import org.p2c2e.zing.IGlk;
import org.p2c2e.zing.Style;

public class PreferencePane extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	JButton okay;
	JButton apply;
	JButton cancel;
	StyleEditPane bsp;
	StyleEditPane gsp;
	JFrame f;
	CloseCallback cc;

	public PreferencePane(JFrame f, CloseCallback cc) {
		super();
		this.f = f;
		this.cc = cc;
		BoxLayout l = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(l);

		okay = new JButton("OK");
		cancel = new JButton("Cancel");
		apply = new JButton("Apply");

		okay.setActionCommand("ok-styles");
		cancel.setActionCommand("cancel-styles");
		apply.setActionCommand("apply-styles");

		Box bbox = new Box(BoxLayout.X_AXIS);
		bbox.add(cancel);
		bbox.add(apply);
		bbox.add(okay);

		JTabbedPane jtp = new JTabbedPane();
		bsp = new StyleEditPane(IGlk.WINTYPE_TEXT_BUFFER);
		gsp = new StyleEditPane(IGlk.WINTYPE_TEXT_GRID);
		jtp.add("Story Windows", bsp);
		jtp.add("Grid Windows", gsp);
		add(jtp);
		add(bbox);

		okay.addActionListener(this);
		cancel.addActionListener(this);
		apply.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String st = e.getActionCommand();

		if ("ok-styles".equals(st)) {
			bsp.applyStyles();
			gsp.applyStyles();

			Window.applyStyle();

			cc.close();
		} else if ("apply-styles".equals(st)) {
			bsp.applyStyles();
			gsp.applyStyles();

			Window.applyStyle();
		} else if ("cancel-styles".equals(st)) {
			cc.close();
		}
	}

	class StyleEditPane extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;

		Style[] styles;
		int type;
		JComboBox styleCombo;
		FontPane fp;
		ParaPane pp;

		StyleEditPane(int iType) {
			super();
			type = iType;
			styles = new Style[IGlk.STYLE_NUMSTYLES];

			BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);

			Box styleBox = new Box(BoxLayout.X_AXIS);
			styleBox.add(new JLabel("Style to edit: "));
			styleCombo = new JComboBox();
			styleCombo.setActionCommand("change-style");

			for (int i = 0; i < IGlk.STYLE_NUMSTYLES; i++) {
				styles[i] = (Style) Style.getStyle(IGlk.STYLES[i], type)
						.clone();
				styleCombo.addItem(IGlk.STYLES[i]);
			}

			styleBox.add(styleCombo);
			add(styleBox);

			fp = new FontPane();
			fp.init(styles[styleCombo.getSelectedIndex()]);
			add(fp);

			pp = new ParaPane();
			pp.init(styles[styleCombo.getSelectedIndex()]);
			add(pp);

			styleCombo.addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if ("change-style".equals(e.getActionCommand())) {
				fp.init(styles[styleCombo.getSelectedIndex()]);
				pp.init(styles[styleCombo.getSelectedIndex()]);
			}
		}

		void applyStyles() {
			for (int i = 0; i < IGlk.STYLE_NUMSTYLES; i++) {
				styles[i].setMap(null);
				Style.addStyle(styles[i], type);
			}
		}
	}

	class ParaPane extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;

		Style s;
		JComboBox left, right, par, just;

		ParaPane() {
			super();
			BoxLayout l = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(l);

			setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(java.awt.Color.black),
					"Paragraph Formatting"));

			Box leftBox = new Box(BoxLayout.X_AXIS);
			Box rightBox = new Box(BoxLayout.X_AXIS);
			Box parBox = new Box(BoxLayout.X_AXIS);
			Box justBox = new Box(BoxLayout.X_AXIS);

			String[] per = new String[] { "0%", "3%", "6%", "9%", "12%", "15%",
					"18%", "21%", "24%" };
			left = new JComboBox(per);
			right = new JComboBox(per);
			par = new JComboBox(per);
			just = new JComboBox(new String[] { "left justified",
					"left/right justified", "centered", "right justified" });
			left.setActionCommand("left-indent-change");
			right.setActionCommand("right-indent-change");
			par.setActionCommand("paragraph-indent-change");
			just.setActionCommand("justification-change");

			left.addActionListener(this);
			right.addActionListener(this);
			par.addActionListener(this);
			just.addActionListener(this);

			leftBox.add(new JLabel(
					"Left indentation (as % of available width): "));
			leftBox.add(left);
			rightBox.add(new JLabel(
					"Right indentation (as % of available width): "));
			rightBox.add(right);
			parBox.add(new JLabel(
					"Paragraph indentation (as % of available width): "));
			parBox.add(par);

			justBox.add(new JLabel("Justification: "));
			justBox.add(just);

			add(leftBox);
			add(rightBox);
			add(parBox);
			add(justBox);
		}

		void init(Style s) {
			this.s = s;
			left.setSelectedIndex(s.leftIndent);
			right.setSelectedIndex(s.rightIndent);
			par.setSelectedIndex(s.parIndent);
			just.setSelectedIndex(s.justification);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String st = e.getActionCommand();

			if ("left-indent-change".equals(st)) {
				s.leftIndent = left.getSelectedIndex();
			} else if ("right-indent-change".equals(st)) {
				s.rightIndent = right.getSelectedIndex();
			} else if ("paragraph-indent-change".equals(st)) {
				s.parIndent = par.getSelectedIndex();
			} else if ("justification-change".equals(st)) {
				s.justification = just.getSelectedIndex();
			}
		}
	}

	class FontPane extends JPanel implements ActionListener, ItemListener {
		private static final long serialVersionUID = 1L;

		Object lastDeselected;
		Style s;
		JComboBox wf;
		JComboBox sb;
		JCheckBox ib;
		JCheckBox bb;
		JCheckBox ub;
		JPanel tcPanel, bcPanel;

		FontPane() {
			super();
			BoxLayout l = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(l);

			setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createLineBorder(java.awt.Color.black),
					"Fonts"));

			Box tfBox = new Box(BoxLayout.X_AXIS);
			tfBox.add(new JLabel("Typeface: "));

			wf = new JComboBox(GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.getAvailableFontFamilyNames());
			tfBox.add(wf);
			wf.setActionCommand("font-family-change");
			wf.addActionListener(this);

			Box szBox = new Box(BoxLayout.X_AXIS);
			szBox.add(new JLabel("Size: "));
			sb = new JComboBox(
					new String[] { "9", "10", "12", "14", "18", "24" });
			sb.setEditable(true);
			szBox.add(sb);
			sb.setActionCommand("font-size-change");
			sb.addActionListener(this);

			Box attributeBox = new Box(BoxLayout.X_AXIS);
			ib = new JCheckBox("Italic");
			bb = new JCheckBox("Bold");
			ub = new JCheckBox("Underline");
			attributeBox.add(ib);
			attributeBox.add(bb);
			attributeBox.add(ub);
			ib.setActionCommand("font-italic-change");
			ib.addActionListener(this);
			bb.setActionCommand("font-bold-change");
			bb.addActionListener(this);
			ub.setActionCommand("font-underline-change");
			ub.addActionListener(this);

			JPanel cGrid = new JPanel(new GridLayout(2, 3));
			cGrid.add(new JLabel("Text color: "));
			tcPanel = new JPanel();
			tcPanel.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			tcPanel.setOpaque(true);
			tcPanel.setSize(new Dimension(30, 30));
			tcPanel.setBackground(java.awt.Color.black);
			cGrid.add(tcPanel);
			JButton tcButton = new JButton("Change");
			tcButton.setActionCommand("change-text-color");
			tcButton.addActionListener(this);
			cGrid.add(tcButton);

			cGrid.add(new JLabel("Background color: "));
			bcPanel = new JPanel();
			bcPanel.setBorder(BorderFactory
					.createBevelBorder(BevelBorder.LOWERED));
			bcPanel.setOpaque(true);
			bcPanel.setSize(new Dimension(30, 30));
			bcPanel.setBackground(java.awt.Color.white);
			cGrid.add(bcPanel);
			JButton bcButton = new JButton("Change");
			bcButton.setActionCommand("change-back-color");
			bcButton.addActionListener(this);
			cGrid.add(bcButton);

			add(tfBox);
			add(szBox);
			add(attributeBox);
			add(cGrid);
		}

		void init(Style s) {
			this.s = s;
			wf.setSelectedItem(s.family);
			sb.setSelectedItem(String.valueOf(s.size));
			ib.setSelected(s.isOblique);
			ub.setSelected(s.isUnderlined);
			bb.setSelected(s.weight.floatValue() > TextAttribute.WEIGHT_REGULAR
					.floatValue());
			tcPanel.setBackground(new java.awt.Color(s.textColor.getRed(),
					s.textColor.getGreen(), s.textColor.getBlue()));
			bcPanel.setBackground(new java.awt.Color(s.backColor.getRed(),
					s.backColor.getGreen(), s.backColor.getBlue()));
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.DESELECTED)
				lastDeselected = e.getItem();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String st = e.getActionCommand();

			if ("change-text-color".equals(st)) {
				java.awt.Color c = JColorChooser.showDialog(this,
						"Choose a text color", tcPanel.getBackground());
				if (c != null) {
					tcPanel.setBackground(c);
					tcPanel.repaint();

					s.textColor = new Color(c.getRed(), c.getGreen(),
							c.getBlue());
				}
			} else if ("change-back-color".equals(st)) {
				java.awt.Color c = JColorChooser.showDialog(this,
						"Choose a background color", bcPanel.getBackground());
				if (c != null) {
					bcPanel.setBackground(c);
					bcPanel.repaint();

					s.backColor = new Color(c.getRed(), c.getGreen(),
							c.getBlue());
				}
			} else if ("font-family-change".equals(st)) {
				s.family = (String) wf.getSelectedItem();
			} else if ("font-size-change".equals(st)) {
				String stItem = sb.getSelectedItem().toString();

				try {
					s.size = Integer.parseInt(stItem);
				} catch (NumberFormatException ex) {
					if (lastDeselected != null)
						sb.setSelectedItem(lastDeselected);
					else
						sb.setSelectedIndex(0);
				}
			} else if ("font-italic-change".equals(st)) {
				s.isOblique = ib.isSelected();
			} else if ("font-bold-change".equals(st)) {
				s.weight = (bb.isSelected()) ? TextAttribute.WEIGHT_BOLD
						: TextAttribute.WEIGHT_REGULAR;
			} else if ("font-underline-change".equals(st)) {
				s.isUnderlined = ub.isSelected();
			}
		}
	}

	public interface CloseCallback {
		public void close();
	}
}
