// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

/**
 * Container for ContextMenuItems
 * @author singhalsara48@gmail.com (Sara Singhal)
 *
 */
@DesignerComponent(version = YaVersion.CONTEXT_MENU_COMPONENT_VERSION,
        category = ComponentCategory.LAYOUT,
        description = "Component for context menu to hold ContextMenuItems.")
@SimpleObject
public class ContextMenu implements Component, ComponentContainer {

    private Form form;

    /**
     * Create a new Context Menu component.
     *
     * @param form The form that will render this menu.
     */
    public ContextMenu(Form form) {
        this.form = form;
    }



    // ComponentContainer implementation

    @Override
    public Activity $context() {
        return form;
    }

    @Override
    public Form $form() {
        return form;
    }

    @Override
    public void $add(AndroidViewComponent component) {
        throw new UnsupportedOperationException("Menu.$add() called");
    }

    @Override
    public void setChildWidth(AndroidViewComponent component, int width) {
        throw new UnsupportedOperationException("Menu.setChildWidth() called");
    }

    @Override
    public void setChildHeight(AndroidViewComponent component, int height) {
        throw new UnsupportedOperationException("Menu.setChildHeight() called");
    }

    @Override
    public int Width() {
        return 0;
    }

    @Override
    public int Height() {
        return 0;
    }

    // Component implementation

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return form;
    }

}
