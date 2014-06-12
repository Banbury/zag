package org.p2c2e.zing.types;

import org.p2c2e.util.FastByteBuffer;

public class GlkDate extends GlkType {
	private int year; /* full (four-digit) year */
	private int month; /* 1-12, 1 is January */
	private int day; /* 1-31 */
	private int weekday; /* 0-6, 0 is Sunday */
	private int hour; /* 0-23 */
	private int minute; /* 0-59 */
	private int second; /* 0-59, maybe 60 during a leap second */
	private int microsec; /* 0-999999 */

	public GlkDate(int year, int month, int day, int weekday, int hour,
			int minute, int second, int microsec) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.weekday = weekday;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.microsec = microsec;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getWeekday() {
		return weekday;
	}

	public void setWeekday(int weekday) {
		this.weekday = weekday;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public int getMicrosec() {
		return microsec;
	}

	public void setMicrosec(int microsec) {
		this.microsec = microsec;
	}

	@Override
	public int pushToBuffer(int addr, FastByteBuffer buffer) {
		int addr1 = addr;
		buffer.putInt(addr1, (getYear()));
		addr1 += 4;
		buffer.putInt(addr1, (getMonth()));
		addr1 += 4;
		buffer.putInt(addr1, (getDay()));
		addr1 += 4;
		buffer.putInt(addr1, (getWeekday()));
		addr1 += 4;
		buffer.putInt(addr1, (getHour()));
		addr1 += 4;
		buffer.putInt(addr1, (getMinute()));
		addr1 += 4;
		buffer.putInt(addr1, (getSecond()));
		addr1 += 4;
		buffer.putInt(addr1, (getMicrosec()));
		addr1 += 4;
		return addr1;
	}
}
