// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.GalleryPage;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.PostUtil;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;

import java.util.Date;

/**
 * Command for saving all editors.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class SaveAllEditorsCommand extends ChainableCommand {
  /**
   * Creates a new save all editors command, with additional behavior provided
   * by another ChainableCommand.
   *
   * @param nextCommand the command to execute after the save has finished
   */
  public SaveAllEditorsCommand(ChainableCommand nextCommand) {
    super(nextCommand);
  }

  @Override
  public boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  public void execute(final ProjectNode node) {
    // TODO: Not sure if this is only guaranteed to happen after project has been saved
    Project currentSelectedProject = Ode.getInstance().getProjectManager().getProject(node.getProjectId());
    if(!currentSelectedProject.isPublished()){
      // app is not yet published
      publishToGallery(currentSelectedProject);
    } else {
      updateGalleryApp(currentSelectedProject);
    }
  }

  private void publishToGallery(Project p) {
    User user = Ode.getInstance().getUser();
    PostUtil.addAppToGallery(user, p.getProjectId(), p.getProjectName());
  }

  private void updateGalleryApp(Project p) {
    // setup what happens when we load the app in
    final OdeAsyncCallback<GalleryApp> callback = new OdeAsyncCallback<GalleryApp>(
            MESSAGES.galleryError()) {
      @Override
      public void onSuccess(GalleryApp app) {
        // the server has returned us something
        int editStatus= GalleryPage.UPDATEAPP;
        Ode.getInstance().switchToGalleryAppView(app, editStatus);
      }
    };
    // ok, this is below the call back, but of course it is done first
    Ode.getInstance().getGalleryService().getApp(p.getGalleryId(),callback);
  }
}
