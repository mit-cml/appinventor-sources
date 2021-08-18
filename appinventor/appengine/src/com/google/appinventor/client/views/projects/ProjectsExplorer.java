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
import com.google.gwt.user.client.Window;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.components.Button;
import com.google.appinventor.client.components.Dialog;
import com.google.appinventor.client.components.Dropdown;
import com.google.appinventor.client.explorer.commands.AddFolderCommand;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.wizards.ProjectUploadWizard;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;

import java.util.ArrayList;
import java.util.List;

public class ProjectsExplorer extends Composite {
  interface ProjectsExplorerUiBinder extends UiBinder<FlowPanel, ProjectsExplorer> {}
  private static final ProjectsExplorerUiBinder UI_BINDER = GWT.create(ProjectsExplorerUiBinder.class);

  @UiField FlowPanel projectsViewActions;
  @UiField FlowPanel trashViewActions;

  @UiField Button newProjectButton;
  @UiField Button importProjectButton;
  @UiField Button importFromTemplateButton;
  @UiField Button newFolderButton;

  @UiField Button renameButton;
  @UiField Button downloadButton;
  @UiField Button exportButton;
  @UiField Button publishButton;

  @UiField Button trashButton;
  @UiField Button moveButton;

  @UiField Button restoreButton;
  @UiField Button deleteButton;

  @UiField FlowPanel exportDropdownContainer;
  Dropdown exportDropdown = new Dropdown();

  @UiField Button mobileOverflowButton;
  @UiField FlowPanel mobileDropdownContainer;
  Dropdown mobileDropdown = new Dropdown(true);

  @UiField ProjectsList projectsList;
  @UiField ProjectsList trashList;

  @UiField(provided=true)
  Resources.ProjectsExplorerStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.projectsExplorerStyleDark() :
      Resources.INSTANCE.projectsExplorerStyleLight();

  public ProjectsExplorer() {
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
    switchToProjects();
    if (Ode.isMobile()) {
      newProjectButton.setText("");
    }
    projectsList.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        List<Project> selectedProjects = projectsList.getSelectedProjects();
        List<Folder> selectedFolders = projectsList.getSelectedFolders();
        OdeLog.log("Projects list selection change raised for " + selectedFolders.size() +
            " folders and " + selectedProjects.size() + " projects");
        int projectCount = selectedProjects.size();
        int folderCount = selectedFolders.size();
        int totalSelected = projectCount + folderCount;
        downloadButton.setEnabled(false);
        exportButton.setEnabled(false);
        moveButton.setEnabled(false);
        publishButton.setEnabled(false);
        renameButton.setEnabled(false);
        trashButton.setEnabled(false);
        if (projectCount > 0 && folderCount == 0) {
          downloadButton.setEnabled(true);
        }
        if (projectCount == 1 && folderCount == 0) {
          publishButton.setEnabled(true);
          exportButton.setEnabled(true);
        }
        if (totalSelected > 0) {
          renameButton.setEnabled(true);
          trashButton.setEnabled(true);
          moveButton.setEnabled(true);
        }
      }
    });

    trashList.setSelectionChangeHandler(new ProjectSelectionChangeHandler() {
      @Override
      public void onSelectionChange(boolean selected) {
        List<Project> selectedProjects = trashList.getSelectedProjects();
        List<Folder> selectedFolders = trashList.getSelectedFolders();
        OdeLog.log("Trash list selection change raised for " + selectedFolders.size() +
            " folders and " + selectedProjects.size() + " projects");
        int projectCount = selectedProjects.size();
        int folderCount = selectedFolders.size();
        deleteButton.setEnabled(false);
        restoreButton.setEnabled(false);
        if (projectCount + folderCount > 0) {
          restoreButton.setEnabled(true);
          deleteButton.setEnabled(true);
        }
      }
    });

    mobileDropdown.setDropdownButton(mobileOverflowButton);
    mobileDropdown.setWidget(mobileDropdownContainer);
    exportDropdown.setDropdownButton(exportButton);
    exportDropdown.setWidget(exportDropdownContainer);
  }

  public void switchToTrash() {
    projectsViewActions.setVisible(false);
    projectsList.setVisible(false);
    trashViewActions.setVisible(true);
    trashList.setVisible(true);
  }

  public void switchToProjects() {
    projectsViewActions.setVisible(true);
    projectsList.setVisible(true);
    trashViewActions.setVisible(false);
    trashList.setVisible(false);
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

  @UiHandler("importProjectButton")
  void importProject(ClickEvent e) {
    new ProjectUploadWizard().center();
  }

  @UiHandler("importFromTemplateButton")
  void importFromTemplate(ClickEvent e) {
    new TemplateUploadWizard().center();
  }

  @UiHandler("newFolderButton")
  void newFolder(ClickEvent e) {
    new AddFolderCommand().execute();
  }

  @UiHandler("renameButton")
  void renameSelectedProjects(ClickEvent e) {
    new RenameProjectsCommand().execute(null);
    projectsList.setSelected(false);
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
    projectsList.setSelected(false);
  }

  @UiHandler("publishButton")
  void publishProject(ClickEvent e) {
    Project project = projectsList.getSelectedProjects().get(0);
    projectsList.setSelected(false);
    Ode.getInstance().getProjectService().sendToGallery(project.getProjectId(),
      new OdeAsyncCallback<RpcResult>(
        MESSAGES.GallerySendingError()) {
        @Override
        public void onSuccess(RpcResult result) {
          if (result.getResult() == RpcResult.SUCCESS) {
            Window.open(result.getOutput(), "_blank", "");
          } else {
            ErrorReporter.reportError(result.getError());
          }
        }
        @Override
        public void onFailure(Throwable t) {
          super.onFailure(t);
        }
      });
  }

  @UiHandler("trashButton")
  void trashSelectedProjects(ClickEvent e) {
    if (confirmDelete(projectsList.getSelectedProjects())) {
      for (Project project : projectsList.getSelectedProjects()) {
        project.moveToTrash();
      }
      // for (Folder folder : projectsList.getSelectedFolders()) {
      //   Ode.getInstance().getFolderManager().moveToTrash(folder);
      // }
      projectsList.setSelected(false);
      trashList.setSelected(false);
    }
  }

  @UiHandler("restoreButton")
  void restoreSelectedProjects(ClickEvent e) {
    for(Project project : trashList.getSelectedProjects()) {
      project.restoreFromTrash();
    }
    projectsList.setSelected(false);
    trashList.setSelected(false);
  }

  @UiHandler("deleteButton")
  void deleteSelectedProjects(ClickEvent e) {
    for(Project project : trashList.getSelectedProjects()) {
      project.deleteFromTrash();
    }
    projectsList.setSelected(false);
    trashList.setSelected(false);
  }

  @UiHandler("viewTrashButton")
  void viewTrash(ClickEvent e) {
    switchToTrash();
  }

  @UiHandler("viewProjectsButton")
  void viewProjects(ClickEvent e) {
    switchToProjects();
  }

  private boolean confirmDelete(List<Project> projects) {
    String message;
    if (projects.size() == 1) {
      message = MESSAGES.confirmMoveToTrashSingleProject(projects.get(0).getProjectName());
    } else {
      StringBuilder sb = new StringBuilder();
      String separator = "";
      for (Project project : projects) {
        sb.append(separator).append(project.getProjectName());
        separator = ", ";
      }
      String projectNames = sb.toString();
      message = MESSAGES.confirmMoveToTrash(projectNames);
    }
    return Window.confirm(message);
  }
}
