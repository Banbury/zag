package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;

public abstract class GlkType {
	protected boolean out = false;

	public boolean isOut() {
		return out;
	}

	public void setOut(boolean out) {
		this.out = out;
	}

	public abstract int pushToBuffer(int addr, FastByteBuffer buffer);
}
