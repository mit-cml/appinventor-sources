// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.os.Build;
import android.util.Log;
import android.view.Menu;
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
    private PopupMenu popupMenu;
    private MenuItem item;

    private String text = "";
    private boolean enabled = true;
    private boolean visible = true;

    public ContextMenuItem(ContextMenu parent) {
        contextMenu = parent;
        contextMenu.addMenuItem(this);
    }

    public ContextMenuItem(PopupMenu parent) {
        popupMenu = parent;
        popupMenu.addPopupMenuItem(this);
    }

    public void addToPopupMenu(android.widget.PopupMenu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            item = menu.getMenu().add(Menu.NONE, Menu.NONE, Menu.NONE, text)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Click();
                            return false;
                        }
                    });
        }
        item.setVisible(enabled);
        item.setEnabled(enabled);
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
        item.setEnabled(enabled);
        item.setVisible(visible);
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

    /**
     * Returns true if the menu item is active and clickable.
     *
     * @return  {@code true} indicates enabled, {@code false} disabled
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR,
            description = "If true, user can tap menu item to cause action.")
    public boolean Enabled() {
        return enabled;
    }

    /**
     * Specifies whether the menu item should be active and clickable.
     *
     * @param enabled  {@code true} for enabled, {@code false} disabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        this.enabled = enabled;
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    /**
     * Returns true if the menu item is visible, false otherwise.
     *
     * @return  {@code true} iff the menu item is visible.
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public boolean Visible() {
        return visible;
    }

    /**
     * Specifies whether the menu item should be visible or hidden from menu.
     *
     * @param  visible  {@code true} iff the menu item should be visible.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY,
            defaultValue = "True")
    @SimpleProperty
    public void Visible(boolean visible) {
        this.visible = visible;
        if (item != null) {
            item.setVisible(visible);
        }
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return contextMenu.$form();
    }
}
