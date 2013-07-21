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
import com.google.appinventor.components.runtime.util.GCMServerUtilities;
import java.util.zip.*;
import java.io.*;

import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.app.Activity;

import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.util.Log;

@DesignerComponent(version = YaVersion.NOTIFIER_COMPONENT_VERSION,
    category = ComponentCategory.MISC,
    description = "Google Cloud Messaging",
    nonVisible = true,
    iconName = "images/GoogleCloudMessaging.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.GET_ACCOUNTS, " +
					"android.permission.WAKE_LOCK, com.google.android.c2dm.permission.RECEIVE, android.permission.VIBRATE")
@UsesLibraries(libraries = "gcm.jar")
public final class GoogleCloudMessaging extends AndroidNonvisibleComponent implements Component, OnResumeListener, OnPauseListener, OnInitializeListener, OnStopListener {

  private final Activity activity;
  private final Handler handler;
  
  public static final String TAG = "GCM Component";
 
	
  private String apiProjectNumber;
  private boolean notificationsEnabled;
  private boolean isInitialized;
  private static boolean isRunning;
  
  private static final String PREF_FILE = "GCMState";    // State of GCM component
  private static final String PREF_NENABLED = "nenabled";   // Boolean flag for GV is enabled
  private static final String CACHE_FILE = "gcmcachedmsg";
  private static int messagesCached;
  private static Object cacheLock = new Object();
  
  private ComponentContainer container; // Need this for error reporting
  
  // Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
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
	
	this.container = container;
	
	SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
	if (prefs != null) {
		notificationsEnabled = prefs.getBoolean(PREF_NENABLED, false);
	} else {
		notificationsEnabled = false;
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
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
  @SimpleProperty()
  public void NotificationsEnabled(boolean enabled) {

      this.notificationsEnabled = enabled;
      SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(PREF_NENABLED, enabled);
      editor.commit();  
   
  }
  
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
	  description = "Register into GCM")
  public String Register() {
	if (!isConnectedToInternet()) {
			// Internet Connection is not present
			Log.i(TAG, "NO INTERNET NO FUN :C");
			// stop executing code by return
			return "";
	}
	
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(activity);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(activity);
	
		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(activity);

		// Check if regid already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM			
			GCMRegistrar.register(activity, APIProjectNumber());
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(activity)) {
				// Skips registration.				
				///Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = activity;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// Register on our server
						// On server creates a new user
						GCMServerUtilities.register(context, "X", "Y", regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
  }
  
  
  
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
	  description = "Internet pls")
  public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
 
          }
          return false;
    }
  
}
