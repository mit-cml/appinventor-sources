package com.google.appinventor.components.runtime.util;

import android.provider.BaseColumns;
//We want to implement a database like this
// two columns: project id and hashfile. We will not have any primary key since one project id can map to multiple files
//the way we want to key all files needed for one projectid is
//like: select * from hashTable where projectid = currentProjectId
public class HashDbInitialize {
    private HashDbInitialize() {}

    /* Inner class that defines the table contents */
    public static class hashTable implements BaseColumns {
        public static final String TABLE_NAME = "HashDatabase";
        public static final String COLUMN_1_NAME = "projectId";
        public static final String COLUMN_2_NAME = "hashFile";
    }

}
