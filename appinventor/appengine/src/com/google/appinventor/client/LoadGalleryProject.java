// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * This module is used to tell the App Inventor server to load a
 * project from the new Gallery. It works similarly to the template
 * loading code, however whereas the template loading code first
 * fetching the project contents to the client browser and then sends
 * it to the server, we request the server to load the project from
 * the gallery itself and then let us know when it is ready to be
 * loaded into the client.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */

package com.google.appinventor.client;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.io.IOException;

public class LoadGalleryProject {

  public static void openProjectFromGallery(final String galleryId, final NewProjectCommand onSuccessCommand) {
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
      // failure message
      MESSAGES.createProjectError()) {
        @Override
        public void onSuccess(UserProject projectInfo) {
          // This just adds the new project to the project manager, not to AppEngine
          Project project = Ode.getInstance().getProjectManager().addProject(projectInfo);
          // And this opens the project
          if (onSuccessCommand != null) {
            onSuccessCommand.execute(project);

          }
        }
        @Override
        public void onFailure(Throwable caught) {
          if (caught instanceof IOException) {
            ErrorReporter.reportError("IOException: " + caught.getMessage());
            return;
          } else {
            super.onFailure(caught);
          }
        }
      };
    Ode.getInstance().getProjectService().loadFromGallery(galleryId, callback);
  }

}

