// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HiddenComponentsCheckbox extends CheckBox {
  private static final Logger LOG = Logger.getLogger(HiddenComponentsCheckbox.class.getName());

  private static HiddenComponentsCheckbox INSTANCE;
  private static final Map<String, MockForm> formInstances = new HashMap<>();
  private static final Map<String, Boolean> screenCheckboxState = new HashMap<>();
  private static String currentFormTitle;

  private HiddenComponentsCheckbox() {
    addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        toggleHiddenComponents();
      }
    });
  }

  public static void show(MockForm form) {
    if (form == null) {
      LOG.severe("❌ Attempted to show HiddenComponentsCheckbox with a null form.");
      return;
    }

    checkInstance();
    String formTitle = form.getTitle();
    currentFormTitle = formTitle;

    formInstances.put(formTitle, form);
    screenCheckboxState.putIfAbsent(formTitle, Ode.getCurrentProjectEditor().getScreenCheckboxState(formTitle));

    LOG.info("📌 [show] HiddenComponentsCheckbox for screen: " + formTitle + " | State: " + screenCheckboxState.get(formTitle));

    INSTANCE.setValue(screenCheckboxState.get(formTitle));
  }

  public static boolean getCheckboxStateForScreen(String formTitle) {
    checkInstance();
    boolean state = screenCheckboxState.getOrDefault(formTitle, false);
    LOG.info("📌 [getCheckboxStateForScreen] Screen: " + formTitle + " | State: " + state);
    return state;
  }

  public static void toggleHiddenComponents() {
    checkInstance();
    LOG.info("✅ [toggleHiddenComponents] Checkbox clicked!");

    if (currentFormTitle == null) {
      LOG.warning("❌ No current form title set.");
      return;
    }

    MockForm currentForm = formInstances.get(currentFormTitle);
    if (currentForm == null) {
      LOG.warning("❌ Form not found for screen: " + currentFormTitle);
      return;
    }

    boolean newState = INSTANCE.getValue();
    LOG.info("✅ [toggleHiddenComponents] Toggling hidden components for screen: " + currentFormTitle + " to " + newState);

    screenCheckboxState.put(currentFormTitle, newState);
    Ode.getCurrentProjectEditor().setScreenCheckboxState(currentFormTitle, newState);

    LOG.info("📌 [toggleHiddenComponents] Checkbox state updated: " + currentFormTitle + " = " + newState);

    for (MockForm form : formInstances.values()) {
      LOG.info("🔄 Refreshing screen: " + form.getTitle());
      for (MockComponent child : form.getChildren()) {
        LOG.info("🔄 Updating visibility for component: " + child.getName());
        child.updateVisibility();
      }
      form.doRefresh();
    }
  }

  // ✅ Fixed: Define `setVisibility(boolean)`
  public static void setVisibility(boolean visible) {
    checkInstance();
    INSTANCE.setVisible(visible);
  }

  private static void checkInstance() {
    if (INSTANCE == null) {
      INSTANCE = new HiddenComponentsCheckbox();
    }
  }
}
