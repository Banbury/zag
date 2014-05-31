package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;

public class Int extends GlkType {
	public int val;

	@Override
	public int pushToBuffer(int addr, FastByteBuffer buffer) {
		int addr1 = addr;
		buffer.putInt(addr1, val);
		addr1 += 4;
		return addr1;
	}
}
