package org.p2c2e.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class FastByteBuffer {
	private byte[] data;
	private int position;
	private int limit;
	private int capacity;
	private int minSize;

	public FastByteBuffer(int cap) {
		data = new byte[cap];
		capacity = cap;
		limit = cap;
		position = 0;
	}

	public void setMinSize(int minSize) {
		this.minSize = minSize;
	}

	public int take(InputStream in) throws IOException {
		int r;
		int tot = limit - position;
		int amt = tot;

		while (amt > 0) {
			r = in.read(data, position, amt);
			if (r == -1)
				break;
			position += r;
			amt -= r;
		}
		return tot - amt;
	}

	public void send(OutputStream out) throws IOException {
		out.write(data, position, limit - position);
		position = limit;
	}

	public void get(byte[] arr, int off) {
		System.arraycopy(data, position, arr, off, limit - position);
		position = limit;
	}

	public byte get() {
		return data[position++];
	}

	public byte get(int pos) {
		return data[pos];
	}

	public short getShort() {
		return (short) (((data[position++]) << 8) | ((data[position++]) & 0xff));
	}

	public short getShort(int pos) {
		return (short) (((data[pos]) << 8) | ((data[pos + 1]) & 0xff));
	}

	public int getInt() {
		return (((data[position++]) << 24)
				| (((data[position++]) & 0xff) << 16)
				| (((data[position++]) & 0xff) << 8) | ((data[position++]) & 0xff));
	}

	public int getInt(int pos) {
		return (((data[pos]) << 24) | (((data[pos + 1]) & 0xff) << 16)
				| (((data[pos + 2]) & 0xff) << 8) | ((data[pos + 3]) & 0xff));
	}

	public void put(byte[] arr, int off, int len) {
		System.arraycopy(arr, off, data, position, len);
		position += len;
	}

	public void put(FastByteBuffer fb) {
		int len = fb.limit - fb.position;
		System.arraycopy(fb.data, fb.position, data, position, len);

		position += len;
	}

	public void put(ByteBuffer bb) {
		if (bb.hasArray()) {
			put(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining());
			position += bb.remaining();
		} else {
			asByteBuffer().put(bb);
		}
		position += bb.remaining();
	}

	public void put(byte b) {
		put(position++, b);
	}

	public void put(int pos, byte b) {
		data[pos] = b;
	}

	public void putShort(short s) {
		put((byte) (s >>> 8));
		put((byte) (s & 0xff));
	}

	public void putShort(int pos, short s) {
		put(pos, (byte) (s >>> 8));
		put(pos + 1, (byte) (s & 0xff));
	}

	public void putInt(int i) {
		put((byte) (i >>> 24));
		put((byte) ((i >>> 16) & 0xff));
		put((byte) ((i >>> 8) & 0xff));
		put((byte) (i & 0xff));
	}

	public void putInt(int pos, int i) {
		put(pos, (byte) (i >>> 24));
		put(pos + 1, (byte) ((i >>> 16) & 0xff));
		put(pos + 2, (byte) ((i >>> 8) & 0xff));
		put(pos + 3, (byte) (i & 0xff));
	}

	public byte[] array() {
		return data;
	}

	public int capacity() {
		return capacity;
	}

	public int limit() {
		return limit;
	}

	public void limit(int lim) {
		limit = lim;
	}

	public int position() {
		return position;
	}

	public void position(int pos) {
		position = pos;
	}

	public int remaining() {
		return limit - position;
	}

	public void rewind() {
		position = 0;
	}

	public boolean hasRemaining() {
		return (limit - position) > 0;
	}

	public void flip() {
		limit = position;
		position = 0;
	}

	public void clear() {
		position = 0;
		limit = capacity;
	}

	public FastByteBuffer resize(int newsize) {
		if (newsize < minSize) {
			return null;
		}

		if ((newsize & 0xff) != 0) {
			return null;
		}
		if (newsize < capacity()) {
			limit(newsize);
			return this;
		}

		FastByteBuffer newmem = new FastByteBuffer(newsize);
		newmem.setMinSize(minSize);
		position(0);
		newmem.put(this);
		for (int i = limit(); i < newsize; i++)
			newmem.put((byte) 0);

		return newmem;
	}

	public ByteBuffer asByteBuffer() {
		ByteBuffer b = ByteBuffer.wrap(data, 0, capacity);
		b.position(position);
		b.limit(limit);
		return b;
	}

	public ByteBuffer slice() {
		ByteBuffer b = asByteBuffer();
		b.position(position);
		return b.slice();
	}
}
