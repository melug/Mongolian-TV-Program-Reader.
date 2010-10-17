package com.shine.tvprogram.db;

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
    private static final String DATABASE_TABLE = "channels";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE = "create table channels (id integer primary key autoincrement, channel_name text not null);" ;
    private final Context context; 
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;
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
            db.execSQL(DATABASE_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS channels");
            onCreate(db);
        }
    }    
    //---opens the database---
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
    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    //---insert a title into the database---
    public long insertChannel(String name) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("channel_name", name);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }
    //---deletes a particular title---
    public boolean deleteChannel(long rowId) 
    {
        return db.delete(DATABASE_TABLE, "id = " + rowId, null) > 0;
    }
    //---retrieves all the titles---
    public Cursor getAllChannels() 
    {
        return db.query(DATABASE_TABLE, new String[] {
        		"id", "channel_name",}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }
    //---retrieves a particular title---
    public Cursor getChannel(long rowId) throws SQLException 
    {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] { "id", "channel_name" }, 
             "id = " + rowId, 
             null,
             null, 
             null, 
             null, 
             null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    //---updates a title---
    public boolean updateChannel(long rowId, String channel_name) 
    {
        ContentValues args = new ContentValues();
        args.put("channel_name", channel_name);
        return db.update(DATABASE_TABLE, args, "id = " + rowId, null) > 0;
    }
}
