// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.Duration;
import com.google.gwt.user.client.Command;

/**
 * Abstract base class for chainable commands that perform some action on a
 * {@link ProjectNode}. A command may be synchronous or asynchronous.
 *
 * <p/>Each subclass must implement {@link #willCallExecuteNextCommand}.
 *
 * <p/>Each subclass whose willCallExecuteNextCommand returns true,
 * must call {@link #executeNextCommand} after its own command has completed
 * successfully and must call {@link #executionFailedOrCanceled} if its own
 * command execution fails or has been canceled.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class ChainableCommand {
  // The next chainable command to be executed after this one is finished.
  private final ChainableCommand nextCommand;

  // Tracks elapsed time for entire chain
  private Duration chainDuration;
  // Tracks elapsed time for this command within the chain
  private Duration linkDuration;

  // Name of action this chain of commands represents (for analytics tracking purposes)
  private String actionName;

  // A command to execute after the chain is finished, regardless of whether it succeeds.
  // If any link in the chain is unable to call the next command (willCallExecuteNextCommand
  // returns false), the finallyCommand will be executed immediately after that link's execute
  // method returns.
  private Command finallyCommand;


  /**
   * Creates a new command.
   */
  public ChainableCommand() {
    this(null);
  }

  /**
   * Creates a new command.
   *
   * @param nextCommand the command to execute after this command has completed
   */
  public ChainableCommand(ChainableCommand nextCommand) {
    this.nextCommand = nextCommand;
  }

  /**
   * Initializes the tracking information on each link in the chain
   *
   * @param actionName the name of action this chain of commands represents
   * @param chainDuration duration to be used for the entire chain
   */
  private void initTrackingInformation(String actionName, Duration chainDuration) {
    this.actionName = actionName;
    // Note that all links in the chain have a reference to the same chainDuration.
    this.chainDuration = chainDuration;
    if (nextCommand != null) {
      nextCommand.initTrackingInformation(actionName, chainDuration);
    }
  }

  /**
   * Resets the start time for the link duration timer
   *
   */
  private final void resetLinkDuration() {
    // Note that each link in the chain has a unique linkDuration.
    linkDuration = new Duration();
  }

  /**
   * Returns the elapsed milliseconds that this link took to execute
   *
   */
  protected final int getElapsedMillis() {
    return linkDuration.elapsedMillis();
  }

  /**
   * Method that needs to be overridden by subclasses to indicate whether
   * or not they will call executeNextCommand when done and
   * executionFailedOrCanceled upon failure/cancelation.
   *
   * <p/>This method should only return false if the subclass cannot determine
   * when it has finished.
   */
  protected abstract boolean willCallExecuteNextCommand();

  /**
   * Kick off the chain of commands.
   *
   * @param actionName the name of action this chain of commands represents
   * @param node the project node to which the chain of commands is applied
   */
  public final void startExecuteChain(String actionName, ProjectNode node) {
    startExecuteChain(actionName, node, null);
  }

  /**
   * Kick off the chain of commands.
   *
   * @param actionName the name of action this chain of commands represents
   * @param node the project node to which the chain of commands is applied
   * @param finallyCommand a command to execute after the chain is finished,
   *                       regardless of whether it succeeds
   */
  public final void startExecuteChain(String actionName, ProjectNode node,
      Command finallyCommand) {
    // The node must not be null.
    // If you are calling startExecuteChain with null for the node parameter, maybe you should
    // question why you are using a ChainableCommand at all. ChainableCommands were designed to
    // perform an operation on a ProjectNode.
    Preconditions.checkNotNull(node);

    setFinallyCommand(finallyCommand);
    initTrackingInformation(actionName, new Duration());

    executeLink(node);
  }

  private final void executeLink(ProjectNode node) {
    resetLinkDuration();
    execute(node);
    if (!willCallExecuteNextCommand()) {
      // If this command won't end up calling executeNextCommand, execute the finally command
      // and do the tracking now. This may result in underestimating the overall time (should we
      // skip the time estimates in this case?).
      executeFinallyCommand();
      track(node);
    }
  }

  /**
   * Method that needs to be overridden by subclasses to implement the
   * actual behavior of the command.  Do not call this directly (call
   * startExecuteChain instead)!
   *
   * @param node the project node to which the command is applied
   */
  protected abstract void execute(ProjectNode node);

  /**
   * Executes the next command in the chain.
   *
   * @param node the project node to which the command is applied
   */
  protected final void executeNextCommand(ProjectNode node) {
    if (nextCommand != null) {
      nextCommand.executeLink(node);
    } else {
      // This is the end of the chain. Execute the finally command and track.
      executeFinallyCommand();
      track(node);
    }
  }

  /**
   * Indicates that this command's execution has failed or has been canceled.
   */
  protected final void executionFailedOrCanceled() {
    executeFinallyCommand();
  }

  /**
   * Sets a command to execute after the chain is finished, regardless of
   * whether it succeeds.
   *
   * @param finallyCommand a command to execute after the chain is finished,
   *                       regardless of whether it succeeds
   */
  private void setFinallyCommand(Command finallyCommand) {
    // Set the finallyCommand field on each link in the chain.
    this.finallyCommand = finallyCommand;
    if (nextCommand != null) {
      nextCommand.setFinallyCommand(finallyCommand);
    }
  }

  private void executeFinallyCommand() {
    if (finallyCommand != null) {
      finallyCommand.execute();
    }
  }

  private void track(ProjectNode node) {
    if (!actionName.isEmpty()) {
      Tracking.trackEvent(Tracking.PROJECT_EVENT, actionName,
          node.getName(), chainDuration.elapsedMillis());
    }
  }

  /**
   * Returns the project a node belongs to.
   *
   * @param node the node for which to determine the project
   */
  protected final Project getProject(ProjectNode node) {
    return Ode.getInstance().getProjectManager().getProject(node);
  }
}
