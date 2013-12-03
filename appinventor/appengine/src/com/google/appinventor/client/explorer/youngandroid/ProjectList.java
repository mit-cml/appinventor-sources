// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.DownloadProjectOutputCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
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
  private final Label dateCreatedSortIndicator;
  private final Label dateModifiedSortIndicator;

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
    table = new Grid(1, 6); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    dateCreatedSortIndicator = new Label("");
    dateModifiedSortIndicator = new Label("");
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
    table.setWidget(0, 0, nameHeader);

    HorizontalPanel dateCreatedHeader = new HorizontalPanel();
    final Label dateCreatedHeaderLabel = new Label(MESSAGES.projectDateCreatedHeader());
    dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedHeaderLabel);
    dateCreatedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateCreatedHeader.add(dateCreatedSortIndicator);
    table.setWidget(0, 1, dateCreatedHeader);

    HorizontalPanel dateModifiedHeader = new HorizontalPanel();
    final Label dateModifiedHeaderLabel = new Label(MESSAGES.projectDateModifiedHeader());
    dateModifiedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateModifiedHeader.add(dateModifiedHeaderLabel);
    dateModifiedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateModifiedHeader.add(dateModifiedSortIndicator);
    table.setWidget(0, 2, dateModifiedHeader);

    table.getCellFormatter().setWidth(0, 0, "44%");
    table.getCellFormatter().setWidth(0, 1, "18%");
    table.getCellFormatter().setWidth(0, 2, "18%");
    table.getCellFormatter().setWidth(0, 3, "6%");
    table.getCellFormatter().setWidth(0, 4, "6%");
    table.getCellFormatter().setWidth(0, 5, "6%");

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

  private class ProjectWidgets {
    //final CheckBox checkBox;
    final Label nameLabel;
    final Label dateCreatedLabel;
    final Label dateModifiedLabel;
    final Button srcButton;
    final Button apkButton;
    final Button delButton;

    private ProjectWidgets(final Project project) {
      //checkBox = new CheckBox();
      /*checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
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
      */

      nameLabel = new Label(project.getProjectName());
      nameLabel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Ode.getInstance().openYoungAndroidProjectInDesigner(project);
        }
      });
      nameLabel.addStyleName("ode-ProjectNameLabel");

      DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MM/dd/yy h:mma");

      Date dateCreated = new Date(project.getDateCreated());
      dateCreatedLabel = new Label(dateTimeFormat.format(dateCreated));

      Date dateModified = new Date(project.getDateModified());
      dateModifiedLabel = new Label(dateTimeFormat.format(dateModified));

      srcButton = new Button(".aia");
      srcButton.setWidth("45px");
      srcButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          // TODO Auto-generated method stub
          exportProject(project);
        }
      });
      
      apkButton = new Button(".apk");
      apkButton.setWidth("45px");
      apkButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          // TODO Auto-generated method stub
          packageProject(project);
        }
      });
      
      delButton = new Button("del");
      delButton.setWidth("45px");
      delButton.setStyleName("gwt-Red-Button");
      delButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent arg0) {
          // TODO Auto-generated method stub
          deleteProject(project);
        }
      });
      
      
    }
  }
  private void deleteProject(Project project) {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_DELETE_PROJECT_YA, project.getProjectName());

    final long projectId = project.getProjectId();

    Ode ode = Ode.getInstance();
    boolean isCurrentProject = (projectId == ode.getCurrentYoungAndroidProjectId());
    ode.getEditorManager().closeProjectEditor(projectId);
    if (isCurrentProject) {
      // If we're deleting the project that is currently open in the Designer we
      // need to clear the ViewerBox first.
      ViewerBox.getViewerBox().clear();
    }
    // Make sure that we delete projects even if they are not open.
    doDeleteProject(projectId);
  }
  private void exportProject(Project project) {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_DOWNLOAD_PROJECT_SOURCE_YA, project.getProjectName());

    Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
        ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + project.getProjectId());
  }
  private void doDeleteProject(final long projectId) {
    Ode.getInstance().getProjectService().deleteProject(projectId,
        new OdeAsyncCallback<Void>(
            // failure message
            MESSAGES.deleteProjectError()) {
      @Override
      public void onSuccess(Void result) {
        Ode.getInstance().getProjectManager().removeProject(projectId);
        // Show a welcome dialog in case there are no
        // projects saved.
        if (Ode.getInstance().getProjectManager().getProjects().size() == 0) {
          Ode.getInstance().createWelcomeDialog(false);
        }
      }
    });
  }
//WORK IN PROGRESSSSSSSS
  private class loadCommand extends ChainableCommand{
    public loadCommand(ChainableCommand nextCommand){
      super(nextCommand);
    }
    @Override
    protected boolean willCallExecuteNextCommand() {
      return true;
    }
    @Override
    protected void execute(ProjectNode node) {
      //Ode.getInstance().openYoungAndroidProjectInDesigner(node.getProjectId());
      Ode.getInstance().openProject(String.valueOf((node.getProjectId())));
      executeNextCommand(node);
    }

  }
  public void packageProject(Project project) {
    ProjectRootNode projectRootNode = project.getRootNode();
    if (projectRootNode != null) {
      String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
      ChainableCommand cmd = new loadCommand(new SaveAllEditorsCommand(
          new GenerateYailCommand(
              new BuildCommand(target,
                  new ShowProgressBarCommand(target,
                      new WaitForBuildResultCommand(target,
                          new DownloadProjectOutputCommand(target)), "DownloadAction")))));
//      updateBuildButton(true);
      cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_DOWNLOAD_YA, projectRootNode,
          new Command() {
            @Override
            public void execute() {
              Ode.getInstance().switchToProjectsView();
//            updateBuildButton(false);
            }
          });
    }
    else{
      //try to print that it's null
      Ode.getInstance().createWelcomeDialog(true);
    }

    
  }
 //WORK IN PROGRESSSSS

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

    refreshSortIndicators();

    // Refill the table.
    table.resize(1 + projects.size(), 6);
    int row = 1;
    for (Project project : projects) {
      ProjectWidgets pw = projectWidgets.get(project);
      if (selectedProjects.contains(project)) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
        //pw.checkBox.setValue(true);
      } else {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        //pw.checkBox.setValue(false);
      }
      //table.setWidget(row, 0, pw.checkBox);
      table.setWidget(row, 0, pw.nameLabel);
      table.setWidget(row, 1, pw.dateCreatedLabel);
      table.setWidget(row, 2, pw.dateModifiedLabel);
      table.setWidget(row, 3, pw.srcButton);
      table.setWidget(row, 4, pw.apkButton);
      table.setWidget(row, 5, pw.delButton);
      row++;
    }

    //Ode.getInstance().getProjectToolbar().updateButtons();
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
    //Ode.getInstance().getProjectToolbar().updateButtons();
  }
}
