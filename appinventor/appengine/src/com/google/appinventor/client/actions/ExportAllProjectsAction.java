// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class ExportAllProjectsAction implements Command {
  @Override
  public void execute() {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_DOWNLOAD_ALL_PROJECTS_SOURCE_YA);

    // Is there a way to disable the Download All button until this completes?
    if (Window.confirm(MESSAGES.downloadAllAlert())) {

      Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
          ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE);
    }
  }
}
