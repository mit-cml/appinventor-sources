// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

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
  private static final Logger LOG = Logger.getLogger(ProjectEditor.class.getName());

  protected final ProjectRootNode projectRootNode;
  protected final UiStyleFactory uiFactory;
  protected final long projectId;
  protected final Project project;

  // Invariants: openFileEditors, fileIds, and deckPanel contain corresponding
  // elements, i.e., if a FileEditor is in openFileEditors, its fileid should be
  // in fileIds and the FileEditor should be in deckPanel. If selectedFileEditor
  // is non-null, it is one of the file editors in openFileEditors and the 
  // one currently showing in deckPanel. 
  private final Map<String, FileEditor> openFileEditors;
  protected final List<String> fileIds; 
  private final HashMap<String,String> locationHashMap = new HashMap<String,String>();
  private final DeckPanel deckPanel;
  private FileEditor selectedFileEditor;
  private final TreeMap<String, Boolean> screenHashMap = new TreeMap<String, Boolean>();

  /**
   * Creates a {@code ProjectEditor} instance.
   *
   * @param projectRootNode  the project root node
   */
  public ProjectEditor(ProjectRootNode projectRootNode, UiStyleFactory uiFactory) {
    this.projectRootNode = projectRootNode;
    this.uiFactory = uiFactory;
    projectId = projectRootNode.getProjectId();
    project = Ode.getInstance().getProjectManager().getProject(projectId);

    openFileEditors = Maps.newHashMap();
    fileIds = new ArrayList<String>();

    deckPanel = new DeckPanel();

    deckPanel.setSize("100%", "100%");
    initWidget(deckPanel);
    // Note: I'm not sure that the setSize call below does anything useful.
    setSize("100%", "100%");
  }

  /**
   * Processes the project before loading into the project editor.
   * To do any any pre-processing of the Project
   * Calls the loadProject() after prepareProject() is fully executed.
   * Currently, prepareProject loads all external components associated with project.
   */
  public abstract void processProject();

  /**
   * Called when the ProjectEditor widget is loaded after having been hidden. 
   * Subclasses must implement this method, taking responsibility for causing 
   * the onShow method of the selected file editor to be called and for updating 
   * any other UI elements related to showing the project editor.
   */
  protected abstract void onShow();
  
  /**
   * Called when the ProjectEditor widget is about to be unloaded. Subclasses
   * must implement this method, taking responsibility for causing the onHide 
   * method of the selected file editor to be called and for updating any 
   * other UI elements related to hiding the project editor.
   */
  protected abstract void onHide();

  public UiStyleFactory getUiFactory() {
    return uiFactory;
  }

  public final void setScreenCheckboxState(String screen, Boolean isChecked) {
    screenHashMap.put(screen, isChecked);
    changeProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP,
        getScreenCheckboxMapString()
    );
  }

  public final Boolean getScreenCheckboxState(String screen) {
    if (screenHashMap.size() == 0) {
      buildScreenHashMap();
    }
    return screenHashMap.get(screen);
  }

  public final String getScreenCheckboxMapString() {
    String screenCheckboxMap = "";
    int count = 0;
    Set<String> screens = screenHashMap.keySet();
    int size = screens.size();
    for (String screen : screens) {
      Boolean isChecked = screenHashMap.get(screen);
      if (isChecked == null) {
        continue;
      }
      String isCheckedString = (isChecked) ? "True" : "False";
      String separator = (count == size) ? "" : " ";
      screenCheckboxMap += screen + ":" + isCheckedString + separator;
    }
    return screenCheckboxMap;
  }

  public final void buildScreenHashMap() {
    String screenCheckboxMap = getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_SCREEN_CHECKBOX_STATE_MAP
    );
    String[] pairs = screenCheckboxMap.split(" ");
    for (String pair : pairs) {
      String[] mapping = pair.split(":");
      String screen = mapping[0];
      Boolean isChecked = Boolean.parseBoolean(mapping[1]);
      screenHashMap.put(screen, isChecked);
    }
  }

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
    LOG.info("Inserted file editor for " + fileEditor.getFileId() + " at pos " + beforeIndex);

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
        LOG.warning("Can't find widget for fileEditor " + fileEditor.getFileId());
      } else {
        LOG.warning("Not expecting selectFileEditor(null)");
      }
    }
    LOG.info("ProjectEditor: got selectFileEditor for "
        + ((fileEditor == null) ? null : fileEditor.getFileId())
        +  " selectedFileEditor is " 
        + ((selectedFileEditor == null) ? null : selectedFileEditor.getFileId()));
    if (selectedFileEditor != null && selectedFileEditor != fileEditor) {
      selectedFileEditor.onHide();
    }
    // Note that we still want to do the following statements even if 
    // selectedFileEditor == fileEditor already. This handles the case of switching back
    // to a previously opened project from another project.
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
   * Returns the set of open file editors
   */
  public final Iterable<FileEditor> getOpenFileEditors() {
    return Collections.unmodifiableCollection(openFileEditors.values());
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
        LOG.severe("File editor is unexpectedly null for " + fileId);
        continue;
      }
      int index = deckPanel.getWidgetIndex(fileEditor);
      fileIds.remove(index);
      deckPanel.remove(fileEditor);
      if (selectedFileEditor == fileEditor) {
        selectedFileEditor.onHide();
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
      LOG.info("ProjectEditor: changeProjectSettingsProperty: " + name + " " + currentValue +
                 " => " + newValue);
      settings.changePropertyValue(name, newValue);
      // Deal with the Tutorial Panel
      Ode ode = Ode.getInstance();
      if (name.equals("TutorialURL")) {
        ode.setTutorialURL(newValue);
      }
      ode.getEditorManager().scheduleAutoSave(projectSettings);
    }
  }

  /**
   * Keep track of components that require the
   * "android.permission.ACCESS_FINE_LOCATION" (and related
   * permissions). This code is in particular for use of the WebViewer
   * component. The WebViewer exports the Javascript location
   * API. However it cannot be used by an app with location
   * permissions. Each WebViewer has a "UsesLocation" property which
   * is only available from the designer. Each WebViewer then
   * registers its value here. Each time this hashtable is updated we
   * recompute whether or not location permission is needed based on a
   * logical OR of all of the WebViewer components registered. Note:
   * Even if no WebViewer component requires location permission, other
   * components, such as the LocationSensor may require it. That is
   * handled via the @UsesPermissions mechanism and is independent of
   * this code.
   *
   * @param componentName The name of the component registering location permission
   * @param newValue either "True" or "False" indicating whether permission is need.
   */

  public final void recordLocationSetting(String componentName, String newValue) {
    LOG.info("ProjectEditor: recordLocationSetting(" + componentName + "," + newValue + ")");
    locationHashMap.put(componentName, newValue);
    recomputeLocationPermission();
  }

  private void recomputeLocationPermission() {
    String usesLocation = "False";
    for (String c : locationHashMap.values()) {
      LOG.info("ProjectEditor:recomputeLocationPermission: " + c);
      if (c.equals("True")) {
        usesLocation = "True";
        break;
      }
    }
    changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS, SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION,
      usesLocation);
  }

  public void clearLocation(String componentName) {
    LOG.info("ProjectEditor:clearLocation: clearing " + componentName);
    locationHashMap.remove(componentName);
    recomputeLocationPermission();
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
    LOG.info("ProjectEditor: got onLoad for project " + projectId);
    super.onLoad();
    String tutorialURL = getProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                                                    SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
    if (!tutorialURL.isEmpty()) {
      Ode ode = Ode.getInstance();
      ode.setTutorialURL(tutorialURL);
    }

    onShow();
  }

  @Override
  protected void onUnload() {
    // onUnload is called immediately before a widget becomes detached from the browser's document.
    Ode ode = Ode.getInstance();
    ode.setTutorialVisible(false);
    ode.getDesignToolbar().setTutorialToggleVisible(false);
    LOG.info("ProjectEditor: got onUnload for project " + projectId);
    super.onUnload();
    onHide();
  }
}
