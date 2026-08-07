// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.undo;

/**
 * Interface for undoable commands in the Designer panel.
 *
 * <p>Each command encapsulates a user action that can be undone and redone.
 * Commands are stored on the undo/redo stacks managed by {@link DesignerUndoManager}.
 */
public interface DesignerCommand {

  /**
   * Executes (or re-executes) this command.
   * Called when the command is first performed or when it is redone.
   */
  void execute();

  /**
   * Reverses this command.
   * Called when the user triggers an undo operation.
   */
  void undo();

  /**
   * Returns a human-readable description of this command,
   * e.g. "Change Button1.Text" or "Delete Label1".
   *
   * @return description string
   */
  String getDescription();
}
