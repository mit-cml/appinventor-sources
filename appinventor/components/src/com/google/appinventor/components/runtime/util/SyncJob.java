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
    private String accountNm = null;
    private String projectID = null;


    public SyncJob(Context context){
        super();
        this.context = context;
    }

    public SyncJob(Context context, String accountNm, String projectID){
        super();
        this.context = context;
        this.accountNm = accountNm;
        this.projectID = projectID;
    }


    /*
    The method syncs data in the SQLite DB with data in CloudDB server.
     */
    @Override
    protected Result onRunJob(Params params){
        Log.d(TAG,"SyncJob started ...");
        CloudDBCacheHelper cloudDBCacheHelper = new CloudDBCacheHelper(this.context);
        final SQLiteDatabase db = cloudDBCacheHelper.getWritableDatabase();
        Log.d(TAG, "db object obtained successfully");

        String[] projection = {CloudDBCache.Table1.COLUMN_NAME_KEY, CloudDBCache.Table1.COLUMN_NAME_VALUE};
        String selection = CloudDBCache.Table1.COLUMN_UPLOAD_FLAG + " = ?";
        String[] selectionArgs = {"0"};

        final Cursor cursor = db.query(CloudDBCache.Table1.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
        if(cursor != null && cursor.getCount() > 0){
            Log.d(TAG, "data from cache available...");
            Jedis jedis = null;
            try{
                jedis = new Jedis("128.52.179.76", 6379);
                jedis.auth("test6789");
                Log.d(TAG, "connected to redis server...");
                while(cursor.moveToNext()){
                    String key = cursor.getString(cursor.getColumnIndex(CloudDBCache.Table1.COLUMN_NAME_KEY));
                    String value = cursor.getString(cursor.getColumnIndex(CloudDBCache.Table1.COLUMN_NAME_VALUE));
                    String status = jedis.set(key,value);
                    Log.d(TAG, "Set Status = " + status);
                    Log.d(TAG, " set (key,value) = " + key + "," + value + " in redis");
                    if(status.equalsIgnoreCase("OK")){
                        Log.d(TAG, "get " + key + " = " + jedis.get(key));
                        /*
                        update the upload flag in the cache
                        */
                        ContentValues values = new ContentValues();
                        values.put(CloudDBCache.Table1.COLUMN_UPLOAD_FLAG,1);
                        String updtSelection = CloudDBCache.Table1.COLUMN_UPLOAD_FLAG + " = ? AND " + CloudDBCache.Table1.COLUMN_NAME_KEY + " = ?";
                        String[] updtSelectionArgs = {"0",key};
                        int rowsUpdt = db.update(CloudDBCache.Table1.TABLE_NAME,values,updtSelection,updtSelectionArgs);
                        if(rowsUpdt > 0) Log.d(TAG, rowsUpdt + " rows updated");
                        else Log.d(TAG, "No rows updated");
                    }
                    else Log.d(TAG, "Value not set in Redis...");

                }

            }
            catch(JedisConnectionException e){
                Log.d(TAG, "jedis connection error in SyncJob...");
                e.printStackTrace();
            }
            catch(Exception e) {
                Log.d(TAG, "error in SyncJob...");
                e.printStackTrace();
            }
            finally {
                if(cursor != null) cursor.close();
                if(db != null) db.close();
                if(jedis != null) jedis.close();
            }


        }
        else{
            Log.d(TAG,"No data to sync...");
        }
        return Result.SUCCESS;
    }

    public static int scheduleSync(){
        Log.d(TAG,"SyncJob scheduleSync called ...");
        int jobId = new JobRequest.Builder(TAG)//.setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                //.setPeriodic(9_00_000L)
                .setExecutionWindow(30_000L, 40_000L)
                //.setRequirementsEnforced(true)
                .build()
                .schedule();
        Log.d(TAG,"SyncJob scheduleSync finished ...");
        return jobId;
    }
}
