// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import android.content.Intent;
import android.content.ActivityNotFoundException;

/**
 * Abstract superclass for all of the "Picker" components.
 *
 */
@SimpleObject
public abstract class Picker extends ButtonBase implements ActivityResultListener {
  protected final ComponentContainer container;

  /* Used to identify the call to startActivityForResult. Will be passed back into the
  resultReturned() callback method. */
  protected int requestCode;

  public Picker(ComponentContainer container) {
    super(container);
    this.container = container;
  }

  /**
   *  Provides the Intent used to launch the picker activity.
   */
  protected abstract Intent getIntent();

  @Override
  public void click() {
    BeforePicking();
    if (requestCode == 0) { // only need to register once
      requestCode = container.$form().registerForActivityResult(this);
    }
    try {
      container.$context().startActivityForResult(getIntent(), requestCode);
      String openAnim = container.$form().getOpenAnimType();
      AnimationUtil.ApplyOpenScreenAnimation(container.$context(), openAnim);
    } catch (ActivityNotFoundException e) {
      container.$form().dispatchErrorOccurredEvent(this, "getIntent",
          ErrorMessages.ERROR_ACTIVITY_STARTER_NO_CORRESPONDING_ACTIVITY);
    }
  }

  // Functions

  /**
   * Opens the picker, as though the user clicked on it.
   */
  @SimpleFunction
  public void Open() {
    click();
  }

  // Events

  /**
   * Simple event to raise when the component is clicked but before the
   * picker activity is started.
   */
  @SimpleEvent
  public void BeforePicking() {
    EventDispatcher.dispatchEvent(this, "BeforePicking");
  }

  /**
   * Simple event to be raised after the picker activity returns its
   * result and the properties have been filled in.
   */
  @SimpleEvent
  public void AfterPicking() {
    EventDispatcher.dispatchEvent(this, "AfterPicking");
  }
}
