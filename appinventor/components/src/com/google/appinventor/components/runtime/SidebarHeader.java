// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime;

import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.YaVersion;

/**
 * Container to contain components for Sidebar Header.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
@DesignerComponent(version = YaVersion.SIDEBAR_HEADER_COMPONENT_VERSION,
        description = "Container to contain components for Sidebar Header",
        category = ComponentCategory.LAYOUT,
        showOnPalette = false)
@SimpleObject
public class SidebarHeader extends HVArrangement<FrameLayout> implements Component,ComponentContainer{

    private Sidebar sidebar;

    public SidebarHeader(ComponentContainer container) {
        super(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL, new FrameLayout(container.$context()));
        container.$add(this);
        sidebar = (Sidebar) container;
        sidebar.addHeader(this);
    }
}
