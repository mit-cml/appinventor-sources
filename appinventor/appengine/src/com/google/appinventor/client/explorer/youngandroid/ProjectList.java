// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
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
 * <p> The project name and date created will be shown in the table.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ProjectList extends Composite implements ProjectManagerEventListener {
  private enum SortField {
    NAME,
    DATE,
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }
  private final List<Project> projects;
  private final List<Project> selectedProjects;
  private final Map<Project, ProjectWidgets> projectWidgets;
  private SortField sortField;
  private SortOrder sortOrder;

  // UI elements
  private final Grid table;
  private final Label nameSortIndicator;
  private final Label dateSortIndicator;

  /**
   * Creates a new ProjectList
   */
  public ProjectList() {
    projects = new ArrayList<Project>();
    selectedProjects = new ArrayList<Project>();
    projectWidgets = new HashMap<Project, ProjectWidgets>();

    sortField = SortField.NAME;
    sortOrder = SortOrder.ASCENDING;

    // Initialize UI
    table = new Grid(1, 3); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    dateSortIndicator = new Label("");
    refreshSortIndicators();
    setHeaderRow();

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(table);
    initWidget(panel);

    // It is important to listen to project manager events as soon as possible.
    Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
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

    HorizontalPanel dateHeader = new HorizontalPanel();
    Label dateHeaderLabel = new Label(MESSAGES.projectDateHeader());
    dateHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateHeader.add(dateHeaderLabel);
    dateSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateHeader.add(dateSortIndicator);
    table.setWidget(0, 2, dateHeader);

    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent e) {
        SortField clickedSortField =
            (e.getSource() == nameHeaderLabel || e.getSource() == nameSortIndicator)
            ? SortField.NAME
            : SortField.DATE;
        changeSortOrder(clickedSortField);
      }
    };
    nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
    nameSortIndicator.addMouseDownHandler(mouseDownHandler);
    dateHeaderLabel.addMouseDownHandler(mouseDownHandler);
    dateSortIndicator.addMouseDownHandler(mouseDownHandler);
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
        dateSortIndicator.setText("");
        break;
      case DATE:
        dateSortIndicator.setText(text);
        nameSortIndicator.setText("");
        break;
    }
  }

  private class ProjectWidgets {
    final CheckBox checkBox;
    final Label nameLabel;
    final Label dateLabel;

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
          Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        }
      });
      nameLabel.addStyleName("ode-ProjectNameLabel");

      Date date = new Date(project.getDateCreated());
      DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
      dateLabel = new Label(dateTimeFormat.format(date));
    }
  }

  private void refreshTable(boolean needToSort) {
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
        case DATE:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_DATE_ASCENDING
              : ProjectComparators.COMPARE_BY_DATE_DESCENDING;
          break;
      }
      Collections.sort(projects, comparator);
    }

    refreshSortIndicators();

    // Refill the table.
    table.resize(1 + projects.size(), 3);
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
      table.setWidget(row, 2, pw.dateLabel);
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
}
