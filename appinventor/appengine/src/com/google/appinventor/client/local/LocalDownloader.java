// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.local;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.shared.rpc.ServerLayout;

public class LocalDownloader extends Downloader {
  @Override
  public final void download(String path) {
    ErrorReporter.hide();
    String[] parts = path.split("/");
    if (ServerLayout.DOWNLOAD_PROJECT_SOURCE.equals(parts[1])) {
      String projectId = parts[parts.length - 1];
      LocalProjectService projectService = (LocalProjectService) Ode.getInstance().getProjectService();
      String tempProjectName = projectService.getProjectName(projectId);
      final String projectName = tempProjectName != null ? tempProjectName : "Project" + projectId;
      projectService.exportProject(Long.parseLong(projectId))
          .then(zip -> {
            String fileName = projectName + ".aia";
            triggerDownload(zip, fileName);
            return Promise.resolve(zip);
          });
    }
  }

  private static native void triggerDownload(String zipBase64, String fileName) /*-{
      var a = document.createElement("a");
      a.href = 'data:application/octet-stream;base64,' + zipBase64;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
  }-*/;
}
