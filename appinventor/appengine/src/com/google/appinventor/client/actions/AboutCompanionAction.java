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

    String downloadinfo = "";
    //noinspection ConstantValue
    if (!YaVersion.COMPANION_UPDATE_URL1.isEmpty()) {
      String baseUrl = GWT.getHostPageBaseURL();
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);  // strip trailing slash
      String url = baseUrl + YaVersion.COMPANION_UPDATE_URL1;
      downloadinfo = "<br/>\n<a href=" + url + ">" + MESSAGES.companionDownloadUrl(url)
          + "</a><br/>\n" + BlocklyPanel.getQRCode(url);
    }

    VerticalPanel dialogBoxContents = new VerticalPanel();
    HTML message = new HTML(
        MESSAGES.companionVersion(BlocklyPanel.getCompVersion()) + downloadinfo
    );

    SimplePanel holder = new SimplePanel();
    Button ok = new Button(MESSAGES.hdrClose());
    ok.addClickHandler((e) -> db.hide());
    holder.add(ok);
    dialogBoxContents.add(message);
    dialogBoxContents.add(holder);
    db.setWidget(dialogBoxContents);
    db.show();
  }
}
