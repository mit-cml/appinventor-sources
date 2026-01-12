// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.uibinder.client.ElementParserToUse;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Toolbar is a horizontal arrangement of buttons (which can be a mixture
 * of regular and drop-down buttons). There is a left-aligned group of buttons
 * and a right-aligned group.
 *
 */
@ElementParserToUse(className = "com.google.appinventor.client.widgets.ToolbarParser")
public class Toolbar extends FlowPanel {
  private static final String DROP_DOWN_TRIANGLE = "\u25BE";

  private static final Map<String, ToolbarPrototype> prototypes = new HashMap<>();

  // All mappings are widget name to widget.
  
  // Regular button widgets
  private final Map<String, TextButton> buttonMap = new LinkedHashMap<>();
  // Drop-down button widgets
  private final Map<String, DropDownButton> dropDownButtonMap = new LinkedHashMap<>();


  private final FlowPanel leftButtons = new FlowPanel();
  private final FlowPanel rightButtons = new FlowPanel();
  private final FlowPanel centerButtons = new FlowPanel();

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public Toolbar() {
    setStylePrimaryName("ya-Toolbar");
    leftButtons.setStylePrimaryName("left");
    rightButtons.setStylePrimaryName("right");
    centerButtons.setStylePrimaryName("center");

    super.add(leftButtons);
    super.add(centerButtons);
    super.add(rightButtons);
  }

  public interface ToolbarBuilder {
    void update(Toolbar toolbar);
  }

  public static class ToolbarPrototype {
    private final List<ToolbarBuilder> builders = new ArrayList<>();

    public void add(ToolbarBuilder item) {
      builders.add(item);
    }
  }

  public static ToolbarPrototype getPrototype(String name) {
    if (!prototypes.containsKey(name)) {
      prototypes.put(name, new ToolbarPrototype());
    }
    return prototypes.get(name);
  }

  protected final void build(String name) {
    for (ToolbarBuilder builder : getPrototype(name).builders) {
      builder.update(this);
    }
  }

  public void extend(Toolbar toolbar) {
    add(toolbar);
  }

  public void add(Widget widget) {
    leftButtons.add(widget);
  }

  public void add(DropDownButton button) {
    dropDownButtonMap.put(button.getName(), button);
    if ("right".equals(button.getAlign())) {
      rightButtons.add(button);
    } else if ("center".equals(button.getAlign())) {
      centerButtons.add(button);
    } else {
      leftButtons.add(button);
    }
  }

  public void add(final ToolbarItem item) {
    TextButton button = new TextButton(item.getCaption(), item.getIcon(), item.getStyleName());
    button.setEnabled(item.getEnabled());
    if (!item.getTooltip().isEmpty()) {
      button.setTitle(item.getTooltip());
    }
    button.addClickHandler(event -> item.getCommand().execute());
    if ("right".equals(item.getAlign())) {
      rightButtons.add(button);
    } else if ("center".equals(item.getAlign())) {
      centerButtons.add(button);
    } else {
      leftButtons.add(button);
    }
    buttonMap.put(item.getName(), button);
    button.setVisible(item.isVisible());
  }

  public void addRight(final ToolbarItem item) {
    item.setAlign("right");
    add(item);
  }

  protected void setEnabledItem(ToolbarItem item, boolean enabled) {
    TextButton button = buttonMap.get(item.getName());
    if (button != null) {
      button.setEnabled(enabled);
    }
  }

  protected void setVisibleItem(ToolbarItem item, boolean visible) {
    TextButton button = buttonMap.get(item.getName());
    if (button != null) {
      button.setVisible(visible);
    }
  }

  public void setStylename(String styleName) {
    setStylePrimaryName(styleName);
  }

  protected void populateToolbar(Toolbar other) {
    leftButtons.clear();
    rightButtons.clear();
    centerButtons.clear();
    buttonMap.clear();
    dropDownButtonMap.clear();
    while (other.leftButtons.getWidgetCount() > 0) {
      Widget w = other.leftButtons.getWidget(0);
      w.removeFromParent();
      leftButtons.add(w);
    }
    while (other.centerButtons.getWidgetCount() > 0) {
      Widget w = other.centerButtons.getWidget(0);
      w.removeFromParent();
      centerButtons.add(w);
    }
    while (other.rightButtons.getWidgetCount() > 0) {
      Widget w = other.rightButtons.getWidget(0);
      w.removeFromParent();
      rightButtons.add(w);
    }
    buttonMap.putAll(other.buttonMap);
    dropDownButtonMap.putAll(other.dropDownButtonMap);
  }

  /**
   * Sets enabled for button
   *
   * @param widgetName name of button
   * @param enabled enabled status
   */
  public void setButtonEnabled(String widgetName, boolean enabled) {
    TextButton button = buttonMap.get(widgetName);
    if (button != null) {
      button.setEnabled(enabled);
    }
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
    if (buttonMap.containsKey(widgetName)) {
      buttonMap.get(widgetName).setVisible(enabled);
    }
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
    if (dropDownButtonMap.containsKey(widgetName)) {
      dropDownButtonMap.get(widgetName).setVisible(enabled);
    }
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
   * Adds an item to a drop down button 
   *
   * @param dropDownName name used for internal map
   * @param item item to add to drop down
   */
  protected void addDropDownButtonItem(String dropDownName, DropDownItem item) {
    final DropDownButton button = dropDownButtonMap.get(dropDownName);
    if (button != null && item != null) {
      button.addItem(item);
    }
  }

  protected void addDropDownButtonSeparator(String dropDownName) {
    final DropDownButton button = dropDownButtonMap.get(dropDownName);
    if (button != null) {
      button.addSeparator();
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

  protected void removeDropDownButtonSeparator(String dropDownName) {
    final DropDownButton button = dropDownButtonMap.get(dropDownName);
    if (button != null) {
      button.removeSeparator();
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

  protected void setDropDownItemHtml(String dropDownName, String itemName, String newHtml) {
    final DropDownButton button = dropDownButtonMap.get(dropDownName);
    if (button != null) {
      button.setItemHtmlById(itemName, newHtml);
    }
  }
}
