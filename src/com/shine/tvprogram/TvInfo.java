package com.shine.tvprogram;

import com.shine.tvprogram.db.ProgramDatabase;
import com.shine.tvprogram.threads.DatabaseSync;
import com.shine.tvprogram.threads.ProgramDownloader;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;
import android.util.Log;

public class TvInfo extends Activity {
	SimpleCursorTreeAdapter programListCursorAdapter;
	ExpandableListView expandableView;
	ProgramDownloader downloaderTask;
	ProgramDatabase db;
	Cursor allChannelsCursor;
	final String tag = "TvInfo";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		db = new ProgramDatabase(TvInfo.this, getContentResolver());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tv_info);
		db.openAsRead();
		selected_day = DateHelper.getTodayOfWeek();
		expandableView = (ExpandableListView) findViewById(R.id.expandable_view);
		updatePrograms();
	}
	
	int selected_day;
	
	void updatePrograms() {
		try {
			if(allChannelsCursor != null) {
				allChannelsCursor.close();
			}
			allChannelsCursor = db.getAllChannels();
			programListCursorAdapter = new SimpleCursorTreeAdapter(
			TvInfo.this, allChannelsCursor, 
			R.layout.channels_for_expandable, 
			new String[] {"channel_name", "img_url"}, new int[] {R.id.channel_name, R.id.channel_icon}, 
			R.layout.programs_for_expandable, 
			new String[] {"program_name", "time_to_air"}, new int[] {R.id.program_name, R.id.time}) 
			{
				@Override
				protected Cursor getChildrenCursor(Cursor arg0) {
					long channel_id = arg0.getLong(0);
					return db.getPrograms(channel_id, selected_day);
				}
			
				@Override
				protected void setViewImage(ImageView v, String value) {
					v.setImageURI(Uri.parse(value));
				}
			};
			expandableView.setAdapter(programListCursorAdapter);
			//allChannels.close();
		} catch(Exception e) {
			e.printStackTrace();
			Log.e(tag, e.toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.programs_menu, menu);
	    return true;
	}
	
	final int DOWNLOAD_PROGRESS_DIALOG = 0;
	ProgressDialog downloadProgress;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id) {
			case DOWNLOAD_PROGRESS_DIALOG: {
				downloadProgress = new ProgressDialog(TvInfo.this);
				downloadProgress.setMessage("Татаж байна.");
				return downloadProgress;
			}
		}
		return super.onCreateDialog(id);
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
			case R.id.download_program: {
				showDialog(DOWNLOAD_PROGRESS_DIALOG);
				downloaderTask = new ProgramDownloader(handler);
				downloaderTask.start();
				break;
			}
			case R.id.search_program: {
				onSearchRequested();
				break;
			}
		}
		if(day != -1) {
			selected_day = day;
			updatePrograms();
		}
		return true;
	}

	final Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			Bundle b = msg.getData();
			String status = b.getString("status");
			if(ProgramDownloader.DOWNLOAD_FINISHED.equals(status)) {
				String data = b.getString(ProgramDownloader.DATA);
				downloadProgress.setMessage("Татаж дууслаа, одоо хадгалж байна.");
				DatabaseSync ds = new DatabaseSync(handler, data, db);
				ds.start();
			} else if(ProgramDownloader.DOWNLOAD_FAILED.equals(status)) {
				dismissDialog(DOWNLOAD_PROGRESS_DIALOG);
				alert("Амжилтгүй боллоо.");
			} else if(ProgramDownloader.DOWNLOAD_PROGRESS.equals(status)) {
				int progress = b.getInt(ProgramDownloader.PROGRESS);
				downloadProgress.setMessage("Татаж байна. " + progress + "кб");
			} else if("saving".equals(status)) {
				String channel = b.getString("channel");
				downloadProgress.setMessage(channel + "-ийг татаж байна");
			} else if("saved".equals(status)) {
				dismissDialog(DOWNLOAD_PROGRESS_DIALOG);
				updatePrograms();
			}
		};
	};
	
	public void alert(String alertText) {
		Toast.makeText(TvInfo.this, alertText, Toast.LENGTH_SHORT).show();
	}
}
