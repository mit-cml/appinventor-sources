// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

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
  
  /**
   * Class representing a drop-down button with its associated menu. Note
   * that all items in the menu should have unique captions for removeItem
   * and setItemEnabled to work properly.
   */
  public static class DropDownButton extends TextButton {
    private final ContextMenu menu;
    private final List<MenuItem> items;
    
    // Create a new drop-down menu button, initially populated with items. Null
    // items in the list cause a separator to be added at that position.
    public DropDownButton(String widgetName, String caption, List<ToolbarItem> toolbarItems,
        final boolean rightAlign) {
      super(caption + " \u25BE");  // drop down triangle
      this.menu = new ContextMenu();
      this.items = new ArrayList<MenuItem>();
      for (ToolbarItem item : toolbarItems) {
        if (item != null) {
          this.items.add(menu.addItem(item.caption, item.command));
        } else {
          menu.addSeparator();
        }
      }
      addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          menu.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
              int left = getAbsoluteLeft();
              if (rightAlign) {
                left += getOffsetWidth() - offsetWidth;
              }
              int top = getAbsoluteTop() + getOffsetHeight();
              menu.setPopupPosition(left, top);
            }
          });
        }
      });
    }
    
    public void clearAllItems() {
      for (MenuItem item : items) {
        menu.removeItem(item);
      }
      items.clear();
    }
    
    public void addItem(ToolbarItem item) {
      items.add(menu.addItem(item.caption, item.command));
    }
    
    public void removeItem(String itemName) {
      for (MenuItem item : items) {
        if (item.getText().equals(itemName)) {
          menu.removeItem(item);
          items.remove(item);
          break;
        }
      }
    }
    
    public void setItemEnabled(String itemName, boolean enabled) {
      for (MenuItem item : items) {
        if (item.getText().equals(itemName)) {
          item.setEnabled(enabled);
          break;
        }
      }
    }
    
    public ContextMenu getContextMenu() {
      return menu;
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
  protected void addDropDownButton(String dropDownName, String caption, List<ToolbarItem> items,
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
   * Adds an item to a drop down button 
   *
   * @param dropDownName name used for internal map
   * @param item item to add to drop down
   */
  protected void addDropDownButtonItem(String dropDownName, ToolbarItem item) {
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
  protected void addDropDownButton(String dropDownName, String caption, List<ToolbarItem> items) {
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
