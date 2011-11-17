// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.Ode;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;


/**
 * The status panel contains various links.
 *
 */
public class StatusPanel extends Composite {
  private static Label buildTimeStamp = new Label();

  // Note that timestamp will be the real timestamp only
  // when running the deploy jar.
  public static void showBuildTimeStamp(String timestamp) {
    buildTimeStamp.setText("Build: " + timestamp);
  }

  private String AppInventorFooter =
    "&copy;2010 Google" +
    " - <a href='" + Ode.APP_INVENTOR_URL + "/about/'" +
    " target=_blank>" + MESSAGES.aboutLink() + "</a>" +
    " - <a href='" + Ode.APP_INVENTOR_URL + "/about/privacy.html'" +
    " target=_blank>" + MESSAGES.privacyLink() + "</a>" +
    " - <a href='" + Ode.APP_INVENTOR_URL + "/about/termsofservice.html'" +
    " target=_blank>" + MESSAGES.termsLink() + "</a>";

  /**
   * Initializes and assembles all UI elements shown in the status panel.
   */
  public StatusPanel() {
    HorizontalPanel hpanel = new HorizontalPanel();
    hpanel.setWidth("100%");
    hpanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
    hpanel.add(new HTML(AppInventorFooter));
    hpanel.add(buildTimeStamp);
    hpanel.setCellHorizontalAlignment(buildTimeStamp, HorizontalPanel.ALIGN_RIGHT);

    initWidget(hpanel);
    setStyleName("ode-StatusPanel");
  }
}
