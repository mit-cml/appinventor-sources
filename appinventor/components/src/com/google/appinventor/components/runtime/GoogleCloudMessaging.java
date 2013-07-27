// @author pablomorpheo@gmail.com (Pablo García)

package com.google.appinventor.components.runtime;



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
import com.google.appinventor.components.runtime.util.WakeLocker;

import java.io.*;

import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.app.Activity;

import java.lang.Runnable;
import java.util.Collections;

import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.telephony.TelephonyManager;

import android.util.Log;

@DesignerComponent(version = YaVersion.NOTIFIER_COMPONENT_VERSION,
    category = ComponentCategory.MISC,
    description = "Google Cloud Messaging",
    nonVisible = true,
    iconName = "images/GoogleCloudMessaging.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.GET_ACCOUNTS, " +
					"android.permission.WAKE_LOCK, com.google.android.c2dm.permission.RECEIVE, android.permission.VIBRATE, android.permission.READ_PHONE_STATE")
@UsesLibraries(libraries = "gcm.jar")
public class GoogleCloudMessaging extends AndroidNonvisibleComponent implements Component, OnResumeListener, OnPauseListener, OnInitializeListener, OnStopListener {

  private static Activity activity;
  //private final Handler handler;
  
  public static final String TAG = "GCM Component";
 
  private String GCMregId = "";
  private String apiProjectNumber = "";
  private String defaultNotificationTitle = "";
  private String defaultNotificationScreen = "";
  private boolean notificationsEnabled;
  private boolean isInitialized;
  private static boolean isRunning;
  
  private static final String PREF_FILE = "GCMState";    // State of GCM component
  private static final String PREF_NENABLED = "nenabled";   // Boolean flag for GV is enabled
  private static final String PREF_SENDERID = "sid";
  private static final String PREF_DEFTITLE = "deftitle";
  private static final String PREF_DEFSCREEN = "defscreen";
  private static final String CACHE_FILE = "gcmcachedmsg";
  private static final String MESSAGE_DELIMITER = "\u0001";
  private static int messagesCached;
  private static Object cacheLock = new Object();
  
  private ComponentContainer container; // Need this for error reporting
  private static Component component;
  
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
    //handler = new Handler();
	
	this.container = container;
	
	isInitialized = false; // Set true when the form is initialized and can dispatch
    isRunning = false;     // This will be set true in onResume and false in onPause
	
	GoogleCloudMessaging.component = (GoogleCloudMessaging)this;
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
  
  
  
    private Handler handler = new Handler();
	private Runnable runnable = new Runnable() 
	{

		public void run() 
		{
			 processCachedMessages();
			 handler.postDelayed(this, 500);
		}
	};
  
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
    processCachedMessages();
	runnable.run();
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
	
	//String packageName = activity.getPackageName();
	/////////////////activity.registerReceiver(mHandleMessageReceiver, new IntentFilter(packageName + ".DISPLAY_MESSAGE"));
	
    if (isInitialized) {
      processCachedMessages();
	  runnable.run();
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
	handler.removeCallbacks(runnable);
    isRunning = false;
  }
  
  /**
   * Save the component's state in shared preference file before it is killed.
   */
  @Override
  public void onStop() {
    Log.i(TAG, "onStop()");
	////////////////////activity.unregisterReceiver(mHandleMessageReceiver);
  }
  
  
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "Default notification title if not using <title>||<msg> as msg")
  public void DefaultNotificationTitle(String title) {
    this.defaultNotificationTitle = title;
	SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PREF_DEFTITLE, title);
    editor.commit();  
  }
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "Default notification title if not using <title>||<msg> as msg")
  public String DefaultNotificationTitle() {
    return this.defaultNotificationTitle;
  }
  
  
  
  
  
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "Screen1")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "MUST BE NUMBER!")
  public void DefaultNotificationScreen(String screen) {
    this.defaultNotificationScreen = screen;
	SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PREF_DEFSCREEN, screen);
    editor.commit();  
  }
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "")
  public String DefaultNotificationScreen() {
    return this.defaultNotificationScreen;
  }
  
  
  
  
  
  
  
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "Goto google api console to obtain one (not the API Key)")
  public void APIProjectNumber(String api) {
    this.apiProjectNumber = api;
	SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PREF_SENDERID, api);
    editor.commit();  
  }
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "Goto google api console to obtain one (not the API Key)")
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
  
  
  @SimpleFunction
  public void Register() {
	if (!isConnectedToInternet()) {
			// Internet Connection is not present
			Log.i(TAG, "NO INTERNET NO FUN :C");
			// stop executing code by return
			return ;
	}
	
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(activity);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(activity);
	
		// Get GCM registration id
		String regId = GCMRegistrar.getRegistrationId(activity);

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

				final Context context = activity;

				regId = GCMRegistrar.getRegistrationId(activity);
			}
		}
		GCMregId = regId;
		
	  
		Log.i(TAG, "regid = " + regId);
	return ;
  }
  
  
  @SimpleFunction
  public void Unegister() {
	if (!isConnectedToInternet()) {
			// Internet Connection is not present
			Log.i(TAG, "NO INTERNET NO FUN :C");
			// stop executing code by return
			return ;
	}
	
	GCMRegistrar.setRegisteredOnServer(activity, false);
	GCMRegistrar.unregister(activity);
	
	GCMregId = "";
		
	Log.i(TAG, "unregistered from gcm");
	return ;
  }
  
  
  
  
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "your regid to receive push")
  public String RegistrationID() {
    return GCMRegistrar.getRegistrationId(activity);
  }
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "get phone name")
  public String GetPhoneName() {
    return android.os.Build.MODEL;
  }
  
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description =  "get phone IMEI")
  public String GetPhoneIMEI() {
	TelephonyManager mngr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE); 
    return mngr.getDeviceId();
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
  
  


	//La proxima vez que hagas una jilipollez asi, recuerda, te buscare, y como soy yo, me encontrare y me dare una paliza.
	//PRIMER AVISO

  @SimpleEvent
  public static void OnPush(String pushTitle, String PushMessage) {
	EventDispatcher.dispatchEvent(component, "OnPush", pushTitle, PushMessage);
  }




  public static void processCachedMessages() {
  
        SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
		if (prefs != null) {
		
			
			String cachedMessages = prefs.getString(CACHE_FILE, "");
			
			if (cachedMessages == null || cachedMessages == "") 
			return;
			
			String[] messagelist = cachedMessages.split(MESSAGE_DELIMITER);
			
			//Collections.reverse(messagelist);
			
			//for (int k = 0; k < messagelist.length; k++) {
			for (int k = messagelist.length-1; k >= 0; k--) {
				String phoneAndMessage = messagelist[k];
				Log.i(TAG, "Message + " + k + " " + phoneAndMessage);
				
				
				String[] lines = new String[3];
					
					if (phoneAndMessage.contains("\\|\\|") || phoneAndMessage.contains("||")) {
					
						String[] lin = phoneAndMessage.split("\\|\\|");
						lines[0] = lin[0];
						lines[1] = lin[1];
						
					} else {
					
						lines[1] = phoneAndMessage;
						lines[0] = prefs.getString(PREF_DEFTITLE, "");
					}
				
				
				OnPush(lines[0], lines[1]);
			}
			
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(CACHE_FILE, "");
			editor.commit();
			
    }
  }

  /* ALL HAIL TO HYPNOTOAD! */
  
  
  
  
  
  
  
  
  
}
