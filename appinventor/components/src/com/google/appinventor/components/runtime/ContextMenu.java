// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;
import android.view.View;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for ContextMenuItems
 * @author singhalsara48@gmail.com (Sara Singhal)
 *
 */
@DesignerComponent(version = YaVersion.CONTEXT_MENU_COMPONENT_VERSION,
        category = ComponentCategory.LAYOUT,
        description = "Component for context menu to hold ContextMenuItems.")
@SimpleObject
public class ContextMenu implements Component, ComponentContainer, OnCreateContextMenuListener {

    private Form form;
    private android.view.ContextMenu menu;

    private List<ContextMenuItem> items;
    private AndroidViewComponent component;

    /**
     * Create a new Context Menu component.
     *
     * @param form The form that will render this menu.
     */
    public ContextMenu(Form form) {
        items = new ArrayList<>();
        form.registerForOnCreateContextMenu(this);
        this.form = form;
    }

    @Override
    public void onCreateContextMenu(android.view.ContextMenu menu, View view, android.view.ContextMenu.ContextMenuInfo menuInfo) {
        this.menu = menu;
        menu.clear();
        for (ContextMenuItem item : items) {
            item.addToContextMenu(menu);
        }
    }

    public void addMenuItem(ContextMenuItem item) {
        items.add(item);
        if (menu != null) {
            item.addToContextMenu(menu);
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COMPONENT )
    @SimpleProperty
    public void ComponentSelector(AndroidViewComponent component) {
        this.component=component;
        form.getComponent(component.getView(),this);
        if(component instanceof ListView) {
            this.form.registerForContextMenu(((ListView) component).getListView());
        }
        else {
            this.form.registerForContextMenu(component.getView());
        }
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
