package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;

public class StreamResult extends GlkType {
	public int readcount;
	public int writecount;

	public StreamResult() {
		super();
		out = true;
	}

	@Override
	public int pushToBuffer(int addr, FastByteBuffer buffer) {
		int addr1 = addr;
		buffer.putInt(addr1, readcount);
		addr1 += 4;
		buffer.putInt(addr1, writecount);
		addr1 += 4;
		return addr1;
	}
}