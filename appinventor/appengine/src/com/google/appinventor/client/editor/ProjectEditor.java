// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.editor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.settings.Settings;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.client.settings.project.ProjectSettings;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

  private final Map<String, FileEditor> openFileEditors;
  protected final List<String> tabNames;  // tab names in the same order as the TabBar.

  private final HashMap<String,String> locationHashMap = new HashMap<String,String>();

  // UI elements
  private final TabBar tabBar;
  private final ScrollPanel tabBarScrollPanel;
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
    tabNames = new ArrayList<String>();

    tabBar = new TabBar();
    deckPanel = new DeckPanel();

    // Make the TabBar have a horizontal scroll bar if there are too many tabs.
    // This was much harder than I would have thought. If I set the ScrollPanel's width to 100% (or
    // I don't set the width at all), the TabBar doesn't actually get a scroll bar. It just gets
    // wider and wider in order to show all the tabs.
    tabBarScrollPanel = new ScrollPanel(tabBar);
    // Initially, set the ScrollPanel's width to 350 pixels, which is a reasonable minimum size.
    tabBarScrollPanel.setWidth("350px");
    final Timer timer = new Timer() {
      @Override
      public void run() {
        int width = deckPanel.getOffsetWidth();
        if (width > 0) {
          // Set the tabBarScrollPanel's width to the same as the DeckPanel.
          tabBarScrollPanel.setWidth(width + "px");
        } else {
          // If the DeckPanel's width is 0, try again in 100 millis.
          schedule(100);
        }
      }
    };
    tabBarScrollPanel.addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached()) {
          // When the ScrollPanel is attached, call the timer's run method directly which will set
          // the ScrollPanel's width to match the DeckPanel's width, deferring if necessary if the
          // DeckPanel's width is still 0.
          timer.run();
        }
      }
    });
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        // Cancel the previously scheduled timer. We only want the timer to go off after the user
        // has finished resizing.
        timer.cancel();

        // While the user is resizing, set the ScrollPanel's width to a reasonable minimum size.
        tabBarScrollPanel.setWidth("350px");

        // Reset the timer.
        timer.schedule(500);
      }
    });

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
    panel.add(tabBarScrollPanel);
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
   * Adds a file editor to this project editor.
   *
   * @param fileEditor  file editor to add
   */
  public final void addFileEditor(FileEditor fileEditor) {
    String fileId = fileEditor.getFileId();
    openFileEditors.put(fileId, fileEditor);

    String tabText = fileEditor.getTabText();
    tabNames.add(tabText);
    tabBar.addTab(tabText);
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

    String tabText = fileEditor.getTabText();
    tabNames.add(beforeIndex, tabText);
    tabBar.insertTab(tabText, beforeIndex);
    deckPanel.insert(fileEditor, beforeIndex);
  }

  /**
   * Selects the given file editor.
   *
   * @param fileEditor  file editor to select
   */
  public final void selectFileEditor(FileEditor fileEditor) {
    if (fileEditor != selectedFileEditor) {
      int index = deckPanel.getWidgetIndex(fileEditor);
      tabBar.selectTab(index, true);
      TabBar.Tab tab = tabBar.getTab(index);
      if (tab instanceof UIObject) {
        tabBarScrollPanel.ensureVisible((UIObject) tab);
      }
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
      int index = deckPanel.getWidgetIndex(fileEditor);
      tabNames.remove(index);
      tabBar.removeTab(index);
      deckPanel.remove(fileEditor);

      // Select the editor that is just before this one.
      if (index > 0) {
        index--;
      }
      tabBar.selectTab(index, true);
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
   * Keep track of components that require the
   * "android.permission.ACCESS_FINE_LOCATION" (and related
   * permissions). This code is in particular for use of the WebViewer
   * component. The WebViewer exports the Javascript location
   * API. However it cannot be used by an app with location
   * permissions. Each WebViewer has a "UsesLocation" property which
   * is only available from the designer. Each WebViewer then
   * registers its value here. Each time this hashtable is updated we
   * recompute whether or not location permission is needed based on a
   * logical OR of all of the WebViwer components registered. Note:
   * Even if no WebViewer component requires location permisson, other
   * components, such as the LocationSensor may require it. That is
   * handled via the @UsesPermissions mechanism and is independent of
   * this code.
   *
   * @param componentName The name of the component registering location permission
   * @param newVlue either "True" or "False" indicating whether permission is need.
   */

  public final void recordLocationSetting(String componentName, String newValue) {
    OdeLog.log("ProjectEditor: recordLocationSetting(" + componentName + "," + newValue + ")");
    locationHashMap.put(componentName, newValue);
    recomputeLocationPermission();
  }

  private final void recomputeLocationPermission() {
    String usesLocation = "False";
    for (String c : locationHashMap.values()) {
      OdeLog.log("ProjectEditor:recomputeLocationPermission: " + c);
      if (c.equals("True")) {
        usesLocation = "True";
        break;
      }
    }
    changeProjectSettingsProperty(SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS, SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION,
      usesLocation);
  }

  public void clearLocation(String componentName) {
    OdeLog.log("ProjectEditor:clearLocation: clearing " + componentName);
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
