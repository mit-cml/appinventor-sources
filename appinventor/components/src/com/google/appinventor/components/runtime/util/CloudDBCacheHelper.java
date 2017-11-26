package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class for CloudDBCache.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
public class CloudDBCacheHelper extends SQLiteOpenHelper{

    //If you change the Database Schema you must increment the DATABASE_VERSION
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "CloudDBCache.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + CloudDBCache.Table1.TABLE_NAME + " (" +
                    CloudDBCache.Table1._ID + " INTEGER PRIMARY KEY," +
                    CloudDBCache.Table1.COLUMN_NAME_KEY + " TEXT," +
                    CloudDBCache.Table1.COLUMN_NAME_VALUE + " TEXT," +
                    CloudDBCache.Table1.COLUMN_UPLOAD_FLAG + " INTEGER," +
                    CloudDBCache.Table1.COLUMN_TIMESTAMP + " INTEGER)";

    private static final String SQL_CREATE_TABLE_2 =
            "CREATE TABLE " + CloudDBCache.Table2.TABLE_NAME + " (" +
                    CloudDBCache.Table2._ID + " INTEGER PRIMARY KEY," +
                    CloudDBCache.Table2.COLUMN_TOKEN + " TEXT)";

    private static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + CloudDBCache.Table1.TABLE_NAME;

    private static final String SQL_DROP_TABLE_2 =
            "DROP TABLE IF EXISTS " + CloudDBCache.Table2.TABLE_NAME;

    private static CloudDBCacheHelper cloudDBCacheHelper = null;

    public static CloudDBCacheHelper getInstance(Context context){
        if(cloudDBCacheHelper == null){
            cloudDBCacheHelper = new CloudDBCacheHelper(context);
        }
        return cloudDBCacheHelper;
    }

    private CloudDBCacheHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_TABLE);
        db.execSQL(SQL_CREATE_TABLE_2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DROP_TABLE);
        db.execSQL(SQL_DROP_TABLE_2);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

}
