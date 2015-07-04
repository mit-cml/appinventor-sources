// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.user.client.Window;

/**
 * Implementation for a box that can hold multiple viewers (including editors).
 *
 */
public class ViewerBox extends Box {
  // Singleton viewer box instance
  private static final ViewerBox INSTANCE = new ViewerBox();

  /**
   * Return the singleton viewer box.
   *
   * @return  viewer box
   */
  public static ViewerBox getViewerBox() {
    return INSTANCE;
  }

  /**
   * Creates new empty viewer box.
   */
  private ViewerBox() {
    super(MESSAGES.viewerBoxCaption(),
        600,    // height
        false,  // minimizable
        false); // removable
  }

  /**
   * Shows the content associated with the given project in the viewer.
   *
   * @param projectRootNode  the root node of the project to show in the viewer
   */
  public ProjectEditor show(ProjectRootNode projectRootNode) {
    ProjectEditor projectEditor = Ode.getInstance().getEditorManager().openProject(projectRootNode);
    OdeLog.log("ViewerBox: switching the content in the viewer box");
    setContent(projectEditor);
    Ode.getInstance().switchToDesignView();
    return projectEditor;
  }
}
