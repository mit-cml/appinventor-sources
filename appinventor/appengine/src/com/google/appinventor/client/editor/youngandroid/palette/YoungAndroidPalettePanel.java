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
import com.google.appinventor.client.editor.simple.palette.CollapsablePanel;
import com.google.appinventor.client.editor.simple.palette.DropTargetProvider;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;

import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

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

  private final CollapsablePanel stackPalette;

  // store Component Type along with SimplePaleteItem to enable removal of components
  private final Map<String, SimplePaletteItem> simplePaletteItems;

  private DropTargetProvider dropTargetProvider;

  // panel that holds all palette items
  final VerticalPanel panel;
  // Map translated component names to English names
  private final Map<String, String> translationMap;

  private final TextBox searchText; 
  private final VerticalPanel searchResults;
  private JsArrayString arrayString = (JsArrayString) JsArrayString.createArray();
  private String lastSearch = "";
  private Map<String, SimplePaletteItem> searchSimplePaletteItems =
      new HashMap<String, SimplePaletteItem>();

  @SuppressWarnings("checkstyle:LineLength")
  private native NativeArray filter(String match)/*-{
    return this.@com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel::arrayString.filter(function(x) { return x.indexOf(match) >= 0 });
  }-*/;

  @SuppressWarnings("checkstyle:LineLength")
  private native void sort()/*-{
    this.@com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel::arrayString.sort();
  }-*/;

  private Scheduler.ScheduledCommand rebuild = null;

  private void requestRebuildList() {
    if (rebuild != null) {
      return;
    }

    rebuild = new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        arrayString.setLength(0);
        for (String s : translationMap.keySet()) {
          arrayString.push(s);
        }
        sort();
        // Refresh the list by repeating the search
        doSearch(true);
        rebuild = null;
      }
    };
    Scheduler.get().scheduleDeferred(rebuild);
  }

  @JsType
  public static class NativeArray extends JsArrayString implements Iterable<String> {
    protected NativeArray() {
    }

    @Override
    @JsOverlay
    public final Iterator<String> iterator() {
      return new Iterator<String>() {
        int index = 0;

        @Override
        public boolean hasNext() {
          return index < NativeArray.this.length();
        }

        @Override
        public String next() {
          return NativeArray.this.get(index++);
        }
      };
    }
  }

  /**
   * Creates a new component palette panel.
   *
   * @param editor parent editor of this panel
   */
  public YoungAndroidPalettePanel(YaFormEditor editor) {
    this.editor = editor;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(editor.getProjectId());

    stackPalette = new CollapsablePanel();
    stackPalette.setStylePrimaryName("ode-CollapsablePanel");

    paletteHelpers = new HashMap<ComponentCategory, PaletteHelper>();
    // If a category has a palette helper, add it to the paletteHelpers map here.
    paletteHelpers.put(ComponentCategory.LEGOMINDSTORMS, new LegoPaletteHelper());
    simplePaletteItems = new HashMap<String, SimplePaletteItem>();
    translationMap = new HashMap<String, String>();
    panel = new VerticalPanel();
    panel.setWidth("100%");

    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      String translationName = ComponentsTranslation.getComponentName(component).toLowerCase();
      arrayString.push(translationName);
      translationMap.put(translationName, component);
    }

    searchText = new TextBox();
    searchText.setWidth("100%");
    searchText.getElement().setPropertyString("placeholder", MESSAGES.searchComponents());
    searchText.getElement().setAttribute("style", "width: 100%; box-sizing: border-box;");

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
        addComponentCategory(category);
      }
    }
    stackPalette.show(0);
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
   *  Users press escape button, results and searchText will be cleared
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

  private void doSearch() {
    doSearch(false);
  }

  /**
   *  User clicks on searchButton and results will be added to searchResults panel
   */
  private void doSearch(boolean force) {
    String search_str = searchText.getText().trim().toLowerCase();
    if (search_str.equals(lastSearch) && !force) {
      // nothing to do here.
      return;
    }
    // Empty strings will return nothing
    if (search_str.length() != 0) {
      long start = System.currentTimeMillis();
      // Remove previous search results
      searchResults.clear();
      Iterable<String> allComponents = filter(search_str);
      for (String name : allComponents) {
        if (translationMap.containsKey(name)) {
          final String codeName = translationMap.get(name);
          if (simplePaletteItems.containsKey(codeName)) {
            searchResults.add(searchSimplePaletteItems.get(codeName));
          }
        }
      }
    } else {
      searchResults.clear();
    }
    lastSearch = search_str;
  }

  private static boolean showCategory(ComponentCategory category) {
    if (category == ComponentCategory.UNINITIALIZED) {
      return false;
    }
    // We should only show FUTURE components if the future feature flag is enabled...
    if (category == ComponentCategory.FUTURE &&
        !AppInventorFeatures.enableFutureFeatures()) {
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
    for (ComponentCategory category : ComponentCategory.values()) {
      if (showCategory(category)) {
        addComponentCategory(category);
      }
    }
    initExtensionPanel();
    for (String component : COMPONENT_DATABASE.getComponentNames()) {
      this.addComponent(component);
    }
    stackPalette.show(0);
  }

  public void reloadComponentsFromSet(Set<String> set) {
    clearComponents();
    initExtensionPanel();
    for (String component : set) {
      addComponent(component);
    }
    stackPalette.show(0);
  }

  @Override
  public void configureComponent(MockComponent mockComponent) {
    String componentType = mockComponent.getType();
    PropertiesUtil.populateProperties(mockComponent,
        COMPONENT_DATABASE.getPropertyDefinitions(componentType), editor);
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

      // Make a second copy for the search mechanism
      item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentTypeName, editor, version, versionName, dateBuilt,
              helpString, helpUrl, categoryDocUrlString, showOnPalette, nonVisible, external),
          dropTargetProvider);
      // Handle extensions
      if (external) {
        translationMap.put(componentTypeName.toLowerCase(), componentTypeName);
        requestRebuildList();
      }
      searchSimplePaletteItems.put(componentTypeName, item);
    }
  }

  public void removeComponent(String componentTypeName) {
    String categoryString = COMPONENT_DATABASE.getCategoryString(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (simplePaletteItems.containsKey(componentTypeName)) {
      removePaletteItem(simplePaletteItems.get(componentTypeName), category);
      simplePaletteItems.remove(componentTypeName);
    }
    if (category == ComponentCategory.EXTENSION) {
      searchSimplePaletteItems.remove(componentTypeName);
      translationMap.remove(componentTypeName);
      requestRebuildList();
    }
  }

  /*
   * Adds a component entry to the palette.
   */
  private void addPaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = stackPalette.getCategory(category);
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
    String title = "";
    if (ComponentCategory.EXTENSION.equals(category)) {
      title = MESSAGES.extensionComponentPallette();
    } else {
      title = ComponentsTranslation.getCategoryName(category.getName());
    }
    stackPalette.add(panel, category, title);
    // When the categories are loaded, we want the first one open, which will almost always be User Interface
    return panel;
  }

  private void removePaletteItem(SimplePaletteItem component, ComponentCategory category) {
    VerticalPanel panel = stackPalette.getCategory(category);
    panel.remove(component);
    if (panel.getWidgetCount() < 1) {
      stackPalette.remove(panel, category);
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

    VerticalPanel categoryPanel = stackPalette.getCategory(ComponentCategory.EXTENSION);
    if (categoryPanel == null) {
      categoryPanel = addComponentCategory(ComponentCategory.EXTENSION);
    }
    categoryPanel.add(addComponentAnchor);
    categoryPanel.setCellHorizontalAlignment(
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
    stackPalette.clear();
    for (PaletteHelper pal : paletteHelpers.values()) {
      pal.clear();
    }
    paletteHelpers.clear();
    simplePaletteItems.clear();
  }

  @Override
  public void reloadComponents() {
    clearComponents();
    loadComponents();
  }

}
