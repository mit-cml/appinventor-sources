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
import com.google.appinventor.components.runtime.util.PhoneCallUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Component for making a phone call to a programatically-specified number.
 *
 * TODO(markf): Note that the initial carrier for Android phones only supports 3 participants
 *              in a conference call, so that's all that the current implementation of this
 *              component supports.  In the future we can generalize this to more participants.
 *
 * @author markf@google.com (Mark Friedman)
 */
@DesignerComponent(version = YaVersion.PHONECALL_COMPONENT_VERSION,
    description = "<p>A non-visible component that makes a phone call to " +
    "the number specified in the <code>PhoneNumber</code> property, which " +
    "can be set either in the Designer or Blocks Editor. The component " +
    "has a <code>MakePhoneCall</code> method, enabling the program to launch " +
    "a phone call.</p>" +
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
    iconName = "images/phoneCall.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.CALL_PHONE, android.permission.READ_PHONE_STATE, android.permission.PROCESS_OUTGOING_CALLS")
public class PhoneCall extends AndroidNonvisibleComponent implements Component, OnDestroyListener {

  private String phoneNumber;
  private final Context context;
  private final CallStateReceiver callStateReceiver;
  private static final String LOG_TAG = "PhoneCallComponent";

  /**
   * Creates a Phone Call component.
   *
   * @param container container, component will be placed in
   */
  public PhoneCall(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
    form.registerForOnDestroy(this);
    PhoneNumber("");
    callStateReceiver = new CallStateReceiver();
    registerCallStateMonitor();
  }

  /**
   * PhoneNumber property getter method.
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR)
  public String PhoneNumber() {
    return phoneNumber;
  }

  /**
   * PhoneNumber property setter method: sets a phone number to call.
   *
   * @param phoneNumber a phone number to call
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void PhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * Makes a phone call using the number in the PhoneNumber property.
   */
  @SimpleFunction
  public void MakePhoneCall() {
    PhoneCallUtil.makePhoneCall(context, phoneNumber);
  }
  
  /**
   * Event indicating that a phone call has started.
   * mode "out":outgoing call; "in":incoming call
   * 
   * @param mode "out":outgoing call; "in":incoming call
   * @param phoneNumber incoming call phone number
   */
  @SimpleEvent
  public void PhoneCallStarted(String mode, String phoneNumber) {
    // invoke the application's "PhoneCallStarted" event handler.
    EventDispatcher.dispatchEvent(this, "PhoneCallStarted", mode, phoneNumber);
  }
    
  /**
   * Event indicating that a phone call has ended.
   * mode "out":outgoing call; "in":incoming call
   * 
   * @param mode "out":outgoing call; "in":incoming call
   * @param phoneNumber ended call phone number
   */
  @SimpleEvent
  public void PhoneCallEnded(String mode, String phoneNumber) {
    // invoke the application's "PhoneCallEnded" event handler.
    EventDispatcher.dispatchEvent(this, "PhoneCallEnded", mode, phoneNumber);
  }
  
  /**
   * BroadcastReceiver for incomming/outgoing phonecall state changes
   *
   */
  private class CallStateReceiver extends BroadcastReceiver {
    private String mode;
    private String number;
    public CallStateReceiver() {
      mode = "";
      number = "";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)){
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        Toast.makeText(context, state, Toast.LENGTH_LONG).show();
        if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
          // Incoming call
          mode = "in";
          number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
          Log.d(LOG_TAG, "IncommingCall: " + number);
          PhoneCallStarted(mode, number);
        }else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
          // Call offhook
        }else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)){
          // Incomming/Outgoing Call ends          
          Log.d(LOG_TAG, "EndCall: " + number);
          PhoneCallEnded(mode, number);
        }
      }else if(Intent.ACTION_NEW_OUTGOING_CALL.equals(action)){
        // Outgoing call
        Toast.makeText(context, "Outgoing", Toast.LENGTH_LONG).show();
        mode = "out";
        number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(LOG_TAG, "OutgoingCall: " + number);
        PhoneCallStarted(mode,number);
      }
    }
  }
    
  /**
   * Registers phonecall state monitor
   */
  private void registerCallStateMonitor(){
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
    intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
    context.registerReceiver(callStateReceiver, intentFilter);
  }
      
  /**
   * Unregisters phonecall state monitor
   */
  private void unregisterCallStateMonitor(){
    context.unregisterReceiver(callStateReceiver);
  }

  @Override
  public void onDestroy() {
    // TODO Auto-generated method stub
    unregisterCallStateMonitor();
  }
}
