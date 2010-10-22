package com.shine.tvprogram;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import com.shine.tvprogram.db.ProgramDatabase;

public class ProgramList extends ListActivity {
	/** Called when the activity is first created. */
	public long channel_id;
	public int selected_day;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		channel_id = getIntent().getLongExtra("channel_id", -1);
		selected_day = DateHelper.getTodayOfWeek();
		fillWithPrograms();
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.programs_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int day = -1;
		switch(item.getItemId()) {
			case R.id.monday: {
				alert("Даваа");
				day = 1;
				break;
			}
			case R.id.tuesday: {
				day = 2;
				alert("Мягмар");
				break;
			}
			case R.id.wednesday: {
				day = 3;
				alert("Лхагва");
				break;
			}
			case R.id.thursday: {
				day = 4;
				alert("Пүрэв");
				break;
			}
			case R.id.friday: {
				day = 5;
				alert("Баасан");
				break;
			}
			case R.id.saturday: {
				day = 6;
				alert("Бямба");
				break;
			}
			case R.id.sunday: {
				day = 7;
				alert("Ням");
				break;
			}
		}
		if(day != -1) {
			fillWithPrograms(day);
		}
		return true;
	}
	
	public void fillWithPrograms() {
		fillWithPrograms(DateHelper.getTodayOfWeek());
	}
	
	public void alert(String alertText) {
		Toast.makeText(ProgramList.this, alertText, Toast.LENGTH_SHORT).show();
	}
	
	public void fillWithPrograms(int day) {
		ProgramDatabase db = new ProgramDatabase(ProgramList.this);
		db.openAsRead();
		Cursor tvprograms = db.getPrograms(channel_id, day);
		SimpleCursorAdapter cursor = new SimpleCursorAdapter(ProgramList.this, R.layout.tv_program, 
				tvprograms,	new String[] { "program_name", "time_to_air" }, 
				new int[] { R.id.program_name, R.id.time_to_air });
		setListAdapter(cursor);
		cursor.notifyDataSetChanged();
		db.close();
	}
}
