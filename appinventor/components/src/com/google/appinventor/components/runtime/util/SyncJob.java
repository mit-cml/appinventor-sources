package com.google.appinventor.components.runtime.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import edu.mit.appinventor.CloudDBCache;
import edu.mit.appinventor.CloudDBCacheHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * The class manages the job that needs to be scheduled
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
public class SyncJob extends Job {

    public static final String TAG = "SyncJob";
    private Context context = null;


    public SyncJob(Context context){
        super();
        this.context = context;
    }
    /*
    The method syncs data in the SQLite DB with data in CloudDB server.
     */
    @Override
    protected Result onRunJob(Params params){
        Log.d(TAG,"SyncJob started ...");
        CloudDBCacheHelper cloudDBCacheHelper = new CloudDBCacheHelper(this.context);
        SQLiteDatabase db = cloudDBCacheHelper.getWritableDatabase();

        String[] projection = {CloudDBCache.Table1.COLUMN_NAME_KEY, CloudDBCache.Table1.COLUMN_NAME_VALUE};
        String selection = CloudDBCache.Table1.COLUMN_UPLOAD_FLAG + " = ?";
        String[] selectionArgs = {"0"};

        Cursor cursor = db.query(CloudDBCache.Table1.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
        if(cursor != null && cursor.getCount() > 0){
            try{
                Jedis jedis = new Jedis("128.52.179.76", 6379);
                jedis.auth("test6789");
                while(cursor.moveToNext()){
                    String key = cursor.getString(1);
                    String value = cursor.getString(2);
                    jedis.set(key,value);
                }
                /*
                update the upload flag in the cache
                 */
                ContentValues values = new ContentValues();
                values.put(CloudDBCache.Table1.COLUMN_UPLOAD_FLAG,1);
                String updtSelection = CloudDBCache.Table1.COLUMN_UPLOAD_FLAG + " = ?";
                String[] updtSelectionArgs = {"0"};
                db.update(CloudDBCache.Table1.TABLE_NAME,values,updtSelection,updtSelectionArgs);
            }
            catch(JedisConnectionException e){
                Log.d(TAG, "jedis connection error in SyncJob...");
                e.printStackTrace();
            }
            catch(Exception e) {
                Log.d(TAG, "error in SyncJob...");
                e.printStackTrace();
            }

        }
        else{
            Log.d(TAG,"No data to sync...");
        }
        return Result.SUCCESS;
    }

    public static int scheduleSync(){
        Log.d(TAG,"SyncJob scheduleSync called ...");
        int jobId = new JobRequest.Builder(TAG).setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                //.setPeriodic(7_200_000L)
                //.setExecutionWindow(30_000L, 40_000L)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
        Log.d(TAG,"SyncJob scheduleSync finished ...");
        return jobId;
    }
}
