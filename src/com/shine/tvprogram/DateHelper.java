package com.shine.tvprogram;

import java.util.Calendar;

public class DateHelper {
	static String dayLocalization[] = new String[]{
			"Даваа","Мягмар","Лхагва","Пүрэв","Баасан","Бямба","Ням",
	};
	public static int getTodayOfWeek() {
		Calendar this_moment = Calendar.getInstance();
    	int today = this_moment.get(Calendar.DAY_OF_WEEK), exact_day = 0;
    	switch(today) {
    		case Calendar.MONDAY: 		{ exact_day = 1; break; }
    		case Calendar.TUESDAY: 		{ exact_day = 2; break; }
    		case Calendar.WEDNESDAY: 	{ exact_day = 3; break; }
    		case Calendar.THURSDAY: 	{ exact_day = 4; break; }
    		case Calendar.FRIDAY: 		{ exact_day = 5; break; }
    		case Calendar.SATURDAY: 	{ exact_day = 6; break; }
    		case Calendar.SUNDAY: 		{ exact_day = 7; break; }
    	}
    	return exact_day;
	}
	public static String getDayName(int day) {
		return dayLocalization[day - 1];
	}
}
