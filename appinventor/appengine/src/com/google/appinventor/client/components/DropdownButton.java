package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;

public class DropdownButton extends Button {

  private static Resources.DropdownStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dropdownStyleDark() : Resources.INSTANCE.dropdownStyleLight();

  public DropdownButton() {
    super();
    style.ensureInjected();
  }

  @Override
  protected String makeText() {
    Icon arrowIcon = new Icon("arrow_drop_down");
    arrowIcon.addStyleName(style.buttonIcon());
    return super.makeText() + arrowIcon.toString();
  }
}
