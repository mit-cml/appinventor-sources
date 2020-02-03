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
 * ![VerticalArrangement icon](images/verticalarrangement.png)
 *
 * Use a `VerticalArrangement` component to display a group of components laid out from top to
 * bottom, left-aligned.
 *
 * This component is a formatting element in which you place components that should be displayed
 * one below another. The first child component is stored on top, the second beneath it, and so on.
 * If you want to have components displayed next to one another, use {@link HorizontalArrangement}
 * instead.
 *
 * In a `VerticalArrangement`, components are arranged along the vertical axis, left-aligned.
 *
 * If a `VerticalArrangement`'s {@link #Width()} property is set to `Automatic`, the actual width
 * of the arrangement is determined by the widest component in the arrangement whose
 * {@link #Width()} property is not set to `Fill Parent`. If a `VerticalArrangement`'s
 * {@link #Width()} property is set to `Automatic` and it contains only components whose
 * {@link #Width()} properties are set to `Fill Parent`, the actual width of the arrangement is
 * calculated using the automatic widths of the components. If a `VerticalArrangement`'s
 * {@link #Width()} property is set to `Automatic` and it is empty, the width will be 100.
 *
 * If a `VerticalArrangement`'s {@link #Height()} property is set to `Automatic`, the actual height
 * of the arrangement is determined by the sum of the heights of the components. **If a
 * `VerticalArrangement`'s {@link #Height()} property is set to `Automatic`, any components whose
 * `Height` properties are set to `Fill Parent` will behave as if they were set to `Automatic`.**
 *
 * If a `VerticalArrangement`'s {@link #Height()} property is set to `Fill Parent` or specified in
 * pixels, any components whose Height properties are set to `Fill Parent` will equally take up the
 * height not occupied by other components.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */

@DesignerComponent(version = YaVersion.VERTICALARRANGEMENT_COMPONENT_VERSION,
    description = "<p>A formatting element in which to place components " +
    "that should be displayed one below another.  (The first child component " +
    "is stored on top, the second beneath it, etc.)  If you wish to have " +
    "components displayed next to one another, use " +
    "<code>HorizontalArrangement</code> instead.</p>",
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class VerticalArrangement extends HVArrangement {

  public VerticalArrangement(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
      ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
  }

}
