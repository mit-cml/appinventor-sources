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
}
