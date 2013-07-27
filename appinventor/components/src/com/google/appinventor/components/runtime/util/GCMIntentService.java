// @author pablomorpheo@gmail.com (Pablo García)
package com.google.appinventor.components.runtime.util;


//import static com.androidhive.pushnotifications.CommonUtilities.SENDER_ID;
//import static com.androidhive.pushnotifications.CommonUtilities.displayMessage;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.R;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.Toast;

import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.GoogleCloudMessaging;
import com.google.appinventor.components.runtime.ReplForm;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	private static final String PREF_FILE = "GCMState";    // State of GCM component
    private static final String PREF_NENABLED = "nenabled";   // Boolean flag for GV is enabled
    private static final String PREF_SENDERID = "sid";
	private static final String PREF_DEFTITLE = "deftitle";
    private static final String PREF_DEFSCREEN = "defscreen";
	
	private static final String CACHE_FILE = "gcmcachedmsg";
    private static final String MESSAGE_DELIMITER = "\u0001";
	
	/*
    public GCMIntentService() {
        super("97180579230");
    }
	*/
	
	//PARECE QUE CON ESTO PUEDO TENER SENDER IDS DINAMICOS WHOA!
	// belencruzz@gmail.com (Belén Cruz)
	//
	@Override
	protected String[] getSenderIds(Context context) {
		String[] ids = new String[1];
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
		if (prefs != null) {
			ids[0] = prefs.getString(PREF_SENDERID, "");
		} else {
			ids[0] = "";
		}
		
		//ids[0] = Constants.SENDER_ID;
		return ids;// return super
	}

    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        //displayMessage(context, "Your device registred with GCM");
        //Log.d("NAME", MainActivity.name);
        //GCMServerUtilities.register(context, "NAME", "EMAIL", registrationId);
    }

    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        //displayMessage(context, getString(R.string.gcm_unregistered));
        //GCMServerUtilities.unregister(context, registrationId);
    }

    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("price");
        
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = "";
        //displayMessage(context, message);
        // notifies user
        //generateNotification(context, message);
    }

    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        //displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        //displayMessage(context, getString(R.string.gcm_recoverable_error,
        //        errorId));
        return super.onRecoverableError(context, errorId);
    }

	
	
	
	
	 static void displayMessage(Context context, String message) {
		/*String packageName = context.getPackageName();
        Intent intent = new Intent(packageName + ".DISPLAY_MESSAGE");
        intent.putExtra("message", message);
        context.sendBroadcast(intent);*/
    }
  
	
	
	
	
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        //int icon = Android.R.drawable.ya;
		String packageName = context.getPackageName();
		int icon = context.getResources().getIdentifier("ya", "drawable", packageName);
        long when = System.currentTimeMillis();
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
		if (prefs != null) {
		
			//Toast.makeText(context, "0tosend "+message, Toast.LENGTH_LONG).show();
			//GoogleCloudMessaging.handledReceivedMessage(context, message);
			
			String cachedMessages = prefs.getString(CACHE_FILE, "");
			
			SharedPreferences.Editor editor = prefs.edit();
			if (cachedMessages=="") {
				editor.putString(CACHE_FILE, message);
			} else {
				editor.putString(CACHE_FILE, message + MESSAGE_DELIMITER + cachedMessages);
			}
			editor.commit();
			
			
			
			if (prefs.getBoolean(PREF_NENABLED, false)) {
				try {
		
					
					
					
					
					
					String nscreen = ".Screen1";
					String[] lines = new String[3];
					
					if (message.contains("\\|\\|") || message.contains("||")) {
					
						String[] lin = message.split("\\|\\|");
						lines[0] = lin[0];
						lines[1] = lin[1];
						
					} else {
					
						lines[1]=message;
						lines[0] = prefs.getString(PREF_DEFTITLE, "");
					}
					
					nscreen = "." + prefs.getString(PREF_DEFSCREEN, "Screen1");
					
					
					String classname = packageName + nscreen;
					
					NotificationManager notificationManager = (NotificationManager)
							context.getSystemService(Context.NOTIFICATION_SERVICE);
					Notification notification = new Notification(icon, lines[1], when);
					
					String title = lines[0];
					
					Intent notificationIntent = new Intent(context, Class.forName(classname));
					// set intent so it does not start a new activity
					notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
							Intent.FLAG_ACTIVITY_SINGLE_TOP);
					PendingIntent intent =
							PendingIntent.getActivity(context, 0, notificationIntent, 0);
					notification.setLatestEventInfo(context, title, lines[1], intent);
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					
					// Play default notification sound
					notification.defaults |= Notification.DEFAULT_SOUND;
					
					//notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "your_sound_file_name.mp3");
					
					// Vibrate if vibrate is enabled
					notification.defaults |= Notification.DEFAULT_VIBRATE;
					notificationManager.notify(0, notification);   
					//wakelocker here!
				} catch (ClassNotFoundException e) {
				  e.printStackTrace();
				}
			}
		} 
		
		
		

    }

}