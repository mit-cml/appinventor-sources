// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.gwt.user.client.ui.DialogBox;

// Simple wrapper for the GWT Dialogbox that exposes the caption to UIBinder

public class Dialog extends DialogBox {


  private Caption caption;

  public Dialog() {
    super(false, true, new CaptionImpl());
    caption = getCaption();
    setGlassEnabled(true);
    setModal(false);
  }

  public void setCaption(String text) {
    caption.setText(text);
  }
}
