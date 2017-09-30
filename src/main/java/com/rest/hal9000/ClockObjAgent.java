package com.rest.hal9000;

import java.util.Calendar;
import java.util.function.Consumer;

public class ClockObjAgent extends HalObjAgent {

    private class ExposedAttributes {
	public String date;
	public String time;
	public int weekDay; // 0:Sun -- 6:Sat
	private int epochtime; // value in Sec from 1-1-2000

	public ExposedAttributes() {
	    super();
	    date = "01-01-2000";
	    time = "00:00";
	    weekDay = 0;
	    epochtime = 0;
	}
	    
	public void setEpocTime(int et) {

	    epochtime = et;
	    weekDay = (epochtime / 3600) / 24;
	    weekDay = (weekDay + 6) % 7; // 0:Sun --- 6:Sat

	    int hour = ((epochtime / 3600) % 24);
	    int min = ((epochtime / 60) % 60);
	    time = String.format("%2d:%2d", hour, min);

	    int years;
	    int epoch = epochtime / 3600;
	    epoch /= 24;
	    years = epoch / (365 * 4 + 1) * 4;
	    epoch %= 365 * 4 + 1;
	    int year;
	    int month;
	    int day;
	    for (year = 3; year > 0; year--) {
		if (epoch >= days[year][0])
		    break;
	    }
	    for (month = 11; month > 0; month--) {
		if (epoch >= days[year][month])
		    break;
	    }
	    day = epoch - days[year][month] + 1;
	    year = 2000 + years + year;
	    month += 1;

	    date = String.format("2%d-2%d-4%d", day, month, year);
	}

    }

    private final static int[] day1 = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };
    private final static int[] day2 = { 366, 397, 425, 456, 486, 517, 547, 578, 609, 639, 670, 700 };
    private final static int[] day3 = { 731, 762, 790, 821, 851, 882, 912, 943, 974, 1004, 1035, 1065 };
    private final static int[] day4 = { 1096, 1127, 1155, 1186, 1216, 1247, 1277, 1308, 1339, 1369, 1400, 1430 };

    private final static int[][] days = { day1, day2, day3, day4 }; // days[4][12]

    ExposedAttributes exposedAttributes = new ExposedAttributes();

    public ClockObjAgent(char id, Consumer<String> sendMsgCallBack) {
	super(id, sendMsgCallBack);
	// TODO Auto-generated constructor stub
    }

    public void setActualTime() {
	Calendar cal = Calendar.getInstance();

	int val;
	int second = cal.get(Calendar.SECOND); // 0-59
	int minute = cal.get(Calendar.MINUTE); // 0-59
	int hour = cal.get(Calendar.HOUR); // 0-23
	int day = cal.get(Calendar.DAY_OF_MONTH) - 1; // 0-30
	int month = cal.get(Calendar.MONTH) - 1; // 0-11
	int year = cal.get(Calendar.YEAR) - 2000; // 0-99

	val = (((year / 4 * (365 * 4 + 1) + days[year % 4][month] + day) * 24 + hour) * 60 + minute) * 60 + second;

	sendMsgToObj("SCE" + val);
    }

    @Override
    public void parseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	case 'D':
	    exposedAttributes.date = msg;
	    break;
	case 'T':
	    exposedAttributes.time = msg;
	    break;
	case 'W':
	    exposedAttributes.weekDay = Integer.parseInt(msg);
	    break;
	case 'E':
	    exposedAttributes.setEpocTime(Integer.parseInt(msg));
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    public void parseEvent(char event, String msg) {
	switch (event) {
	case 'D':
	    alignAll();
	    break;
	default:
	    wrongAttribute();
	}
    }
    
    @Override
    public void alignAll() {
	sendMsgToObj("GCE");
    }
    
    @Override
    public String exposeData() {
	return "error from clock";
	//TODO:
    }
    
    @Override
    public boolean executeCmd(String cmd) {
	setActualTime();
  	return true;
      }

}
