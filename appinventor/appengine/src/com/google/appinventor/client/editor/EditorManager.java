// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.blocks.BlocksCodeGenerationException;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.project.FileDescriptorWithContent;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Manager class for opened project editors.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class EditorManager {
  private static final Logger LOG = Logger.getLogger(EditorManager.class.getName());

  // Map of project IDs to open project editors
  private final Map<Long, ProjectEditor> openProjectEditors;

  // Timeout (in ms) after which changed content is auto-saved if the user did
  // not continue typing.
  // TODO(user): Make this configurable.
  private static final int AUTO_SAVE_IDLE_TIMEOUT = 5000;
  // Currently set to 5 seconds. Note: the GWT code as a ClosingHandler
  // that will perform a save when the user closes the window.

  // Timeout (in ms) after which changed content is auto-saved even if the user
  // continued typing.
  // TODO(user): Make this configurable.
  private static final int AUTO_SAVE_FORCED_TIMEOUT = 30000;

  // Fields used for saving and auto-saving.
  /** Files waiting to be saved and files whose save has not answered yet. */
  private final PendingSaves<FileEditor> files;

  /** Project settings waiting to be saved and settings whose save has not answered yet. */
  private final PendingSaves<ProjectSettings> settings;
  private final Timer autoSaveTimer;
  private boolean autoSaveIsScheduled;
  private long autoSaveRequestTime;

  private class DateHolder {
    long date;
    long projectId;
  }

  /**
   * Creates the editor manager.
   */
  public EditorManager() {
    openProjectEditors = Maps.newHashMap();

    files = new PendingSaves<FileEditor>(new PendingSaves.Key<FileEditor>() {
      @Override
      public Object of(FileEditor fileEditor) {
        return fileEditor.getFileId();
      }
    });
    settings = new PendingSaves<ProjectSettings>(new PendingSaves.Key<ProjectSettings>() {
      @Override
      public Object of(ProjectSettings projectSettings) {
        return projectSettings;
      }
    });

    autoSaveTimer = new Timer() {
      @Override
      public void run() {
        // When the timer goes off, save everything that is waiting.
        Ode.getInstance().lockScreens(true); // Lock out changes
        saveDirtyEditors(new Command() {
            @Override
            public void execute() {
              Ode.getInstance().lockScreens(false); // I/O finished, unlock
            }
          });
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
        
        // Tell the DesignToolbar about this project
        Ode.getInstance().getDesignToolbar().addProject(projectId, projectRootNode.getName());

        // Prepare the project before Loading into the editor.
        // Components are prepared before the project is actually loaded.
        // Load the project into the editor. The actual loading is asynchronous.
        projectEditor.processProject();
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
   * Closes the file editors for the specified files, without saving.
   * This is used when the files are about to be deleted.
   *
   * @param projectId  project ID
   * @param fileIds  file IDs of the file editors to be closed
   */
  public void closeFileEditors(long projectId, String[] fileIds) {
    ProjectEditor projectEditor = openProjectEditors.get(projectId);
    if (projectEditor != null) {
      for (String fileId : fileIds) {
        FileEditor fileEditor = projectEditor.getFileEditor(fileId);
        // in case the file is not open in an editor (possible?) check 
        // the FileEditors for null. 
        if (fileEditor != null) {
          files.discard(fileEditor);
        }
      }
      projectEditor.closeFileEditors(fileIds);
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
    settings.discard(projectSettings);
    openProjectEditors.remove(projectId);
  }

  /**
   * Schedules auto-save of the given project settings.
   * This method can be called often, as the user is modifying project settings.
   *
   * @param projectSettings the project settings for which to schedule auto-save
   */
  public void scheduleAutoSave(ProjectSettings projectSettings) {
    // Note that the project settings need saving.
    settings.add(projectSettings);
    scheduleAutoSaveTimer();
  }

  /**
   * Schedules auto-save of the given file editor.
   * This method can be called often, as the user is modifying a file.
   *
   * @param fileEditor the file editor for which to schedule auto-save
   */
  public void scheduleAutoSave(FileEditor fileEditor) {
    // Note that the file editor needs saving.
    if (!fileEditor.isDamaged()) { // Don't save damaged files
      files.add(fileEditor);
    } else {
      LOG.info("Not saving blocks for " + fileEditor.getFileId() + " because it is damaged.");
    }
    scheduleAutoSaveTimer();
  }

  /**
   * Check whether there is an open project editor.
   *
   * @return true if at least one project is open (or in the process of opening), otherwise false
   */
  public boolean hasOpenEditor() {
    return openProjectEditors.size() > 0;
  }

  /**
   * Check whether any editor or project setting still holds changes that are not on the server.
   *
   * <p>The command given to {@link #saveDirtyEditors(Command)} runs once every save has finished,
   * whether it succeeded or not, and a file whose save failed is put back in the dirty set. Asking
   * this from that command is therefore how a caller tells a completed save apart from a failed
   * one before it acts on the saved content.
   *
   * @return true if something is still unsaved, otherwise false
   */
  /**
   * Puts settings whose save did not do what it was asked back in the queue.
   *
   * <p>The next save takes them, the way a failed file save is retried, and no timer is
   * started for them. Retrying a failure on a clock of its own would hammer a server that is
   * already in trouble, which the file path deliberately avoids, so the settings path avoids
   * it the same way.
   */
  public void settingsSaveFailed(ProjectSettings projectSettings) {
    settings.failed(projectSettings);
  }

  /**
   * How many completions {@link #saveDirtyEditors} waits for before it reports back.
   *
   * <p>Each project settings save is its own operation, and so is each file, because
   * {@link #saveMultipleFilesAtOnce} sends one request per file and answers the command once
   * for each of them. With no files to save it still answers once. Counting the files as a
   * single operation would report back while the rest were still in flight, and would clear
   * anything still waiting before a later failure could put its editor back.
   *
   * @param settingsCount how many project settings are being saved
   * @param fileCount how many files are being saved
   * @return the number of completions to wait for
   */
  @VisibleForTesting
  static int pendingSaveOperationCount(int settingsCount, int fileCount) {
    return settingsCount + (fileCount == 0 ? 1 : fileCount);
  }

  public boolean hasUnsavedChanges() {
    return !files.isEmpty() || !settings.isEmpty();
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
   * Saves what needs saving and runs the afterSaving command once every save it
   * sent has answered, whether it succeeded or not. What is still unsaved
   * afterwards is what {@link #hasUnsavedChanges} reports, which is how a
   * caller tells the two apart. With nothing to send it runs the command
   * immediately, not asynchronously.
   *
   * @param afterSaving  optional command to be executed once every save this
   *                     call sent has answered
   */
  public void saveDirtyEditors(final Command afterSaving) {
    // Note, We don't do any saving if we are in read only mode
    if (Ode.getInstance().isReadOnly()) {
      if (afterSaving != null) {
        afterSaving.execute();
      }
      return;
    }

    // Take everything that can be sent now. Anything whose previous save has not answered yet
    // is held back, because two saves of one thing can land in either order and the older one
    // landing last would undo the newer one.
    List<FileEditor> editorsToSave = files.take();
    final List<ProjectSettings> projectSettingsToSave = settings.take();
    List<FileDescriptorWithContent> filesToSave = new ArrayList<FileDescriptorWithContent>();
    for (FileEditor fileEditor : editorsToSave) {
      filesToSave.add(new FileDescriptorWithContent(
          fileEditor.getProjectId(), fileEditor.getFileId(), fileEditor.getRawFileContent()));
    }

    autoSaveTimer.cancel();
    autoSaveIsScheduled = false;
    if (files.heldBack() || settings.heldBack()) {
      // Something is waiting for a save that has not answered yet. Nothing else would come
      // back for it, since the timer only starts again when the learner types, so it is
      // started here and the next save takes what is waiting.
      scheduleAutoSaveTimer();
    }

    // Keep count as each save operation finishes so we can set the projects' modified date and
    // call the afterSaving command after everything has been saved.
    final AtomicInteger pendingSaveOperations = new AtomicInteger(
        pendingSaveOperationCount(projectSettingsToSave.size(), filesToSave.size()));
    final DateHolder dateHolder = new DateHolder();
    Command callAfterSavingCommand = new Command() {
      @Override
      public void execute() {
        if (pendingSaveOperations.decrementAndGet() == 0) {
          // We get here when all save operations have completed, either
          // with success or not. Each one has already said so for itself as it
          // finished, since clearing the whole map here would also drop the files of a save
          // that is still running, and a save started while another is in flight is ordinary.
          // Execute the afterSaving command if one was given.
          if (afterSaving != null) {
            afterSaving.execute();
          }
          // Set the project modification date to the returned date
          // for one of the saved files (it doesn't really matter which one).
          if ((dateHolder.date != 0) && (dateHolder.projectId != 0)) { // We have a date back from the server
            Ode.getInstance().updateModificationDate(dateHolder.projectId, dateHolder.date);
          }
        }
      }
    };

    // Save all files at once (asynchronously).
    saveMultipleFilesAtOnce(editorsToSave, filesToSave, callAfterSavingCommand, dateHolder);

    // Save project settings one at a time (asynchronously). Each completion first marks its
    // own save answered, or the settings would count as on their way forever and everything
    // waiting for the last save to finish would wait for good.
    for (ProjectSettings projectSettings : projectSettingsToSave) {
      projectSettings.saveSettings(
          PendingSaves.answering(settings, projectSettings, callAfterSavingCommand));
    }
  }
  
  /**
   * For each block editor (screen) in the current project, generate and save yail code for the 
   * blocks.
   *
   * @param successCommand  optional command to be executed if yail generation and saving succeeds.
   * @param failureCommand  optional command to be executed if yail generation and saving fails.
   */
  public void generateYailForBlocksEditors(final Command successCommand, 
      final Command failureCommand) {
    List<FileDescriptorWithContent> yailFiles =  new ArrayList<FileDescriptorWithContent>();
    long currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    for (long projectId : openProjectEditors.keySet()) {
      if (projectId == currentProjectId) {
        // Generate yail for each blocks editor in this project and add it to the list of 
        // yail files. If an error occurs we stop the generation process, report the error, 
        // and return without executing nextCommand.
        ProjectEditor projectEditor = openProjectEditors.get(projectId);
        for (FileEditor fileEditor : projectEditor.getOpenFileEditors()) {
          if (fileEditor instanceof YaBlocksEditor) {
            YaBlocksEditor yaBlocksEditor = (YaBlocksEditor) fileEditor;
            try {
              yailFiles.add(yaBlocksEditor.getYail());
            } catch (BlocksCodeGenerationException e) {
              ErrorReporter.reportInfo(MESSAGES.yailGenerationError(e.getEntityName(),
                  e.getMessage()));
              if (failureCommand != null) {
                failureCommand.execute();
              }
              return;
            }
          }
        }
        break;
      }
    }
   
    Ode.getInstance().getProjectService().save(Ode.getInstance().getSessionId(),
        yailFiles,
        new OdeAsyncCallback<Long>(MESSAGES.saveErrorMultipleFiles()) {
      @Override
      public void onSuccess(Long date) {
        if (successCommand != null) {
          successCommand.execute();
        }
      }
      
      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        if (failureCommand != null) {
          failureCommand.execute();
        }
      }
    });
  }


  /**
   * This code used to send the contents of all changed files to the server
   * in the same RPC transaction. However we are now sending them separately
   * so that we can have more fine grained control over handling errors that
   * happen only on one file. In particular, we need to handle the case where
   * a trivial blocks workspace is attempting to be written over a non-trivial
   * file.
   *
   * The afterSavingFiles command is executed once for every file, whether its
   * save succeeded or not, which is how the caller counts the batch down. If
   * filesWithContent is empty, the afterSavingFiles command is called
   * immediately, not asynchronously.
   *
   * @param filesWithContent  the files that need to be saved
   * @param afterSavingFiles  optional command to be executed after file
   *                          editors are saved.
   */
  private void saveMultipleFilesAtOnce(final List<FileEditor> editorsBeingSaved,
      final List<FileDescriptorWithContent> filesWithContent, final Command afterSavingFiles,
      final DateHolder dateHolder) {
    final Map<String, String> contentById = new HashMap<String, String>();
    for (FileDescriptorWithContent fileDescriptor : filesWithContent) {
      contentById.put(fileDescriptor.getFileId(), fileDescriptor.getContent());
    }
    if (filesWithContent.isEmpty()) {
      // No files needed saving.
      // Execute the afterSavingFiles command if one was given.
      if (afterSavingFiles != null) {
        afterSavingFiles.execute();
      }

    } else {
      for (final FileEditor fileEditor : editorsBeingSaved) {
        final long projectId = fileEditor.getProjectId();
        final String fileId = fileEditor.getFileId();
        final String content = contentById.get(fileId);
        Ode.CLog("Saving fileId " + fileId + " for projectId " + projectId);
        Ode.getInstance().getProjectService().save2(Ode.getInstance().getSessionId(),
          projectId, fileId, false, content, new OdeAsyncCallback<Long>(MESSAGES.saveErrorMultipleFiles()) {
            @Override
            public void onSuccess(Long date) {
              files.answered(fileEditor);
              if (dateHolder.date != 0) {
                // This sets the project modification time to that of one of
                // the successful file saves. It doesn't really matter which
                // file date we use, they will all be close. However it is important
                // to use some files date because that will be based on the server's
                // time. If we used the local clients time, then we may be off if the
                // client's computer's time isn't set correctly.
                dateHolder.date = date;
                dateHolder.projectId = projectId;
              }
              if (afterSavingFiles != null) {
                afterSavingFiles.execute();
              }
            }
            @Override
            public void onFailure(Throwable caught) {
              // Here is where we handle BlocksTruncatedException
              if (caught instanceof BlocksTruncatedException) {
                // The learner is being asked whether to save truncated blocks, and this same
                // callback answers again once they decide, so the attempt is not finished.
                // Counting it as finished here would let a submission go out while the
                // question is still on the screen, and would count this file twice.
                Ode.getInstance().blocksTruncatedDialog(projectId, fileId, content, this);
                return;
              } else {
                // We mark the file editor as dirty again because the save failed.
                //
                // Note: I considered re-scheduling the auto-save and decided against
                // it. One reason we might be getting errors is due to a problem with
                // the server. If a lot of clients start re-scheduling saves, this might
                // make the situation worse due to the "thundering Herd!" So we compromise
                // we mark the editors as dirty, so the next update by the user to any
                // file will retry all of the non-saved files. The "Save Project" menu
                // item will also re-attempt the failed I/O
                files.failed(fileEditor);
                super.onFailure(caught);
              }
              if (afterSavingFiles != null) { // Need to call this to decrement the count
                afterSavingFiles.execute();   // of files saved (or not in this case)
              }
            }
          });
      }
    }
  }
}
