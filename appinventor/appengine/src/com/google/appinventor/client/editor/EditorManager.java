// Copyright 2007 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager class for opened project editors.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class EditorManager {
  // Map of project IDs to open project editors
  private final Map<Long, ProjectEditor> openProjectEditors;

  // Timeout (in ms) after which changed content is auto-saved if the user did
  // not continue typing.
  // TODO(user): Make this configurable.
  private static final int AUTO_SAVE_IDLE_TIMEOUT = 500;

  // Timeout (in ms) after which changed content is auto-saved even if the user
  // continued typing.
  // TODO(user): Make this configurable.
  private static final int AUTO_SAVE_FORCED_TIMEOUT = 30000;

  // Fields used for saving and auto-saving.
  private final Set<ProjectSettings> dirtyProjectSettings;
  private final Set<FileEditor> dirtyFileEditors;
  private final Timer autoSaveTimer;
  private boolean autoSaveIsScheduled;
  private long autoSaveRequestTime;

  /**
   * Creates the editor manager.
   */
  public EditorManager() {
    openProjectEditors = new HashMap<Long, ProjectEditor>();

    dirtyProjectSettings = new HashSet<ProjectSettings>();
    dirtyFileEditors = new HashSet<FileEditor>();

    autoSaveTimer = new Timer() {
      @Override
      public void run() {
        // When the timer goes off, save all dirtyProjectSettings and
        // dirtyFileEditors.
        saveDirtyEditors(null);
      }
    };
  }

  /**
   * Opens the project editor for the given project.
   * If there is an editor already open for the project, it will be returned.
   * Otherwise, it will create an appropriate editor for the project.
   *
   * @param projectRootNode  the root node of the project to open
   * @return  project editor for the given project
   */
  public ProjectEditor openProject(ProjectRootNode projectRootNode) {
    long projectId = projectRootNode.getProjectId();
    ProjectEditor projectEditor = openProjectEditors.get(projectId);
    if (projectEditor == null) {
      // No open editor for this project yet.
      // Use the ProjectEditorRegistry to get the factory and create the project editor.
      ProjectEditorFactory factory = Ode.getProjectEditorRegistry().get(projectRootNode);
      if (factory != null) {
        projectEditor = factory.createProjectEditor(projectRootNode);

        // Add the editor to the openProjectEditors map.
        openProjectEditors.put(projectId, projectEditor);

        // Load the project into the editor. The actual loading is asynchronous.
        projectEditor.loadProject();
      }
    }

    return projectEditor;
  }

  /**
   * Gets the open project editor of the given project ID.
   *
   * @param projectId the project ID
   * @return the ProjectEditor of the specified project, or null
   */
  public ProjectEditor getOpenProjectEditor(long projectId) {
    return openProjectEditors.get(projectId);
  }

  /**
   * Closes the file editor for a specific file, without saving.
   * This is used when the file is about to be deleted.
   *
   * @param projectId  project ID
   * @param fileId  file ID of the file to be closed
   */
  public void closeFileEditor(long projectId, String fileId) {
    ProjectEditor projectEditor = openProjectEditors.get(projectId);
    if (projectEditor != null) {
      FileEditor fileEditor = projectEditor.getFileEditor(fileId);
      // The file may not be open in an editor. For example, an asset file can be deleted, but there
      // won't be a file editor for it. So, check fileEditor for null.
      if (fileEditor != null) {
        dirtyFileEditors.remove(fileEditor);
        projectEditor.closeFileEditor(fileId);
      }
    }
  }

  /**
   * Closes the project editor for a particular project, without saving.
   * Does not actually remove the editor from the ViewerBox.
   * This is used when the project is about to be deleted.
   *
   * @param projectId  ID of project whose editor is to be closed
   */
  public void closeProjectEditor(long projectId) {
    // TODO(lizlooney) - investigate whether the ProjectEditor and all its FileEditors stay in
    // memory even after we've removed them.
    Project project = Ode.getInstance().getProjectManager().getProject(projectId);
    ProjectSettings projectSettings = project.getSettings();
    dirtyProjectSettings.remove(projectSettings);
    openProjectEditors.remove(projectId);
  }

  /**
   * Schedules auto-save of the given project settings.
   * This method can be called often, as the user is modifying project settings.
   *
   * @param projectSettings the project settings for which to schedule auto-save
   */
  public void scheduleAutoSave(ProjectSettings projectSettings) {
    // Add the project settings to the dirtyProjectSettings list.
    dirtyProjectSettings.add(projectSettings);
    scheduleAutoSaveTimer();
  }

  /**
   * Schedules auto-save of the given file editor.
   * This method can be called often, as the user is modifying a file.
   *
   * @param fileEditor the file editor for which to schedule auto-save
   */
  public void scheduleAutoSave(FileEditor fileEditor) {
    // Add the file editor to the dirtyFileEditors list.
    dirtyFileEditors.add(fileEditor);
    scheduleAutoSaveTimer();
  }

  /**
   * Schedules the auto-save timer.
   */
  private void scheduleAutoSaveTimer() {
    if (autoSaveIsScheduled) {
      // The auto-save timer is already scheduled.
      // The user is making multiple changes and, in general, we want to wait until they are idle
      // before saving. However, we don't want to delay the auto-save forever.
      // If the time that the auto-save was first requested wasn't too long ago, cancel and
      // reschedule the timer. Otherwise, leave the scheduled timer alone.
      if (System.currentTimeMillis() - autoSaveRequestTime < AUTO_SAVE_FORCED_TIMEOUT) {
        autoSaveTimer.cancel();
        autoSaveTimer.schedule(AUTO_SAVE_IDLE_TIMEOUT);
      }
    } else {
      // The auto-save timer is not already scheduled.
      // Schedule it now and set autoSaveRequestTime.
      autoSaveTimer.schedule(AUTO_SAVE_IDLE_TIMEOUT);
      autoSaveRequestTime = System.currentTimeMillis();
      autoSaveIsScheduled = true;
    }
  }

  /**
   * Saves all modified files and project settings and calls the afterSaving
   * command after they have all been saved successfully.
   *
   * If any errors occur while saving, the afterSaving command will not be
   * executed.
   * If nothing needs to be saved, the afterSavingFiles command is called
   * immediately, not asynchronously.
   *
   * @param afterSaving  optional command to be executed after project
   *                     settings and file editors are saved successfully
   */
  public void saveDirtyEditors(final Command afterSaving) {
    // Collect the files that need to be saved.
    List<FileDescriptorWithContent> filesToSave = new ArrayList<FileDescriptorWithContent>();
    for (FileEditor fileEditor : dirtyFileEditors) {
      FileDescriptorWithContent fileContent = new FileDescriptorWithContent(
          fileEditor.getProjectId(), fileEditor.getFileId(), fileEditor.getRawFileContent());
      filesToSave.add(fileContent);
    }
    dirtyFileEditors.clear();

    // Collect the project settings that need to be saved.
    List<ProjectSettings> projectSettingsToSave = new ArrayList<ProjectSettings>();
    projectSettingsToSave.addAll(dirtyProjectSettings);
    dirtyProjectSettings.clear();

    autoSaveTimer.cancel();
    autoSaveIsScheduled = false;

    // Keep count as each save operation finishes so we can set the projects' modified date and
    // call the afterSaving command after everything has been saved.
    // Each project settings is saved as a separate operation, but all files are saved as a single
    // save operation. So the initial value of pendingSaveOperations is the size of
    // projectSettingsToSave plus 1.
    final AtomicInteger pendingSaveOperations = new AtomicInteger(projectSettingsToSave.size() + 1);
    Command callAfterSavingCommand = new Command() {
      @Override
      public void execute() {
        if (pendingSaveOperations.decrementAndGet() == 0) {
          // Execute the afterSaving command if one was given.
          if (afterSaving != null) {
            afterSaving.execute();
          }
        }
      }
    };

    // Save all files at once (asynchronously).
    saveMultipleFilesAtOnce(filesToSave, callAfterSavingCommand);

    // Save project settings one at a time (asynchronously).
    for (ProjectSettings projectSettings : projectSettingsToSave) {
      projectSettings.saveSettings(callAfterSavingCommand);
    }
  }

  /**
   * Saves multiple files to the ODE server and calls the afterSavingFiles
   * command after they have all been saved successfully.
   *
   * If any errors occur while saving, the afterSavingFiles command will not be
   * executed.
   * If filesWithContent is empty, the afterSavingFiles command is called
   * immediately, not asynchronously.
   *
   * @param filesWithContent  the files that need to be saved
   * @param afterSavingFiles  optional command to be executed after file
   *                          editors are saved.
   */
  private void saveMultipleFilesAtOnce(
      final List<FileDescriptorWithContent> filesWithContent, final Command afterSavingFiles) {
    if (filesWithContent.isEmpty()) {
      // No files needed saving.
      // Execute the afterSavingFiles command if one was given.
      if (afterSavingFiles != null) {
        afterSavingFiles.execute();
      }

    } else {
      Ode.getInstance().getProjectService().save(filesWithContent,
          new OdeAsyncCallback<Long>(MESSAGES.saveErrorMultipleFiles()) {
        @Override
        public void onSuccess(Long date) {
          // Call the project editor's onSave method for each file that was saved and update the
          // project's modification date.
          for (FileDescriptorWithContent fileDescriptor : filesWithContent) {
            long projectId = fileDescriptor.getProjectId();
            ProjectEditor projectEditor = openProjectEditors.get(projectId);
            if (projectEditor != null) {
              projectEditor.onSave(fileDescriptor.getFileId());
            }
            Ode.getInstance().updateModificationDate(projectId, date);
          }

          // Execute the afterSavingFiles command if one was given.
          if (afterSavingFiles != null) {
            afterSavingFiles.execute();
          }
        }
      });
    }
  }
}
