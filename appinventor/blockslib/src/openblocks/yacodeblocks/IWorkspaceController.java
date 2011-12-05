// Copyright 2011 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import openblocks.codeblockutil.AIDirectory;

import java.awt.event.MouseEvent;
import java.io.IOException;


/**
 * The IWorkspaceController interface defines the public methods that a
 * workspace controller must implement.
 *
 * See WorkspaceControllerHolder for a class designed to hold an instance of a
 * class that implements IWorkspaceController.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface IWorkspaceController {
  /**
   * Factory interface to create a workspace controller.
   */
  public interface Factory {
    IWorkspaceController create();
  }

  /**
   * Returns true iff the workspace controller is loading blocks
   */
  boolean isLoadingBlocks();

  /**
   * Returns true iff the workspace controller has a project loaded.
   */
  boolean haveProject();

  /**
   * Returns the ComponentBlockManager for this workspace controller.
   */
  ComponentBlockManager getComponentBlockManager();

  /**
   * Notification from ComponentBlockManager that a component has been renamed.
   */
  void componentRenamed(String oldName, String newName);

  /**
   * Notification from ComponentBlockManager that a component has been removed.
   */
  void componentRemoved(String name);

  /*
   * Notification from BlockCanvas.
   *
   * @param e the MouseEvent containing the location
   */
  void mouseMovedOnCanvas(MouseEvent e);

  /**
   * Returns the PhoneCommManager for this workspace controller.
   *
   * <p>For some workspace controllers, this method may throw
   * UnsupportedOperationException.</p>
   */
  PhoneCommManager getPhoneCommManager();

  /**
   * Returns the AndroidController for this workspace controller.
   *
   * <p>For some workspace controllers, this method may throw
   * UnsupportedOperationException.</p>
   */
  AndroidController getAndroidController();

  /**
   * Returns the AIDirectory manager for this workspace controller.
   *
   * <p>For some workspace controllers, this method may throw
   * UnsupportedOperationException.</p>
   */
  AIDirectory getAIDir();

  /**
   * Returns the YAIL code for all the project definitions in a format suitable
   * for feeding to the phone REPL.
   *
   * <p>For some workspace controllers, this method may throw
   * UnsupportedOperationException.</p>
   */
  String getProjectDefinitionsForRepl()
      throws IOException, YailGenerationException, NoProjectException;
}
