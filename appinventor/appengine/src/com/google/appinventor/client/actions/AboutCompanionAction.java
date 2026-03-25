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
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * Command for displaying information about the companion app.
 * Shows a choice between Android and IOS companions.
 */
public class AboutCompanionAction implements Command {
  @Override
  public void execute() {
    // Main dialog (choice between Android and IOS)

    final DialogBox mainDialog = new DialogBox(false, true);
    mainDialog.setText(MESSAGES.aboutCompanionDialogTitle());
    mainDialog.setStyleName("ode-DialogBox");
    mainDialog.setHeight("150px");
    mainDialog.setWidth("400px");
    mainDialog.setGlassEnabled(true);
    mainDialog.setAnimationEnabled(true);
    mainDialog.center();

    VerticalPanel mainContents = new VerticalPanel();
    mainContents.setSpacing(10);

    HTML choice = new HTML("<p>" + MESSAGES.companionChoosePlatform() + "</p>");

    //Android and Ios buttons
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setSpacing(10);
    
    Button androidBtn = new Button(MESSAGES.companionAndroidButton());
    Button iosBtn = new Button(MESSAGES.companionIosButton());
    Button cancelBtn = new Button(MESSAGES.hdrClose());

    androidBtn.addClickHandler(e -> {
      mainDialog.hide();
      showAndroidDialog();
    });

    iosBtn.addClickHandler(e -> {
      mainDialog.hide();
      showIosDialog();
    });

    cancelBtn.addClickHandler(e -> mainDialog.hide());

    buttonPanel.add(androidBtn);
    buttonPanel.add(iosBtn);

    mainContents.add(choice);
    mainContents.add(buttonPanel);
    mainContents.add(cancelBtn);

    mainDialog.setWidget(mainContents);
    mainDialog.show();
  }

  private void showAndroidDialog(){
    final DialogBox db = new DialogBox(false, true);
    db.setText(MESSAGES.companionAndroidTitle());
    db.setStyleName("ode-DialogBox");
    db.setHeight("200px");
    db.setWidth("400px");
    db.setGlassEnabled(true);
    db.setAnimationEnabled(true);
    db.center();    

    String downloadinfo = "";
    //noinspection ConstantValue
    if (!YaVersion.COMPANION_UPDATE_URL1.isEmpty()) {
      String baseUrl = GWT.getHostPageBaseURL();
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1); // strip trailing slash
      String url = baseUrl + YaVersion.COMPANION_UPDATE_URL1;
      downloadinfo = "<br/>\n<a href=" + url + ">"
          + MESSAGES.companionDownloadUrl(url)
          + "</a><br/>\n" + BlocklyPanel.getQRCode(url);
    } else{
      String fallbackUrl = "https://play.google.com/store/apps/details?id=edu.mit.appinventor.aicompanion3";
      downloadinfo = "<br/>\n<a href=\"" + fallbackUrl + "\">"
          + MESSAGES.companionDownloadUrl(fallbackUrl)
          + "</a><br/>\n" + BlocklyPanel.getQRCode(fallbackUrl);
    }

    VerticalPanel contents = new VerticalPanel();
    HTML message = new HTML(
        MESSAGES.companionVersion(BlocklyPanel.getCompVersion()) + downloadinfo
    );
    SimplePanel holder = new SimplePanel();
    Button ok = new Button(MESSAGES.hdrClose());
    ok.addClickHandler(e -> db.hide());
    holder.add(ok);
    contents.add(message);
    contents.add(holder);
    db.setWidget(contents);
    db.show();
  }

  // ios dialog
  private void showIosDialog() {
    final DialogBox db = new DialogBox(false, true);
    db.setText(MESSAGES.companionIosTitle());
    db.setStyleName("ode-DialogBox");
    db.setHeight("200px");
    db.setWidth("400px");
    db.setGlassEnabled(true);
    db.setAnimationEnabled(true);
    db.center();

    // ios App Store link
    String iosUrl = YaVersion.COMPANION_IOS_URL;
    String iosInfo = "<p>" + MESSAGES.companionIosMessage() + "</p>"
        + "<a href=\"" + iosUrl + "\">" + iosUrl + "</a><br/>\n"
        + BlocklyPanel.getQRCode(iosUrl);

    VerticalPanel contents = new VerticalPanel();
    HTML message = new HTML(iosInfo);
    SimplePanel holder = new SimplePanel();
    Button ok = new Button(MESSAGES.hdrClose());
    ok.addClickHandler(e -> db.hide());
    holder.add(ok);
    contents.add(message);
    contents.add(holder);
    db.setWidget(contents);
    db.show();
  }
}