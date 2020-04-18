// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.Ode.getSystemConfig;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import java.util.List;
import java.util.MissingResourceException;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  // Strings for links and dropdown menus:

  interface TopPanelUiBinder extends UiBinder<FlowPanel, TopPanel> {}

  private static final TopPanelUiBinder UI_BINDER = GWT.create(TopPanelUiBinder.class);

  @UiField(provided = true) FlowPanel header = new FlowPanel("header");
  @UiField TopToolbar topToolbar;
  @UiField ImageElement logo;
  @UiField Label readOnly;
  @UiField FlowPanel rightPanel;
  @UiField TextButton myProjects;
  @UiField TextButton viewTrash;
  @UiField TextButton gallery;
  @UiField TextButton guideLink;
  @UiField TextButton feedbackLink;
  @UiField TextButton moderation;
  @UiField DropDownButton languageDropDown;
  @UiField DropDownButton accountButton;
  @UiField FlowPanel links;
  private final String WIDGET_NAME_SIGN_OUT = "Signout";
  private static final String WIDGET_NAME_LANGUAGE = "Language";

  private static final String SIGNOUT_URL = "/ode/_logout";

  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_ai2";

  final Ode ode = Ode.getInstance();

  interface Translations extends ClientBundle {
    Translations INSTANCE = GWT.create(Translations.class);

    @Source("languages.json")
    TextResource languages();
  }

  static {
    loadLanguages(Translations.INSTANCE.languages().getText());
    LANGUAGES = Dictionary.getDictionary("LANGUAGES");
  }

  private static native void loadLanguages(String resource)/*-{
    $wnd['LANGUAGES'] = JSON.parse(resource);
  }-*/;

  private static final Dictionary LANGUAGES;

  /**
   * Initializes and assembles all UI elements shown in the top panel.
   */
  public TopPanel() {
    /*
     * The layout of the top panel is as follows:
     *
     *  +-- topPanel ------------------------------------+
     *  |+-- logo --++-----tools-----++--links/account--+|
     *  ||          ||               ||                 ||
     *  |+----------++---------------++-----------------+|
     *  +------------------------------------------------+
     */
    Config config = getSystemConfig();
    initWidget(UI_BINDER.createAndBindUi(this));

    String logoUrl = config.getLogoUrl();
    if (!Strings.isNullOrEmpty(logoUrl)) {
      Image.wrap(logo).addClickHandler(new WindowOpenClickHandler(logoUrl));
    }

    if (!Ode.getInstance().isReadOnly()) {
      readOnly.removeFromParent();
    }

    String guideUrl = config.getGuideUrl();
    if (!Strings.isNullOrEmpty(guideUrl)) {
      guideLink.addClickHandler(new WindowOpenClickHandler(guideUrl));
    } else {
      guideLink.removeFromParent();
    }

    // Feedback Link
    String feedbackUrl = config.getFeedbackUrl();
    if (!Strings.isNullOrEmpty(feedbackUrl)) {
      feedbackLink.addClickHandler(new WindowOpenClickHandler(feedbackUrl));
    } else {
      feedbackLink.removeFromParent();
    }

    accountButton.addItem(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(), new SignOutAction()));

    // Language
    List<DropDownItem> languageItems = Lists.newArrayList();
    for (String localeName : LocaleInfo.getAvailableLocaleNames()) {
      if (!localeName.equals("default")) {
        languageItems.add(new DropDownItem(WIDGET_NAME_LANGUAGE, getDisplayName(localeName),
            new SelectLanguage(localeName)));
      }
    }
    languageDropDown.setItems(languageItems);
    languageDropDown.setCaption(getDisplayName(LocaleInfo.getCurrentLocale().getLocaleName()));
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  public TopToolbar getTopToolbar() {
    return topToolbar;
  }

  @SuppressWarnings("unused")
  @UiHandler("myProjects")
  public void switchToMyProjects(ClickEvent e) {
    ode.switchToProjectsView();
  }

  @SuppressWarnings("unused")
  @UiHandler("viewTrash")
  public void switchToTrash(ClickEvent e) {
    ode.switchToTrash();
  }

  @SuppressWarnings("unused")
  @UiHandler("gallery")
  public void switchToGallery(ClickEvent e) {
    ode.switchToGalleryView();
  }

  @SuppressWarnings("unused")
  @UiHandler("moderation")
  public void switchToModeration(ClickEvent e) {
    ode.switchToModerationPageView();
  }

  private String getDisplayName(String localeName){
    String nativeName=LocaleInfo.getLocaleNativeDisplayName(localeName);
    try {
      return LANGUAGES.get(localeName);
    } catch (MissingResourceException e) {
      return nativeName;
    }
  }

  public void updateAccountMessageButton(){
    // Since we want to insert "Messages" before "Sign Out", we need to clear first.
    accountButton.clearAllItems();

    // Gallery Items
    // (1)Private User Profile
    accountButton.addItem(new DropDownItem("Profile",
        MESSAGES.privateProfileLink(), new PrivateProfileAction()));
    // (2)Sign Out
    accountButton.addItem(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(),
        new SignOutAction()));
  }

  /**
   * Updates the UI to show the user's email address.
   *
   * @param email the email address
   */
  public void showUserEmail(String email) {
    accountButton.setCaption(email);
  }

  /**
   * Updates the UI to show the moderation's link.
   */
  public void showModerationLink(boolean b) {
    moderation.setVisible(b);
  }

  /**
   * Updates the UI to show the moderation's link.
   */
  public void showGalleryLink(boolean b) {
    gallery.setVisible(b);
  }

  /**
   * Adds the MOTD box to the right panel. This should only be called once.
   */
  public void showMotd() {
    rightPanel.add(MotdBox.getMotdBox());
  }

  private static class WindowOpenClickHandler implements ClickHandler {
    private final String url;

    WindowOpenClickHandler(String url) {
      this.url = url;
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
      Window.open(url, WINDOW_OPEN_LOCATION, WINDOW_OPEN_FEATURES);
    }
  }

  private static class SignOutAction implements Command {
    @Override
    public void execute() {
      // Maybe take a screenshot
      Ode.getInstance().screenShotMaybe(new Runnable() {
          @Override
          public void run() {
            Window.Location.replace(SIGNOUT_URL);
          }
        }, true);               // Wait for i/o
    }
  }

  private static class SelectLanguage implements Command {

    private String localeName;

    public SelectLanguage(String localeName) {
      this.localeName = localeName;
    }

    @Override
    public void execute() {
      final String queryParam = LocaleInfo.getLocaleQueryParam();
      Command savecmd = new SaveAction();
      savecmd.execute();
      if (queryParam != null) {
        UrlBuilder builder = Window.Location.createUrlBuilder().setParameter(
            queryParam, localeName);
        Window.Location.replace(builder.buildString());
      } else {
        // If we are using only cookies, just reload
        Window.Location.reload();
      }
    }

    public void setLocale(String nativeName) {
      localeName = nativeName;
    }

  }

  private static class SaveAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(null);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
      }
    }
  }

  private static class PrivateProfileAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToPrivateUserProfileView();
    }
  }
}

