package com.google.appinventor.components.runtime.util;
import android.content.Context;    //is this the right class to import?
import android.database.sqlite.*;

//more questions: is this the right way (2 cols) to implement our database?

public final class HashDatabase extends SQLiteOpenHelper{

    private static HashDbInitialize.hashTable hashTable;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + hashTable.TABLE_NAME + " (" +
                    hashTable.COLUMN_1_NAME + " INTEGER," +
                    hashTable.COLUMN_2_NAME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + hashTable.TABLE_NAME;

    public static final int DATABASE_VERSION = 1;  //what version should we use?
    public static final String DATABASE_NAME = "hashTable.db";

    public HashDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
