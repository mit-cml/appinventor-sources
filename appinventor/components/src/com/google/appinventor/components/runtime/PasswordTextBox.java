// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * Text box for entering passwords.
 *
 */
@DesignerComponent(version = YaVersion.PASSWORDTEXTBOX_COMPONENT_VERSION,
    description = "<p>A box for entering passwords.  This is the same as " +
    "the ordinary <code>TextBox</code> component except this does not " +
    "display the characters typed by the user.</p><p>The value of the text " +
    "in the box can be found or set through the <code>Text</code> property. " +
    "If blank, the <code>Hint</code> property, which appears as faint text " +
    "in the box, can provide the user with guidance as to what to type.</p> " +
    "<p>Text boxes are usually used with the <code>Button</code> " +
    "component, with the user clicking on the button when text entry is " +
    "complete.</p>",
    category = ComponentCategory.USERINTERFACE)
@SimpleObject
public final class PasswordTextBox extends TextBoxBase {
  /**
   * Creates a new PasswordTextBox component.
   *
   * @param container  container, component will be placed in
   */
  public PasswordTextBox(ComponentContainer container) {
    super(container, new EditText(container.$context()));

    // Disable auto-suggestion.
    view.setRawInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

    // make the box single line
    view.setSingleLine(true);
    // Add a transformation method to hide password text.   This must
    // be done after the SingleLine command
    view.setTransformationMethod(new PasswordTransformationMethod());

    // make sure the done action is Done and not Next.  See comment in Textbox.java
    view.setImeOptions(EditorInfo.IME_ACTION_DONE);

  }
}
