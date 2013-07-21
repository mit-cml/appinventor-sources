package com.google.appinventor.components.runtime.util;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class MyGCMBroadcastReceiver extends GCMBroadcastReceiver
{
    @Override
    protected String getGCMIntentServiceClassName(Context context)
    {
        return GCMIntentService.class.getName(); // Don't hard-code like "com.example.oldpackage.MyIntentService", see http://stackoverflow.com/a/936696/1402846
    }
}