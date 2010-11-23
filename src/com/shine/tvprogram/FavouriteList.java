package com.shine.tvprogram;

import com.shine.tvprogram.db.ProgramDatabase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Би таны дуртай нэвтрүүлгүүдийн жагсаалтыг үзүүлнэ.
 * Тэр дундаас хэрэггүй болсноо хасаарай :)
 * @author tulga
 *
 */
public class FavouriteList extends Activity {
	ListView favList;
	ProgramDatabase db;
	SimpleCursorAdapter adapter;
	Long selectedProgramId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.favourite_programs);
			db = new ProgramDatabase(this);
			db.openAsRead();
			favList = (ListView)findViewById(R.id.fav_list);
			fillPrograms();
		} catch(Exception e) {
			alert(e.toString() + "\n" + e.getMessage());
		}
	}

	protected void fillPrograms() {
		Cursor cursor = db.getFavouritePrograms();
		adapter = new SimpleCursorAdapter(this, R.layout.favourite_entry, cursor
				, new String[] { "program_name", "time_to_air", "channel_id", "day" }
				, new int[] { R.id.fav_program_name, R.id.fav_time, R.id.fav_channel, R.id.fav_day });
		adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if(cursor.getColumnIndex("channel_id") == columnIndex) {
					Long channelId = cursor.getLong(columnIndex);
					Cursor channelCursor = db.getChannel(channelId);
					channelCursor.moveToFirst();
					TextView tv = (TextView)view;
					String channelName = channelCursor.getString(
							channelCursor.getColumnIndex("channel_name"));
					tv.setText(channelName);
					channelCursor.close();
					return true;
				} else if(cursor.getColumnIndex("day") == columnIndex) {
					Integer day = cursor.getInt(columnIndex);
					TextView tv = (TextView)view;
					tv.setText(DateHelper.getDayName(day));
					return true;
				}
				return false;
			}
		});
		favList.setAdapter(adapter);
		favList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				showDialog(REMOVE_DIALOG);
				selectedProgramId = id;
			}
		});
	}
	static final int REMOVE_DIALOG = 1;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case REMOVE_DIALOG: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Энэ нэвтрүүлгийг санамжаас хасах уу?")
			       .setPositiveButton("Тийм", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   db.setReminderOff(selectedProgramId);
			        	   fillPrograms();
			           }
			       })
			       .setNegativeButton("Үгүй", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       });
				AlertDialog alert = builder.create();
				return alert;
			}
		}
		return super.onCreateDialog(id);
	}
	
	public void alert(String alertText) {
		Toast.makeText(FavouriteList.this, alertText, Toast.LENGTH_LONG).show();
	}
}
