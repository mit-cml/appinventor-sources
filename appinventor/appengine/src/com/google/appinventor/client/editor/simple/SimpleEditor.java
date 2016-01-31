// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.shared.rpc.project.FileNode;

import java.util.List;
import java.util.Map;

/**
 * Abstract editor for files containing Simple components.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class SimpleEditor extends FileEditor {

  protected SimpleEditor(ProjectEditor projectEditor, FileNode fileNode) {
    super(projectEditor, fileNode);
  }

  /**
   * Returns true if the loading of the source file is complete, false otherwise.
   */
  public abstract boolean isLoadComplete();

  /**
   * Gets a map of the component instances. The caller can modify the map
   * without affecting the actual components.
   *
   * @return a map of the names and component instances
   */
  public abstract Map<String, MockComponent> getComponents();

  /*
   * Gets a list of the names of component instances. The caller can modify the
   * list without affecting the actual properties.
   *
   * @return a list of the names of component instances
   */
  public abstract List<String> getComponentNames();

  /**
   * Returns the component palette panel
   *
   * @return  component palette panel
   */
  public abstract SimplePalettePanel getComponentPalettePanel();

  /**
   * Returns the non-visible components panel
   *
   * @return  non-visible components panel
   */
  public abstract SimpleNonVisibleComponentsPanel getNonVisibleComponentsPanel();

  /**
   * Returns the visible components panel
   *
   * @return  visible components panel
   */
  public abstract SimpleVisibleComponentsPanel getVisibleComponentsPanel();

  /**
   * Returns true if this editor is for Screen1.
   */
  public abstract boolean isScreen1();
}
