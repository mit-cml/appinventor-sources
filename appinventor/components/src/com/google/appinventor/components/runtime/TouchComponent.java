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
public abstract class TouchComponent<T extends View> extends AndroidViewComponent {

  protected T view;

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
  }

  @Override
  public View getView() {
    return view;
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
}