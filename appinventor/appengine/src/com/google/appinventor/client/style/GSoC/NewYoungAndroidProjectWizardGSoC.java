package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;

public class NewYoungAndroidProjectWizardGSoC extends NewYoungAndroidProjectWizard {
  interface NewYoungAndroidProjectWizardUiBinderGSoC
      extends UiBinder<Dialog, NewYoungAndroidProjectWizardGSoC> {}

  @UiField protected Dialog addDialog;
  @UiField protected Button addButton;
  @UiField protected Button cancelButton;
  @UiField protected LabeledTextBox projectNameTextBox;

  @Override
  public void bindUI() {
    NewYoungAndroidProjectWizardUiBinderGSoC UI_BINDER =
        GWT.create(NewYoungAndroidProjectWizardUiBinderGSoC.class);
    UI_BINDER.createAndBindUi(this);
    super.addDialog = addDialog;
    super.addButton = addButton;
    super.cancelButton = cancelButton;
    super.projectNameTextBox = projectNameTextBox;
  }

  @UiHandler("cancelButton")
  protected void cancelAdd(ClickEvent e) {
    super.cancelAdd(e);
  }

  @UiHandler("addButton")
  protected void addProject(ClickEvent e) {
    super.addProject(e);
  }
}
