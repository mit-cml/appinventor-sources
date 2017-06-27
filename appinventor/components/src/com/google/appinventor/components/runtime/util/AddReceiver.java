package com.google.appinventor.components.runtime.util;

import android.content.Context;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

/**
 * A broadcast receiver that adds a Job instance to the queue of jobs that need to be scheduled.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
public final class AddReceiver extends JobCreator.AddJobCreatorReceiver {

    @Override
    protected void addJobCreator(Context context, JobManager manager) {
        android.util.Log.d(SyncJob.TAG,"AddReceiver called...");
        manager.addJobCreator(new MyJobCreator());
        android.util.Log.d(SyncJob.TAG,"MyJobCreator added...");
    }
}
