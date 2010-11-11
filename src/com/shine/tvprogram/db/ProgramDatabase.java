package com.shine.tvprogram.db;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.shine.tvprogram.DateHelper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

public class ProgramDatabase 
{
	//Энэ классыг статик байх хэрэгтэй юм шиг санагдаад байна.
    private static final String TAG = "DBAdapter";
    private static final String DATABASE_NAME = "tvprogram";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE_CHANNELS = "create table channels (_id integer primary key autoincrement, img_url text null, channel_name text not null);";
    private static final String DATABASE_CREATE_PROGRAMS = "create table programs (_id integer primary key autoincrement, channel_id integer, day integer, program_name text not null, time_to_air text not null);";
    private final Context context; 
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
    private ContentResolver resolver;
    String tag = "ProgramDatabase";
    
    public ProgramDatabase(Context ctx, ContentResolver resolver)
    {
        this.context = ctx;
        this.resolver = resolver;
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
    public ProgramDatabase open() throws SQLException 
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
    public long insertChannel(String name, String img_url) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("channel_name", name);
        initialValues.put("img_url", img_url);
        return db.insert("channels", null, initialValues);
    }
    public boolean deleteChannel(long rowId) 
    {
        return db.delete("channels", "_id = " + rowId, null) > 0;
    }
    public Cursor getAllChannels() 
    {
        return db.query("channels", new String[] {
        		"_id", "channel_name", "img_url",}, null, null, null, null, null);
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
    public Cursor getPrograms(long channel_id) {
    	int this_day = DateHelper.getTodayOfWeek();
    	return getPrograms(channel_id, this_day);
    }
    public Cursor getPrograms(Long channel_id, Integer day) {
    	Cursor mCursor = db.query(true, "programs", new String[]{"_id", "channel_id", "program_name", "time_to_air"}, 
        		"channel_id = ? and day = ?", new String[]{ channel_id.toString(), day.toString()}, null, null, "time_to_air", null);
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
    /*
     */
    public void syncProgram(String programsStr, Handler handler) {
		db.beginTransaction();
    	try {
    		// reset database:
    		Cursor cur = getAllChannels();
    		cur.moveToFirst();
    		while(!cur.isAfterLast()) {
    			String img_uri = cur.getString(cur.getColumnIndex("img_url"));
    			resolver.delete(Uri.parse(img_uri), null, null);
    			cur.moveToNext();
    		}
    		cur.close();
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
    				b.putString("status", "saving");
    				b.putString("channel", channelRow[0]);
    				msg.setData(b);
    				handler.sendMessage(msg);
    				String contentUri = saveImage(channelRow[1]);
    				currentChannelId = insertChannel(channelRow[0], contentUri);
    			}
    		}
    		db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(tag, e.toString());
		} finally {
			db.endTransaction();
		}
    }
    
    public String saveImage(String url) {
    	DefaultHttpClient client = new DefaultHttpClient();
    	try {
			HttpResponse response = client.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			
			Bitmap bm = BitmapFactory.decodeStream(is);
			ContentValues values = new ContentValues(2);
			values.put(Media.DISPLAY_NAME, url);
			values.put(Media.MIME_TYPE, response.getHeaders("Content-Type")[0].getValue());
			Uri uri = resolver.insert(Media.EXTERNAL_CONTENT_URI, values);
			
			OutputStream outStream = resolver.openOutputStream(uri);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.close();

			return uri.toString();
		} catch (Exception e) {
			Log.e(tag, e.toString());
		}
    	return null;
    }
}
