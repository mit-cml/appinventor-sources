// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract superclass for all project editors.
 * Each ProjectEditor is associated with a single project and may have multiple
 * FileEditors open in a DeckPanel.
 * 
 * TODO(sharon): consider merging this into YaProjectEditor, since we now
 * only have one type of project editor. 
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class ProjectEditor extends Composite {

  protected final ProjectRootNode projectRootNode;
  protected final long projectId;
  protected final Project project;

  // Invariants: openFileEditors, fileIds, and deckPanel contain corresponding
  // elements, i.e., if a FileEditor is in openFileEditors, its fileid should be
  // in fileIds and the FileEditor should be in deckPanel. If selectedFileEditor
  // is non-null, it is one of the file editors in openFileEditors and the 
  // one currently showing in deckPanel. 
  private final Map<String, FileEditor> openFileEditors;
  protected final List<String> fileIds; 
  private final DeckPanel deckPanel;
  private FileEditor selectedFileEditor;

  /**
   * Creates a {@code ProjectEditor} instance.
   *
   * @param projectRootNode  the project root node
   */
  public ProjectEditor(ProjectRootNode projectRootNode) {
    this.projectRootNode = projectRootNode;
    projectId = projectRootNode.getProjectId();
    project = Ode.getInstance().getProjectManager().getProject(projectId);

    openFileEditors = Maps.newHashMap();
    fileIds = new ArrayList<String>();

    deckPanel = new DeckPanel();

    VerticalPanel panel = new VerticalPanel();
    panel.add(deckPanel);
    deckPanel.setSize("100%", "100%");
    panel.setSize("100%", "100%");
    initWidget(panel);
    // Note: I'm not sure that the setSize call below does anything useful.
    setSize("100%", "100%");
  }

  /**
   * Loads the project into the project editor.
   * This may result in multiple FileEditors being added.
   */
  public abstract void loadProject();
  
  /**
   * Called when the ProjectEditor widget is loaded after having been hidden. 
   * Subclasses must implement this method, taking responsiblity for causing 
   * the onShow method of the selected file editor to be called and for updating 
   * any other UI elements related to showing the file editor.
   */
  protected abstract void onShow();
  
  /**
   * Called when the ProjectEditor widget is about to be unloaded. Subclasses
   * must implement this method, taking responsiblity for causing the onHide 
   * method of the selected file editor to be called and for updating any 
   * other UI elements related to hiding the file editor.
   */
  protected abstract void onHide();

  /**
   * Adds a file editor to this project editor.
   *
   * @param fileEditor  file editor to add
   */
  public final void addFileEditor(FileEditor fileEditor) {
    String fileId = fileEditor.getFileId();
    openFileEditors.put(fileId, fileEditor);
    fileIds.add(fileId);
    
    deckPanel.add(fileEditor);
  }

  /**
   * Inserts a file editor in this editor at the specified index.
   *
   * @param fileEditor  file editor to insert
   * @param beforeIndex  the index before which fileEditor will be inserted
   */
  public final void insertFileEditor(FileEditor fileEditor, int beforeIndex) {
    String fileId = fileEditor.getFileId();
    openFileEditors.put(fileId, fileEditor);
    fileIds.add(beforeIndex, fileId);
    deckPanel.insert(fileEditor, beforeIndex);
    OdeLog.log("Inserted file editor for " + fileEditor.getFileId() + " at pos " + beforeIndex);

  }

  /**
   * Selects the given file editor in the deck panel and calls its onShow()
   * method. Calls onHide() for a previously selected file editor if there was 
   * one (and it wasn't the same one).
   * 
   * Note: all actions that cause the selected file editor to change should
   * be going through DesignToolbar.SwitchScreenAction.execute(), which calls
   * this method. If you're thinking about calling this method directly from 
   * somewhere else, please reconsider!
   *
   * @param fileEditor  file editor to select
   */
  public final void selectFileEditor(FileEditor fileEditor) {
    int index = deckPanel.getWidgetIndex(fileEditor);
    if (index == -1) {
      if (fileEditor != null) {
        OdeLog.wlog("Can't find widget for fileEditor " + fileEditor.getFileId());
      } else {
        OdeLog.wlog("Not expecting selectFileEditor(null)");
      }
    }
    OdeLog.log("ProjectEditor: got selectFileEditor for " 
        + ((fileEditor == null) ? null : fileEditor.getFileId())
        +  " selectedFileEditor is " 
        + ((selectedFileEditor == null) ? null : selectedFileEditor.getFileId()));
    if (selectedFileEditor != null && selectedFileEditor != fileEditor) {
      selectedFileEditor.onHide();
    }
    selectedFileEditor = fileEditor;
    deckPanel.showWidget(index);
    selectedFileEditor.onShow();
  }

  /**
   * Returns the file editor for the given file ID.
   *
   * @param fileId  file ID of the file
   */
  public final FileEditor getFileEditor(String fileId) {
    return openFileEditors.get(fileId);
  }
  
  /**
   * Returns the currently selected file editor
   */
  protected final FileEditor getSelectedFileEditor() {
    return selectedFileEditor;
  }

  /**
   * Closes the file editors for the given file IDs, without saving.
   * This is used when the files are about to be deleted. If  
   * selectedFileEditor is closed, sets selectedFileEditor to null.
   *
   * @param closeFileIds  file IDs of the files to be closed
   */
  public final void closeFileEditors(String[] closeFileIds) {
    for (String fileId : closeFileIds) {
      FileEditor fileEditor = openFileEditors.remove(fileId);
      if (fileEditor == null) {
        OdeLog.elog("File editor is unexpectedly null for " + fileId);
        continue;
      }
      int index = deckPanel.getWidgetIndex(fileEditor);
      fileIds.remove(index);
      deckPanel.remove(fileEditor);
      if (selectedFileEditor == fileEditor) {
        selectedFileEditor = null;
      }
      fileEditor.onClose();
    }
  }
  
  /**
   * Returns the value of a project settings property.
   *
   * @param category  property category
   * @param name  property name
   * @return the property value
   */
  public final String getProjectSettingsProperty(String category, String name) {
    ProjectSettings projectSettings = project.getSettings();
    Settings settings = projectSettings.getSettings(category);
    return settings.getPropertyValue(name);
  }

  /**
   * Changes the value of a project settings property.
   *
   * @param category  property category
   * @param name  property name
   * @param newValue  new property value
   */
  public final void changeProjectSettingsProperty(String category, String name, String newValue) {
    ProjectSettings projectSettings = project.getSettings();
    Settings settings = projectSettings.getSettings(category);
    String currentValue = settings.getPropertyValue(name);
    if (!newValue.equals(currentValue)) {
      settings.changePropertyValue(name, newValue);
      Ode.getInstance().getEditorManager().scheduleAutoSave(projectSettings);
    }
  }

  /**
   * Notification that the file with the given file ID has been saved.
   *
   * @param fileId  file ID of the file that was saved
   */
  public final void onSave(String fileId) {
    FileEditor fileEditor = openFileEditors.get(fileId);
    if (fileEditor != null) {
      fileEditor.onSave();
    }
  }

  // GWT Widget methods

  @Override
  protected void onLoad() {
    // onLoad is called immediately after a widget becomes attached to the browser's document.
    // onLoad will be called both when a project is opened the first time and when an
    // already-opened project is re-opened.
    // This is different from the ProjectEditor method loadProject, which is called to load the
    // project just after the editor is created.
    OdeLog.log("ProjectEditor: got onLoad for project " + projectId);
    super.onLoad();

    onShow();
  }

  @Override
  protected void onUnload() {
    // onUnload is called immediately before a widget becomes detached from the browser's document.
    OdeLog.log("ProjectEditor: got onUnload for project " + projectId);
    super.onUnload();
    onHide();
  }
}
