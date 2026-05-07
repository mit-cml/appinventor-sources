// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.Command;

public class DownloadCsrAction implements Command {
  @Override
  public void execute() {
    Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE
        + ServerLayout.DOWNLOAD_CSR);
  }
}
