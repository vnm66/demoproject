package com.birlasoft.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeUtils {
	public static Date convertStringtoDate(String date, String sourceDateFormat) throws ParseException {
		return new SimpleDateFormat(sourceDateFormat).parse(date);
	}
	
	public static Date convertStringtoDate(String date) throws ParseException {
		return new SimpleDateFormat().parse(date);
	}
	
	public static String convertDatetoString(Date date) {
		return date.toString();
	}
	
	public static String returnDateInRequestedFormat(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	
	public static String returnDateInRequestedFormat(String date, String sourceFormat, String destFormat) throws ParseException {
		return new SimpleDateFormat(destFormat).format(convertStringtoDate(date,sourceFormat));
	}
	
	public static String getTime() {
		Calendar cal = new GregorianCalendar();
		return Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + ":" + Integer.toString(cal.get(Calendar.MINUTE)) + ":"
		        + Integer.toString(cal.get(Calendar.SECOND));
	}
	
	public static String getDateInTextFormat(String data, String... dateFormat) {
		Calendar cal = Calendar.getInstance();
		String format;
		
		if(dateFormat == null) {
			format = "dd/MM/yyyy";
		} else {
			format = dateFormat[0];
		}
		
		DateFormat dtFormat = new SimpleDateFormat(format);
				
		if("Tomorrow".equalsIgnoreCase(data)) {
			cal.add(Calendar.DATE, 1);
		} else if("Yesterday".equalsIgnoreCase(data)) {
			cal.add(Calendar.DATE, -1);
		} else if("Today".equalsIgnoreCase(data)) {
		} else if("DateAfterMonth".equalsIgnoreCase(data)) {
			cal.add(Calendar.DATE, +31);
		} else if(data.toLowerCase().startsWith("today+")) {
			cal.add(Calendar.DATE, Integer.valueOf(data.toLowerCase().replaceAll("today+", "")));
		} else if(data.toLowerCase().startsWith("today-")) {
			cal.add(Calendar.DATE, -Integer.valueOf(data.toLowerCase().replaceAll("today-", "")));
		}
		return dtFormat.format(cal.getTime());
	}
}