// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass for all project editors.
 * Each ProjectEditor is associated with a single project and may have multiple
 * FileEditors open in a DeckPanel with a TabBar used to control which widget
 * in the DeckPanel is visible.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class ProjectEditor extends Composite {

  protected final ProjectRootNode projectRootNode;
  protected final long projectId;
  protected final Project project;

  protected final Map<String, FileEditor> openFileEditors;

  // UI elements
  private final TabBar tabBar;
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

    openFileEditors = new HashMap<String, FileEditor>();

    tabBar = new TabBar();
    deckPanel = new DeckPanel();

    tabBar.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      @Override
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        if (selectedFileEditor != null) {
          selectedFileEditor.onHide();
        }
      }
    });
    tabBar.addSelectionHandler(new SelectionHandler<Integer>() {
      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        int selectedIndex = event.getSelectedItem();
        if (selectedIndex >= 0 && selectedIndex < deckPanel.getWidgetCount()) {
          deckPanel.showWidget(selectedIndex);
          selectedFileEditor = (FileEditor) deckPanel.getWidget(selectedIndex);
          selectedFileEditor.onShow();
        } else {
          selectedFileEditor = null;
        }
      }
    });

    VerticalPanel panel = new VerticalPanel();
    if (!Ode.isProduction()) {
      // TODO(lizlooney) - Move the Add Screen button - maybe to the left of the Screens tabs (it
      // is current in ode/client/DesignToolbar.java)
      // TODO(lizlooney) - Consider how to deal with more Screens than fit across the Screens tab
      // bar
      // TODO(lizlooney) - Get the tab bar to look good in Chrome
      // TODO(lizlooney) - add more vertical space between tab bar and current screen info
      panel.add(tabBar);
    }
    panel.add(deckPanel);
    deckPanel.setSize("100%", "100%");
    panel.setSize("100%", "100%");
    initWidget(panel);
    setSize("100%", "100%");
  }

  /**
   * Loads the project into the project editor.
   * This may result in multiple FileEditors being added.
   */
  public abstract void loadProject();

  /**
   * Called when the ProjectEditor is about to be shown.
   */
  protected void onShow() {
    if (selectedFileEditor != null) {
      selectedFileEditor.onShow();
    }
  }

  /**
   * Called when the ProjectEditor is about to be hidden.
   */
  protected void onHide() {
    if (selectedFileEditor != null) {
      selectedFileEditor.onHide();
    }
  }

  /**
   * Adds a file editor panel to the editor.
   *
   * @param fileEditor  panel to add
   */
  public final void addFileEditor(FileEditor fileEditor) {
    String fileId = fileEditor.getFileId();
    openFileEditors.put(fileId, fileEditor);

    tabBar.addTab(fileEditor.getTabText());
    deckPanel.add(fileEditor);
  }

  /**
   * Inserts a file editor in this editor at the specified index.
   *
   * @param fileEditor  panel to insert
   * @param beforeIndex  the index before which fileEditor will be inserted
   */
  public final void insertFileEditor(FileEditor fileEditor, int beforeIndex) {
    String fileId = fileEditor.getFileId();
    openFileEditors.put(fileId, fileEditor);

    tabBar.insertTab(fileEditor.getTabText(), beforeIndex);
    deckPanel.insert(fileEditor, beforeIndex);
  }

  public final void selectFileEditor(FileEditor fileEditor) {
    if (fileEditor != selectedFileEditor) {
      tabBar.selectTab(deckPanel.getWidgetIndex(fileEditor), true);
    }
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
   * Closes the file editor for the given file ID, without saving.
   * This is used when the file is about to be deleted.
   *
   * @param fileId  file ID of the file to be closed
   */
  public final void closeFileEditor(String fileId) {
    FileEditor fileEditor = openFileEditors.remove(fileId);
    if (fileEditor != null) {
      tabBar.removeTab(deckPanel.getWidgetIndex(fileEditor));
      deckPanel.remove(fileEditor);
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
    super.onLoad();
    onShow();
  }

  @Override
  protected void onUnload() {
    // onUnload is called immediately before a widget becomes detached from the browser's document.
    super.onUnload();
    onHide();
  }
}
