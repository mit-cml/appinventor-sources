// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.project.ProjectNode;
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

  /**
   * Creates a new projects manager.
   */
  public ProjectManager() {
    projectsMap = new HashMap<Long, Project>();
    projectManagerEventListeners = new ArrayList<ProjectManagerEventListener>();
    Ode.getInstance().getProjectService().getProjectInfos(
        new OdeAsyncCallback<List<UserProject>>(
        MESSAGES.projectInformationRetrievalError()) {
      @Override
      public void onSuccess(List<UserProject> projectInfos) {
        for (UserProject projectInfo : projectInfos) {
          addProject(projectInfo);
        }
        fireProjectsLoaded();
      }
    });
  }

  /**
   * Returns a list of all projects.
   *
   * @return  a list of projects
   */
  public List<Project> getProjects() {
    List<Project> projects = new ArrayList<Project>();

    for (Project project : projectsMap.values()) {
      projects.add(project);
    }

    return projects;
  }

  /**
   * Returns a list of the projects with the given project name prefix.
   *
   * @param prefix  project name prefix
   * @return  a list of projects
   */
  public List<Project> getProjects(String prefix) {
    List<Project> projects = new ArrayList<Project>();

    for (Project project : projectsMap.values()) {
      if (project.getProjectName().startsWith(prefix)) {
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
   * Removes the given project.
   *
   * @param projectId project ID
   */
  public void removeProject(long projectId) {
    Project project = projectsMap.remove(projectId);
    fireProjectRemoved(project);
  }

  /**
   * Handles situation when a project has been published
   *
   * @param projectId project ID
   * @param galleryId gallery ID
   */
  public void publishProject (long projectId, long galleryId){
    Project project = getProject(projectId);
    project.setGalleryId(galleryId);
    projectsMap.put(projectId, project);
    fireProjectPublishedOrUnpublished();
  }
    /**
   * Handles situation when a project has been published
   *
   * @param projectId project ID
   * @param galleryId gallery ID
   */
  public void UnpublishProject (long projectId) {
    Project project = getProject(projectId);
    project.setGalleryId(UserProject.NOTPUBLISHED);
    projectsMap.put(projectId, project);
    fireProjectPublishedOrUnpublished();
  }

  /**
   * Adds a {@link ProjectManagerEventListener} to the listener list.
   *
   * @param listener  the {@code ProjectManagerEventListener} to be added
   */
  public void addProjectManagerEventListener(ProjectManagerEventListener listener) {
    projectManagerEventListeners.add(listener);
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
    return new ArrayList<ProjectManagerEventListener>(projectManagerEventListeners);
  }

  /*
   * Triggers a 'project added' event to be sent to the listener on the listener list.
   */
  private void fireProjectAdded(Project project) {
    for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
      listener.onProjectAdded(project);
    }
  }

  /*
   * Triggers a 'project removed' event to be sent to the listener on the listener list.
   */
  private void fireProjectRemoved(Project project) {
    for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
      listener.onProjectRemoved(project);
    }
  }

  /*
   * Triggers a 'projects loaded' event to be sent to the listener on the listener list.
   */
  private void fireProjectsLoaded() {
    for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
      listener.onProjectsLoaded();
    }
  }

  private void fireProjectPublishedOrUnpublished() {
    for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
      listener.onProjectPublishedOrUnpublished();
    }
  }
}
