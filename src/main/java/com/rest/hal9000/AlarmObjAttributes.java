package com.rest.hal9000;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AlarmObjAttributes {

    private static final Logger log = LoggerFactory.getLogger(AlarmObjAttributes.class);

    private final static int MAX_NUM_OF_KEYS = 10;

    private static class KeyPinRecord {
	private int keyCode;
	private int pinCode;

	@JsonCreator
	public KeyPinRecord(@JsonProperty("keyCode") int keyCode, @JsonProperty("pinCode") int pincode) {
	    this.keyCode = keyCode;
	    this.pinCode = pincode;
	}

	public KeyPinRecord(final int keyCode) {
	    this.keyCode = keyCode;
	    pinCode = 0;
	}

    }

    private int armed = 0;
    private int alarmed = 0;
    private int keyProgramming = 0;
    private int masterKey = 0;
    private int camPower = 0;
    private int autoCamPower = 0;
    private Map<Integer, KeyPinRecord> keys = new HashMap();

    public void storeKey(final int idx, final int keyval) {
	KeyPinRecord k = keys.get(idx);
	if (k == null) {
	    k = new KeyPinRecord(keyval);
	    keys.put(idx, k);
	} else {
	    k.keyCode = keyval;
	}
    }

    public boolean keyExists(final int keyval) {
	for (Map.Entry<Integer, KeyPinRecord> e : keys.entrySet()) {
	    if (e.getValue().keyCode == keyval) {
		return true;
	    }
	}
	return false;
    }

    public void storePin(final int idx, final int pin) {
	KeyPinRecord k = keys.get(idx);
	if (k == null) {
	    k = new KeyPinRecord(0);
	    keys.put(idx, k);
	}
	k.pinCode = pin;
    }

    public void flushKeys() {
	keys.clear();
    }

    public int getPin(final int idx) {
	KeyPinRecord k = keys.get(idx);
	if (k != null) {
	    return k.pinCode;
	}
	return -1;
    }

    public Map<Integer, KeyPinRecord> getKeys() {
	return keys;
    }

    public void setKeys(final Map<Integer, KeyPinRecord> keys) {
	this.keys = keys;
    }

    public int getArmed() {
	return armed;
    }

    public AlarmObjAttributes() {

    }

    // return true if changed
    public boolean setArmed(final int armed) {
    	final boolean retVal = (this.armed != armed);
    	this.armed = armed;
    	return retVal;
    }

    public int getAlarmed() {
	return alarmed;
    }

    // return true if changed
    public boolean setAlarmed(final int alarmed) {
    	final boolean retVal = (this.alarmed != alarmed);
    	this.alarmed = alarmed;
    	return retVal;
    }

    public int getKeyProgramming() {
	return keyProgramming;
    }

    // return true if changed
    public boolean setKeyProgramming(final int keyProgramming) {
    	final boolean retVal = (this.keyProgramming != keyProgramming);
    	this.keyProgramming = keyProgramming;
    	return retVal;
    }

    public int getMasterKey() {
	return masterKey;
    }

    public void setMasterKey(final int masterKey) {
	this.masterKey = masterKey;
    }

    // return true if changed
    public boolean setCamPower(final int val) {
    	final boolean retVal = (this.camPower != val);
    	this.camPower = val;
    	return retVal;
    }

    public int getCamPower() {
	    return camPower;
    }
    
    // return true if changed
    public boolean setAutoCamPower(final int val) {
    	final boolean retVal = (this.autoCamPower != val);
    	this.autoCamPower = val;
    	return retVal;
    }

    public int getAutoCamPower() {
	    return autoCamPower;
    }
}
