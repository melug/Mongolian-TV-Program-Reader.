package com.shine.tvprogram;

import com.shine.tvprogram.db.ProgramDatabase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
/**
 * Урьдчилж бэлтгэсэн сэрүүлэг нь энэхүү хүлээн авагчийг дуудах болно.
 * Хүлээн авагчийг дуудахдаа түүнд программын дугаарыг дамжуулах
 * хэрэгтэй.
 * @author tulga
 *
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Long programId = intent.getLongExtra("programId", -1);
		ProgramDatabase db = new ProgramDatabase(context);
		db.openAsRead();
		Cursor programCursor = db.getProgram(programId);
		String programName = programCursor.getString(programCursor.getColumnIndex("program_name"));
		Long channelId = programCursor.getLong(programCursor.getColumnIndex("channel_id"));
		Cursor channelCursor = db.getChannel(channelId);
		String channelName = channelCursor.getString(channelCursor.getColumnIndex("channel_name"));
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(R.drawable.icon, programName, when);
		
		CharSequence contentTitle = channelName + " сувгаар.";
		CharSequence contentText = programName;
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, null, 0);
		
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.tickerText = "Дуртай нэвтрүүлэг тань эхэлж байна.";
		mNotificationManager.notify((int)(programId % Integer.MAX_VALUE), notification);
		
		programCursor.close();
		channelCursor.close();
		db.close();
	}
}
