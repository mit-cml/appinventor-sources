// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVerticalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.properties.BadPropertyEditorException;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;

/**
 * Mock Form component. This implementation provides two main preview sizes corresponding to
 * 'normal' and 'large' buckets (http://developer.android.com/guide/practices/screens_support.html).
 * Normal size is a 1:1 with pixels on a device with dpi:160. We use that as the baseline for the
 * browser too. All UI elements should be scaled to DP for buckets other than 'normal'.
 */
public final class MockForm extends MockContainer {

  /*
   * Widget for the mock form title bar.
   */
  private class TitleBar extends Composite {
    private static final int HEIGHT = 24;

    // UI elements
    private Label title;
    private AbsolutePanel bar;

    /*
     * Creates a new title bar.
     */
    TitleBar() {
      title = new Label();
      title.setStylePrimaryName("ode-SimpleMockFormTitle");
      title.setHorizontalAlignment(Label.ALIGN_LEFT);

      bar = new AbsolutePanel();
      bar.add(title, 12, 4);

      initWidget(bar);

      setStylePrimaryName("ode-SimpleMockFormTitleBar");
      setSize("100%", HEIGHT + "px");
    }

    /*
     * Changes the title in the title bar.
     */
    void changeTitle(String newTitle) {
      title.setText(newTitle);
    }
  }

  /*
   * Widget for a mock phone status bar.
   */
  private class PhoneBar extends Composite {
    private static final int HEIGHT = 24;

    // UI elements
    private DockPanel bar;
    private Image phoneBarImage;

    /*
     * Creates a new phone status bar.
     */
    PhoneBar() {
      phoneBarImage = new Image(images.phonebar());

      bar = new DockPanel();
      bar.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
      bar.add(phoneBarImage, DockPanel.EAST);

      initWidget(bar);

      setStylePrimaryName("ode-SimpleMockFormPhoneBar");
      setSize("100%", HEIGHT + "px");
    }
  }

  /*
   *
   * Widget for a mock phone navigation bar; Shows at the bottom of the viewer
   */
  private class NavigationBar extends Composite {
    private static final int HEIGHT = 35;

    // UI elements
    private DockPanel bar;
    private Image navigationBarImage;

    /*
     * Creates a new phone navigation bar; Shows at the bottom of the viewer.
     */

    NavigationBar() {
      navigationBarImage = new Image(images.navigationbar());
      bar = new DockPanel();
      bar.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
      bar.add(navigationBarImage, DockPanel.CENTER);
      initWidget(bar);

      setStylePrimaryName("ode-SimpleMockFormPhoneBar"); //reuse the css for the phone
      setSize("100%", HEIGHT + "px");
    }
  }

  /**
   * Component type name.
   */
  public static final String TYPE = "Form";

  private static final String VISIBLE_TYPE = "Screen";

  // Currently App Inventor provides two main sizes that correspond to 'normal' and 'large'
  // screens. We use phone=normal (470 x 320 DP) and tablet=large (640 x 480 DP).
  // More information about 'bucket' sizes at:
  // http://developer.android.com/guide/practices/screens_support.html
  // The values for Phone and Tablet were decided by trial and error. The main reason is that in
  // the designer we use sizes of GWT widgets, and not the sizes of the actual Android widgets.

  private static final int PHONE_PORTRAIT_WIDTH = 320;
  private static final int PHONE_PORTRAIT_HEIGHT = 470 + 35; // Adds 35 for the navigation bar
  private static final int PHONE_LANDSCAPE_WIDTH = PHONE_PORTRAIT_HEIGHT;
  private static final int PHONE_LANDSCAPE_HEIGHT = PHONE_PORTRAIT_WIDTH;

  private static final int TABLET_PORTRAIT_WIDTH = 480;
  private static final int TABLET_PORTRAIT_HEIGHT = 640 + 35; // Adds 35 for the navigation bar
  private static final int TABLET_LANDSCAPE_WIDTH = TABLET_PORTRAIT_HEIGHT;
  private static final int TABLET_LANDSCAPE_HEIGHT = TABLET_PORTRAIT_WIDTH;

  // These are default values but they can be changed in the changePreviewSize method
  private int PORTRAIT_WIDTH = PHONE_PORTRAIT_WIDTH;
  private int PORTRAIT_HEIGHT = PHONE_PORTRAIT_HEIGHT;
  private int LANDSCAPE_WIDTH = PHONE_LANDSCAPE_WIDTH;
  private int LANDSCAPE_HEIGHT = PHONE_LANDSCAPE_HEIGHT;
  private boolean landscape = false;

  // Property names
  private static final String PROPERTY_NAME_TITLE = "Title";
  private static final String PROPERTY_NAME_SCREEN_ORIENTATION = "ScreenOrientation";
  private static final String PROPERTY_NAME_SCROLLABLE = "Scrollable";
  private static final String PROPERTY_NAME_ICON = "Icon";
  private static final String PROPERTY_NAME_VCODE = "VersionCode";
  private static final String PROPERTY_NAME_VNAME = "VersionName";
  private static final String PROPERTY_NAME_ANAME = "AppName";
  private static final String PROPERTY_NAME_SIZING = "Sizing"; // Don't show except on screen1

  // Form UI components
  AbsolutePanel formWidget;
  ScrollPanel scrollPanel;
  private TitleBar titleBar;
  private MockComponent selectedComponent;

  int screenWidth;              // TEMP: Make package visible so we can use it MockHVLayoutBase
  private int screenHeight;
  int usableScreenHeight;       // TEMP: Make package visible so we can use it MockHVLayoutBase

  // Set of listeners for any changes of the form
  final HashSet<FormChangeListener> formChangeListeners = new HashSet<FormChangeListener>();

  // Don't access the verticalScrollbarWidth field directly. Use getVerticalScrollbarWidth().
  private static int verticalScrollbarWidth;

  private MockFormLayout myLayout;
  
  // flag to control attempting to enable/disable vertical
  // alignment when scrollable property is changed
  private boolean initialized = false;
  
  private YoungAndroidVerticalAlignmentChoicePropertyEditor myVAlignmentPropertyEditor;

  public static final String PROPERTY_NAME_HORIZONTAL_ALIGNMENT = "AlignHorizontal";
  public static final String PROPERTY_NAME_VERTICAL_ALIGNMENT = "AlignVertical";

  /**
   * Creates a new MockForm component.
   *
   * @param editor  editor of source file the component belongs to
   */
  public MockForm(SimpleEditor editor) {
    // Note(Hal): This helper thing is a kludge because I really want to write:
    // myLayout = new MockHVLayout(orientation);
    // super(editor, type, icon, myLayout);
    // but Java won't let me do that.

    super(editor, TYPE, images.form(), MockFormHelper.makeLayout());
    // Note(hal): There better not be any calls to MockFormHelper before the
    // next instruction.  Note that the Helper methods are synchronized to avoid possible
    // future problems if we ever have threads creating forms in parallel.
    myLayout = MockFormHelper.getLayout();

    formWidget = new AbsolutePanel();
    formWidget.setStylePrimaryName("ode-SimpleMockForm");

    // Initialize mock form UI by adding the phone bar and title bar.
    formWidget.add(new PhoneBar());
    titleBar = new TitleBar();
    formWidget.add(titleBar);

    // Put a ScrollPanel around the rootPanel.
    scrollPanel = new ScrollPanel(rootPanel);
    formWidget.add(scrollPanel);

    //Add navigation bar at the bottom of the viewer.
    formWidget.add(new NavigationBar());

    initComponent(formWidget);
    
    // Set up the initial state of the vertical alignment property editor and its dropdowns
    try {
      myVAlignmentPropertyEditor = PropertiesUtil.getVAlignmentEditor(properties);
    } catch (BadPropertyEditorException e) {
      OdeLog.log(MESSAGES.badAlignmentPropertyEditorForArrangement());
      return;
    }
    enableAndDisableDropdowns();
    initialized = true;
    // Now that the default for Scrollable is false, we need to force setting the property when creating the MockForm
    setScrollableProperty(getPropertyValue(PROPERTY_NAME_SCROLLABLE));
  }

  public void changePreviewSize(boolean isTablet) {
    if (isTablet) {
      PORTRAIT_WIDTH   = TABLET_PORTRAIT_WIDTH;
      PORTRAIT_HEIGHT  = TABLET_PORTRAIT_HEIGHT;
      LANDSCAPE_WIDTH  = TABLET_LANDSCAPE_WIDTH;
      LANDSCAPE_HEIGHT = TABLET_LANDSCAPE_HEIGHT;
    }
    else {
      PORTRAIT_WIDTH = PHONE_PORTRAIT_WIDTH;
      PORTRAIT_HEIGHT = PHONE_PORTRAIT_HEIGHT;
      LANDSCAPE_WIDTH = PHONE_LANDSCAPE_WIDTH;
      LANDSCAPE_HEIGHT = PHONE_LANDSCAPE_HEIGHT;
    }

    if (landscape)
      resizePanel(LANDSCAPE_WIDTH, LANDSCAPE_HEIGHT);
    else
      resizePanel(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
  }

  /*
   * Resizes the scrollPanel and formWidget based on the screen size.
   */
  private void resizePanel(int newWidth, int newHeight){
    screenWidth = newWidth;
    screenHeight = newHeight;
    usableScreenHeight = screenHeight - PhoneBar.HEIGHT - TitleBar.HEIGHT - NavigationBar.HEIGHT;


    rootPanel.setPixelSize(screenWidth, usableScreenHeight);
    scrollPanel.setPixelSize(screenWidth + getVerticalScrollbarWidth(), usableScreenHeight);
    formWidget.setPixelSize(screenWidth + getVerticalScrollbarWidth(), screenHeight);
    // Store properties
    changeProperty(PROPERTY_NAME_WIDTH, "" + screenWidth);
    boolean scrollable = Boolean.parseBoolean(getPropertyValue(PROPERTY_NAME_SCROLLABLE));
    if (!scrollable) {
      changeProperty(PROPERTY_NAME_HEIGHT, "" + usableScreenHeight);
    }
  }

   /*
   * Returns the width of a vertical scroll bar, calculating it if necessary.
   */
  private static int getVerticalScrollbarWidth() {
    // We only calculate the vertical scroll bar width once, then we store it in the static field
    // verticalScrollbarWidth. If the field is non-zero, we don't need to calculate it again.
    if (verticalScrollbarWidth == 0) {
      // The following code will calculate (on the fly) the width of a vertical scroll bar.
      // We'll create two divs, one inside the other and add the outer div to the document body,
      // but off-screen where the user won't see it.
      // We'll measure the width of the inner div twice: (first) when the outer div's vertical
      // scrollbar is hidden and (second) when the outer div's vertical scrollbar is visible.
      // The width of inner div will be smaller when outer div's vertical scrollbar is visible.
      // By subtracting the two measurements, we can calculate the width of the vertical scrollbar.

      // I used code from the following websites as reference material:
      // http://jdsharp.us/jQuery/minute/calculate-scrollbar-width.php
      // http://www.fleegix.org/articles/2006-05-30-getting-the-scrollbar-width-in-pixels

      Document document = Document.get();

      // Create an outer div.
      DivElement outerDiv = document.createDivElement();
      Style outerDivStyle = outerDiv.getStyle();
      // Use absolute positioning and set the top/left so that it is off-screen.
      // We don't want the user to see anything while we do this calculation.
      outerDivStyle.setProperty("position", "absolute");
      outerDivStyle.setProperty("top", "-1000px");
      outerDivStyle.setProperty("left", "-1000px");
      // Set the width and height of the outer div to a fixed size in pixels.
      outerDivStyle.setProperty("width", "100px");
      outerDivStyle.setProperty("height", "50px");
      // Hide the outer div's scrollbar by setting the "overflow" property to "hidden".
      outerDivStyle.setProperty("overflow", "hidden");

      // Create an inner div and put it inside the outer div.
      DivElement innerDiv = document.createDivElement();
      Style innerDivStyle = innerDiv.getStyle();
      // Set the height of the inner div to be 4 times the height of the outer div so that a
      // vertical scrollbar will be necessary (but hidden for now) on the outer div.
      innerDivStyle.setProperty("height", "200px");
      outerDiv.appendChild(innerDiv);

      // Temporarily add the outer div to the document body. It's off-screen so the user won't
      // actually see anything.
      Element bodyElement = document.getElementsByTagName("body").getItem(0);
      bodyElement.appendChild(outerDiv);

      // Get the width of the inner div while the outer div's vertical scrollbar is hidden.
      int widthWithoutScrollbar = innerDiv.getOffsetWidth();
      // Show the outer div's vertical scrollbar by setting the "overflow" property to "auto".
      outerDivStyle.setProperty("overflow", "auto");
      // Now, get the width of the inner div while the vertical scrollbar is visible.
      int widthWithScrollbar = innerDiv.getOffsetWidth();

      // Remove the outer div from the document body.
      bodyElement.removeChild(outerDiv);

      // Calculate the width of the vertical scrollbar by subtracting the two widths.
      verticalScrollbarWidth = widthWithoutScrollbar - widthWithScrollbar;
    }

    return verticalScrollbarWidth;
  }

  @Override
  public final MockForm getForm() {
    return this;
  }

  @Override
  public boolean isForm() {
    return true;
  }


  @Override
  public String getVisibleTypeName() {
    return VISIBLE_TYPE;
  }

  @Override
  protected void addWidthHeightProperties() {
    addProperty(PROPERTY_NAME_WIDTH, "" + PORTRAIT_WIDTH, null,
        new YoungAndroidLengthPropertyEditor());
    addProperty(PROPERTY_NAME_HEIGHT, "" + LENGTH_PREFERRED, null,
        new YoungAndroidLengthPropertyEditor());
  }

  @Override
  public boolean isPropertyPersisted(String propertyName) {
    // We use the Width and Height properties to make the form appear correctly in the designer,
    // but they aren't actually persisted to the .scm file.
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }
    return super.isPropertyPersisted(propertyName);
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_WIDTH) ||
        propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      return false;
    }

    if (propertyName.equals(PROPERTY_NAME_ICON)) {
      // The Icon property actually applies to the application and is only visible on Screen1.
      return editor.isScreen1();
    }

    if (propertyName.equals(PROPERTY_NAME_VNAME)) {
      // The VersionName property actually applies to the application and is only visible on Screen1.
      return editor.isScreen1();
    }

    if (propertyName.equals(PROPERTY_NAME_VCODE)) {
      // The VersionCode property actually applies to the application and is only visible on Screen1.
      return editor.isScreen1();
    }

    if (propertyName.equals(PROPERTY_NAME_SIZING)) {
      // The Sizing property actually applies to the application and is only visible on Screen1.
      return editor.isScreen1();
    }

    if (propertyName.equals(PROPERTY_NAME_ANAME)) {
      // The AppName property actually applies to the application and is only visible on Screen1.
      return editor.isScreen1();
    }

    return super.isPropertyVisible(propertyName);
  }

  /*
   * Sets the form's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isNoneColor(text)) {
      text = "&HFF000000";  // black
    } else if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFFFFFFFF";  // white
    }
    MockComponentsUtil.setWidgetBackgroundColor(rootPanel, text);
  }

  /*
   * Sets the form's BackgroundImage property to a new value.
   */
  private void setBackgroundImageProperty(String text) {
    String url = convertImagePropertyValueToUrl(text);
    if (url == null) {
      // text was not recognized as an asset.
      url = "";
    }
    MockComponentsUtil.setWidgetBackgroundImage(rootPanel, url);
  }

  private void setScreenOrientationProperty(String text) {
    if (hasProperty(PROPERTY_NAME_WIDTH) && hasProperty(PROPERTY_NAME_HEIGHT) &&
        hasProperty(PROPERTY_NAME_SCROLLABLE)) {
      if (text.equalsIgnoreCase("landscape")) {
        screenWidth = LANDSCAPE_WIDTH;
        screenHeight = LANDSCAPE_HEIGHT;
        landscape = true;
      } else {
        screenWidth = PORTRAIT_WIDTH;
        screenHeight = PORTRAIT_HEIGHT;
        landscape = false;
      }
      usableScreenHeight = screenHeight - PhoneBar.HEIGHT - TitleBar.HEIGHT - NavigationBar.HEIGHT;
      resizePanel(screenWidth, screenHeight);

      changeProperty(PROPERTY_NAME_WIDTH, "" + screenWidth);
      boolean scrollable = Boolean.parseBoolean(getPropertyValue(PROPERTY_NAME_SCROLLABLE));
      if (!scrollable) {
        changeProperty(PROPERTY_NAME_HEIGHT, "" + usableScreenHeight);
      }
    }
  }

  private void setScrollableProperty(String text) {
    if (hasProperty(PROPERTY_NAME_HEIGHT)) {
      final boolean scrollable = Boolean.parseBoolean(text);
      int heightHint = scrollable ? LENGTH_PREFERRED : usableScreenHeight;
      changeProperty(PROPERTY_NAME_HEIGHT, "" + heightHint);
    }
  }

  private void setIconProperty(String icon) {
    // The Icon property actually applies to the application and is only visible on Screen1.
    // When we load a form that is not Screen1, this method will be called with the default value
    // for icon (empty string). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON, icon);
    }
  }

  private void setVCodeProperty(String vcode) {
    // The VersionCode property actually applies to the application and is only visible on Screen1.
    // When we load a form that is not Screen1, this method will be called with the default value
    // for VersionCode (1). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE, vcode);
    }
  }

  private void setVNameProperty(String vname) {
    // The VersionName property actually applies to the application and is only visible on Screen1.
    // When we load a form that is not Screen1, this method will be called with the default value
    // for VersionName (1.0). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME, vname);
    }
  }

  private void setSizingProperty(String sizingProperty) {
    // The Compatibility property actually applies to the application and is only visible on
    // Screen1. When we load a form that is not Screen1, this method will be called with the
    // default value for CompatibilityProperty (false). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING, sizingProperty);
    }
  }

  private void setANameProperty(String aname) {
    // The AppName property actually applies to the application and is only visible on Screen1.
    // When we load a form that is not Screen1, this method will be called with the default value
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME, aname);
    }
  }

  /**
   * Forces a re-layout of the child components of the container.
   */
  public final void refresh() {
    Map<MockComponent, LayoutInfo> layoutInfoMap = new HashMap<MockComponent, LayoutInfo>();

    collectLayoutInfos(layoutInfoMap, this);

    LayoutInfo formLayoutInfo = layoutInfoMap.get(this);
    layout.layoutChildren(formLayoutInfo);
    rootPanel.setPixelSize(formLayoutInfo.width,
        Math.max(formLayoutInfo.height, usableScreenHeight));

    for (LayoutInfo layoutInfo : layoutInfoMap.values()) {
      layoutInfo.cleanUp();
    }
    layoutInfoMap.clear();
  }

  /*
   * Collects the LayoutInfo of the given component and, recursively, all of
   * its children.
   *
   * If a component's width/height hint is automatic, the corresponding
   * LayoutInfo's width/height will be set to the calculated width/height.
   * If a component's width/height hint is fill parent, the corresponding
   * LayoutInfo's width/height may be set to fill parent. This will be resolved
   * when layoutChildren is called.
   */
  private static void collectLayoutInfos(Map<MockComponent, LayoutInfo> layoutInfoMap,
      MockComponent component) {

    LayoutInfo layoutInfo = component.createLayoutInfo(layoutInfoMap);

    // If this component is a container, collect the LayoutInfos of its children.
    if (component instanceof MockContainer) {
      if (!layoutInfo.visibleChildren.isEmpty()) {
        // We resize the container to be very large so that we get accurate
        // results when we ask for a child's size using getOffsetWidth/getOffsetHeight.
        // If the container is its normal size (or perhaps the default empty
        // size), then the browser won't give us anything bigger than that
        // when we ask for a child's size.
        if (component.isForm()) {
          ((MockForm) component).rootPanel.setPixelSize(1000, 1000);
        } else {
          component.setPixelSize(1000, 1000);
        }

        // Show children that should be shown and collect their layoutInfos.
        // Note that some MockLayout implementations may hide children that are in the
        // visibleChildren list. For example, in MockTableLayout, if two or more children occupy
        // the same cell in the table, all but one of the children are hidden.
        for (MockComponent child : layoutInfo.visibleChildren) {
          child.setVisible(true);
          collectLayoutInfos(layoutInfoMap, child);
        }
      }

      // Hide children that should be hidden.
      for (MockComponent child : component.getHiddenVisibleChildren()) {
        child.setVisible(false);
      }
    }

    layoutInfo.gatherDimensions();
  }

  /**
   * Adds an {@link FormChangeListener} to the listener set if it isn't already in there.
   *
   * @param listener  the {@code FormChangeListener} to be added
   */
  public void addFormChangeListener(FormChangeListener listener) {
    formChangeListeners.add(listener);
  }

  /**
   * Removes an {@link FormChangeListener} from the listener list.
   *
   * @param listener  the {@code FormChangeListener} to be removed
   */
  public void removeFormChangeListener(FormChangeListener listener) {
    formChangeListeners.remove(listener);
  }

  /**
   * Triggers a component property change event to be sent to the listener on the listener list.
   */
  protected void fireComponentPropertyChanged(MockComponent component,
      String propertyName, String propertyValue) {
    for (FormChangeListener listener : formChangeListeners) {
      listener.onComponentPropertyChanged(component, propertyName, propertyValue);
    }
  }

  /**
   * Triggers a component removed event to be sent to the listener on the listener list.
   */
  protected void fireComponentRemoved(MockComponent component, boolean permanentlyDeleted) {
    for (FormChangeListener listener : formChangeListeners) {
      listener.onComponentRemoved(component, permanentlyDeleted);
    }
  }

  /**
   * Triggers a component added event to be sent to the listener on the listener list.
   */
  protected void fireComponentAdded(MockComponent component) {
    for (FormChangeListener listener : formChangeListeners) {
      listener.onComponentAdded(component);
    }
  }

  /**
   * Triggers a component renamed event to be sent to the listener on the listener list.
   */
  protected void fireComponentRenamed(MockComponent component, String oldName) {
    for (FormChangeListener listener : formChangeListeners) {
      listener.onComponentRenamed(component, oldName);
    }
  }

  /**
   * Triggers a component selection change event to be sent to the listener on the listener list.
   */
  protected void fireComponentSelectionChange(MockComponent component, boolean selected) {
    for (FormChangeListener listener : formChangeListeners) {
      listener.onComponentSelectionChange(component, selected);
    }
  }

  /**
   * Changes the component that is currently selected in the form.
   * <p>
   * There will always be exactly one component selected in a form
   * at any given time.
   */
  public final void setSelectedComponent(MockComponent newSelectedComponent) {
    MockComponent oldSelectedComponent = selectedComponent;

    if (newSelectedComponent == null) {
      throw new IllegalArgumentException("at least one component must always be selected");
    }
    if (newSelectedComponent == oldSelectedComponent) {
      return;
    }

    selectedComponent = newSelectedComponent;

    if (oldSelectedComponent != null) {     // Can be null initially
      oldSelectedComponent.onSelectedChange(false);
    }
    newSelectedComponent.onSelectedChange(true);
  }

  public final MockComponent getSelectedComponent() {
    return selectedComponent;
  }

  /**
   * Builds a tree of the component hierarchy of the form for display in the
   * {@code SourceStructureExplorer}.
   *
   * @return  tree showing the component hierarchy of the form
   */
  public TreeItem buildComponentsTree() {
    return buildTree();
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    // Apply changed properties to the mock component
    if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDIMAGE)) {
      setBackgroundImageProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SCREEN_ORIENTATION)) {
      setScreenOrientationProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SCROLLABLE)) {
      setScrollableProperty(newValue);
      adjustAlignmentDropdowns();
    } else if (propertyName.equals(PROPERTY_NAME_TITLE)) {
      titleBar.changeTitle(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SIZING)) {
      if (newValue.equals("Fixed")){ // Disable Tablet Preview
        editor.getVisibleComponentsPanel().enableTabletPreviewCheckBox(false);
      }
      else {
        editor.getVisibleComponentsPanel().enableTabletPreviewCheckBox(true);
      }
      setSizingProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ICON)) {
      setIconProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_VCODE)) {
      setVCodeProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_VNAME)) {
      setVNameProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ANAME)) {
      setANameProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_HORIZONTAL_ALIGNMENT)) {
      myLayout.setHAlignmentFlags(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_VERTICAL_ALIGNMENT)) {
      myLayout.setVAlignmentFlags(newValue);
      refreshForm();
    }
  }

  // enableAndDisable It should not be called until the component is initialized.
  // Otherwise, we'll get NPEs in trying to use myAlignmentPropertyEditor.
  private void adjustAlignmentDropdowns() {
    if (initialized) enableAndDisableDropdowns();
  }

  // Don't forget to call this on initialization!!!
  // If scrollable is True, the selector for vertical alignment should be disabled.
  private void enableAndDisableDropdowns() {
    String scrollable = properties.getProperty(PROPERTY_NAME_SCROLLABLE).getValue();
    if (scrollable.equals("True")) {
      myVAlignmentPropertyEditor.disable();
    } else {
      myVAlignmentPropertyEditor.enable();
    }
  }

  @Override
  public EditableProperties getProperties() {
    // Before we return the Properties object, we make sure that the
    // Sizing property has the value from the project's properties
    // this is because Sizing is per project, not per Screen(Form)
    // We only have to do this on screens other then screen1 because
    // screen1's value is definitive.
    if(!editor.isScreen1()) {
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING,
        editor.getProjectEditor().getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING));
    }
    return properties;
  }

}
