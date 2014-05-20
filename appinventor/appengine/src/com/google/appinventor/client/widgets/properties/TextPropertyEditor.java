// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.widgets.properties;

import com.google.gwt.user.client.ui.TextBox;

/**
 * Property editor for short text.  Appears as a one-line box.
 *
 */
public class TextPropertyEditor extends TextPropertyEditorBase{

  public TextPropertyEditor() {
   super(new TextBox());
  }
}
