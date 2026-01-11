// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;

import java.util.logging.Logger;

public class UISettingsWizard {

  interface UISettingsWizardUiBinder extends UiBinder<Dialog, UISettingsWizard> {}

  private static final Logger LOG = Logger.getLogger(UISettingsWizard.class.getName());

  // UI element for project name
  @UiField protected Dialog uiDialog;
  @UiField protected Button applyButton;
  @UiField protected Button cancelButton;
  @UiField protected Label introText;
  @UiField protected Button topInvisible;
  @UiField protected Button bottomInvisible;
  @UiField protected InputElement classicRadioButton;
  @UiField protected InputElement modernRadioButton;
  @UiField protected ListBox themeSelector;
  boolean userLayoutPreference;
  boolean firstUIChoice = false;
  private HandlerRegistration resizeHandler;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public UISettingsWizard() {
    this(false);
  }

  public UISettingsWizard(boolean intro) {
    bindUI();
    firstUIChoice = intro;
    userLayoutPreference = Ode.getUserNewLayout();
    if (intro || userLayoutPreference) {
      modernRadioButton.setChecked(true);
    } else {
      classicRadioButton.setChecked(true);
    }
    themeSelector.addItem(MESSAGES.lightMode(), "light");
    themeSelector.addItem(MESSAGES.darkMode(), "dark");
    themeSelector.setSelectedIndex(Ode.getUserDarkThemeEnabled() ? 1 : 0);
    updateStyle();
    introText.setVisible(intro);
    cancelButton.setVisible(!intro);
    show();
  }

  public void bindUI() {
    UISettingsWizardUiBinder uibinder = GWT.create(UISettingsWizardUiBinder.class);
    uibinder.createAndBindUi(this);
    resizeHandler = Window.addResizeHandler(event -> {
      if (uiDialog.isShowing()) {
        uiDialog.center();
      }
    });
  }

  public void show() {
    // We add a delay here in case the DeckPanel that makes up the main App Inventor display
    // is in the middle of animating. Unfortunately, there seems to be no way to acquire its
    // animation state from GWT.
    Scheduler.get().scheduleFixedDelay(() -> {
      uiDialog.center();
      classicRadioButton.focus();
      return false;
    }, 350);
  }

  public void hide() {
    uiDialog.hide();
    resizeHandler.removeHandler();
  }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    Ode.setUserNewLayout(userLayoutPreference);
    hide();
  }

  @UiHandler("applyButton")
  protected void applySettings(ClickEvent e) {

    if (firstUIChoice) {
      Ode.setShowUIPicker(false);
    }
    Ode.setUserNewLayout(modernRadioButton.isChecked());
    Ode.setUserDarkThemeEnabled("dark".equals(themeSelector.getSelectedValue()));
    Ode.saveUserDesignSettings();
    hide();
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
     applyButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
     classicRadioButton.focus();
  }

  @UiHandler("themeSelector")
  protected void onChange(ChangeEvent event) {
    updateStyle();
  }

  private void updateStyle() {
    uiDialog.removeStyleDependentName("dark");
    uiDialog.removeStyleDependentName("light");
    uiDialog.addStyleDependentName(themeSelector.getSelectedValue());
  }
}
