// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import com.google.appinventor.client.ComponentsTranslation;
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
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.google.appinventor.client.utils.Trie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

import static com.google.appinventor.client.Ode.MESSAGES;

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
  private List<Integer> categoryOrder;
  // initialize a Trie
  private Trie componentTrie;

  // panel that holds all palette items
  final VerticalPanel panel;
  // Map translated component names to English names
  private final Map<String, String> translationMap;

  private final TextBox searchText; 
  private final VerticalPanel searchResults;

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
    categoryOrder = new ArrayList<Integer>();

    componentTrie = new Trie();
    translationMap = new HashMap<String, String>();
    panel = new VerticalPanel();
    panel.setWidth("100%");

    //Load Component strings to Trie
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      String translationName = ComponentsTranslation.getComponentName(component).toLowerCase();
      componentTrie.insert(translationName);
      translationMap.put(translationName, component);
    }

    searchText = new TextBox();
    searchText.setWidth("100%");
    searchText.getElement().setPropertyString("placeholder", MESSAGES.searchComponents());
    searchText.getElement().setAttribute("type", "search");

    searchText.addKeyUpHandler(new SearchKeyUpHandler());
    searchText.addKeyPressHandler(new ReturnKeyHandler());
    searchText.addKeyDownHandler(new EscapeKeyDownHandler());
    searchText.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        doSearch();
      }
    });
    searchText.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        doSearch();
      }
    });

    panel.setSpacing(3);
    panel.add(searchText);
    panel.setWidth("100%");

    searchResults = new VerticalPanel();
    searchResults.setWidth("100%");
    stackPalette.setWidth("100%");

    initWidget(panel);
    panel.add(searchResults);
    panel.add(stackPalette);

    for (ComponentCategory category : ComponentCategory.values()) {
      if (showCategory(category)) {
        VerticalPanel categoryPanel = new VerticalPanel();
        categoryPanel.setWidth("100%");
        categoryPanels.put(category, categoryPanel);
        // The production version will not include a mapping for Extension because
        // only compile-time categories are included. This allows us to i18n the
        // Extension title for the palette.
        String title = ComponentCategory.EXTENSION.equals(category) ?
          MESSAGES.extensionComponentPallette() :
          ComponentsTranslation.getCategoryName(category.getName());
        stackPalette.add(categoryPanel, title);
      }
    }

    initExtensionPanel();
  }

   /**
   *  Automatic search and list results as users input the string
   */
  private class SearchKeyUpHandler implements KeyUpHandler {
    @Override
    public void onKeyUp(KeyUpEvent event) {
      doSearch();
    }
  }

  /**
   *  Users press escapte button, results and searchText will be cleared
   */
  private class EscapeKeyDownHandler implements KeyDownHandler {
    @Override
    public void onKeyDown(KeyDownEvent event) {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
        searchResults.clear();
        searchText.setText("");
      }
    }
  }

  /**
   *  Users press enter button, results will be added to searchResults panel
   */
  private class ReturnKeyHandler implements KeyPressHandler {
     @Override
      public void onKeyPress(KeyPressEvent event) {
        switch (event.getCharCode()) {
          case KeyCodes.KEY_END:
          case KeyCodes.KEY_DELETE:
          case KeyCodes.KEY_BACKSPACE:
            doSearch();
            break;
        }
      }
  }

  /**
   *  User clicks on searchButton and results will be added to searchResults panel
   */
  private void doSearch() {
    String search_str = searchText.getText().trim().toLowerCase();
    // Empty strings will return nothing
    if (search_str.length() != 0) {
      // Remove previous search results
      searchResults.clear();
      Collection<String> allComponents = componentTrie.getAllWords(search_str);
      for (String name : allComponents) {
        if (translationMap.containsKey(name)) {
          String englishName = translationMap.get(name);
          if (simplePaletteItems.containsKey(englishName)) {
            SimplePaletteItem item = simplePaletteItems.get(englishName);
            int version = COMPONENT_DATABASE.getComponentVersion(englishName);
            String versionName = COMPONENT_DATABASE.getComponentVersionName(englishName);
            String dateBuilt = COMPONENT_DATABASE.getComponentBuildDate(englishName);
            String helpString = COMPONENT_DATABASE.getHelpString(englishName);
            String helpUrl = COMPONENT_DATABASE.getHelpUrl(englishName);
            String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(englishName);
            String categoryString = COMPONENT_DATABASE.getCategoryString(englishName);
            Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(englishName);
            Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(englishName);
            Boolean external = COMPONENT_DATABASE.getComponentExternal(englishName);

            SimpleComponentDescriptor scd =
                new SimpleComponentDescriptor(englishName, editor, version, versionName,
                    dateBuilt, helpString, helpUrl, categoryDocUrlString, showOnPalette, nonVisible, external);
            SimplePaletteItem newItem = new SimplePaletteItem(scd, dropTargetProvider);
            searchResults.add(newItem);
          }
        }
      }
    } else {
      searchResults.clear();
    }
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
    int version = COMPONENT_DATABASE.getComponentVersion(componentTypeName);
    String versionName = COMPONENT_DATABASE.getComponentVersionName(componentTypeName);
    String dateBuilt = COMPONENT_DATABASE.getComponentBuildDate(componentTypeName);
    String helpString = COMPONENT_DATABASE.getHelpString(componentTypeName);
    String helpUrl = COMPONENT_DATABASE.getHelpUrl(componentTypeName);
    String categoryDocUrlString = COMPONENT_DATABASE.getCategoryDocUrlString(componentTypeName);
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    Boolean showOnPalette = COMPONENT_DATABASE.getShowOnPalette(componentTypeName);
    Boolean nonVisible = COMPONENT_DATABASE.getNonVisible(componentTypeName);
    Boolean external = COMPONENT_DATABASE.getComponentExternal(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (showOnPalette && showCategory(category)) {
      SimplePaletteItem item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentTypeName, editor, version, versionName, dateBuilt, helpString, helpUrl,
              categoryDocUrlString, showOnPalette, nonVisible, external),
            dropTargetProvider);
      simplePaletteItems.put(componentTypeName, item);
      addPaletteItem(item, category);
    }
  }

  public void removeComponent(String componentTypeName) {
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (simplePaletteItems.containsKey(componentTypeName)) {
      removePaletteItem(simplePaletteItems.get(componentTypeName), category);
      simplePaletteItems.remove(componentTypeName);
    }
  }

  /*
   * Adds a component entry to the palette.
   */
  private void addPaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    if (panel == null) {
      panel = addComponentCategory(category);
    }
    PaletteHelper paletteHelper = paletteHelpers.get(category);
    if (paletteHelper != null) {
      paletteHelper.addPaletteItem(panel, component);
    } else {
      panel.add(component);
    }
  }

  private VerticalPanel addComponentCategory(ComponentCategory category) {
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");
    categoryPanels.put(category, panel);
    // The production version will not include a mapping for Extension because
    // only compile-time categories are included. This allows us to i18n the
    // Extension title for the palette.
    int insert_index = Collections.binarySearch(categoryOrder, category.ordinal());
    insert_index = - insert_index - 1;
    stackPalette.insert(panel, insert_index);
    String title = "";
    if (ComponentCategory.EXTENSION.equals(category)) {
      title = MESSAGES.extensionComponentPallette();
      initExtensionPanel();
    } else {
      title = ComponentsTranslation.getCategoryName(category.getName());
    }
    stackPalette.setStackText(insert_index, title);
    categoryOrder.add(insert_index, category.ordinal());
    // When the categories are loaded, we want the first one open, which will almost always be User Interface
    stackPalette.showStack(0);
    return panel;
  }

  private void removePaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = categoryPanels.get(category);
    panel.remove(component);
    if (panel.getWidgetCount() < 1) {
      stackPalette.remove(panel);
      categoryPanels.remove(category);
    }
  }

  private void initExtensionPanel() {
    Anchor addComponentAnchor = new Anchor(MESSAGES.importExtensionMenuItem());
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
      stackPalette.remove(panel);
    }
    for (PaletteHelper pal : paletteHelpers.values()) {
      pal.clear();
    }
    categoryPanels.clear();
    paletteHelpers.clear();
    categoryOrder.clear();
    simplePaletteItems.clear();
  }

  // Intended for use by Blocks Toolkit, which needs to be able to refresh without
  // bothering the loaded extensions
  public void clearComponentsExceptExtension() {
    for (ComponentCategory category : categoryPanels.keySet()) {
      if (!ComponentCategory.EXTENSION.equals(category)) {
        VerticalPanel panel = categoryPanels.get(category);
        panel.clear();
        stackPalette.remove(panel);
      }
    }
    for (PaletteHelper pal : paletteHelpers.values()) {
      pal.clear();
    }
    categoryPanels.clear();
    paletteHelpers.clear();
    categoryOrder.clear();
    simplePaletteItems.clear();
  }


  @Override
  public void reloadComponents() {
    clearComponents();
    loadComponents();
  }

}
