// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * This view is displayed when a service has passed an expiration date. The expiration date is
 * configured in appengine/war/WEB-INF/appengine-web.xml. You can control the appearance by
 * modifying the &lt;ui:style&gt; block in ExpiredServiceOverlay.ui.xml.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class ExpiredServiceOverlay extends SimplePanel {

  interface ExpiredServiceOverlayUiBinder extends UiBinder<HTMLPanel, ExpiredServiceOverlay> {
  }

  private static final UiBinder<HTMLPanel, ExpiredServiceOverlay> BINDER =
      GWT.create(ExpiredServiceOverlayUiBinder.class);

  public ExpiredServiceOverlay() {
    add(BINDER.createAndBindUi(this));
  }
}
