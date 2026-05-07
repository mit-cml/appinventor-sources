// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.folder;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.settings.SettingsConstants;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class manages folders.
 *
 */
public final class FolderManager {
  private static final Logger LOG = Logger.getLogger(FolderManager.class.getName());

  private ProjectFolder globalFolder;
  private ProjectFolder trashFolder;

  private boolean foldersLoaded;

  private final List<FolderManagerEventListener> folderManagerEventListeners;
  private final UiStyleFactory uiFactory;

  public FolderManager(UiStyleFactory uiFactory) {
    this.uiFactory = uiFactory;
    folderManagerEventListeners = new ArrayList<>();
    LOG.info("Created new folder manager");
  }

  public void loadFolders() {
    String foldersAsString = Ode.getUserSettings()
        .getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .getPropertyValue(SettingsConstants.FOLDERS);
    foldersLoaded = true;

    if (foldersAsString.isEmpty()) {
      LOG.info("Initialize folders");
      initializeFolders();
      fireFoldersLoaded();
      return;
    }

    JSONObject folderJSON = JSONParser.parse(foldersAsString).isObject();
    if (folderJSON.get(FolderJSONKeys.PROJECTS).isArray().size() == 0
        && folderJSON.get(FolderJSONKeys.CHILD_FOLDERS).isArray().size() == 0) {
      LOG.info("Global folder is empty");
      initializeFolders();
      fireFoldersLoaded();
      return;
    }

    LOG.info("folderJSON - " + folderJSON);
    globalFolder = uiFactory.createProjectFolder(folderJSON, null);
    LOG.info("Creating Trash Folder");
    trashFolder = globalFolder.getChildFolder(FolderJSONKeys.TRASH_FOLDER);
    checkForUnassignedProjects();
    fireFoldersLoaded();
  }

  public void saveAllFolders() {
    LOG.info("Saved Folder JSON: " + globalFolder.toJSON().toString());

    Ode.getUserSettings()
        .getSettings(SettingsConstants.USER_GENERAL_SETTINGS)
        .changePropertyValue(SettingsConstants.FOLDERS, globalFolder.toJSON().toString());
    Ode.getUserSettings().saveSettings(null);
  }

  public ProjectFolder createFolder(String name, ProjectFolder parent) {
    ProjectFolder folder = uiFactory.createProjectFolder(name, System.currentTimeMillis(),
        System.currentTimeMillis(), parent);
    parent.addChildFolder(folder);
    while ((parent = parent.getParentFolder()) != null) {
      parent.clearCache();
    }
    saveAllFolders();
    fireFolderAdded(folder);
    return folder;
  }

  public void renameFolders(List<String> folderNames, List<ProjectFolder> foldersToRename) {
    for (int i = 0; i < foldersToRename.size(); ++i) {
      foldersToRename.get(i).setName(folderNames.get(i));
    }
    saveAllFolders();
  }

  public void moveItemsToFolder(List<Project> projects, List<ProjectFolder> folders,
      ProjectFolder destination) {
    LOG.info("Moving projects count " + projects.size() + " to " + destination.getName());
    for (Project project : projects) {
      LOG.info("Moving project " + project.getProjectName()  + " from "
          + project.getHomeFolder().getName() + " to " + destination.getName());
      destination.addProject(project);
    }
    for (ProjectFolder folder : folders) {
      LOG.info("Moving folder " + folder.getName()  + " from " + folder.getParentFolder().getName()
          + " to " + destination.getName());
      destination.addChildFolder(folder);
    }
    saveAllFolders();
    fireFoldersChanged();
  }

  public ProjectFolder getGlobalFolder() {
    return globalFolder;
  }

  public ProjectFolder getTrashFolder() {
    return trashFolder;
  }

  private void initializeFolders() {
    LOG.info("Initializing folders for new user");
    globalFolder = uiFactory.createProjectFolder(FolderJSONKeys.GLOBAL_FOLDER,
        System.currentTimeMillis(), null);
    trashFolder = uiFactory.createProjectFolder(FolderJSONKeys.TRASH_FOLDER,
        System.currentTimeMillis(), globalFolder);
    globalFolder.addChildFolder(trashFolder);

    for (Project project : Ode.getInstance().getProjectManager().getProjects("")) {
      if(project.isInTrash()) {
        trashFolder.addProject(project);
      } else {
        globalFolder.addProject(project);
      }
    }
    saveAllFolders();
  }

  // If users are switching back and forth between old and new view, they may have created
  // projects with the old view. Find those and assign to global root folder.
  private void checkForUnassignedProjects() {
    for (Project project : Ode.getInstance().getProjectManager().getProjectsWithoutFolder()) {
      if (project.isInTrash()) {
        trashFolder.addProject(project);
      } else {
        globalFolder.addProject(project);
      }
    }
  }


  public void addFolderManagerEventListener(FolderManagerEventListener listener) {
    folderManagerEventListeners.add(listener);
    if(foldersLoaded) {
      listener.onFoldersLoaded();
    }
  }

  public void removeFolderManagerEventListener(FolderManagerEventListener listener) {
    folderManagerEventListeners.remove(listener);
  }

  private List<FolderManagerEventListener> copyFolderManagerEventListeners() {
    return new ArrayList<FolderManagerEventListener>(folderManagerEventListeners);
  }

  private void fireFolderRenamed(ProjectFolder folder) {
    for (FolderManagerEventListener listener : copyFolderManagerEventListeners()) {
      listener.onFolderRenamed(folder);
    }
  }

  private void fireFolderAdded(ProjectFolder folder) {
    for (FolderManagerEventListener listener : copyFolderManagerEventListeners()) {
      listener.onFolderAdded(folder);
    }
  }

  private void fireFoldersChanged() {
    for (FolderManagerEventListener listener : copyFolderManagerEventListeners()) {
      listener.onFoldersChanged();
    }
  }

  private void fireFoldersLoaded() {
    for (FolderManagerEventListener listener : copyFolderManagerEventListeners()) {
      listener.onFoldersLoaded();
    }
  }
}
