// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.folder.FolderManagerEventListener;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;

import com.google.appinventor.client.explorer.project.ProjectSelectionChangeHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;


/**
 * The project list shows all projects in a table.
 *
 * <p>The project name, date created, and date modified will be shown in the table.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProjectList extends Composite implements FolderManagerEventListener,
    ProjectManagerEventListener {
  interface ProjectListUiBinder extends UiBinder<FlowPanel, ProjectList> {}

  private static final Logger LOG = Logger.getLogger(ProjectList.class.getName());
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

  private SortField sortField;
  private SortOrder sortOrder;

  private ProjectFolder folder;
  private boolean isTrash;
  private boolean projectsLoaded = false;

  // UI elements
  @UiField
  CheckBox selectAllCheckBox;
  @UiField FlowPanel container;
  @UiField InlineLabel projectNameSortDec;
  @UiField InlineLabel projectNameSortAsc;
  @UiField InlineLabel createDateSortDec;
  @UiField InlineLabel createDateSortAsc;
  @UiField InlineLabel modDateSortDec;
  @UiField InlineLabel modDateSortAsc;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;

    initWidget(UI_BINDER.createAndBindUi(this));
    refreshSortIndicators();
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    setIsTrash(false);
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
    LOG.info("Refresh ProjectList");
    List<Project> projects = folder.getProjects();
    List<ProjectFolder> folders = folder.getChildFolders();
    if (needToSort) {
      // Sort the projects.
      Comparator<Project> comparator;
      Comparator<ProjectFolder> folderComparator;
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
    }

    refreshSortIndicators();

    container.clear();
    ProjectSelectionChangeHandler selectionEvent = new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        fireSelectionChangeEvent();
      }
    };

    for (final ProjectFolder childFolder : folders) {
      if ("*trash*".equals(childFolder.getName())) {
        continue;
      }
      childFolder.setSelectionChangeHandler(selectionEvent);
      childFolder.refresh();
      container.add(childFolder);
    }
    folder.clearProjectList();
    for (final Project project : projects) {
      ProjectListItem item = new ProjectListItem(project);
      item.setSelectionChangeHandler(selectionEvent);
      folder.addProjectListItem(item);
      container.add(item);
    }
    selectAllCheckBox.setValue(false);
    Ode.getInstance().getProjectToolbar().updateButtons();
    if (isTrash && folder.getProjects().isEmpty()) {
      Ode.getInstance().createEmptyTrashDialog(true);
    }
  }

  public boolean isSelected() {
    return selectAllCheckBox.getValue();
  }

  public void fireSelectionChangeEvent() {
    int selectableFolders = folder.getSelectableFolders(false).size();
    int visibleProjects = folder.getVisibleProjects(false).size();
    int selectedFolders = folder.getSelectedFolders().size();
    int selectedProjects = folder.getSelectedProjects().size();

    LOG.info("Checking SelectAll checkbox: SelectableFolders=" + selectableFolders
        + " visibleProjects=" + visibleProjects + " " + "SelectedFolders=" + selectedFolders
        + " SelectedProjects=" + selectedProjects);

    if (selectableFolders + visibleProjects > 0
        && selectableFolders == selectedFolders
        && visibleProjects == selectedProjects) {
      selectAllCheckBox.setValue(true);
    } else {
      selectAllCheckBox.setValue(false);
    }
    Ode.getInstance().getProjectToolbar().updateButtons();
  }

  public List<Project> getSelectedProjects() {
    return folder.getSelectedProjects();
  }

  public List<ProjectFolder> getSelectedFolders() {
    return folder.getSelectedFolders();
  }

  @UiHandler("selectAllCheckBox")
  void toggleAllItemSelection(ClickEvent e) {
    folder.selectAll(selectAllCheckBox.getValue());
    fireSelectionChangeEvent();
  }

  /**
   * Gets the number of selected projects.
   *
   * @return the number of selected projects
   */
  public int getSelectedProjectsCount() {
    if (folder != null) {
      return folder.getSelectedProjects().size() + folder.getSelectedFolders().size();
    } else {
      return 0;
    }
  }

  public boolean listContainsProjects() {
    return folder.containsAnyProjects();
  }

  public int getMyProjectsCount() {
    int count = 0;
    if (folder == null) {
      return 0;
    }
    for (Project project : folder.getVisibleProjects()) {
      if (!project.isInTrash()) {
        ++ count;
      };
    }
    return count;
  }

  public void setIsTrash(boolean isTrash) {
    this.isTrash = isTrash;
    if (isTrash) {
      folder = Ode.getInstance().getFolderManager().getTrashFolder();
    } else {
      folder = Ode.getInstance().getFolderManager().getGlobalFolder();
    }
    if (folder != null) {
      refresh();
    }
  }

  // FolderManagerEventListener implementation
  @Override
  public void onFolderAdded(ProjectFolder folder) {
    refresh(true);
  }

  @Override
  public void onFolderRemoved(ProjectFolder folder) {
    refresh();
  }

  @Override
  public void onFolderRenamed(ProjectFolder folder) {
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
    if (projectsLoaded) {
      folder.addProject(project);
      Ode.getInstance().getFolderManager().saveAllFolders();
      refresh(true);
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
    refresh(true);
  }
}
