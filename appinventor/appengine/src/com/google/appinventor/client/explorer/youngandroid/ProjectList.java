// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.appinventor.shared.rpc.project.GalleryApp;
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
 * The project list shows all projects in a table.
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


  private final List<Project> projects;
  private final List<Project> selectedProjects;
  private final Map<Project, ProjectWidgets> projectWidgets;
  private SortField sortField;
  private SortOrder sortOrder;

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
    projects = new ArrayList<Project>();
    selectedProjects = new ArrayList<Project>();
    projectWidgets = new HashMap<Project, ProjectWidgets>();

    sortField = SortField.DATE_MODIFIED;
    sortOrder = SortOrder.DESCENDING;

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

  private class ProjectWidgets {
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
          int row = 1 + projects.indexOf(project);
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
      Collections.sort(projects, comparator);
    }

    refreshSortIndicators();

    // Refill the table.
    table.resize(1 + projects.size(), 5);
    int row = 1;
    for (Project project : projects) {
      ProjectWidgets pw = projectWidgets.get(project);
      if (selectedProjects.contains(project)) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
        pw.checkBox.setValue(true);
      } else {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        pw.checkBox.setValue(false);
      }
      table.setWidget(row, 0, pw.checkBox);
      table.setWidget(row, 1, pw.nameLabel);
      table.setWidget(row, 2, pw.dateCreatedLabel);
      table.setWidget(row, 3, pw.dateModifiedLabel);
      table.setWidget(row, 4, pw.publishedLabel);
      if(Ode.getGallerySettings().galleryEnabled()){
        if (project.isPublished()) {
          pw.publishedLabel.setText(PUBLISHED);
        }
        else {
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
    return projects.size();
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
    projects.add(project);
    projectWidgets.put(project, new ProjectWidgets(project));
    refreshTable(true);
  }
  @Override
  public void onProjectRemoved(Project project) {
    projects.remove(project);
    projectWidgets.remove(project);

    refreshTable(false);

    selectedProjects.remove(project);
    Ode.getInstance().getProjectToolbar().updateButtons();
  }

  @Override
  public void onProjectsLoaded() {
    // This can be empty
  }
  public void onProjectPublishedOrUnpublished() {
    refreshTable(false);
  }

  public void setPublishedHeaderVisible(boolean visible){
    table.getWidget(0, 4).setVisible(visible);
  }
}
