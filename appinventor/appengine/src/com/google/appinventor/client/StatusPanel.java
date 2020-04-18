// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * The status panel contains various links.
 *
 */
public class StatusPanel extends Composite {

  interface StatusPanelUiBinder extends UiBinder<FlowPanel, StatusPanel> {}
  private static final StatusPanelUiBinder UI_BINDER = GWT.create(StatusPanelUiBinder.class);

  @UiField(provided = true) FlowPanel footer = new FlowPanel("footer");
  @UiField Anchor tosLink;

  /**
   * Initializes and assembles all UI elements shown in the status panel.
   */
  public StatusPanel() {
    initWidget(UI_BINDER.createAndBindUi(this));

    String tosUrl = Ode.getSystemConfig().getTosUrl();
    if (!Strings.isNullOrEmpty(tosUrl)) {
      tosLink.setHref(tosUrl);
    } else {
      tosLink.removeFromParent();
    }
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }
}
