// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.shared.rpc.project.ProjectNode;

/**
 * Class for UI commands that can be added to the context menu of a
 * {@link ProjectNode}.
 *
 * The {@link com.google.appinventor.client.explorer.youngandroid.AssetList} uses
 * ProjectNodeCommand in its context menu.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class ProjectNodeCommand {
  // The label of the command.
  private final String label;

  // The action name for tracking purposes.
  private final String actionName;

  // The chainable command to be executed when this command is selected from
  // the context menu.
  private final ChainableCommand command;

  /**
   * Creates a new command.
   *
   * @param label the label of the command
   * @param actionName the action name of the command
   * @param command the chainable command to be executed
   */
  public ProjectNodeCommand(String label, String actionName, ChainableCommand command) {
    this.label = label;
    this.actionName = actionName;
    this.command = command;
  }

  /**
   * Returns the command label.
   *
   * @return the command label
   */
  public String getLabel() {
    return label;
  }

  public void execute(ProjectNode node) {
    command.startExecuteChain(actionName, node);
  }
}
