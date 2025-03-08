// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;

import java.util.logging.Logger;

public class HiddenComponentsCheckbox extends CheckBox {
  private static final Logger LOG = Logger.getLogger(HiddenComponentsCheckbox.class.getName());

  private static HiddenComponentsCheckbox INSTANCE;
  private MockForm form;

  // This is an attempt to create a Singleton class that does not have to be referenced
  // by Ode and thus can be placed anywhere in a UIBinder layout.
  private HiddenComponentsCheckbox() {
    if (INSTANCE != null) {
      LOG.warning("Attempted to create second instance of HiddenComponentsCheckbox singleton");
      ErrorReporter.reportError("Attempted to create second instance of HiddenComponentsCheckbox singleton");
    } else {
      addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          toggleHiddenComponents(event);
        }
      });
      INSTANCE = this;
    }
  }

  public static void checkInstance() {
    if (INSTANCE == null) {
      INSTANCE = new HiddenComponentsCheckbox();
    }
  }

  public static void show(MockForm form) {
    checkInstance();
    INSTANCE.form = form;
    INSTANCE.setValue(Ode.getCurrentProjectEditor().getScreenCheckboxState(form.getTitle()));
  }

  public static void setVisibility(boolean visible) {
    checkInstance();
    INSTANCE.setVisible(visible);
  }

  // TODO: This should not require navigating through Ode
  public static void toggleHiddenComponents(ClickEvent e) {
    LOG.info("Received checkbox click");
    if (INSTANCE.form != null) {
      LOG.info("Updating hidden components");
      Ode.getCurrentProjectEditor().setScreenCheckboxState(INSTANCE.form.getTitle(), INSTANCE.getValue());
      INSTANCE.form.doRefresh();
    }
  }
}
