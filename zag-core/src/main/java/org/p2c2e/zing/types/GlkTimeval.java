package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;

public class GlkTimeval extends GlkType {
	private int highsec;
	private int lowsec;
	private int microsec;

	public GlkTimeval(int highsec, int lowsec, int microsec) {
		super();
		this.highsec = highsec;
		this.lowsec = lowsec;
		this.microsec = microsec;
	}

	public int getHighsec() {
		return highsec;
	}

	public void setHighsec(int highsec) {
		this.highsec = highsec;
	}

	public int getLowsec() {
		return lowsec;
	}

	public void setLowsec(int lowsec) {
		this.lowsec = lowsec;
	}

	public int getMicrosec() {
		return microsec;
	}

	public void setMicrosec(int microsec) {
		this.microsec = microsec;
	}

	public Long getUnixTime() {
		return ((long) highsec << 32) + lowsec;
	}

	@Override
	public int pushToBuffer(int addr, FastByteBuffer buffer) {
		int addr1 = addr;
		buffer.putInt(addr1, (getHighsec()));
		addr1 += 4;
		buffer.putInt(addr1, (getLowsec()));
		addr1 += 4;
		buffer.putInt(addr1, (getMicrosec()));
		addr1 += 4;
		return addr1;
	}
}
