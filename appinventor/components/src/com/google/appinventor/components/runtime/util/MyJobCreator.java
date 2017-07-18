package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.util.Log;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * creates an instance of a Job that needs to be scheduled.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
public class MyJobCreator implements JobCreator {

    private Context context = null;
    private String accountNm = null;
    private String projectID = null;

    public MyJobCreator(Context context){
        this.context = context;
    }

    public MyJobCreator(Context context, String accountNm, String projectID){
        this.context = context;
        this.accountNm = accountNm;
        this.projectID = projectID;

    }

    @Override
    public Job create(String tag){
        Log.d(SyncJob.TAG,"MyJobCreator create called with tag = " + tag);
        switch (tag){
            case SyncJob.TAG:
                Log.d(SyncJob.TAG,"returning instance of SyncJob...");
                return new SyncJob(this.context, this.accountNm, this.projectID);
            default:
                return null;
        }
    }
}
