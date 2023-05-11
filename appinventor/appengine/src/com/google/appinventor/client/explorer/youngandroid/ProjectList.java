// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.folder.FolderManagerEventListener;
import com.google.appinventor.client.explorer.folder.ProjectsFolderListItem;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.appinventor.client.explorer.folder.ProjectsFolder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;


/**
 * The project list shows all projects in a table.
 *
 * <p> The project name, date created, and date modified will be shown in the table.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProjectList extends ProjectsFolder implements FolderManagerEventListener,
    ProjectManagerEventListener {
  private static final Logger LOG = Logger.getLogger(ProjectList.class.getName());
  interface ProjectListUiBinder extends UiBinder<FlowPanel, ProjectList> {}
  private static final ProjectListUiBinder UI_BINDER = GWT.create(ProjectListUiBinder.class);
  private enum SortField {
    NAME,
    DATE_CREATED,
    DATE_MODIFIED,
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }
  private List<Project> projects;
  private final List<Project> selectedProjects;
  private final List<ProjectListItem> selectedProjectListItems = new ArrayList<>();
  private final List<ProjectListItem> projectListItems = new ArrayList<>();
  private SortField sortField;
  private SortOrder sortOrder;

  private boolean projectListLoading = true;

  // UI elements
//  private final Grid table;
  @UiField
  CheckBox selectAllCheckBox;
  @UiField
  FlowPanel container;
  boolean projectsLoaded = false;
  @UiField
  InlineLabel projectNameSortDec;
  @UiField
  InlineLabel projectNameSortAsc;
  @UiField
  InlineLabel createDateSortDec;
  @UiField
  InlineLabel createDateSortAsc;
  @UiField
  InlineLabel modDateSortDec;
  @UiField
  InlineLabel modDateSortAsc;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {
    projects = new ArrayList<>();
    selectedProjects = new ArrayList<Project>();

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;

    initWidget(UI_BINDER.createAndBindUi(this));
    refreshSortIndicators();
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    setDepth(0);
  }

  @SuppressWarnings("unused")
  @UiHandler("projectName")
  public void sortByNameField(ClickEvent e) {
    changeSortOrder(SortField.NAME);
  }

  @UiHandler("createDate")
  public void sortByCreateDate(ClickEvent e) {
    changeSortOrder(SortField.DATE_CREATED);
  }

  @UiHandler("modDate")
  public void sortByModDate(ClickEvent e) {
    changeSortOrder(SortField.DATE_MODIFIED);
  }

  private void changeSortOrder(SortField clickedSortField) {
    if (sortField != clickedSortField) {
      sortField = clickedSortField;
      sortOrder = SortOrder.ASCENDING;
    } else {
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
    }
    refresh(true);
  }

  private void refreshSortIndicators() {
    projectNameSortDec.setVisible(false);
    projectNameSortAsc.setVisible(false);
    createDateSortDec.setVisible(false);
    createDateSortAsc.setVisible(false);
    modDateSortDec.setVisible(false);
    modDateSortAsc.setVisible(false);

    switch (sortField) {
      case NAME:
        if (sortOrder == SortOrder.ASCENDING) {
          projectNameSortAsc.setVisible(true);
        } else {
          projectNameSortDec.setVisible(true);
        }
        break;
      case DATE_CREATED:
        if (sortOrder == SortOrder.ASCENDING) {
          createDateSortAsc.setVisible(true);
        } else {
          createDateSortDec.setVisible(true);
        }
        break;
      case DATE_MODIFIED:
        if (sortOrder == SortOrder.ASCENDING) {
          modDateSortAsc.setVisible(true);
        } else {
          modDateSortDec.setVisible(true);
        }
        break;
    }
  }

  @Override
  public void refresh() {
    refresh(false);
  }

  // TODO(user): This method was made public so it can be called
  // directly from from Ode when the Project List View is selected
  // from another view.  Ode now clears any selected projects and
  // calls this to refresh the table as a result. Not sure this is
  // correct thing do to. The alternative is to add a call to the
  // ProjectManagerEventListener interface that this is the
  // implementation of.

  public void refresh(boolean needToSort) {
    projects = folder.getProjects();
    List<Folder> folders = folder.getChildFolders();
//    if (needToSort) {
      // Sort the projects.
      Comparator<Project> comparator;
      Comparator<Folder> folderComparator;
      folderComparator = ProjectComparators.COMPARE_BY_FOLDER_NAME_ASCENDING;
      switch (sortField) {
        default:
        case NAME:
          if (sortOrder == SortOrder.ASCENDING) {
            comparator = ProjectComparators.COMPARE_BY_NAME_ASCENDING;
          } else {
            comparator = ProjectComparators.COMPARE_BY_NAME_DESCENDING;
            folderComparator = ProjectComparators.COMPARE_BY_FOLDER_NAME_DESCENDING;
          }
          break;
        case DATE_CREATED:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_DATE_CREATED_ASCENDING
              : ProjectComparators.COMPARE_BY_DATE_CREATED_DESCENDING;
          break;
        case DATE_MODIFIED:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_DATE_MODIFIED_ASCENDING
              : ProjectComparators.COMPARE_BY_DATE_MODIFIED_DESCENDING;
          break;
      }
      Collections.sort(projects, comparator);
      Collections.sort(folders, folderComparator);
//    }

    refreshSortIndicators();

    container.clear();
    selectedProjectListItems.clear();
    projectListItems.clear();
    projectsFolderListItems.clear();
    for (final Folder childFolder : folders) {
      if ("*trash*".equals(childFolder.getName())) {
        continue;
      }
      ProjectsFolderListItem item = createProjectsFolderListItem(childFolder, container);
      projectsFolderListItems.add(item);
    }
    for(final Project project : projects) {
      ProjectListItem item = createProjectListItem(project, container);
      projectListItems.add(item);
    }
    selectAllCheckBox.setValue(false);
    Ode.getInstance().getBindProjectToolbar().updateButtons();
  }

  public void setSelected(boolean selected) {
    selectedProjectListItems.clear();
    for(ProjectListItem item : projectListItems) {
      item.setSelected(selected);
      if(selected) {
        selectedProjectListItems.add(item);
      }
    }
    for(ProjectsFolderListItem item : projectsFolderListItems) {
      item.setSelected(selected);
    }
  }

  public boolean isSelected() {
    return selectAllCheckBox.getValue();
  }

  protected void fireSelectionChangeEvent() {
    int selectableFolders = getSelectableFolders().size();
    int visibleProjects = getVisibleProjects().size();
    if (selectableFolders + visibleProjects > 0 &&
            selectableFolders == getSelectedFolders().size() &&
            visibleProjects == getSelectedProjects().size()) {
      selectAllCheckBox.setValue(true);
    } else {
      selectAllCheckBox.setValue(false);
    }
  }

  @UiHandler("selectAllCheckBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(selectAllCheckBox.getValue());
  }
  /**
   * Gets the number of selected projects
   *
   * @return the number of selected projects
   */
  public int getSelectedProjectsCount() {
    return selectedProjectListItems.size();
  }

  public int getMyProjectsCount() {
    int count = 0;
    for (Project project : projects) {
      if (!project.isInTrash()) {
        ++ count;
      };
    }
    return count;
  }

  /**
   * Returns if the specified project is in Trash or in MyProjects
   */
  public int getProjectCurrentView(Project project) {
    if (project.isInTrash()) {
      return Ode.TRASHCAN;
    } else {
      return Ode.PROJECTS;
    }
  }

  /**
   * Returns true if all projects under the current view have been selected, and false if not.
   */
  public boolean isAllProjectsSelected() {
    if (Ode.getInstance().getCurrentView() == Ode.PROJECTS
          && getSelectedProjectsCount() == getMyProjectsCount()) {
      return true;
    }
    if (Ode.getInstance().getCurrentView() == Ode.TRASHCAN
          && getSelectedProjectsCount() == projects.size() - getMyProjectsCount()) {
      return true;
    }
    return false;
  }
  /**
   * Returns the list of selected projects.
   *
   * @return the selected projects
   */
  public void setIsTrash(boolean isTrash) {
    this.isTrash = isTrash;
    if (isTrash) {
      setFolder(Ode.getInstance().getFolderManager().getTrashFolder());
    } else {
      setFolder(Ode.getInstance().getFolderManager().getGlobalFolder());
    }
    if (folder != null) {
      refresh();
    }
  }
  // FolderManagerEventListener implementation
  @Override
  public void onFolderAdded(Folder folder) {
    refresh();
  }

  @Override
  public void onFolderRemoved(Folder folder) {
    refresh();
  }

  @Override
  public void onFolderRenamed(Folder folder) {
    refresh();
  }

  @Override
  public void onFoldersChanged() {
    refresh();
  }

  @Override
  public void onFoldersLoaded() {
    setIsTrash(isTrash);
  }

  @Override
  public void onProjectAdded(Project project) {
    if (folder == null) {
    } else {
      LOG.info(folder.getName());
    }

    if (projectsLoaded) {
      folder.addProject(project);
      Ode.getInstance().getFolderManager().saveAllFolders();
      refresh();
    }
  }

  @Override
  public void onTrashProjectRestored(Project project) {
    Ode.getInstance().getFolderManager().getGlobalFolder().addProject(project);
    Ode.getInstance().getFolderManager().getTrashFolder().removeProject(project);
    Ode.getInstance().getFolderManager().saveAllFolders();
    refresh();
  }

  @Override
  public void onProjectTrashed(Project project) {
    folder.removeProject(project);
    Ode.getInstance().getFolderManager().getTrashFolder().addProject(project);
    Ode.getInstance().getFolderManager().saveAllFolders();
    refresh();
  }

  public void onProjectMoved(Project project) {
    refresh();
  }

  @Override
  public void onProjectDeleted(Project project) {
    folder.removeProject(project);
    Ode.getInstance().getFolderManager().saveAllFolders();
    refresh();
  }

  @Override
  public void onProjectsLoaded() {
    projectsLoaded = true;
    projectListLoading = false;
    refresh();
  }

  private static native void configureDraggable(Element el)/*-{
    if (el.getAttribute('draggable') != 'true') {
      el.setAttribute('draggable', 'true');
      el.addEventListener('dragstart', function(e) {
        e.dataTransfer.setData('DownloadURL', this.dataset.exporturl);
      });
    }
  }-*/;
}
