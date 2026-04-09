// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.executor;

import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;

import java.util.Map;

/**
 * Carries target screen editors through the execution pipeline.
 * Replaces static AIEditorState lookups for screen-targeted operations.
 * For the current (visible) screen, use {@link #forCurrentScreen()}.
 * For background screens, construct with explicit editors from YaProjectEditor.editorMap.
 */
public class ScreenExecutionContext {
  private final String screenName;
  private final YaFormEditor formEditor;
  private final YaBlocksEditor blocksEditor;

  public ScreenExecutionContext(String screenName,
      YaFormEditor formEditor, YaBlocksEditor blocksEditor) {
    this.screenName = screenName;
    this.formEditor = formEditor;
    this.blocksEditor = blocksEditor;
  }

  /** Creates a context for the currently visible screen (backward compat). */
  public static ScreenExecutionContext forCurrentScreen() {
    return new ScreenExecutionContext(
        null,
        AIEditorState.getCurrentFormEditor(),
        AIEditorState.getCurrentBlocksEditor());
  }

  public String getScreenName() { return screenName; }
  public YaFormEditor getFormEditor() { return formEditor; }
  public YaBlocksEditor getBlocksEditor() { return blocksEditor; }

  /** Whether this targets the currently visible screen (screenName is null). */
  public boolean isCurrentScreen() { return screenName == null; }

  /**
   * Returns true if a component with the given name exists on this context's screen.
   */
  public boolean componentExists(String name) {
    if (formEditor == null) {
      return false;
    }
    Map<String, ?> components = formEditor.getComponents();
    return components.containsKey(name);
  }

  /**
   * Returns true if a block with the given YAIL identifier exists on this
   * context's screen.
   */
  public boolean blockExists(String yailId) {
    if (blocksEditor == null) {
      // Can't check — assume the block exists so the delete is attempted.
      return true;
    }
    return blocksEditor.blockExists(yailId);
  }

  /**
   * Returns true if a screen with the given name exists in the project.
   * Screen existence is project-wide, not specific to this context's screen.
   */
  public boolean screenExists(String name) {
    return AIEditorState.screenExists(name);
  }
}
