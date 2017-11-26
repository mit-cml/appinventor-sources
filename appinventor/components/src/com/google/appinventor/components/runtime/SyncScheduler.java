package com.google.appinventor.components.runtime;


import android.content.Intent;
import android.util.Log;
import com.evernote.android.job.JobManager;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.content.Context;
import com.google.appinventor.components.runtime.util.MyJobCreator;
import com.google.appinventor.components.runtime.util.SyncJob;

/**
 * The component helps sync local data with a remote server.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
@DesignerComponent(version = YaVersion.SYNC_SCHEDULER_COMPONENT_VERSION,
        description = "SyncScheduler is a NonVisibleComponent that allows apps to sync data stored locally with a remote server",
        category = ComponentCategory.STORAGE,
        nonVisible = true,
        iconName = "images/image.png")

@SimpleObject
@UsesBroadcastReceivers(receivers = {
        /*@ReceiverElement(name = "com.google.appinventor.components.runtime.util.AddReceiver",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "com.evernote.android.job.ADD_JOB_CREATOR")})
                },
                exported = "false"),*/
        @ReceiverElement(name = "com.evernote.android.job.v14.PlatformAlarmReceiver",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "com.evernote.android.job.v14.RUN_JOB"),
                                @ActionElement(name = "net.vrallev.android.job.v14.RUN_JOB")})
                },
                exported = "false"),
        @ReceiverElement(name = "com.evernote.android.job.JobBootReceiver",
                intentFilters = {
                        @IntentFilterElement(actionElements = {
                                @ActionElement(name = "android.intent.action.BOOT_COMPLETED"),
                                @ActionElement(name = "android.intent.action.QUICKBOOT_POWERON"),
                                @ActionElement(name = "com.htc.intent.action.QUICKBOOT_POWERON"),
                                @ActionElement(name = "android.intent.action.MY_PACKAGE_REPLACED")})
                },
                exported = "false")
})
@UsesLibraries(libraries = "android-job.jar,catlog.jar,android-support-v4.jar,android-support-annotations.jar")
@UsesPermissions(permissionNames = "android.permission.BIND_JOB_SERVICE,android.permission.WAKE_LOCK,android.permission.ACCESS_NETWORK_STATE,android.permission.RECEIVE_BOOT_COMPLETED")
public class SyncScheduler extends AndroidNonvisibleComponent implements Component{

    private Context context;

    public SyncScheduler(Form form){
        super(form);
    }

    @SimpleFunction(description = "schedule a sync")
    public void scheduleSync(){
        Log.d(SyncJob.TAG,"begin schedule...");
        SyncJob.scheduleSync();
        Log.d(SyncJob.TAG,"finish schedule...");
    }
}
