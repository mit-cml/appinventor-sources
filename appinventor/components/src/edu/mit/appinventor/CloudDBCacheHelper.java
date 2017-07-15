package edu.mit.appinventor;

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
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CloudDBCache.db";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + CloudDBCache.Table1.TABLE_NAME + " (" +
                    CloudDBCache.Table1._ID + " INTEGER PRIMARY KEY," +
                    CloudDBCache.Table1.COLUMN_NAME_KEY + " TEXT," +
                    CloudDBCache.Table1.COLUMN_NAME_VALUE + " TEXT," +
                    CloudDBCache.Table1.COLUMN_UPLOAD_FLAG + " INTEGER)";

    private static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + CloudDBCache.Table1.TABLE_NAME;

    public CloudDBCacheHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

}
