// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The toolbar houses command buttons (currently only used by Young Android).
 *
 */
public class Toolbar extends Composite {
  /**
   * Initial attributes for a Toolbar item.
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

  // Mappings of name to widget.
  // Note that the name does not need to be the same as the caption.
  private final Map<String, TextButton> buttonMap;
  private final Map<String, TextButton> dropDownButtonMap;
  private final Map<String, MenuItem> dropDownItemMap;

  private final HorizontalPanel leftButtons;
  private final HorizontalPanel rightButtons;

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public Toolbar() {
    buttonMap = new HashMap<String, TextButton>();
    dropDownButtonMap = new HashMap<String, TextButton>();
    dropDownItemMap = new HashMap<String, MenuItem>();

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
   * Sets enabled for drop down button
   *
   * @param widgetName name of button
   * @param enabled enabled status
   */
  public void setDropDownButtonEnabled(String widgetName, boolean enabled) {
    dropDownButtonMap.get(widgetName).setEnabled(enabled);
  }

  /**
   * Sets enabled for drop down item
   *
   * @param widgetName name of drop down item
   * @param enabled enabled status
   */
  public void setDropItemEnabled(String widgetName, boolean enabled) {
    dropDownItemMap.get(widgetName).setEnabled(enabled);
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
   * @param items list of items to add to drop down
   * @param rightAlign button should be right aligned in toolbar
   */
  protected void addDropDownButton(String dropDownName, String caption, List<ToolbarItem> items,
      final boolean rightAlign) {
    final TextButton button = new TextButton(caption + " \u25BE");  // drop down triangle
    final ContextMenu contextMenu = new ContextMenu();
    for (ToolbarItem item : items) {
      MenuItem menuItem = contextMenu.addItem(item.caption, item.command);
      dropDownItemMap.put(item.widgetName, menuItem);
    }
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        contextMenu.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          @Override
          public void setPosition(int offsetWidth, int offsetHeight) {
            int left = button.getAbsoluteLeft();
            if (rightAlign) {
              left += button.getOffsetWidth() - offsetWidth;
            }
            int top = button.getAbsoluteTop() + button.getOffsetHeight();
            contextMenu.setPopupPosition(left, top);
          }
        });
      }
    });
    if (rightAlign) {
      rightButtons.add(button);
    } else {
      leftButtons.add(button);
    }
    dropDownButtonMap.put(dropDownName, button);
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
    dropDownButtonMap.get(widgetName).setText(caption);
  }
}
