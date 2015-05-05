// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Toolbar is a horizontal arrangement of buttons (which can be a mixture
 * of regular and drop-down buttons). There is a left-aligned group of buttons
 * and a right-aligned group.
 *
 */
public class Toolbar extends Composite {
  /**
   * A Toolbar item has a widgetName that is unique over all items in
   * the toolbar, a caption (displayed to the user)
   * and a command to run when the item is selected.
   */
  public static class ToolbarItem {
    private final String widgetName;
    private final String caption;
    private final Command command;

    public ToolbarItem(String widgetName, String caption, Command command) {
      this.widgetName = widgetName;
      this.caption = caption;
      this.command = command;
    }
  }
  private static final String DROP_DOWN_TRIANGLE = "\u25BE";

  // All mappings are widget name to widget.
  
  // Regular button widgets
  private final Map<String, TextButton> buttonMap;
  // Drop-down button widgets
  private final Map<String, DropDownButton> dropDownButtonMap;


  private final HorizontalPanel leftButtons;
  private final HorizontalPanel rightButtons;

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public Toolbar() {
    buttonMap = new HashMap<String, TextButton>();
    dropDownButtonMap = new HashMap<String, DropDownButton>();

    leftButtons = new HorizontalPanel();
    leftButtons.setSpacing(4);

    rightButtons = new HorizontalPanel();
    rightButtons.setSpacing(4);
    rightButtons.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);

    HorizontalPanel toolbar = new HorizontalPanel();
    toolbar.add(leftButtons);  // this nesting keeps buttons left aligned
    toolbar.add(rightButtons);
    toolbar.setCellHorizontalAlignment(rightButtons, HorizontalPanel.ALIGN_RIGHT);
    toolbar.setWidth("100%");
    toolbar.setStylePrimaryName("ya-Toolbar");

    initWidget(toolbar);
  }

  /**
   * Sets enabled for button
   *
   * @param widgetName name of button
   * @param enabled enabled status
   */
  public void setButtonEnabled(String widgetName, boolean enabled) {
    buttonMap.get(widgetName).setEnabled(enabled);
  }

  /**
   * Sets text for button
   *
   * @param widgetName name of button
   * @param text text of button
   */
  public void setButtonText(String widgetName, String text) {
    buttonMap.get(widgetName).setText(text);
  }

  /**
   * Sets button visibility
   *
   * @param widgetName name of button
   * @param enabled enabled status
   */
  public void setButtonVisible(String widgetName, boolean enabled) {
    buttonMap.get(widgetName).setVisible(enabled);
  }

  /**
   * Sets enabled for drop down button
   *
   * @param widgetName name of button
   * @param enabled enabled status
   */
  public void setDropDownButtonEnabled(String widgetName, boolean enabled) {
    dropDownButtonMap.get(widgetName).setEnabled(enabled);
  }

  /**
   * Sets drop down button visibility
   *
   * @param widgetName name of button
   * @param enabled enabled status
   */
  public void setDropDownButtonVisible(String widgetName, boolean enabled) {
    dropDownButtonMap.get(widgetName).setVisible(enabled);
  }

  /**
   * Sets enabled for drop down item
   *
   * @param dropWidgetName name of drop-down widget
   * @param itemName name of item within dropWidgetName
   * @param enabled enabled status
   */
  public void setDropItemEnabled(String dropWidgetName, String itemName, boolean enabled) {
    final DropDownButton button = dropDownButtonMap.get(dropWidgetName);
    if (button != null) {
      button.setItemEnabled(itemName, enabled);
    }
  }

  /**
   * Adds a button to the toolbar
   *
   * @param item button to add
   * @param rightAlign {@code true} if the button should be right-aligned,
   *                   {@code false} if left-aligned
   */
  protected void addButton(final ToolbarItem item, boolean rightAlign) {
    TextButton button = new TextButton(item.caption);
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        item.command.execute();
      }
    });
    if (rightAlign) {
      rightButtons.add(button);
    } else {
      leftButtons.add(button);
    }
    buttonMap.put(item.widgetName, button);
  }

  /**
   *
   * @param item button to add
   * @param rightAlign true if button is right-aligned, false if left
   * @param top special styling if the button is on the top.
   */
  protected void addButton(final ToolbarItem item, boolean rightAlign, boolean top) {
    TextButton button = new TextButton(item.caption);
    button.setStyleName("ode-TopPanelDropDownButton");
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        item.command.execute();
      }
    });
    if (rightAlign) {
      rightButtons.add(button);
    } else {
      leftButtons.add(button);
    }
    buttonMap.put(item.widgetName, button);
  }

  /**
   * Adds a (left-aligned) button to the toolbar
   *
   * @param item the button to add
   */
  protected void addButton(ToolbarItem item) {
    addButton(item, false);
  }

  /**
   * Adds a drop down button to the toolbar
   *
   * @param dropDownName name used for internal map
   * @param caption name of button
   * @param items list of items to add to drop down; null items indicate a separator should be
   *        addded
   * @param rightAlign button should be right aligned in toolbar
   */
  protected void addDropDownButton(String dropDownName, String caption, List<DropDownButton.DropDownItem> items,
      final boolean rightAlign) {
    final DropDownButton button = new DropDownButton(dropDownName, caption,
        items, rightAlign);
    if (rightAlign) {
      rightButtons.add(button);
    } else {
      leftButtons.add(button);
    }
    dropDownButtonMap.put(dropDownName, button);
  }

  /**
   *
   * @param dropDownName name used for internal map
   * @param caption name of button
   * @param items list of items to add to the drop down; null items indicate a separator
   * @param rightAlign button should be right aligned in toolbar
   * @param top special style if button is added to the topPanel
   */
  protected void addDropDownButton(String dropDownName, String caption, List<DropDownButton.DropDownItem> items,
      final boolean rightAlign, final boolean top) {
    final DropDownButton button = new DropDownButton(dropDownName, caption,
        items, rightAlign);
    button.setStyleName("ode-TopPanelDropDownButton");
    if (rightAlign) {
      rightButtons.add(button);
    } else {
      leftButtons.add(button);
    }
    dropDownButtonMap.put(dropDownName, button);
  }

  /**
   * Adds an item to a drop down button 
   *
   * @param dropDownName name used for internal map
   * @param item item to add to drop down
   */
  protected void addDropDownButtonItem(String dropDownName, DropDownButton.DropDownItem item) {
    final DropDownButton button = dropDownButtonMap.get(dropDownName);
    if (button != null && item != null) {
      button.addItem(item);
    }
  }

  /**
   * Removes an item from a drop down button if it exists.
   *
   * @param dropDownName name used for internal map
   * @param itemName  name (text) of item to remove from drop down
   */
  protected void removeDropDownButtonItem(String dropDownName, String itemName) {
    final DropDownButton button = dropDownButtonMap.get(dropDownName);
    if (button != null) {
      button.removeItem(itemName);
    }
  }
  
  /**
   * Clear all items from a drop-down button menu
   * 
   * @param dropDownName the name of the drop-down button whose menu should
   *   be cleared
   */
  protected void clearDropDownMenu(String dropDownName) {
    dropDownButtonMap.get(dropDownName).clearAllItems();
  }
  
  /**
   * Adds a (left-aligned) drop down button to the toolbar
   *
   * @param dropDownName name used for internal map
   * @param caption name of button
   * @param items list of items to add to drop down
   */
  protected void addDropDownButton(String dropDownName, String caption, List<DropDownButton.DropDownItem> items) {
    addDropDownButton(dropDownName, caption, items, false);
  }

  /**
   * Changes a button's caption
   *
   * @param widgetName name of button
   * @param caption the new caption
   */
  protected void setButtonCaption(String widgetName, String caption) {
    buttonMap.get(widgetName).setText(caption);
  }

  /**
   * Changes a drop down button's caption
   *
   * @param widgetName name of button
   * @param caption the new caption
   */
  protected void setDropDownButtonCaption(String widgetName, String caption) {
    dropDownButtonMap.get(widgetName).setText(caption + " " + DROP_DOWN_TRIANGLE);
  }
}
