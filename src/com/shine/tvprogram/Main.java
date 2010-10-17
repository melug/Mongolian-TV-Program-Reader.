package com.shine.tvprogram;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.shine.tvprogram.db.ProgramDatabase;;

public class Main extends ListActivity {
    /** Called when the activity is first created. */
	String[] TVChannels = new String[] {
		"МҮОНРТ", "UBS", "MN25", "TV5", "TV9", "C1", "NTV",
		"TV8", "MGTV", "TM", "SBN", "Боловсрол", "EagleTV",
		"BTV", "SCH",
	  };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ProgramDatabase db = new ProgramDatabase(Main.this);
		db.open();
		Cursor channels_cursor = db.getAllChannels();
		TVChannels = new String[channels_cursor.getCount()];
		for(int i=0;i<channels_cursor.getCount();i++) {
			channels_cursor.moveToNext();
			TVChannels[i] = channels_cursor.getString(1);
		}
		db.close();
		

		setListAdapter(new ArrayAdapter<String>(this, R.layout.tv_item, TVChannels));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
				Main.this.startActivity(new Intent(Main.this, ProgramList.class));
			}
		});
	}
}