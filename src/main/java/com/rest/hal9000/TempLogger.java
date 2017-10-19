package com.rest.hal9000;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.function.Consumer;

public class TempLogger extends HalObjAgent {

    public TempLogger(final String pathName, final Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
	// TODO Auto-generated constructor stub
    }

    @Override
    protected Object getExposedData() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected String getExposedAttribute(final String attr) throws Exception {
	// TODO Auto-generated method stub
	return null;
    }

    public void logTemperature(final String val) {
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat date_format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	log.info("LOG_TEMP [{}]{}", date_format.format(cal.getTime()), val);
    }

    @Override
    protected void specializedParseGetAnswer(final char attribute, final String msg) {
	return;
    }

    @Override
    protected void specializedParseEvent(final char event, final String msg) {
	return;
    }

    @Override
    public void alignAll() {
	return;
    }

}
