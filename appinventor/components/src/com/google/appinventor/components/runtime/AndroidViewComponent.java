// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimplePropertyCopier;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.ErrorMessages;

/**
 * Underlying base class for all components with views; not accessible to Simple programmers.
 *
 * <p>Provides implementations for standard properties and events.
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

  private int left = ComponentConstants.DEFAULT_X_Y;
  private int top = ComponentConstants.DEFAULT_X_Y;

  private String paddingString = ComponentConstants.DEFAULT_PADDING_VALUE;
  private String marginString = ComponentConstants.DEFAULT_MARGIN_VALUE;


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
   * Returns true iff the `%type%` is visible.
   * @return  true iff the component is visible
   */
  @SimpleProperty(
      description = "Specifies whether the %type% should be visible on the screen. "
          + "Value is true if the %type% is showing and false if hidden.",
      category = PropertyCategory.APPEARANCE)
  public boolean Visible() {
    return getView().getVisibility() == View.VISIBLE;
  }

  /**
   * Specifies whether the `%type%` should be visible on the screen.  Value is `true`{:.logic.block}
   * if the `%type%` is showing and `false`{:.logic.block} if hidden.
   * @param  visibility desired state
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY,
      defaultValue = "True")
  @SimpleProperty
  public void Visible(boolean visibility) {
    // The principle of least astonishment suggests we not offer the
    // Android option INVISIBLE.
    getView().setVisibility(visibility ? View.VISIBLE : View.GONE);
  }

  /**
   * Returns the horizontal width of the `%type%`, measured in pixels.
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
   * Specifies the horizontal width of the `%type%`, measured in pixels.
   *
   * @param  width in pixels
   */
  @Override
  @SimpleProperty(description = "Specifies the horizontal width of the %type%, measured in pixels.")
  public void Width(int width) {
    container.setChildWidth(this, width);
    lastSetWidth = width;
    if (width <= Component.LENGTH_PERCENT_TAG) {
      container.$form().registerPercentLength(this, width, Form.PercentStorageRecord.Dim.WIDTH);
    } else {
      container.$form().unregisterPercentLength(this, Form.PercentStorageRecord.Dim.WIDTH);
    }
  }

  /**
   * Specifies the horizontal width of the `%type%` as a percentage
   * of the [`Screen`'s `Width`](userinterface.html#Screen.Width).
   *
   * @param pCent width in percent
   */

  @Override
  @SimpleProperty(description = "Specifies the horizontal width of the %type% as a percentage of "
      + "the width of the Screen.")
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
   * Returns the `%type%`'s vertical height, measured in pixels.
   *
   * @return  height in pixels
   */
  @Override
  @SimpleProperty
  public int Height() {
    return (int)(getView().getHeight() / container.$form().deviceDensity());
  }

  /**
   * Specifies the `%type%`'s vertical height, measured in pixels.
   *
   * @param  height in pixels
   */
  @Override
  @SimpleProperty(description = "Specifies the vertical height of the %type%, measured in pixels.")
  public void Height(int height) {
    container.setChildHeight(this, height);
    lastSetHeight = height;
    if (height <= Component.LENGTH_PERCENT_TAG) {
      container.$form().registerPercentLength(this, height, Form.PercentStorageRecord.Dim.HEIGHT);
    } else {
      container.$form().unregisterPercentLength(this, Form.PercentStorageRecord.Dim.HEIGHT);
    }
  }

  /**
   * Specifies the `%type%`'s vertical height as a percentage
   * of the [`Screen`'s `Height`](userinterface.html#Screen.Height).
   *
   * @param pCent height in percent
   */

  @Override
  @SimpleProperty(description = "Specifies the vertical height of the %type% as a percentage of "
      + "the height of the Screen.")
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
   * Specifies the position of the Left edge of the component relative to an
   * AbsoluteArrangement.
   *
   * @return the left edge of the view, in pixels
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public int Left() {
    return left;
  }

  /**
   * Specifies the position of the Left edge of the component relative to an
   * AbsoluteArrangement.
   *
   * @param x x property used by the absolute arrangement
   */
  @SimpleProperty
  public void Left(int x) {
    this.left = x;
    container.setChildNeedsLayout(this);
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

  /**
   * Specifies the position of the Top edge of the component relative to an
   * AbsoluteArrangement.
   *
   * @return the top edge of the view, in pixels
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public int Top() {
    return top;
  }

  /**
   * Specifies the position of the Top edge of the component relative to an
   * AbsoluteArrangement.
   *
   * @param y y property used by the absolute arrangement
   */
  @SimpleProperty
  public void Top(int y) {
    this.top = y;
    container.setChildNeedsLayout(this);
  }

  /**
   * Returns the padding space inside the component's bounds (Top,Left,Right,Bottom).
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public String Padding() {
    return paddingString;
  }

  /**
   * Specifies the padding space inside the component's bounds (Top,Left,Right,Bottom).
   *
   * @param padding comma-separated "top,left,right,bottom" values in pixels
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_PADDING,
          defaultValue = "0,0,0,0")
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "The padding space inside the component bounds (Top,Left,Right,Bottom).")
  public void Padding(String padding) {
    this.paddingString = padding;
    applyPadding();
  }

  /**
   * Applies the current paddingString to the underlying view. Subclasses whose
   * appearance logic resets padding as a side effect (e.g. background drawable
   * reassignment) should call this again after that logic runs.
   */
  protected void applyPadding() {
    if (paddingString != null && paddingString.contains(",")) {
      String[] sides = paddingString.split(",");
      if (sides.length == 4) {
        try {
          int t = Integer.parseInt(sides[0].trim());
          int l = Integer.parseInt(sides[1].trim());
          int r = Integer.parseInt(sides[2].trim());
          int b = Integer.parseInt(sides[3].trim());
          getView().setPadding(l, t, r, b);
        } catch (NumberFormatException e) {
          // Ignore malformed input safely
        }
      }
    }
  }

  /**
   * Returns the margin space outside the %type%'s bounds (Top,Left,Right,Bottom).
   */
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          userVisible = false)
  public String Margin() {
    return marginString;
  }

  /**
   * Specifies the margin space outside the component's bounds (Top,Left,Right,Bottom).
   *
   * @param margin comma-separated "top,left,right,bottom" values in pixels
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MARGIN,
          defaultValue = "0,0,0,0")
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "The margin space outside the component bounds (Top,Left,Right,Bottom).")
  public void Margin(String margin) {
    this.marginString = margin;
    applyMargin();
  }

  /**
   * Applies the current marginString to the underlying view's LayoutParams,
   * if the parent container's LayoutParams subtype supports margins.
   */
  protected void applyMargin() {
    if (marginString != null && marginString.contains(",")) {
      String[] sides = marginString.split(",");
      if (sides.length == 4) {
        try {
          final int t = Integer.parseInt(sides[0].trim());
          final int l = Integer.parseInt(sides[1].trim());
          final int r = Integer.parseInt(sides[2].trim());
          final int b = Integer.parseInt(sides[3].trim());

          android.view.ViewGroup.LayoutParams lp = getView().getLayoutParams();
          if (lp instanceof android.view.ViewGroup.MarginLayoutParams) {
            ((android.view.ViewGroup.MarginLayoutParams) lp).setMargins(l, t, r, b);
            getView().requestLayout();
          }
        } catch (NumberFormatException e) {
          // Ignore malformed input safely
        }
      }
    }
  }

  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return container.$form();
  }
}
