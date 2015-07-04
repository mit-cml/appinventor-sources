// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;

import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.AlignmentUtil;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.ViewUtil;

/**
 * A container for components that arranges them linearly, either
 * horizontally or vertically.
 *
 * @author sharon@google.com (Sharon Perl)
 */

@SimpleObject
public class HVArrangement extends AndroidViewComponent implements Component, ComponentContainer {
  private final Activity context;

  // Layout
  private final int orientation;
  private final LinearLayout viewLayout;

  // translates App Inventor alignment codes to Android gravity
  private final AlignmentUtil alignmentSetter;

  // the alignment for this component's LinearLayout
  private int horizontalAlignment;
  private int verticalAlignment;

  /**
   * Creates a new HVArrangement component.
   *
   * @param container  container, component will be placed in
   * @param orientation one of
   *     {@link ComponentConstants#LAYOUT_ORIENTATION_HORIZONTAL}.
   *     {@link ComponentConstants#LAYOUT_ORIENTATION_VERTICAL}
  */
  public HVArrangement(ComponentContainer container, int orientation) {
    super(container);
    context = container.$context();

    this.orientation = orientation;
    viewLayout = new LinearLayout(context, orientation,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH,
        ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT);
    alignmentSetter = new AlignmentUtil(viewLayout);

    horizontalAlignment = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT;
    verticalAlignment = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT;
    alignmentSetter.setHorizontalAlignment(horizontalAlignment);
    alignmentSetter.setVerticalAlignment(verticalAlignment);

    container.$add(this);
  }

  // ComponentContainer implementation

  @Override
  public Activity $context() {
    return context;
  }

  @Override
  public Form $form() {
    return container.$form();
  }

  @Override
  public void $add(AndroidViewComponent component) {
    viewLayout.add(component);
  }

  @Override
  public void setChildWidth(AndroidViewComponent component, int width) {
    if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      ViewUtil.setChildWidthForHorizontalLayout(component.getView(), width);
    } else {
      ViewUtil.setChildWidthForVerticalLayout(component.getView(), width);
    }
  }

  @Override
  public void setChildHeight(AndroidViewComponent component, int height) {
    if (orientation == ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL) {
      ViewUtil.setChildHeightForHorizontalLayout(component.getView(), height);
    } else {
      ViewUtil.setChildHeightForVerticalLayout(component.getView(), height);
    }
  }

  // AndroidViewComponent implementation

  @Override
  public View getView() {
    return viewLayout.getLayoutManager();
  }

 // These property definitions are duplicated in Form.java

  // The numeric encodings are defined Component Constants

  /**
   * Returns a number that encodes how contents of the arrangement are aligned horizontally.
   * The choices are: 1 = left aligned, 2 = horizontally centered, 3 = right aligned
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how contents of the arrangement are aligned " +
          " horizontally. The choices are: 1 = left aligned, 2 = horizontally centered, " +
          " 3 = right aligned.  Alignment has no effect if the arrangement's width is " +
          "automatic.")
  public int AlignHorizontal() {
    return horizontalAlignment;
  }

  /**
   * Sets the horizontal alignment for contents of the arrangement
   *
   * @param alignment
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT,
      defaultValue = ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignHorizontal(int alignment) {
    try {
      // notice that the throw will prevent the alignment from being changed
      // if the argument is illegal
      alignmentSetter.setHorizontalAlignment(alignment);
      horizontalAlignment = alignment;
    } catch (IllegalArgumentException e) {
      container.$form().dispatchErrorOccurredEvent(this, "HorizontalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_HORIZONTAL_ALIGNMENT, alignment);
    }
  }

  /**
   * Returns a number that encodes how contents of the arrangement are aligned vertically.
   * The choices are: 1 = top, 2 = vertically centered, 3 = aligned at the bottom.
   * Alignment has no effect if the arrangement's height is automatic.
   */
   @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "A number that encodes how the contents of the arrangement are aligned " +
          " vertically. The choices are: 1 = aligned at the top, 2 = vertically centered, " +
          "3 = aligned at the bottom.  Alignment has no effect if the arrangement's height " +
          "is automatic.")
  public int AlignVertical() {
    return verticalAlignment;
  }

  /**
   * Sets the vertical alignment for contents of the arrangement
   *
   * @param alignment
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT,
      defaultValue = ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT + "")
  @SimpleProperty
  public void AlignVertical(int alignment) {
    try {
      // notice that the throw will prevent the alignment from being changed
      // if the argument is illegal
      alignmentSetter.setVerticalAlignment(alignment);
      verticalAlignment = alignment;
    } catch (IllegalArgumentException e) {
      container.$form().dispatchErrorOccurredEvent(this, "VerticalAlignment",
          ErrorMessages.ERROR_BAD_VALUE_FOR_VERTICAL_ALIGNMENT, alignment);
    }
  }

}
