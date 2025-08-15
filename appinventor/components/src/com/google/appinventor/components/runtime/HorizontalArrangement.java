// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * ![HorizontalArrangement icon](images/horizontalarrangement.png)
 *
 * Use a horizontal arrangement component to display a group of components laid out from left to
 * right.
 *
 * This component is a formatting element in which you place components that should be displayed
 * from left to right. If you want to have components displayed one over another, use
 * {@link VerticalArrangement} instead.
 *
 * In a `HorizontalArrangement`, components are arranged along the horizontal axis, vertically
 * center-aligned.
 *
 * If a `HorizontalArrangement`'s {@link #Height()} property is set to `Automatic`, the actual
 * height of the arrangement is determined by the tallest component in the arrangement whose
 * {@link #Height()} property is not set to `Fill Parent`. If a `HorizontalArrangment`'s
 * {@link #Height()} property is set to `Automatic` and it contains only components whose `Height`
 * properties are set to `Fill Parent`, the actual height of the arrangement is calculated using
 * the automatic heights of the components. If a `HorizontalArrangement`'s {@link #Height()}
 * property is set to `Automatic` and it is empty, the {@link #Height()} will be 100.
 *
 * If a `HorizontalArrangement`'s {@link #Width()} property is set to `Automatic`, the actual width
 * of the arrangement is determined by the sum of the widths of the components. **If a
 * `HorizontalArrangement`'s {@link #Width()} property is set to `Automatic`, any components whose
 * {@link #Width()} properties are set to `Fill Parent` will behave as if they were set to
 * `Automatic`.**
 *
 * If a `HorizontalArrangement`'s {@link #Width() property is set to `Fill Parent` or specified in
 * pixels, any components whose {@link #Width()} properties are set to `Fill Parent` will equally
 * take up the width not occupied by other components.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
@DesignerComponent(version = YaVersion.HORIZONTALARRANGEMENT_COMPONENT_VERSION,
    description = "<p>A formatting element in which to place components " +
    "that should be displayed from left to right.  If you wish to have " +
    "components displayed one over another, use " +
    "<code>VerticalArrangement</code> instead.</p>",
    category = ComponentCategory.LAYOUT,
    iconName = "images/horizontal.png")
@SimpleObject
public class HorizontalArrangement extends HVArrangement {
  public HorizontalArrangement(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL,
      ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
  }

}
