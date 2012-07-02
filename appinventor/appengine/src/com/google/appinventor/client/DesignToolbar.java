// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.explorer.commands.AddFormCommand;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.CopyYoungAndroidProjectCommand;
import com.google.appinventor.client.explorer.commands.DeleteFileCommand;
import com.google.appinventor.client.explorer.commands.DownloadProjectOutputCommand;
import com.google.appinventor.client.explorer.commands.EnsurePhoneConnectedCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.ShowBarcodeCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The design toolbar houses command buttons in the Young Android Design
 * tab (for the UI designer and Blocks Editor).
 *
 */
public class DesignToolbar extends Toolbar {
  
  /*
   * A Screen groups together the form editor and blocks editor for an 
   * application screen. Name is the name of the screen (form) displayed
   * in the screens pull-down.
   */
  private static class Screen {
    private String name;
    private FileEditor formEditor;
    private FileEditor blocksEditor;
    
    public Screen(String name, FileEditor formEditor, FileEditor blocksEditor) {
      this.name = name;
      this.formEditor = formEditor;
      this.blocksEditor = blocksEditor;
    }
  }
  
  /*
   * A project as represented in the DesignToolbar. Each project has a name 
   * (as displayed in the DesignToolbar on the left), a set of named screens,
   * and an indication of which screen is currently being edited.
   */
  private static class DesignProject {
    private String name;
    private Map<String, Screen> screens; // screen name -> Screen
    private String currentScreen; // name of currently displayed screen
    
    public DesignProject(String name) {
      this.name = name;
      screens = new HashMap<String, Screen>();
      // Screen1 is initial screen by default
      currentScreen = YoungAndroidSourceNode.screen1FormName(); 
    }
    
    // Returns true if we added the screen (it didn't previously exist), false otherwise.
    public boolean addScreen(String name, FileEditor formEditor, FileEditor blocksEditor) {
      if (!screens.containsKey(name)) {
        screens.put(name, new Screen(name, formEditor, blocksEditor));
        return true;
      } else {
        return false;
      }
    }
    
    public void removeScreen(String name) {
      screens.remove(name);
    }
  }
  
  private static final String WIDGET_NAME_SAVE = "Save";
  private static final String WIDGET_NAME_SAVE_AS = "SaveAs";
  private static final String WIDGET_NAME_CHECKPOINT = "Checkpoint";
  private static final String WIDGET_NAME_ADDFORM = "AddForm";
  private static final String WIDGET_NAME_REMOVEFORM = "RemoveForm";
  private static final String WIDGET_NAME_BUILD = "Build";
  private static final String WIDGET_NAME_BUILD_BARCODE = "Barcode";
  private static final String WIDGET_NAME_BUILD_DOWNLOAD = "Download";
  private static final String WIDGET_NAME_SCREENS_DROPDOWN = "ScreensDropdown";
  private static final String WIDGET_NAME_SWITCH_TO_BLOCKS_EDITOR = "SwitchToBlocksEditor";
  private static final String WIDGET_NAME_SWITCH_TO_DESIGNER = "SwitchToBlocksEditor";
  
  // true if the viewer is showing the designer, false if showing the blocks editor
  private boolean viewDesigner = true;

  private Label projectNameLabel;
  
  // Project currently displayed in designer
  private DesignProject currentProject;
  
  // Map of project id to project info for all projects we've ever showed
  // in the Designer in this session.
  private Map<Long, DesignProject> projectMap = new HashMap<Long, DesignProject>();
  
  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public DesignToolbar() {
    super();

    projectNameLabel = new Label();
    projectNameLabel.setStyleName("ya-ProjectName");
    HorizontalPanel toolbar = (HorizontalPanel) getWidget();
    toolbar.insert(projectNameLabel, 0);
    toolbar.setCellWidth(projectNameLabel, "222px"); // width of palette minus
                                                // cellspacing/border of buttons

    addButton(new ToolbarItem(WIDGET_NAME_SAVE, MESSAGES.saveButton(),
        new SaveAction()));
    addButton(new ToolbarItem(WIDGET_NAME_SAVE_AS, MESSAGES.saveAsButton(),
        new SaveAsAction()));
    addButton(new ToolbarItem(WIDGET_NAME_CHECKPOINT, MESSAGES.checkpointButton(),
        new CheckpointAction()));
    if (AppInventorFeatures.allowMultiScreenApplications()) {
      addButton(new ToolbarItem(WIDGET_NAME_ADDFORM, MESSAGES.addFormButton(),
          new AddFormAction()));
      addButton(new ToolbarItem(WIDGET_NAME_REMOVEFORM, MESSAGES.removeFormButton(),
          new RemoveFormAction()));
    }

    List<ToolbarItem> screenItems = Lists.newArrayList();
    addDropDownButton(WIDGET_NAME_SCREENS_DROPDOWN, MESSAGES.screensButton(), screenItems, true);

    addButton(new ToolbarItem(WIDGET_NAME_SWITCH_TO_DESIGNER,
        MESSAGES.switchToDesignerButton(), new SwitchToDesignerAction()), true);
    addButton(new ToolbarItem(WIDGET_NAME_SWITCH_TO_BLOCKS_EDITOR,
        MESSAGES.switchToBlocksEditorButton(), new SwitchToBlocksEditorAction()), true);

    List<ToolbarItem> buildItems = Lists.newArrayList();
    buildItems.add(new ToolbarItem(WIDGET_NAME_BUILD_BARCODE,
        MESSAGES.showBarcodeButton(), new BarcodeAction()));
    buildItems.add(new ToolbarItem(WIDGET_NAME_BUILD_DOWNLOAD,
        MESSAGES.downloadToComputerButton(), new DownloadAction()));
    addDropDownButton(WIDGET_NAME_BUILD, MESSAGES.buildButton(), buildItems, true);

  }

  private class SaveAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(null);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
      }
    }
  }

  private class SaveAsAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new CopyYoungAndroidProjectCommand(false));
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_AS_YA, projectRootNode);
      }
    }
  }

  private class CheckpointAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new CopyYoungAndroidProjectCommand(true));
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_CHECKPOINT_YA, projectRootNode);
      }
    }
  }

  private class AddFormAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new AddFormCommand();
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_ADDFORM_YA, projectRootNode);
      }
    }
  }

  private class RemoveFormAction implements Command {
    @Override
    public void execute() {
      YoungAndroidSourceNode sourceNode = Ode.getInstance().getCurrentYoungAndroidSourceNode();
      if (sourceNode != null && !sourceNode.isScreen1()) {
        // DeleteFileCommand handles the whole operation, including displaying the confirmation
        // message dialog, closing the form editor and the blocks editor,
        // deleting the files in the server's storage, and deleting the 
        // corresponding client-side nodes (which will ultimately trigger the
        // screen deletion in the DesignToolbar).
        final String deleteConfirmationMessage = MESSAGES.reallyDeleteForm(
            sourceNode.getFormName());
        ChainableCommand cmd = new DeleteFileCommand() {
          @Override
          protected boolean deleteConfirmation() {
            return Window.confirm(deleteConfirmationMessage);
          }
        };
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_REMOVEFORM_YA, sourceNode);
      }
    }
  }
  
  private class SwitchScreenAction implements Command {
    final private long projectId;
    final private String name;

    public SwitchScreenAction(long projectId, String name) {
      this.projectId = projectId;
      this.name = name;
    }
    @Override
    public void execute() {
      if (!projectMap.containsKey(projectId)) {
        OdeLog.wlog("DesignToolbar: no project with id " + projectId 
            + ". Ignoring SwitchScreenAction.execute().");
        return;
      }
      DesignProject project = projectMap.get(projectId);
      if (currentProject != project) {
        // need to switch projects first. 
        switchToProject(projectId, project.name);
        // currentProject == project now
      }
      if (!currentProject.screens.containsKey(name)) {
        OdeLog.wlog("Trying to switch to non-existent screen " + name + 
            " in project " + currentProject.name);
        return;
      }
      Screen screen = currentProject.screens.get(name);
      ProjectEditor projectEditor = screen.formEditor.getProjectEditor();
      currentProject.currentScreen = name;
      setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, name);
      OdeLog.log("Setting currentScreen to " + name);
      if (showingDesigner()) {
        projectEditor.selectFileEditor(screen.formEditor);
      } else {
        projectEditor.selectFileEditor(screen.blocksEditor);
      }
      updateButtons();
    }
  }

  private class BarcodeAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new BuildCommand(target,
                new WaitForBuildResultCommand(target,
                    new ShowBarcodeCommand(target))));
        updateBuildButton(true);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_BARCODE_YA, projectRootNode,
            new Command() {
          @Override
          public void execute() {
            updateBuildButton(false);
          }
        });
      }
    }
  }

  private class DownloadAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new BuildCommand(target,
                new WaitForBuildResultCommand(target,
                    new DownloadProjectOutputCommand(target))));
        updateBuildButton(true);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_DOWNLOAD_YA, projectRootNode,
            new Command() {
          @Override
          public void execute() {
            updateBuildButton(false);
          }
        });
      }
    }
  }

  private class SwitchToBlocksEditorAction implements Command {
    @Override
    public void execute() {
      if (currentProject == null) {
        OdeLog.wlog("DesignToolbar.currentProject is null. Ignoring SwitchToBlocksEditorAction.execute().");
        return;
      }
      if (viewDesigner) {
        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectRootNode().getProjectId();
        switchToScreen(projectId, currentProject.currentScreen, false);
      }
    }
  }
  
  private class SwitchToDesignerAction implements Command {
    @Override
    public void execute() {
      if (currentProject == null) {
        OdeLog.wlog("DesignToolbar.currentProject is null. Ignoring SwitchToDesignerAction.execute().");
        return;
      }
      if (!viewDesigner) {
        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectRootNode().getProjectId();
        switchToScreen(projectId, currentProject.currentScreen, true);
      }
    }
  }

  /*
   * Returns true if we're showing the designer view, or false if showing the blocks view
   */
  public boolean showingDesigner() {
    return viewDesigner;
  }
  
  public void addProject(long projectId, String name) {
    if (!projectMap.containsKey(projectId)) {
      projectMap.put(projectId, new DesignProject(name));
      OdeLog.log("DesignToolbar added project " + name + " with id " + projectId);
    } else {
      OdeLog.wlog("DesignToolbar ignoring addProject for existing project " + name 
          + " with id " + projectId);
    }
  }
 
  // Switch to an existing project. Note that this does not switch screens.
  // TODO(sharon): it would probably be good to throw an exception if the 
  // project doesn't exist.
  private void switchToProject(long projectId, String name) {
    if (projectMap.containsKey(projectId)) {
      DesignProject project = projectMap.get(projectId);
      if (project == currentProject) {
        OdeLog.wlog("DesignToolbar: ignoring call to switchToProject for current project");
        return;
      }
      clearDropDownMenu(WIDGET_NAME_SCREENS_DROPDOWN);
      OdeLog.log("DesignToolbar: switching to existing project " + name + " with id " + projectId);
      currentProject = projectMap.get(projectId);
      // TODO(sharon): add screens to drop-down menu in the right order
      for (Screen screen : currentProject.screens.values()) {
        addDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, new ToolbarItem(screen.name,
            screen.name, new SwitchScreenAction(projectId, screen.name)));
      }
      projectNameLabel.setText(name);
    } else {
      ErrorReporter.reportError("Design toolbar doesn't know about project " + name + " with id " + projectId);
      OdeLog.wlog("Design toolbar doesn't know about project " + name + " with id " + projectId);
    }
  }
  
  /*
   * Add a screen name to the drop-down for the project with id projectId. 
   * name is the form name, formEditor is the file editor for the form UI,
   * and blocksEditor is the file editor for the form's blocks.
   */
  public void addScreen(long projectId, String name, FileEditor formEditor, 
      FileEditor blocksEditor) {
    if (!projectMap.containsKey(projectId)) {
      OdeLog.wlog("DesignToolbar can't find project " + name + " with id " + projectId 
          + ". Ignoring addScreen().");
      return;
    }
    DesignProject project = projectMap.get(projectId);
    if (project.addScreen(name, formEditor, blocksEditor)) {
      if (currentProject == project) {
        addDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, new ToolbarItem(name,
            name, new SwitchScreenAction(projectId, name)));
      }
    }
  }

  /*
   * Switch to screen name in project projectId. Also switches projects if
   * necessary.
   */
  public void switchToScreen(long projectId, String name, boolean showDesigner) {
    viewDesigner = showDesigner;
    new SwitchScreenAction(projectId, name).execute();
  }
  
  /*
   * Remove screen name (if it exists) from project projectId 
   */
  public void removeScreen(long projectId, String name) {
    if (!projectMap.containsKey(projectId)) {
      OdeLog.wlog("DesignToolbar can't find project " + name + " with id " + projectId 
          + " Ignoring removeScreen().");
      return;
    }
    OdeLog.log("DesignToolbar: got removeScreen for project " + projectId 
        + ", screen " + name);
    DesignProject project = projectMap.get(projectId);
    if (!project.screens.containsKey(name)) {
      // already removed this screen
      return;
    }
    if (currentProject == project) {
      // if removing current screen, choose a new screen to show
      if (currentProject.currentScreen.equals(name)) {
        // TODO(sharon): maybe make a better choice than screen1, but for now
        // switch to screen1 because we know it is always there
        switchToScreen(projectId, YoungAndroidSourceNode.screen1FormName(), true);
      }
      removeDropDownButtonItem(WIDGET_NAME_SCREENS_DROPDOWN, name);
    }
    project.removeScreen(name);
  }
  
  /**
   * Enables and/or disables buttons based (mostly) on whether there is a
   * current form.
   */
  private void updateButtons() {
    String screenName = currentProject == null ? null : currentProject.currentScreen;
    boolean enabled = (currentProject != null);
    setButtonEnabled(WIDGET_NAME_SAVE, enabled);
    setButtonEnabled(WIDGET_NAME_SAVE_AS, enabled);
    setButtonEnabled(WIDGET_NAME_CHECKPOINT, enabled);
    setDropItemEnabled(WIDGET_NAME_BUILD, WIDGET_NAME_BUILD_BARCODE, enabled);
    setDropItemEnabled(WIDGET_NAME_BUILD, WIDGET_NAME_BUILD_DOWNLOAD, enabled);

    if (AppInventorFeatures.allowMultiScreenApplications()) {
      setButtonEnabled(WIDGET_NAME_ADDFORM, enabled);
      enabled = (currentProject != null && 
          !YoungAndroidSourceNode.screen1FormName().equals(screenName));
      setButtonEnabled(WIDGET_NAME_REMOVEFORM, enabled);
    }

    if (currentProject != null) {
      setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, screenName);
    } else {
      setDropDownButtonCaption(WIDGET_NAME_SCREENS_DROPDOWN, MESSAGES.screensButton());
    }
  }

  /**
   * Shows feedback on Package for Phone (Build) button while building.
   */
  private void updateBuildButton(boolean isBuilding) {
    setDropDownButtonEnabled(WIDGET_NAME_BUILD, !isBuilding);
    setDropDownButtonCaption(WIDGET_NAME_BUILD,
        isBuilding ? MESSAGES.isBuildingButton() : MESSAGES.buildButton());
  }
}
