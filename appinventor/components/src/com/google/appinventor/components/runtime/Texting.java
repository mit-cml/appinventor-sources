// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.Manifest;
import android.text.TextUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.OAuth2Helper;
import com.google.appinventor.components.runtime.util.OnInitializeListener;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.SmsBroadcastReceiver;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesBroadcastReceivers;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * A component capable of sending and receiving text messages via SMS.

 * @author markf@google.com (Mark Friedman)
 * @author ram8647@gmail.com (Ralph Morelli)
 */
@SuppressWarnings("deprecation")
@DesignerComponent(version = YaVersion.TEXTING_COMPONENT_VERSION,
  description = "<p>A component that will, when the <code>SendMessage</code> method is " +
  "called, send the text message specified in the <code>Message</code> " +
  "property to the phone number specified in the <code>PhoneNumber</code> " +
  "property.</p> " +
  "<p>If the <code>ReceivingEnabled</code> property is set to 1 messages " +
  "will <b>not</b> be received. If <code>ReceivingEnabled</code> is set " +
  "to 2 messages will be received only when the application is " +
  "running. Finally if <code>ReceivingEnabled</code> is set to 3, " +
  "messages will be received when the application is running <b>and</b> " +
  "when the application is not running they will be queued and a " +
  "notification displayed to the user.</p> " +
  "<p>When a message arrives, the <code>MessageReceived</code> event is " +
  "raised and provides the sending number and message.</p> " +
  "<p> An app that includes this component will receive messages even " +
  "when it is in the background (i.e. when it's not visible on the " +
  "screen) and, moreso, even if the app is not running, so long as it's " +
  "installed on the phone. If the phone receives a text message when the " +
  "app is not in the foreground, the phone will show a notification in " +
  "the notification bar.  Selecting the notification will bring up the " +
  "app.  As an app developer, you'll probably want to give your users the " +
  "ability to control ReceivingEnabled so that they can make the phone " +
  "ignore text messages.</p> " +
  "<p>If the GoogleVoiceEnabled property is true, messages can be sent " +
  "over Wifi using Google Voice. This option requires that the user have " +
  "a Google Voice account and that the mobile Voice app is installed on " +
  "the phone. The Google Voice option works only on phones that support " +
  "Android 2.0 (Eclair) or higher.</p> " +
  "<p>To specify the phone number (e.g., 650-555-1212), set the " +
  "<code>PhoneNumber</code> property to a Text string with the specified " +
  "digits (e.g., 6505551212).  Dashes, dots, and parentheses may be " +
  "included (e.g., (650)-555-1212) but will be ignored; spaces may not be " +
  "included.</p> " +
  "<p>Another way for an app to specify a phone number would be to " +
  "include a <code>PhoneNumberPicker</code> component, which lets the " +
  "users select a phone numbers from the ones stored in the the phone's " +
  "contacts.</p>",
  category = ComponentCategory.SOCIAL,
  nonVisible = true,
  iconName = "images/texting.png")

@SimpleObject
@UsesPermissions(permissionNames =
  "android.permission.RECEIVE_SMS, android.permission.SEND_SMS, " +
  "com.google.android.apps.googlevoice.permission.RECEIVE_SMS, " +
  "com.google.android.apps.googlevoice.permission.SEND_SMS, " +
  "android.permission.ACCOUNT_MANAGER, android.permission.MANAGE_ACCOUNTS, " +
  "android.permission.GET_ACCOUNTS, android.permission.USE_CREDENTIALS")
@UsesLibraries(libraries =
  "google-api-client-beta.jar," +
  "google-api-client-android2-beta.jar," +
  "google-http-client-beta.jar," +
  "google-http-client-android2-beta.jar," +
  "google-http-client-android3-beta.jar," +
  "google-oauth-client-beta.jar," +
  "guava-14.0.1.jar")
@UsesBroadcastReceivers(receivers = {
    @ReceiverElement(name = "com.google.appinventor.components.runtime.util.SmsBroadcastReceiver",
                     intentFilters = {
                         @IntentFilterElement(actionElements = {
                             @ActionElement(name = "android.provider.Telephony.SMS_RECEIVED"),
                             @ActionElement(name = "com.google.android.apps.googlevoice.SMS_RECEIVED")
                         })
    })
})
public class Texting extends AndroidNonvisibleComponent
  implements Component, OnResumeListener, OnPauseListener, OnInitializeListener, OnStopListener {

  public static final String TAG = "Texting Component";

  public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
  public static final String GV_SMS_RECEIVED = "com.google.android.apps.googlevoice.SMS_RECEIVED";
  public static final String PHONE_NUMBER_TAG = "com.google.android.apps.googlevoice.PHONE_NUMBER";
  public static final String MESSAGE_TAG = "com.google.android.apps.googlevoice.TEXT";
  public static final String TELEPHONY_INTENT_FILTER = "android.provider.Telephony.SMS_RECEIVED";
  public static final String GV_INTENT_FILTER = "com.google.android.apps.googlevoice.SMS_RECEIVED";
  public static final String GV_PACKAGE_NAME = "com.google.android.apps.googlevoice";
  public static final String GV_SMS_SEND_URL = "https://www.google.com/voice/b/0/sms/send/";
  public static final String GV_URL = "https://www.google.com/voice/b/0/redirection/voice";


  // Meta data key and value that identify an app for handling incoming SMS
  // Used by Texting component
  public static final String META_DATA_SMS_KEY = "sms_handler_component";
  public static final String META_DATA_SMS_VALUE = "Texting";

  //  private static final String GV_PHONES_INFO_URL = "https://www.google.com/voice/b/0/settings/tab/phones";
  private static final String GV_SERVICE = "grandcentral";
  private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13";
  private static final int SERVER_TIMEOUT_MS = 30000;
  private static final String SENT = "SMS_SENT";
  private static final String UTF8 = "UTF-8";
  private static final String MESSAGE_DELIMITER = "\u0001";
  private static final String PREF_GVENABLED = "gvenabled";   // Boolean flag for GV is enabled
  private static final String PREF_RCVENABLED_LEGACY = "receiving"; // Is receiving enabled (Legacy boolean version)
  private static final String PREF_RCVENABLED = "receiving2"; // Is receiving enabled
  private static final String PREF_FILE = "TextingState";    // State of Texting component


  // Google Voice oauth helper
  private GoogleVoiceUtil gvHelper;
  private static Activity activity;
  private static Component component;
  private String authToken;

  // Indicates whether the component is receiving messages or not
  private static int receivingEnabled = ComponentConstants.TEXT_RECEIVING_FOREGROUND;
  private SmsManager smsManager;

  // The phone number to send the text message to.
  private String phoneNumber;
  // The message to send
  private String message;
  // Whether or not Google Voice is enabled.
  private boolean googleVoiceEnabled;

  // No messages can be received until Initialized
  private boolean isInitialized;

  // True when resumed and false when paused. 
  // Messages are cached when app is not running
  private static boolean isRunning;

  // Cache file for cached messages
  private static final String CACHE_FILE = "textingmsgcache";
  private static int messagesCached;
  private static Object cacheLock = new Object();

  //Stores up to 50 pending messages awaiting authentication
  private Queue<String> pendingQueue = new ConcurrentLinkedQueue<String>();

  private ComponentContainer container; // Need this for error reporting

  // Do we have SEND_SMS permission enabled?
  private boolean havePermission = false;

  // Do we have RECEIVE_SMS permission enabled?
  private boolean haveReceivePermission = false;

  /**
   * Creates a new TextMessage component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public Texting(ComponentContainer container) {
    super(container.$form());
    Log.d(TAG, "Texting constructor");
    this.container = container;
    Texting.component = (Texting)this;
    activity = container.$context();

    SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    if (prefs != null) {
      receivingEnabled = prefs.getInt(PREF_RCVENABLED, -1);
      if (receivingEnabled == -1) {
        if (prefs.getBoolean(PREF_RCVENABLED_LEGACY, true)) {
          receivingEnabled = ComponentConstants.TEXT_RECEIVING_FOREGROUND;
        } else {
          receivingEnabled = ComponentConstants.TEXT_RECEIVING_OFF;
        }
      }

      googleVoiceEnabled = prefs.getBoolean(PREF_GVENABLED, false);
      Log.i(TAG, "Starting with receiving Enabled=" + receivingEnabled + " GV enabled=" + googleVoiceEnabled);
    } else {
      receivingEnabled = ComponentConstants.TEXT_RECEIVING_FOREGROUND;
      googleVoiceEnabled = false;
    }

    // Handles authenticating for GV feature.  This sets the authToken.
    if (googleVoiceEnabled)
      new AsyncAuthenticate().execute();

    smsManager = SmsManager.getDefault();
    PhoneNumber("");

    isInitialized = false; // Set true when the form is initialized and can dispatch
    isRunning = false;     // This will be set true in onResume and false in onPause

    // Register this component for lifecycle callbacks 
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
    processCachedMessages();
    NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(SmsBroadcastReceiver.NOTIFICATION_ID);
  }

  // Called from runtime.scm
  public void Initialize() {
    if (receivingEnabled > ComponentConstants.TEXT_RECEIVING_OFF && !haveReceivePermission) {
      // Request receive SMS permission if we are configured to receive but do not have permission
      requestReceiveSmsPermission("Initialize");
    }
  }

  /**
   * Sets the phone number to send the text message to when the SendMessage function is called.
   *
   * @param phoneNumber a phone number to call
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void PhoneNumber(String phoneNumber) {
    Log.i(TAG, "PhoneNumber set: " + phoneNumber);
    this.phoneNumber = phoneNumber;
  }

  /**
   * The number that the message will be sent to when the SendMessage method is called.  The 
   * number is a text string with the specified digits (e.g., 6505551212).  Dashes, dots, 
   * and parentheses may be included (e.g., (650)-555-1212) but will be ignored; spaces
   * should not be included.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description =  "The number that the message will be sent to when the SendMessage method " +
    "is called. The "  +
    "number is a text string with the specified digits (e.g., 6505551212).  Dashes, dots, " +
    "and parentheses may be included (e.g., (650)-555-1212) but will be ignored; spaces " +
    "should not be included.")
  public String PhoneNumber() {
    return phoneNumber;
  }

  /**
   * The text message to that will be sent when the SendMessage method is called.
   *
   * @param message the message to send when the SendMessage function is called.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "The message that will be sent when the SendMessage method is called.")
  public void Message(String message) {
   Log.i(TAG, "Message set: " + message);
   this.message = message;
  }

  /**
   * The text message that will be sent when the SendMessage method is called.
   */
  @SimpleProperty
  public String Message() {
    return message;
  }

  /**
   * Send a text message
   */
  @SimpleFunction
  public void SendMessage() {
    Log.i(TAG, "Sending message "  + message + " to " + phoneNumber);

    // To avoid possible timing issues, save phoneNumber and message locally
    String phoneNumber = this.phoneNumber;
    String message = this.message;

    // If sending by Google Voice, we need authentication
    if (this.googleVoiceEnabled) {

      // If no authToken, get one before trying to send the message.
      if (authToken == null) {
        Log.i(TAG, "Need to get an authToken -- enqueing " + phoneNumber + " " + message);
        boolean ok = pendingQueue.offer(phoneNumber + ":::" + message);

        // Try enqueuing the message
        if (!ok) {
          Toast.makeText(activity, "Pending message queue full. Can't send message", Toast.LENGTH_SHORT).show();
          return;
        }

        // If this is the first pending message, start authentication;
        //  otherwise a previous message will have started it.

        if (pendingQueue.size() == 1)
          new AsyncAuthenticate().execute();

        // If we already have the authToken, just send the message.
      } else {
       Log.i(TAG, "Creating AsyncSendMessage");
       new AsyncSendMessage().execute(phoneNumber, message);
      }

      // We're sending via built-in Sms
    } else {
      Log.i(TAG, "Sending via SMS");
      this.sendViaSms("SendMessage");
    }
  }

  /**
   * Sends pending messages that have been queued awaiting authentication.
   */
  private void processPendingQueue() {
    while (pendingQueue.size() != 0) {
      String entry = (String)pendingQueue.remove();
      String phoneNumber = entry.substring(0,entry.indexOf(":::"));
      String message = entry.substring(entry.indexOf(":::") + 3);
      Log.i(TAG, "Sending queued message " + phoneNumber + " " + message);
      new AsyncSendMessage().execute(phoneNumber, message);
    }
  }

  /**
   * Event that's raised when a text message is received by the phone.
   * 
   * 
   * @param number the phone number that the text message was sent from.
   * @param messageText the text of the message.
   */
  @SimpleEvent
  public static void MessageReceived(String number, String messageText) {
    if (receivingEnabled > ComponentConstants.TEXT_RECEIVING_OFF) {
      Log.i(TAG, "MessageReceived from " + number + ":" + messageText);
      if (EventDispatcher.dispatchEvent(component, "MessageReceived", number, messageText)) {
        Log.i(TAG, "Dispatch successful");
      } else {
        Log.i(TAG, "Dispatch failed, caching");
        synchronized (cacheLock) {
          addMessageToCache(activity, number, messageText);
        }
      }
    }
  }

  /**
   * If this property is true, then SendMessage will attempt to send messages using
   * Google voice.
   *
   * @return 'true' or 'false' depending on whether you want to
   * use Google Voice for sending/receiving messages.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "If true, then SendMessage will attempt to send messages over Wifi " +
    "using Google Voice.  This requires that the Google Voice app must be installed " +
    "and set up on the phone or tablet, with a Google Voice account.  If GoogleVoiceEnabled " +
    "is false, the device must have phone and texting service in order to send or " +
    "receive messages with this component.")
  public boolean GoogleVoiceEnabled() {
    return googleVoiceEnabled;
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
  public void GoogleVoiceEnabled(boolean enabled) {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR) {
      this.googleVoiceEnabled = enabled;
      SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putBoolean(PREF_GVENABLED, enabled);
      editor.commit();
    } else {
      Toast.makeText(activity, "Sorry, your phone's system does not support this option.", Toast.LENGTH_LONG).show();
    }
  }

  /**
   * Gets whether you want the {@link #MessageReceived(String,String)} event to
   * get run when a new text message is received.
   *
   * @return 1,2 or 3 indicating that receiving is disabled (1) or foreground only
   *          (2) or always (3).
   *          {@link #MessageReceived(String,String)} event to get run when a
   *          new text message is received.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "If set to 1 (OFF) no messages will be received.  If set to 2 (FOREGROUND) or" +
    "3 (ALWAYS) the component will respond to messages if it is running. If the " +
    "app is not running then the message will be discarded if set to 2 " +
    "(FOREGROUND). If set to 3 (ALWAYS) and the app is not running the phone will " +
    "show a notification.  Selecting the notification will bring up the app " +
    "and signal the MessageReceived event.  Messages received when the app " +
    "is dormant will be queued, and so several MessageReceived events might " +
    "appear when the app awakens.  As an app developer, it would be a good " +
    "idea to give your users control over this property, so they can make " +
    "their phones ignore text messages when your app is installed.")
  public int ReceivingEnabled() {
    return receivingEnabled;
  }

  /**
   * Sets whether you want the {@link #MessageReceived(String,String)} event to
   * get run when a new text message is received.
   *
   * @param enabled  0 = never receive, 1 = receive foreground only, 2 = receive always
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXT_RECEIVING, defaultValue = "2") // Default is FOREGROUND
  @SimpleProperty()
  public void ReceivingEnabled(int enabled) {
    if ((enabled < ComponentConstants.TEXT_RECEIVING_OFF) ||
        (enabled > ComponentConstants.TEXT_RECEIVING_ALWAYS)) {
      container.$form().dispatchErrorOccurredEvent(this, "Texting",
          ErrorMessages.ERROR_BAD_VALUE_FOR_TEXT_RECEIVING, enabled);
      return;
    }

    Texting.receivingEnabled = enabled;
    SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(PREF_RCVENABLED, enabled);
    editor.remove(PREF_RCVENABLED_LEGACY); // Remove any legacy value
    editor.commit();
    if (enabled > ComponentConstants.TEXT_RECEIVING_OFF && !haveReceivePermission) {
      requestReceiveSmsPermission("ReceivingEnabled");
    }
  }

  public static int isReceivingEnabled(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    int retval = prefs.getInt(PREF_RCVENABLED, -1);
    if (retval == -1) {         // Fetch legacy value
      if (prefs.getBoolean(PREF_RCVENABLED_LEGACY, true))
        return ComponentConstants.TEXT_RECEIVING_FOREGROUND; // Foreground
      else
        return ComponentConstants.TEXT_RECEIVING_OFF; // Off
    }
    return retval;
  }

  /**
   * Parse the messages out of the extra fields from the "android.permission.RECEIVE_SMS" broadcast
   * intent.
   *
   * Note: This code was copied from the Android android.provider.Telephony.Sms.Intents class.
   *
   * @param intent the intent to read from
   * @return an array of SmsMessages for the PDUs
   */
  public static SmsMessage[] getMessagesFromIntent(
                                                   Intent intent) {
    Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
    byte[][] pduObjs = new byte[messages.length][];

    for (int i = 0; i < messages.length; i++) {
      pduObjs[i] = (byte[]) messages[i];
    }
    byte[][] pdus = new byte[pduObjs.length][];
    int pduCount = pdus.length;
    SmsMessage[] msgs = new SmsMessage[pduCount];
    for (int i = 0; i < pduCount; i++) {
      pdus[i] = pduObjs[i];
      msgs[i] = SmsMessage.createFromPdu(pdus[i]);
    }
    return msgs;
  }

  /**
   * Sends all the messages in the cache through MessageReceived and
   * clears the cache.
   */
  private void processCachedMessages() {
    String[] messagelist = null;
    synchronized (cacheLock) {
      messagelist =  retrieveCachedMessages();
    }
    if (messagelist == null)
      return;
    Log.i(TAG, "processing " +  messagelist.length + " cached messages ");

    for (int k = 0; k < messagelist.length; k++) {
      String phoneAndMessage = messagelist[k];
      Log.i(TAG, "Message + " + k + " " + phoneAndMessage);

      int delim = phoneAndMessage.indexOf(":");

      // If receiving is not enabled, messages are not dispatched
      if ((receivingEnabled > ComponentConstants.TEXT_RECEIVING_OFF) && delim != -1) {
        MessageReceived(phoneAndMessage.substring(0,delim),
            phoneAndMessage.substring(delim+1));
      }
    }
  }

  /**
   * Retrieves cached messages from the cache file
   * and deletes the file.
   * @return
   */
  private String[] retrieveCachedMessages() {
    Log.i(TAG, "Retrieving cached messages");
    String cache = "";
    try {
      byte[] bytes = FileUtil.readFile(CACHE_FILE);
      cache = new String(bytes);
      activity.deleteFile(CACHE_FILE);
      messagesCached = 0;
      Log.i(TAG, "Retrieved cache " + cache);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "No Cache file found -- this is not (usually) an error");
      return null;
    } catch (IOException e) {
      Log.e(TAG, "I/O Error reading from cache file");
      e.printStackTrace();
      return null;
    }
    String messagelist[] = cache.split(MESSAGE_DELIMITER);
    return messagelist;
  }

  /**
   * Called by SmsBroadcastReceiver
   * @return isRunning if the app is running in the foreground.
   */
  public static boolean isRunning() {
    return isRunning;
  }

  /**
   * Used to keep count in Notifications.
   * @return message count
   */
  public static int getCachedMsgCount() {
    return messagesCached;
  }

  /**
   * Processes cached messages if the app is initialized
   */
  @Override
  public void onResume() {
    Log.i(TAG, "onResume()");
    isRunning = true;
    if (isInitialized) {
      processCachedMessages();
      NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
      nm.cancel(SmsBroadcastReceiver.NOTIFICATION_ID);
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
   * This method is called by SmsBroadcastReceiver when a message is received.
   * @param phone
   * @param msg
   */
  public static void handledReceivedMessage(Context context, String phone, String msg) {
    if (isRunning) {
      MessageReceived(phone, msg);
    } else {
      synchronized (cacheLock) {
        addMessageToCache(context, phone, msg);
      }
    }
  }

  /**
   * Messages a cached in a private file
   * @param context
   * @param phone
   * @param msg
   */
  private static void addMessageToCache(Context context, String phone, String msg) {
    try {
      String cachedMsg = phone + ":" + msg + MESSAGE_DELIMITER;
      Log.i(TAG, "Caching " + cachedMsg);
      FileOutputStream fos = context.openFileOutput(CACHE_FILE, Context.MODE_APPEND);
      fos.write(cachedMsg.getBytes());
      fos.close();
      ++messagesCached;
      Log.i(TAG, "Cached " + cachedMsg);
    } catch (FileNotFoundException e) {
      Log.e(TAG, "File not found error writing to cache file");
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(TAG, "I/O Error writing to cache file");
      e.printStackTrace();
    }
  }

  /**
   * Utility class built from Free Software (GPLv3 or later)
   * by cannibalizing parts of Voice.java of the free software
   * package, Google-Voice-Java:
   *
   * @see http://code.google.com/p/google-voice-java/
   *
   */
  class GoogleVoiceUtil {
    private final int MAX_REDIRECTS = 5;
    private static final String COOKIES_HEADER = "Set-Cookie";

    String general; // Google's GV page
    String rnrSEE;  // Value that passed into SMS's
    String authToken;
    int redirectCounter;
    private boolean isInitialized;
    CookieManager cookies = new CookieManager();

    /**
     * The constructor sometimes fails to getGeneral
     * @param authToken
     */
    public GoogleVoiceUtil(String authToken) {
      Log.i(TAG, "Creating GV Util");
      this.authToken = authToken;
      try {
        this.general = getGeneral();
        Log.i(TAG, "general = " + this.general);
        setRNRSEE();
        isInitialized = true;   // If we make it to here, we're good to go
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public boolean isInitialized() {
      return isInitialized;
    }

    /**
     * Free software method copied and adapted from Voice.java
     * @see http://code.google.com/p/google-voice-java/
     * 
     */
    private String sendGvSms(String smsData) {
      Log.i(TAG, "sendGvSms()");
      StringBuilder response = new StringBuilder();
      try {
        // Add the RNR_SE to the message
        smsData += "&" + URLEncoder.encode("_rnr_se", UTF8) + "=" + URLEncoder.encode(rnrSEE, UTF8);
        Log.i(TAG, "smsData = " + smsData);
        URL smsUrl = new URL(GV_SMS_SEND_URL);

        HttpURLConnection smsConn = (HttpURLConnection) smsUrl.openConnection();
        smsConn.setRequestProperty( "Authorization", "GoogleLogin auth=" + authToken );
        smsConn.setRequestProperty("User-agent", USER_AGENT);
        setCookies(smsConn);
        smsConn.setDoOutput(true);
        smsConn.setConnectTimeout(SERVER_TIMEOUT_MS);

        Log.i(TAG, "sms request = " + smsConn);
        OutputStreamWriter callwr = new OutputStreamWriter(smsConn.getOutputStream());
        callwr.write(smsData);
        callwr.flush();

        processCookies(smsConn);
        BufferedReader callrd = new BufferedReader(new InputStreamReader(smsConn.getInputStream()));

        String line;
        while ((line = callrd.readLine()) != null) {
          response.append(line);
          response.append("\n");  // HTTP uses \r\n, but Android is built on Linux, so use Unix line endings
        }
        Log.i(TAG, "sendGvSms:  Sent SMS, response = " + response);

        callwr.close();
        callrd.close();

        if (response.length() == 0) {
          throw new IOException("No Response Data Received.");
        } else {
          return response.toString();
        }
      } catch (IOException e) {
        Log.i(TAG, "IO Error on Send " + e.getMessage(), e);
        return "IO Error Message not sent";
      }
    }


    /**
     * Fetches the page Source Code for the Voice homepage. This file contains
     * most of the useful information for the Google Voice Account such as
     * attached PhoneOld info and Contacts.
     *
     * @return the general
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String getGeneral() throws IOException {
      Log.i(TAG, "getGeneral()");
      return get(GV_URL);
    }

    /**
     * Internal method which parses the Homepage source code to determine the
     * rnrsee variable, this variable is passed into most functions for placing
     * calls and sms.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void setRNRSEE() throws IOException {
      Log.i(TAG, "setRNRSEE()");
      if (general != null) {
        if(general.contains("'_rnr_se': '")) {
          String p1 = general.split("'_rnr_se': '", 2)[1];
          rnrSEE = p1.split("',", 2)[0];
          Log.i(TAG,"Successfully Received rnr_se.");
          p1 = null;
        } else {
          Log.i(TAG, "Answer did not contain rnr_se! "+ general);
          throw new IOException("Answer did not contain rnr_se! "+ general);
        }
      } else {
        Log.i(TAG,"setRNRSEE(): Answer was null!");
        throw new IOException("setRNRSEE(): Answer was null!");
      }
    }

    /**
     * Sets the Cookie HTTP header if any cookies are currently stored in the
     * cookie manager.
     *
     * @param conn HTTP connection over which to send cookies
     */
    void setCookies(HttpURLConnection conn) {
      if (cookies.getCookieStore().getCookies().size() > 0) {
        conn.setRequestProperty("Cookie", TextUtils.join(";", cookies.getCookieStore().getCookies()));
      }
    }

    /**
     * Processes the Set-Cookie header(s), if any, received via an
     * HttpURLConnection and stores them in the cookie manager.
     *
     * @param conn HTTP connection over which to receive cookies
     */
    void processCookies(HttpURLConnection conn) {
      List<String> cookiesHeader = conn.getHeaderFields().get(COOKIES_HEADER);
      if (cookiesHeader != null) {
        for (String cookie : cookiesHeader) {
          cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
        }
      }
    }

    /**
     * HTTP GET request for a given URL String.
     *
     * @param urlString
     *            the url string
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String get(String urlString) throws IOException {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      int responseCode = 0;
      try {
        conn.setRequestProperty( "Authorization", "GoogleLogin auth="+authToken );
        conn.setRequestProperty("User-agent", USER_AGENT);
        conn.setInstanceFollowRedirects(false); // will follow redirects of same protocol http to http, but does not follow from http to https for example if set to true
        setCookies(conn);

        // Get the response
        conn.connect();
        responseCode = conn.getResponseCode();
        Log.i(TAG, urlString + " - " + conn.getResponseMessage());
      } catch (Exception e) {
        throw new IOException(urlString + " : " + conn.getResponseMessage() + "("+responseCode+") : IO Error."); 
      }

      processCookies(conn);

      InputStream is;
      if(responseCode==200) {
        is = conn.getInputStream();
      } else if(responseCode==HttpURLConnection.HTTP_MOVED_PERM || responseCode==HttpURLConnection.HTTP_MOVED_TEMP || responseCode==HttpURLConnection.HTTP_SEE_OTHER || responseCode==307) {
        redirectCounter++;
        if(redirectCounter > MAX_REDIRECTS) {
          redirectCounter = 0;
          throw new IOException(urlString + " : " + conn.getResponseMessage() + "("+responseCode+") : Too many redirects. exiting.");
        }
        String location = conn.getHeaderField("Location");
        if(location!=null && !location.equals("")) {
          System.out.println(urlString + " - " + responseCode + " - new URL: " + location);
          return get(location);
        } else {
          throw new IOException(urlString + " : " + conn.getResponseMessage() + "("+responseCode+") : Received moved answer but no Location. exiting.");
        }
      } else {
        is = conn.getErrorStream();
      }
      redirectCounter = 0;

      if(is==null) {
        throw new IOException(urlString + " : " + conn.getResponseMessage() + "("+responseCode+") : InputStream was null : exiting.");
      }

      String result="";
      try {
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
          sb.append(line + "\n\r");
        }
        rd.close();
        result = sb.toString();
      } catch (Exception e) {
        throw new IOException(urlString + " - " + conn.getResponseMessage() + "("+responseCode+") - " +e.getLocalizedMessage());
      }
      return result;
    }
  }


  /**
   * Callback method to handle the result of attempting to send a message. 
   * Each message is assigned a Broadcast receiver that is notified by 
   * the phone's radio regarding the status of the sent message. The 
   * receivers call this method.  (See transmitMessage() method below.)
   * 
   * @param context
   *            The context in which the calling BroadcastReceiver is running.
   * @param receiver
   *            Currently unused. Intended as a special BroadcastReceiver to
   *            send results to. (For instance, if another plugin wanted to do
   *            its own handling.)
   * @param resultCode, the code sent back by the phone's Radio
   * @param seq, the message's sequence number
   * @param smsMsg, the message being processed
   */
  private synchronized void handleSentMessage(Context context,
                                              BroadcastReceiver receiver, int resultCode, String smsMsg) {
    switch (resultCode) {
      case Activity.RESULT_OK:
        Log.i(TAG, "Received OK, msg:" + smsMsg);
        Toast.makeText(activity, "Message sent", Toast.LENGTH_SHORT).show();
        break;
      case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
        Log.e(TAG, "Received generic failure, msg:" + smsMsg);
        Toast.makeText(activity, "Generic failure: message not sent", Toast.LENGTH_SHORT).show();
        break;
      case SmsManager.RESULT_ERROR_NO_SERVICE:
        Log.e(TAG, "Received no service error, msg:"  + smsMsg);
        Toast.makeText(activity, "No Sms service available. Message not sent.", Toast.LENGTH_SHORT).show();
        break;
      case SmsManager.RESULT_ERROR_NULL_PDU:
        Log.e(TAG, "Received null PDU error, msg:"  + smsMsg);
        Toast.makeText(activity, "Received null PDU error. Message not sent.", Toast.LENGTH_SHORT).show();
        break;
      case SmsManager.RESULT_ERROR_RADIO_OFF:
        Log.e(TAG, "Received radio off error, msg:" + smsMsg);
        Toast.makeText(activity, "Could not send SMS message: radio off.", Toast.LENGTH_LONG).show();
        break;
    }
  }

  /**
   * Sends a text message via SMS. No authentication required.
   * This method is called only when the UseGoogleVoice option is disabled.
   */
  private void sendViaSms(final String caller) {
    Log.i(TAG, "Sending via built-in Sms");

    // Need to make sure we have the SEND_SMS permission
    if (!havePermission) {
      final Form form = container.$form();
      final Texting me = this;
      form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            form.askPermission(Manifest.permission.SEND_SMS,
              new PermissionResultHandler() {
                @Override
                public void HandlePermissionResponse(String permission, boolean granted) {
                  if (granted) {
                    me.havePermission = true;
                    me.sendViaSms(caller);
                  } else {
                    form.dispatchPermissionDeniedEvent(me, caller, Manifest.permission.SEND_SMS);
                  }
                }
              });
          }
        });
      return;
    }

    ArrayList<String> parts = smsManager.divideMessage(message);
    int numParts = parts.size();
    ArrayList<PendingIntent> pendingIntents = new ArrayList<PendingIntent>();
    for (int i = 0; i < numParts; i++)
      pendingIntents.add(PendingIntent.getBroadcast(activity, 0, new Intent(SENT), 0));

    // Receiver for when the SMS is sent
    BroadcastReceiver sendReceiver = new BroadcastReceiver() {
      @Override
      public synchronized void onReceive(Context arg0, Intent arg1) {
        try {
          handleSentMessage(arg0, null, getResultCode(), message);
          activity.unregisterReceiver(this);
        } catch (Exception e) {
          Log.e("BroadcastReceiver",
              "Error in onReceive for msgId "  + arg1.getAction());
          Log.e("BroadcastReceiver", e.getMessage());
          e.printStackTrace();
        }
      }
    };
    // This may result in an error -- a "sent" or "error" message will be displayed
    activity.registerReceiver(sendReceiver, new IntentFilter(SENT));
    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, pendingIntents, null);
  }

  private void requestReceiveSmsPermission(final String caller) {
    form.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        form.askPermission(Manifest.permission.RECEIVE_SMS,
            new PermissionResultHandler() {
              @Override
              public void HandlePermissionResponse(String permission, boolean granted) {
                if (granted) {
                  haveReceivePermission = true;
                } else {
                  form.dispatchPermissionDeniedEvent(Texting.this, caller, Manifest.permission.RECEIVE_SMS);
                }
              }
            });
      }
    });

  }

  /**
   * Handles authentication needed for sending via Google Voice
   */
  class AsyncAuthenticate extends AsyncTask<Void, Void, String> {

    @Override
    protected String doInBackground(Void... arg0) {
      Log.i(TAG, "Authenticating");

      // Get and return the authtoken
      return new OAuth2Helper().getRefreshedAuthToken(activity, GV_SERVICE);
    }

    /**
     * Sets the authToken instance variable and sends a message if one is waiting.
     */
    @Override
    protected void onPostExecute(String result) {
      Log.i(TAG, "authToken = " + result);
      authToken = result;

      Toast.makeText(activity, "Finished authentication", Toast.LENGTH_SHORT).show();

      // Send any pending messages
      processPendingQueue();
    }
  }

  /**
   * Asynchronously Sends a text message via Google Voice.  This requires authentication.
   * This is used only when UseGoogleVoice option is enabled.
   *
   * NOTE: Because a background process is used, sending multiple messages with GoogleVoice
   * cannot be done in a loop. The app must use a clock timer.
   */
  class AsyncSendMessage extends AsyncTask<String, Void, String> {

    /**
     * Handles sending SMS over Google Voice or built-in SMS
     * @param arg0 a String array containing the phoneNumber and message.
     *
     * NOTE: The authToken referenced here is a Texting instance variable.
     * It is set when the Texting constructor.
     */
    @Override
    protected String doInBackground(String... args) {
      String phoneNumber = args[0];
      String message = args[1];
      String response = "";
      String smsData = "";

      Log.i(TAG, "Async sending phoneNumber = " + phoneNumber + " message = " + message);

      try {

        // Set up the smsMessage for Google Voice
        smsData = 
          URLEncoder.encode("phoneNumber", UTF8) + "=" + URLEncoder.encode(phoneNumber, UTF8) + 
          "&" + URLEncoder.encode("text", UTF8) + "=" + URLEncoder.encode(message, UTF8);

        if (gvHelper == null) {
          gvHelper = new GoogleVoiceUtil(authToken);  
        }
        if (gvHelper.isInitialized()) {
          response = gvHelper.sendGvSms(smsData);
          Log.i(TAG, "Sent SMS, response = " + response);
        } else {
          return "IO Error: unable to create GvHelper";
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return response;
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);

      JSONObject json;
      boolean ok = false;
      int code = 0;
      try {
        json = new JSONObject(result);
        ok = json.getBoolean("ok");
        code = json.getJSONObject("data").getInt("code");
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if (ok)
        Toast.makeText(activity, "Message sent", Toast.LENGTH_SHORT).show();
      else if (code == 58) 
        Toast.makeText(activity, "Errcode 58: SMS limit reached", Toast.LENGTH_SHORT).show();
      else if (result.contains("IO Error")) 
        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
    }
  }

  /**
   * Save the component's state in shared preference file before it is killed.
   */
  @Override
  public void onStop() {
    SharedPreferences prefs = activity.getSharedPreferences(PREF_FILE, Activity.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(PREF_RCVENABLED, receivingEnabled);
    editor.putBoolean(PREF_GVENABLED, googleVoiceEnabled);
    editor.commit();
  }

}

