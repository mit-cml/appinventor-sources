package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

/**
 * Created by Joy on 6/21/17.
 */
public class SyncJob extends Job {

    public static final String TAG = "SyncJob";

    @Override
    protected Result onRunJob(Params params){
        Log.d(TAG,"SyncJob is running ...");
        return Result.SUCCESS;
    }

    public static void scheduleSync(){
        Log.d(TAG,"SyncJob scheduleSync called ...");
        new JobRequest.Builder(TAG).setExecutionWindow(30_000L, 40_000L)
                .build()
                .schedule();
        Log.d(TAG,"SyncJob scheduleSync finished ...");
    }
}
