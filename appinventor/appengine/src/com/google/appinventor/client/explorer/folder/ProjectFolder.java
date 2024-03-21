// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.folder;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.components.Icon;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectSelectionChangeHandler;
import com.google.appinventor.client.explorer.youngandroid.ProjectListItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProjectFolder extends Composite {
  private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat(DATE_TIME_MEDIUM);
  /**
   * This class represents a folder containing project objects.
   */
  private String name;
  private List<Project> projects = new ArrayList<>();
  private final List<ProjectListItem> projectListItems = new ArrayList<>();
  private Map<String, ProjectFolder> folders = new HashMap<>();
  private final long dateCreated;
  protected long dateModified;
  private ProjectFolder parent;
  protected JSONObject cachedJson;
  private ProjectSelectionChangeHandler changeHandler;

  interface ProjectFolderUiBinder extends UiBinder<FlowPanel, ProjectFolder> {
  }

  private static final ProjectFolder.ProjectFolderUiBinder UI_BINDER =
      GWT.create(ProjectFolder.ProjectFolderUiBinder.class);
  private boolean isExpanded = false;


  @UiField
  FlowPanel container;
  @UiField
  FlowPanel childrenContainer;
  @UiField
  Label nameLabel;
  @UiField
  Label dateModifiedLabel;
  @UiField
  Label dateCreatedLabel;
  @UiField
  CheckBox checkBox;
  @UiField
  Icon expandButton;

  public ProjectFolder(String name, long dateCreated, long dateModified, ProjectFolder parent) {
    initWidget(UI_BINDER.createAndBindUi(this));
    this.name = name;
    nameLabel.setText(name);
    this.dateCreated = dateCreated;
    this.dateCreatedLabel.setText(DATE_FORMAT.format(new Date(dateCreated)));
    this.dateModified = dateModified;
    this.dateModifiedLabel.setText(DATE_FORMAT.format(new Date(dateModified)));
    this.parent = parent;
    this.projects = new ArrayList<>();
    this.folders = new HashMap<>();
  }

  public ProjectFolder(String name, long dateCreated, ProjectFolder parent) {
    this(name, dateCreated, dateCreated, parent);
  }

  public ProjectFolder(JSONObject json, ProjectFolder parent) {
    initWidget(UI_BINDER.createAndBindUi(this));
    this.parent = parent;
    name = json.get(FolderJSONKeys.NAME).isString().stringValue();
    nameLabel.setText(name);
    dateCreated = Long.parseLong(json.get(FolderJSONKeys.DATE_CREATED).isString().stringValue());
    dateModified = Long.parseLong(json.get(FolderJSONKeys.DATE_MODIFIED).isString().stringValue());

    this.projects = new ArrayList<>();
    this.folders = new HashMap<>();
    JSONArray projectsJson = json.get(FolderJSONKeys.PROJECTS).isArray();
    for (int i = 0; i < projectsJson.size(); i++) {
      long projectId = Long.parseLong(projectsJson.get(i).isString().stringValue());
      Project project = Ode.getInstance().getProjectManager().getProject(projectId);
      // If users switch back and forth between old and new explorer, the projects
      // may have changed
      if (project != null) {
        addProject(project);
      }
    }

    JSONArray childFoldersJson = json.get(FolderJSONKeys.CHILD_FOLDERS).isArray();
    for (int i = 0; i < childFoldersJson.size(); i++) {
      addChildFolder(new ProjectFolder(childFoldersJson.get(i).isObject(), this));
    }
    cachedJson = null;
  }

  public void setSelectionChangeHandler(ProjectSelectionChangeHandler changeHandler) {
    this.changeHandler = changeHandler;
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("checkBox")
  void toggleFolderSelection(ClickEvent e) {
    setSelected(checkBox.getValue());
    fireSelectionChangeEvent();
  }

  @UiHandler("expandButton")
  void toggleExpandedState(ClickEvent e) {
    setSelected(false);
    isExpanded = !isExpanded;
    if (isExpanded) {
      expandButton.setIcon("expand_more");
      childrenContainer.removeStyleName("ode-ProjectRowHidden");
      checkBox.addStyleName("ode-ProjectElementHidden");
      checkBox.setValue(false);
    } else {
      expandButton.setIcon("chevron_right");
      childrenContainer.addStyleName("ode-ProjectRowHidden");
      checkBox.removeStyleName("ode-ProjectElementHidden");
    }
    fireSelectionChangeEvent();
  }

  public void setSelected(boolean selected) {
    checkBox.setValue(selected);
    if (selected) {
      container.addStyleDependentName("Highlighted");
    } else {
      container.removeStyleDependentName("Highlighted");
    }
  }

  public void addProject(Project project) {
    if (project.getHomeFolder() != null) {
      project.getHomeFolder().removeProject(project);
    }
    project.setHomeFolder(this);
    projects.add(project);
    cachedJson = null;
  }

  public void refresh() {
    nameLabel.setText(name);
    dateCreatedLabel.setText(DATE_FORMAT.format(new Date(dateCreated)));
    dateModifiedLabel.setText(DATE_FORMAT.format(new Date(dateModified)));
    childrenContainer.clear();
    for (ProjectFolder f : folders.values()) {
      if (changeHandler != null) {
        f.setSelectionChangeHandler(changeHandler);
      }
      f.refresh();
      childrenContainer.add(f);
    }
    projectListItems.clear();
    for (Project p : projects) {
      ProjectListItem item = new ProjectListItem(p);
      if (changeHandler != null) {
        item.setSelectionChangeHandler(changeHandler);
      }
      childrenContainer.add(item);
      projectListItems.add(item);
    }
  }

  public void removeProject(Project project) {
    projects.remove(project);
    cachedJson = null;
  }

  public void addChildFolder(ProjectFolder folder) {
    if (folder.parent != null) {
      folder.parent.removeChildFolder(folder);
    }
    if (changeHandler != null) {
      folder.setSelectionChangeHandler(changeHandler);
    }
    folders.put(folder.name, folder);
    folder.parent = this;
    cachedJson = null;
  }

  public void removeChildFolder(ProjectFolder folder) {
    folders.remove(folder.name);
    cachedJson = null;
  }

  public long getDateModified() {
    return dateModified;
  }

  public long getDateCreated() {
    return dateCreated;
  }

  public boolean isSelected() {
    return checkBox.getValue();
  }

  public boolean isExpanded() {
    return isExpanded;
  }

  public void addProjectListItem(ProjectListItem item) {
    projectListItems.add(item);
  }

  public void clearProjectList() {
    projectListItems.clear();
  }

  public List<Project> getSelectedProjects() {
    return getVisibleProjects(true);
  }

  public List<Project> getProjects() {
    return projects;
  }

  public List<Project> getVisibleProjects(boolean onlySelected) {
    List<Project> list = new ArrayList<>();
    for (ProjectListItem item : projectListItems) {
      if (!onlySelected || item.isSelected()) {
        list.add(item.getProject());
      }
    }
    for (ProjectFolder f : folders.values()) {
      if ("*trash*".equals(f.getName())) {
        continue;
      }
      if (f.isExpanded()) {
        list.addAll(f.getVisibleProjects(onlySelected));
      }
    }
    return list;
  }

  public boolean containsAnyProjects() {
    if (projectListItems.size() > 0) {
      return true;
    } else if (hasChildFolders()) {
      for (ProjectFolder f : folders.values()) {
        if (f.containsAnyProjects()) {
          return true;
        }
      }
    }
    return false;
  }

  public List<ProjectFolder> getSelectedFolders() {
    return getSelectableFolders(true);
  }

  public List<ProjectFolder> getSelectableFolders(boolean onlySelected) {
    List<ProjectFolder> list = new ArrayList<>();
    for (ProjectFolder f : folders.values()) {
      if ("*trash*".equals(f.getName())) {
        continue;
      }
      if (f.isExpanded) {
        list.addAll(f.getSelectableFolders(onlySelected));
      } else if (!onlySelected || f.isSelected()) {
        list.add(f);
      }
    }
    return list;
  }

  public void selectAll(boolean selected) {
    for (ProjectListItem item : projectListItems) {
      item.setSelected(selected);
    }
    for (ProjectFolder f : folders.values()) {
      if ("*trash*".equals(f.getName())) {
        continue;
      }
      if (f.isExpanded) {
        f.selectAll(selected);
      } else  {
        f.setSelected(selected);
      }
    }
  }

  public void setName(String name) {
    this.name = name;
    dateModified = System.currentTimeMillis();
    cachedJson = null;
  }

  public String getName() {
    return name;
  }

  public List<Project> getVisibleProjects() {
    return getVisibleProjects(false);
  }

  public List<Project> getNestedProjects() {
    List<Project> plist = new ArrayList<>();
    plist.addAll(projects);
    for (ProjectFolder child : getChildFolders()) {
      plist.addAll(child.getNestedProjects());
    }
    return plist;
  }

  public List<ProjectFolder> getChildFolders() {
    return Arrays.asList(folders.values().toArray(new ProjectFolder[0]));
  }

  public boolean hasChildFolders() {
    return folders.size() > 0;
  }

  public ProjectFolder getChildFolder(String name) {
    return folders.get(name);
  }

  public ProjectFolder getParentFolder() {
    return parent;
  }

  public JSONObject toJSON() {
    if (cachedJson != null) {
      return cachedJson;
    }
    cachedJson = makeJSON();
    return cachedJson;
  }

  public void clearCache() {
    cachedJson = null;
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
    for (ProjectFolder folder : folders.values()) {
      foldersJSON.set(index++, folder.toJSON());
    }
    json.put(FolderJSONKeys.CHILD_FOLDERS, foldersJSON);
    return json;
  }

  private void fireSelectionChangeEvent() {
    if (changeHandler != null) {
      changeHandler.onSelectionChange(checkBox.getValue());
    }
  }
}
