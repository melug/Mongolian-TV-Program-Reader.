package com.shine.tvprogram;

import com.shine.tvprogram.db.ProgramDatabase;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

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
				, programs, new String[]{"time_to_air", "program_name"}, new int[]{R.id.entry_time, R.id.entry_program});
		setListAdapter(cursorAdapter);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		db.close();
		super.onDestroy();
	}
}
