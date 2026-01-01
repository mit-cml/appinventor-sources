// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;          // New import
import com.google.gwt.user.client.ui.VerticalPanel;    // New import
import com.google.gwt.event.dom.client.KeyUpEvent;     // New import
import com.google.gwt.event.dom.client.KeyUpHandler;   // New import
import java.util.ArrayList;                            // New import
import java.util.List;                                 // New  import

/**
 * Context menu widget implementation.
 *
 */
public final class ContextMenu {

  // UI elements
  private final PopupPanel popupPanel;
  private final MenuBar menuBar;
  private final List<MenuItem> items = new ArrayList<>(); // All items  list
  private final List<MenuItemSeparator> separators = new ArrayList<>(); // New list for separators

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
    //popupPanel.add(menuBar);
     
     //Add line from 45-49
    // make a  VerticalPanel,which hold Search Box and MenuBar 
    VerticalPanel container = new VerticalPanel();
    container.add(menuBar); // first add menuBar 
    
    popupPanel.add(container);
    addSearchFilter();
  }

  //add line from 54-79

  public void addSearchFilter() {
    final TextBox searchBox = new TextBox();
    searchBox.getElement().setAttribute("placeholder", "Search language...");
    searchBox.setStyleName("ode-ContextMenuItem");
    searchBox.getElement().getStyle().setProperty("width", "100%");

    searchBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        String query = searchBox.getText().toLowerCase().trim();
        boolean isSearching = !query.isEmpty();
        //filter language
        for (MenuItem item : items) {
          boolean matches = item.getText().toLowerCase().contains(query);
          item.getElement().getStyle().setProperty("display", matches ? "" : "none");
        }

        // 2.  handle Separators  
    for (MenuItemSeparator sep : separators) {
      // if search then show seperator otherwise hide it
      sep.getElement().getStyle().setProperty("display", isSearching ? "none" : "");
    }
      }
    });

    VerticalPanel container = (VerticalPanel) popupPanel.getWidget();
    container.insert(searchBox, 0); 
    
    popupPanel.addAttachHandler(event -> {
      if (event.isAttached()) {
        searchBox.setFocus(true);
      }
    });
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
    menuBar.addItem(menuItem);
    items.add(menuItem); // <--  line add 
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
    menuBar.addItem(menuItem);
    items.add(menuItem); // <-- line add 
    return menuItem;
  }

  /**
   * Removes a menu item from the context menu.
   *
   * @param item  menu item to be removed
   */
  public void removeItem(MenuItem item) {
    menuBar.removeItem(item);
    items.remove(item); // <--  line add 
  }

  /**
   * Adds a separator to the context menu.
   */
  public MenuItemSeparator addSeparator() {
    MenuItemSeparator menuItemSeparator = menuBar.addSeparator();
    menuItemSeparator.setStylePrimaryName("ode-ContextMenuItemSeparator");
    separators.add(menuItemSeparator); //  add line
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

}
