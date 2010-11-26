package com.shine.tvprogram;

import java.util.Calendar;

/**
 * Грегорийн календарыг хараал ид. Еэээ!!
 * @author tulga
 *
 */
public class DateHelper {
	static String dayLocalization[] = new String[]{
			"Даваа","Мягмар","Лхагва","Пүрэв","Баасан","Бямба","Ням",
	};
	
	public static int getTodayOfWeek() {
		Calendar thisMoment = Calendar.getInstance();
    	int today = thisMoment.get(Calendar.DAY_OF_WEEK), exactDay = 0;
    	switch(today) {
    		case Calendar.MONDAY: 		{ exactDay = 1; break; }
    		case Calendar.TUESDAY: 		{ exactDay = 2; break; }
    		case Calendar.WEDNESDAY: 	{ exactDay = 3; break; }
    		case Calendar.THURSDAY: 	{ exactDay = 4; break; }
    		case Calendar.FRIDAY: 		{ exactDay = 5; break; }
    		case Calendar.SATURDAY: 	{ exactDay = 6; break; }
    		case Calendar.SUNDAY: 		{ exactDay = 7; break; }
    	}
    	return exactDay;
	}
	
	public static String getDayName(int day) {
		return dayLocalization[day - 1];
	}
	
	public static String getCurrentTime() {
		Calendar thisMoment = Calendar.getInstance();
		String time = leftPadding(thisMoment.get(Calendar.HOUR_OF_DAY) + "", 2) + "." + 
			leftPadding(thisMoment.get(Calendar.MINUTE) + "", 2);
		return time;
	}
	
	public static long getTimeOfProgram(Integer day, String time) {
		Calendar thisMoment = Calendar.getInstance();
		thisMoment.set(Calendar.DAY_OF_WEEK, reverseDayOfWeek(day));
		String[] timeParts = time.split("\\.", 2);
		thisMoment.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
		thisMoment.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
		return thisMoment.getTimeInMillis();
	}
	
	private static Integer reverseDayOfWeek(Integer day) {
		Integer result = null;
		switch(day) {
			case 1 : {
				result = Calendar.MONDAY;
				break;
			}
			case 2 : {
				result = Calendar.TUESDAY;
				break;
			}
			case 3 : {
				result = Calendar.WEDNESDAY;
				break;
			}
			case 4 : {
				result = Calendar.THURSDAY;
				break;
			}
			case 5 : {
				result = Calendar.FRIDAY;
				break;
			}
			case 6 : {
				result = Calendar.SATURDAY;
				break;
			}
			case 7 : {
				result = Calendar.SUNDAY;
				break;
			}
		}
		return result;
	}
	
	private static String leftPadding(String str, int size) {
		while(str.length() < size) {
			str = '0' + str;
		}
		return str;
	}
}
