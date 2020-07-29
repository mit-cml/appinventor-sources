// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MenuItem;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.io.IOException;

/**
 * A Sidebar Item can only be placed inside Sidebar component.
 * It displays a piece of text specified by the Text property and
 * an icon specified by the Icon property.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
@DesignerComponent(version = YaVersion.SIDEBARITEM_COMPONENT_VERSION,
        description = "A Sidebar Item can only be placed inside Sidebar component. " +
                "It displays a piece of text specified by the <code>Text</code> property and " +
                " an icon specified by the <code>Icon</code> property. ",
        category = ComponentCategory.USERINTERFACE)
@SimpleObject
public class SidebarItem implements Component{

    private Sidebar sidebar;
    int index;
    private MenuItem menuItem;
    private String iconPath = "";
    private Drawable iconDrawable;
    private boolean visible;

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public SidebarItem (ComponentContainer container) {
        sidebar = (Sidebar)container;
        index = sidebar.additem(this);
        menuItem = sidebar.$form().navigationView.getMenu().getItem(index);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public void Text(String text) {
        menuItem.setTitle(text)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Click();
                return false;
            }
        });
    }

    /**
     * Returns the text displayed by the menu item.
     *
     * @return  menu item text
     */
    @SimpleProperty
    public String Text() {
        CharSequence text = menuItem.getTitle();
        return (text == null ? "" : text.toString());
    }

    /**
     * Returns true if the menu item is visible, false otherwise.
     *
     * @return  {@code true} iff the menu item is visible.
     */
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
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
        if (menuItem != null) {
            menuItem.setVisible(visible);
        }
    }


    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Path of the icon to display for this menu item.")
    public String Icon() {
        return iconPath;
    }

    /**
     * Specifies the path of the menu item's icon.
     *
     * @param path  the path of the menu item's icon
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
    @SimpleProperty(description = "Specifies the path of the menu item's icon.")
    public void Icon(String path) {
        // If it's the same as on the prior call and the prior load was successful,
        // do nothing.
        if (path.equals(iconPath) && iconDrawable != null) {
            return;
        }

        iconPath = (path == null) ? "" : path;

        // Load image from file.
        if (iconPath.length() > 0) {
            try {
                iconDrawable = MediaUtil.getBitmapDrawable(sidebar.$form(), iconPath);
            } catch (IOException ioe) {
                return;
            }
            if (menuItem != null) {
                menuItem.setIcon(iconDrawable);
            }
        }
    }

    public void setIcon(int assetId) {
        if (menuItem != null) {
            menuItem.setIcon(assetId);
        }
    }

    @SimpleEvent(description = "Event raised when user selects this sidebar item.")
    public void Click() {
        EventDispatcher.dispatchEvent(this, "Click");
    }

    @Override
    public HandlesEventDispatching getDispatchDelegate() {
        return sidebar.$form();
    }

}
