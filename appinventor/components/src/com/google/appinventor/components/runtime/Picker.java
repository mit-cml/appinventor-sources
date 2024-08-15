// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.runtime.errors.StopBlocksExecution;
import com.google.appinventor.components.runtime.util.AnimationUtil;
import android.content.Intent;

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
    if (!BeforePicking()) {
      // If a block throws an exception here, we need the error to be displayed rather than opening
      // the ListPickerActivity so that it can be dealt with first.
      return;
    }
    if (requestCode == 0) { // only need to register once
      requestCode = container.$form().registerForActivityResult(this);
    }
    container.$context().startActivityForResult(getIntent(), requestCode);
    String openAnim = container.$form().OpenScreenAnimation();
    AnimationUtil.ApplyOpenScreenAnimation(container.$context(), openAnim);
  }

  // Functions

  /**
   * Opens the `%type%`, as though the user clicked on it.
   */
  @SimpleFunction(description = "Opens the %type%, as though the user clicked on it.")
  public void Open() {
    click();
  }

  // Events

  /**
   * Event to raise when the `%type%` is clicked or the picker is shown
   * using the {@link #Open()} method.  This event occurs before the picker is displayed, and
   * can be used to prepare the picker before it is shown.
   *
   * @return true if the picker should be opened, false if it should not.
   */
  @SimpleEvent
  public boolean BeforePicking() {
    Object result = EventDispatcher.dispatchFallibleEvent(this, "BeforePicking");
    return !(result instanceof StopBlocksExecution);
  }

  /**
   * Event to be raised after the `%type%` activity returns its
   * result and the properties have been filled in.
   */
  @SimpleEvent
  public void AfterPicking() {
    EventDispatcher.dispatchEvent(this, "AfterPicking");
  }
}
