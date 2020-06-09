package com.pavi2410;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(version = 1,
    description = "test",
    category = ComponentCategory.EXTENSION)
@SimpleObject(external = true)
public class SimpleContainer extends HVArrangement {
  public SimpleContainer(ComponentContainer container) {
    super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL,
      ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
  }
}
