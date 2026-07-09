// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.view.View;
import android.view.ViewGroup;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimplePropertyCopier;
import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.BoxSide;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.YailDictionary;

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

  protected YailDictionary paddingSource;
  protected YailDictionary marginSource;

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
   * This hidden property is used solely to force the App Inventor compiler
   * to generate the helper dropdown blocks for LayoutDimension.
   * It is not intended to be used by the end-user.
   */
  @SimpleProperty(
          userVisible = false,
          description = "Internal helper to expose LayoutDimension blocks."
  )
  public void LayoutDimensionHelper(@Options(BoxSide.class) String dimension) {
    // Dummy setter; no implementation needed as this is just a compiler hook.
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_PADDING,
          defaultValue = ComponentConstants.DEFAULT_PADDING_VALUE)
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "The padding space inside the component bounds. Accepts a Dictionary in blocks."
  )
  public void Padding(YailDictionary padding) {
    this.paddingSource = padding;
    applyPadding();
  }

  /**
   * Re-applies the last known padding input to the view. Safe to call with
   * no arguments any time the view's padding may have been reset as a side
   * effect of other logic (e.g. background drawable reassignment).
   */
  protected void applyPadding() {
    applyPadding(paddingSource);
  }

  protected void applyPadding(YailDictionary input) {
    View view = getView();
    float density = container.$form().deviceDensity();

    int t = (int) (view.getPaddingTop() / density);
    int l = (int) (view.getPaddingLeft() / density);
    int r = (int) (view.getPaddingRight() / density);
    int b = (int) (view.getPaddingBottom() / density);

    int[] merged = mergeSides(input, t, l, r, b); // merged is in dp
    if (merged != null) {
      view.setPadding(
              Math.round(merged[1] * density),
              Math.round(merged[0] * density),
              Math.round(merged[2] * density),
              Math.round(merged[3] * density)
      ); // l, t, r, b
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_MARGIN,
          defaultValue = ComponentConstants.DEFAULT_MARGIN_VALUE)
  @SimpleProperty(
          category = PropertyCategory.APPEARANCE,
          description = "The margin space outside the component bounds. Accepts a Dictionary in blocks."
  )
  public void Margin(YailDictionary margin) {
    this.marginSource = margin;
    applyMargin();
  }

  /**
   * Re-applies the last known margin input to the view's LayoutParams. Safe
   * to call with no arguments any time margins may need to be reasserted.
   */
  protected void applyMargin() {
    applyMargin(marginSource);
  }

  protected void applyMargin(YailDictionary input) {
    View view = getView();
    ViewGroup.LayoutParams lp = view.getLayoutParams();

    if (lp instanceof ViewGroup.MarginLayoutParams) {
      ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) lp;
      float density = container.$form().deviceDensity();

      int t = (int) (marginLp.topMargin / density);
      int l = (int) (marginLp.leftMargin / density);
      int r = (int) (marginLp.rightMargin / density);
      int b = (int) (marginLp.bottomMargin / density);

      int[] merged = mergeSides(input, t, l, r, b); // merged is in dp
      if (merged != null) {
        marginLp.setMargins(
                Math.round(merged[1] * density),
                Math.round(merged[0] * density),
                Math.round(merged[2] * density),
                Math.round(merged[3] * density)
        ); // l, t, r, b
        view.requestLayout();
      }
    }
  }

  /**
   * Resolves a Padding/Margin YailDictionary against the view's current
   * side values, returning a merged [top, left, right, bottom] array.
   * "all" overrides every side if present and valid; otherwise each side
   * is read individually, with "leading"/"trailing" as fallbacks for
   * "left"/"right". Any side not specified (or invalid) keeps its current
   * value. Returns null if the input is null.
   */
  private int[] mergeSides(YailDictionary dict, int curT, int curL, int curR, int curB) {
    if (dict == null) {
      return null;
    }

    int t = curT, l = curL, r = curR, b = curB;

    int allVal = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.All));
    if (allVal >= 0) {
      return new int[]{allVal, allVal, allVal, allVal};
    }

    int dictTop = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.Top));
    if (dictTop >= 0) t = dictTop;

    int dictLeft = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.Left));
    if (dictLeft < 0) dictLeft = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.Leading));
    if (dictLeft >= 0) l = dictLeft;

    int dictRight = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.Right));
    if (dictRight < 0) dictRight = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.Trailing));
    if (dictRight >= 0) r = dictRight;

    int dictBottom = parsePositiveIntOrMinusOne(getSide(dict, BoxSide.Bottom));
    if (dictBottom >= 0) b = dictBottom;

    return new int[]{t, l, r, b};
  }

  /**
   * Looks up a side's value in the dictionary, accepting either the
   * BoxSide enum constant itself as a key, or its underlying String
   * value (e.g. "top") — the latter being what blocks-built
   * dictionaries will actually contain, since OptionList blocks
   * evaluate to their underlying value at runtime.
   */
  private Object getSide(YailDictionary dict, BoxSide side) {
    Object val = dict.get(side.toUnderlyingValue());
    if (val == null) {
      val = dict.get(side);
    }
    return val;
  }

  private int parsePositiveIntOrMinusOne(Object val) {
    if (val == null) return -1;
    try {
      int parsed = (val instanceof Number) ? ((Number) val).intValue() : Integer.parseInt(val.toString().trim());
      return (parsed >= 0) ? parsed : -1;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  // Component implementation

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    return container.$form();
  }
}
