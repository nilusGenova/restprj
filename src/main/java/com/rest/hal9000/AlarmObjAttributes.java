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

	public KeyPinRecord(int keyCode) {
	    this.keyCode = keyCode;
	    pinCode = 0;
	}

    }

    private int armed = 0;
    private int alarmed = 0;
    private int keyProgramming = 0;
    private int masterKey = 0;
    private Map<Integer, KeyPinRecord> keys = new HashMap();
    private int sensGreen = 0;
    private int sensRed = 0;
    private int sensAlm = 0;
    private int validSensValue = 0;

    public void storeKey(int idx, int keyval) {
	KeyPinRecord k = keys.get(idx);
	if (k == null) {
	    k = new KeyPinRecord(keyval);
	    keys.put(idx, k);
	} else {
	    k.keyCode = keyval;
	}
    }

    public boolean keyExists(int keyval) {
	for (Map.Entry<Integer, KeyPinRecord> e : keys.entrySet()) {
	    if (e.getValue().keyCode == keyval) {
		return true;
	    }
	}
	return false;
    }

    public void storePin(int idx, int pin) {
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

    public int getPin(int idx) {
	KeyPinRecord k = keys.get(idx);
	if (k != null) {
	    return k.pinCode;
	}
	return -1;
    }

    public Map<Integer, KeyPinRecord> getKeys() {
	return keys;
    }

    public void setKeys(Map<Integer, KeyPinRecord> keys) {
	this.keys = keys;
    }

    public int getArmed() {
	return armed;
    }

    public AlarmObjAttributes() {

    }

    public void setArmed(int armed) {
	this.armed = armed;
    }

    public int getAlarmed() {
	return alarmed;
    }

    public void setAlarmed(int alarmed) {
	this.alarmed = alarmed;
    }

    public int getKeyProgramming() {
	return keyProgramming;
    }

    public void setKeyProgramming(int keyProgramming) {
	this.keyProgramming = keyProgramming;
    }

    public int getMasterKey() {
	return masterKey;
    }

    public void setMasterKey(int masterKey) {
	this.masterKey = masterKey;
    }

    public int getSensGreen() {
	return sensGreen;
    }

    public void setSensGreen(int sensGreen) {
	this.sensGreen = sensGreen;
    }

    public int getSensRed() {
	return sensRed;
    }

    public void setSensRed(int sensRed) {
	this.sensRed = sensRed;
    }

    public int getSensAlm() {
	return sensAlm;
    }

    public void setSensAlm(int sensAlm) {
	this.sensAlm = sensAlm;
    }

    public int getValidSensValue() {
	return validSensValue;
    }

    public void setValidSensValue(int validSensValue) {
	this.validSensValue = validSensValue;
    }

}
