// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.util.Log;
import android.view.MenuItem;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

@DesignerComponent(version = YaVersion.CONTEXT_MENUITEM_COMPONENT_VERSION,
        description = "A Context Menu Item can only be placed inside Context Menu components. " ,
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class ContextMenuItem implements Component {

    private ContextMenu contextMenu;
    private MenuItem item;

    private String text = "";

    public ContextMenuItem(ContextMenu parent) {
        contextMenu = parent;
        contextMenu.addMenuItem(this);
    }

    public void addToContextMenu(android.view.ContextMenu menu) {
        item = menu.add(android.view.Menu.NONE, android.view.Menu.NONE, android.view.Menu.NONE, text)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Click();
                        return false;
                    }
                });
    }

    @SimpleEvent(description = "Event raised when user selects this menu item.")
    public void Click() {
        EventDispatcher.dispatchEvent(this, "Click");
    }

    /**
     * Returns the text displayed by the menu item.
     *
     * @return  menu item text
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public String Text() {
        return text;
    }

    /**
     * Specifies the text displayed by the menu item.
     *
     * @param text  new text for menu item
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty
    public void Text(String text) {
        this.text = text;
        if (item != null) {
            item.setTitle(text);
        }
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return contextMenu.$form();
    }
}
