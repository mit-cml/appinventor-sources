package com.google.appinventor.components.runtime.util;

import android.util.Log;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * creates an instance of a Job that needs to be scheduled.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
public class MyJobCreator implements JobCreator {

    @Override
    public Job create(String tag){
        Log.d(SyncJob.TAG,"MyJobCreator create called with tag = " + tag);
        switch (tag){
            case SyncJob.TAG:
                Log.d(SyncJob.TAG,"returning instance of SyncJob...");
                return new SyncJob();
            default:
                return null;
        }
    }
}
