// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import android.view.MotionEvent;
import android.view.View;

/**
 * Button with the ability to launch events on initialization, focus
 * change, or a user click.  It is implemented using
 * {@link android.widget.Button}.
 *
 */
@DesignerComponent(version = YaVersion.BUTTON_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Button with the ability to detect clicks.  Many aspects " +
    "of its appearance can be changed, as well as whether it is clickable " +
    "(<code>Enabled</code>), can be changed in the Designer or in the Blocks " +
    "Editor.")
@SimpleObject
public final class Button extends ButtonBase {

  // Used for determining if this button is Press/Release or Click behavior.
  private boolean isPressRelease=false;

  /**
   * Creates a new Button component.
   *
   * @param container container, component will be placed in
   */
  public Button(ComponentContainer container) {
    super(container);
  }

  /**
   * Specifies if this is a Press/Release type button or a ragular one.
   *
   * @param isPressRelease  {@code true} treats onTouch as Press/Release event
   *                        {@code false} treats onTouch as Click pass through
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "False")
  @SimpleProperty(description = "Specifies if this button should be treated " +
          "as a button that can be pressed and released.")

  public void IsPressRelease(boolean isPressRelease) {
    this.isPressRelease=isPressRelease;
  }

  /**
   * Returns true if the button is of Press/Release type.
   *
   * @return {@code true} indicates button is of Press/Release type
   *         {@code false} indicates button is simple Click type
   */
  @SimpleProperty(
          category = PropertyCategory.BEHAVIOR,
          description = "Returns the button's usage type.")
  public boolean IsPressRelease() {
      return isPressRelease;
  }

  /**
   * If this button instance isPressRelease type, dispatch Press Release events
   */
  @Override
  public boolean onTouch(View view, MotionEvent me)
  {
     if (!IsPressRelease()) {
       return super.onTouch(view, me);
     } else {
       super.onTouch(view, me);
       switch ( me.getAction() ) {
          case MotionEvent.ACTION_DOWN:
              Press();
              break;
          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_CANCEL:
              Release();
              break;
        }
     }
     return true;
  }

  /**
   * Indicates a user pressed down on the button.
   */
  @SimpleEvent
  public void Press() {
    EventDispatcher.dispatchEvent(this, "Press");
  }

  /**
   * Indicates a user released the button.
   */
  @SimpleEvent
  public void Release() {
    EventDispatcher.dispatchEvent(this, "Release");
  }

 @Override
  public void click() {
    // Call the users Click event handler. Note that we distinguish the click() abstract method
    // implementation from the Click() event handler method.
    Click();
  }

  /**
   * Indicates a user has clicked on the button.
   */
  @SimpleEvent
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  @Override
  public boolean longClick() {
    // Call the users Click event handler. Note that we distinguish the longclick() abstract method
    // implementation from the LongClick() event handler method.
    return LongClick();
  }

  /**
   * Indicates a user has long clicked on the button.
   */
  @SimpleEvent
  public boolean LongClick() {
    return EventDispatcher.dispatchEvent(this, "LongClick");
  }
}
