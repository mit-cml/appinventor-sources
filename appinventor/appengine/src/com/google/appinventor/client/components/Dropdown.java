package com.google.appinventor.client.components;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class Dropdown extends PopupPanel {

  private static Resources.DropdownStyle style = Resources.INSTANCE.dropdownStyle();

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
    setCenter(center);
  }

  public void setDropdownButton(Button button) {
    dropdownButton = button;
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        open();
      }
    });
  }

  public void setCenter(boolean center) {
    this.center = center;
    setGlassEnabled(center);
    setAnimationEnabled(center);
    setAnimationType(AnimationType.CENTER);
  }

  public void open() {
    if(center) {
      center();
    } else {
      showRelativeTo(dropdownButton);
    }
  }
}
