// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

/**
 * WorkspaceControllerHolder is used to create and hold the workspace
 * controller.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class WorkspaceControllerHolder {
  private static IWorkspaceController.Factory factory;
  private static volatile boolean headless;
  private static IWorkspaceController workspaceController;

  private WorkspaceControllerHolder() {} // not to be instantiated

  /**
   * Sets the factory that will be used to create the workspace controller the
   * first time the get method (below) is called. If the workspace controller
   * has already been created, this method has no effect.
   *
   * @param factory the factory to create the workspace controller
   * @param headless whether this is a headless environment
   */
  public static synchronized void setFactory(IWorkspaceController.Factory factory,
      boolean headless) {
    if (workspaceController == null) {
      WorkspaceControllerHolder.factory = factory;
      WorkspaceControllerHolder.headless = headless;
    }
  }

  /**
   * Returns true if this is a headless environment.
   */
  public static boolean isHeadless() {
    return headless;
  }

  /**
   * Returns the workspace controller, creating it if necessary.
   */
  public static synchronized IWorkspaceController get() {
    if (workspaceController == null) {
      workspaceController = factory.create();
    }
    return workspaceController;
  }
}
