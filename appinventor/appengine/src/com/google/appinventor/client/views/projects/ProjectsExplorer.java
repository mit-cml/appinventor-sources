package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.components.Button;
import com.google.appinventor.client.components.Dropdown;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.shared.rpc.ServerLayout;

public class ProjectsExplorer extends Composite {
  interface ProjectsExplorerUiBinder extends UiBinder<FlowPanel, ProjectsExplorer> {}
  private static final ProjectsExplorerUiBinder UI_BINDER = GWT.create(ProjectsExplorerUiBinder.class);

  @UiField Button newProjectButton;

  @UiField Button downloadButton;
  @UiField Button exportButton;
  @UiField Button publishButton;

  @UiField Button deleteButton;

  @UiField FlowPanel exportDropdownContainer;
  Dropdown exportDropdown = new Dropdown();

  @UiField Button mobileOverflowButton;
  @UiField FlowPanel mobileDropdownContainer;
  Dropdown mobileDropdown = new Dropdown(true);

  @UiField ProjectsList projectsList;

  public ProjectsExplorer() {
    initWidget(UI_BINDER.createAndBindUi(this));
    if(Ode.isMobile()) {
      newProjectButton.setText("");
    }
    projectsList.setSelectionChangeHandler(new ProjectsList.SelectionChangeHandler() {
      @Override
      public void onSelectionChange(int selectedItemCount) {
        if(selectedItemCount == 0) {
          downloadButton.setEnabled(false);
          exportButton.setEnabled(false);
          deleteButton.setEnabled(false);
        } else {
          downloadButton.setEnabled(true);
          exportButton.setEnabled(true);
          deleteButton.setEnabled(true);
        }

        if(selectedItemCount == 1) {
          publishButton.setEnabled(true);
        } else {
          publishButton.setEnabled(false);
        }
      }
    });

    mobileDropdown.setDropdownButton(mobileOverflowButton);
    mobileDropdown.setWidget(mobileDropdownContainer);
    exportDropdown.setDropdownButton(exportButton);
    exportDropdown.setWidget(exportDropdownContainer);
  }

  @UiFactory
  public OdeMessages getMessages() {
    return MESSAGES;
  }

  @UiHandler("newProjectButton")
  void createNewProject(ClickEvent e) {
    if (Ode.getInstance().screensLocked()) {
      // Refuse to switch if locked (save file happening)
      return;
    }
    new NewYoungAndroidProjectWizard(null).center();
  }

  @UiHandler("downloadButton")
  void downloadSelectedProjects(ClickEvent e) {
    String selectedProjPath = ServerLayout.DOWNLOAD_SERVLET_BASE;

    if(projectsList.getSelectedProjects().size() == 1) {
      selectedProjPath += ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" +
          projectsList.getSelectedProjects().get(0).getProjectId();
    } else {
      selectedProjPath += ServerLayout.DOWNLOAD_SELECTED_PROJECTS_SOURCE + "/";
      for (Project project : projectsList.getSelectedProjects()) {
        selectedProjPath += project.getProjectId() + "-";
      }
    }

    Downloader.getInstance().download(selectedProjPath);
  }

  @UiHandler("deleteButton")
  void deleteSelectedProjects(ClickEvent e) {
    for(Project project : projectsList.getSelectedProjects()) {
      project.moveToTrash();
    }
  }
}
