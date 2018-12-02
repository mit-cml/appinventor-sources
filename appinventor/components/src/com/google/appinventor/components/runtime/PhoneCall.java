// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.PhoneCallUtil;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

/**
 * Component for making a phone call to a programatically-specified number.
 *
 * TODO(markf): Note that the initial carrier for Android phones only supports 3 participants
 *              in a conference call, so that's all that the current implementation of this
 *              component supports.  In the future we can generalize this to more participants.
 *
 * @author markf@google.com (Mark Friedman)
 * @author rekygx@gmail.com (Xian Gao)
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
  private boolean havePermission = false;

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
  }

  @SuppressWarnings({"unused"})
  public void Initialize() {
    form.askPermission(Manifest.permission.PROCESS_OUTGOING_CALLS, new PermissionResultHandler() {
      @Override
      public void HandlePermissionResponse(String permission, boolean granted) {
        if (granted) {
          registerCallStateMonitor();
        } else {
          form.dispatchPermissionDeniedEvent(PhoneCall.this, "Initialize",
              Manifest.permission.PROCESS_OUTGOING_CALLS);
        }
      }
    });
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
    // Check that we have permission and ask for it if we don't
    if (!havePermission) {
      form.askPermission(Manifest.permission.CALL_PHONE,
        new PermissionResultHandler() {
          @Override
          public void HandlePermissionResponse(String permission, boolean granted) {
            if (granted) {
              PhoneCall.this.havePermission = true;
              PhoneCall.this.MakePhoneCall();
            } else {
              form.dispatchPermissionDeniedEvent(PhoneCall.this, "MakePhoneCall",
                  Manifest.permission.READ_EXTERNAL_STORAGE);
            }
          }
        });
    } else {
      PhoneCallUtil.makePhoneCall(context, phoneNumber);
    }
  }

  /**
   * Event indicating that a phone call has started.
   * status: 1:incoming call is ringing; 2:outgoing call is dialled.
   *
   * @param status 1:incoming call is ringing; 2:outgoing call is dialled.
   * @param phoneNumber incoming/outgoing call phone number
   */
  @SimpleEvent(
      description =
          "Event indicating that a phonecall has started." +
              " If status is 1, incoming call is ringing; " +
              "if status is 2, outgoing call is dialled. " +
              "phoneNumber is the incoming/outgoing phone number.")
  public void PhoneCallStarted(int status, String phoneNumber) {
    // invoke the application's "PhoneCallStarted" event handler.
    EventDispatcher.dispatchEvent(this, "PhoneCallStarted", status, phoneNumber);
  }

  /**
   * Event indicating that a phone call has ended.
   * status: 1:incoming call is missed or rejected; 2:incoming call is answered before hanging up; 3:Outgoing call is hung up.
   *
   * @param status 1:incoming call is missed or rejected; 2:incoming call is answered before hanging up; 3:Outgoing call is hung up.
   * @param phoneNumber ended call phone number
   */
  @SimpleEvent(
      description =
          "Event indicating that a phone call has ended. " +
              "If status is 1, incoming call is missed or rejected; " +
              "if status is 2, incoming call is answered before hanging up; " +
              "if status is 3, outgoing call is hung up. " +
              "phoneNumber is the ended call phone number.")
  public void PhoneCallEnded(int status, String phoneNumber) {
    // invoke the application's "PhoneCallEnded" event handler.
    EventDispatcher.dispatchEvent(this, "PhoneCallEnded", status, phoneNumber);
  }

  /**
   * Event indicating that an incoming phone call is answered.
   *
   * @param phoneNumber incoming call phone number
   */
  @SimpleEvent(
      description =
          "Event indicating that an incoming phone call is answered. " +
              "phoneNumber is the incoming call phone number.")
  public void IncomingCallAnswered(String phoneNumber) {
    // invoke the application's "IncomingCallAnswered" event handler.
    EventDispatcher.dispatchEvent(this, "IncomingCallAnswered", phoneNumber);
  }

  /**
   * BroadcastReceiver for incomming/outgoing phonecall state changes
   *
   */
  private class CallStateReceiver extends BroadcastReceiver {
    private int status; // 0:undetermined, 1:incoming ringed, 2:outgoing dialled, 3: incoming answered
    private String number; // phone call number
    public CallStateReceiver() {
      status = 0;
      number = "";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)){
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
          // Incoming call rings
          status = 1;
          number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
          PhoneCallStarted(1, number);
        }else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
          // Call off-hook
          if(status == 1){
            // Incoming call answered
            status = 3;
            IncomingCallAnswered(number);
          }
        }else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)){
          // Incomming/Outgoing Call ends
          if(status == 1){
            // Incoming Missed or Rejected
            PhoneCallEnded(1, number);
          }else if(status == 3){
            // Incoming Answer Ended
            PhoneCallEnded(2, number);
          }else if(status == 2){
            // Outgoing Ended
            PhoneCallEnded(3, number);
          }
          status = 0;
          number = "";
        }
      }else if(Intent.ACTION_NEW_OUTGOING_CALL.equals(action)){
        // Outgoing call dialled
        status = 2;
        number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        PhoneCallStarted(2, number);
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
    unregisterCallStateMonitor();
  }
}
