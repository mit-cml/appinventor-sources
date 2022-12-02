package com.google.appinventor.client.components;

import com.google.appinventor.client.Ode;

public class Button extends com.google.gwt.user.client.ui.Button {
  private GroupPosition position;
  private Type type;
  private String text;
  private boolean raised;
  private Icon icon;

  private static Resources.ButtonStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.buttonStyleDark() : Resources.INSTANCE.buttonStyleLight();

  public Button() {
    style.ensureInjected();
    this.type = Type.DEFAULT;
    this.position = GroupPosition.NONE;
    this.raised = false;
    setAppearance();
    setText("");
  }

  @Override
  public void setText(String text) {
    this.text = text;
    super.setHTML(makeText());
  }

  public void setIcon(String iconName) {
    icon = new Icon(iconName);
    super.setHTML(makeText());
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
    super.setHTML(makeText());
  }

  public void setGroupPosition(GroupPosition position) {
    this.position = position;
    setAppearance();
  }

  public void setType(Type type) {
    this.type = type;
    setAppearance();
  }

  public void setRaised(boolean raised) {
    this.raised = raised;
    setAppearance();
  }

  protected String makeText() {
    String text = "";
    if(icon != null) {
      text += icon.toString();
    }
    text+= this.text;
    return text;
  }

  private void setAppearance() {
    getElement().setAttribute("class", "");
    setStylePrimaryName(style.base());
    switch(position) {
      case LEFT: addStyleName(style.left()); break;
      case RIGHT: addStyleName(style.right()); break;
      case CENTER: addStyleName(style.center()); break;
      default: addStyleName(style.none()); break;
    }

    switch(type) {
      case PRIMARY: addStyleName(style.primary()); break;
      case DANGER: addStyleName(style.danger()); break;
      case INLINE: addStyleName(style.inline()); break;
      default: addStyleName(style.action()); break;
    }

    if(raised) {
      addStyleName(style.raised());
    }
  }

  public static enum GroupPosition {
    LEFT,
    CENTER,
    RIGHT,
    NONE
  }

  public static enum Type {
    PRIMARY,
    DANGER,
    INLINE,
    DEFAULT
  }
}
