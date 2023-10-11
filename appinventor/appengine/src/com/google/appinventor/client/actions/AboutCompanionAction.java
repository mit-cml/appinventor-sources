// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.appinventor.components.common.YaVersion;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AboutCompanionAction implements Command {
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
