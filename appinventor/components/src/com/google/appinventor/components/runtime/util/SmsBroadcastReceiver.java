// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

/*
 * This is revised from here: https://github.com/cicada-dev
 * and was original published under Apache 2.0 license.
 *
 */
package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Texting;
import com.google.appinventor.components.runtime.ReplForm;

import android.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

/**
 * This broadcast receiver accepts incoming SMS messages from either
 * Messaging, the built in Android SMS program or from Google Voice (which
 * must be installed on the user's phone.  The receiver will be invoked by
 * the system even when the app is not running.
 *
 * The receiver does not need to be instantiated in the app.
 * The receiver is declared statically in the <application> tag but
 * outside the <activity> tag in the Manifest:
 *
        <receiver
            android:name="org.hfoss.SmsBroadcastReceiver"
            android:enabled="true"
            android:exported="true" >
            <intent-filter>
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
                <action
                    android:name="com.google.android.apps.googlevoice.SMS_RECEIVED"
                    android:permission="com.google.android.apps.googlevoice.permission.RECEIVE_SMS" />
            </intent-filter>
        </receiver>

 * @author rmorelli
 *
 */
@SuppressWarnings("deprecation")
public class SmsBroadcastReceiver extends BroadcastReceiver {

  public static final String TAG = "SmsBroadcastReceiver";
  public static final int NOTIFICATION_ID = 8647;

  /**
   * Called by the system when an incoming SMS is received either from Google Voice
   * or through the built-in Telephony app.
   *
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "onReceive");

    // Extract the phone number and message.
    String phone = getPhoneNumber(intent);
    String msg = getMessage(intent);

    Log.i(TAG, "Received " + phone + " : " + msg);

    // If activity is receiving messages, send the message;
    // It'll be cached if the app isn't running
    if (Texting.isReceivingEnabled(context)) {
      if (isRepl(context)) {    // If we are the Repl, we only handle texts if we are running
        if (Texting.isRunning()) {
          Texting.handledReceivedMessage(context, phone, msg);
        } else {
          Log.i(TAG, context.getApplicationInfo().packageName + " is not running and we are the repl, ignoring message.");
        }
      } else {
        Texting.handledReceivedMessage(context, phone, msg);
        if (!Texting.isRunning()) {
          // If the app isn't running, send a Notification
          sendNotification(context, phone, msg);
        } else {
          Log.i(TAG, context.getApplicationInfo().packageName + " is running");
        }
      }
    } else {
      Log.i(TAG, context.getApplicationInfo().packageName + " receiving disabled");
    }
  }

  /**
   * Extracts the phone number from the intent and formats it.
   * @param intent
   * @return
   */
  private String getPhoneNumber(Intent intent) {
    String phone = "";

    // For Google Voice, phone and msg are stored in String extras. Pretty them up

    if (intent.getAction().equals("com.google.android.apps.googlevoice.SMS_RECEIVED")) {
      phone = intent.getExtras().getString(Texting.PHONE_NUMBER_TAG);
      phone = PhoneNumberUtils.formatNumber(phone);

      // For Telephony, phone and msg are stored in PDUs.

    } else {
      Object[] pdus = (Object[]) intent.getExtras().get("pdus");
      for (Object pdu : pdus) {
        SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) pdu);
        phone = smsMsg.getOriginatingAddress();
        phone = PhoneNumberUtils.formatNumber(phone);
      }
    }
    return phone;
  }

  /**
   * Extracts the message from the intent.
   * @param intent
   * @return
   */
  private String getMessage(Intent intent) {
    String msg = "";

    // For Google Voice, msg is stored in String extras.

    if (intent.getAction().equals("com.google.android.apps.googlevoice.SMS_RECEIVED")) {
      msg = intent.getExtras().getString(Texting.MESSAGE_TAG);

      // For Telephony, phone and msg are stored in PDUs.

    } else {
      Object[] pdus = (Object[]) intent.getExtras().get("pdus");
      for (Object pdu : pdus) {
        SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) pdu);
        msg = smsMsg.getMessageBody();
      }
    }
    return msg;
  }

  /**
   * Called if the Texting Component App is not running.
   * @param context
   * @param phone
   * @param msg
   */
  private void sendNotification(Context context, String phone, String msg) {
    Log.i(TAG, "sendingNotification " + phone + ":" + msg);

    // Get this app's name
    String packageName = context.getPackageName();
    Log.i(TAG, "Package name : " + packageName);

    Intent newIntent = null;

    // Will the activity name always be "Screen1"?   If not, we need to revise this
    try {
      String classname = packageName + ".Screen1";
      newIntent = new Intent(context, Class.forName(classname));
      newIntent.setAction(Intent.ACTION_MAIN);
      newIntent.addCategory(Intent.CATEGORY_LAUNCHER);

      // These flags seem to work, but maybe there's a way to improve on this?
      // NEW_TASK: the activity will become a new task on activity stack
      // SINGLE_TOP: activity won't be launched if already on top of stack
      newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

      // Create the Notification
      NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      Notification note = new Notification(R.drawable.sym_call_incoming, phone + " : " + msg , System.currentTimeMillis());
      note.flags |= Notification.FLAG_AUTO_CANCEL;
      note.defaults |= Notification.DEFAULT_SOUND;

      PendingIntent activity = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      note.setLatestEventInfo(context, "Sms from " + phone, msg, activity);
      note.number = Texting.getCachedMsgCount();
      nm.notify(null, NOTIFICATION_ID, note);
      Log.i(TAG, "Notification sent, classname: " + classname);

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private boolean isRepl(Context context) {
    try {
      String packageName = context.getPackageName();
      String classname = packageName + ".Screen1";
      Class appClass = Class.forName(classname);
      Class superClass = appClass.getSuperclass(); // This should be either Form or ReplForm
      if (superClass.equals(ReplForm.class))
        return true;
      else
        return false;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return false;             // If we loose, say we are not the repl
    }
  }
}
