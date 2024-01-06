package com.google.appinventor.client.style.GSoC;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.simple.SimpleVisibleComponentsPanel;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

public class SimpleVisibleComponentsPanelGSoC extends SimpleVisibleComponentsPanel {
  interface SimpleVisibleComponentsPanelUiBinderGSoC extends UiBinder<VerticalPanel,
       SimpleVisibleComponentsPanelGSoC> {}
  @UiField protected VerticalPanel phoneScreen;
  @UiField(provided = true) protected ListBox listboxPhoneTablet; // A ListBox for Phone/Tablet/Monitor preview sizes
  @UiField(provided = true) protected ListBox listboxPhonePreview;

  public SimpleVisibleComponentsPanelGSoC(final SimpleEditor editor,
      SimpleNonVisibleComponentsPanel nonVisibleComponentsPanel) {
    super(editor, nonVisibleComponentsPanel);
  }

  @Override
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

  @Override
  protected void bindUI() {
    SimpleVisibleComponentsPanelUiBinderGSoC UI_BINDER =
        GWT.create(SimpleVisibleComponentsPanelUiBinderGSoC.class);
    UI_BINDER.createAndBindUi(this);
    super.listboxPhonePreview = listboxPhonePreview;
    super.listboxPhoneTablet = listboxPhoneTablet;
    super.phoneScreen = phoneScreen;
  }
}
