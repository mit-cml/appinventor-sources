// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Box implementation for palette panels.
 *
 */
public final class PaletteBox extends Box {

  // Singleton palette box instance
  private static final PaletteBox INSTANCE = new PaletteBox();

  /**
   * Return the palette box.
   *
   * @return  palette box
   */
  public static PaletteBox getPaletteBox() {
    return INSTANCE;
  }
  private final StackPanel palettePanel;
  private Map<String, VerticalPanel> panelsByCat;
  // Component database: information about components (including their properties and events)
  private final SimpleComponentDatabase COMPONENT_DATABASE;

  /**
   * Creates new palette box.
   */
  private PaletteBox() {
    super(MESSAGES.paletteBoxCaption(),
        200,       // height
        false,     // minimizable
        false,     // removable
        false,     // startMinimized
        false,     // usePadding
        false);    // highlightCaption

    palettePanel = new StackPanel();
    palettePanel.setWidth("100%");
    panelsByCat = new HashMap<>();
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance();
    for (ComponentCategory cat: ComponentCategory.values()) {
      if (showCategory(cat)) {
        VerticalPanel panel = new VerticalPanel();
        panelsByCat.put(cat.getName(), panel);
        palettePanel.add(panel,
            ComponentsTranslation.getCategoryName(cat.getName()));
      }
    }

    setContent(palettePanel);
  }
  private static boolean showCategory(ComponentCategory category) {
    if (category == ComponentCategory.UNINITIALIZED) {
      return false;
    }
    if (category == ComponentCategory.INTERNAL &&
        !AppInventorFeatures.showInternalComponentsCategory()) {
      return false;
    }
    return true;
  }

  /**
   *  Loads a single Component to Palette. Used for adding Components.
   */
//  @Override
//  public void addComponent(String componentTypeName) {
//    if (simplePaletteItems.containsKey(componentTypeName)) { // We are upgrading
//      removeComponent(componentTypeName);
//    }
//    int version = COMPONENT_DATABASE.getComponentVersion(componentTypeName);
//    String versionName = COMPONENT_DATABASE.getComponentVersionName(componentTypeName);
//    String dateBuilt = COMPONENT_DATABASE.getComponentBuildDate(componentTypeName);
//    String helpString = COMPONENT_DATABASE.getHelpString(componentTypeName);
//    String helpUrl = COMPONENT_DATABASE.getHelpUrl(componentTypeName);
//    String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(componentTypeName);
//    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
//    Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(componentTypeName);
//    Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(componentTypeName);
//    Boolean external = COMPONENT_DATABASE.getComponentExternal(componentTypeName);
//    ComponentCategory category = ComponentCategory.valueOf(categoryString);
//    if (showOnPalette && showCategory(category)) {
//      SimplePaletteItem item = new SimplePaletteItem(
//          new SimpleComponentDescriptor(componentTypeName, editor, version, versionName, dateBuilt, helpString, helpUrl,
//              categoryDocUrlString, showOnPalette, nonVisible, external),
//          dropTargetProvider);
//      simplePaletteItems.put(componentTypeName, item);
//      addPaletteItem(item, category);
//
//      // Make a second copy for the search mechanism
//      item = new SimplePaletteItem(
//          new SimpleComponentDescriptor(componentTypeName, editor, version, versionName, dateBuilt,
//              helpString, helpUrl, categoryDocUrlString, showOnPalette, nonVisible, external),
//          dropTargetProvider);
//      // Handle extensions
//      if (external) {
//        translationMap.put(componentTypeName.toLowerCase(), componentTypeName);
//        requestRebuildList();
//      }
//      searchSimplePaletteItems.put(componentTypeName, item);
//    }
//  }
}
