package com.google.appinventor.client.components;

public class DropdownButton extends Button {

  private static Resources.DropdownStyle style = Resources.INSTANCE.dropdownStyle();

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
