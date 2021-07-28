// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.HORIZONTALRADIOGROUP_COMPONENT_VERSION,
    description = "A RadioGroup is used for set of radio buttons. If we check one " + 
    "radio button that belongs to a radio group, it automatically unchecks any " + 
    "previously checked radio button within the same group. This layout arranges them " +
    "in horizontal order.",
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class HorizontalRadioGroup extends RadioGroup {
  public HorizontalRadioGroup(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL, 
    ComponentConstants.EMPTY_HV_ARRANGEMENT_WIDTH, ComponentConstants.EMPTY_HV_ARRANGEMENT_HEIGHT);
  }

}