package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.wizards.Dialog;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.wizards.NewProjectWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import java.util.logging.Logger;
import java.lang.Boolean;

public class UISettingsWizard {

  interface UISettingsWizardUiBinder extends UiBinder<Dialog, UISettingsWizard> {}

  private static final Logger LOG = Logger.getLogger(UISettingsWizard.class.getName());

  // UI element for project name
  @UiField protected Dialog UIDialog;
  @UiField protected Button applyButton;
  @UiField protected Button cancelButton;
  @UiField protected Button darkModeButton;
  @UiField protected RadioButton classicRadioButton;
  @UiField protected RadioButton modernRadioButton;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public UISettingsWizard() {
    bindUI();
    classicRadioButton.setValue(true);
    show();
  }

  public void bindUI() {
    UISettingsWizardUiBinder UI_BINDER = GWT.create(UISettingsWizardUiBinder.class);
    UI_BINDER.createAndBindUi(this);
  }

  public void show() {
    UIDialog.center();
  }

  @UiHandler("darkModeButton")
  protected void switchTheme(ClickEvent e) {
    Boolean userThemePreference;
    if (Ode.getUserDarkThemeEnabled()) {
        userThemePreference = false;
      } else {
        userThemePreference = true;
      }
    Ode.setUserDarkThemeEnabled(userThemePreference);
    UIDialog.hide();
  }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    UIDialog.hide();
  }

  @UiHandler("applyButton")
  protected void applySettings(ClickEvent e) {
    Boolean userThemePreference;
    if (Ode.getUserDarkThemeEnabled()) {
        userThemePreference = false;
      } else {
        userThemePreference = true;
      }
    Ode.setUserDarkThemeEnabled(userThemePreference);
    if (classicRadioButton.getValue()){
        if (Ode.getUserNewLayout()){
            Ode.setUserNewLayout(false);
        }
    }else{
        if (!Ode.getUserNewLayout()){
            Ode.setUserNewLayout(true);
        }
    }
    UIDialog.hide();
  }

}
