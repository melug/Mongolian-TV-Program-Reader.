package com.shine.tvprogram.threads;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ProgramDownloader extends Thread {
	Handler handler;
	public static final String FEED_URL = "http://tvmongolier.appspot.com/tvfeed/json";
	public static final String DOWNLOAD_FINISHED = "finished download";
	public static final String DOWNLOAD_FAILED = "failed to download";
	public static final String DOWNLOAD_PROGRESS = "download progress";
	public static final String DATA = "downloaded data";
	public static final String PROGRESS = "total size";
	String tag = "ProgramDownloader";
	

	public ProgramDownloader(Handler handler) {
		this.handler = handler;
	}

	public void run() {
		DefaultHttpClient client = new DefaultHttpClient();
		String status = ProgramDownloader.DOWNLOAD_FINISHED;
		Message endmsg = handler.obtainMessage();
		Bundle endb = new Bundle();
		try {
			HttpResponse response = client.execute(new HttpGet(
					ProgramDownloader.FEED_URL));
			HttpEntity entity = response.getEntity();
			BufferedReader breader = new BufferedReader(new InputStreamReader(
					entity.getContent()));
			int MAX_SIZE = 1024, readBytes = 0;
			char[] buffer = new char[MAX_SIZE];
			StringBuffer downloadBuffer = new StringBuffer();
			long currentTime, previousTime = System.currentTimeMillis();
			while ((readBytes = breader.read(buffer, 0, MAX_SIZE)) != -1) {
				downloadBuffer.append(buffer, 0, readBytes);
				currentTime = System.currentTimeMillis();
				if (currentTime - previousTime > 500) {
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					b.putString("status", ProgramDownloader.DOWNLOAD_PROGRESS);
					int total = downloadBuffer.length() / 1024;
					b.putInt(ProgramDownloader.PROGRESS, total);
					msg.setData(b);
					handler.sendMessage(msg);
					previousTime = System.currentTimeMillis();
				}
			}
			endb.putString(ProgramDownloader.DATA, downloadBuffer.toString());
		} catch (Exception e) {
			status = ProgramDownloader.DOWNLOAD_FAILED;
		}
		endb.putString("status", status);
		endmsg.setData(endb);
		handler.sendMessage(endmsg);
	}
}
