package com.google.appinventor.client.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

public class DropdownItem extends Label {

  private static Resources.DropdownStyle style = Resources.INSTANCE.dropdownStyle();

  public DropdownItem() {
    super();
    style.ensureInjected();
    setStylePrimaryName(style.dropdownItem());
  }

  public void setHasDivider(boolean hasDivider) {
    if(hasDivider) {
      addStyleName(style.dropdownItemWithDivider());
    } else {
      removeStyleName(style.dropdownItemWithDivider());
    }
  }
}
