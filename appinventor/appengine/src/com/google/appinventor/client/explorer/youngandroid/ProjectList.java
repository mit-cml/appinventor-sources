// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.folder.FolderManagerEventListener;
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
  private final List<Project> projects;
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
//  private final Label nameSortIndicator;
//  private final Label dateCreatedSortIndicator;
//  private final Label dateModifiedSortIndicator;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {
    LOG.info("Creating Project-legacy List");
    projects = new ArrayList<Project>();
    selectedProjects = new ArrayList<Project>();

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;


//    nameSortIndicator = new Label("");
//    dateCreatedSortIndicator = new Label("");
//    dateModifiedSortIndicator = new Label("");
//    refreshSortIndicators();
    initWidget(UI_BINDER.createAndBindUi(this));
    Ode.getInstance().getFolderManager().addFolderManagerEventListener(this);
    LOG.info("Added legacy project manager event listener");

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    setDepth(0);
  }

  /**
   * Adds the header row to the table.
   *
   */
//  private void setHeaderRow() {
//    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");
//
//    selectAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
//      @Override
//      public void onValueChange(ValueChangeEvent<Boolean> event) {
//        boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
//        for (Map.Entry<Project, ProjectWidgets> projectWidget : projectWidgets.entrySet()) {
//          if (getProjectCurrentView(projectWidget.getKey()) != Ode.getInstance().getCurrentView()) {
//            continue;
//          }
//          int row = Integer.valueOf(projectWidget.getValue().checkBox.getName());
//          if (isChecked) {
//            if (selectedProjects.contains(projectWidget.getKey())) {
//              continue;
//            }
//            table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
//            selectedProjects.add(projectWidget.getKey());
//            projectWidget.getValue().checkBox.setValue(true);
//          } else {
//            table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
//            selectedProjects.remove(projectWidget.getKey());
//            projectWidget.getValue().checkBox.setValue(false);
//          }
//        }
//        Ode.getInstance().getBindProjectToolbar().updateButtons();
//      }
//    });
//    table.setWidget(0, 0, selectAllCheckBox);
//
//    HorizontalPanel nameHeader = new HorizontalPanel();
//    final Label nameHeaderLabel = new Label(MESSAGES.projectNameHeader());
//    nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
//    nameHeader.add(nameHeaderLabel);
//    nameSortIndicator.addStyleName("ode-ProjectHeaderLabel");
//    nameHeader.add(nameSortIndicator);
//    table.setWidget(0, 1, nameHeader);
//
//    HorizontalPanel dateCreatedHeader = new HorizontalPanel();
//    final Label dateCreatedHeaderLabel = new Label(MESSAGES.projectDateCreatedHeader());
//    dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
//    dateCreatedHeader.add(dateCreatedHeaderLabel);
//    dateCreatedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
//    dateCreatedHeader.add(dateCreatedSortIndicator);
//    table.setWidget(0, 2, dateCreatedHeader);
//
//    HorizontalPanel dateModifiedHeader = new HorizontalPanel();
//    final Label dateModifiedHeaderLabel = new Label(MESSAGES.projectDateModifiedHeader());
//    dateModifiedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
//    dateModifiedHeader.add(dateModifiedHeaderLabel);
//    dateModifiedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
//    dateModifiedHeader.add(dateModifiedSortIndicator);
//    table.setWidget(0, 3, dateModifiedHeader);
//
//    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
//      @Override
//      public void onMouseDown(MouseDownEvent e) {
//        SortField clickedSortField;
//        if (e.getSource() == nameHeaderLabel || e.getSource() == nameSortIndicator) {
//          clickedSortField = SortField.NAME;
//        } else if (e.getSource() == dateCreatedHeaderLabel || e.getSource() == dateCreatedSortIndicator) {
//          clickedSortField = SortField.DATE_CREATED;
//        } else {
//          clickedSortField = SortField.DATE_MODIFIED;
//        }
//        changeSortOrder(clickedSortField);
//      }
//    };
//    nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
//    nameSortIndicator.addMouseDownHandler(mouseDownHandler);
//    dateCreatedHeaderLabel.addMouseDownHandler(mouseDownHandler);
//    dateCreatedSortIndicator.addMouseDownHandler(mouseDownHandler);
//    dateModifiedHeaderLabel.addMouseDownHandler(mouseDownHandler);
//    dateModifiedSortIndicator.addMouseDownHandler(mouseDownHandler);
//  }

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

//  private void refreshSortIndicators() {
//    String text = (sortOrder == SortOrder.ASCENDING)
//        ? "\u25B2"      // up-pointing triangle
//        : "\u25BC";     // down-pointing triangle
//    switch (sortField) {
//      case NAME:
//        nameSortIndicator.setText(text);
//        dateCreatedSortIndicator.setText("");
//        dateModifiedSortIndicator.setText("");
//        break;
//      case DATE_CREATED:
//        dateCreatedSortIndicator.setText(text);
//        dateModifiedSortIndicator.setText("");
//        nameSortIndicator.setText("");
//        break;
//      case DATE_MODIFIED:
//        dateModifiedSortIndicator.setText(text);
//        dateCreatedSortIndicator.setText("");
//        nameSortIndicator.setText("");
//        break;
//    }
//  }

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
    LOG.warning("Refreshing");
    if (needToSort) {
      // Sort the projects.
      Comparator<Project> comparator;
      switch (sortField) {
        default:
        case NAME:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_NAME_ASCENDING
              : ProjectComparators.COMPARE_BY_NAME_DESCENDING;
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
    }

//    refreshSortIndicators();

    container.clear();
    selectedProjectListItems.clear();
    projectListItems.clear();
    for (final Folder childFolder : folder.getChildFolders()) {
      if ("*trash*".equals(childFolder.getName())) {
//        if (childFolder.getProjects().size() == 0) {
//          Ode.getInstance().createEmptyTrashDialog(true);
//        }
        continue;
      }
      createProjectsFolder(childFolder, container);
    }
    for(final Project project : folder.getProjects()) {
      createProjectListItem(project, container);
    }
    selectAllCheckBox.setValue(false);
    Ode.getInstance().getBindProjectToolbar().updateButtons();
  }

//  protected void createProjectListItem(final Project project, final ComplexPanel container) {
//    final ProjectListItem projectListItem = new ProjectListItem(project, depth + 1);
//    projectListItem.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
//      @Override
//      public void onSelectionChange(boolean selected) {
//        if (selected) {
//          selectedProjectListItems.add(projectListItem);
//        } else {
//          selectedProjectListItems.remove(projectListItem);
//        }
//        fireSelectionChangeEvent();
//      }
//    });
//
//    if(!isTrash) {
//      projectListItem.setClickHandler(new ClickHandler() {
//        @Override
//        public void onClick(ClickEvent e) {
//          if(!project.isInTrash())
//            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
//        }
//      });
//    }
//    projectListItems.add(projectListItem);
//    container.add(projectListItem);
//  }

  public void setSelected(boolean selected) {
    selectAllCheckBox.setValue(selected);
    selectedProjectListItems.clear();
    for(ProjectListItem item : projectListItems) {
      item.setSelected(selected);
      if(selected) {
        selectedProjectListItems.add(item);
      }
    }
    for(ProjectsFolder item : projectsFolders) {
      item.setSelected(selected);
    }
  }

  public boolean isSelected() {
    return selectAllCheckBox.getValue();
  }

  protected void fireSelectionChangeEvent() {
    if (getFolders().size() == getSelectedFolders().size() &&
            getProjects().size() == getSelectedProjects().size()) {
      selectAllCheckBox.setValue(true);
    } else {
      selectAllCheckBox.setValue(false);
    }
    super.fireSelectionChangeEvent();
  }

  @UiHandler("selectAllCheckBox")
  void toggleItemSelection(ClickEvent e) {
    setSelected(selectAllCheckBox.getValue());
    fireSelectionChangeEvent();
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
    LOG.info("On project added " + project.getProjectName() + " projectsLoaded=" + projectsLoaded);
    if (folder == null) {
      LOG.info("Folder is null ");
    } else {
      LOG.info("Folder is: ");
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
    LOG.info("ProjectList.onProjectsLoaded");
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
