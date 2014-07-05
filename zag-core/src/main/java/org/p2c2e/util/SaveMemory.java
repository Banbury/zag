package org.p2c2e.util;

import org.p2c2e.zag.Zag;

public class SaveMemory extends FastByteBuffer {
	private int ramstart = 0;

	public SaveMemory(int cap) {
		super(cap);
	}

	public int getRamstart() {
		return ramstart;
	}

	public void setRamstart(int ramstart) {
		this.ramstart = ramstart;
	}

	@Override
	public void put(int pos, byte b) {
		if (pos < ramstart) {
			Zag.fatal("Attempt to write to ROM.");
		}
		super.put(pos, b);
	}
}
