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
//  @UiField protected Button darkModeButton;
//  @UiField protected RadioButton lightModeRadioButton;
//  @UiField protected RadioButton darkModeRadioButton;
  @UiField protected Button topInvisible;
  @UiField protected Button bottomInvisible;
  @UiField protected InputElement classicRadioButton;
  @UiField protected InputElement modernRadioButton;
  Boolean userThemePreference;
  Boolean userLayoutPreference;
  Boolean firstUIChoice = false;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public UISettingsWizard() {
    bindUI();
    userThemePreference = Ode.getUserDarkThemeEnabled();
    userLayoutPreference = Ode.getUserNewLayout();
    if (userLayoutPreference) {
      modernRadioButton.setChecked(true);
    } else {
      classicRadioButton.setChecked(true);
    }
//    if (userThemePreference){
//      darkModeRadioButton.setValue(true);
//    }else{
//      lightModeRadioButton.setValue(true);
//    }
  }

  public UISettingsWizard(boolean intro) {
    this();
    introText.setVisible(intro);
    cancelButton.setVisible(!intro);
    firstUIChoice = intro;
  }

  public void bindUI() {
    UISettingsWizardUiBinder uibinder = GWT.create(UISettingsWizardUiBinder.class);
    uibinder.createAndBindUi(this);
  }

  public void show() {
    UIDialog.center();
    classicRadioButton.focus();
  }

  // @UiHandler("darkModeButton")
  // protected void switchTheme(ClickEvent e) {
  //   // Boolean userThemePreference;
  //   if (Ode.getUserDarkThemeEnabled()) {
  //       darkModeButton.setTitle("light");
  //       Ode.setUserDarkThemeEnabled(false);
  //     } else {
  //       darkModeButton.setTitle("dark");
  //       Ode.setUserDarkThemeEnabled(true);
  //     }
  //   // Ode.setUserDarkThemeEnabled(userThemePreference);
  //   // UIDialog.hide();
  // }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    Ode.setUserNewLayout(userLayoutPreference);
    Ode.setUserDarkThemeEnabled(userThemePreference);
    UIDialog.hide();
  }

  @UiHandler("applyButton")
  protected void applySettings(ClickEvent e) {
    // Boolean userThemePreference;
    // if (Ode.getUserDarkThemeEnabled()) {
    //     userThemePreference = false;
    //   } else {
    //     userThemePreference = true;
    //   }
    // Ode.setUserDarkThemeEnabled(userThemePreference);
//    if (lightModeRadioButton.getValue()){
//      if (Ode.getUserDarkThemeEnabled()){
//          Ode.setUserDarkThemeEnabled(false);
//      }
//    }else{
//      if (!Ode.getUserDarkThemeEnabled()){
//          Ode.setUserDarkThemeEnabled(true);
//      }
//    }
    if (firstUIChoice) {
      Ode.setShowUIPicker(false);
    }
    Ode.setUserNewLayout(modernRadioButton.isChecked());
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
