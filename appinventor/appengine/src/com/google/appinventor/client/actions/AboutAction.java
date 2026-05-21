// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.blocks.BlocklyPanel;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.common.base.Strings;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AboutAction implements Command {
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
