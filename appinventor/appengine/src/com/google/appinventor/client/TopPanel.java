// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.DropDownButton.DropDownItem;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  // Strings for links and dropdown menus:
  private final DropDownButton accountButton;
  public DropDownButton languageDropDown;

  private final String WIDGET_NAME_MESSAGES = "Messages";
  private final String WIDGET_NAME_PRIVATE_USER_PROFILE = "Profile";
  private final String WIDGET_NAME_SIGN_OUT = "Signout";
  private final String WIDGET_NAME_USER = "User";
  private static final String WIDGET_NAME_LANGUAGE = "Language";

  private final String WIDGET_NAME_ABOUT = "About";
  private final String WIDGET_NAME_AIVERSION = "App Inventor Version";
  private static final String WIDGET_NAME_COMPANIONINFO = "CompanionInformation";
  private static final String WIDGET_NAME_COMPANIONUPDATE = "CompanionUpdate";
  private static final String WIDGET_NAME_HARDRESET_BUTTON = "EmulatorCompanionResetUpdate";
  private static final String WIDGET_NAME_SHOWSPLASH = "ShowSplash";

  private static final String SIGNOUT_URL = "/ode/_logout";
  private static final String LOGO_IMAGE_URL = "/images/logo.png";

  private static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  private static final String WINDOW_OPEN_LOCATION = "_ai2";

  private final VerticalPanel rightPanel;  // remember this so we can add MOTD later if needed

  final Ode ode = Ode.getInstance();

  private final TextButton gallery;
  private final TextButton moderation;
  private TextButton myProjects;

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
    HorizontalPanel topPanel = new HorizontalPanel();
    topPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    // Create the Tools
    TopToolbar tools = new TopToolbar();
    ode.setTopToolbar(tools);

    // Create the Links
    HorizontalPanel links = new HorizontalPanel();
    links.setStyleName("ode-TopPanelLinks");
    links.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    if (Ode.getInstance().isReadOnly()) {
      Label readOnly = new Label(MESSAGES.readOnlyMode());
      readOnly.setStyleName("ode-TopPanelWarningLabel");
      links.add(readOnly);
    }

    // My Projects Link
    myProjects = new TextButton(MESSAGES.myProjectsTabName());
    myProjects.setStyleName("ode-TopPanelButton");
    myProjects.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ode.switchToProjectsView();
      }
    });
    links.add(myProjects);

    // Gallery Link
    gallery = new TextButton(MESSAGES.galleryTabName());
    gallery.setStyleName("ode-TopPanelButton");
    gallery.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToGalleryView();
      }
    });
    links.add(gallery);

    // About -> {AI Version; Current Companion Version; Update the Companion; Update Companion on Emulator; Show Welcome Splash Screen;}
    List<DropDownItem> aboutItems = Lists.newArrayList();
    aboutItems.add(new DropDownItem(WIDGET_NAME_AIVERSION, MESSAGES.appInventorVersionMenuItem(),
        new AIVersionAction()));
    aboutItems.add(new DropDownItem(WIDGET_NAME_COMPANIONINFO, MESSAGES.companionInformation(),
        new AboutCompanionAction()));
    aboutItems.add(null);
    aboutItems.add(new DropDownItem(WIDGET_NAME_COMPANIONUPDATE, MESSAGES.companionUpdate(),
        new CompanionUpdateAction()));
    aboutItems.add(new DropDownItem(WIDGET_NAME_HARDRESET_BUTTON, MESSAGES.hardResetConnectionsMenuItem(),
        new HardResetAction()));
    aboutItems.add(null);
    aboutItems.add(new DropDownItem(WIDGET_NAME_SHOWSPLASH, MESSAGES.showSplashMenuItem(),
        new ShowSplashAction()));
    DropDownButton about = new DropDownButton(WIDGET_NAME_ABOUT, MESSAGES.aboutTabName(), aboutItems, true);
    about.setStyleName("ode-TopPanelButton");
    links.add(about);

    // Teach Link
    Config config = ode.getSystemConfig();
    //TODO Change this to the Educator site URL
    String teachUrl = config.getGuideUrl();
    if (!Strings.isNullOrEmpty(teachUrl)) {
      TextButton teachLink = new TextButton(MESSAGES.teachTabName());
      teachLink.addClickHandler(new WindowOpenClickHandler(teachUrl));
      teachLink.setStyleName("ode-TopPanelButton");
      links.add(teachLink);
      //teachLink.setVisible(false); //currently doesn't exist, so hide
    }


    // Moderation Link
    moderation = new TextButton(MESSAGES.moderationTabName());
    moderation.setStyleName("ode-TopPanelButton");
    moderation.addClickHandler(new ClickHandler() {
    @Override
      public void onClick(ClickEvent clickEvent) {
        ode.switchToModerationPageView();
      }
    });
    moderation.setVisible(false);
    links.add(moderation);

    // Create the Account Information
    rightPanel = new VerticalPanel();
    rightPanel.setHeight("100%");
    rightPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

    HorizontalPanel account = new HorizontalPanel();
    account.setStyleName("ode-TopPanelAccount");

    // Account Drop Down Button
    List<DropDownItem> userItems = Lists.newArrayList();

    // Sign Out
    userItems.add(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(), new SignOutAction()));

    accountButton = new DropDownButton(WIDGET_NAME_USER, " " , userItems, true);
    accountButton.setItemEnabled(WIDGET_NAME_MESSAGES, false);
    accountButton.setStyleName("ode-TopPanelButton");

    // Language
    List<DropDownItem> languageItems = Lists.newArrayList();
    String[] localeNames = LocaleInfo.getAvailableLocaleNames();
    String nativeName;
    for (String localeName : localeNames) {
      if (!localeName.equals("default")) {
        SelectLanguage lang = new SelectLanguage();
        lang.setLocale(localeName);
        nativeName = getDisplayName(localeName);
        languageItems.add(new DropDownItem(WIDGET_NAME_LANGUAGE, nativeName, lang));
      }
    }
    String currentLang = LocaleInfo.getCurrentLocale().getLocaleName();
    String nativeDisplayName = getDisplayName(currentLang);
    languageDropDown = new DropDownButton(WIDGET_NAME_LANGUAGE, nativeDisplayName, languageItems, true);
    languageDropDown.setStyleName("ode-TopPanelButton");

    account.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    account.add(links);
    account.add(languageDropDown);
    account.add(accountButton);

    rightPanel.add(account);

    // Add the Logo, Tools, Links to the TopPanel
    addLogo(topPanel);
    topPanel.add(tools);
    topPanel.add(rightPanel);
    topPanel.setCellVerticalAlignment(rightPanel, HorizontalPanel.ALIGN_MIDDLE);
    rightPanel.setCellHorizontalAlignment(account, HorizontalPanel.ALIGN_RIGHT);
    topPanel.setCellHorizontalAlignment(rightPanel, HorizontalPanel.ALIGN_RIGHT);

    initWidget(topPanel);

    setStyleName("ode-TopPanel");
    setWidth("100%");
  }

  private String getDisplayName(String localeName){
    String nativeName=LocaleInfo.getLocaleNativeDisplayName(localeName);
    if (localeName == "zh_CN") {
      nativeName = MESSAGES.SwitchToSimplifiedChinese();
    } else if (localeName == "zh_TW") {
      nativeName = MESSAGES.SwitchToTraditionalChinese();
    } else if (localeName == "es_ES") {
      nativeName = MESSAGES.SwitchToSpanish();
    } else if (localeName == "fr_FR") {
      nativeName = MESSAGES.SwitchToFrench();
    } else if (localeName == "it_IT") {
      nativeName = MESSAGES.SwitchToItalian();
    } else if (localeName == "ru") {
      nativeName = MESSAGES.SwitchToRussian();
    } else if (localeName == "ko_KR") {
      nativeName = MESSAGES.SwitchToKorean();
    } else if (localeName == "sv") {
      nativeName = MESSAGES.SwitchToSwedish();
    } else if (localeName == "pt_BR") {
      nativeName = MESSAGES.switchToPortugueseBR();
    }
    return nativeName;
  }

  public void updateAccountMessageButton(){
    // Since we want to insert "Messages" before "Sign Out", we need to clear first.
    accountButton.clearAllItems();

    // Gallery Items
    // (1)Private User Profile
    accountButton.addItem(new DropDownItem(WIDGET_NAME_PRIVATE_USER_PROFILE, MESSAGES.privateProfileLink(), new PrivateProfileAction()));
    // (2)Sign Out
    accountButton.addItem(new DropDownItem(WIDGET_NAME_SIGN_OUT, MESSAGES.signOutLink(), new SignOutAction()));
  }

  private void addLogo(HorizontalPanel panel) {
    // Logo should be a link to App Inv homepage. Currently, after the user
    // has logged in, the top level *is* ODE; so for now don't make it a link.
    // Add timestamp to logo url to get around browsers that agressively cache
    // the image! This same trick is used in StorageUtil.getFilePath().
    Image logo = new Image(LOGO_IMAGE_URL + "?t=" + System.currentTimeMillis());
    logo.setSize("40px", "40px");
    logo.setStyleName("ode-Logo");
    String logoUrl = ode.getSystemConfig().getLogoUrl();
    if (!Strings.isNullOrEmpty(logoUrl)) {
      logo.addClickHandler(new WindowOpenClickHandler(logoUrl));
    }
    panel.add(logo);
    panel.setCellWidth(logo, "50px");
    Label title = new Label("MIT App Inventor");
    title.setStyleName("ode-LogoText");
    VerticalPanel titleContainer = new VerticalPanel();
    titleContainer.add(title);
    panel.add(titleContainer);
    panel.setCellWidth(titleContainer, "180px");
    panel.setCellHorizontalAlignment(logo, HorizontalPanel.ALIGN_LEFT);
    panel.setCellVerticalAlignment(logo, HorizontalPanel.ALIGN_MIDDLE);
  }

  private void addMotd(VerticalPanel panel) {
    MotdBox motdBox = MotdBox.getMotdBox();
    panel.add(motdBox);
    panel.setCellHorizontalAlignment(motdBox, HorizontalPanel.ALIGN_RIGHT);
    panel.setCellVerticalAlignment(motdBox, HorizontalPanel.ALIGN_BOTTOM);
  }

  /**
   * Enables and/or disables buttons based on how many projects exist
   * (in the case of "Download All Projects") or are selected (in the case
   * of "Delete" and "Download Source").
   */
  public void updateTopMenuButtons(int view) {
    if (view == 0) {  // We are in the Projects view
      myProjects.setEnabled(false);
      myProjects.addStyleDependentName("disabled");
      gallery.setEnabled(true);
      gallery.removeStyleDependentName("disabled");
    } else if (view == 2) { // We are in the Gallery view
      myProjects.setEnabled(true);
      myProjects.removeStyleDependentName("disabled");
      gallery.setEnabled(false);
      gallery.addStyleDependentName("disabled");
    } else {
      myProjects.setEnabled(true);
      gallery.setEnabled(true);
      myProjects.removeStyleDependentName("disabled");
      gallery.removeStyleDependentName("disabled");
    }
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
    addMotd(rightPanel);
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
      Window.Location.replace(SIGNOUT_URL);
    }
  }

  private class SelectLanguage implements Command {

    private String localeName;

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

  private class SaveAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(null);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
      }
    }
  }

  private static class AIVersionAction implements Command {
    @Override
    public void execute() {
      final DialogBox db = new DialogBox(false, true);
      db.setText("About MIT App Inventor");
      db.setStyleName("ode-DialogBox");
      db.setHeight("200px");
      db.setWidth("400px");
      db.setGlassEnabled(true);
      db.setAnimationEnabled(true);
      db.center();

      VerticalPanel DialogBoxContents = new VerticalPanel();
      String html = MESSAGES.gitBuildId(GitBuildId.getDate(), GitBuildId.getVersion()) +
          "<BR/>Use Companion: " + BlocklyPanel.getCompVersion();
      Config config = Ode.getInstance().getSystemConfig();
      String releaseNotesUrl = config.getReleaseNotesUrl();
      if (!Strings.isNullOrEmpty(releaseNotesUrl)) {
        html += "<BR/><BR/>Please see <a href=\"" + releaseNotesUrl +
            "\" target=\"_blank\">release notes</a>";
      }
      String tosUrl = config.getTosUrl();
      if (!Strings.isNullOrEmpty(tosUrl)) {
        html += "<BR/><BR/><a href=\"" + tosUrl +
            "\" target=\"_blank\">" + MESSAGES.privacyTermsLink() + "</a>";
      }
      HTML message = new HTML(html);

      SimplePanel holder = new SimplePanel();
      Button ok = new Button("Close");
      ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          db.hide();
        }
      });
      holder.add(ok);
      DialogBoxContents.add(message);
      DialogBoxContents.add(holder);
      db.setWidget(DialogBoxContents);
      db.show();
    }
  }

  private static class AboutCompanionAction implements Command {
    @Override
    public void execute() {
      final DialogBox db = new DialogBox(false, true);
      db.setText("About The Companion");
      db.setStyleName("ode-DialogBox");
      db.setHeight("200px");
      db.setWidth("400px");
      db.setGlassEnabled(true);
      db.setAnimationEnabled(true);
      db.center();

      String downloadinfo = "";
      if (!YaVersion.COMPANION_UPDATE_URL1.equals("")) {
        String url = "http://" + Window.Location.getHost() + YaVersion.COMPANION_UPDATE_URL1;
        downloadinfo = "<br/>\n<a href=" + url + ">Download URL: " + url + "</a><br/>\n";
        downloadinfo += BlocklyPanel.getQRCode(url);
      }

      VerticalPanel DialogBoxContents = new VerticalPanel();
      HTML message = new HTML(
          "Companion Version " + BlocklyPanel.getCompVersion() + downloadinfo
      );

      SimplePanel holder = new SimplePanel();
      Button ok = new Button("Close");
      ok.addClickListener(new ClickListener() {
        public void onClick(Widget sender) {
          db.hide();
        }
      });
      holder.add(ok);
      DialogBoxContents.add(message);
      DialogBoxContents.add(holder);
      db.setWidget(DialogBoxContents);
      db.show();
    }
  }

  private static class CompanionUpdateAction implements Command {
    @Override
    public void execute() {
      DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
      if (currentProject == null) {
        Window.alert(MESSAGES.companionUpdateMustHaveProject());
        return;
      }
      DesignToolbar.Screen screen = currentProject.screens.get(currentProject.currentScreen);
      screen.blocksEditor.updateCompanion();
    }
  }

  private void replHardReset() {
    DesignToolbar.DesignProject currentProject = Ode.getInstance().getDesignToolbar().getCurrentProject();
    if (currentProject == null) {
      OdeLog.wlog("DesignToolbar.currentProject is null. "
          + "Ignoring attempt to do hard reset.");
      return;
    }
    DesignToolbar.Screen screen = currentProject.screens.get(currentProject.currentScreen);
    ((YaBlocksEditor)screen.blocksEditor).hardReset();
    TopToolbar instance = Ode.getInstance().getTopToolbar();
    instance.updateConnectToDropDownButton(false, false, false);
  }

  private class HardResetAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().okToConnect()) {
        replHardReset();
      }
    }
  }

  private static class ShowSplashAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().showWelcomeDialog();
    }
  }

  private static class PrivateProfileAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().switchToPrivateUserProfileView();
    }
  }
}

