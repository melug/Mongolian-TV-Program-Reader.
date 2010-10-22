package com.shine.tvprogram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.shine.tvprogram.db.ProgramDatabase;;

public class ChannelList extends ListActivity {
	
	SimpleCursorAdapter channel_adapter;
	Cursor channels_cursor;
	static String feed_url = "http://tvmongolier.appspot.com/tvfeed/json";
	final int DOWNLOADING_PROGRAM_DIALOG = 1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		updateChannels();
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
				long id) {
				Intent i = new Intent();
				i.setClass(ChannelList.this, ProgramList.class);
				i.putExtra("channel_id", id);
				ChannelList.this.startActivity(i);
			}
		});
	}
	
	public void updateChannels() {
		updateChannels(null, null);
	}
	
	public void updateChannels(String json, Handler mhandler) {
		ProgramDatabase db = new ProgramDatabase(ChannelList.this);
		db.open();
		if (json != null) {
			db.syncProgram(json, mhandler);
		}
		channels_cursor = db.getAllChannels();
		channel_adapter = new SimpleCursorAdapter(this, R.layout.tv_channels, channels_cursor, 
			new String[] {"channel_name"}, new int[] {R.id.channel_name});
		setListAdapter(channel_adapter);
		db.close();
	}
	
	final int DOWNLOAD_PROGRAMS_ITEM = 1,
		ABOUT_ITEM = 2;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, DOWNLOAD_PROGRAMS_ITEM, 0, R.string.download);
		menu.add(0, ABOUT_ITEM, 0, R.string.aboutus);
		return true;
	}
	
	AlertDialog progressDialog;
	Downloader downloadThread;
	DatabaseSync syncThread;
	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				String status = msg.getData().getString("status");
				if(status.equals("finished")) {
					progressDialog.setMessage("Татаж дууслаа. Хадгалж байна.");
					String data = msg.getData().getString("downloadedHtml");
					syncThread = new DatabaseSync(handler, data);
					syncThread.start();
				} else if(status.equals("progress")) {
					progressDialog.setMessage("ТВ програм татах явц : " + msg.getData().getInt("total") + " килобайт");
				} else if (status.equals("saving")) {
					progressDialog.setMessage("\"" + msg.getData().getString("channel") + "\"-ийг хадгалж байна.");
				} else if(status.equals("failed")) {
					dismissDialog(DOWNLOADING_PROGRAM_DIALOG);
					alert("Татаж авалт амжилтгүй боллоо.");
				} else if(status.equals("saved")) {
					dismissDialog(DOWNLOADING_PROGRAM_DIALOG);
					channel_adapter.notifyDataSetChanged();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		};
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
			case DOWNLOAD_PROGRAMS_ITEM: {
				showDialog(DOWNLOADING_PROGRAM_DIALOG);
				downloadThread = new Downloader(handler);
				downloadThread.start();
				break;
			}
			case ABOUT_ITEM: {
				Intent i = new Intent();
				i.setClass(ChannelList.this, About.class);
				ChannelList.this.startActivity(i);
			}
		}
		return true;
	}
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case DOWNLOADING_PROGRAM_DIALOG: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("ТВ програм татах явц : 0 килобайт");
				progressDialog = builder.create();
	            return progressDialog;
			}
			default: {
				
			}
		}
		return null;
	}
	
	class Downloader extends Thread {
		Handler handler;
        Downloader(Handler handler) {
        	this.handler = handler;
        }
       
        public void run() {
			DefaultHttpClient client = new DefaultHttpClient();
			String status = "finished";
			Message endmsg = handler.obtainMessage();
            Bundle endb = new Bundle();
            
			try {
				HttpResponse response = client.execute(new HttpGet(ChannelList.feed_url));
				HttpEntity entity = response.getEntity();
				BufferedReader breader = new BufferedReader(new InputStreamReader(
					entity.getContent()));

				int MAX_SIZE = 1024, readBytes = 0;
				char[] buffer = new char[MAX_SIZE];
				StringBuffer downloadBuffer = new StringBuffer();
				long currentTime, previousTime = System.currentTimeMillis();
				while((readBytes = breader.read(buffer, 0, MAX_SIZE))!= -1) {
					downloadBuffer.append(buffer, 0, readBytes);
					currentTime = System.currentTimeMillis();
					if (currentTime - previousTime > 500) {
						Message msg = handler.obtainMessage();
			            Bundle b = new Bundle();
			            b.putString("status", "progress");
			            int total = downloadBuffer.length() / 1024;
			            b.putInt("total", total);
			            msg.setData(b);
			            handler.sendMessage(msg);
			            previousTime = System.currentTimeMillis();
					}
				}
				endb.putString("downloadedHtml", downloadBuffer.toString());
			} catch (ClientProtocolException e) {
				status = "failed";
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				status = "failed";
			} catch(Exception e) {
				e.printStackTrace();
			}
            endb.putString("status", status);
            endmsg.setData(endb);
            handler.sendMessage(endmsg);
        }
	}
	
	class DatabaseSync extends Thread {
		Handler handler;
		String data;
        DatabaseSync(Handler handler, String data) {
        	this.handler = handler;
        	this.data = data;
        }
       
        public void run() {
			String status = "saved";
			Message endmsg = handler.obtainMessage();
            Bundle endb = new Bundle();
			try {
				updateChannels(data, handler);
			} catch(Exception e) {
				e.printStackTrace();
			}
            endb.putString("status", status);
            endmsg.setData(endb);
            handler.sendMessage(endmsg);
        }
	}


	public void alert(String alertText) {
		Toast.makeText(ChannelList.this, alertText, Toast.LENGTH_SHORT).show();
	}
}

