// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.utils.PostUtil;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.user.User;

/**
 * Command for sharing project to gallery.
 */
public class ShareToGalleryCommand extends ChainableCommand {
  /**
   * Creates a new share to gallery command, with additional behavior provided
   * by another ChainableCommand.
   *
   * @param nextCommand the command to execute after the save has finished
   */
  public ShareToGalleryCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    final Ode ode = Ode.getInstance();
    Project currentProject = ode.getProjectManager().getProject(node);
    publishToGallery(currentProject);
  }

  private void publishToGallery(Project p) {
    User user = Ode.getInstance().getUser();
    PostUtil.addAppToGallery(user, p.getProjectId(), p.getProjectName());
  }
}
