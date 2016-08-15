// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.TranslationDesignerPallete;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.PropertyDefinition;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel showing Simple components which can be dropped onto the Young Android
 * visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidPalettePanel extends Composite implements SimplePalettePanel, ComponentDatabaseChangeListener {

  // Component database: information about components (including their properties and events)
  private final SimpleComponentDatabase COMPONENT_DATABASE;

  // Associated editor
  private final YaFormEditor editor;

  private final Map<ComponentCategory, PaletteHelper> paletteHelpers;

  private final StackPanel stackPalette;
  private final Map<ComponentCategory, VerticalPanel> categoryPanels;
  // store Component Type along with SimplePaleteItem to enable removal of components
  private final Map<String, SimplePaletteItem> simplePaletteItems;

  private DropTargetProvider dropTargetProvider;

  /**
   * Creates a new component palette panel.
   *
   * @param editor parent editor of this panel
   */
  public YoungAndroidPalettePanel(YaFormEditor editor) {
    this.editor = editor;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(editor.getProjectId());

    stackPalette = new StackPanel();

    paletteHelpers = new HashMap<ComponentCategory, PaletteHelper>();
    // If a category has a palette helper, add it to the paletteHelpers map here.
    paletteHelpers.put(ComponentCategory.LEGOMINDSTORMS, new LegoPaletteHelper());

    categoryPanels = new HashMap<ComponentCategory, VerticalPanel>();
    simplePaletteItems = new HashMap<String, SimplePaletteItem>();

    for (ComponentCategory category : ComponentCategory.values()) {
      if (showCategory(category)) {
        VerticalPanel categoryPanel = new VerticalPanel();
        categoryPanel.setWidth("100%");
        categoryPanels.put(category, categoryPanel);
        stackPalette.add(categoryPanel,
            TranslationDesignerPallete.getCorrespondingString(category.getName()));
      }
    }

    initExtensionPanel();

    stackPalette.setWidth("100%");
    initWidget(stackPalette);
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
   * Loads all components to be shown on this palette.  Specifically, for
   * each component (except for those whose category is UNINITIALIZED, or
   * whose category is INTERNAL and we're running on a production server,
   * or who are specifically marked as not to be shown on the palette),
   * this creates a corresponding {@link SimplePaletteItem} with the passed
   * {@link DropTargetProvider} and adds it to the panel corresponding to
   * its category.
   *
   * @param dropTargetProvider provider of targets that palette items can be
   *                           dropped on
   */
  @Override
  public void loadComponents(DropTargetProvider dropTargetProvider) {
    this.dropTargetProvider = dropTargetProvider;
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      this.addComponent(component);
    }
  }

  public void loadComponents() {
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      this.addComponent(component);
    }
  }

  @Override
  public void configureComponent(MockComponent mockComponent) {
    String componentType = mockComponent.getType();
    PropertiesUtil.populateProperties(mockComponent, COMPONENT_DATABASE.getPropertyDefinitions(componentType), editor);

  }

  /**
   *  Loads a single Component to Palette. Used for adding Components.
   */
  @Override
  public void addComponent(String componentTypeName) {
    if (simplePaletteItems.containsKey(componentTypeName)) { // We are upgrading
      removeComponent(componentTypeName);
    }
    String helpString = COMPONENT_DATABASE.getHelpString(componentTypeName);
    String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(componentTypeName);
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(componentTypeName);
    Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(componentTypeName);
    Boolean external = COMPONENT_DATABASE.getComponentExternal(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (showOnPalette && showCategory(category)) {
      SimplePaletteItem item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentTypeName, editor, helpString,
              categoryDocUrlString, showOnPalette, nonVisible, external),
            dropTargetProvider);
      simplePaletteItems.put(componentTypeName, item);
      addPaletteItem(item, category);
    }
  }

  public void removeComponent(String componentTypeName) {
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    removePaletteItem(simplePaletteItems.get(componentTypeName), category);
  }



  /*
   * Adds a component entry to the palette.
   */
  private void addPaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    PaletteHelper paletteHelper = paletteHelpers.get(category);
    if (paletteHelper != null) {
      paletteHelper.addPaletteItem(panel, component);
    } else {
      panel.add(component);
    }
  }

  private void removePaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    panel.remove(component);
  }

  private void initExtensionPanel() {
    Anchor addComponentAnchor = new Anchor("Import extension");
    addComponentAnchor.setStylePrimaryName("ode-ExtensionAnchor");
    addComponentAnchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        new ComponentImportWizard().center();
      }
    });

    categoryPanels.get(ComponentCategory.EXTENSION).add(addComponentAnchor);
    categoryPanels.get(ComponentCategory.EXTENSION).setCellHorizontalAlignment(
        addComponentAnchor, HasHorizontalAlignment.ALIGN_CENTER);
  }

  @Override
  public void onComponentTypeAdded(List<String> componentTypes) {
    for (String componentType : componentTypes) {
      this.addComponent(componentType);
    }
  }

  @Override
  public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
    boolean result = true;
    for (String componentType : componentTypes) {
      this.removeComponent(componentType);
    }
    return result;
  }

  @Override
  public void onComponentTypeRemoved(Map<String, String> componentTypes) {

  }

  @Override
  public void onResetDatabase() {
    reloadComponents();
  }

  @Override
  public void clearComponents() {
    for (ComponentCategory category : categoryPanels.keySet()) {
      VerticalPanel panel = categoryPanels.get(category);
      panel.clear();
    }
  }

  @Override
  public void reloadComponents() {
    clearComponents();
    loadComponents();
  }

}
