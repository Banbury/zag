package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;
import org.p2c2e.zing.IWindow;

public class GlkEvent extends GlkType {
	public int type;
	public IWindow win;
	public int val1, val2;

	public GlkEvent() {
		super();
		out = true;
	}

	@Override
	public int pushToBuffer(int addr, FastByteBuffer buffer) {
		int addr1 = addr;
		buffer.putInt(addr1, type);
		addr1 += 4;
		buffer.putInt(addr1, ((win == null) ? 0 : win.hashCode()));
		addr1 += 4;
		buffer.putInt(addr1, val1);
		addr1 += 4;
		buffer.putInt(addr1, val2);
		addr1 += 4;
		return addr1;
	}
}