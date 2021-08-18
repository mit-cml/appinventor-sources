package com.google.appinventor.client.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

import com.google.appinventor.client.Ode;

public class Dropdown extends PopupPanel {

  private static Resources.DropdownStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dropdownStyleDark() : Resources.INSTANCE.dropdownStyleLight();

  private Button dropdownButton;
  private boolean center;

  public Dropdown() {
    super(true);
    style.ensureInjected();
    addStyleName(style.dropdown());
    hide();
  }

  public Dropdown(boolean center) {
    this();
    setCentered(center);
  }

  public void setCentered(boolean center) {
    this.center = center;
    setGlassEnabled(center);
    setAnimationEnabled(center);
    setAnimationType(AnimationType.CENTER);
  }
}
