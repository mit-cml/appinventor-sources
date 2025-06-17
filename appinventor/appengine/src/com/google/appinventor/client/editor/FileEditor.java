// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import com.google.appinventor.client.AssetManager;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import java.util.logging.Logger;

/**
 * Abstract superclass for all file editors.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class FileEditor extends Composite {
  private static final Logger LOG = Logger.getLogger(FileEditor.class.getName());

  // The project editor that contains this file editor.
  protected final ProjectEditor projectEditor;

  // FileNode associated with this file editor
  protected final FileNode fileNode;

  private boolean damaged = false;

  /**
   * Creates a {@code FileEditor} instance.
   *
   * @param projectEditor  the project editor that contains this file editor
   * @param fileNode  FileNode associated with this file editor
   */
  public FileEditor(ProjectEditor projectEditor, FileNode fileNode) {
    this.projectEditor = projectEditor;
    this.fileNode = fileNode;
  }

  /**
   * Returns the project editor associated with this file editor.
   *
   * @return  project editor associated with this file editor
   */
  public final ProjectEditor getProjectEditor() {
    return projectEditor;
  }


  /**
   * Returns the drag drop targets for the gui
   *
   * @return  project editor associated with this file editor
   */
  public abstract DropTargetProvider getDropTargetProvider();

  /**
   * Returns the project ID associated with this file editor.
   *
   * @return  project ID associated with this file editor
   */
  public final long getProjectId() {
    return fileNode.getProjectId();
  }

  /**
   * Returns the project root node associated with this file editor.
   *
   * @return  project root node associated with this file editor
   */
  public final ProjectRootNode getProjectRootNode() {
    return fileNode.getProjectRoot();
  }

  /**
   * Returns the file ID associated with this file editor.
   *
   * @return  file ID associated with this file editor
   */
  public final String getFileId() {
    return fileNode.getFileId();
  }

  /**
   * Returns the file node associated with this file editor.
   *
   * @return  file node associated with this file editor
   */
  public final FileNode getFileNode() {
    return fileNode;
  }

  /**
   * Loads the content of the file into the editor.
   *
   * @param afterFileLoaded  optional command to be executed after the file has
   *                         been loaded
   */
  public abstract void loadFile(Command afterFileLoaded);

  /**
   * Returns the text that should appear on the tab for this file editor.
   */
  public abstract String getTabText();

  /**
   * Called when the FileEditor is about to be shown. Calls
   * Ode.getInstance().setCurrentFileEditor(this). The subclass should do anything else necessary
   * to get its UI set up after calling super.onShow().
   */
  public void onShow() {
    Ode.getInstance().setCurrentFileEditor(this);
  }

  /**
   * Called when the FileEditor is about to be hidden. Calls
   * Ode.getInstance().setCurrentFileEditor(null). The subclass should do anything else necessary
   * to get its UI cleaned up after calling super.onHide().
   */
  public void onHide() {
    // When an editor is detached, if we are the "current" editor,
    // set the current editor to null.
    // Note: I'm not sure it is possible that we would not be the "current"
    // editor when this is called, but we check just to be safe.
    if (Ode.getInstance().getCurrentFileEditor() == this) {
      Ode.getInstance().setCurrentFileEditor(null);
    }
  }

  /**
   * Returns true if the current editor is shown, that is, only if the designer
   * view is visible and this editor is the current file editor.
   * @return true if the editor is shown, otherwise false.
   */
  @SuppressWarnings("unused")  // called from JSNI
  public boolean isActiveEditor() {
    return Ode.getInstance().getCurrentView() == 0 &&
        Ode.getInstance().getCurrentFileEditor() == this;
  }

  /**
   * Called when the FileEditor is about to be closed. Subclasses can override this
   * to remove themselves as listeners.
   */
  public void onClose() {
  }

  /**
   * Called to trigger the Repl (Wireless) to start. This version does nothing
   * but the YaBlocksEditor overrides this version with one that start the
   * Repl going.
   */
  public void startRepl(boolean alreadyRunning, boolean forChromebook, boolean forEmulator, boolean forUsb) {
  }

  /**
   * Gets the raw content of the file associated with the editor.
   * This method is used when the EditorManager saves modified files.
   */
  public abstract String getRawFileContent();

  /**
   * Invoked after a save operation completes successfully.
   */
  public abstract void onSave();

  public void setDamaged(boolean damaged) {
    this.damaged = damaged;
  }

  public boolean isDamaged() {
    return damaged;
  }

  /**
   * Trigger and Update of the Companion.
   *
   */

  public void updateCompanion() {
  }

  public void getBlocksImage(Callback<String, String> callback) {
  }

  /**
   * Make the workspace managed by the file editor the active workspace.
   * This is called on a YaBlocksEditor to transition between screens when working with the
   * companion.
   */
  public void makeActiveWorkspace() {
  }

  /**
   * YaBlockEditor will use Blockly.hideChaff to close tooltips, context menus etc.
   */
  public void hideChaff() {
  }

  /**
   * Resize the editor in response to an external event.
   */
  public void resize() {
  }
}
