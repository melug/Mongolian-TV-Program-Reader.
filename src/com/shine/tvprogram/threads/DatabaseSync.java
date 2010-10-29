package com.shine.tvprogram.threads;

import com.shine.tvprogram.db.ProgramDatabase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DatabaseSync extends Thread {
	Handler handler;
	String data;
	ProgramDatabase db;
	String tag = "DatabaseSync";
	
	public DatabaseSync(Handler handler, String data, ProgramDatabase db) {
    	this.handler = handler;
    	this.data = data;
    	this.db = db;
    }
   
    public void run() {
		String status = "saved";
		Message endmsg = handler.obtainMessage();
        Bundle endb = new Bundle();
		try {
			db.syncProgram(data, handler);
		} catch(Exception e) {
			Log.e(tag, e.toString());
			e.printStackTrace();
		}
        endb.putString("status", status);
        endmsg.setData(endb);
        handler.sendMessage(endmsg);
    }
    
}
