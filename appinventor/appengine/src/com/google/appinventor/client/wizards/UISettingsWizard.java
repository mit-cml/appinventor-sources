// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;

import java.util.logging.Logger;
import java.lang.Boolean;

public class UISettingsWizard {

  interface UISettingsWizardUiBinder extends UiBinder<Dialog, UISettingsWizard> {}

  private static final Logger LOG = Logger.getLogger(UISettingsWizard.class.getName());

  // UI element for project name
  @UiField protected Dialog UIDialog;
  @UiField protected Button applyButton;
  @UiField protected Button cancelButton;
  @UiField protected Label introText;
  @UiField protected Button topInvisible;
  @UiField protected Button bottomInvisible;
  @UiField protected InputElement classicRadioButton;
  @UiField protected InputElement modernRadioButton;
  @UiField protected RadioButton lightModeRadioButton;
  @UiField protected RadioButton darkModeRadioButton;
  Boolean userThemePreference;
  Boolean userLayoutPreference;
  Boolean firstUIChoice = false;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public UISettingsWizard() {
    this(false);
  }

  public UISettingsWizard(boolean intro) {
    bindUI();
    firstUIChoice = intro;
    userThemePreference = Ode.getUserDarkThemeEnabled();
    userLayoutPreference = Ode.getUserNewLayout();
    if (intro || userLayoutPreference) {
      modernRadioButton.setChecked(true);
    } else {
      classicRadioButton.setChecked(true);
    }
    if (userThemePreference) {
      darkModeRadioButton.setValue(true);
    } else {
      lightModeRadioButton.setValue(true);
    }
    introText.setVisible(intro);
    cancelButton.setVisible(!intro);
    show();
  }

  public void bindUI() {
    UISettingsWizardUiBinder uibinder = GWT.create(UISettingsWizardUiBinder.class);
    uibinder.createAndBindUi(this);
  }

  public void show() {
    UIDialog.center();
    classicRadioButton.focus();
  }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    Ode.setUserNewLayout(userLayoutPreference);
    Ode.setUserDarkThemeEnabled(userThemePreference);
    UIDialog.hide();
  }

  @UiHandler("applyButton")
  protected void applySettings(ClickEvent e) {

    if (firstUIChoice) {
      Ode.setShowUIPicker(false);
    }
    Ode.setUserNewLayout(modernRadioButton.isChecked());
    Ode.setUserDarkThemeEnabled(darkModeRadioButton.getValue());
    Ode.saveUserDesignSettings();
    UIDialog.hide();
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
     applyButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
     classicRadioButton.focus();
  }
}
