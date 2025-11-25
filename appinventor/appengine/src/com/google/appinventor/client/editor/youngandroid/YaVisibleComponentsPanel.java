// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.SimpleVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.logging.Logger;

/**
 * An implementation of SimpleVisibleComponentsPanel for the MockForm designer.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class YaVisibleComponentsPanel extends SimpleVisibleComponentsPanel<MockForm> {
  interface YaVisibleComponentsPanelUiBinder extends UiBinder<VerticalPanel,
       YaVisibleComponentsPanel> {}
  private static final Logger LOG = Logger.getLogger(YaVisibleComponentsPanel.class.getName());
  // UI elements
  @UiField protected VerticalPanel phoneScreen;
  @UiField(provided = true) protected ListBox listboxPhoneTablet; // A ListBox for Phone/Tablet/Monitor preview sizes
  @UiField(provided = true) protected ListBox listboxPhonePreview; // A ListBox for Holo/Material/iOS preview styles
  protected final String[] drop_lst_phone_preview = { "Android Material", "Android Holo", "iOS" };
  protected final ProjectEditor projectEditor;
  @UiField protected CheckBox HiddenComponentsCheckbox;

  /**
   * Creates new component design panel for visible components.
   *
   * @param projectEditor
   * @param nonVisibleComponentsPanel corresponding panel for non-visible
   */
  public YaVisibleComponentsPanel(final ProjectEditor projectEditor,
      SimpleNonVisibleComponentsPanel<MockForm> nonVisibleComponentsPanel) {
    super(nonVisibleComponentsPanel);

    this.projectEditor = projectEditor;
    initializeListboxes();

    bindUI();

    listboxPhoneTablet.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String[] selectedValue = listboxPhoneTablet.getSelectedValue().split(",");
        int idx = listboxPhoneTablet.getSelectedIndex();

        int width = Integer.parseInt(selectedValue[0].trim());
        int height = Integer.parseInt(selectedValue[1].trim());
        String val = Integer.toString(idx) + "," + Integer.toString(width) + "," + Integer.toString(height);
        // here, we can change settings by putting val into it
        projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET, val);
        changeFormPreviewSize(idx, width, height);
      }
    });
    listboxPhonePreview.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        int idx = Integer.parseInt(listboxPhonePreview.getSelectedValue());
        String val = drop_lst_phone_preview[idx];
        // here, we can change settings by putting chosenStyle value into it
        projectEditor.changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW, val);
        changeFormPhonePreview(idx, val);
      }
    });
    HiddenComponentsCheckbox.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        HiddenComponentsManager.getInstance().toggle();
      }
    });
    initWidget(phoneScreen);
  }

  protected void initializeListboxes() {
    // Initialize UI
    listboxPhoneTablet = new ListBox() {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        String sizing = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING);
        boolean fixed = (sizing.equals("Fixed"));
        listboxPhoneTablet.setVisible(!fixed);
        if (fixed) {
          changeFormPreviewSize(0, 320, 505);
        } else {
          getUserSettingChangeSize();
        }
      }
    };

    listboxPhonePreview = new ListBox() {
      @Override
      protected void onLoad() {
        // onLoad is called immediately after a widget becomes attached to the browser's document.
        String previewStyle = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME);
        boolean classic = (previewStyle.equals("Classic"));
        listboxPhonePreview.setVisible(!classic);
        if (classic) {
          changeFormPhonePreview(-1, "Classic");
        } else {
          getUserSettingChangePreview();
        }
      }
    };
  }

  /**
   * Associates a Simple root component with this panel.
   *
   * @param form  backing mocked root component
   */
  @Override
  public void setRoot(MockForm form) {
    this.root = form;
    phoneScreen.add(form);
  }

  // get width and height stored in user settings, and change the preview size.
  protected void getUserSettingChangeSize() {
    String val = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_TABLET);
    int idx = 0;
    int width = 320;
    int height = 505;

    // Note: Initialization values above should not be changed without good reason. The settings property PHONE_TABLET
    // is a legacy setting indicating tablet (true) or phone (false).
    if (val.equals("True")) {
      idx = 1;
    } else {
      String[] parts = val.split(",");
      if (parts.length == 3) {
        idx = Integer.parseInt(parts[0]);
      }
    }

    if (listboxPhoneTablet.getItemCount() >= idx) {
      String[] selectedValue = listboxPhoneTablet.getValue(idx).split(",");
      width = Integer.parseInt(selectedValue[0].trim());
      height = Integer.parseInt(selectedValue[1].trim());
    }

    listboxPhoneTablet.setItemSelected(idx, true);
    changeFormPreviewSize(idx, width, height);
  }

  // get Phone Preview stored in user settings, and change the preview style.
  protected void getUserSettingChangePreview() {
    String val = projectEditor.getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW);
    int idx = 0;

    if (val.equals("Classic")) {
      val = "Android Material";
    }

    if (val.equals("Android Holo")) {
      idx = 1;
    } else if (val.equals("iOS")) {
      idx = 2;
    }
    listboxPhonePreview.setItemSelected(idx, true);
    changeFormPhonePreview(idx, val);
  }

  protected void changeFormPreviewSize(int idx, int width, int height) {
    if (root == null)
      return;

    root.changePreviewSize(width, height, idx);
  }

  protected void changeFormPhonePreview(int idx, String chosenVal) {

    if (root == null)
      return;

    root.changePhonePreview(idx, chosenVal);
  }

  public void enableTabletPreviewCheckBox(boolean enable){
    if (root != null){
      if (!enable){
        changeFormPreviewSize(0, 320, 505);
      } else {
        getUserSettingChangeSize();
      }
      listboxPhoneTablet.setVisible(enable);
    }
    listboxPhoneTablet.setEnabled(enable);
  }

  public void enablePhonePreviewCheckBox(boolean enable){
    if (root != null) {
      if (!enable) {
        changeFormPhonePreview(-1, "Classic");
      } else {
        getUserSettingChangePreview();
      }
      listboxPhonePreview.setVisible(enable);
    }
    listboxPhonePreview.setEnabled(enable);
  }

  public void focusCheckbox() {
    HiddenComponentsCheckbox.setFocus(true);
  }

  public void show(MockForm form) {
    this.root = form;
    HiddenComponentsManager manager = HiddenComponentsManager.getInstance();
    manager.setCurrentForm(form);
    Boolean state = Ode.getCurrentProjectEditor().getScreenCheckboxState(form.getTitle());
    boolean effectiveState = (state != null) ? state : false;
    LOG.info("Setting checkbox state for " + form.getTitle() + " to " + effectiveState);
    HiddenComponentsCheckbox.setValue(effectiveState);
  }

  public void showHiddenComponentsCheckbox() {
    if (HiddenComponentsCheckbox != null) {
      HiddenComponentsCheckbox.setVisible(true);
    } else {
      LOG.severe("HiddenComponentsCheckbox is null in showHiddenComponentsCheckbox");
    }
  }

  public void hideHiddenComponentsCheckbox() {
    if (HiddenComponentsCheckbox != null) {
      HiddenComponentsCheckbox.setVisible(false);
    } else {
      LOG.severe("HiddenComponentsCheckbox is null in hideHiddenComponentsCheckbox");
    }
  }

  protected void bindUI() {
    YaVisibleComponentsPanel.YaVisibleComponentsPanelUiBinder uibinder =
        GWT.create(YaVisibleComponentsPanel.YaVisibleComponentsPanelUiBinder.class);
    uibinder.createAndBindUi(this);
  }
}
