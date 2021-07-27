package com.google.appinventor.client.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import com.google.appinventor.client.Ode;

public class DropdownItem extends Label {

  private static Resources.DropdownStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dropdownStyleDark() : Resources.INSTANCE.dropdownStyleLight();

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
