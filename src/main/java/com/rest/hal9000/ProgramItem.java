package com.rest.hal9000;

public class ProgramItem implements Comparable<ProgramItem> {

    private final static int MASK_DAY = 0xe0;
    private final static int MASK_TERM_MODE = 0x10;
    private final static int MASK_MODE = 0x0e;
    private final static int MASK_INTERP = 0x08;
    private final static int MASK_TEMP_LEVEL = 0x06;
    private final static int MASK_AMPM = 0x01;
    private final static int MASK_HOUR = 0xf0;
    private final static int MASK_MIN = 0x0f;

    // Hour-5Min-Day-Mode
    private int hour; // 24 ore 5 bit : am/pm + 0-11
    private int min; // 60:5=12 4 bit *5 min
    private int day; // 7+1 3 bit 0:Sun – 6:Sat 7:Special
    private int tempLevel; // Temp (bit 3-4): 0=off 1=T1 2=T2 3=T3
    private boolean interpolation; // Interp 1 bit: on/off 1 bit

    public ProgramItem() {
	hour = 0;
	min = 0;
	day = 0;
	tempLevel = 0;
	interpolation = false;
    }

    public ProgramItem(int hour, int min, int day, int tempLevel, boolean interpolation) {
	this.hour = hour;
	this.min = (min / 5) * 5;
	this.day = day;
	this.tempLevel = tempLevel;
	this.interpolation = interpolation;
    }

    public String getHalFormat() {
	int byte1 = 0;
	int byte2 = 0;

	byte1 = ((day << 5) & MASK_DAY);
	byte1 += MASK_TERM_MODE;
	if (interpolation) {
	    byte1 += MASK_INTERP;
	}
	byte1 += (tempLevel << 1) & MASK_TEMP_LEVEL;

	byte2 = hour;
	if (hour >= 12) {
	    byte1 += MASK_AMPM;
	    byte2 -= 12;
	}

	byte2 = ((byte2 << 4) & MASK_HOUR);
	byte2 += (min / 5) & MASK_MIN;

	// System.out.println(String.format("0x%02x %02x", byte1, byte2));

	return Integer.toString((byte1 << 8) + byte2);
    }

    public boolean isValidFormat(String val) {
	int value = Integer.parseInt(val);
	int byte1 = (byte) (value >> 8);
	int byte2 = (byte) (value & 0xff);

	if ((byte1 & MASK_TERM_MODE) != MASK_TERM_MODE) {
	    return false;
	}
	if (((byte2 & MASK_HOUR) >> 4) >= 12) {
	    return false;
	}
	if ((byte2 & MASK_MIN) >= 12) {
	    return false;
	}
	return true;
    }

    public boolean setFromHalFormat(String val) {
	if (!isValidFormat(val)) {
	    return false;
	}
	int value = Integer.parseInt(val);
	int byte1 = (value >> 8);
	int byte2 = (value & 0xff);

	day = (byte1 & MASK_DAY) >> 5;
	interpolation = ((byte1 & MASK_INTERP) == MASK_INTERP);
	tempLevel = (byte1 & MASK_TEMP_LEVEL) >> 1;

	hour = (byte2 & MASK_HOUR) >> 4;
	if ((byte1 & MASK_AMPM) == MASK_AMPM) {
	    hour += 12;
	}
	min = 5 * (byte2 & MASK_MIN);

	return true;
    }

    public int getHour() {
	return hour;
    }

    public void setHour(int hour) {
	this.hour = hour;
    }

    public int getMin() {
	return min;
    }

    public void setMin(int min) {
	this.min = min;
    }

    public int getDay() {
	return day;
    }

    public void setDay(int day) {
	this.day = day;
    }

    public int getTempLevel() {
	return tempLevel;
    }

    public void setTempLevel(int temp) {
	this.tempLevel = temp;
    }

    public boolean isInterpolation() {
	return interpolation;
    }

    public void setInterpolation(boolean interpolation) {
	this.interpolation = interpolation;
    }

    @Override
    public int compareTo(final ProgramItem item) {
	// final int BEFORE = -1;
	// final int EQUAL = 0;
	// final int AFTER = 1;

	if (this.day != item.day) {
	    return Integer.compare(this.day, item.day);
	}
	if (this.hour != item.hour) {
	    return Integer.compare(this.hour, item.hour);
	}
	return Integer.compare(this.min, item.min);
    }

    @Override
    public String toString() {
	return "[" + day + "-" + hour + ":" + min + "]T" + tempLevel + (interpolation ? "i" : "");
    }

}
