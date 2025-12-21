// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.components.common.YaVersion;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Command for displaying information about the companion app.
 */
public class AboutCompanionAction implements Command {
  @Override
  public void execute() {
    final DialogBox db = new DialogBox(false, true);
    db.setText(MESSAGES.aboutCompanionDialogTitle());
    db.setStyleName("ode-DialogBox");
    db.setHeight("200px");
    db.setWidth("400px");
    db.setGlassEnabled(true);
    db.setAnimationEnabled(true);
    db.center();

    VerticalPanel dialogBoxContents = new VerticalPanel();

    HTML message = new HTML("<b>Select your Companion App:</b><br/><br/>");

    Button androidBtn = new Button("Android Companion");
    Button iosBtn = new Button("iOS Companion");

    androidBtn.addClickHandler((e) -> showAndroidPopup());
    iosBtn.addClickHandler((e) -> showIosPopup());

    dialogBoxContents.add(message);
    dialogBoxContents.add(androidBtn);
    dialogBoxContents.add(iosBtn);

    SimplePanel holder = new SimplePanel();
    Button ok = new Button(MESSAGES.hdrClose());
    ok.addClickHandler((e) -> db.hide());
    holder.add(ok);

    dialogBoxContents.add(holder);

    db.setWidget(dialogBoxContents);
    db.show();
  }

  private void showAndroidPopup() {
    DialogBox adb = new DialogBox(false, true);
    adb.setText("Android Companion");
    adb.setStyleName("ode-DialogBox");
    adb.setGlassEnabled(true);
    adb.setAnimationEnabled(true);

    String baseUrl = GWT.getHostPageBaseURL();
    baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    String url = baseUrl + YaVersion.COMPANION_UPDATE_URL1;

    HTML content = new HTML(
        "Download Android Companion:<br/><a href='" + url + "'>" + url + "</a><br/><br/>"
            + BlocklyPanel.getQRCode(url));

    VerticalPanel vp = new VerticalPanel();
    vp.add(content);

    Button close = new Button(MESSAGES.hdrClose());
    close.addClickHandler((e) -> adb.hide());
    vp.add(close);

    adb.setWidget(vp);
    adb.center();
    adb.show();
  }

  private void showIosPopup() {
    DialogBox idb = new DialogBox(false, true);
    idb.setText("iOS Companion");
    idb.setStyleName("ode-DialogBox");
    idb.setGlassEnabled(true);
    idb.setAnimationEnabled(true);

    String iosUrl = "https://appinventor.mit.edu/ios";

    HTML content = new HTML(
        "Download iOS Companion:<br/><a href='" + iosUrl + "'>" + iosUrl + "</a><br/><br/>"
            + BlocklyPanel.getQRCode(iosUrl));

    VerticalPanel vp = new VerticalPanel();
    vp.add(content);

    Button close = new Button(MESSAGES.hdrClose());
    close.addClickHandler((e) -> idb.hide());
    vp.add(close);

    idb.setWidget(vp);
    idb.center();
    idb.show();
  }
}
