// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.Ode.getSystemConfig;

import com.google.appinventor.client.actions.SelectLanguage;

import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;

import com.google.appinventor.shared.rpc.user.Config;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Logger;

/**
 * The top panel, which contains the main menu, various links plus ads.
 *
 */
public class TopPanel extends Composite {
  // Strings for links and dropdown menus:

  interface TopPanelUiBinder extends UiBinder<FlowPanel, TopPanel> {}

  private static final String WIDGET_NAME_LANGUAGE = "Language";
  private static final String WIDGET_NAME_DELETE_ACCOUNT = "DeleteAccount";
  public static final String WINDOW_OPEN_FEATURES = "menubar=yes,location=yes,resizable=yes,scrollbars=yes,status=yes";
  public static final String WINDOW_OPEN_LOCATION = "_ai2";

  @UiField protected TopToolbar topToolbar;
  @UiField protected ImageElement logo;
  @UiField protected Label readOnly;
  @UiField protected FlowPanel rightPanel;
  @UiField protected DropDownButton languageDropDown;
  @UiField protected DropDownButton accountButton;
  @UiField protected DropDownItem deleteAccountItem;
  @UiField protected FlowPanel links;

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
  private static final Logger LOG = Logger.getLogger(TopPanel.class.getName());

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
    bindUI();
    Config config = getSystemConfig();
    String logoUrl = config.getLogoUrl();


    if (!Strings.isNullOrEmpty(logoUrl)) {
      try {
        Image wpr = Image.wrap(logo);
        wpr.addClickHandler(new WindowOpenClickHandler(logoUrl));
      } catch (AssertionError e) {
        LOG.warning("assertion error in getting Image from logo url");
      }
    }

    if (Ode.getInstance().isReadOnly()) {
      accountButton.setItemVisible(WIDGET_NAME_DELETE_ACCOUNT, false);
    } else {
      readOnly.removeFromParent();
    }

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

  public void bindUI() {
    TopPanelUiBinder uibinder = GWT.create(TopPanelUiBinder.class);
    initWidget(uibinder.createAndBindUi(this));
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  public TopToolbar getTopToolbar() {
    return topToolbar;
  }

  private String getDisplayName(String localeName){
    String nativeName=LocaleInfo.getLocaleNativeDisplayName(localeName);
    try {
      return LANGUAGES.get(localeName);
    } catch (MissingResourceException e) {
      return nativeName;
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
}

