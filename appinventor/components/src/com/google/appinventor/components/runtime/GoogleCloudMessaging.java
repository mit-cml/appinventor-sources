// @author pablomorpheo@gmail.com (Pablo García)

package com.google.appinventor.components.runtime;





/*
import static com.google.appinventor.components.runtime.util.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.google.appinventor.components.runtime.util.CommonUtilities.EXTRA_MESSAGE;
import static com.google.appinventor.components.runtime.util.CommonUtilities.SENDER_ID;
import static com.google.appinventor.components.runtime.util.CommonUtilities.SERVER_URL;
*/
import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import android.app.Notification;
import android.app.NotificationManager;


import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import java.io.File; 
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import java.io.BufferedReader;
import android.widget.EditText;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import java.util.zip.*;
import java.io.*;

import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.app.Activity;

import android.content.SharedPreferences;
import android.util.Log;

@DesignerComponent(version = YaVersion.NOTIFIER_COMPONENT_VERSION,
    category = ComponentCategory.MISC,
    description = "Google Cloud Messaging",
    nonVisible = true,
    iconName = "images/GoogleCloudMessaging.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.GET_ACCOUNTS, " +
					"android.permission.WAKE_LOCK, com.google.android.c2dm.permission.RECEIVE")
@UsesLibraries(libraries = "gcm.jar")
public final class GoogleCloudMessaging extends AndroidNonvisibleComponent implements Component, OnResumeListener, OnPauseListener, OnInitializeListener, OnStopListener {

  private final Activity activity;
  private final Handler handler;
  
  public static final String TAG = "GCM Component";
 
	
  private String apiProjectNumber;
  private boolean isInitialized;
  private static boolean isRunning;
  
  private static final String PREF_FILE = "GCMState";    // State of GCM component
  private static final String PREF_NENABLED = "nenabled";   // Boolean flag for GV is enabled
  private static final String CACHE_FILE = "gcmcachedmsg";
  private static int messagesCached;
  private static Object cacheLock = new Object();
  
  
/**
   * Creates a new GoogleCloudMessaging component.
   *
   * @param container the enclosing component
   */
  public GoogleCloudMessaging (ComponentContainer container) {
    super(container.$form());
	Log.d(TAG, "GCM constructor");
    activity = container.$context();
    handler = new Handler();
	
	
	SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
	if (prefs != null) {
		notificationsEnabled = prefs.getBoolean(PREF_NENABLED, false);
	} else {
		notificationsEnabled = true;
	}
	
	container.$form().registerForOnInitialize(this);
    container.$form().registerForOnResume(this);
    container.$form().registerForOnPause(this);  
    container.$form().registerForOnStop(this);
  }
  
  /**
   * Callback from Form. No incoming messages can be processed through
   * MessageReceived until the Form is initialized. Messages are cached
   * until this method is called.
   */
  @Override
  public void onInitialize() {
    Log.i(TAG, "onInitialize()");
    isInitialized = true;
    isRunning = true;    // Added b/c REPL does not call onResume when starting Texting component
    //processCachedMessages();
    //NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
    //nm.cancel(SmsBroadcastReceiver.NOTIFICATION_ID);
  }
  
  
  
  
  /**
   * Processes cached messages if the app is initialized
   */
  @Override
  public void onResume() {
    Log.i(TAG, "onResume()");
    isRunning = true;
    if (isInitialized) {
      //processCachedMessages();
      //NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
      //nm.cancel(SmsBroadcastReceiver.NOTIFICATION_ID);
    }
  }

  /**
   * Messages received while paused will be cached
   */
  @Override
  public void onPause() {
    Log.i(TAG, "onPause()");
    isRunning = false;
  }
  
  /**
   * Save the component's state in shared preference file before it is killed.
   */
  @Override
  public void onStop() {
    Log.i(TAG, "onStop()");
  }
  
  
  
  
  
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "Goto https://code.google.com/apis/console/ to obtain one (not the API Key)")
  public void APIProjectNumber(String api) {
    this.apiProjectNumber = api;
  }
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "Goto https://code.google.com/apis/console/ to obtain one (not the API Key)")
  public String APIProjectNumber() {
    return apiProjectNumber;
  }
  
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
	  description = "Enable or disable notifications")
  public boolean NotificationsEnabled() {
    return notificationsEnabled;
  }

  /**
   * If this property is true, then SendMessage will attempt to send messages over
   * WiFi, using Google voice.
   *
   * @param enabled  Set to 'true' or 'false' depending on whether you want to
   *  use Google Voice to send/receive messages.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty()
  public void NotificationsEnabled(boolean enabled) {

      this.notificationsEnabled = enabled;
      SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(PREF_NENABLED, enabled);
      editor.commit();  
   
  }
  
  
  
  
  
  
  
  
  
  
}
