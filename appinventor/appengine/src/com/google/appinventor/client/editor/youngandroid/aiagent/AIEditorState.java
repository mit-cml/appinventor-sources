// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.DesignProject;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.Screen;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

import java.util.Map;

/**
 * Provides static access to the current editor state (project, form editor,
 * blocks editor) for the AI agent module.
 */
public final class AIEditorState {

  private AIEditorState() {}

  public static DesignProject getCurrentProject() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    return toolbar.getCurrentProject();
  }

  public static YaFormEditor getCurrentFormEditor() {
    DesignProject project = getCurrentProject();
    if (project == null) {
      return null;
    }
    Screen screen = project.screens.get(project.currentScreen);
    if (screen == null) {
      return null;
    }
    FileEditor editor = screen.designerEditor;
    if (editor instanceof YaFormEditor) {
      return (YaFormEditor) editor;
    }
    return null;
  }

  public static YaBlocksEditor getCurrentBlocksEditor() {
    DesignProject project = getCurrentProject();
    if (project == null) {
      return null;
    }
    Screen screen = project.screens.get(project.currentScreen);
    if (screen == null) {
      return null;
    }
    FileEditor editor = screen.blocksEditor;
    if (editor instanceof YaBlocksEditor) {
      return (YaBlocksEditor) editor;
    }
    return null;
  }

  /**
   * Returns true if a component with the given name exists on the current screen.
   */
  public static boolean componentExists(String name) {
    YaFormEditor formEditor = getCurrentFormEditor();
    if (formEditor == null) {
      return false;
    }
    Map<String, ?> components = formEditor.getComponents();
    return components.containsKey(name);
  }

  /**
   * Returns true if a screen with the given name exists in the project.
   */
  public static boolean screenExists(String screenName) {
    DesignProject project = getCurrentProject();
    if (project == null) {
      return false;
    }
    return project.screens.containsKey(screenName);
  }

  /**
   * Returns true if a block with the given YAIL identifier exists on the
   * current screen.  There is no efficient lookup for blocks by YAIL id in
   * the Blockly layer, so this always returns {@code false} — the executor
   * will attempt the operation and let it fail gracefully if the block is
   * not found.
   */
  public static boolean blockExists(String yailId) {
    // No clean block-existence query is available; return false so the
    // idempotency guard never skips a DELETE_BLOCK silently.
    return false;
  }
}
