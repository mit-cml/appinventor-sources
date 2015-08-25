// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimplePropertyCopier;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.view.View;

/**
 * Underlying base class for all components with views; not accessible to Simple programmers.
 * <p>
 * Provides implementations for standard properties and events.
 *
 */
@SimpleObject
public abstract class AndroidViewComponent extends VisibleComponent {

  protected final ComponentContainer container;

  private int percentWidthHolder = LENGTH_UNKNOWN;
  private int percentHeightHolder = LENGTH_UNKNOWN;
  private int lastSetWidth = LENGTH_UNKNOWN;
  private int lastSetHeight = LENGTH_UNKNOWN;

  private int column = ComponentConstants.DEFAULT_ROW_COLUMN;
  private int row = ComponentConstants.DEFAULT_ROW_COLUMN;

  /**
   * Creates a new AndroidViewComponent.
   *
   * @param container  container, component will be placed in
   */
  protected AndroidViewComponent(ComponentContainer container) {
    this.container = container;
  }

  /**
   * Returns the {@link View} that is displayed in the UI.
   */
  public abstract View getView();

  /**
   * Returns true iff the component is visible.
   * @return  true iff the component is visible
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public boolean Visible() {
    return getView().getVisibility() == View.VISIBLE;
  }

  /**
   * Specifies whether the component should be visible on the screen.  Value is true if the 
   * component is showing and false if hidden.
   * @param  visibility desired state
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY,
      defaultValue = "True")
  @SimpleProperty(description = "Specifies whether the component should be visible on the screen. "
      + "Value is true if the component is showing and false if hidden.")
  public void Visible(Boolean visibility) {
    // The principle of least astonishment suggests we not offer the
    // Android option INVISIBLE.
    getView().setVisibility(visibility ? View.VISIBLE : View.GONE);
  }

  /**
   * Returns the component's horizontal width, measured in pixels.
   *
   * @return  width in pixels
   */
  @Override
  @SimpleProperty
  public int Width() {
    int zWidth = (int)(getView().getWidth() / container.$form().deviceDensity());
//    System.err.println("AndroidViewComponent: Width() Called, returning " + zWidth);
    return zWidth;
  }

  /**
   * Specifies the component's horizontal width, measured in pixels.
   *
   * @param  width in pixels
   */
  @Override
  @SimpleProperty
  public void Width(int width) {
    container.setChildWidth(this, width);
    lastSetWidth = width;
    if (width <= Component.LENGTH_PERCENT_TAG)
      container.$form().registerPercentLength(this, width, Form.PercentStorageRecord.Dim.WIDTH);
  }

  /**
   * Specifies the component's horizontal width as a percentage
   * of the Width of its parent Component.
   *
   * @param width in percent
   */

  @Override
  @SimpleProperty
  public void WidthPercent(int pCent) {
    if (pCent < 0 || pCent > 100) {
      container.$form().dispatchErrorOccurredEvent(this, "WidthPercent",
        ErrorMessages.ERROR_BAD_PERCENT, pCent);
      return;
    }
    int v = -pCent + Component.LENGTH_PERCENT_TAG;
    Width(v);
  }

  public void setLastWidth(int width) {
//    System.err.println(this + " percentWidthHolder being set to " + width);
    percentWidthHolder = width;
  }

  public int getSetWidth() {
//    System.err.println(this + " getSetWidth() percentWidthHolder = " + percentWidthHolder);
    if (percentWidthHolder == LENGTH_UNKNOWN) {
      return Width();           // best guess...
    } else {
      return percentWidthHolder;
    }
  }

  public void setLastHeight(int height) {
//    System.err.println(this + " percentHeightHolder being set to " + height);
    percentHeightHolder = height;
  }

  public int getSetHeight() {
//    System.err.println(this + " getSetHeight() percentHeightHolder = " + percentHeightHolder);
    if (percentHeightHolder == LENGTH_UNKNOWN) {
      return Height();           // best guess...
    } else {
      return percentHeightHolder;
    }
  }

  /**
   * Copy the width from another component to this one.  Note that we don't use
   * the getter method to get the property value from the source because the
   * getter returns the computed width whereas we want the width that it was
   * last set to.  That's because we want to preserve values like
   * LENGTH_FILL_PARENT and LENGTH_PREFERRED
   *
   * @param sourceComponent the component to copy from
   */
  @SimplePropertyCopier
  public void CopyWidth(AndroidViewComponent sourceComponent) {
    Width(sourceComponent.lastSetWidth);
  }

  /**
   * Returns the component's vertical height, measured in pixels.
   *
   * @return  height in pixels
   */
  @Override
  @SimpleProperty
  public int Height() {
    return (int)(getView().getHeight() / container.$form().deviceDensity());
  }

  /**
   * Specifies the component's vertical height, measured in pixels.
   *
   * @param  height in pixels
   */
  @Override
  @SimpleProperty
  public void Height(int height) {
    container.setChildHeight(this, height);
    lastSetHeight = height;
    if (height <= Component.LENGTH_PERCENT_TAG)
      container.$form().registerPercentLength(this, height, Form.PercentStorageRecord.Dim.HEIGHT);
  }

  /**
   * Specifies the component's vertical height as a percentage
   * of the height of its parent Component.
   *
   * @param height in percent
   */

  @Override
  @SimpleProperty
  public void HeightPercent(int pCent) {
    if (pCent < 0 || pCent > 100) {
      container.$form().dispatchErrorOccurredEvent(this, "HeightPercent",
        ErrorMessages.ERROR_BAD_PERCENT, pCent);
      return;
    }
    int v = -pCent + Component.LENGTH_PERCENT_TAG;
    Height(v);
  }

  /**
   * Copy the height from another component to this one.  Note that we don't use
   * the getter method to get the property value from the source because the
   * getter returns the computed width whereas we want the width that it was
   * last set to.  That's because we want to preserve values like
   * LENGTH_FILL_PARENT and LENGTH_PREFERRED
   *
   * @param sourceComponent the component to copy from
   */
  @SimplePropertyCopier
  public void CopyHeight(AndroidViewComponent sourceComponent) {
    Height(sourceComponent.lastSetHeight);
  }

  /**
   * Column property getter method.
   *
   * @return  column property used by the table arrangement
   */
  @SimpleProperty(userVisible = false)
  public int Column() {
    return column;
  }

  /**
   * Column property setter method.
   *
   * @param column  column property used by the table arrangement
   */
  @SimpleProperty(userVisible = false)
  public void Column(int column) {
    this.column = column;
  }

  /**
   * Row property getter method.
   *
   * @return  row property used by the table arrangement
   */
  @SimpleProperty(userVisible = false)
  public int Row() {
    return row;
  }

  /**
   * Row property setter method.
   *
   * @param row  row property used by the table arrangement
   */
  @SimpleProperty(userVisible = false)
  public void Row(int row) {
    this.row = row;
  }

  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return container.$form();
  }
}
