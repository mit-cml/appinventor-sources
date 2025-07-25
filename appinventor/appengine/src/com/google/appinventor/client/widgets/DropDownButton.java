// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2013-2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.utils.PZAwarePositionCallback;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.ElementParserToUse;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuItem;

import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.UIObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class representing a drop-down button with its associated menu. Note
 * that all items in the menu should have unique captions for removeItem
 * and setItemEnabled to work properly.
 */
@ElementParserToUse(className = "com.google.appinventor.client.widgets.DropDownButtonParser")
public class DropDownButton extends TextButton {

  private String name = "";
  private final ContextMenu menu = new ContextMenu();
  private final Map<String, MenuItem> itemsById = new HashMap<>();
  private final List<MenuItem> items = new ArrayList<>();
  private final List<UIObject> allItems = new ArrayList<>();
  private boolean rightAlign;
  private String align = "left";
  private Icon icon = null;
  private String caption = "";
  private MenuItemSeparator separator = null;

  /**
   * A subclass of PZAwarePositionCallback designed to position the ContextMenu
   * of a DropDownButton.
   */
  private class DropDownPositionCallback extends PZAwarePositionCallback {
    public DropDownPositionCallback(Element elem) {
      super(elem);
    }

    /**
     * @param offsetWidth width of the ContextMenu being positioned on the parent element
     * @param offsetHeight height of the ContextMenu being positioned on the parent element
     * Sets the position of the ContextMenu on the screen given it's dimensions
     */
    @Override
    public void setPosition(int offsetWidth, int offsetHeight) {

      // getAbsoluteLeft/Right() gives the top coordinate of the parent element
      // getOffsetWidth/Height() gives the width/height of the parent element
      int left = Window.Navigator.getUserAgent().contains("Chrome") && isPinchZoomed()
              ? getTrueAbsoluteLeft() : getAbsoluteLeft();

      if (rightAlign) {
        left += getOffsetWidth() - offsetWidth;
      }

      int top = Window.Navigator.getUserAgent().contains("Chrome") && isPinchZoomed()
              ? getTrueAbsoluteTop() + getOffsetHeight()
              : getAbsoluteTop() + getOffsetHeight();

      // Values to determine how to display the ContextMenu - above or below

      int dropDownBottom = top + offsetHeight;
      int screenBottom  = Window.getScrollTop()+Window.getClientHeight();

      // if the bottom will go off the current browser screen, display
      // the dropdown as a 'dropup' where the ContextMenu appears
      // above instead

      if(dropDownBottom > screenBottom) {
          int newTop = Window.Navigator.getUserAgent().contains("Chrome") && isPinchZoomed()
            ? getTrueAbsoluteTop() -offsetHeight
            : getAbsoluteTop() - offsetHeight;

          // account for the extreeeemely unlikely case where newTop
          // also goes off the screen in this case, it makes more
          // sense to just go off the bottom of the screen (the screen
          // won't grow up, and so the menu would get completely cut
          // off at the top

          if(newTop >= 0) {
              top = newTop;
          }
      }

      menu.setPopupPosition(left, top);

    }
  }

  @SuppressWarnings("unused")  // invoked by GWT
  public DropDownButton() {
    super();

    addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (menu.isShowing()) {
          menu.hide();
        } else {
          menu.resetSelection();
          menu.setPopupPositionAndShow(new DropDownPositionCallback(getElement()));
        }
      }
    });

    addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN && menu.isShowing()) {
          event.preventDefault();
          menu.moveSelectionDown();
          menu.focus();
        } else if (event.getNativeKeyCode() == KeyCodes.KEY_UP && menu.isShowing()) {
          event.preventDefault();
          menu.moveSelectionUp();
          menu.focus();
        }
      }
    });

    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (event.getTypeInt() == Event.ONKEYDOWN && nativeEvent.getKeyCode() == KeyCodes.KEY_TAB && menu.isShowing()) {
          nativeEvent.preventDefault();
          menu.hide();
          setFocus(true);
        }
      }
    });
  }

  // Create a new drop-down menu button (with text), initially populated with items. Null
  // items in the list cause a separator to be added at that position.
  public DropDownButton(String name, String caption, List<DropDownItem> toolbarItems,
                        boolean rightAlign) {
    this();

    setCaption(caption + " \u25BE ");  // drop down triangle
    this.name = name;
    this.rightAlign = rightAlign;

    for (DropDownItem item : toolbarItems) {
      if (item != null) {
        MenuItem m = menu.addItem(item.caption, true, item.command);
        if (item.dependentStyleName != null) {
          m.addStyleDependentName(item.dependentStyleName);
        }
        if (!item.getVisible()) {
          m.setVisible(false);
        }
        this.items.add(m);
      } else {
        menu.addSeparator();
      }
    }
  }

  public DropDownButton(String name, String caption, List<DropDownItem> toolbarItems,
                        boolean rightAlign, boolean hasTriangle, boolean hasHtmlCaption) {
    this(name, caption, toolbarItems, rightAlign);

    if (hasHtmlCaption) {
      // Set the button's caption as an HTML String with or without a dropdown triangle
      if (hasTriangle) {
        setCaption(caption);
      } else {
        setHTML(caption);
      }
    }
  }

  protected String makeText(String caption, Icon icon, boolean hasTriangle) {
    String text = "";
    if (icon != null) {
      text += icon.toString();
    }
    text+= caption;
    if (hasTriangle) {
      text+= " \u25BE ";
    }
    return text;
  }

  public String getAlign() {
    return align;
  }

  public void setAlign(String align) {
    this.align = align;
  }

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(String iconName) {
    setIcon(new com.google.appinventor.client.components.Icon(iconName));
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
    setHTML(makeText(caption, icon, true));
  }

  public void clearAllItems() {
    for (MenuItem item : items) {
      menu.removeItem(item);
    }
    if (separator != null) {
      menu.removeSeparator(separator);
      separator = null;
    }
    items.clear();
  }

  public void addItem(DropDownItem item) {
    if (item == null) {
      allItems.add(menu.addSeparator());
    } else {
      String content = item.caption;
      if (item.icon != null) {
        content = "<img src=\"" + item.icon.getUrl() + "\">&nbsp;" + content;
      }
      MenuItem menuItem = menu.addItem(content, true, item.command, item.styleName);
      if (item.dependentStyleName != null) {
        menuItem.addStyleDependentName(item.dependentStyleName);
      }
      if (!item.getVisible()) {
        menuItem.setVisible(false);
      }
      itemsById.put(item.getName(), menuItem);
      items.add(menuItem);
      allItems.add(menuItem);
    }
  }

  /**
   * Removes a menu item identified by {@code id} if it exists.
   *
   * @param id the identifier of the menu item
   */
  public void removeItemById(String id) {
    if (itemsById.containsKey(id)) {
      MenuItem item = itemsById.remove(id);
      items.remove(item);
      allItems.remove(item);
      menu.removeItem(item);
    }
  }

  public void removeItem(String itemName) {
    for (MenuItem item : items) {
      String strippedItemText = item.getText().replaceAll("^\\s+", "");
      if (strippedItemText.equals(itemName)) {
        menu.removeItem(item);
        items.remove(item);
        allItems.remove(item);
        break;
      }
    }
  }

  /**
   * Enables or disables a menu item identified by {@code id}.
   *
   * @param id the identifier of the menu item
   * @param enabled true if the menu item should be enabled, false for disabled
   */
  public void setItemEnabledById(String id, boolean enabled) {
    MenuItem item = itemsById.get(id);
    if (item != null) {
      item.setEnabled(enabled);
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

  public void addSeparator() {
    if (separator == null) {
      separator = menu.addSeparator();
    }
  }

  public void removeSeparator() {
    if (separator != null) {
      menu.removeSeparator(separator);
      separator = null;
    }
  }

  @SuppressWarnings("deprecation")
  public void setCommand(String itemName, final Command command) {
    for (MenuItem item : items) {
      if (item.getText().equals(itemName)) {
        item.setCommand(new Command() {
          @Override
          public void execute() {
            menu.hide();
            command.execute();
          }
        });
        break;
      }
    }
  }

  @SuppressWarnings("deprecation")
  public void setCommandById(String id, final Command command) {
    MenuItem item = itemsById.get(id);
    if (item != null) {
      item.setCommand(new Command() {
        @Override
        public void execute() {
          menu.hide();
          command.execute();
        }
      });
    }
  }

  public void removeUnneededSeparators() {
    Iterator<UIObject> it = allItems.iterator();
    boolean first = true;
    boolean previousWasSeparator = false;
    while (it.hasNext()) {
      UIObject object = it.next();
      if (object instanceof MenuItemSeparator) {
        if (first || previousWasSeparator) {
          it.remove();
          menu.removeSeparator((MenuItemSeparator) object);
        }
        previousWasSeparator = true;
      } else if (!object.isVisible()) {
        // treat invisible objects as separators for this algorithm
        it.remove();
        menu.removeItem((MenuItem) object);
      } else {
        previousWasSeparator = false;
      }
      first = false;
    }
    while (allItems.get(allItems.size() - 1) instanceof MenuItemSeparator) {
      menu.removeSeparator((MenuItemSeparator) allItems.remove(allItems.size() - 1));
    }
  }

  public void setItemVisible(String itemName, boolean enabled) {
    for (MenuItem item : items) {
      if (item.getText().equals(itemName)) {
        item.setVisible(enabled);
        break;
      }
    }
  }

  public void setItemVisibleById(String id, boolean visible) {
    MenuItem item = itemsById.get(id);
    if (item != null) {
      item.setVisible(visible);
    }
  }

  public void replaceLastItem(DropDownItem item) {
    menu.removeItem(items.get(items.size()-1));
    items.remove(items.size()-1);
    items.add(menu.addItem(item.caption, true, item.command));
  }

  /**
   * Sets the HTML content of a menu item, identified by {@code id}, to the given {@code html}.
   *
   * @param id the identifier of the menu item
   * @param html the HTML content to use for the menu item
   */
  public void setItemHtmlById(String id, String html) {
    MenuItem item = itemsById.get(id);
    if (item != null) {
      item.setHTML(html);
    }
  }

  public void setName(String widgetName) {
    this.name = widgetName;
  }

  public String getName() {
    return name;
  }

  public void setCaption(String caption) {
    this.caption = caption;
    this.setHTML(makeText(caption, icon, true));
  }

  @SuppressWarnings("unused")  // Invoked by GWT
  public void setRightAlign(boolean rightAlign) {
    this.rightAlign = rightAlign;
  }

  public void setItems(Collection<? extends DropDownItem> items) {
    for (MenuItem item : this.items) {
      this.menu.removeItem(item);
    }
    this.items.clear();
    for (DropDownItem item : items) {
      addItem(item);
    }
  }
}
