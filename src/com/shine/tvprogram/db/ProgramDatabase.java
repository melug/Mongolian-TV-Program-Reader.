package com.shine.tvprogram.db;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import com.shine.tvprogram.DateHelper;
import com.shine.tvprogram.threads.ContentUpdater;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProgramDatabase 
{
    private static final String TAG = "DBAdapter";
    private static final String DATABASE_NAME = "tvprogram";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE_CHANNELS = "create table channels (_id integer primary key autoincrement, img_data blob null, channel_name text not null);";
    private static final String DATABASE_CREATE_PROGRAMS = "create table programs (_id integer primary key autoincrement, channel_id integer, day integer, program_name text not null, time_to_air text not null, is_reminded integer default 0);";
    private final Context context; 
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    String tag = "ProgramDatabase";
    
    public ProgramDatabase(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	db.execSQL(DATABASE_CREATE_CHANNELS);
        	db.execSQL(DATABASE_CREATE_PROGRAMS);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS channels");
            db.execSQL("DROP TABLE IF EXISTS programs");
            onCreate(db);
        }
    }
    
    public ProgramDatabase openAsWrite() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }
    
    public ProgramDatabase openAsRead() throws SQLException 
    {
        db = DBHelper.getReadableDatabase();
        return this;
    }
    
    public void close() 
    {
        DBHelper.close();
    }
    
    public long insertChannel(String name, byte[] img_data) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("channel_name", name);
        initialValues.put("img_data", img_data);
        return db.insert("channels", null, initialValues);
    }
    
    public boolean deleteChannel(long rowId) 
    {
        return db.delete("channels", "_id = " + rowId, null) > 0;
    }
    
    public Cursor getAllChannels() 
    {
        return db.query("channels", new String[] {
        		"_id", "channel_name", "img_data",}, null, null, null, null, null);
    }
    
    public Cursor getChannel(long rowId) throws SQLException 
    {
        Cursor mCursor = db.query(true, "channels", new String[] { "_id", "channel_name" }, 
             "_id = " + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public boolean updateChannel(long rowId, String channel_name) 
    {
        ContentValues args = new ContentValues();
        args.put("channel_name", channel_name);
        return db.update("channels", args, "_id = " + rowId, null) > 0;
    }
    
    public Cursor getCurrentProgram(Long channelId) {
    	Integer currentDay = DateHelper.getTodayOfWeek();
    	String timeToAir = DateHelper.getCurrentTime();
    	Cursor mCursor = db.query("programs", null, 
    			"time_to_air <= ? AND day = ? AND channel_id = ?", 
    			new String[]{ timeToAir, currentDay.toString(), channelId.toString() }, 
    			null, null, "time_to_air DESC", "1");
    	if(mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    public Cursor getPrograms(long channelId) {
    	int this_day = DateHelper.getTodayOfWeek();
    	return getPrograms(channelId, this_day);
    }
    
    public Cursor getProgram(Long programId) {
    	Cursor mCursor = db.query("programs", null, "_id = ?", new String[] { programId.toString() }
    		, null, null, null, "1");
    	if(mCursor != null) {
    		mCursor.moveToFirst();
    	}
    	return mCursor;
    }
    
    public Cursor getFavouritePrograms() {
    	Cursor mCursor = db.query("programs", null, "is_reminded = ?", new String[] { "1" }
		, null, null, "day, time_to_air ASC");
    	if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
    }
    
    public Cursor getFavouriteProgramsNotReminded() {
    	Integer today = DateHelper.getTodayOfWeek();
    	Cursor mCursor = null;
    	try {
    	mCursor = db.query("programs", null, "is_reminded = ? AND day >= ?", new String[] { "1", today.toString() }
    	, null, null, "day, time_to_air ASC");
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	if(mCursor != null) {
			mCursor.moveToFirst();
		}
    	return mCursor;
    }
    
    public int setReminderOn(Long programId) {
    	return setReminder(programId, true);
    }
    
    public int setReminderOff(Long programId) {
    	return setReminder(programId, false);
    }
    
    private int setReminder(Long programId, boolean onOff) {
    	Integer isReminded = (onOff)? 1: 0;
    	ContentValues updateValues = new ContentValues(1);
    	updateValues.put("is_reminded", isReminded);
    	return db.update("programs", updateValues, " _id = ?", new String[]{ programId.toString() });
    }
    
    public Cursor getPrograms(Long channelId, Integer day) {
    	Cursor mCursor = db.query(true, "programs", new String[]{"_id", "channel_id", "program_name", "time_to_air"}, 
        		"channel_id = ? and day = ?", new String[]{ channelId.toString(), day.toString()}, null, null, "time_to_air", null);
        if(mCursor != null) { 
        	mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    public Cursor searchPrograms(String query) {
    	return searchPrograms(query, 60);
    }
    
    public Cursor searchPrograms(String query, Integer limit) {
    	Integer today = DateHelper.getTodayOfWeek();
    	Cursor cursor = db.query("programs", null, "program_name LIKE ? AND day >= ?", 
    			new String[]{"%" + query + "%", today.toString()}, null, null, "day, time_to_air, channel_id", limit.toString());
    	return cursor;
    }
    
    public long insertProgram(long channel_id, String time_to_air, int day, String program_name) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("channel_id", channel_id);
        initialValues.put("time_to_air", time_to_air);
        initialValues.put("day", day);
        initialValues.put("program_name", program_name);
        return db.insert("programs", null, initialValues);
    }
 
    public void syncProgram(String programsStr, Handler handler) throws Exception {
		db.beginTransaction();
		Exception exceptionOrigin = null;
    	try {
    		// reset database:
    		db.delete("channels", null, null);
    		db.delete("programs", null, null);
    		
    		String lines[] = programsStr.split("\n");
    		String channelRow[], programRow[];
    		long currentChannelId = 0;
    		int currentDay = 0;
    		for(String l : lines) {
    			if(l.startsWith("\t\t")) {
    				programRow = l.substring(2).split("#", 2);
    				insertProgram(currentChannelId, programRow[0], currentDay, programRow[1]);
    			} else if(l.startsWith("\t")) {
    				currentDay = Integer.parseInt(l.substring(1));
    			} else {
    				channelRow = l.split("#");
    				Message msg = handler.obtainMessage();
    				Bundle b = new Bundle();
    				b.putString("status", ContentUpdater.SAVING);
    				b.putString("channel", channelRow[0]);
    				msg.setData(b);
    				handler.sendMessage(msg);
    				//where we want to download it from
    				URL url = new URL(channelRow[1]);  
    				//open the connection
    				URLConnection ucon = url.openConnection();
    				//buffer the download
    				InputStream is = ucon.getInputStream();
    				BufferedInputStream bis = new BufferedInputStream(is, 128);
    				ByteArrayBuffer baf = new ByteArrayBuffer(128);
    				//get the bytes one by one
    				int current = 0;
    				while ((current = bis.read()) != -1) {
    				        baf.append((byte) current);
    				}
    				//store the data as a ByteArray
    				currentChannelId = insertChannel(channelRow[0], baf.toByteArray());
    			}
    		}
    		db.setTransactionSuccessful();
		} catch (Exception e) {
			exceptionOrigin = e;
		} finally {
			db.endTransaction();
		}
		if(exceptionOrigin != null) {
			throw exceptionOrigin;
		}
    }
    
}
