package com.shine.tvprogram.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.shine.tvprogram.db.ProgramDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ContentUpdater extends Thread {
	
	Handler handler;
	public static final String FEED_URL = "http://tvmongolier.appspot.com/tvfeed/json";
	//Statuses:
	public static final String DOWNLOAD_FINISHED = "finished download";
	public static final String DOWNLOAD_FAILED = "failed to download";
	public static final String DOWNLOAD_PROGRESS = "download progress";
	public static final String SAVING = "saving";
	public static final String SAVED = "saved";
	
	public static final String PROGRESS = "progress";
	String tag = "ProgramDownloader";
	ProgramDatabase db;
	
	public ContentUpdater() {
		
	}
	
	public ContentUpdater(Handler handler, ProgramDatabase db) {
		this.handler = handler;
		this.db = db;
	}
	
	public void run() {
		Message msg = handler.obtainMessage();
		Bundle b = new Bundle();
		try {
			String data = downloadProgram();
			db.syncProgram(data, handler);
			b.putString("status", ContentUpdater.SAVED);
		} catch (Exception e) {
			b.putString("status", ContentUpdater.DOWNLOAD_FAILED);
			b.putString("reason", e.toString());
		}
		msg.setData(b);
		handler.sendMessage(msg);
	}
	
	public String downloadProgram() throws ClientProtocolException, IOException {
		//Download stuffs
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(new HttpGet(ContentUpdater.FEED_URL));
		HttpEntity entity = response.getEntity();
		BufferedReader breader = new BufferedReader(new InputStreamReader(entity.getContent()));
		int MAX_SIZE = 1024, readBytes = 0;
		char[] buffer = new char[MAX_SIZE];
		StringBuffer downloadBuffer = new StringBuffer();
		long currentTime, previousTime = System.currentTimeMillis();
		while ((readBytes = breader.read(buffer, 0, MAX_SIZE)) != -1) {
			downloadBuffer.append(buffer, 0, readBytes);
			currentTime = System.currentTimeMillis();
			if (handler != null && currentTime - previousTime > 500) {
				Message msg = handler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("status", ContentUpdater.DOWNLOAD_PROGRESS);
				int total = downloadBuffer.length() / 1024;
				b.putInt(ContentUpdater.PROGRESS, total);
				msg.setData(b);
				handler.sendMessage(msg);
				previousTime = System.currentTimeMillis();
			}
		}
		return downloadBuffer.toString();
	}
}
