// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.client.Ode.getImageBundle;

import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.common.collect.Ordering;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The project list shows all   and the current folder's projects in a table.
 *
 * <p> The project name, date created, and date modified will be shown in the table.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProjectList extends Composite implements ProjectManagerEventListener {
  private enum SortField {
    NAME,
    DATE_CREATED,
    DATE_MODIFIED,
  }

  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }

  // TODO: add these to OdeMessages.java
  private static final char FOLDER_DIVIDER = '/';

  private final List<String> selectedFolders;
  private final Map<String, FolderWidgets> folderWidgets;
  private final List<Project> allProjects;
  private final List<Project> selectedProjects;
  private final Map<Project, ProjectWidgets> projectWidgets;
  private SortField sortField;
  private SortOrder sortOrder;

  private String currentFolder;
  private List<Project> currentProjects;
  private List<String> currentSubFolders;
  private final Map<String, List<Project>> projectsByFolder;

  private boolean projectListLoading = true;

  // UI elements
  private final Grid table;
  private final CheckBox selectAllCheckBox;
  private final Label nameSortIndicator;
  private final Label dateCreatedSortIndicator;
  private final Label dateModifiedSortIndicator;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {
    allProjects = new ArrayList<Project>();
    selectedProjects = new ArrayList<Project>();
    projectWidgets = new HashMap<Project, ProjectWidgets>();
    folderWidgets = new HashMap<String, FolderWidgets>();

    selectedFolders = new ArrayList<String>();

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;

    currentProjects = new ArrayList<Project>();
    currentSubFolders = new ArrayList<String>();
    currentFolder = null;
    projectsByFolder = new HashMap<String, List<Project>>();
    projectsByFolder.put(null, currentProjects);

    // Initialize UI
    table = new Grid(3, 5); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    dateCreatedSortIndicator = new Label("");
    dateModifiedSortIndicator = new Label("");
    refreshSortIndicators();
    selectAllCheckBox = new CheckBox();

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(table);
    initWidget(panel);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
  }

  /**
   * Adds the header row to the table.
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

    selectAllCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
        for (int row = 1; row < table.getRowCount(); ++row) {
          CheckBox c = (CheckBox)table.getWidget(row, 0);
          if (isChecked && !c.getValue() && c.isEnabled()) {
            c.setValue(true, true);
          } else if (!isChecked && c.getValue() && c.isEnabled()) {
            c.setValue(false, true);
          }
        }
        Ode.getInstance().getProjectToolbar().updateButtons();
      }
    });
    table.setWidget(0, 0, selectAllCheckBox);

    HorizontalPanel nameHeader = new HorizontalPanel();
    final Label nameHeaderLabel = new Label(MESSAGES.projectNameHeader());
    nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    nameHeader.add(nameHeaderLabel);
    nameSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    nameHeader.add(nameSortIndicator);
    table.setWidget(0, 2, nameHeader);
    table.getColumnFormatter().setWidth(0, "30px");
    table.getColumnFormatter().setWidth(1, "1px");

    HorizontalPanel dateCreatedHeader = new HorizontalPanel();
    final Label dateCreatedHeaderLabel = new Label(MESSAGES.projectDateCreatedHeader());
    dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedHeaderLabel);
    dateCreatedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedSortIndicator);
    table.setWidget(0, 3, dateCreatedHeader);
    table.getColumnFormatter().setWidth(3, "200px");


    HorizontalPanel dateModifiedHeader = new HorizontalPanel();
    final Label dateModifiedHeaderLabel = new Label(MESSAGES.projectDateModifiedHeader());
    dateModifiedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateModifiedHeader.add(dateModifiedHeaderLabel);
    dateModifiedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateModifiedHeader.add(dateModifiedSortIndicator);
    table.setWidget(0, 4, dateModifiedHeader);
    table.getColumnFormatter().setWidth(4, "200px");

    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent e) {
        SortField clickedSortField;
        if (e.getSource() == nameHeaderLabel || e.getSource() == nameSortIndicator) {
          clickedSortField = SortField.NAME;
        } else if (e.getSource() == dateCreatedHeaderLabel || e.getSource() == dateCreatedSortIndicator) {
          clickedSortField = SortField.DATE_CREATED;
        } else {
          clickedSortField = SortField.DATE_MODIFIED;
        }
        changeSortOrder(clickedSortField);
      }
    };
    nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
    nameSortIndicator.addMouseDownHandler(mouseDownHandler);
    dateCreatedHeaderLabel.addMouseDownHandler(mouseDownHandler);
    dateCreatedSortIndicator.addMouseDownHandler(mouseDownHandler);
    dateModifiedHeaderLabel.addMouseDownHandler(mouseDownHandler);
    dateModifiedSortIndicator.addMouseDownHandler(mouseDownHandler);
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
    refreshTable(true);
  }

  private void refreshSortIndicators() {
    String text = (sortOrder == SortOrder.ASCENDING)
        ? "\u25B2"      // up-pointing triangle
        : "\u25BC";     // down-pointing triangle
    switch (sortField) {
      case NAME:
        nameSortIndicator.setText(text);
        dateCreatedSortIndicator.setText("");
        dateModifiedSortIndicator.setText("");
        break;
      case DATE_CREATED:
        dateCreatedSortIndicator.setText(text);
        dateModifiedSortIndicator.setText("");
        nameSortIndicator.setText("");
        break;
      case DATE_MODIFIED:
        dateModifiedSortIndicator.setText(text);
        dateCreatedSortIndicator.setText("");
        nameSortIndicator.setText("");
        break;
    }
  }

  private abstract class ListEntryWidgets {
    CheckBox checkBox;
    Image nameIcon;
    Label nameLabel;
    Label dateCreatedLabel;
    Label dateModifiedLabel;
  }

  private class FolderWidgets extends ListEntryWidgets {
    final CheckBox checkBox;
    final Image nameIcon;
    final Label nameLabel;
    final Label dateCreatedLabel = new Label();
    final Label dateModifiedLabel = new Label();

    /**
     * Constructor for standard folder links- the displayed folder name is relative its parent
     */
    private FolderWidgets(final String folderName) {
      this.checkBox = new CheckBox();
      this.checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          int row = Integer.parseInt(checkBox.getName());
          if (isChecked) {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
            selectedFolders.add(folderName);
          } else {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
            selectedFolders.remove(folderName);
          }
          Ode.getInstance().getProjectToolbar().updateButtons();
        }
      });

      final int lastDividerLocation = folderName.lastIndexOf('/');
      final String displayName = (lastDividerLocation == -1)
          ? folderName
          : folderName.substring(lastDividerLocation + 1);
      ClickHandler folderClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode ode = Ode.getInstance();
          if (ode.screensLocked()) {
            return;             // i/o in progress, ignore request
          }
          changeCurrentFolder(folderName);
        }
      };
      nameLabel = new Label(displayName);
      nameLabel.addClickHandler(folderClick);
      nameLabel.addStyleName("ode-FolderNameLabel");
      nameIcon = new Image(getImageBundle().folderIcon());
      nameIcon.addClickHandler(folderClick);
      nameIcon.setStylePrimaryName("ode-SimplePaletteItem-icon");
    }

    /**
     * Special constructor for parent folder widget- gets internationalized "Previous Folder"
     * from OdeMessages, and disables checkbox
     */
    private FolderWidgets() {
      this.checkBox = new CheckBox();
      this.checkBox.setEnabled(false);

      ClickHandler folderClick = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode ode = Ode.getInstance();
          if (ode.screensLocked()) {
            return;             // i/o in progress, ignore request
          }
          changeCurrentFolder(getParentFolder());
        }
      };

      nameLabel = new Label(MESSAGES.parentFolderName());
      nameLabel.addClickHandler(folderClick);
      nameLabel.addStyleName("ode-FolderNameLabel");
      nameIcon = new Image(getImageBundle().upFolder());
      nameIcon.addClickHandler(folderClick);
      nameIcon.setStylePrimaryName("ode-SimplePaletteItem-icon");
    }
  }

  private class ProjectWidgets extends ListEntryWidgets {
    final CheckBox checkBox;
    final Label nameLabel;
    final Label dateCreatedLabel;
    final Label dateModifiedLabel;

    private ProjectWidgets(final Project project) {
      checkBox = new CheckBox();
      checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          int row = Integer.parseInt(checkBox.getName());
          if (isChecked) {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
            selectedProjects.add(project);
            if (isAllProjectsSelected()) {
              selectAllCheckBox.setValue(true);
            }
          } else {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
            selectedProjects.remove(project);
            if (selectAllCheckBox.getValue()) {
              selectAllCheckBox.setValue(false);
            }
          }
          Ode.getInstance().getProjectToolbar().updateButtons();
        }
      });

      nameLabel = new Label(project.getProjectName());
      nameLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode ode = Ode.getInstance();
          // If the screens are locked, don't take this action. Also
          // do not open the project if it is in the trash!
          if (ode.screensLocked() || project.isInTrash()) {
            return;
          }
          ode.openYoungAndroidProjectInDesigner(project);
        }
      });
      nameLabel.addStyleName("ode-ProjectNameLabel");

      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();

      Date dateCreated = new Date(project.getDateCreated());
      dateCreatedLabel = new Label(dateTimeFormat.format(dateCreated));

      Date dateModified = new Date(project.getDateModified());
      dateModifiedLabel = new Label(dateTimeFormat.format(dateModified));
    }
  }

  // TODO(user): This method was made public so it can be called
  // directly from from Ode when the Project List View is selected
  // from another view.  Ode now clears any selected projects and
  // calls this to refresh the table as a result. Not sure this is
  // correct thing do to. The alternative is to add a call to the
  // ProjectManagerEventListener interface that this is the
  // implementation of.
  public void refreshTable(boolean needToSort) {
    if (Ode.getInstance().getCurrentView() == Ode.TRASHCAN) {
      refreshTable(needToSort, true);
    } else {
      refreshTable(needToSort, false);
    }
  }

  public void refreshTable(boolean needToSort, boolean isInTrash) {
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
      Collections.sort(currentProjects, comparator);

      if (sortField == SortField.NAME) {
        if (sortOrder == SortOrder.ASCENDING) {
          Collections.sort(currentSubFolders, Ordering.natural());
        } else {
          Collections.sort(currentSubFolders, Ordering.natural().reversed());
        }
      }
    }

    refreshSortIndicators();

    // Refill the table.
    table.clear();
    setHeaderRow();
    final int folderNumber = (currentFolder != null) ? 1 + currentSubFolders.size() : currentSubFolders.size();
    table.resize(1 + folderNumber + currentProjects.size(), 5);
    int previous_rowmax = table.getRowCount() - 1;
    int row = 1;
      if (currentFolder != null) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        FolderWidgets fw = new FolderWidgets();  // This is the parent folder option
        configureFolderDragDrop(table.getRowFormatter().getElement(row), row, getParentFolder(), false);
        fw.checkBox.setValue(false);
        fw.checkBox.setName(String.valueOf(row));
        table.setWidget(row, 0, fw.checkBox);
        table.setWidget(row, 1, fw.nameIcon);
        table.setWidget(row, 2, fw.nameLabel);
        table.setWidget(row, 3, fw.dateCreatedLabel);
        table.setWidget(row, 4, fw.dateModifiedLabel);
        row++;
      }
      for (String folder : currentSubFolders) {
        FolderWidgets fw = folderWidgets.get(folder);
        if (selectedFolders.contains(folder)) {
          table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
          fw.checkBox.setValue(true);
        } else {
          table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
          fw.checkBox.setValue(false);
        }
        fw.checkBox.setName(String.valueOf(row));
        configureFolderDragDrop(table.getRowFormatter().getElement(row), row, folder, true);
        table.setWidget(row, 0, fw.checkBox);
        table.setWidget(row, 1, fw.nameIcon);
        table.setWidget(row, 2, fw.nameLabel);
        table.setWidget(row, 3, fw.dateCreatedLabel);
        table.setWidget(row, 4, fw.dateModifiedLabel);
        row++;
      }

    for (Project project : currentProjects) {
      if (project.isInTrash() == isInTrash) {

        ProjectWidgets pw = projectWidgets.get(project);

        if (selectedProjects.contains(project)) {
          table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
          pw.checkBox.setValue(true);
        } else {
          table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
          pw.checkBox.setValue(false);
        }
        configureProjectDrag(table.getRowFormatter().getElement(row), Long.toString(project.getProjectId()));
        table.setWidget(row, 0, pw.checkBox);
        table.setWidget(row, 2, pw.nameLabel);
        table.setWidget(row, 3, pw.dateCreatedLabel);
        table.setWidget(row, 4, pw.dateModifiedLabel);

        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        pw.checkBox.setValue(false);
        table.getRowFormatter().getElement(row).setAttribute("data-exporturl",
            "application/octet-stream:" + project.getProjectName() + ".aia:"
                + GWT.getModuleBaseURL() + ServerLayout.DOWNLOAD_SERVLET_BASE
                + ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + project.getProjectId());
        configureDraggable(table.getRowFormatter().getElement(row));
        pw.checkBox.setName(String.valueOf(row));
        if (row >= previous_rowmax) {
          table.insertRow(row + 1);
        }
        row++;
      }
    }
    selectAllCheckBox.setValue(false);
    table.resizeRows(row);

    if (isInTrash && table.getRowCount() == 1) {
      Ode.getInstance().createEmptyTrashDialog(true);
    }
    Ode.getInstance().getProjectToolbar().updateButtons();
  }

  /**
   * Gets the number of selected projects
   *
   * @return the number of selected projects
   */
  public int getSelectedProjectsCount() {
    return selectedProjects.size();
  }

  public int getMyProjectsCount() {
    int count = 0;
    for (Project project : allProjects) {
      if (!project.isInTrash()) {
        ++count;
      }
      ;
    }
    return count;
  }

  public int getNumProjects() {
    return allProjects.size();
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
        && getSelectedProjectsCount() == allProjects.size() - getMyProjectsCount()) {
      return true;
    }
    return false;
  }

  /**
   * Returns the list of selected projects.
   * Gets the number of selected folders
   *
   * @return the number of selected folders
   */
  public int getNumSelectedFolders() {
    return selectedFolders.size();
  }

  /**
   * Returns the list of selected projects
   *
   * @return the selected projects
   */
  public List<Project> getSelectedProjects() {
    return selectedProjects;
  }

  //  ProjectManagerEventListener implementation

  /**
   * Returns the list of selected folders
   *
   * @return the selected folders
   */
  public List<String> getSelectedFolders() {
    return selectedFolders;
  }

  /**
   * Returns a copy set of all the folders that exist
   *
   * @return all the folders in the Project List
   */
  public Set<String> getFolders() {
    return new HashSet<String>(this.projectsByFolder.keySet());
  }

  /**
   * Returns the current folder
   *
   * @return the active folder in the project list
   */
  public String getCurrentFolder() {
    return this.currentFolder;
  }

  // ProjectManagerEventListener implementation

  @Override
  public void onProjectAdded(Project project) {
    allProjects.add(project);
    projectWidgets.put(project, new ProjectWidgets(project));
    onProjectMovedToFolder(project);

    if (!projectListLoading) {
      refreshTable(true);
    }
  }

  @Override
  public void onTrashProjectRestored(Project project) {
    selectedProjects.remove(project);
    updateCurrentSubFolders();
    if (!projectListLoading) {
      refreshTable(false);
    }
    Ode.getInstance().getProjectToolbar().updateButtons();
  }

    @Override
    public void onProjectTrashed (Project project){
      selectedProjects.remove(project);
      updateCurrentSubFolders();
      if (!projectListLoading) {
        refreshTable(false);
      }
      Ode.getInstance().getProjectToolbar().updateButtons();
    }

    @Override
    public void onProjectDeleted (Project project){
//      selectedProjects.remove(project);
//      updateCurrentSubFolders();
//      projectWidgets.remove(project);
//      if (!projectListLoading) {
//        refreshTable(false);
//      }
      Ode.getInstance().getProjectToolbar().updateButtons();
    }

    @Override
    public void onProjectsLoaded () {
      projectListLoading = false;
      updateCurrentSubFolders();
      refreshTable(true);
    }

    private static native void configureDraggable (Element el)/*-{
    if (el.getAttribute('draggable') != 'true') {
      el.setAttribute('draggable', 'true');
      el.addEventListener('dragstart', function(e) {
        e.dataTransfer.setData('DownloadURL', this.dataset.exporturl);
      });
    }
  }-*/
    ;

    @Override
    public void onFolderAddition (String folder){
      if (!projectsByFolder.containsKey(folder)) {
        projectsByFolder.put(folder, new ArrayList<Project>());
        this.folderWidgets.put(folder, new FolderWidgets(folder));
        updateCurrentSubFolders();
      }
      if (!projectListLoading) {
        refreshTable(true);
      }
    }

    @Override
    public void onFolderDeletion (String deletionFolder){
      final Set<String> folders = new HashSet<String>(projectsByFolder.keySet());
      for (String folder : folders) {
        if (isParentOrSameFolder(deletionFolder, folder)) {
          projectsByFolder.remove(folder);
          folderWidgets.remove(folder);
        }
      }

      selectedFolders.remove(deletionFolder);
      folderWidgets.remove(deletionFolder);
      updateCurrentSubFolders();
      Ode ode = Ode.getInstance();
      ode.getProjectToolbar().updateButtons();
      if (!projectListLoading) {
        refreshTable(true);
      }
    }

    /**
     * Navigates to newFolder and refreshes the table
     */
    public void changeCurrentFolder (String newFolder){
      this.currentFolder = newFolder;
      this.currentProjects = this.projectsByFolder.get(newFolder);
      selectedFolders.clear();
      Ode ode = Ode.getInstance();
      ode.setProjectViewFolder(newFolder);
      updateCurrentSubFolders();
      if (!projectListLoading) {
        refreshTable(true);
      }
    }

    @Override
    public void onProjectMovedToFolder (Project project){
      String parentFolder = project.getParentFolder();
      final List<Project> parentFolderProjects = this.projectsByFolder.get(project.getParentFolder());
      if (parentFolderProjects != null) {
        parentFolderProjects.add(project);
      } else {
        final List<Project> newList = new ArrayList<Project>();
        newList.add(project);
        this.projectsByFolder.put(parentFolder, newList);
        this.folderWidgets.put(parentFolder, new FolderWidgets(parentFolder));

        // Make sure the folder is not orphaned/unreachable
        int lastDivider = parentFolder.lastIndexOf(FOLDER_DIVIDER);
        parentFolder = parentFolder.substring(0, lastDivider);
        while (lastDivider != -1 && !this.projectsByFolder.containsKey(parentFolder)) {
          this.projectsByFolder.put(parentFolder, new ArrayList<Project>());
          this.folderWidgets.put(parentFolder, new FolderWidgets(parentFolder));
          lastDivider = parentFolder.lastIndexOf(FOLDER_DIVIDER);
          parentFolder = parentFolder.substring(0, lastDivider);
        }
      }
      updateCurrentSubFolders();
    }

    @Override
    public void onProjectRemovedFromFolder (Project project){
      final List<Project> parentFolderProjects = this.projectsByFolder.get(project.getParentFolder());
      if (parentFolderProjects != null) {
        parentFolderProjects.remove(project);
      }
      updateCurrentSubFolders();
    }

    /**
     * Returns the name of the parent folder of the current folder.
     */
    private String getParentFolder () {
      if (currentFolder == null) {
        return null;
      }
      final int lastDivider = currentFolder.lastIndexOf(FOLDER_DIVIDER);
      if (lastDivider == -1) {
        return null;
      } else {
        return currentFolder.substring(0, lastDivider);
      }
    }

    public void trashFolder(String folder) {
      for (String f : projectsByFolder.keySet()) {
        // Projects from this folder and all subfolders must be trashed.
        // Deleting the single folder should handle subfolders (?)
        if (f != null && f.startsWith(folder)) {
          for (Project p : projectsByFolder.get(f)) {
            if (!p.isInTrash()) {
              p.moveToTrash();
            }
          }
        }
      }
      currentSubFolders.remove(folder);
    }

    public void restoreFolder(String folder) {
      for (String f : projectsByFolder.keySet()) {
        // Projects from this folder and all subfolders must be trashed.
        // Deleting the single folder should handle subfolders (?)
        if (f != null && f.startsWith(folder)) {
          for (Project p : projectsByFolder.get(f)) {
            if (p.isInTrash()) {
              p.restoreFromTrash();
            }
          }
        }
      }
      currentSubFolders.remove(folder);
    }

    private void doDeleteFolder(final String folderName) {
      Ode.getInstance().getProjectManager().deleteFolder(folderName);// TODO() Need to call RPC to delete folder
    }

    /**
     * Updates the current sub folders to reflect the current folder
     *
     * @return List of direct child folders
     */
    private void updateCurrentSubFolders () {
      boolean isInTrash = Ode.getInstance().getCurrentView() == Ode.TRASHCAN;
      this.currentSubFolders = new ArrayList<String>();
      if (this.currentFolder == null) {
        for (String folder : this.projectsByFolder.keySet()) {
          if (folder != null && !folder.contains("/")) {
            if (!isInTrash && projectsByFolder.get(folder).size() == 0) {
              this.currentSubFolders.add(folder);
            }
            for (Project p : projectsByFolder.get(folder)) {
              if (p.isInTrash() == isInTrash) {
                this.currentSubFolders.add(folder);
                break;
              }
            }
          }
        }
      } else {
        for (String folder : this.projectsByFolder.keySet()) {
          // Only add direct subfolders of the new folder
          if (folder != null
              && !folder.equals(this.currentFolder)
              && folder.startsWith(this.currentFolder + "/")
              && folder.indexOf(FOLDER_DIVIDER, this.currentFolder.length() + 1) == -1) {
            if (!isInTrash && projectsByFolder.get(folder).size() == 0) {
              this.currentSubFolders.add(folder);
            }
            for (Project p : projectsByFolder.get(folder)) {
              if (p.isInTrash() == isInTrash) {
                this.currentSubFolders.add(folder);
                break;
              }
            }
          }
        }
      }
    }

  /**
   * Handles the moving projects and folders that are dragged (serialized in data) and dropped into the
   * targetFolder.
   *
   * @param data serialized project id or folder name that is being dragged
   * @param targetFolder the folder it is being dropped into
   */
  private void handleFolderDropEvent(final String data, final String targetFolder){
    final Ode ode = Ode.getInstance();
    if (data.startsWith("p/")){
      handleProjectMove(Long.parseLong(data.substring(2)), targetFolder);
    }
    else if (data.startsWith("f/")){
      handleFolderMove(data.substring(2), targetFolder);
    }
  }

  /**
   * Handles the movement of the project with id projectId to targetFolder.
   */
  public void handleProjectMove(final long projectId, final String targetFolder){
    Project p1 = null;
    for (Project p : this.currentProjects){
      if (p.getProjectId() == projectId){
        p1 = p;
      }
    }
    final Project project = p1;
    final String newParent = getNewParentFolderName(project.getParentFolder(), targetFolder);
    Ode.getInstance().getProjectService().moveProjectToFolder(projectId, newParent,
        new AsyncCallback<UserProject>() {
          @Override
          public void onFailure(Throwable throwable) {
            ErrorReporter.reportError(MESSAGES.couldNotChangeProjectFolder());
          }

          @Override
          public void onSuccess(UserProject userProject) {
            onProjectRemovedFromFolder(project);
            selectedProjects.remove(project);
            project.setParentFolder(userProject.getParentFolder());
            onProjectMovedToFolder(project);
            updateCurrentSubFolders();
            refreshTable(false);
          }
        });
  }

  /**
   * Handles the movement of the folder movingFolder (and all subfolders & projects within) to
   * targetFolder.
   */
  public void handleFolderMove(final String movingFolder, final String targetFolder){
      if (movingFolder.equals(targetFolder)) return;
      final List<Project> projects = getProjectsInFolder(movingFolder);
      final List<Long> projectIds = new ArrayList<Long>();
      final List<String> newParents = new ArrayList<String>();

      final String movingFolderNewName = getNewParentFolderName(movingFolder, targetFolder);
      if (projectsByFolder.keySet().contains(movingFolderNewName)) {
        ErrorReporter.reportError(MESSAGES.duplicateFolderNameError(movingFolderNewName));
        return;
      }

      for (Project project : projects) {
        projectIds.add(project.getProjectId());
        newParents.add(getNewParentFolderName(project.getParentFolder(), targetFolder));
      }

      Ode.getInstance().getProjectService().moveProjectsToFolder(projectIds, newParents,
          new AsyncCallback<List<UserProject>>() {
            @Override
            public void onFailure(Throwable throwable) {
              ErrorReporter.reportError(MESSAGES.couldNotChangeProjectFolder());
            }

            @Override
            public void onSuccess(List<UserProject> userProjects) {
              for (int x = 0; x < projects.size(); x++) { // Project Movement
                final Project project = projects.get(x);
                final UserProject userProject = userProjects.get(x);
                project.setParentFolder(userProject.getParentFolder());
                onProjectMovedToFolder(project);
              }

              final Set<String> folders = new HashSet<String>(projectsByFolder.keySet());
              for (String folder : folders) { // Folder Movement
                if (isParentOrSameFolder(movingFolder, folder)) {
                  final String newFolder = getNewParentFolderName(folder, targetFolder);
                  if (!projectsByFolder.containsKey(newFolder)) {
                    projectsByFolder.put(newFolder, new ArrayList<Project>());
                    folderWidgets.put(newFolder, new FolderWidgets(newFolder));
                  }
                  projectsByFolder.remove(folder);
                  folderWidgets.remove(folder);
                }
              }

              selectedFolders.remove(targetFolder);
              updateCurrentSubFolders();
              refreshTable(false);
            }
          });
    }

  /**
   * Returns all projects that are contained in parentFolder as well as parentFolder's children
   * @param parentFolder the top level folder
   * @return all projects
   */
  public List<Project> getProjectsInFolder(final String parentFolder){
    if (parentFolder == null) {
      return allProjects;
    }

    List<Project> result = new ArrayList<Project>();
    for (final String folderName : projectsByFolder.keySet()){
      if (isParentOrSameFolder(parentFolder, folderName)){
        result.addAll(projectsByFolder.get(folderName));
      }
    }
    return result;
  }

  /**
   * Gets the new parent folder name that would result from moving the project into the folder
   * specified by newFolder, e.g.:
   * Current Parent: test/currentParent
   * Current Folder: test/currentParent
   * Folder to move to: test
   * New Parent: test
   *
   * Current Parent: test/currentParent/subDirectory
   * Current Folder: test/currentParent
   * Folder to move to: test
   * New Parent: test/subDirectory
   *
   * Handles special cases related to top level parent being null.
   *
   * @param oldParent the old parent folder name
   * @param newFolder the folder to move them into
   * @return the new final parent folder name
   */
  private String getNewParentFolderName(final String oldParent, final String newFolder){
    if (currentFolder == null || oldParent == null){
      return (oldParent == null) ? newFolder : newFolder + "/" + oldParent;
    }
    if (newFolder == null){
      final String result = oldParent.replace(currentFolder, "");
      return (result.indexOf('/') == 0) ? result.substring(1) : null;
    }
    return oldParent.replace(currentFolder, newFolder);
  }

  /**
   * Returns true if parent is a parent directory of folder or the same folder
   */
  private boolean isParentOrSameFolder(String parent, String folder){
    return folder != null && (folder.equals(parent) || folder.startsWith(parent + "/"));
  }

  /**
   * Configures drag and drop functionality for a row.
   *
   * Disables drag handler if draggable is false.
   */
  private native void configureFolderDragDrop(Element row, int rowNum, String folderName, boolean draggable) /*-{
    if (row.rowDragHandler){ // Delete previous row handlers
        row.removeEventListener('dragstart', row.rowDragHandler);
        row.rowDragHandler = null;
    }
    if (row.rowDropHandler){
        row.removeEventListener('drop', row.rowDropHandler);
        row.rowDropHandler = null;
    }
    if (row.rowDragOverHandler){
        row.removeEventListener('dragover', row.rowDragOverHandler);
        row.rowDragOverHandler = null;
    }
    if (row.rowDragLeaveHandler){
        row.removeEventListener('dragleave', row.rowDragLeaveHandler);
        row.rowDragLeaveHandler = null;
    }

    if (draggable) {
      row.setAttribute('draggable', 'true');
      var dragHandler = function(e){
            e.dataTransfer.setData('text', 'f/'+folderName);
            e.dataTransfer.effectAllowed = "copy";
      };
      row.rowDragHandler = dragHandler;
      row.addEventListener('dragstart', dragHandler);
    } else {
     row.setAttribute('draggable', 'false');
    }

    var projectList = this;
    var dropHandler = function(e) {
        e.preventDefault();
        var data = e.dataTransfer.getData('text');
        projectList.@com.google.appinventor.client.explorer.youngandroid.ProjectList::handleFolderDropEvent(Ljava/lang/String;Ljava/lang/String;)(data, folderName);
    };
    row.rowDropHandler = dropHandler;
    row.addEventListener('drop', dropHandler);

    var dragOverHandler = function(e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = "copy";
        row.setAttribute('class', 'ode-ProjectRowHighlighted');
    };
    row.rowDragOverHandler = dragOverHandler;
    row.addEventListener('dragover', dragOverHandler);

    var dragLeaveHandler = function(e) {
        e.preventDefault();
        row.setAttribute('class', 'ode-ProjectRowUnHighlighted');
    };
    row.rowDragLeaveHandler = dragLeaveHandler;
    row.addEventListener('dragleave', dragLeaveHandler);
  }-*/;


  /**
   * Configures drag functionality for a project.
   */
  private native void configureProjectDrag(Element row, String projectId) /*-{
    if (row.rowDragHandler){ // Delete previous row handlers
        row.removeEventListener('dragstart', row.rowDragHandler);
        row.rowDragHandler = null;
    }
    if (row.rowDropHandler){
        row.removeEventListener('drop', row.rowDropHandler);
        row.rowDropHandler = null;
    }
    if (row.rowDragOverHandler){
        row.removeEventListener('dragover', row.rowDragOverHandler);
        row.rowDragOverHandler = null;
    }
    if (row.rowDragLeaveHandler){
        row.removeEventListener('dragleave', row.rowDragLeaveHandler);
        row.rowDragLeaveHandler = null;
    }

    row.setAttribute('draggable', 'true');
    var dragHandler = function(e) {
        e.dataTransfer.setData('text', 'p/' + projectId);
        e.dataTransfer.effectAllowed = "copy";
    };
    row.rowDragHandler = dragHandler;
    row.addEventListener('dragstart', dragHandler);
  }-*/;

}
