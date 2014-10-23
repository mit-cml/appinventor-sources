// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.UserProject;

/**
 * Superclass for wizards creating new projects.
 *
 */
public abstract class NewProjectWizard extends Wizard {

  /**
   * Interface for a command to run after a new project was successfully
   * created.
   */
  public static interface NewProjectCommand {

    /**
     * Will be invoked after a new project was created.
     *
     * @param project  newly created project
     */
    void execute(Project project);
  }

  // Index to give new projects a unique default name
  protected static int projectIndex = 1;

  protected NewProjectWizard(String title) {
    super(title, true, false);
  }

  /**
   * Creates a new project.
   *
   * @param projectType project type
   * @param projectName project name
   * @param params optional parameters (project type dependent)
   * @param onSuccessCommand command to be executed after process creation
   *                         succeeds (can be {@code null})
   */
  protected void createNewProject(String projectType, final String projectName,
      NewProjectParameters params, final NewProjectCommand onSuccessCommand) {
    // Callback for updating the project explorer after the project was created on the back-end
    final Ode ode = Ode.getInstance();
    OdeAsyncCallback<UserProject> callback =
        new OdeAsyncCallback<UserProject>(
            // failure message
            MESSAGES.createProjectError()) {
          @Override
          public void onSuccess(UserProject projectInfo) {
            // Update project explorer
            Project project = ode.getProjectManager().addProject(projectInfo);
            if (onSuccessCommand != null) {
              onSuccessCommand.execute(project);
            }
          }
    };

    // TODO(user): input error checking

    // Create the project on the back-end
    ode.getProjectService().newProject(projectType, projectName, params, callback);

    projectIndex++;
  }
}
