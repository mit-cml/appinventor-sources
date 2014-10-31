// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.GingerbreadUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * Controller for Near Field Communication
 *
 */
@DesignerComponent(version = YaVersion.NEARFIELD_COMPONENT_VERSION,
    description = "<p>Non-visible component to provide NFC capabilities.  " +
    "For now this component supports the reading and writing of text tags only " +
    "(if supported by the device)</p>" +
    "<p>In order to read and write text tags, the component must have its " +
    "<code>ReadMode</code> property set to True or False respectively.</p>",
    category = ComponentCategory.SENSORS,
    nonVisible = true,
    iconName = "images/nearfield.png")

@SimpleObject
@UsesPermissions(permissionNames = "android.permission.NFC")
public class NearField extends AndroidNonvisibleComponent
implements OnStopListener, OnResumeListener, OnPauseListener, OnNewIntentListener, Deleteable {
  private static final String TAG = "nearfield";
  private Activity activity;

  private NfcAdapter nfcAdapter;
  private boolean readMode = true;
  private int writeType;
  private String tagContent = "";
  private String textToWrite = "";

  /* Used to identify the call to startActivityForResult. Will be passed back into the
  resultReturned() callback method. */
  protected int requestCode;

  /**
   * Creates a new NearField component
   * @param container  ignored (because this is a non-visible component)
   */
  public NearField(ComponentContainer container) {
    super(container.$form());
    activity = container.$context();
    writeType = 1;
    nfcAdapter = (SdkLevel.getLevel() >= SdkLevel.LEVEL_GINGERBREAD)
        ? GingerbreadUtil.newNfcAdapter(activity)
        : null;
    // register with the forms to that OnResume and OnNewIntent
    // messages get sent to this component
    form.registerForOnResume(this);
    form.registerForOnNewIntent(this);
    form.registerForOnPause(this);
    Log.d(TAG, "Nearfield component created");
  }

  // Events

  /**
   * Indicates that a new tag has been detected.
   * Currently this is only a plain text tag, as specified in the
   * manifest.  See Compiler.java.
   */
  @SimpleEvent
  public void TagRead(String message) {
    Log.d(TAG, "Tag read: got message " + message);
    tagContent = message;
    EventDispatcher.dispatchEvent(this, "TagRead", message);
  }

  @SimpleEvent
  public void TagWritten() {
    Log.d(TAG, "Tag written: " + textToWrite);
    EventDispatcher.dispatchEvent(this, "TagWritten");
  }

  // Properties

  /**
   * Returns the content of the most recently received tag.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)//what does this mean?
  public String LastMessage() {
    Log.d(TAG, "String message method stared");
    return tagContent;
  }

  /**
   * Returns true if in read mode, false if in write mode.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public boolean ReadMode() {
    Log.d(TAG, "boolean method stared");
    return readMode;
  }

  /**
   * Returns the content of the text that's going to be written to the tag.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)//what does this mean?
  public String TextToWrite() {
    Log.d(TAG, "String message method stared");
    return textToWrite;
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public int WriteType() {
    return writeType;
  }

  /**
   * Allows the user to set read/write mode
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void ReadMode(boolean newMode) {
    Log.d(TAG, "Read mode set to" + newMode);
    readMode = newMode;
    if(!readMode && SdkLevel.getLevel() >= SdkLevel.LEVEL_GINGERBREAD){
      GingerbreadUtil.enableNFCWriteMode(activity, nfcAdapter, textToWrite);
    }
  }

  /**
   * Allows the user to set the content of the tag that will be written
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void TextToWrite(String newText) {
    Log.d(TAG, "Text to write set to" + newText);
    textToWrite = newText;
    if(!readMode && writeType == 1){
      if(SdkLevel.getLevel() >= SdkLevel.LEVEL_GINGERBREAD){
        GingerbreadUtil.enableNFCWriteMode(activity, nfcAdapter, textToWrite);
      }
    }
  }

  // When NFC is detected, the form's onNewIntent method is triggered (because of the
  // specification in the manifest.  The form then sends that intent here.
  @Override
  public void onNewIntent(Intent intent) {
    Log.d(TAG, "Nearfield on onNewIntent.  Intent is: " + intent);
    resolveIntent(intent);
  }

  // TODO: Re-enable NFC communication if it had been disabled
  @Override
  public void onResume() {
    Intent intent = activity.getIntent();
    Log.d(TAG, "Nearfield on onResume.  Intent is: " + intent);
  }

  void resolveIntent(Intent intent) {
    Log.d(TAG, "resolve intent. Intent is: " + intent);
    // Parse the intent
    if(SdkLevel.getLevel() >= SdkLevel.LEVEL_GINGERBREAD){
      GingerbreadUtil.resolveNFCIntent(intent, this);
    }
  }

  // TODO: Disable NFC communication in onPause and onDelete
  // and restore it in onResume

  public void onPause() {
    Log.d(TAG, "OnPause method started.");
    if (nfcAdapter != null) {
      GingerbreadUtil.disableNFCAdapter(activity, nfcAdapter);
    }
    //nfcAdapter.disableForegroundDispatch(activity);
  }

  @Override
  public void onDelete() {
    // TODO Auto-generated method stub
    // need to delete the nearfieldActivity

  }

  @Override
  public void onStop() {
    // TODO Auto-generated method stub
  }
}
