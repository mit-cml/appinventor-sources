// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.TopPanel;
import com.google.appinventor.shared.rpc.user.Config;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import static com.google.appinventor.client.Ode.getSystemConfig;

public class OpenLinkAction implements Command {
  private static final String WIDGET_NAME_LIBRARY = "Library";
  private static final String WIDGET_NAME_GETSTARTED = "GetStarted";
  private static final String WIDGET_NAME_TUTORIALS = "Tutorials";
  private static final String WIDGET_NAME_EXTENSIONS = "Extensions";
  private static final String WIDGET_NAME_TROUBLESHOOTING = "Troubleshooting";
  private static final String WIDGET_NAME_FORUMS = "Forums";
  private static final String WIDGET_NAME_FEEDBACK = "ReportIssue";

  private String link_name = "Guide";

  // Used bu UIBinder
  public void setLink_name(String link_name) {
    this.link_name = link_name;
  }

  @Override
  public void execute() {
    Config config = getSystemConfig();
    String url;

    switch (link_name) {
      case "Feedback":
        url = config.getFeedbackUrl();
        break;
      case WIDGET_NAME_LIBRARY:
        url = config.getLibraryUrl();
        break;
      case WIDGET_NAME_GETSTARTED:
        url = config.getGetStartedUrl();
        break;
      case WIDGET_NAME_EXTENSIONS:
        url = config.getExtensionsUrl();
        break;
      case WIDGET_NAME_TUTORIALS:
        url = config.getTutorialsUrl();
        break;
      case WIDGET_NAME_TROUBLESHOOTING:
        url = config.getTroubleshootingUrl();
        break;
      case WIDGET_NAME_FORUMS:
        url = config.getForumsUrl();
        break;
      case WIDGET_NAME_FEEDBACK:
        url = config.getFeedbackUrl();
        break;
      default:
        url = config.getGuideUrl();
        break;
    }
    if (config.getGuideUrl() != null) {
      Window.open(url, TopPanel.WINDOW_OPEN_LOCATION, TopPanel.WINDOW_OPEN_FEATURES);
    }
  }
}
