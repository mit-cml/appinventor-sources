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
  // @UiField protected Button darkModeButton;
  @UiField protected RadioButton lightModeRadioButton;
  @UiField protected RadioButton darkModeRadioButton;
  @UiField protected RadioButton classicRadioButton;
  @UiField protected RadioButton modernRadioButton;
  Boolean userThemePreference;
  Boolean userLayoutPreference;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public UISettingsWizard() {
    bindUI();
    userThemePreference = Ode.getUserDarkThemeEnabled();
    userLayoutPreference = Ode.getUserNewLayout();
    if (userLayoutPreference){
      modernRadioButton.setValue(true);
    }else{
      classicRadioButton.setValue(true);
    }   
    if (userThemePreference){
      darkModeRadioButton.setValue(true);
    }else{
      lightModeRadioButton.setValue(true);
    }  
    show();
  }

  public void bindUI() {
    UISettingsWizardUiBinder uibinder = GWT.create(UISettingsWizardUiBinder.class);
    uibinder.createAndBindUi(this);
  }

  public void show() {
    UIDialog.center();
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
    if (lightModeRadioButton.getValue()){
      if (Ode.getUserDarkThemeEnabled()){
          Ode.setUserDarkThemeEnabled(false);
      }
    }else{
      if (!Ode.getUserDarkThemeEnabled()){
          Ode.setUserDarkThemeEnabled(true);
      }
    }
    if (classicRadioButton.getValue()){
      if (Ode.getUserNewLayout()){
          Ode.setUserNewLayout(false);
      }
    }else{
      if (!Ode.getUserNewLayout()){
          Ode.setUserNewLayout(true);
      }
    }
    Ode.saveUserDesignSettings();
    UIDialog.hide();
  }
}
