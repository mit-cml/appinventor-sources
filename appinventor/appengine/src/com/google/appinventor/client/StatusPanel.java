// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import com.google.common.base.Strings;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

import static com.google.appinventor.client.Ode.MESSAGES;


/**
 * The status panel contains various links.
 *
 */
public class StatusPanel extends Composite {
  /**
   * Initializes and assembles all UI elements shown in the status panel.
   */
  public StatusPanel() {
    HorizontalPanel hpanel = new HorizontalPanel();
    hpanel.setWidth("100%");
    hpanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    String tosUrl = Ode.getInstance().getSystemConfig().getTosUrl();
    if (!Strings.isNullOrEmpty(tosUrl)) {
      String appInventorFooter =
          "<a href=\"" + tosUrl + "\" target=\"_blank\">" + MESSAGES.privacyTermsLink() + "</a>";
      hpanel.add(new HTML(appInventorFooter));
    }

    // This shows the git version and the date of the build
//    String version = GitBuildId.getVersion();
//    String date = GitBuildId.getDate();
//    if (version != null && date != null) {
//      Label buildId = new Label(MESSAGES.gitBuildId(date, version));
//      hpanel.add(buildId);
//      hpanel.setCellHorizontalAlignment(buildId, HorizontalPanel.ALIGN_RIGHT);
//    }

    initWidget(hpanel);
    setStyleName("ode-StatusPanel");
  }
}
