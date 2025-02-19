// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.shared.rpc.project.FileNode;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  public abstract ComponentDatabaseInterface getComponentDatabase();

  /**
   * Refresh the properties panel in the event the set of visible properties has changed.
   */
  public abstract void refreshPropertiesPanel();

  /**
   * Returns a new, unique component name based on the set of names returned by
   * {@link #getComponentNames()}. If hint is given it will be used as the base
   * for the new name. Otherwise, the type will be used.
   *
   * Examples:
   *
   * <code>gensymName("Button", null)</code>
   * <em>=> "Button1"</em>
   *
   * <code>gensymName("Button", "Information1")</code>
   * <em>=> "Information2"</em>
   *
   * @param type the type of the component
   * @param hint an optional hint for the component name. pass null if not needed
   * @return a new unique name that does not occur in the list of component names
   * returned by {@link #getComponentNames()}
   */
  public String gensymName(String type, String hint) {
    RegExp regexp = RegExp.compile("(.*?)([0-9]+)$");
    if (hint != null) {
      Set<String> names = new HashSet<String>(getComponentNames());
      String base = hint;
      int number = 1;
      if (regexp.test(hint)) {
        MatchResult result = regexp.exec(hint);
        base = result.getGroup(1);
        number = Integer.parseInt(result.getGroup(2));
      }
      number++;
      while (names.contains(base + number)) number++;
      return base + number;
    }
    int highIndex = 0;
    final String typeName = ComponentTranslationTable.getComponentName(type)
        .toLowerCase()
        .replace(" ", "_")
        .replace("'", "_");
    final int nameLength = typeName.length();
    for (String cName : this.getComponentNames()) {
      cName = cName.toLowerCase();
      try {
        if (cName.startsWith(typeName)) {
          highIndex = Math.max(highIndex, Integer.parseInt(cName.substring(nameLength)));
        }
      } catch (NumberFormatException e) {
        continue;
      }
    }
    return type + (highIndex + 1);
  }
}
