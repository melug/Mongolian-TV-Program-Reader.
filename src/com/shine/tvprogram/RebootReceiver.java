package com.shine.tvprogram;

import com.shine.tvprogram.db.ProgramDatabase;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
/**
 * Бэлтгэгдсэн байсан сэрүүлгүүд нь зөвхөн утас асаалттай үед ачааллагддаг.
 * Утас унтраад асах үед төлөвлөгдсөн байсан сэрүүлгүүд нь
 * санах ойгоос арчигдаж ажиллах боломжгүй болдог. Тиймээс
 * Утас аслаа гэсэн мэдээллийг хүлээн авагч хийж түүн дээрээ
 * сэрүүлэгтэй нэвтрүүлгүүдийн сэрүүлгийг дахин шинэчилж оруулах
 * хэрэгтэй. Дараагийн шинэчлэлт дээр AlarmManager класс
 * зохион байгуулах хэрэгтэй. Тэгэхгүй бол класс бүр тусдаа
 * өөрийн гэсэн сэрүүлэгтэй ажилладаг функцтай, тэр нь ажиллаж
 * байгаа ч гэсэн алдаа гарсан үед зөвхөн 1 хэсэгт л засвар
 * хий
 * @author tulga
 *
 */
public class RebootReceiver extends BroadcastReceiver {
	ProgramDatabase db;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		db = new ProgramDatabase(context);
		db.openAsRead();
		Cursor favouritePrograms = db.getFavouriteProgramsNotReminded();
		
		while(favouritePrograms.isAfterLast()) {
			
			Intent favIntent = new Intent(context, AlarmReceiver.class);
			Long programId = favouritePrograms.getLong(
					favouritePrograms.getColumnIndex("_id"));
		    favIntent.putExtra("programId", programId);

		    PendingIntent alarm = PendingIntent.getBroadcast(context, (int)(programId % Integer.MAX_VALUE), 
				   favIntent, PendingIntent.FLAG_ONE_SHOT);
		   
		    AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		   
		    long rightNow = System.currentTimeMillis();
		    long programTime = DateHelper.getTimeOfProgram(
		    		favouritePrograms.getInt(favouritePrograms.getColumnIndex("day")),
		    		favouritePrograms.getString(favouritePrograms.getColumnIndex("time_to_air"))
		    	);
		   
		    long fromNow = (programTime - rightNow) / (1000 * 60);
		    if (fromNow > 0) {
			   alarmManager.set(AlarmManager.RTC_WAKEUP, programTime, 
					   alarm);
		    }
		    favouritePrograms.moveToNext();
		}
		favouritePrograms.close();
	}
}
