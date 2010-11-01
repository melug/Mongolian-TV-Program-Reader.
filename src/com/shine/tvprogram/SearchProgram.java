package com.shine.tvprogram;

import com.shine.tvprogram.db.ProgramDatabase;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class SearchProgram extends ListActivity {
	
	ProgramDatabase db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new ProgramDatabase(SearchProgram.this, getContentResolver());
		db.open();
	    Intent intent = getIntent();
	    
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      try {
	    	  doSearch(query);
	      } catch(Exception e) {
	    	  e.printStackTrace();
	      }
	    }
	}
	
	protected void doSearch(String query) {
		Cursor programs = db.searchPrograms(query);
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(SearchProgram.this, R.layout.search_entry
				, programs, 
				new String[]{"time_to_air", "program_name", "day", "channel_id"}, 
				new int[]{R.id.entry_time, R.id.entry_program, R.id.entry_day, R.id.entry_channel});
		cursorAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				Integer channelColumnIndex = cursor.getColumnIndex("channel_id");
				if (channelColumnIndex == columnIndex) {
					Long channelId = cursor.getLong(channelColumnIndex);
					Cursor channelCursor = db.getChannel(channelId);
					channelCursor.moveToFirst();
					String channelName = channelCursor.getString(
							channelCursor.getColumnIndex("channel_name"));
					TextView tv = (TextView)view;
					tv.setText(channelName);
					channelCursor.close();
					return true;
				}
				Integer dayColumnIndex = cursor.getColumnIndex("day");
				if (dayColumnIndex == columnIndex) {
					TextView tv = (TextView)view;
					Integer day = cursor.getInt(dayColumnIndex);
					tv.setText(DateHelper.getDayName(day) + ", ");
					return true;
				}
				return false;
			}
		});
		setListAdapter(cursorAdapter);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		db.close();
		super.onDestroy();
	}
}
