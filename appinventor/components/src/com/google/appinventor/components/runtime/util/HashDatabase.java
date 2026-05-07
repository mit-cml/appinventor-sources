// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

import com.google.appinventor.components.runtime.util.HashDbInitialize.HashTable;

import java.io.File;

public final class HashDatabase extends SQLiteOpenHelper {

  private static class ExternalContext extends ContextWrapper {
    Context base;

    public ExternalContext(Context base) {
      super(base);
      this.base = base;
    }

    @Override
    public File getDatabasePath(String name) {
      String dir = QUtil.getReplDatabasePath(base, true);
      String dbfile = dir + File.separator + name;
      if (!dbfile.endsWith(".db")) {
        dbfile += ".db";
      }
      File result = new File(dbfile);
      if (!result.getParentFile().exists()) {
        result.getParentFile().mkdirs();
      }
      return result;

    }
  }

  private static final String TABLE_NAME = HashTable.TABLE_NAME ;
  private static final String KEY_NAME = HashTable.COLUMN_1_NAME;
  private static final String KEY_HASH = HashTable.COLUMN_2_NAME;
  private static final String KEY_TIMESTAMP = HashTable.COLUMN_3_NAME;
  private static final String[] COLUMNS = { KEY_NAME, KEY_HASH, KEY_TIMESTAMP };

  private static final String SQL_CREATE_ENTRIES =
    "CREATE TABLE " + TABLE_NAME + " (" +
    KEY_NAME + " TEXT," +
    KEY_HASH + " TEXT," +
    KEY_TIMESTAMP + " TEXT)";

  private static final String SQL_DELETE_ENTRIES =
    "DROP TABLE IF EXISTS " + TABLE_NAME;

  public static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "hashTable.db";

  public HashDatabase(Context context) {
    super(new ExternalContext(context), DATABASE_NAME, null, DATABASE_VERSION);
  }
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_ENTRIES);
  }

  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // migrate the data to the new table and then delete the old data
    db.execSQL(SQL_DELETE_ENTRIES);
    onCreate(db);
  }

  public void deleteOne(HashFile hashFile) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.delete(TABLE_NAME, "fileName = ?", new String[] { hashFile.getFileName() });
    db.close();
  }

  public HashFile getHashFile(String fileName) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.query(TABLE_NAME, // a. table
      COLUMNS, // b. column names
      " fileName = ?", // c. selections
      new String[] { fileName }, // d. selections args
      null, // e. group by
      null, // f. having
      null, // g. order by
      null); // h. limit
    Log.d("Database",cursor.toString());
    if (cursor == null  || cursor.getCount()<1) {
      cursor.close();
      db.close();
      return null;
    }
    if (cursor != null)
      cursor.moveToFirst();

    HashFile hashFile = new HashFile();
    hashFile.setFileName(cursor.getString(0));
    hashFile.setHash(cursor.getString(1));
    hashFile.setTimestamp(cursor.getString(2));
    cursor.close();
    db.close();
    return hashFile;
  }

  public void insertHashFile(HashFile hashFile) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(KEY_NAME, hashFile.getFileName());
    values.put(KEY_HASH, hashFile.getHash());
    values.put(KEY_TIMESTAMP, hashFile.getTimestamp());
    db.insert(TABLE_NAME, null, values);
    db.close();
  }

  public int updateHashFile(HashFile hashFile) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(KEY_NAME, hashFile.getFileName());
    values.put(KEY_HASH, hashFile.getHash());
    values.put(KEY_TIMESTAMP, hashFile.getTimestamp());

    int i = db.update(TABLE_NAME, // table
      values, // column/value
      "fileName = ?", // selections
      new String[] { hashFile.getFileName() });
    db.close();
    return i;
  }
}
