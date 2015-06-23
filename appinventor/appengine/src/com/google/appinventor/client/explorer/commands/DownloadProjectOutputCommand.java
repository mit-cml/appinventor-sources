// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.common.base.Preconditions;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectNode;

/**
 * Command for downloading a project target
 *
 * <p/>This command is often chained with SaveAllEditorsCommand and BuildCommand.
 *
 */
public class DownloadProjectOutputCommand extends ChainableCommand {
  // The download target
  private String target;

  /**
   * Creates a new command for downloading a project target.
   *
   * @param target the target to be downloaded (must be non-null,
   *               use "" if there is no particular target)
   */
  public DownloadProjectOutputCommand(String target) {
    // Since we don't know when the download is finished, we can't support a
    // command after this one.
    super(null); // no next command
    Preconditions.checkNotNull(target);
    this.target = target;
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return false;
  }

  @Override
  public void execute(ProjectNode node) {
    Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
        ServerLayout.DOWNLOAD_PROJECT_OUTPUT + "/" + node.getProjectId() + "/" + target);
  }
}
