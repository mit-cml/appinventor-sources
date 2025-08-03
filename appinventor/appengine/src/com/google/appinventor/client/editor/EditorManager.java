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
import com.google.common.collect.Maps;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Manager class for opened project editors.
 */
public final class EditorManager {
    private static final Logger LOG = Logger.getLogger(EditorManager.class.getName());

    // Map of project IDs to open project editors
    private final Map<Long, ProjectEditor> openProjectEditors;

    // Timeout (in ms) after which changed content is auto-saved if the user did not continue typing.
    private static final int AUTO_SAVE_IDLE_TIMEOUT = 5000;

    // Timeout (in ms) after which changed content is auto-saved even if the user continued typing.
    private static final int AUTO_SAVE_FORCED_TIMEOUT = 30000;

    // Fields used for saving and auto-saving.
    private final Set<ProjectSettings> dirtyProjectSettings;
    private final Set<FileEditor> dirtyFileEditors;
    private final Map<String, FileEditor> pendingFileEditors;
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
        dirtyProjectSettings = new HashSet<>();
        dirtyFileEditors = new HashSet<>();
        pendingFileEditors = new HashMap<>();

        autoSaveTimer = new Timer() {
            @Override
            public void run() {
                Ode.getInstance().lockScreens(true);
                saveDirtyEditors(() -> Ode.getInstance().lockScreens(false));
            }
        };
    }

    /**
     * Opens the project editor for the given project.
     * If there is an editor already open for the project, it will be returned.
     * Otherwise, it will create an appropriate editor for the project.
     *
     * @param projectRootNode the root node of the project to open
     * @return project editor for the given project
     */
    public ProjectEditor openProject(ProjectRootNode projectRootNode) {
        long projectId = projectRootNode.getProjectId();
        ProjectEditor projectEditor = openProjectEditors.get(projectId);
        if (projectEditor == null) {
            ProjectEditorFactory factory = Ode.getProjectEditorRegistry().get(projectRootNode);
            if (factory != null) {
                projectEditor = factory.createProjectEditor(projectRootNode);
                openProjectEditors.put(projectId, projectEditor);
                Ode.getInstance().getDesignToolbar().addProject(projectId, projectRootNode.getName());
                projectEditor.processProject();
            }
        }
        return projectEditor;
    }

    /**
     * Closes the file editors for the specified files, without saving.
     * This is used when the files are about to be deleted.
     *
     * @param projectId project ID
     * @param fileIds   file IDs of the file editors to be closed
     */
    public void closeFileEditors(long projectId, String[] fileIds) {
        ProjectEditor projectEditor = openProjectEditors.get(projectId);
        if (projectEditor != null) {
            Arrays.stream(fileIds).forEach(fileId -> {
                FileEditor fileEditor = projectEditor.getFileEditor(fileId);
                if (fileEditor != null) {
                    dirtyFileEditors.remove(fileEditor);
                }
            });
            projectEditor.closeFileEditors(fileIds);
        }
    }

    /**
     * Schedules auto-save of the given project settings.
     * This method can be called often, as the user is modifying project settings.
     *
     * @param projectSettings the project settings for which to schedule auto-save
     */
    public void scheduleAutoSave(ProjectSettings projectSettings) {
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
        if (!fileEditor.isDamaged()) {
            dirtyFileEditors.add(fileEditor);
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
        return !openProjectEditors.isEmpty();
    }

    /**
     * Schedules the auto-save timer.
     */
    private void scheduleAutoSaveTimer() {
        if (autoSaveIsScheduled) {
            if (System.currentTimeMillis() - autoSaveRequestTime < AUTO_SAVE_FORCED_TIMEOUT) {
                autoSaveTimer.cancel();
                autoSaveTimer.schedule(AUTO_SAVE_IDLE_TIMEOUT);
            }
        } else {
            autoSaveTimer.schedule(AUTO_SAVE_IDLE_TIMEOUT);
            autoSaveRequestTime = System.currentTimeMillis();
            autoSaveIsScheduled = true;
        }
    }

    /**
     * Saves all modified files and project settings and calls the afterSaving command after they have all been saved successfully.
     *
     * @param afterSaving optional command to be executed after project settings and file editors are saved successfully
     */
    public void saveDirtyEditors(final Command afterSaving) {
        if (Ode.getInstance().isReadOnly()) {
            afterSaving.execute();
            return;
        }

        List<FileDescriptorWithContent> filesToSave = new ArrayList<>();
        for (FileEditor fileEditor : dirtyFileEditors) {
            FileDescriptorWithContent fileContent = new FileDescriptorWithContent(
                fileEditor.getProjectId(), fileEditor.getFileId(), fileEditor.getRawFileContent());
            filesToSave.add(fileContent);
            pendingFileEditors.put(fileEditor.getFileId(), fileEditor);
        }
        dirtyFileEditors.clear();

        List<ProjectSettings> projectSettingsToSave = new ArrayList<>(dirtyProjectSettings);
        dirtyProjectSettings.clear();

        autoSaveTimer.cancel();
        autoSaveIsScheduled = false;

        final AtomicInteger pendingSaveOperations = new AtomicInteger(projectSettingsToSave.size() + 1);
        final DateHolder dateHolder = new DateHolder();
        Command callAfterSavingCommand = () -> {
            if (pendingSaveOperations.decrementAndGet() == 0) {
                pendingFileEditors.clear();
                if (afterSaving != null) {
                    afterSaving.execute();
                }
                if (dateHolder.date != 0 && dateHolder.projectId != 0) {
                    Ode.getInstance().updateModificationDate(dateHolder.projectId, dateHolder.date);
                }
            }
        };

        saveMultipleFilesAtOnce(filesToSave, callAfterSavingCommand, dateHolder);

        for (ProjectSettings projectSettings : projectSettingsToSave) {
            projectSettings.saveSettings(callAfterSavingCommand);
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

      Ode.getInstance().getProjectService().save(Ode.getInstance ().getSessionId(),
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
     * Saves multiple files at once in a single RPC transaction.
     *
     * @param filesWithContent the files that need to be saved
     * @param afterSavingFiles optional command to be executed after file editors are saved.
     * @param dateHolder holds the date of the last successful save
     */
    private void saveMultipleFilesAtOnce(
            final List<FileDescriptorWithContent> filesWithContent, final Command afterSavingFiles, final DateHolder dateHolder) {
        if (filesWithContent.isEmpty()) {
            if (afterSavingFiles != null) {
                afterSavingFiles.execute();
            }
        } else {
            for (FileDescriptorWithContent fileDescriptor : filesWithContent) {
                final long projectId = fileDescriptor.getProjectId();
                final String fileId = fileDescriptor.getFileId();
                final String content = fileDescriptor.getContent();
                Ode.CLog("Saving fileId " + fileId + " for projectId " + projectId);
                Ode.getInstance().getProjectService().save2(Ode.getInstance().getSessionId(),
                        projectId, fileId, false, content, new OdeAsyncCallback<Long>(MESSAGES.saveErrorMultipleFiles()) {
                            @Override
                            public void onSuccess(Long date) {
                                if (dateHolder.date != 0) {
                                    dateHolder.date = date;
                                    dateHolder.projectId = projectId;
                                }
                                if (afterSavingFiles != null) {
                                    afterSavingFiles.execute();
                                }
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                if (caught instanceof BlocksTruncatedException) {
                                    Ode.getInstance().blocksTruncatedDialog(projectId, fileId, content, this);
                                } else {
                                    if (pendingFileEditors.containsKey(fileId)) {
                                        dirtyFileEditors.add(pendingFileEditors.get(fileId));
                                    }
                                    super.onFailure(caught);
                                }
                                if (afterSavingFiles != null) {
                                    afterSavingFiles.execute();
                                }
                            }
                        });
            }
        }
    }
}
