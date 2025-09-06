package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.editor.youngandroid.actions.*;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.ToolbarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.appinventor.client.actions.*;

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

  @UiField TextButton languageButton;
  @UiField
  DisclosurePanel accountPanel;
  @UiField static Label accountButton;
  @UiField ToolbarItem deleteAccountItem;

  @UiField VerticalPanel menuContent;

  private PopupPanel menuPopup;

  private static final Dictionary LANGUAGES = Dictionary.getDictionary("LANGUAGES");

  interface Translations extends ClientBundle {
    Translations INSTANCE = GWT.create(Translations.class);

    @Source("languages.json")
    TextResource languages();
  }

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
    menuPopup.addCloseHandler(event -> menuContent.setVisible(false));

    languageButton.setText(getMessages().switchLanguageButton() + ": " + getDisplayName(LocaleInfo.getCurrentLocale().getLocaleName()));

    // Default text, will be updated with email
    accountButton.setText(getMessages().signOutLink());

    // Handle delete account visibility based on read-only mode
    if (Ode.getInstance().isReadOnly()) {
      deleteAccountItem.setVisible(false);
    }

    setupAccountPanelToggle();

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
  }

  /**
   * Updates the UI to show the user's email address in the account section.
   * This method should be called from the main application when the user info is available.
   *
   * @param email the user's email address
   */
  public static void showUserEmail(String email) {
    if (accountButton != null) {
      // Set initial state with closed arrow and email
      accountButton.setText("▶ " + email);
    }
  }

  /**
   * Sets up the disclosure panel event handlers to manage arrow state.
   */
  private void setupAccountPanelToggle() {
    accountPanel.addOpenHandler(event -> {
      String currentText = accountButton.getText();
      if (currentText.startsWith("▶ ")) {
        // Replace right arrow with down arrow when opened
        accountButton.setText("▼ " + currentText.substring(2));
      }
    });

    accountPanel.addCloseHandler(event -> {
      String currentText = accountButton.getText();
      if (currentText.startsWith("▼ ")) {
        accountButton.setText("▶ " + currentText.substring(2));
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