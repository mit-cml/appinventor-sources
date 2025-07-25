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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
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

  private Comparator<Project> projectComparator = ProjectComparators.COMPARE_BY_DATE_MODIFIED_DESCENDING;
  private Comparator<ProjectFolder> folderComparator = ProjectComparators.COMPARE_BY_FOLDER_DATE_MODIFIED_DESCENDING;

  // UI elements
  @UiField protected CheckBox selectAllCheckBox;
  @UiField protected FlowPanel container;
  @UiField protected InlineLabel projectNameSortDec;
  @UiField protected InlineLabel projectNameSortAsc;
  @UiField protected InlineLabel createDateSortDec;
  @UiField protected InlineLabel createDateSortAsc;
  @UiField protected InlineLabel modDateSortDec;
  @UiField protected InlineLabel modDateSortAsc;
  @UiField protected FocusPanel nameFocusPanel;
  @UiField protected FocusPanel createdateFocusPanel;
  @UiField protected FocusPanel modDateFocusPanel;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;

    bindIU();
    setIsTrash(false);
    refreshSortIndicators();
  }

  public void bindIU() {
    ProjectListUiBinder uibinder = GWT.create(ProjectListUiBinder.class);
    initWidget(uibinder.createAndBindUi(this));
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
  }

  @SuppressWarnings("unused")
  @UiHandler("nameFocusPanel")
  public void sortByNameField(ClickEvent e) {
    changeSortOrder(SortField.NAME);
  }

  @SuppressWarnings("unused")
  @UiHandler("nameFocusPanel")
  public void sortByNameField(KeyDownEvent e) {
    if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      changeSortOrder(SortField.NAME);
    }
  }

  @UiHandler("createdateFocusPanel")
  public void sortByCreateDate(ClickEvent e) {
    changeSortOrder(SortField.DATE_CREATED);
  }

  @UiHandler("createdateFocusPanel")
  public void sortByCreateDate(KeyDownEvent e) {
    if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      changeSortOrder(SortField.DATE_CREATED);
    }
  }

  @UiHandler("modDateFocusPanel")
  public void sortByModDate(ClickEvent e) {
    changeSortOrder(SortField.DATE_MODIFIED);
  }

  @UiHandler("modDateFocusPanel")
  public void sortByModDate(KeyDownEvent e) {
    if (e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      changeSortOrder(SortField.DATE_MODIFIED);
    }
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
    switch (sortField) {
      default:
      case NAME:
        if (sortOrder == SortOrder.ASCENDING) {
          projectComparator = ProjectComparators.COMPARE_BY_NAME_ASCENDING;
          folderComparator = ProjectComparators.COMPARE_BY_FOLDER_NAME_ASCENDING;
        } else {
          projectComparator = ProjectComparators.COMPARE_BY_NAME_DESCENDING;
          folderComparator = ProjectComparators.COMPARE_BY_FOLDER_NAME_DESCENDING;
        }
        break;
      case DATE_CREATED:
        if (sortOrder == SortOrder.ASCENDING) {
          projectComparator = ProjectComparators.COMPARE_BY_DATE_CREATED_ASCENDING;
          folderComparator = ProjectComparators.COMPARE_BY_FOLDER_DATE_MODIFIED_ASCENDING;
        } else {
          projectComparator = ProjectComparators.COMPARE_BY_DATE_CREATED_DESCENDING;
          folderComparator = ProjectComparators.COMPARE_BY_FOLDER_DATE_CREATED_DESCENDING;
        }
        break;
      case DATE_MODIFIED:
        if (sortOrder == SortOrder.ASCENDING) {
          projectComparator = ProjectComparators.COMPARE_BY_DATE_MODIFIED_ASCENDING;
          folderComparator = ProjectComparators.COMPARE_BY_FOLDER_DATE_MODIFIED_ASCENDING;
        } else {
          projectComparator = ProjectComparators.COMPARE_BY_DATE_MODIFIED_DESCENDING;
          folderComparator = ProjectComparators.COMPARE_BY_FOLDER_DATE_MODIFIED_DESCENDING;
        }
        break;
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
    refreshSortIndicators();

    container.clear();
    ProjectSelectionChangeHandler selectionEvent = new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        fireSelectionChangeEvent();
      }
    };
    selectAllCheckBox.setValue(false);
    List<ProjectFolder> sortedFolders = folder.getChildFolders();
    sortedFolders.sort(folderComparator);
    for (final ProjectFolder childFolder : sortedFolders) {
      if ("*trash*".equals(childFolder.getName())) {
        continue;
      }
      childFolder.setSelectionChangeHandler(selectionEvent);
      childFolder.refresh(projectComparator, folderComparator, needToSort);
      container.add(childFolder);
    }
    folder.clearProjectList();
    projects.sort(projectComparator);
    for (final Project project : projects) {
      ProjectListItem item = createProjectListItem(project);
      item.setSelectionChangeHandler(selectionEvent);
      folder.addProjectListItem(item);
      container.add(item);
    }

    Ode.getInstance().getProjectToolbar().updateButtons();
    if (isTrash && folder.getProjects().isEmpty() && folder.getChildFolders().isEmpty()) {
      Ode.getInstance().createEmptyTrashDialog(true);
    }
  }

  public ProjectListItem createProjectListItem(Project p) {
   return new ProjectListItem(p);
  }

  public boolean isSelected() {
    return selectAllCheckBox.getValue();
  }

  public void fireSelectionChangeEvent() {
    int selectableFolders = folder.getSelectableFolders(false).size();
    int visibleProjects = folder.getVisibleProjects(false).size();
    int selectedFolders = folder.getSelectedFolders().size();
    int selectedProjects = folder.getSelectedProjects().size();

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
  protected void toggleAllItemSelection(ClickEvent e) {
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
      refresh(true);
    }
  }


  // FolderManagerEventListener implementation
  @Override
  public void onFolderAdded(ProjectFolder folder) {
    refresh(true);
  }

  @Override
  public void onFolderRemoved(ProjectFolder folder) {
    refresh(false);
  }

  @Override
  public void onFolderRenamed(ProjectFolder folder) {
    refresh(false);
  }

  @Override
  public void onFoldersChanged() {
    refresh(false);
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
  }

  @Override
  public void onProjectTrashed(Project project) {

  }

  @Override
  public void onProjectDeleted(Project project) {

  }

  @Override
  public void onProjectsLoaded() {
    projectsLoaded = true;
    refresh(true);
  }
}
