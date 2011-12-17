// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.common.version.MercurialBuildId;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;


/**
 * The status panel contains various links.
 *
 */
public class StatusPanel extends Composite {
  private String AppInventorFooter =
    "&copy;2010 Google" +
    " - <a href='" + Ode.APP_INVENTOR_DOCS_URL + "/about/'" +
    " target=_blank>" + MESSAGES.aboutLink() + "</a>" +
    " - <a href='" + Ode.APP_INVENTOR_DOCS_URL + "/about/privacy.html'" +
    " target=_blank>" + MESSAGES.privacyLink() + "</a>" +
    " - <a href='" + Ode.APP_INVENTOR_DOCS_URL + "/about/termsofservice.html'" +
    " target=_blank>" + MESSAGES.termsLink() + "</a>";

  /**
   * Initializes and assembles all UI elements shown in the status panel.
   */
  public StatusPanel() {
    HorizontalPanel hpanel = new HorizontalPanel();
    hpanel.setWidth("100%");
    hpanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
    hpanel.add(new HTML(AppInventorFooter));

    // This shows the id from mercurial hg id -i -n
    String version = MercurialBuildId.getVersion();
    String id = MercurialBuildId.getId();
    if (version != null && id != null) {
      Label buildId = new Label(MESSAGES.mercurialBuildId(version, id));
      hpanel.add(buildId);
      hpanel.setCellHorizontalAlignment(buildId, HorizontalPanel.ALIGN_RIGHT);
    }

    initWidget(hpanel);
    setStyleName("ode-StatusPanel");
  }
}
