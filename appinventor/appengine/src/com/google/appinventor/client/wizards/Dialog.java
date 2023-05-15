package com.google.appinventor.client.wizards;

import com.google.gwt.user.client.ui.DialogBox;

// Simple wrapper for the GWT Dialogbox that exposes the caption to UIBinder

public class Dialog extends DialogBox {


  private Caption caption;

  public Dialog() {
    super(false, true, new CaptionImpl());
    caption = getCaption();
    setGlassEnabled(true);
  }

  public void setCaption(String text) {
    caption.setText(text);
  }
}
