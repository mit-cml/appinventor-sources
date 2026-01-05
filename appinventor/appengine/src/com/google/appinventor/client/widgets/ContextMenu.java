// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Context menu widget implementation.
 *
 */
public final class ContextMenu {

  // UI elements
  private final PopupPanel popupPanel;
  private final MenuBar menuBar;
  // Track menu items for type-ahead navigation
  private final java.util.List<MenuItem> menuItems = new java.util.ArrayList<MenuItem>();

  /**
   * Creates a new context menu.
   */
  public ContextMenu() {
    popupPanel = new PopupPanel(true);  // autoHide
    //Enabling Glass under the popups so that clicks on the iframe (blockly) also hide the panel
    popupPanel.setGlassEnabled(true);
    popupPanel.setGlassStyleName("none"); //No style is passed (the default grays out the window)
    menuBar = new MenuBar(true);
    menuBar.setStylePrimaryName("ode-ContextMenu");
    popupPanel.add(menuBar);
  }

  /**
   * Adds a menu item to the context menu.
   *
   * @param text  caption of menu item
   * @param command   command to execute when menu item is chosen
   * @return  menu item
   */
  public MenuItem addItem(String text, final Command command) {
    MenuItem menuItem = new MenuItem(text, new Command() {
      @Override
      public void execute() {
        hide();
        command.execute();
      }
    });
    menuItem.setStylePrimaryName("ode-ContextMenuItem");
    Roles.getMenuitemRole().set(menuItem.getElement());
    menuBar.addItem(menuItem);
    menuItems.add(menuItem); // Track for type-ahead
    return menuItem;
  }

  /**
   * Adds a menu item to the context menu.
   *
   * @param text  caption of menu item
   * @param asHtml whether to treat text as html
   * @param command   command to execute when menu item is chosen
   * @return  menu item
   */
  public MenuItem addItem(String text, boolean asHtml, final Command command) {
    return addItem(text, asHtml, command, null);
  }

  /**
   * Adds a menu item to the context menu.
   *
   * @param text  caption of menu item
   * @param asHtml whether to treat text as html
   * @param command   command to execute when menu item is chosen
   * @param style CSS Style to use or null for default
   * @return  menu item
   */
  public MenuItem addItem(String text, boolean asHtml, final Command command, final String style) {
    MenuItem menuItem = new MenuItem(text, asHtml, new Command() {
      @Override
      public void execute() {
        hide();
        command.execute();
      }
    });
    if (style != null) {
      menuItem.setStylePrimaryName(style);
    } else {
      menuItem.setStylePrimaryName("ode-ContextMenuItem");
    }
    Roles.getMenuitemRole().set(menuItem.getElement());
    menuBar.addItem(menuItem);
    menuItems.add(menuItem); // Track for type-ahead
    return menuItem;
  }

  /**
   * Removes a menu item from the context menu.
   *
   * @param item  menu item to be removed
   */
  public void removeItem(MenuItem item) {
    menuBar.removeItem(item);
    menuItems.remove(item); // Remove from our tracking list
  }

  /**
   * Adds a separator to the context menu.
   */
  public MenuItemSeparator addSeparator() {
    MenuItemSeparator menuItemSeparator = menuBar.addSeparator();
    menuItemSeparator.setStylePrimaryName("ode-ContextMenuItemSeparator");
    return menuItemSeparator;
  }

  public void removeSeparator(MenuItemSeparator separator) {
    menuBar.removeSeparator(separator);
  }

  /**
   * Shows the context menu.
   */
  public void show() {
    popupPanel.show();
  }

  /**
   * Sets the popup's position using a PopupPanel.PositionCallback, and shows the popup.
   */
  public void setPopupPositionAndShow(PopupPanel.PositionCallback callback) {
    popupPanel.setPopupPositionAndShow(callback);
  }

  /**
   * Sets the popup position of the context menu.
   */
  public void setPopupPosition(int left, int top) {
    popupPanel.setPopupPosition(left, top);
  }

  /**
   * Hides the context menu.
   */
  public void hide() {
    popupPanel.hide();
  }

  /* Returns if the context menu is showing */
  public boolean isShowing() {
    return popupPanel.isShowing();
  }

  /* Gives the menubar focus */
  public void focus() {
    menuBar.focus();
  }

  public void moveSelectionDown() {
    menuBar.moveSelectionDown();
  }

  public void moveSelectionUp() {
    menuBar.moveSelectionUp();
  }

  public void resetSelection() {
    menuBar.selectItem(null);
  }

  /**
   * Selects the first enabled menu item.
   * Used for Home key navigation.
   */
  public void selectFirstItem() {
    if (menuItems.isEmpty()) {
      return;
    }
    // Find first enabled item
    for (MenuItem item : menuItems) {
      if (item.isEnabled()) {
        menuBar.selectItem(item);
        return;
      }
    }
  }

  /**
   * Selects the last enabled menu item.
   * Used for End key navigation.
   */
  public void selectLastItem() {
    if (menuItems.isEmpty()) {
      return;
    }
    // Find last enabled item (search backwards)
    for (int i = menuItems.size() - 1; i >= 0; i--) {
      MenuItem item = menuItems.get(i);
      if (item.isEnabled()) {
        menuBar.selectItem(item);
        return;
      }
    }
  }

  /**
   * Selects the next menu item starting with the given character.
   * Used for type-ahead navigation.
   *
   * @param startChar The character to search for (uppercase)
   * @param findNext If true, find next match; if false, find first match
   */
  public void selectItemStartingWith(char startChar, boolean findNext) {
    if (menuItems.isEmpty()) {
      return;
    }

    // Simple approach: just search through all items
    // If findNext is true, we'll cycle through matches
    MenuItem firstMatch = null;
    boolean foundCurrent = false;

    for (MenuItem item : menuItems) {
      if (item.isEnabled() && itemStartsWith(item, startChar)) {
        if (firstMatch == null) {
          firstMatch = item;
        }

        if (findNext && !foundCurrent) {
          if (item.getStyleName().contains("-selected")) {
            foundCurrent = true;
            continue;
          }
        } else {
          selectItem(item);
          return;
        }
      }
    }

    // If we get here and findNext was true, wrap to first match
    if (firstMatch != null) {
      selectItem(firstMatch);
    }
  }

  /**
   * Selects a specific menu item.
   */
  private void selectItem(MenuItem target) {
    menuBar.selectItem(target);
  }

  /**
   * Checks if a menu item's text starts with the given character.
   *
   * @param item The menu item to check
   * @param startChar The character to match (case-insensitive)
   * @return true if the item's text starts with the character
   */
  private boolean itemStartsWith(MenuItem item, char startChar) {
    String text = item.getText().trim();
    if (text.isEmpty()) {
      return false;
    }
    // Remove HTML tags if present (items might have HTML content)
    text = text.replaceAll("<[^>]*>", "").trim();
    if (text.isEmpty()) {
      return false;
    }
    char firstChar = Character.toUpperCase(text.charAt(0));
    return firstChar == startChar;
  }

  /**
   * Sets the ARIA role for this menu.
   * @param role The ARIA role (should be "menu")
   */
  public void setRole(String role) {
    if ("menu".equals(role)) {
      Roles.getMenuRole().set(menuBar.getElement());
    }
  }

  /**
   * Sets the aria-label attribute.
   * @param label The accessible label
   */
  public void setAriaLabel(String label) {
    menuBar.getElement().setAttribute("aria-label", label);
  }

  /**
   * Sets a unique ID on this menu's popup element.
   * Used for aria-controls relationships.
   * @param id The unique ID to set
   */
  public void setId(String id) {
    popupPanel.getElement().setId(id);
  }

  /**
   * Gets the ID of this menu's popup element.
   * @return The ID, or null if not set
   */
  public String getId() {
    return popupPanel.getElement().getId();
  }

}
