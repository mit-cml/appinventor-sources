// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.common.collect.Ordering;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The project list shows all folders and the current folder's projects in a table.
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
    PUBLISHED,
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }

  // TODO: add these to OdeMessages.java
  private static final String NOT_PUBLISHED = "No";
  private static final String PUBLISHED = "Yes";
  private static final String PUBLISHBUTTONTITLE = "Open a dialog to publish your app to the Gallery";
  private static final String UPDATEBUTTONTITLE = "Open a dialog to publish your newest version in the Gallery";
  private static final char FOLDER_DIVIDER = '/';


  private final List<String> folders;
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
  private final Label nameSortIndicator;
  private final Label dateCreatedSortIndicator;
  private final Label dateModifiedSortIndicator;
  private final Label publishedSortIndicator;

  GalleryClient gallery = null;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {
    allProjects = new ArrayList<Project>();
    selectedProjects = new ArrayList<Project>();
    projectWidgets = new HashMap<Project, ProjectWidgets>();
    folderWidgets = new HashMap<String, FolderWidgets>();

    folders = new ArrayList<String>();
    selectedFolders = new ArrayList<String>();

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;

    currentFolder = null;
    currentSubFolders = new ArrayList<String>();
    currentProjects = new ArrayList<Project>();
    projectsByFolder = new HashMap<String, List<Project>>();
    projectsByFolder.put(null, currentProjects);

    // Initialize UI
    table = new Grid(1, 5); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    dateCreatedSortIndicator = new Label("");
    dateModifiedSortIndicator = new Label("");
    publishedSortIndicator = new Label("");
    refreshSortIndicators();
    setHeaderRow();

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(table);
    initWidget(panel);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);

    gallery = GalleryClient.getInstance();
  }

  /**
   * Adds the header row to the table.
   *
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

    HorizontalPanel nameHeader = new HorizontalPanel();
    final Label nameHeaderLabel = new Label(MESSAGES.projectNameHeader());
    nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    nameHeader.add(nameHeaderLabel);
    nameSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    nameHeader.add(nameSortIndicator);
    table.setWidget(0, 1, nameHeader);

    HorizontalPanel dateCreatedHeader = new HorizontalPanel();
    final Label dateCreatedHeaderLabel = new Label(MESSAGES.projectDateCreatedHeader());
    dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedHeaderLabel);
    dateCreatedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedSortIndicator);
    table.setWidget(0, 2, dateCreatedHeader);

    HorizontalPanel dateModifiedHeader = new HorizontalPanel();
    final Label dateModifiedHeaderLabel = new Label(MESSAGES.projectDateModifiedHeader());
    dateModifiedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateModifiedHeader.add(dateModifiedHeaderLabel);
    dateModifiedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateModifiedHeader.add(dateModifiedSortIndicator);
    table.setWidget(0, 3, dateModifiedHeader);

    HorizontalPanel publishedHeader = new HorizontalPanel();
    final Label publishedHeaderLabel = new Label(MESSAGES.projectPublishedHeader());
    publishedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    publishedHeader.add(publishedHeaderLabel);
    publishedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    publishedHeader.add(publishedSortIndicator);
    table.setWidget(0, 4, publishedHeader);

    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent e) {
        SortField clickedSortField;
        if (e.getSource() == nameHeaderLabel || e.getSource() == nameSortIndicator) {
          clickedSortField = SortField.NAME;
        } else if (e.getSource() == dateCreatedHeaderLabel || e.getSource() == dateCreatedSortIndicator) {
          clickedSortField = SortField.DATE_CREATED;
        } else if (e.getSource() == dateModifiedHeaderLabel || e.getSource() == dateModifiedSortIndicator){
          clickedSortField = SortField.DATE_MODIFIED;
        }else{
          clickedSortField = SortField.PUBLISHED;
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
    publishedHeaderLabel.addMouseDownHandler(mouseDownHandler);
    publishedSortIndicator.addMouseDownHandler(mouseDownHandler);
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
      case PUBLISHED:
        publishedSortIndicator.setText(text);
        nameSortIndicator.setText("");
        dateCreatedSortIndicator.setText("");
        dateModifiedSortIndicator.setText("");
    }
  }

  private abstract class ListEntryWidgets {
    CheckBox checkBox;
    Label nameLabel;
    Label dateCreatedLabel;
    Label dateModifiedLabel;
    Label publishedLabel;
  }

  private class FolderWidgets extends ListEntryWidgets {
    final CheckBox checkBox;
    final Label nameLabel;
    final Label dateCreatedLabel = new Label("");
    final Label dateModifiedLabel = new Label("");
    final Label publishedLabel = new Label("");

    /**
     * Constructor for standard folder links- the displayed folder name is relative its parent
     */
    private FolderWidgets(final String folderName) {
      this.checkBox = new CheckBox();
      this.checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          int row = (currentFolder == null)
              ? 1 + folders.indexOf(folderName)
              : 2 + folders.indexOf(folderName); // The first folder will always be the parent folder when available
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
      nameLabel = new Label(displayName);
      nameLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode ode = Ode.getInstance();
          if (ode.screensLocked()) {
            return;             // i/o in progress, ignore request
          }
          changeCurrentFolder(folderName);
        }
      });
      nameLabel.addStyleName("ode-ProjectNameLabel");

      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
    }

    /**
     * Special constructor for parent folder widget- gets internationalized "Previous Folder"
     * from OdeMessages, and disables checkbox
     */
    private FolderWidgets() {
      this.checkBox = new CheckBox();
      this.checkBox.setEnabled(false);

      nameLabel = new Label(MESSAGES.parentFolderName());
      nameLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode ode = Ode.getInstance();
          if (ode.screensLocked()) {
            return;             // i/o in progress, ignore request
          }
          changeCurrentFolder(getParentFolder());
        }
      });
      nameLabel.addStyleName("ode-ProjectNameLabel");
    }
  }

  private class ProjectWidgets extends ListEntryWidgets {
    final CheckBox checkBox;
    final Label nameLabel;
    final Label dateCreatedLabel;
    final Label dateModifiedLabel;
    final Label publishedLabel;

    private ProjectWidgets(final Project project) {
      checkBox = new CheckBox();
      checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          int row = (currentFolder == null)
              ? 1 + currentSubFolders.size() + currentProjects.indexOf(project)
              : 2 + currentSubFolders.size() + currentProjects.indexOf(project);
          if (isChecked) {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
            selectedProjects.add(project);
          } else {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
            selectedProjects.remove(project);
          }
          Ode.getInstance().getProjectToolbar().updateButtons();
        }
      });

      nameLabel = new Label(project.getProjectName());
      nameLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode ode = Ode.getInstance();
          if (ode.screensLocked()) {
            return;             // i/o in progress, ignore request
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

      publishedLabel = new Label();
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
        case PUBLISHED:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_PUBLISHED_ASCENDING
              : ProjectComparators.COMPARE_BY_PUBLISHED_DESCENDING;
          break;
      }
      Collections.sort(currentProjects, comparator);

      if (sortField == SortField.NAME){
        if (sortOrder == SortOrder.ASCENDING){
          Collections.sort(currentSubFolders, Ordering.natural());
        } else {
          Collections.sort(currentSubFolders, Ordering.natural().reversed());
        }
      }
    }

    refreshSortIndicators();

    System.out.println("REFRESHING THE ODE TABLE PLEASE HELP ME");
    System.out.println(currentFolder);
    // Refill the table.
    table.resize(1 + currentProjects.size(), 5);
    int row = 1;
    if (currentFolder != null){
      addWidgetToTable(row, new FolderWidgets());
      row++;
      System.out.println("ADDING PARENT");
    }
    for (String folder : currentSubFolders) {
      System.out.println(folder);
      FolderWidgets fw = folderWidgets.get(folder);
      if (selectedFolders.contains(folder)) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
        fw.checkBox.setValue(true);
      } else {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        fw.checkBox.setValue(false);
      }
      addWidgetToTable(row, fw);
      row++;
    }
    for (Project project : currentProjects) {
      ProjectWidgets pw = projectWidgets.get(project);
      System.out.println(project.getProjectName());
      if (selectedProjects.contains(project)) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
        pw.checkBox.setValue(true);
      } else {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        pw.checkBox.setValue(false);
      }
      addWidgetToTable(row, pw);
      if(Ode.getGallerySettings().galleryEnabled()){
        if (project.isPublished()) {
          pw.publishedLabel.setText(PUBLISHED);
        } else {
          pw.publishedLabel.setText(NOT_PUBLISHED);
        }
      }
      row++;
    }

    Ode.getInstance().getProjectToolbar().updateButtons();
  }

  /**
   * Gets the number of projects
   *
   * @return the number of projects
   */
  public int getNumProjects() {
    return allProjects.size();
  }

  /**
   * Gets the number of selected projects
   *
   * @return the number of selected projects
   */
  public int getNumSelectedProjects() {
    return selectedProjects.size();
  }

  /**
   * Returns the list of selected projects
   *
   * @return the selected projects
   */
  public List<Project> getSelectedProjects() {
    return selectedProjects;
  }

  // ProjectManagerEventListener implementation

  @Override
  public void onProjectAdded(Project project) {
    allProjects.add(project);
    projectWidgets.put(project, new ProjectWidgets(project));
    if (!projectListLoading) {
      refreshTable(true);
    }

    onProjectMovedToFolder(project);
  }

  @Override
  public void onDeletedProjectAdded(Project project) {}

  @Override
  public void onProjectRemoved(Project project) {
    allProjects.remove(project);
    projectWidgets.remove(project);

    refreshTable(false);

    selectedProjects.remove(project);
    Ode.getInstance().getProjectToolbar().updateButtons();

    onProjectRemovedFromFolder(project);
  }

  @Override
  public void onDeletedProjectRemoved(Project project) { }

  @Override
  public void onProjectsLoaded() {
    projectListLoading = false;
    refreshTable(true);
  }

  public void onProjectPublishedOrUnpublished() {
    refreshTable(false);
  }

  @Override
  public void onFolderAddition(String folder) {
    if (!projectsByFolder.containsKey(folder)){
      projectsByFolder.put(folder, new ArrayList<Project>());
      this.folderWidgets.put(folder, new FolderWidgets(folder));
    }
    Ode.getInstance().getProjectManager().setFolders(this.projectsByFolder.keySet());
  }

  @Override
  public void onFolderDeletion(String deletionFolder) {
    for (String folder : projectsByFolder.keySet()) {
      if (folder.startsWith(deletionFolder)) {
        for (final Project project : projectsByFolder.get(folder)) {
          final long oldProjectId = project.getProjectId();

          Ode.getInstance().getProjectService().moveToTrash(oldProjectId, //Avoid rebuilding table for each project
              new OdeAsyncCallback<UserProject>(
                  // failure message
                  MESSAGES.moveToTrashProjectError()) {
                @Override
                public void onSuccess(UserProject projectInfo) {
                  if (projectInfo.getProjectId() == oldProjectId) {
                    allProjects.remove(project);
                    projectWidgets.remove(project);
                    Ode.getInstance().getProjectManager().addDeletedProject(projectInfo);
                    if (Ode.getInstance().getProjectManager().getDeletedProjects().size() == 0) {
                      Ode.getInstance().createEmptyTrashDialog(true);
                    }
                  }
                }
              });
        }
        projectsByFolder.remove(folder);
        folderWidgets.remove(folder);
      }
    }

    selectedFolders.remove(deletionFolder);
    folderWidgets.remove(deletionFolder);
    Ode ode = Ode.getInstance();
    ode.getProjectToolbar().updateButtons();
    ode.getProjectManager().setFolders(this.projectsByFolder.keySet());
    refreshTable(false);
  }

  public void setPublishedHeaderVisible(boolean visible){
    table.getWidget(0, 4).setVisible(visible);
  }

  private void changeCurrentFolder(String newFolder){
    this.currentFolder = newFolder;
    this.currentProjects = this.projectsByFolder.get(newFolder);
    this.currentSubFolders = new ArrayList<String>();
    if (newFolder == null){
      for (String folder : projectsByFolder.keySet()){
        if (!folder.contains("/")){
          this.currentSubFolders.add(folder);
        }
      }
    } else {
      final int newFolderLength = newFolder.length();
      for (String folder : projectsByFolder.keySet()) {
        // Only add direct subfolders of the new folder
        if (!folder.equals(newFolder)
            && folder.startsWith(newFolder)
            && folder.indexOf(FOLDER_DIVIDER, newFolderLength) == -1) {
          this.currentSubFolders.add(folder);
        }
      }
    }
    selectedFolders.clear();
    refreshTable(false);

    Ode ode = Ode.getInstance();
    ode.getProjectManager().setCurrentFolder(newFolder);
    ode.setProjectViewFolder(newFolder);
  }

  @Override
  public void onProjectMovedToFolder(Project project){
    String parentFolder = project.getParentFolder();
    final List<Project> parentFolderProjects = this.projectsByFolder.get(project.getParentFolder());
    if (parentFolderProjects != null){
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

    Ode.getInstance().getProjectManager().setFolders(this.projectsByFolder.keySet());
  }

  @Override
  public void onProjectRemovedFromFolder(Project project){
    final List<Project> parentFolderProjects = this.projectsByFolder.get(project.getParentFolder());
    if (parentFolderProjects != null){
      parentFolderProjects.remove(project);
    }
  }

  /**
   * Returns the name of the parent folder of the current folder.
   */
  private String getParentFolder(){
    if (currentFolder == null) {
      return null;
    }
    final int lastDivider = currentFolder.lastIndexOf(FOLDER_DIVIDER);
    if (lastDivider == -1){
      return null;
    } else {
      return currentFolder.substring(0, lastDivider);
    }
  }

  /**
   * Adds the ListEntryWidget to the displayed table -- does not do any formatting besides
   * adding all the widget items
   */
  private void addWidgetToTable(int row, ListEntryWidgets w){
    table.setWidget(row, 0, w.checkBox);
    table.setWidget(row, 1, w.nameLabel);
    table.setWidget(row, 2, w.dateCreatedLabel);
    table.setWidget(row, 3, w.dateModifiedLabel);
    table.setWidget(row, 4, w.publishedLabel);
  }
}
