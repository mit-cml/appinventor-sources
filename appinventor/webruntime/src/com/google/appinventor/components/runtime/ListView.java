package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.android.View;

@DesignerComponent(
    version = YaVersion.LISTVIEW_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
    description = "Basic ListView for webruntime",
    iconName = "images/listView.png")
@SimpleObject

public class ListView extends View {
    public ListView(ComponentCategory container) {
        super(container);
    }
}
