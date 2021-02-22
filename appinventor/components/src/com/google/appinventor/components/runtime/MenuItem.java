// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem.OnMenuItemClickListener;
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
 * A Menu Item can only be placed inside Menus components and Sidebar.
 * It displays a piece of text specified by the {@link #Text(String)} property
 * or an icon specified by the {@link #Icon(String)} property on action bar
 * or Sidebar. Additional properties include visibility and enabled/
 * disabled, all of which can be set in the Designer or Blocks Editor.
 * Click event is triggered on user selection.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */

@DesignerComponent(version = YaVersion.MENUITEM_COMPONENT_VERSION,
    description = "A Menu Item can only be placed inside Menus components and Sidebar. " +
        "It displays a piece of text specified by the <code>Text</code> property " +
        "or an icon specified by the <code>Icon</code> property if shown " +
        "on action bar or Sidebar. Additional properties include visibility and enabled/ " +
        "disabled, all of which can be set in the Designer or Blocks Editor. " +
        "Click event is triggered on user selection.",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public final class MenuItem implements Component {
  private static final String LOG_TAG = "MenuItem";

  private Menu menu;
  private ContextMenu contextMenu;
  private PopupMenu popupMenu;
  private Sidebar sidebarMenu;
  private android.view.MenuItem item;

  private String text = "";
  private String iconPath = "";
  private Drawable iconDrawable;
  private boolean enabled = true;
  private boolean visible = true;
  private boolean showOnActionBar = false;
  private OnMenuItemClickListener listener = null;

  public MenuItem(Menu parent) {
    menu = parent;
    menu.addMenuItem(this);
  }

  public MenuItem(ContextMenu parent) {
    contextMenu = parent;
    contextMenu.addMenuItem(this);
  }

  public MenuItem(PopupMenu parent) {
    popupMenu = parent;
    popupMenu.addPopupMenuItem(this);
  }

  public MenuItem(Sidebar parent) {
    sidebarMenu = parent;
    sidebarMenu.addSidebarItem(this);
  }

  public void addToMenu(android.view.Menu menu) {
    item = menu.add(android.view.Menu.NONE, android.view.Menu.NONE, menu.size() + 1, text);
    itemClickListener();
    item.setIcon(iconDrawable);
    ShowOnActionBar(showOnActionBar);
  }

  public void addToPopupMenu(android.widget.PopupMenu menu) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      item = menu.getMenu().add(android.view.Menu.NONE, android.view.Menu.NONE, android.view.Menu.NONE, text);
      itemClickListener();
    }
  }

  public void addToContextMenu(android.view.ContextMenu menu) {
    item = menu.add(android.view.Menu.NONE, android.view.Menu.NONE, android.view.Menu.NONE, text);
    itemClickListener();
  }

  public void addToSidebarMenu(android.view.Menu menu) {
    item = menu.add(android.view.Menu.NONE, android.view.Menu.NONE, menu.size() + 1, text);
    itemClickListener();
    item.setIcon(iconDrawable);
  }

  public void itemClickListener() {
    item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(android.view.MenuItem item) {
        Click();
        return false;
      }
    });
    item.setEnabled(enabled);
    item.setVisible(visible);
  }

  /**
   * Returns the text displayed by the menu item.
   *
   * @return menu item text
   */
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public String Text() {
    return text;
  }

  /**
   * Specifies the text displayed by the menu item.
   *
   * @param text new text for menu item
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
   * Returns the path of the menu item's icon.
   *
   * @return the path of the menu item's icon
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "Path of the icon to display for this menu item.")
  public String Icon() {
    return iconPath;
  }

  /**
   * Specifies the path of the menu item's icon.
   *
   * @param path the path of the menu item's icon
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(description = "Specifies the path of the menu item's icon.")
  public void Icon(String path) {
    // If it's the same as on the prior call and the prior load was successful,
    // do nothing.
    if (path.equals(iconPath) && iconDrawable != null) {
      return;
    }

    iconPath = path;

    // Load image from file.
    if (iconPath.length() > 0) {
      try {
        if (menu != null) {
          iconDrawable = MediaUtil.getBitmapDrawable(menu.$form(), iconPath);
        } else if (sidebarMenu != null) {
          iconDrawable = MediaUtil.getBitmapDrawable(sidebarMenu.$form(), iconPath);
        }
      } catch (IOException ioe) {
        Log.e(LOG_TAG, "Unable to load " + iconPath);
        return;
      }
      if (item != null) {
        item.setIcon(iconDrawable);
      }
    }
  }

  public void setIcon(int assetId) {
    if (item != null) {
      item.setIcon(assetId);
    }
  }

  /**
   * Set a custom on-click listener for this item; Click event will no longer be called.
   *
   * @param listener the on-click listener to replace the default Click event
   */
  public void setOnClickListener(OnMenuItemClickListener listener) {
    Log.d(LOG_TAG, "setOnClickListener item = " + item);
    this.listener = listener;
    if (item != null) {
      item.setOnMenuItemClickListener(listener);
    }
  }

  /**
   * Returns true if the menu item is active and clickable.
   *
   * @return {@code true} indicates enabled, {@code false} disabled
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
   * @param enabled {@code true} for enabled, {@code false} disabled
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
   * @return {@code true} iff the menu item is visible.
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public boolean Visible() {
    return visible;
  }

  /**
   * Specifies whether the menu item should be visible or hidden from menu.
   *
   * @param visible {@code true} iff the menu item should be visible.
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

  /**
   * Returns true if the menu item is shown on action bar, false otherwise.
   *
   * @return {@code true} iff the menu item is shown on action bar.
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE)
  public boolean ShowOnActionBar() {
    return showOnActionBar;
  }

  /**
   * Specifies whether the menu item should show on action bar:
   * If {@code true}, then item will appear as an icon on the action bar
   * (given that there is enough space);
   * If {@code false}, then item will always appear as text in the overflow menu.
   *
   * @param showOnActionBar {@code true} iff the item should appear on action bar.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(description = "Specifies whether the menu item should show as action: " +
      "if true, then item will appear as an icon on action bar (given enough room); " +
      "if false, then item will always appear as text in the overflow menu.")
  public void ShowOnActionBar(boolean showOnActionBar) {
    this.showOnActionBar = showOnActionBar;
    if (item != null) {
      item.setShowAsAction(
          showOnActionBar ? android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
              : android.view.MenuItem.SHOW_AS_ACTION_NEVER
      );
      menu.$form().invalidateOptionsMenu();
    }
  }

  /**
   * Event to handle when user selects this menu item.
   */
  @SimpleEvent(description = "Event raised when user selects this menu item.")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  @Override
  public HandlesEventDispatching getDispatchDelegate() {
    if (menu != null) {
      return menu.$form();
    } else if (contextMenu != null) {
      return contextMenu.$form();
    } else if (popupMenu != null) {
      return popupMenu.$form();
    } else {
      return sidebarMenu.$form();
    }
  }

}
