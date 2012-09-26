// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.gsm.SmsManager;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

/**
 * A component capable of sending and receiving text messages via SMS.

 * @author markf@google.com (Mark Friedman)
 *
 */
@DesignerComponent(version = YaVersion.TEXTING_COMPONENT_VERSION,
    description = "<p>A component that will, when the <code>SendMessage</code> " +
    " method is called, send the text message " +
    "specified in the <code>Message</code> property to the phone number " +
    "specified in the <code>PhoneNumber</code> property. " +
    "<p>This component can also receive text messages unless the " +
    "<code>ReceivingEnabled</code> property is False.  When a message " +
    "arrives, the <code>MessageReceived</code> event is raised and provides " +
    "the sending number and message.</p>" +
    "<p>Often, this component is used with the <code>ContactPicker</code> " +
    "component, which lets the user select a contact from the ones stored " +
    "on the phone and sets the <code>PhoneNumber</code> property to the " +
    "contact's phone number.</p>" +
    "<p>To directly specify the phone number (e.g., 650-555-1212), set " +
    "the <code>PhoneNumber</code> property to a Text with the specified " +
    "digits (e.g., \"6505551212\").  Dashes, dots, and parentheses may be " +
    "included (e.g., \"(650)-555-1212\") but will be ignored; spaces may " +
    "not be included.</p>",
    category = ComponentCategory.SOCIAL,
    nonVisible = true,
    iconName = "images/texting.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.RECEIVE_SMS, android.permission.SEND_SMS")
public class Texting extends AndroidNonvisibleComponent
    implements Component, OnDestroyListener, Deleteable {

  /**
   * Handles the SMS reception
   */
  class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      SmsMessage[] messages = getMessagesFromIntent(intent);
      SmsMessage message = messages[0];
      if (message != null) {
        String from = message.getOriginatingAddress();
        String messageText = message.getMessageBody();
        MessageReceived(from, messageText);
      } else {
        Log.i("Simple", "Sms message suppposedly received but with no actual content.");
      }
    }
  }

  // Provides an event for SMS reception

  // Indicates whether the SMS receiver is running or not
  private boolean receivingEnabled;
  private SmsManager smsManager;

  // The phone number to send the text message to.
  private String phoneNumber;
  // The message to send
  private String message;

  private SmsReceiver smsReceiver;


  /**
   * Creates a new TextMessage component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public Texting(ComponentContainer container) {
    super(container.$form());
    IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    smsReceiver = new SmsReceiver();
    container.$context().registerReceiver(smsReceiver, intentFilter);
    form.registerForOnDestroy(this);
    Log.d("Simple", "Texting constructor");
    smsManager = SmsManager.getDefault();
    PhoneNumber("");
    receivingEnabled = true;
  }

  /**
   * Sets the phone number to send the text message to when the SendMessage function is called.
   *
   * @param phoneNumber a phone number to call
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public void PhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Get the phone number that the message will be sent to when the SendMessage function is called.
   */
  @SimpleProperty
  public String PhoneNumber() {
    return phoneNumber;
  }

  /**
   * Sets the text message to send when the SendMessage function is called.
   *
   * @param message the message to send when the SendMessage function is called.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public void Message(String message) {
    this.message = message;
  }

  /**
   * Get the message that will be sent when the SendMessage function is called.
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
    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
  }

  /**
   * Event that's raised when a new text message is received by the phone.
   * @param number the phone number that the text message was sent from.
   * @param messageText the text of the message.
   */
  @SimpleEvent
  public void MessageReceived(String number, String messageText) {
    // TODO(markf): maybe we should unregister and re-register the SmsReceiver based on the
    // receivingEnabled setting rather than just checking here.
    if (receivingEnabled) {
      Log.d("Simple", "MessageReceived");
      EventDispatcher.dispatchEvent(this, "MessageReceived", number, messageText);
    }
  }


  /**
   * Gets whether you want the {@link #MessageReceived(String,String)} event to
   * get run when a new text message is received.
   *
   * @return 'true' or 'false' depending on whether you want the
   *          {@link #MessageReceived(String,String)} event to get run when a
   *          new text message is received.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public boolean ReceivingEnabled() {
    return receivingEnabled;
  }

  /**
   * Sets whether you want the {@link #MessageReceived(String,String)} event to
   * get run when a new text message is received.
   *
   * @param enabled  Set to 'true' or 'false' depending on whether you want the
   *                 {@link #MessageReceived(String,String)} event to get run
   *                 when a new text message is received.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
                    defaultValue = "True")
  @SimpleProperty()
  public void ReceivingEnabled(boolean enabled) {
    this.receivingEnabled = enabled;
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

  // OnDestroyListener implementation

  @Override
  public void onDestroy() {
    prepareToDie();
  }

  // Deleteable implementation

  @Override
  public void onDelete() {
    prepareToDie();
  }

  private void prepareToDie() {
    form.unregisterReceiver(smsReceiver);
    smsReceiver = null;
  }
}
