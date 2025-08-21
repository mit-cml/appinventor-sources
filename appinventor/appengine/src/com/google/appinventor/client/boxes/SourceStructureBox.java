// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.gwt.user.client.ui.DockPanel;

/**
 * Box implementation for source structure explorer.
 */
public class SourceStructureBox extends Box implements ISourceStructureBox {
  // Singleton source structure explorer box instance
  private static final SourceStructureBox INSTANCE = new SourceStructureBox();
  // Singleton source structure explorer child instance
  private static ISourceStructureBox SUBINSTANCE;

  /**
   * Return the singleton source structure explorer box.
   *
   * @return  source structure explorer box
   */
  public static SourceStructureBox getSourceStructureBox() {
    return INSTANCE;
  }

  /**
   * Creates new source structure explorer box.
   */
  private SourceStructureBox() {
    super(MESSAGES.sourceStructureBoxCaption(),
        300,    // height
        false,  // minimizable
        false); // removable

    // Creates the child instance according to the enabled features.
    SUBINSTANCE = new SourceStructureBoxFilter(this);

    setContent(SUBINSTANCE.getSourceStructureExplorer());
  }

  /**
   * Returns source structure explorer associated with box.
   *
   * @return source structure explorer
   */
  public SourceStructureExplorer getSourceStructureExplorer() {
    return SUBINSTANCE.getSourceStructureExplorer();
  }

  /**
   * Calls the child box and renders it according to its behaviour.
   * @param root current form
   */
  public void show(DesignerRootComponent root) {
    getSourceStructureExplorer().updateTree(root.buildComponentsTree(),
        root.getLastSelectedComponent().getSourceStructureExplorerItem());
    getSourceStructureBox().setVisible(true);
    this.setVisible(true);
    setContent(SUBINSTANCE.getSourceStructureExplorer());
  }

  /**
   * Returns the header container for the source structure box (used by childs).
   * @return DockPanel header container
   */
  public DockPanel getHeaderContainer() {
    return super.getHeaderContainer();
  }
}
