// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/*
 * This is revised from here: https://github.com/cicada-dev
 * and was original published under Apache 2.0 license.
 *
 */
package com.google.appinventor.components.runtime.util;

import java.util.List;

import android.support.v4.app.NotificationCompat;
import com.google.appinventor.components.common.ComponentConstants;
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
import android.telephony.SmsMessage;
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

    int receivingEnabled = Texting.isReceivingEnabled(context);

    // If isReceivingEnabled == 0, then we don't want anything EVER
    if (receivingEnabled == ComponentConstants.TEXT_RECEIVING_OFF) {
      Log.i(TAG, context.getApplicationInfo().packageName +
        " Receiving is not enabled, ignoring message.");
      return;
    }

    // If we get this far, receiving is enabled for either FOREGROUND or ALWAYS

    if (((receivingEnabled == ComponentConstants.TEXT_RECEIVING_FOREGROUND) ||
        isRepl(context)) && !Texting.isRunning()) {
      Log.i(TAG, context.getApplicationInfo().packageName +
        " Texting isn't running, and either receivingEnabled is FOREGROUND or we are the repl.");
      return;
    }

    // If we get this far, we want the message, either foreground or background

    Texting.handledReceivedMessage(context, phone, msg);
    if (Texting.isRunning()) {  // We are running in the foreground
      Log.i(TAG, context.getApplicationInfo().packageName +
        " App in Foreground, delivering message.");
    } else {
      Log.i(TAG, context.getApplicationInfo().packageName +
        " Texting isn't running, but receivingEnabled == 2, sending notification.");
      sendNotification(context, phone, msg);
    }

  }

  /**
   * Extracts the phone number from the intent and formats it.
   * @param intent
   * @return
   */
  private String getPhoneNumber(Intent intent) {
    String phone = "";

    try {
      if (intent.getAction().equals("com.google.android.apps.googlevoice.SMS_RECEIVED")) {
        // For Google Voice, phone and msg are stored in String extras. Pretty them up

        phone = intent.getExtras().getString(Texting.PHONE_NUMBER_TAG);
        phone = PhoneNumberUtils.formatNumber(phone);

      } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_KITKAT) {
        // On KitKat or higher, use the convience getMessageFromIntent method.
        List<SmsMessage> messages = KitkatUtil.getMessagesFromIntent(intent);
        for (SmsMessage smsMsg : messages) {
          if (smsMsg != null) {
            // getOriginatingAddress() can throw a NPE if its wrapped message is null, but there
            // isn't an API to check whether this is the case.
            phone = smsMsg.getOriginatingAddress();
            if (SdkLevel.getLevel() >= SdkLevel.LEVEL_LOLLIPOP) {
              phone = LollipopUtil.formatNumber(phone);
            } else {
              phone = PhoneNumberUtils.formatNumber(phone);
            }
          }
        }
      } else {
        // On SDK older than KitKat, we have to manually process the PDUs.
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        for (Object pdu : pdus) {
          SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) pdu);
          phone = smsMsg.getOriginatingAddress();
          phone = PhoneNumberUtils.formatNumber(phone);
        }
      }
    } catch(NullPointerException e) {
      Log.w(TAG, "Unable to retrieve originating address from SmsMessage", e);
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

    try {
      if (intent.getAction().equals("com.google.android.apps.googlevoice.SMS_RECEIVED")) {
        // For Google Voice, msg is stored in String extras.

        msg = intent.getExtras().getString(Texting.MESSAGE_TAG);

      } else if (SdkLevel.getLevel() >= SdkLevel.LEVEL_KITKAT) {
        // On KitKat or higher, use the convience getMessageFromIntent method.
        StringBuilder sb = new StringBuilder();
        List<SmsMessage> messages = KitkatUtil.getMessagesFromIntent(intent);
        for (SmsMessage smsMsg : messages) {
          if (smsMsg != null) {
            sb.append(smsMsg.getMessageBody());
          }
        }
        msg = sb.toString();
      } else {
        // On SDK older than KitKat, we have to manually process the PDUs.
        StringBuilder sb = new StringBuilder();
        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        for (Object pdu : pdus) {
          SmsMessage smsMsg = SmsMessage.createFromPdu((byte[]) pdu);
          sb.append(smsMsg.getMessageBody());
        }
        msg = sb.toString();
      }
    } catch(NullPointerException e) {
      // getMessageBody() can throw a NPE if its wrapped message is null, but there isn't an
      // API to check whether this is the case.
      Log.w(TAG, "Unable to retrieve message body from SmsMessage", e);
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
      PendingIntent activity = PendingIntent.getActivity(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      Notification note = new NotificationCompat.Builder(context)
          .setSmallIcon(R.drawable.sym_call_incoming)
          .setTicker(phone + " : " + msg)
          .setWhen(System.currentTimeMillis())
          .setAutoCancel(true)
          .setDefaults(Notification.DEFAULT_SOUND)
          .setContentTitle("Sms from " + phone)
          .setContentText(msg)
          .setContentIntent(activity)
          .setNumber(Texting.getCachedMsgCount())
          .build();

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
