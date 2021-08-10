// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.folder;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;


import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a folder.
 *
 */
public final class Folder {
  private String name;
  private List<Project> projects;
  private Map<String, Folder> folders;
  private long dateCreated;
  protected long dateModified;
  private Folder parent;
  protected JSONObject cachedJSON;

  public Folder(String name, long dateCreated, long dateModified, Folder parent) {
    this.name = name;
    this.dateCreated = dateCreated;
    this.dateModified = dateModified;
    this.parent = parent;
    this.projects = new ArrayList<Project>();
    this.folders = new HashMap<String, Folder>();
  }

  public Folder(String name, long dateCreated, Folder parent) {
    this(name, dateCreated, dateCreated, parent);
  }

  public Folder(JSONObject json, Folder parent) {
    this.parent = parent;
    name = json.get(FolderJSONKeys.NAME).isString().stringValue();
    dateCreated = Long.parseLong(json.get(FolderJSONKeys.DATE_CREATED).isString().stringValue());
    dateModified = Long.parseLong(json.get(FolderJSONKeys.DATE_MODIFIED).isString().stringValue());

    this.projects = new ArrayList<Project>();
    this.folders = new HashMap<String, Folder>();
    JSONArray projectsJSON = json.get(FolderJSONKeys.PROJECTS).isArray();
    for (int i = 0; i < projectsJSON.size(); i++) {
      long projectId = Long.parseLong(projectsJSON.get(i).isString().stringValue());
      addProject(Ode.getInstance().getProjectManager().getProject(projectId));
    }

    JSONArray childFoldersJSON = json.get(FolderJSONKeys.CHILD_FOLDERS).isArray();
    for (int i = 0; i < childFoldersJSON.size(); i++) {
      addChildFolder(new Folder(childFoldersJSON.get(i).isObject(), this));
    }
    cachedJSON = json;
  }

  public void addProject(Project project) {
    projects.add(project);
    cachedJSON = null;
  }

  public void removeProject(Project project) {
    projects.remove(project);
    cachedJSON = null;
  }

  public void addChildFolder(Folder folder) {
    if (folder.parent != null) {
      folder.parent.removeChildFolder(folder);
    }
    folders.put(folder.name, folder);
    folder.parent = this;
    cachedJSON = null;
  }

  public void removeChildFolder(Folder folder) {
    folders.remove(folder.name);
    cachedJSON = null;
  }

  public long getDateModified() {
    return dateModified;
  }

  public long getDateCreated() {
    return dateCreated;
  }

  public void setName(String name) {
    this.name = name;
    dateModified = System.currentTimeMillis();
    cachedJSON = null;
  }

  public String getName() {
    return name;
  }

  public List<Project> getProjects() {
    return projects;
  }

  public List<Folder> getChildFolders() {
    return Arrays.asList(folders.values().toArray(new Folder[0]));
  }

  public Folder getChildFolder(String name) {
    return folders.get(name);
  }

  public Folder getParentFolder() {
    return parent;
  }

  public JSONObject toJSON() {
    if (cachedJSON != null) {
      return cachedJSON;
    }
    cachedJSON = makeJSON();
    return cachedJSON;
  }

  public void clearCache() {
    cachedJSON = null;
  }

  protected JSONObject makeJSON() {
    JSONObject json = new JSONObject();
    json.put(FolderJSONKeys.NAME, new JSONString(name));
    json.put(FolderJSONKeys.DATE_CREATED, new JSONString(Long.toString(dateCreated)));
    json.put(FolderJSONKeys.DATE_MODIFIED, new JSONString(Long.toString(dateModified)));

    JSONArray projectsJSON = new JSONArray();
    int index = 0;
    for (Project project : projects) {
      projectsJSON.set(index++, new JSONString(Long.toString(project.getProjectId())));
    }
    json.put(FolderJSONKeys.PROJECTS, projectsJSON);

    JSONArray foldersJSON = new JSONArray();
    index = 0;
    for (Folder folder : folders.values()) {
      foldersJSON.set(index++, folder.toJSON());
    }
    json.put(FolderJSONKeys.CHILD_FOLDERS, foldersJSON);
    return json;
  }
}
