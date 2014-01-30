// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.TextBoxBase;

/**
 * Property editor base for text box editors.
 *
 */
public class TextPropertyEditorBase extends PropertyEditor {

  /**
   * Thrown to indicate that text input is invalid.
   */
  public static class InvalidTextException extends Exception {
    public InvalidTextException(String message) {
      super(message);
    }
  }

  // This is the GWT object that supports the property editor
  // It can be TextBoxBase object, currently in App Inventor only
  // TextBox and TextArea
  protected TextBoxBase textEdit;

  private boolean hasFocus;

  /**
   * Creates a new instance of the property editor.
   */
  public TextPropertyEditorBase(final TextBoxBase widget) {

    textEdit = widget;

    textEdit.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        handleKeyPress(event.getCharCode());
      }
    });
    textEdit.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        handleKeyUp(event.getNativeKeyCode());
      }
    });
    textEdit.addValueChangeHandler(new ValueChangeHandler() {
      @Override
      public void onValueChange(ValueChangeEvent event) {
        validateText();
      }
    });

    // NOTE(lizlooney) - The following handlers for focus, blur, and click are needed to workaround
    // a bug with WebKit browsers (chrome and safari) where clicking in the TextBox causes it to
    // gain focus, but then immediately lose focus (blur). To work around the problem, we keep
    // track of whether the TextBox has focus using a FocusHandler and a BlurHandler. Then, we use
    // a ClickHandler and if we get a ClickEvent and the TextBox does not have focus, we explicitly
    // call setFocus.
    textEdit.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        hasFocus = true;
      }
    });
    textEdit.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        hasFocus = false;
        // Calling validateText here means that we will save the changed property value (if it is
        // valid) when this property editor loses focus (for example, when the user clicks on
        // another property editor).
        validateText();
      }
    });
    textEdit.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (!hasFocus) {
          textEdit.setFocus(true);
        }
      }
    });

    initWidget(textEdit); //kludge for now fix this with instanceOf?

    setHeight("2em");
  }

  @Override
  protected void onUnload() {
    // onUnload is called immediately before a widget becomes detached from the browser's document.
    // Calling validateText here means that we will save the changed property value (if it is
    // valid) when the user clicks on another component.
    validateText();
    super.onUnload();
  }

  @Override
  protected void updateValue() {
    textEdit.setText(property.getValue());
  }

  private void handleKeyPress(char keyCode) {
    if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
      // Pressing <tab>, <enter> or <return> will surrender focus.
      textEdit.cancelKey();
      textEdit.setFocus(false);
    } else if (!validateKeyCode(keyCode)) {
      textEdit.cancelKey();
    }
  }

  private void handleKeyUp(int keyCode) {
    if (keyCode == KeyCodes.KEY_ESCAPE) {
      // Pressing <esc> will reset the content of the editor to the previous property value as well
      // as surrender focus.
      updateValue();  // Restore previous property value.
      textEdit.cancelKey();
      textEdit.setFocus(false);
    }
  }

  /*
   * Validates the text in the textEdit and if it is valid, sets the property
   * value to the text.
   */
  private void validateText() {
    String text = textEdit.getText();
    try {
      validate(text);
      property.setValue(text);
    } catch (InvalidTextException e) {
      String error = e.getMessage();
      if (error == null || error.isEmpty()) {
        error = MESSAGES.malformedInputError();
      }
      Window.alert(error);
      updateValue();  // Restore previous property value.
    }
  }

  /**
   * Validates the given key code.
   *
   * <p/>The implementation here does no validation. Subclasses may override
   * this method to provide actual validation.
   *
   * @param keyCode  key code to validate
   * @return true if the keycode is allowed, false if it is not allowed.
   */
  protected boolean validateKeyCode(char keyCode) {
    return true;
  }

  /**
   * Validates the given text. Throw an InvalidTextException if the text is
   * invalid.
   *
   * <p/>The implementation here does no validation. Subclasses may override
   * this method to provide actual validation.
   *
   * @param text  input string to validate
   * @throws InvalidTextException if the text is invalid
   */
  protected void validate(String text) throws InvalidTextException {
  }


}
