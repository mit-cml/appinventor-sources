// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.utils.Promise.resolve;

import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectServiceAsync;
import com.google.appinventor.shared.rpc.project.UserProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages projects.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ProjectManager {
  // Map to find the project from a project ID.
  private final Map<Long, Project> projectsMap;

  // List of listeners for any project manager events.
  private final List<ProjectManagerEventListener> projectManagerEventListeners;

  private Promise<List<Project>> loadProjectPromise = null;

  /**
   * Flag indicating whether the project infos have all loaded.
   */
  private volatile boolean projectsLoaded = false;

  /**
   * Creates a new projects manager.
   */
  public ProjectManager() {
    projectsMap = new HashMap<>();
    projectManagerEventListeners = new ArrayList<>();
  }

  /**
   * Load the user's projects.
   *
   * <p>The returned Promise is a singleton representing the result of loading the initial
   * project list at the start of the session.</p>
   *
   * @return a Promise to load the user's projects
   */
  public Promise<List<Project>> ensureProjectsLoadedFromServer(ProjectServiceAsync projectService) {
    if (loadProjectPromise == null) {
      loadProjectPromise = Promise.call(MESSAGES.projectInformationRetrievalError(),
              projectService::getProjectInfos)
          .then(projectInfos -> {
            for (UserProject projectInfo : projectInfos) {
              addProject(projectInfo);
            }
            projectsLoaded = true;
            return resolve(new ArrayList<>(projectsMap.values()));
          });
    }
    return loadProjectPromise;
  }

  /**
   * Returns a list of all projects.
   *
   * @return  a list of projects
   */
  public List<Project> getProjects() {
    return new ArrayList<>(projectsMap.values());
  }

  /**
   * Returns a list of the projects with the given project name prefix.
   *
   * @param prefix  project name prefix
   * @return  a list of projects
   */
  public List<Project> getProjects(String prefix) {
    List<Project> projects = new ArrayList<>();

    for (Project project : projectsMap.values()) {
      if (project.getProjectName().startsWith(prefix)) {
        projects.add(project);
      }
    }
    return projects;
  }

  public List<Project> getProjectsWithoutFolder() {
    List<Project> projects = new ArrayList<Project>();
    for (Project project : projectsMap.values()) {
      if (project.getHomeFolder() == null) {
        projects.add(project);
      }
    }
    return projects;
  }

  /**
   * Returns the project that belongs to a project node.
   *
   * @param node the project node for which we want to retrieve the project
   * @return the project of the node
   */
  public Project getProject(ProjectNode node) {
    return projectsMap.get(node.getProjectId());
  }

  /**
   * Returns the project for the given project name.
   *
   * <p>Note that in case of multiple projects with the same name, only the
   * first matching project will be returned.
   *
   * @param name  project name
   * @return  the corresponding project or {@code null}
   */
  public Project getProject(String name) {
    for (Project project : projectsMap.values()) {
      if (project.getProjectName().equals(name)) {
        return project;
      }
    }

    return null;
  }

  /**
   * Returns the project for the given project ID.
   *
   * @param projectId project ID
   * @return the corresponding project or {@code null}
   */
  public Project getProject(long projectId) {
    return projectsMap.get(projectId);
  }

  /**
   * Adds a new project to this project manager.
   *
   * @param projectInfo information about the project
   * @return new project
   */
  public Project addProject(UserProject projectInfo) {
    Project project = new Project(projectInfo);
    projectsMap.put(projectInfo.getProjectId(), project);
    fireProjectAdded(project);
    return project;
  }

  /**
   * Removes the project from trash permanently.
   *
   * @param projectId project ID
   */

  public void removeDeletedProject(long projectId) {
    projectsMap.remove(projectId);
  }

  /**
   * Adds a {@link ProjectManagerEventListener} to the listener list.
   *
   * @param listener  the {@code ProjectManagerEventListener} to be added
   */
  public void addProjectManagerEventListener(ProjectManagerEventListener listener) {
    projectManagerEventListeners.add(listener);
    if (projectsLoaded) {
      // inform the listener that projects have already been loaded
      listener.onProjectsLoaded();
    }
  }

  /**
   * Removes a {@link ProjectManagerEventListener} from the listener list.
   *
   * @param listener  the {@code ProjectManagerEventListener} to be removed
   */
  public void removeProjectManagerEventListener(ProjectManagerEventListener listener) {
    projectManagerEventListeners.remove(listener);
  }

  private List<ProjectManagerEventListener> copyProjectManagerEventListeners() {
    return new ArrayList<>(projectManagerEventListeners);
  }

  /*
   * Triggers a 'project added' event to be sent to the listener on the listener list.
   */
  private void fireProjectAdded(Project project) {
    for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
      listener.onProjectAdded(project);
    }
  }

  public boolean isProjectInTrash(long projectId) {
    Project project = projectsMap.get(projectId);
    return project != null && project.isInTrash();
  }
}
