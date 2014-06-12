package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;
import org.p2c2e.zing.IWindow;

public class OutWindow extends GlkType {
	public IWindow window;

	public OutWindow() {
		this(null);
	}

	public OutWindow(IWindow w) {
		window = w;
		out = true;
	}

	@Override
	public int pushToBuffer(int addr, FastByteBuffer buffer) {
		int addr1 = addr;
		buffer.putInt(addr1, window.hashCode());
		addr1 += 4;
		return addr1;
	}
}
