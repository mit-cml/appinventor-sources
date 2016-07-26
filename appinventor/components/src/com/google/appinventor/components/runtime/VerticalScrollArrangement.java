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
 * A vertical arrangement of components
 * @author sharon@google.com (Sharon Perl)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */

@DesignerComponent(version = YaVersion.VERTICALSCROLLARRANGEMENT_COMPONENT_VERSION,
    description = "<p>A formatting element in which to place components " +
    "that should be displayed one below another.  (The first child component " +
    "is stored on top, the second beneath it, etc.)  If you wish to have " +
    "components displayed next to one another, use " +
    "<code>HorizontalArrangement</code> instead.</p><p> " +
    "This version is scrollable",
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class VerticalScrollArrangement extends HVArrangement {

  public VerticalScrollArrangement(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
      ComponentConstants.SCROLLABLE_ARRANGEMENT);
  }

}
