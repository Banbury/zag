package org.p2c2e.zing.swing;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.UIManager;

public class Properties {
	private static Properties instance;

	public static Properties getInstance() {
		if (instance == null) {
			instance = new Properties();
		}
		return instance;
	}

	private java.util.Properties props;

	public Properties() {
		this.props = new java.util.Properties();
		try {
			InputStream stream = new FileInputStream(
					System.getProperty("user.dir") + "/zag.properties");
			props.load(stream);
		} catch (Exception e) {
			System.out
					.println("No properties file found. Using default values.");
		}
	}

	public String getPlafName() {
		if (props.containsKey("plaf")) {
			String plaf = props.getProperty("plaf");
			if (plaf.equals("system"))
				return UIManager.getSystemLookAndFeelClassName();
			else
				return plaf;
		} else {
			return UIManager.getSystemLookAndFeelClassName();
		}
	}

	public boolean getSaveMemory() {
		if (props.containsKey("save_memory")) {
			return props.getProperty("save_memory").toLowerCase()
					.equals("true");
		} else {
			return true;
		}
	}
}
