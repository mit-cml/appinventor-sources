// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.MultiRegistry;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.rpc.project.ProjectNode;

import java.util.Collections;
import java.util.List;

/**
 * Registry of context menu commands.
 *
 * The {@link com.google.appinventor.client.explorer.youngandroid.AssetList} uses the
 * registry to populate its context menu.
 *
 */
public class CommandRegistry extends MultiRegistry<ProjectNode, CommandRegistry.Entry> {
  /**
   * An entry in the command registry. Commands are wrapped in this class to
   * store their insertion order.
   */
  public static class Entry implements Comparable<Entry> {
    // The position field defines the natural order of entries according to
    // their creation.
    private static int nextPosition = 0;
    private final int position = nextPosition++;

    // the command
    private final ProjectNodeCommand command;

    /**
     * Creates a new entry.
     *
     * @param command the command
     */
    private Entry(ProjectNodeCommand command) {
      this.command = command;
    }

    /**
     * @return the command
     */
    public ProjectNodeCommand getCommand() {
      return command;
    }

    public int compareTo(Entry other) {
      return position - other.position;
    }
  }

  /**
   * Creates a command registry
   */
  public CommandRegistry() {
    super(ProjectNode.class);

    // Files
    registerCommand(FileNode.class, new ProjectNodeCommand(MESSAGES.deleteFileCommand(),
        Tracking.PROJECT_ACTION_DELETE_FILE_YA, new DeleteFileCommand()));
    registerCommand(FileNode.class, new ProjectNodeCommand(MESSAGES.downloadFileCommand(),
        Tracking.PROJECT_ACTION_DOWNLOAD_FILE_YA, new DownloadFileCommand()));
  }

  /**
   * Registers a new command for the given class.
   *
   * @param clazz the class for which to register the command
   * @param command the command
   */
  private void registerCommand(Class<? extends ProjectNode> clazz, ProjectNodeCommand command) {
    register(clazz, new Entry(command));
  }

  /**
   * {@inheritDoc}
   *
   * This implementation returns the entries according to their insertion order.
   */
  @Override
  public List<Entry> get(Class<? extends ProjectNode> key) {
    List<Entry> result = super.get(key);
    Collections.sort(result);
    return result;
  }
}
