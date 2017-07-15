package edu.mit.appinventor;

import android.provider.BaseColumns;

/**
 * This class defines the schema for a SQLLite database that acts as the local cache for CloudDB.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */

public final class CloudDBCache {

    private CloudDBCache(){}

    /*Inner class that defines the table contents. Implementing BaseColumns gives us a promary field called _ID*/
    public static class Table1 implements BaseColumns{
        public static String TABLE_NAME = "table1";
        public static String COLUMN_NAME_KEY = "key";
        public static String COLUMN_NAME_VALUE = "value";
        public static String COLUMN_UPLOAD_FLAG = "uploadFlag";
    }
}
