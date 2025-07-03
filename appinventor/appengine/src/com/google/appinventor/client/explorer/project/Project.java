// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.client.explorer.folder.ProjectFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a project.
 *
 */
public final class Project {
  // Information about the project
  private final UserProject projectInfo;

  // List of listeners for any project changes.
  private final List<ProjectChangeListener> projectChangeListeners;

  private boolean loadingInProgress;

  // Root project node
  private ProjectRootNode projectRoot; // lazily and asynchronously initialized

  // Project specific settings
  private ProjectSettings settings; // lazily and asynchronously initialized

  private ProjectFolder homeFolder = null;

  /**
   * Creates a new project.
   *
   * @param projectInfo information about the project
   */
  Project(UserProject projectInfo) {
    this.projectInfo = projectInfo;

    projectChangeListeners = new ArrayList<ProjectChangeListener>();
  }

  /*
   * Loads the project's nodes from the backend.
   */
  public void loadProjectNodes() {
    if (projectRoot == null && !loadingInProgress) {
      loadingInProgress = true;

      if (settings == null) {
        settings = new ProjectSettings(Project.this);
        settings.loadSettings();
      }

      Ode.getInstance().getProjectService().getProject(
          getProjectId(),
          new OdeAsyncCallback<ProjectRootNode>(
              // failure message
              MESSAGES.projectLoadError()) {
            @Override
            public void onSuccess(ProjectRootNode result) {
              projectRoot = result;

              loadingInProgress = false;
              fireProjectLoaded();
            }

            @Override
            public void onFailure(Throwable caught) {
              loadingInProgress = false;
              super.onFailure(caught);
            }
      });
    }
  }

  /**
   * Returns the id of this project.
   *
   * @return  project id
   */
  public long getProjectId() {
    return projectInfo.getProjectId();
  }

  /**
   * Returns the name of this project.
   *
   * @return  project name
   */
  public String getProjectName() {
    return projectInfo.getProjectName();
  }

  /**
   * Returns the type of this project.
   *
   * @return  project type
   */
  public String getProjectType() {
    return projectInfo.getProjectType();
  }

  /**
   * Returns the date of when the project was created.
   *
   * @return  date created in milliseconds
   */
  public long getDateCreated() {
    return projectInfo.getDateCreated();
  }

  /**
   * Returns the date of when the project was last modified.
   *
   * @return  date modified in milliseconds
   */
  public long getDateModified() {
    return projectInfo.getDateModified();
  }

  /**
   * Sets the date of when the project was last modified.
   *
   */
  public void setDateModified(long date) {
    projectInfo.setDateModified(date);
  }

  /**
   * Returns the date of when the project was last exported as an apk/aab/other.
   *
   * @return  date modified in milliseconds
   */
  public long getDateBuilt() {
    return projectInfo.getDateBuilt();
  }

  /**
   * Sets the date of when the project was last exported.
   *
   */
  public void setDateBuilt(long date) {
     projectInfo.setDateBuilt(date);
  }


  /**
   * The project-folder relationship is stored in the folder object.
   * This is just a back-reference that is set when the folder is created.
   */
  public void setHomeFolder(ProjectFolder folder) {
    homeFolder = folder;
  }

  public ProjectFolder getHomeFolder() {
    return homeFolder;
  }

  /**
   * Returns the project specific settings, or null if the settings haven't
   * been loaded.
   *
   * @return  project settings
   */
  public ProjectSettings getSettings() {
    return settings;
  }

  /**
   * Returns the project's root node, or null if the project nodes haven't
   * been loaded.
   *
   * @return project root node
   */
  public ProjectRootNode getRootNode() {
    return projectRoot;
  }

  /**
   * Adds the given node to the project.
   *
   * <p/>If a node with the same file id already exists, the node is not added
   * and the existing node is returned. However, the 'project node added' event
   * is still fired.
   *
   * @param parent  parent node of node to be added
   * @param node  node to be added
   * @return the node that was added, or the existing node with the same file id
   */
  public ProjectNode addNode(ProjectNode parent, ProjectNode node) {
    boolean nodeAlreadyExists = false;
    for (ProjectNode child : parent.getChildren()) {
      if (child.getFileId().equals(node.getFileId())) {
        nodeAlreadyExists = true;
        node = child;
        break;
      }
    }
    if (!nodeAlreadyExists) {
      parent.addChild(node);
    }

    // Event if the node already exists, we still call fireProjectNodeAdded so that asset property
    // editors can detect that an asset was updated.
    fireProjectNodeAdded(node);
    return node;
  }

  /**
   * Deletes the given node from the project.
   *
   * @param node  node to be deleted
   */
  public void deleteNode(ProjectNode node) {
    ProjectNode parent = node.getParent();
    if (parent != null) {
      parent.removeChild(node);
    }
    fireProjectNodeRemoved(node);
  }

  public void moveToTrash() {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_MOVE_TO_TRASH_PROJECT_YA, getProjectName());
    Ode.getInstance().getProjectService().moveToTrash(getProjectId(),
        new OdeAsyncCallback<UserProject>(
            // failure message
            MESSAGES.moveToTrashProjectError()) {
          @Override
          public void onSuccess(UserProject project) {
            if (project.getProjectId() == projectInfo.getProjectId()) {
              projectInfo.moveToTrash();
              Ode.getInstance().getProjectManager().trashProject(getProjectId());
            }
          }
        });
  }

  public void restoreFromTrash() {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
            Tracking.PROJECT_ACTION_RESTORE_PROJECT_YA, getProjectName());
    Ode.getInstance().getProjectService().restoreProject(getProjectId(),
            new OdeAsyncCallback<UserProject>(
                    // failure message
                    MESSAGES.restoreProjectError()) {
              @Override
              public void onSuccess(UserProject project) {
                if (project.getProjectId() == projectInfo.getProjectId()) {
                  projectInfo.restoreFromTrash();
                  Ode.getInstance().getProjectManager().restoreTrashProject(getProjectId());
                }
              }
            });
  }

  public void deleteFromTrash() {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_DELETE_PROJECT_YA, getProjectName());
    final OdeAsyncCallback<Void> deleteCallback = new OdeAsyncCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        Ode.getInstance().getProjectManager().removeDeletedProject(getProjectId());
      }
    };
    Ode.getInstance().getProjectService().deleteProject(getProjectId(), deleteCallback);
  }

  public boolean isInTrash() {
    return projectInfo.isInTrash();
  }

  /**
   * Adds a {@link ProjectChangeListener} to the listener list.
   *
   * @param listener  the {@code ProjectChangeListener} to be added
   */
  public void addProjectChangeListener(ProjectChangeListener listener) {
    projectChangeListeners.add(listener);
  }

  /**
   * Removes a {@link ProjectChangeListener} from the listener list.
   *
   * @param listener  the {@code ProjectChangeListener} to be removed
   */
  public void removeProjectChangeListener(ProjectChangeListener listener) {
    projectChangeListeners.remove(listener);
  }

  private List<ProjectChangeListener> copyProjectChangeListeners() {
    return new ArrayList<ProjectChangeListener>(projectChangeListeners);
  }

  /*
   * Triggers a 'project loaded' event to be sent to the listener on the listener list.
   */
  private void fireProjectLoaded() {
    for (ProjectChangeListener listener : copyProjectChangeListeners()) {
      listener.onProjectLoaded(this);
    }
  }

  /*
   * Triggers a 'project node added' event to be sent to the listener on the listener list.
   */
  private void fireProjectNodeAdded(ProjectNode node) {
    for (ProjectChangeListener listener : copyProjectChangeListeners()) {
      listener.onProjectNodeAdded(this, node);
    }
  }

  /*
   * Triggers a 'project node removed' event to be sent to the listener on the listener list.
   */
  private void fireProjectNodeRemoved(ProjectNode node) {
    for (ProjectChangeListener listener : copyProjectChangeListeners()) {
      listener.onProjectNodeRemoved(this, node);
    }
  }
}
