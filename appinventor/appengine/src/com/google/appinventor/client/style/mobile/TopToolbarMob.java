package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.editor.youngandroid.actions.*;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.appinventor.client.actions.*;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.MissingResourceException;
import java.util.logging.Logger;

public class TopToolbarMob extends TopToolbar {
  private static final Logger LOG = Logger.getLogger(TopToolbarMob.class.getName());
  interface TopToolbarUiBinderMob extends UiBinder<Toolbar, TopToolbarMob> {}
  private static final TopToolbarUiBinderMob uibinder =
          GWT.create(TopToolbarUiBinderMob.class);

  @UiField DropDownButton fileDropDown;
  @UiField DropDownButton connectDropDown;
  @UiField DropDownButton buildDropDown;
  @UiField DropDownButton settingsDropDown;
  @UiField DropDownButton adminDropDown;
  @UiField(provided = true) Boolean hasWriteAccess;
  @UiField Button myProjectsButton;
  @UiField Button newButton;
  @UiField Button importProjectButton;
  @UiField Button importTemplateButton;
  @UiField Button deleteButton;
  @UiField Button saveButton;
  @UiField Button saveAsButton;
  @UiField Button checkpointButton;
  @UiField Button ExportProjectButton;
  @UiField Button ExportAllProjectsButton;

  @UiField Button wirelessButton;
  @UiField Button emulatorButton;
  @UiField Button usbButton;
  @UiField Button chromebookButton;
  @UiField Button refreshCompanionButton;
  @UiField Button resetButton;
  @UiField Button hardResetButton;
  @UiField Button CompanionInformationButton;
  @UiField Button CompanionUpdateButton;

  @UiField Button buildApkButton;
  @UiField Button buildAabButton;
  @UiField Button buildApk2Button;
  @UiField Button buildAab2Button;
  @UiField Button generateYailButton;

  @UiField Button uiSettingsButton;
  @UiField Button autoloadLastProjectButton;
  @UiField Button dyslexicFontButton;
  @UiField Button uploadKeyStoreButton;
  @UiField Button downloadKeyStoreButton;
  @UiField Button deleteKeyStoreButton;

  @UiField Button downloadUserSourceButton;
  @UiField Button userAdminButton;

    @UiField Button aboutButton;
    @UiField Button guideButton;
    @UiField Button feedbackButton;
    @UiField Button libraryButton;
    @UiField Button getStartedButton;
    @UiField Button extensionsButton;
    @UiField Button tutorialsButton;
    @UiField Button troubleshootingButton;
    @UiField Button forumsButton;
    @UiField Button showSplashButton;
    @UiField Button showShortcutsButton;

    @UiField Button signOutButton;
    @UiField Button deleteAccountItem;

    @UiField Button languageButton;


  @UiField  VerticalPanel menuContent;

  private PopupPanel menuPopup;

  interface  Translations extends ClientBundle {
    Translations INSTANCE = GWT.create(Translations.class);

    @Source("languages.json")
    TextResource languages();
  }

  private static final Dictionary LANGUAGES = Dictionary.getDictionary("LANGUAGES");


  @Override
  public void bindUI() {
    readOnly = Ode.getInstance().isReadOnly();
    hasWriteAccess = !readOnly;

    initWidget(uibinder.createAndBindUi(this));
    super.fileDropDown = fileDropDown;
    super.connectDropDown = connectDropDown;
    super.buildDropDown = buildDropDown;
    super.settingsDropDown = settingsDropDown;
    super.adminDropDown = adminDropDown;

    menuContent.setStyleName("mobile-MenuContainer");
    menuPopup = new PopupPanel(true);
//    menuPopup.setStyleName("mobile-MenuPopup");

    languageButton.setText("Language: " + getDisplayName(LocaleInfo.getCurrentLocale().getLocaleName()));


    settingsDropDown.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        LOG.warning("Hamburger menu clicked");
        menuPopup.setWidget(menuContent);
        menuPopup.center();
        menuContent.setVisible(true);
      }
    });

    languageButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent clickEvent) {
        showLanguagePopup();
        menuPopup.hide();
      }
    });

    // Add click handlers for buttons
    myProjectsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new SwitchToProjectAction().execute();
        menuPopup.hide();
      }
    });

    newButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new NewProjectAction().execute();
        menuPopup.hide();
      }
    });

    importProjectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ImportProjectAction().execute();
        menuPopup.hide();
      }
    });

    importTemplateButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ImportTemplateAction().execute();
        menuPopup.hide();
      }
    });

    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new DeleteAction().execute();
        menuPopup.hide();
      }
    });

    saveButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new SaveAction().execute();
        menuPopup.hide();
      }
    });

    saveAsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new SaveAsAction().execute();
        menuPopup.hide();
      }
    });

    checkpointButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new CheckpointAction().execute();
        menuPopup.hide();
      }
    });

    ExportProjectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ExportProjectAction().execute();
        menuPopup.hide();
      }
    });

    ExportAllProjectsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ExportAllProjectsAction().execute();
        menuPopup.hide();
      }
    });

    wirelessButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new WirelessAction().execute();
        menuPopup.hide();
      }
    });

    emulatorButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new EmulatorAction().execute();
        menuPopup.hide();
      }
    });

    usbButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new UsbAction().execute();
        menuPopup.hide();
      }
    });

    chromebookButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ChromebookAction().execute();
        menuPopup.hide();
      }
    });

    refreshCompanionButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new RefreshCompanionAction().execute();
        menuPopup.hide();
      }
    });

    resetButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ResetAction().execute();
        menuPopup.hide();
      }
    });

    hardResetButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new HardResetAction().execute();
        menuPopup.hide();
      }
    });

    CompanionInformationButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new AboutCompanionAction().execute();
        menuPopup.hide();
      }
    });

    CompanionUpdateButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new CompanionUpdateAction().execute();
        menuPopup.hide();
      }
    });

    buildApkButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new BarcodeAction( false, false).execute();
        menuPopup.hide();
      }
    });

    buildAabButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new BarcodeAction( false, true).execute();
        menuPopup.hide();
      }
    });

    buildApk2Button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new BarcodeAction( true, false).execute();
        menuPopup.hide();
      }
    });

    buildAab2Button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new BarcodeAction( true, true).execute();
        menuPopup.hide();
      }
    });

    generateYailButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new GenerateYailAction().execute();
        menuPopup.hide();
      }
    });

    uiSettingsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new UISettingsAction().execute();
        menuPopup.hide();
      }
    });

    autoloadLastProjectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new DisableAutoloadAction().execute();
        menuPopup.hide();
      }
    });

    dyslexicFontButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new SetFontRegularAction().execute();
        menuPopup.hide();
      }
    });

    uploadKeyStoreButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new UploadKeystoreAction().execute();
        menuPopup.hide();
      }
    });

    downloadKeyStoreButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new DownloadKeystoreAction().execute();
        menuPopup.hide();
      }
    });

    deleteKeyStoreButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new DeleteKeystoreAction().execute();
        menuPopup.hide();
      }
    });

    downloadUserSourceButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new DownloadUserSourceAction().execute();
        menuPopup.hide();
      }
    });

    userAdminButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new SwitchToUserAdminAction().execute();
        menuPopup.hide();
      }
    });

    aboutButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new AboutAction().execute();
        menuPopup.hide();
      }
    });

    guideButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenGuideAction().execute();
        menuPopup.hide();
      }
    });

    feedbackButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenFeedbackAction().execute();
        menuPopup.hide();
      }
    });

    libraryButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenLinkAction().execute();
        menuPopup.hide();
      }
    });

    getStartedButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenLinkAction().execute();
        menuPopup.hide();
      }
    });

    extensionsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenLinkAction().execute();
        menuPopup.hide();
      }
    });

    tutorialsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenLinkAction().execute();
        menuPopup.hide();
      }
    });

    troubleshootingButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenLinkAction().execute();
        menuPopup.hide();
      }
    });

    forumsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new OpenLinkAction().execute();
        menuPopup.hide();
      }
    });

    showSplashButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ShowSplashAction().execute();
        menuPopup.hide();
      }
    });

    showShortcutsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ShowShortcutsAction().execute();
        menuPopup.hide();
      }
    });

    signOutButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new SignOutAction().execute();
        menuPopup.hide();
      }
    });

    deleteAccountItem.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new DeleteAccountAction().execute();
        menuPopup.hide();
      }
    });
  }

  private void showLanguagePopup() {
    PopupPanel languagePopup = new PopupPanel(true);
    VerticalPanel panel = new VerticalPanel();
    panel.setSpacing(5);

    for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
      if (!localeName.equals("default")) {
        String displayName = getDisplayName(localeName);
        Button langButton = new Button(displayName);
        langButton.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            selectLanguage(localeName);
            languagePopup.hide();
          }
        });
        panel.add(langButton);
      }
    }

    languagePopup.setWidget(panel);
    languagePopup.center();
  }

  private void selectLanguage(String localeName) {
    languageButton.setText("Language: " + getDisplayName(localeName));
    String url = Window.Location.createUrlBuilder()
            .setParameter("locale", localeName)
            .buildString();
    Window.Location.assign(url);
  }

  private String getDisplayName(String localeName) {
    String nativeName = LocaleInfo.getLocaleNativeDisplayName(localeName);
    try {
      return LANGUAGES.get(localeName);
    } catch (MissingResourceException e) {
      return nativeName;
    }
  }

}