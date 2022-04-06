package com.google.appinventor.client.components;

import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DialogBox.Caption;
import com.google.gwt.user.client.ui.DialogBox.CaptionImpl;

import com.google.appinventor.client.Ode;

public class Dialog extends DialogBox {

  private static Resources.DialogStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.dialogStyleDark() : Resources.INSTANCE.dialogStyleLight();

  private Caption caption;

  public Dialog() {
    super(false, true, new CaptionImpl());
    caption = getCaption();
    style.ensureInjected();
    setStylePrimaryName(style.dialog());
    setGlassEnabled(true);
  }

  public void setCaption(String text) {
    caption.setText(text);
  }
}
