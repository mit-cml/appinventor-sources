package com.google.appinventor.components.runtime.util;

import android.provider.BaseColumns;

public class HashDbInitialize {
    private HashDbInitialize() {}

    /**
     * Inner class that defines the table contents
     */
    public static class hashTable implements BaseColumns {
        public static final String TABLE_NAME = "HashDatabase";
        public static final String COLUMN_1_NAME = "fileName";
        public static final String COLUMN_2_NAME = "hashFile";
        public static final String COLUMN_3_NAME = "timeStamp";
    }
}
