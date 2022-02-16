package com.google.appinventor.client.views.projects;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.client.components.Button;
import com.google.appinventor.client.components.Dialog;
import com.google.appinventor.client.components.Dropdown;
import com.google.appinventor.client.components.DropdownItem;
import com.google.appinventor.client.explorer.commands.AddFolderCommand;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.GenerateYailCommand;
import com.google.appinventor.client.explorer.commands.RenameProjectsCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowBarcodeCommand;
import com.google.appinventor.client.explorer.commands.ShowProgressBarCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.explorer.commands.WarningDialogCommand;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeAdapter;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.wizards.ProjectUploadWizard;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

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
  @UiField Button exportButton;
  @UiField Button buildButton;
  @UiField Button publishButton;

  @UiField Button trashButton;
  @UiField Button moveButton;

  @UiField Button restoreButton;
  @UiField Button deleteButton;

  @UiField Button mobileOverflowButton;

  @UiField ProjectsList projectsList;

  @UiField(provided=true)
  Resources.ProjectsExplorerStyle style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.projectsExplorerStyleDark() :
      Resources.INSTANCE.projectsExplorerStyleLight();

  private BuildOptions buildOptions;
  private MobileOptions mobileOptions;

  public ProjectsExplorer() {
    style.ensureInjected();
    initWidget(UI_BINDER.createAndBindUi(this));
    buildOptions = new BuildOptions(this);
    mobileOptions = new MobileOptions(this);
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
        if (projectsList.isTrash) {
          if (projectCount + folderCount > 0) {
            restoreButton.setEnabled(true);
            deleteButton.setEnabled(true);
          } else {
            deleteButton.setEnabled(false);
            restoreButton.setEnabled(false);
          }
        } else {
          exportButton.setEnabled(false);
          buildButton.setEnabled(false);
          moveButton.setEnabled(false);
          publishButton.setEnabled(false);
          renameButton.setEnabled(false);
          trashButton.setEnabled(false);
        }
        if (projectCount > 0 && folderCount == 0) {
          exportButton.setEnabled(true);
        }
        if (projectCount == 1 && folderCount == 0) {
          publishButton.setEnabled(true);
          buildButton.setEnabled(true);
        }
        if (totalSelected > 0) {
          renameButton.setEnabled(true);
          trashButton.setEnabled(true);
          moveButton.setEnabled(true);
        }
      }
    });
  }

  public void switchToTrash() {
    projectsViewActions.setVisible(false);
    projectsList.setIsTrash(true);
    trashViewActions.setVisible(true);
    projectsList.setIsTrash(true);
  }

  public void switchToProjects() {
    projectsViewActions.setVisible(true);
    projectsList.setIsTrash(false);
    trashViewActions.setVisible(false);
    projectsList.setIsTrash(false);
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
    new RenameProjectsCommand(projectsList.getSelectedProjects(), projectsList.getSelectedFolders()).execute();
    projectsList.setSelected(false);
  }

  @UiHandler("exportButton")
  void exportSelectedProjects(ClickEvent e) {
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

  @UiHandler("buildButton")
  void openExportOptionsDropdown(ClickEvent e) {
    buildOptions.dropdown.showRelativeTo(buildButton);
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
    }
  }

  @UiHandler("restoreButton")
  void restoreSelectedProjects(ClickEvent e) {
//    for (Project project : trashList.getSelectedProjects()) {
//      project.restoreFromTrash();
//    }
//    projectsList.setSelected(false);
//    trashList.setSelected(false);
  }

  @UiHandler("deleteButton")
  void deleteSelectedProjects(ClickEvent e) {
    if (projectsList.isTrash) {
      for (Project project : projectsList.getSelectedProjects()) {
        project.deleteFromTrash();
      }
      projectsList.setSelected(false);
    } else {
      OdeLog.log("Attempted delete from trash when view is not Trash");
    }
  }

  @UiHandler("viewTrashButton")
  void viewTrash(ClickEvent e) {
    switchToTrash();
  }

  @UiHandler("viewProjectsButton")
  void viewProjects(ClickEvent e) {
    switchToProjects();
  }

  @UiHandler("mobileOverflowButton")
  void viewMobileOptions(ClickEvent e) {
    mobileOptions.dropdown.center();
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

  private void buildProject(final boolean isAab) {
    projectsList.setSelected(false);
    Project project = projectsList.getSelectedProjects().get(0);
    ProjectRootNode projectRootNode = project.getRootNode();
    if (projectRootNode != null) {
      startBuildCommand(isAab, projectRootNode);
    } else {
      project.addProjectChangeListener(new ProjectChangeAdapter() {
        @Override
        public void onProjectLoaded(Project project) {
          project.removeProjectChangeListener(this);
          startBuildCommand(isAab, project.getRootNode());
        }
      });
      project.loadProjectNodes();
    }
  }

  private void startBuildCommand(boolean isAab, ProjectRootNode projectRootNode) {
    boolean secondBuildserver = false;
    String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
    ChainableCommand cmd = new SaveAllEditorsCommand(
        new GenerateYailCommand(
            new BuildCommand(target, secondBuildserver, isAab,
              new ShowProgressBarCommand(target,
                new WaitForBuildResultCommand(target,
                  new ShowBarcodeCommand(target, isAab)), "BarcodeAction"),
              new ShowBarcodeCommand(target, isAab))));
    if (!Ode.getInstance().getWarnBuild(secondBuildserver)) {
      cmd = new WarningDialogCommand(target, secondBuildserver, cmd);
      Ode.getInstance().setWarnBuild(secondBuildserver, true);
    }
    cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_BARCODE_YA, projectRootNode,
        new Command() {
          @Override
          public void execute() {
          }
        });
  }

  public static class BuildOptions {
    interface BuildOptionsUiBinder extends UiBinder<Dropdown, BuildOptions> {}
    private static final BuildOptionsUiBinder UI_BINDER =
        GWT.create(BuildOptionsUiBinder.class);

    @UiField Dropdown dropdown;
    @UiField DropdownItem apkDropdownItem;
    @UiField DropdownItem aabDropdownItem;

    private ProjectsExplorer explorer;

    public BuildOptions(ProjectsExplorer explorer) {
      this.explorer = explorer;
      UI_BINDER.createAndBindUi(this);
    }

    @UiHandler("apkDropdownItem")
    void buildProjectAsApk(ClickEvent e) {
      explorer.buildProject(false);
    }

    @UiHandler("aabDropdownItem")
    void buildProjectAsAab(ClickEvent e) {
      explorer.buildProject(true);
    }
  }

  public static class MobileOptions {
    interface MobileOptionsUiBinder extends UiBinder<Dropdown, MobileOptions> {}
    private static final MobileOptionsUiBinder UI_BINDER =
        GWT.create(MobileOptionsUiBinder.class);

    @UiField Dropdown dropdown;
    @UiField DropdownItem exportButton;
    @UiField DropdownItem apkButton;
    @UiField DropdownItem aabButton;
    @UiField DropdownItem publishButton;
    @UiField DropdownItem moveButton;
    @UiField DropdownItem viewTrashButton;
    @UiField DropdownItem trashButton;

    private ProjectsExplorer explorer;

    public MobileOptions(ProjectsExplorer explorer) {
      this.explorer = explorer;
      UI_BINDER.createAndBindUi(this);
    }

    @UiHandler("apkButton")
    void buildProjectAsApk(ClickEvent e) {
      explorer.buildProject(false);
    }

    @UiHandler("aabButton")
    void buildProjectAsAab(ClickEvent e) {
      explorer.buildProject(true);
    }

    @UiHandler("exportButton")
    void exportSelectedProjects(ClickEvent e) {
      explorer.exportSelectedProjects(e);
    }

    @UiHandler("publishButton")
    void publishProject(ClickEvent e) {
      explorer.publishProject(e);
    }

    @UiHandler("viewTrashButton")
    void viewTrash(ClickEvent e) {
      explorer.viewTrash(e);
    }
  }
}
