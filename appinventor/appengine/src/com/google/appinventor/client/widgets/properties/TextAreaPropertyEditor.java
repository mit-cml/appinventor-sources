// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.user.client.ui.TextArea;

/**
 * Property editor for long text.  Appears as a scrollable, resizable area.
 */
public class TextAreaPropertyEditor extends TextPropertyEditorBase {

  /* same as the height set in TextPropertyEditorBase */
  private static final String MIN_CSS_HEIGHT = "2em";

  /* tightly coupled with the width of properties panel:
   * 186px == width of properties panel - side paddings and borders of textarea
   *       == 194px - 8px
   */
  private static final String MAX_CSS_WIDTH = "186px";

  public TextAreaPropertyEditor() {
   super(new TextArea());

   textEdit.getElement().getStyle().setProperty("minHeight", MIN_CSS_HEIGHT);
   textEdit.getElement().getStyle().setProperty("maxWidth", MAX_CSS_WIDTH);
  }
}
