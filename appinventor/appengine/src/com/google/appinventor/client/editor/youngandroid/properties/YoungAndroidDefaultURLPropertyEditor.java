// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;

/**
 * Property editor for a URL that has a default value.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public class YoungAndroidDefaultURLPropertyEditor extends PropertyEditor {

  private final CheckBox setDefaultCheckbox = new CheckBox(MESSAGES.useDefault());
  private final TextBox urlField = new TextBox();
  private final String defaultURL;

  public YoungAndroidDefaultURLPropertyEditor() {
    this("");
  }

  /**
   * Creates a new length property editor.
   *
   * @param includePercent  whether to include percent of screen option
   */
  public YoungAndroidDefaultURLPropertyEditor(final String defaultURL) {
    // The radio button group cannot be shared across all instances, so we append a unique id.
    this.defaultURL = defaultURL;
    Panel panel = new VerticalPanel();
    panel.add(urlField);
    panel.add(setDefaultCheckbox);

    urlField.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if (setDefaultCheckbox.isChecked()) {
            setDefaultCheckbox.setChecked(false);
          }
        }
      });

    urlField.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        handleKeyPress(event.getCharCode());
      }
    });

    urlField.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        handleKeyUp(event.getNativeKeyCode());
      }
    });

    urlField.addValueChangeHandler(new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent event) {
          property.setValue(urlField.getText());
        }
      });

    urlField.addBlurHandler(new BlurHandler() {
        @Override
        public void onBlur(BlurEvent event) {
          // We lost focus
          if (urlField.getText().equals(defaultURL)) {
            setDefaultCheckbox.setChecked(true);
          }
        }
      });

    setDefaultCheckbox.addValueChangeHandler(new ValueChangeHandler() {
        @Override
        public void onValueChange(ValueChangeEvent event) {
          if (setDefaultCheckbox.isChecked()) {
            urlField.setText(defaultURL);
            property.setValue(defaultURL);
          }
        }
      });

    initWidget(panel);

  }


  @Override
  protected void updateValue() {

    String propertyValue = property.getValue();
    urlField.setText(propertyValue);
    if (propertyValue.equals("DEFAULT")) {
      setDefaultCheckbox.setChecked(true);
    } else {
      setDefaultCheckbox.setChecked(false);
    }
  }

  private void handleKeyPress(char keyCode) {
    if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_TAB) {
      // Pressing <tab>, <enter> or <return> will surrender focus.
      urlField.cancelKey();
      urlField.setFocus(false);
    } else if (!validateKeyCode(keyCode)) {
      urlField.cancelKey();
    }
  }

  private void handleKeyUp(int keyCode) {
    if (keyCode == KeyCodes.KEY_ESCAPE) {
      // Pressing <esc> will reset the content of the editor to the previous property value as well
      // as surrender focus.
      updateValue();  // Restore previous property value.
      urlField.cancelKey();
      urlField.setFocus(false);
    }
  }

  protected boolean validateKeyCode(char keyCode) {
    return true;
  }


}
