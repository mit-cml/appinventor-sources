// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.user.client.ui.TextArea;

/**
 * Property editor for long text.  Appears as a scrollable, resizable area.
 *
 */
public class TextAreaPropertyEditor extends TextPropertyEditorBase{

  public TextAreaPropertyEditor() {
   super(new TextArea());
  }
}
