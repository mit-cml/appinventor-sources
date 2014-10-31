// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;

/**
 * Command for downloading files.
 *
 */
public class DownloadFileCommand extends ChainableCommand {
  /**
   * Creates a new command for downloading files
   */
  public DownloadFileCommand() {
    // Since we don't know when the download is finished, we can't support a
    // command after this one.
    super(null); // no next command
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;
  }

  @Override
  public void execute(ProjectNode node) {
    Downloader.getInstance().download(
        StorageUtil.getFilePath(node.getProjectId(), node.getFileId()));
  }
}
