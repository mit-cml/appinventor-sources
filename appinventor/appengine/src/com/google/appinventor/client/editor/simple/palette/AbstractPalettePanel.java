// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.

package com.google.appinventor.client.editor.simple.palette;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.json.JsArray;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.simple.ComponentDatabaseChangeListener;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Base implementation for the palette panel.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public abstract class AbstractPalettePanel<T extends ComponentDatabaseInterface, U extends DesignerEditor<?, ?, ?, T, ?>> extends Composite implements SimplePalettePanel, ComponentDatabaseChangeListener {
  // Component database: information about components (including their properties and events)
  private final T componentDatabase;

  // Associated editor
  protected final U editor;

  protected final Map<ComponentCategory, PaletteHelper> paletteHelpers;

  private final StackPanel stackPalette;
  protected final Map<ComponentCategory, VerticalPanel> categoryPanels;
  // store Component Type along with SimplePaleteItem to enable removal of components
  private final Map<String, SimplePaletteItem> simplePaletteItems;

  private final ComponentFactory factory;
  private DropTargetProvider dropTargetProvider;
  private List<Integer> categoryOrder;

  // panel that holds all palette items
  private final VerticalPanel panel;
  // Map translated component names to English names
  private final Map<String, String> translationMap;

  private final TextBox searchText;
  private final VerticalPanel searchResults;
  private JsArrayString arrayString = (JsArrayString) JsArrayString.createArray();
  private String lastSearch = "";
  private Map<String, SimplePaletteItem> searchSimplePaletteItems = new HashMap<String, SimplePaletteItem>();

    @SuppressWarnings("checkstyle:LineLength")
  private native JsArray<String> filter(String match)/*-{
    return this.@com.google.appinventor.client.editor.simple.palette.AbstractPalettePanel::arrayString.filter(function(x) { return x.indexOf(match) >= 0 });
  }-*/;

  @SuppressWarnings("checkstyle:LineLength")
  private native void sort()/*-{
    this.@com.google.appinventor.client.editor.simple.palette.AbstractPalettePanel::arrayString.sort();
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

  protected AbstractPalettePanel(U editor, ComponentFactory componentFactory) {
    this(editor, componentFactory, ComponentCategory.values());
  }

  protected AbstractPalettePanel(U editor, ComponentFactory componentFactory,
                                 ComponentCategory... categories) {
    this.editor = editor;
    componentDatabase = editor.getComponentDatabase();
    factory = componentFactory;

    stackPalette = new StackPanel();

    paletteHelpers = new HashMap<ComponentCategory, PaletteHelper>();

    categoryPanels = new HashMap<ComponentCategory, VerticalPanel>();
    simplePaletteItems = new HashMap<String, SimplePaletteItem>();
    categoryOrder = new ArrayList<Integer>();

    translationMap = new HashMap<String, String>();
    panel = new VerticalPanel();
    panel.setWidth("100%");


    for (String component : componentDatabase.getComponentNames()) {
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

    for (ComponentCategory category : categories) {
      if (showCategory(category)) {
        addComponentCategory(category);
      }
    }
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
    } else if (category == ComponentCategory.INTERNAL &&
        !AppInventorFeatures.showInternalComponentsCategory()) {
      return false;
    } else if (category == ComponentCategory.FUTURE &&
        !AppInventorFeatures.enableFutureFeatures()) {
      // We should only show FUTURE components if the future feature flag is enabled...
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
    for (String component : componentDatabase.getComponentNames()) {
      this.addComponent(component);
    }
  }

  private void loadComponents() {
    for (String component : componentDatabase.getComponentNames()) {
      this.addComponent(component);
    }
  }

  @Override
  public void configureComponent(MockComponent mockComponent) {
    String componentType = mockComponent.getType();
    PropertiesUtil.populateProperties(mockComponent, componentDatabase.getPropertyDefinitions(componentType), editor);

  }

  /**
   *  Loads a single Component to Palette. Used for adding Components.
   */
  @Override
  public void addComponent(String componentTypeName) {
    if (simplePaletteItems.containsKey(componentTypeName)) { // We are upgrading
      removeComponent(componentTypeName);
    }
    int version = componentDatabase.getComponentVersion(componentTypeName);
    String versionName = componentDatabase.getComponentVersionName(componentTypeName);
    String dateBuilt = componentDatabase.getComponentBuildDate(componentTypeName);
    String helpString = componentDatabase.getHelpString(componentTypeName);
    String helpUrl = componentDatabase.getHelpUrl(componentTypeName);
    String categoryDocUrlString = componentDatabase.getCategoryDocUrlString(componentTypeName);
    String categoryString = componentDatabase.getCategoryString(componentTypeName);
    boolean showOnPalette = componentDatabase.getShowOnPalette(componentTypeName);
    boolean nonVisible = componentDatabase.getNonVisible(componentTypeName);
    boolean external = componentDatabase.getComponentExternal(componentTypeName);
    String license = factory.getLicense(componentTypeName,
        componentDatabase.getComponentType(componentTypeName));
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (showOnPalette && showCategory(category)) {
      SimplePaletteItem item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentTypeName, version, versionName, dateBuilt, license,
              helpString, helpUrl, categoryDocUrlString, showOnPalette, nonVisible, external,
              factory),
            dropTargetProvider);
      simplePaletteItems.put(componentTypeName, item);
      addPaletteItem(item, category);

      // Make a second copy for the search mechanism
      item = new SimplePaletteItem(
          new SimpleComponentDescriptor(componentTypeName, version, versionName, dateBuilt, license,
              helpString, helpUrl, categoryDocUrlString, showOnPalette, nonVisible, external,
              factory),
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
    String categoryString = componentDatabase.getCategoryString(componentTypeName);
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

  protected VerticalPanel addComponentCategory(ComponentCategory category) {
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

  @Override
  public Widget getWidget() {
    return this;
  }

  @Override
  public abstract SimplePalettePanel copy();

  @Override
  public MockComponent createMockComponent(String name, String type) {
    return factory.createMockComponent(name, type);
  }
}
