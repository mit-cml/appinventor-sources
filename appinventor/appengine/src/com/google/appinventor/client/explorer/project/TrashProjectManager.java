// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.project;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.output.OdeLog;
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
public final class TrashProjectManager {
    // Map to find the project from a project ID.
    private final Map<Long, Project> projectsMap;
    private final Map<Long, Project> deletedProjectsMap;

    // List of listeners for any project manager events.
    private final List<ProjectManagerEventListener> projectManagerEventListeners;

    /**
     * Flag indicating whether the project infos have all loaded.
     */
    private volatile boolean projectsLoaded = false;

    /**
     * Creates a new projects manager.
     */
    public TrashProjectManager() {
        projectsMap = new HashMap<Long, Project>();
        deletedProjectsMap = new HashMap<Long, Project>();
        projectManagerEventListeners = new ArrayList<ProjectManagerEventListener>();
        Ode.getInstance().getProjectService().getProjectInfos(
                new OdeAsyncCallback<List<UserProject>>(
                        MESSAGES.projectInformationRetrievalError()) {
                    @Override
                    public void onSuccess(List<UserProject> projectInfos) {
                        for (UserProject projectInfo : projectInfos) {
                            if(projectInfo.getProjectInTrashFlag()){addDeletedProject(projectInfo);}
                        }
                        fireProjectsLoaded();
                    }
                });
    }

    /**
     * Adds a new project to this project manager.
     *
     * @param projectInfo information about the project
     * @return new project
     */
    public Project addProject(UserProject projectInfo) {
        Project project = new Project(projectInfo);
        return project;
    }

    //find the access
    public void addDeletedProject(UserProject projectInfo) {
        Project project= new Project(projectInfo);
        deletedProjectsMap.put(projectInfo.getProjectId(), project);
        fireDeletedProjectAdded(project);
    }

    /**
     * Adds a {@link ProjectManagerEventListener} to the listener list.
     *
     * @param listener  the {@code ProjectManagerEventListener} to be added
     */
    public void addTrashProjectManagerEventListener(ProjectManagerEventListener listener) {
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
    public void removeTrashProjectManagerEventListener(ProjectManagerEventListener listener) {
        projectManagerEventListeners.remove(listener);
    }


    private List<ProjectManagerEventListener> copyProjectManagerEventListeners() {
        return new ArrayList<ProjectManagerEventListener>(projectManagerEventListeners);
    }

    /*
     * Triggers a 'project added' event to be sent to the listener on the listener list.
     */
    private void fireDeletedProjectAdded(Project project) {
        for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
            listener.onDeletedProjectAdded(project);
        }
    }

    /*
     * Triggers a 'projects loaded' event to be sent to the listener on the listener list.
     */
    private void fireProjectsLoaded() {
        projectsLoaded = true;
        for (ProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
            listener.onProjectsLoaded();
        }
    }

}