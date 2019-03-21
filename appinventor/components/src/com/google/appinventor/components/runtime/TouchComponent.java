// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.PropertyTypeConstants;

import java.io.IOException;

/**
 * Underlying base class for click-based components, not directly accessible to Simple programmers.
 *
 */
@SimpleObject
public abstract class TouchComponent<T extends View> extends AndroidViewComponent
    implements View.OnTouchListener {

  protected T view;

  // Used for determining if visual feedback should be provided for components that have images
  private boolean showFeedback=true;

  /**
   * Creates a new TouchComponent component.
   *
   * @param container  container, component will be placed in
   */
  public TouchComponent(ComponentContainer container) {
    super(container);
  }

  protected void initToggle() {
      // Adds the component to its designated container
      container.$add(this);

      view.setOnTouchListener(this);

      Enabled(true);
  }

  @Override
  public View getView() {
    return view;
  }

    /**
   * Indicates when a component is touch down
   */
  @SimpleEvent(description = "Indicates that the button was pressed down.")
  public void TouchDown() {
    EventDispatcher.dispatchEvent(this, "TouchDown");
  }

  /**
   * Indicates when a component touch ends
   */
  @SimpleEvent(description = "Indicates that a button has been released.")
  public void TouchUp() {
    EventDispatcher.dispatchEvent(this, "TouchUp");
  }

  
    /**
     * If a custom background images is specified for the button, then it will lose the pressed
     * and disabled image effects; no visual feedback.
     * The approach below is to provide a visual feedback if and only if an image is assigned
     * to the button. In this situation, we overlay a gray background when pressed and
     * release when not-pressed.
     */
    @Override
    public boolean onTouch(View view, MotionEvent me) {
      //NOTE: We ALWAYS return false because we want to indicate that this listener has not
      //been consumed. Using this approach, other listeners (e.g. OnClick) can process as normal.
      if (me.getAction() == MotionEvent.ACTION_DOWN) {
        //button pressed, provide visual feedback AND return false
        if (ShowFeedback() && (AppInventorCompatActivity.isClassicMode() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
          view.getBackground().setAlpha(70); // translucent
          view.invalidate();
        }
        TouchDown();
      } else if (me.getAction() == MotionEvent.ACTION_UP ||
              me.getAction() == MotionEvent.ACTION_CANCEL) {
        //button released, set button back to normal AND return false
        if (ShowFeedback() && (AppInventorCompatActivity.isClassicMode() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
          view.getBackground().setAlpha(255); // opaque
          view.invalidate();
        }
        TouchUp();
      }

      return false;
    }

    /**
   * Returns true if the component is active and interacatable.
   *
   * @return  {@code true} indicates enabled, {@code false} disabled
   */
  @SimpleProperty(
      category = PropertyCategory.BEHAVIOR,
      description = "If set, user can tap check box to cause action.")
  public boolean Enabled() {
    return view.isEnabled();
  }

  /**
   * Specifies whether the component should be active and interactable.
   *
   * @param enabled  {@code true} for enabled, {@code false} disabled
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty
  public void Enabled(boolean enabled) {
    view.setEnabled(enabled);
    view.invalidate();
  }

  
  /**
   * Specifies if a visual feedback should be shown when a component with an assigned image
   * is pressed.
   *
   * @param showFeedback  {@code true} enables showing feedback,
   *                 {@code false} disables it
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
          defaultValue = "True")
  @SimpleProperty(description = "Specifies if a visual feedback should be shown " +
          " for a button that as an image as background.")

  public void ShowFeedback(boolean showFeedback) {
    this.showFeedback =showFeedback;
  }

    /**
     * Returns true if the component should provide visual feedwback when it is pressed
     * and there is an image assigned.
     *
     * @return {@code true} indicates visual feedback will be shown,
     *                 {@code false} visual feedback will not be shown
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Returns the button's visual feedback state")
    public boolean ShowFeedback() {
        return showFeedback;
    }
}