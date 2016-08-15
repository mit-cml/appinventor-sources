// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * A horizontal arrangement of components
 * @author sharon@google.com (Sharon Perl)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */
@DesignerComponent(version = YaVersion.HORIZONTALSCROLLARRANGEMENT_COMPONENT_VERSION,
    description = "<p>A formatting element in which to place components " +
    "that should be displayed from left to right.  If you wish to have " +
    "components displayed one over another, use " +
    "<code>VerticalArrangement</code> instead.</p><p>This version is " +
    "scrollable.",
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class HorizontalScrollArrangement extends HVArrangement {
  public HorizontalScrollArrangement(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL,
      ComponentConstants.SCROLLABLE_ARRANGEMENT);
  }

}
