// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.palette;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ComponentsTranslation;
import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.palette.SimpleComponentDescriptor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.editor.simple.palette.SimplePalettePanel;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.wizards.ComponentImportWizard;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

/**
 * Panel showing Simple components which can be dropped onto the Young Android
 * visual designer panel.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidPalettePanel extends Composite
    implements SimplePalettePanel, ComponentDatabaseChangeListener {

  /**
   * The Filter interface is used by the palette panel to determine what components
   * to show. By default, an identity filter is used (everything is shown). Other
   * implementations may override the filter by calling {@link #setFilter(Filter, boolean)}.
   *
   * @author ewpatton@mit.edu (Evan W. Patton)
   */
  public interface Filter {
    /**
     * Tests whether the given component type should be shown in the palette.
     * @param componentTypeName The component type to check.
     * @return True if the component should be shown, otherwise false.
     */
    boolean shouldShowComponent(String componentTypeName);

    /**
     * Tests whether the extensions panel should be shown.
     * @return True if extensions are allowed, otherwise false.
     */
    boolean shouldShowExtensions();
  }

  // Identity filter implementation
  private static final Filter IDENTITY = new Filter() {
    @Override
    public boolean shouldShowComponent(String componentTypeName) {
      return true;
    }

    @Override
    public boolean shouldShowExtensions() {
      return true;
    }
  };

  // The singleton instance of the palette panel
  private static YoungAndroidPalettePanel INSTANCE;

  private final Map<ComponentCategory, PaletteHelper> paletteHelpers;

  private final StackPanel stackPalette;
  private final Map<ComponentCategory, VerticalPanel> categoryPanels;
  // store Component Type along with SimplePaleteItem to enable removal of components
  private final Map<String, SimplePaletteItem> simplePaletteItems;

  private List<Integer> categoryOrder;

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
  // The component database currently rendered by the palette panel.
  private ComponentDatabaseInterface componentDatabase;
  // The editor that is currently active (also acts as the drop target provider)
  private SimpleEditor editor;
  // Currently active filter
  private Filter filter = IDENTITY;
  // Cache of previously constructed palette items to reuse
  private final Map<String, SimplePaletteItem> cachedPaletteItems =
      new HashMap<String, SimplePaletteItem>();
  // Cache of previously constructed palette items to reuse for the search box
  private final Map<String, SimplePaletteItem> cachedSearchPaletteItems =
      new HashMap<String, SimplePaletteItem>();
  /* We keep a static map of image names to images in the image bundle so
   * that we can avoid making individual calls to the server for static image
   * that are already in the bundle. This is purely an efficiency optimization
   * for mock non-visible components.
   */
  private static final Map<String, ImageResource> bundledImages;

  @SuppressWarnings("CheckStyle")
  private native NativeArray filter(String match)/*-{
    return this.@com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel::arrayString.filter(function(x) { return x.indexOf(match) >= 0 });
  }-*/;

  @SuppressWarnings("CheckStyle")
  private native void sort()/*-{
    this.@com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel::arrayString.sort();
  }-*/;

  private Scheduler.ScheduledCommand rebuild = null;

  private void requestRebuildSearchList() {
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

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
  }

  static {
    Images images = Ode.getImageBundle();
    bundledImages = new HashMap<String, ImageResource>();
    bundledImages.put("images/accelerometersensor.png", images.accelerometersensor());
    bundledImages.put("images/ball.png", images.ball());
    bundledImages.put("images/button.png", images.button());
    bundledImages.put("images/canvas.png", images.canvas());
    bundledImages.put("images/imageSprite.png", images.imageSprite());
    bundledImages.put("images/imagePicker.png", images.imagepicker());
    bundledImages.put("images/videoPlayer.png", images.videoplayer());
    bundledImages.put("images/horizontal.png", images.horizontal());
    bundledImages.put("images/vertical.png", images.vertical());
    bundledImages.put("images/table.png", images.table());
    bundledImages.put("images/checkbox.png", images.checkbox());
    bundledImages.put("images/image.png", images.image());
    bundledImages.put("images/label.png", images.label());
    bundledImages.put("images/listPicker.png", images.listpicker());
    bundledImages.put("images/passwordtextbox.png", images.passwordtextbox());
    bundledImages.put("images/slider.png", images.slider());
    bundledImages.put("images/switch.png", images.toggleswitch());
    bundledImages.put("images/textbox.png", images.textbox());
    bundledImages.put("images/webviewer.png", images.webviewer());
    bundledImages.put("images/contactPicker.png", images.contactpicker());
    bundledImages.put("images/emailPicker.png", images.emailpicker());
    bundledImages.put("images/phoneNumberPicker.png", images.phonenumberpicker());
    bundledImages.put("images/lightsensor.png", images.lightsensor());
    bundledImages.put("images/barometer.png", images.barometer());
    bundledImages.put("images/thermometer.png", images.thermometer());
    bundledImages.put("images/hygrometer.png", images.hygrometer());
    bundledImages.put("images/gyroscopesensor.png", images.gyroscopesensor());
    bundledImages.put("images/nearfield.png", images.nearfield());
    bundledImages.put("images/activityStarter.png", images.activitystarter());
    bundledImages.put("images/barcodeScanner.png", images.barcodeScanner());
    bundledImages.put("images/bluetooth.png", images.bluetooth());
    bundledImages.put("images/camera.png", images.camera());
    bundledImages.put("images/camcorder.png", images.camcorder());
    bundledImages.put("images/clock.png", images.clock());
    bundledImages.put("images/fusiontables.png", images.fusiontables());
    bundledImages.put("images/gameClient.png", images.gameclient());
    bundledImages.put("images/locationSensor.png", images.locationSensor());
    bundledImages.put("images/notifier.png", images.notifier());
    bundledImages.put("images/legoMindstormsNxt.png", images.legoMindstormsNxt());
    bundledImages.put("images/legoMindstormsEv3.png", images.legoMindstormsEv3());
    bundledImages.put("images/orientationsensor.png", images.orientationsensor());
    bundledImages.put("images/pedometer.png", images.pedometerComponent());
    bundledImages.put("images/phoneip.png", images.phonestatusComponent());
    bundledImages.put("images/phoneCall.png", images.phonecall());
    bundledImages.put("images/player.png", images.player());
    bundledImages.put("images/soundEffect.png", images.soundeffect());
    bundledImages.put("images/soundRecorder.png", images.soundRecorder());
    bundledImages.put("images/speechRecognizer.png", images.speechRecognizer());
    bundledImages.put("images/spreadsheet.png", images.spreadsheet());
    bundledImages.put("images/textToSpeech.png", images.textToSpeech());
    bundledImages.put("images/texting.png", images.texting());
    bundledImages.put("images/datePicker.png", images.datePickerComponent());
    bundledImages.put("images/timePicker.png", images.timePickerComponent());
    bundledImages.put("images/tinyDB.png", images.tinyDB());
    bundledImages.put("images/file.png", images.file());
    bundledImages.put("images/tinyWebDB.png", images.tinyWebDB());
    bundledImages.put("images/firebaseDB.png", images.firebaseDB());
    bundledImages.put("images/twitter.png", images.twitterComponent());
    bundledImages.put("images/voting.png", images.voting());
    bundledImages.put("images/web.png", images.web());
    bundledImages.put("images/mediastore.png", images.mediastore());
    bundledImages.put("images/sharing.png", images.sharingComponent());
    bundledImages.put("images/spinner.png", images.spinner());
    bundledImages.put("images/listView.png", images.listview());
    bundledImages.put("images/translator.png", images.translator());
    bundledImages.put("images/yandex.png", images.yandex());
    bundledImages.put("images/proximitysensor.png", images.proximitysensor());
    bundledImages.put("images/extension.png", images.extension());
    bundledImages.put("images/cloudDB.png", images.cloudDB());
    bundledImages.put("images/map.png", images.map());
    bundledImages.put("images/marker.png", images.marker());
    bundledImages.put("images/circle.png", images.circle());
    bundledImages.put("images/linestring.png", images.linestring());
    bundledImages.put("images/polygon.png", images.polygon());
    bundledImages.put("images/featurecollection.png", images.featurecollection());
    bundledImages.put("images/rectangle.png", images.rectangle());
    bundledImages.put("images/recyclerView.png", images.recyclerview());
    bundledImages.put("images/navigation.png", images.navigationComponent());
    bundledImages.put("images/arduino.png", images.arduino());
    bundledImages.put("images/magneticSensor.png", images.magneticSensor());
    bundledImages.put("images/chart.png", images.chart());
    bundledImages.put("images/chartData.png", images.chartData2D());
    bundledImages.put("images/dataFile.png", images.dataFile());
  }

  /**
   * Creates a new component palette panel.
   */
  private YoungAndroidPalettePanel() {
    stackPalette = new StackPanel();

    paletteHelpers = new HashMap<ComponentCategory, PaletteHelper>();
    // If a category has a palette helper, add it to the paletteHelpers map here.
    paletteHelpers.put(ComponentCategory.LEGOMINDSTORMS, new LegoPaletteHelper());

    categoryPanels = new HashMap<ComponentCategory, VerticalPanel>();
    simplePaletteItems = new HashMap<String, SimplePaletteItem>();
    categoryOrder = new ArrayList<Integer>();

    translationMap = new HashMap<String, String>();
    panel = new VerticalPanel();
    panel.setWidth("100%");

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
        VerticalPanel categoryPanel = new VerticalPanel();
        categoryPanel.setWidth("100%");
        categoryPanels.put(category, categoryPanel);
        // The production version will not include a mapping for Extension because
        // only compile-time categories are included. This allows us to i18n the
        // Extension title for the palette.
        String title = ComponentCategory.EXTENSION.equals(category)
            ? MESSAGES.extensionComponentPallette()
            : ComponentsTranslation.getCategoryName(category.getName());
        stackPalette.add(categoryPanel, title);
      }
    }

    initExtensionPanel();
    this.setSize("100%", "!005");
  }

  /**
   * Gets the instance of the YoungAndroidPalettePanel.
   *
   * @return The instance of the panel.
   */
  public static YoungAndroidPalettePanel get() {
    if (INSTANCE == null) {
      INSTANCE = new YoungAndroidPalettePanel();
    }
    return INSTANCE;
  }

  /**
   * Automatic search and list results as users input the string.
   */
  private class SearchKeyUpHandler implements KeyUpHandler {
    @Override
    public void onKeyUp(KeyUpEvent event) {
      doSearch();
    }
  }

  /**
   * Users press escape button, results and searchText will be cleared
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
   * Users press enter button, results will be added to searchResults panel.
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
        default:
          break;
      }
    }
  }

  private void doSearch() {
    doSearch(false);
  }

  /**
   * User clicks on searchButton and results will be added to searchResults panel.
   */
  private void doSearch(boolean force) {
    String searchStr = searchText.getText().trim().toLowerCase();
    if (searchStr.equals(lastSearch) && !force) {
      // nothing to do here.
      return;
    }
    // Empty strings will return nothing
    if (searchStr.length() != 0) {
      // Remove previous search results
      searchResults.clear();
      Iterable<String> allComponents = filter(searchStr);
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
    lastSearch = searchStr;
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
    return category != ComponentCategory.INTERNAL
        || AppInventorFeatures.showInternalComponentsCategory();
  }

  /**
   * Loads all components to be shown on this palette.  Specifically, for
   * each component (except for those whose category is UNINITIALIZED, or
   * whose category is INTERNAL and we're running on a production server,
   * or who are specifically marked as not to be shown on the palette),
   * this creates or reuses a corresponding {@link SimplePaletteItem}
   * and adds it to the panel corresponding to its category. Whether or not a
   * component is shown is controlled by the currently active filter object,
   * which can be set by calling {@link #setFilter(Filter, boolean)}.
   */
  public void loadComponents() {
    for (String component : componentDatabase.getComponentNames()) {
      if (filter.shouldShowComponent(component)) {
        this.addComponent(component);
      }
    }
  }

  @Override
  public void configureComponent(MockComponent mockComponent) {
  }

  /**
   * Gets the image from an icon path.
   *
   * @param iconPath The iconName provided by the component descriptor.
   * @param packageName The package name of the component (for extensions).
   * @return An image representing the icon at the given path.
   */
  public static Image getImageFromPath(String iconPath, String packageName) {
    if (iconPath.startsWith("aiwebres/") && packageName != null) {
      // icon for extension
      Image image =
          new Image(StorageUtil.getFileUrl(Ode.getInstance().getCurrentYoungAndroidProjectId(),
              "assets/external_comps/" + packageName + "/" + iconPath));
      image.setWidth("16px");
      image.setHeight("16px");
      return image;
    }
    if (bundledImages.containsKey(iconPath)) {
      return new Image(bundledImages.get(iconPath));
    } else {
      return new Image(iconPath);
    }
  }

  /**
   * Constructs a URL, potentially, to a license file. If the license file is internal to an
   * extension (as part of its aiwebres directory), the URL is constructed relative to the App
   * Inventor DownloadServlet. If a fully qualified URL is provided, it is returned instead.
   * Otherwise, an empty string is returned.
   *
   * @param licensePath the path to the license, as specified by the component
   * @param packageName the package of the component, if coming from an extension
   * @param projectId the project ID
   * @return a URL to a license if given a valid licensePath, otherwise the empty string
   */
  public static String getLicenseUrlFromPath(String licensePath, String packageName,
      long projectId) {
    if (licensePath.startsWith("aiwebres/") && packageName != null) {
      // License file is inside aiwebres
      return StorageUtil.getFileUrl(projectId,
          "assets/external_comps/" + packageName + "/" + licensePath) + "&inline";
    } else if (licensePath.startsWith("http:") || licensePath.startsWith("https:")) {
      // The license is an external URL
      return licensePath;
    } else {
      // No license file specified
      return "";
    }
  }

  private SimplePaletteItem createPaletteItem(String componentTypeName,
      Map<String, SimplePaletteItem> cache) {
    int version = componentDatabase.getComponentVersion(componentTypeName);
    String key = componentTypeName + ":" + version;
    SimplePaletteItem item = cache.get(key);
    if (item == null) {
      String versionName = componentDatabase.getComponentVersionName(componentTypeName);
      String dateBuilt = componentDatabase.getComponentBuildDate(componentTypeName);
      String helpString = componentDatabase.getHelpString(componentTypeName);
      String helpUrl = componentDatabase.getHelpUrl(componentTypeName);
      String categoryDocUrlString = componentDatabase.getCategoryDocUrlString(componentTypeName);
      String type = componentDatabase.getComponentType(componentTypeName);
      String licenseUrl = getLicenseUrlFromPath(componentDatabase.getLicenseName(componentTypeName),
          type.substring(0, type.lastIndexOf('.')), editor.getProjectId());
      Image image = getImageFromPath(componentDatabase.getIconName(componentTypeName),
          type.substring(0, type.lastIndexOf('.')));
      boolean showOnPalette = componentDatabase.getShowOnPalette(componentTypeName);
      boolean nonVisible = componentDatabase.getNonVisible(componentTypeName);
      boolean external = componentDatabase.getComponentExternal(componentTypeName);
      item = new SimplePaletteItem(new SimpleComponentDescriptor(componentTypeName, version,
          versionName, dateBuilt, helpString, helpUrl, categoryDocUrlString, licenseUrl, image,
          showOnPalette, nonVisible, external));
      cache.put(key, item);
    }
    return item;
  }

  private SimplePaletteItem getPaletteItem(String componentTypeName) {
    return createPaletteItem(componentTypeName, cachedPaletteItems);
  }

  private SimplePaletteItem getSearchPaletteItem(String componentTypeName) {
    return createPaletteItem(componentTypeName, cachedSearchPaletteItems);
  }

  /**
   *  Loads a single Component to Palette. Used for adding Components.
   */
  @Override
  public void addComponent(String componentTypeName) {
    String categoryString = componentDatabase.getCategoryString(componentTypeName);
    boolean showOnPalette = componentDatabase.getShowOnPalette(componentTypeName);
    boolean external = componentDatabase.getComponentExternal(componentTypeName);
    ComponentCategory category = ComponentCategory.valueOf(categoryString);
    if (showOnPalette && showCategory(category) && filter.shouldShowComponent(componentTypeName)) {
      SimplePaletteItem item = getPaletteItem(componentTypeName);
      item.setActiveEditor(editor);
      simplePaletteItems.put(componentTypeName, item);
      addPaletteItem(item, category);

      // Make a second copy for the search mechanism
      item = getSearchPaletteItem(componentTypeName);
      item.setActiveEditor(editor);
      searchSimplePaletteItems.put(componentTypeName, item);

      // Handle extensions
      if (external) {
        translationMap.put(componentTypeName.toLowerCase(), componentTypeName);
        requestRebuildSearchList();
      } else {
        String translationName = ComponentsTranslation.getComponentName(componentTypeName)
            .toLowerCase();
        arrayString.push(translationName);
        translationMap.put(translationName, componentTypeName);
      }
    }
  }

  /**
   * Remove a component from the palette panel.
   *
   * @param componentTypeName The type name of the component to remove.
   */
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
      requestRebuildSearchList();
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
    int insertIndex = Collections.binarySearch(categoryOrder, category.ordinal());
    if (insertIndex < 0) {
      insertIndex = - insertIndex - 1;
      stackPalette.insert(panel, insertIndex);
      String title;
      if (ComponentCategory.EXTENSION.equals(category)) {
        title = MESSAGES.extensionComponentPallette();
        initExtensionPanel();
      } else {
        title = ComponentsTranslation.getCategoryName(category.getName());
      }
      stackPalette.setStackText(insertIndex, title);
      categoryOrder.add(insertIndex, category.ordinal());
    }
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
    for (String componentType : componentTypes) {
      this.removeComponent(componentType);
    }
    return true;
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
    categoryOrder.clear();
    simplePaletteItems.clear();
    searchSimplePaletteItems.clear();
  }

  @Override
  public void reloadComponents() {
    clearComponents();
    if (filter.shouldShowExtensions()) {
      addComponentCategory(ComponentCategory.EXTENSION);
    }
    loadComponents();
    requestRebuildSearchList();
  }

  /**
   * Set the filter (if any) for filtering components in the palette. If null
   * is provided for {@code filter}, all components will be shown (identity filter).
   * @param filter A filter instance used for filtering.
   * @param selectFirst If true, selects the first valid stack after applying the filter.
   */
  public void setFilter(Filter filter, boolean selectFirst) {
    this.filter = filter == null ? IDENTITY : filter;
    reloadComponents();
    if (selectFirst) {
      stackPalette.showStack(0);
    }
  }

  /**
   * Set the active editor that will receive palette items dragged from the panel.
   * @param editor The active form editor.
   */
  public void setActiveEditor(SimpleEditor editor) {
    if (this.editor == editor) {
      return;  // already the current editor.
    }
    int stackToSelect;
    if (this.editor == null || this.editor.getProjectEditor() != editor.getProjectEditor()) {
      // When the categories are loaded, we want the first one open, which will almost always be
      // User Interface
      stackToSelect = 0;
      // New project editor possibly means a new filter.
      Filter newFilter = editor.getPaletteFilter();
      this.filter = newFilter == null ? IDENTITY : newFilter;
    } else {
      stackToSelect = stackPalette.getSelectedIndex();
    }
    this.editor = editor;
    componentDatabase = editor.getComponentDatabase();
    reloadComponents();
    if (stackToSelect >= 0) {
      stackPalette.showStack(stackToSelect);
    }
  }

}
