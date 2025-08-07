// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.designer.DesignerChangeListener;
import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaVisibleComponentsPanel;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVerticalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.properties.BadPropertyEditorException;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TreeItem;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Mock Form component. This implementation provides two main preview sizes corresponding to
 * 'normal' and 'large' buckets (http://developer.android.com/guide/practices/screens_support.html).
 * Normal size is a 1:1 with pixels on a device with dpi:160. We use that as the baseline for the
 * browser too. All UI elements should be scaled to DP for buckets other than 'normal'.
 */
public final class MockForm extends MockDesignerRoot implements DesignerRootComponent {
  private static final Logger LOG = Logger.getLogger(MockForm.class.getName());

  /*
   * Widget for the mock form title bar.
   */
  private class TitleBar extends Composite {
    private static final int TITLEBAR_HEIGHT = 24;
    private static final int ACTIONBAR_HEIGHT = 56;
    private static final int NAVBAR_HEIGHT = 44;

    // UI elements
    private Label title;
    private Button menuButton;
    private AbsolutePanel bar;
    private Image bookIconWhite;
    private Image bookIconBlack;
    private boolean actionBar;
    private boolean navBar;
    private String backgroundColor;

    public String getTitle() {
      return title.getText();
    }

    /*
     * Creates a new title bar.
     */
    TitleBar() {
      title = new Label();
      title.setStylePrimaryName("ode-SimpleMockFormTitle");

      menuButton = new Button();
      menuButton.setText("\u22ee");
      menuButton.setStylePrimaryName("ode-SimpleMockFormMenuButton");

      bookIconWhite = new Image(images.bookIconWhite());
      bookIconWhite.setStylePrimaryName("ode-SimpleMockFormIconIOSWhite");
      bookIconBlack = new Image(images.bookIconBlack());
      bookIconBlack.setStylePrimaryName("ode-SimpleMockFormIconIOSBlack");

      bar = new AbsolutePanel();
      bar.add(title);
      bar.add(menuButton);

      initWidget(bar);

      setStylePrimaryName("ode-SimpleMockFormTitleBar");
      setSize("100%", TITLEBAR_HEIGHT + "px");
    }

    /*
     * Changes the title in the title bar.
     */
    void changeTitle(String newTitle) {
      title.setText(newTitle);
    }

    void setActionBar(boolean actionBar , boolean navBar) {
      this.actionBar = actionBar;
      this.navBar = navBar;
      setSize("100%", (navBar ? NAVBAR_HEIGHT : (actionBar ? ACTIONBAR_HEIGHT : TITLEBAR_HEIGHT)) + "px");
      if (actionBar) {
        if (navBar) {
          removeStyleDependentName("ActionBar");
          addStyleDependentName("NavBar");
        } else {
          removeStyleDependentName("NavBar");
          addStyleDependentName("ActionBar");
        }
        MockComponentsUtil.setWidgetBackgroundColor(titleBar.bar, backgroundColor);

      } else {
        removeStyleDependentName("ActionBar");
        MockComponentsUtil.setWidgetBackgroundColor(titleBar.bar, "&HFF696969");
      }
    }

    void changeBookmarkIcon(boolean black) {
      bar.remove(black ? bookIconWhite : bookIconBlack);
      bar.add(black ? bookIconBlack : bookIconWhite);
    }

    void setBackgroundColor(String color) {
      this.backgroundColor = color;
      if (actionBar || navBar) {
        MockComponentsUtil.setWidgetBackgroundColor(titleBar.bar, color);
      }
    }

    int getHeight() {
      return navBar ? NAVBAR_HEIGHT : (actionBar ? ACTIONBAR_HEIGHT : TITLEBAR_HEIGHT);
    }
  }

  /*
   * Widget for a mock phone status bar.
   */
  private class PhoneBar extends Composite {
    private static final int HEIGHT = 24;
    private static final int IPAD_HEIGHT = 20;
    private static final int IPHONEX_HEIGHT = 44;

    // UI elements
    private DockPanel bar;

    private HorizontalPanel phoneBarLeftPanel;
    private HorizontalPanel phoneBarRightPanel;
    private HorizontalPanel iPadPhoneBarLeftPanel;
    private HorizontalPanel iPadPhoneBarRightPanel;
    private Image blackIconsLeft;
    private Image blackIconsRight;
    private Image whiteIconsLeft;
    private Image whiteIconsRight;
    private Image iPadWhiteIconsLeft;
    private Image iPadWhiteIconsRight;
    private Image iPadBlackIconsLeft;
    private Image iPadBlackIconsRight;
    private boolean visible;

    /*
     * Creates a new phone status bar.
     */
    PhoneBar() {
      Image phoneBarImage = new Image(images.phonebar());
      bar = new DockPanel();
      bar.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
      bar.add(phoneBarImage, DockPanel.EAST);

      initWidget(bar);
      setStylePrimaryName("ode-SimpleMockFormPhoneBarAndroidHolo");
      setSize("100%", HEIGHT + "px");
    }

    PhoneBar(String color) {
      Image phoneBarAndroidMaterial = new Image(images.phonebarAndroidMaterial());

      bar = new DockPanel();
      bar.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
      bar.add(phoneBarAndroidMaterial, DockPanel.EAST);

      initWidget(bar);
      MockComponentsUtil.setWidgetBackgroundColor(bar, color);
      setStylePrimaryName("ode-SimpleMockFormPhoneBarAndroidMaterial");
      setSize("100%", HEIGHT + "px");
    }

    PhoneBar(boolean blackIcons, int size, String color) {
      // icons for iPhone
      blackIconsLeft = new Image(images.phonebariPhoneLeftBlack());
      blackIconsRight = new Image(images.phonebariPhoneRightBlack());
      whiteIconsLeft = new Image(images.phonebariPhoneLeftWhite());
      whiteIconsRight = new Image(images.phonebariPhoneRightWhite());

      phoneBarLeftPanel = new HorizontalPanel();
      phoneBarLeftPanel.setStylePrimaryName("ode-SimpleMockFormLeft");
      phoneBarRightPanel = new HorizontalPanel();
      phoneBarRightPanel.setStylePrimaryName("ode-SimpleMockFormRight");

      //icons for iPad
      iPadWhiteIconsLeft = new Image(images.phonebariPadLeftWhite());
      iPadWhiteIconsRight = new Image(images.phonebariPadRightWhite());
      iPadBlackIconsLeft = new Image(images.phonebariPadLeftBlack());
      iPadBlackIconsRight = new Image(images.phonebariPadRightBlack());

      iPadPhoneBarLeftPanel = new HorizontalPanel();
      iPadPhoneBarLeftPanel.setStylePrimaryName("ode-SimpleMockFormLeftIPad");
      iPadPhoneBarRightPanel = new HorizontalPanel();
      iPadPhoneBarRightPanel.setStylePrimaryName("ode-SimpleMockFormRightIPad");

      bar = new DockPanel();
      setIconColor(blackIcons, size);
      bar.add(phoneBarLeftPanel, DockPanel.WEST);
      bar.add(phoneBarRightPanel, DockPanel.EAST);
      bar.add(iPadPhoneBarLeftPanel, DockPanel.WEST);
      bar.add(iPadPhoneBarRightPanel, DockPanel.EAST);
      initWidget(bar);
      MockComponentsUtil.setWidgetBackgroundColor(bar, color);
      setStylePrimaryName("ode-SimpleMockFormPhoneBariOS");
      setSize(size);
    }

    // changing Icons in Device Default and Black Title Text
    void setIconColor(boolean blackIcons, int size) {
      if (size == 0) {
        bar.removeStyleDependentName("iPad");
        bar.addStyleDependentName("iPhone");
        phoneBarLeftPanel.remove(whiteIconsLeft);
        phoneBarLeftPanel.remove(blackIconsLeft);
        phoneBarRightPanel.remove(whiteIconsRight);
        phoneBarRightPanel.remove(blackIconsRight);
        phoneBarLeftPanel.add(blackIcons ? blackIconsLeft : whiteIconsLeft);
        phoneBarRightPanel.add(blackIcons ? blackIconsRight : whiteIconsRight);
      } else {
        bar.removeStyleDependentName("iPhone");
        bar.addStyleDependentName("iPad");
        iPadPhoneBarLeftPanel.remove(iPadWhiteIconsLeft);
        iPadPhoneBarLeftPanel.remove(iPadBlackIconsLeft);
        iPadPhoneBarRightPanel.remove(iPadWhiteIconsRight);
        iPadPhoneBarRightPanel.remove(iPadBlackIconsRight);
        iPadPhoneBarLeftPanel.add(blackIcons ? iPadBlackIconsLeft : iPadWhiteIconsLeft);
        iPadPhoneBarRightPanel.add(blackIcons? iPadBlackIconsRight : iPadWhiteIconsRight);
      }
    }

    //set status bar size for the iOS theme
    void setSize(int size) {
      setSize("100%", (size == 0 ? IPHONEX_HEIGHT : IPAD_HEIGHT) + "px");
    }

    void setVisibility(boolean visible) {
      this.visible = visible;
    }

    int getHeight() {
      // Adjust for CSS borders, which are not included in the height value
      return visible ? PhoneBar.HEIGHT + 3 : 0;
    }
  }

  /*
   *
   * Widget for a mock phone navigation bar; Shows at the bottom of the viewer
   */
  private class NavigationBar extends Composite {
    private static final int HEIGHT = 44;

    // UI elements
    private AbsolutePanel bar;

    /*
     * Creates a new phone navigation bar; Shows at the bottom of the viewer.
     */
    NavigationBar() {
      bar = new AbsolutePanel();
      initWidget(bar);

      setStylePrimaryName("ode-SimpleMockFormNavigationBarPortrait");
    }

    public int getHeight() {
      return HEIGHT;
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

  private static final int PHONE_PORTRAIT_WIDTH_iPHONE = 320;
  private static final int PHONE_PORTRAIT_HEIGHT_iPHONE= 650;
  private static final int PHONE_LANDSCAPE_WIDTH_iPHONE = PHONE_PORTRAIT_HEIGHT_iPHONE;
  private static final int PHONE_LANDSCAPE_HEIGHT_iPHONE= PHONE_PORTRAIT_WIDTH_iPHONE;

  // These are default values but they can be changed in the changePreviewSize method
  private int PORTRAIT_WIDTH = PHONE_PORTRAIT_WIDTH;
  private int PORTRAIT_HEIGHT = PHONE_PORTRAIT_HEIGHT;
  private int LANDSCAPE_WIDTH = PHONE_LANDSCAPE_WIDTH;
  private int LANDSCAPE_HEIGHT = PHONE_LANDSCAPE_HEIGHT;
  private boolean landscape = false;
  private int idxPhoneSize = 0;

  //Default values for theme style
  private boolean changePreviewFlag = false;
  private boolean blackIcons = false ;
  private int idxPhonePreviewStyle = -1;
  private String primaryDarkColor="&HFF41521C";
  private String primaryColor="&HFFA5CF47";
  private boolean actionBar = false;
  private boolean showStatusBar = true;

  // Property names
  private static final String PROPERTY_NAME_TITLE = "Title";
  private static final String PROPERTY_NAME_SCREEN_ORIENTATION = "ScreenOrientation";
  private static final String PROPERTY_NAME_SCROLLABLE = "Scrollable";
  private static final String PROPERTY_NAME_ICON = "Icon";
  private static final String PROPERTY_NAME_VCODE = "VersionCode";
  private static final String PROPERTY_NAME_VNAME = "VersionName";
  private static final String PROPERTY_NAME_ANAME = "AppName";
  private static final String PROPERTY_NAME_SIZING = "Sizing"; // Don't show except on screen1
  private static final String PROPERTY_NAME_TITLEVISIBLE = "TitleVisible";
  private static final String PROPERTY_NAME_SHOW_STATUS_BAR = "ShowStatusBar";
  // Don't show except on screen1
  private static final String PROPERTY_NAME_SHOW_LISTS_AS_JSON = "ShowListsAsJson";
  private static final String PROPERTY_NAME_TUTORIAL_URL = "TutorialURL";
  private static final String PROPERTY_NAME_BLOCK_SUBSET = "BlocksToolkit";
  private static final String PROPERTY_NAME_ACTIONBAR = "ActionBar";
  private static final String PROPERTY_NAME_PRIMARY_COLOR = "PrimaryColor";
  private static final String PROPERTY_NAME_PRIMARY_COLOR_DARK = "PrimaryColorDark";
  private static final String PROPERTY_NAME_ACCENT_COLOR = "AccentColor";
  private static final String PROPERTY_NAME_THEME = "Theme";
  private static final String PROPERTY_NAME_DEFAULTFILESCOPE = "DefaultFileScope";

  // Form UI components
  AbsolutePanel formWidget;
  AbsolutePanel phoneWidget;
  AbsolutePanel responsivePanel;
  AbsolutePanel notchPanel;

  ScrollPanel scrollPanel;
  private TitleBar titleBar;
  private PhoneBar phoneBar;
  private NavigationBar navigationBar;

  int screenWidth;              // TEMP: Make package visible so we can use it MockHVLayoutBase
  private int screenHeight;
  int usableScreenHeight;       // TEMP: Make package visible so we can use it MockHVLayoutBase
  int usableScreenWidth;

  // Set of listeners for DesignPreviewChanges
  final HashSet<DesignPreviewChangeListener> designPreviewChangeListeners = new HashSet<DesignPreviewChangeListener>();

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

    phoneWidget = new AbsolutePanel();
    phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhonePortrait");
    formWidget = new AbsolutePanel();
    formWidget.setStylePrimaryName("ode-SimpleMockForm");
    notchPanel = new AbsolutePanel();
    notchPanel.setStylePrimaryName("ode-SimpleMockFormNotch");
    notchPanel.getElement().getStyle().clearPosition();
    notchPanel.getElement().getStyle().clearOverflow();
    responsivePanel = new AbsolutePanel();

    // Initialize mock form UI by adding the phone bar and title bar.
    changePreview();
    responsivePanel.add(phoneBar);
    titleBar = new TitleBar();
    responsivePanel.add(titleBar);

    // Put a ScrollPanel around the rootPanel.
    scrollPanel = new ScrollPanel(rootPanel);
    responsivePanel.add(scrollPanel);

    formWidget.add(responsivePanel);

    //Add navigation bar at the bottom of the viewer.
    navigationBar = new NavigationBar();
    formWidget.add(navigationBar);

    phoneWidget.add(formWidget);
    phoneWidget.add(notchPanel);
    initComponent(phoneWidget);

    // Set up the initial state of the vertical alignment property editor and its dropdowns
    try {
      myVAlignmentPropertyEditor = PropertiesUtil.getVAlignmentEditor(properties);
    } catch (BadPropertyEditorException e) {
      LOG.info(MESSAGES.badAlignmentPropertyEditorForArrangement());
      return;
    }
    enableAndDisableDropdowns();
    initialized = true;
    // Now that the default for Scrollable is false, we need to force setting the property when creating the MockForm
    setScrollableProperty(getPropertyValue(PROPERTY_NAME_SCROLLABLE));
  }

  public void changePreviewSize(int width, int height, int idx) {
    // It will definitely be modified in the future to add more options.
    PORTRAIT_WIDTH = width;
    PORTRAIT_HEIGHT = height;
    LANDSCAPE_WIDTH = height;
    LANDSCAPE_HEIGHT = width;

    idxPhoneSize = idx;
    setPhoneStyle();
    updateScreenSize();
  }

  private void updateScreenSize() {
    if (landscape) {
      if (idxPhoneSize == 0 && idxPhonePreviewStyle == 2) {
        resizePanel(PHONE_LANDSCAPE_WIDTH_iPHONE, PHONE_LANDSCAPE_HEIGHT_iPHONE);
      } else {
        resizePanel(LANDSCAPE_WIDTH, LANDSCAPE_HEIGHT);
      }
    } else {
      if (idxPhoneSize == 0 && idxPhonePreviewStyle == 2) {
        resizePanel(PHONE_PORTRAIT_WIDTH_iPHONE, PHONE_PORTRAIT_HEIGHT_iPHONE);
      } else {
        resizePanel(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
      }
    }
  }

  public void changePhonePreview(int idx, String previewName ) {
    // storing the new preview style in the user settings
    editor.getProjectEditor().changeProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW, previewName);

    idxPhonePreviewStyle = idx;
    changePreviewFlag = true;
    changePreview();
    setPhoneStyle();
    updateScreenSize();
  }

  private void setPhoneStyle() {
    if (landscape) {
      if (idxPhoneSize == 1) {
        phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhoneLandscapeTablet");
      } else if (idxPhoneSize == 2) {
        phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhoneLandscapeMonitor");
      } else {
        phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhoneLandscape");
      }
      navigationBar.setStylePrimaryName("ode-SimpleMockFormNavigationBarLandscape");
    } else {
      if (idxPhoneSize == 1) {
        phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhonePortraitTablet");
      } else if (idxPhoneSize == 2) {
        phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhonePortraitMonitor");
      } else {
        phoneWidget.setStylePrimaryName("ode-SimpleMockFormPhonePortrait");
      }
      navigationBar.setStylePrimaryName("ode-SimpleMockFormNavigationBarPortrait");
    }
    if (idxPhonePreviewStyle == 2) {
      setIOSPhoneStyle();
    }
    fireDesignPreviewChange();
  }

  private static String[] iosSizesLandscape = {
      "ode-SimpleMockFormIOSLandscape",
      "ode-SimpleMockFormIOSLandscapeTablet",
      "ode-SimpleMockFormIOSLandscapeMonitor"
  };

  private static String[] iosSizesPortrait = {
      "ode-SimpleMockFormIOSPortrait",
      "ode-SimpleMockFormIOSPortraitTablet",
      "ode-SimpleMockFormIOSPortraitMonitor"
  };

  private void setIOSPhoneStyle() {
    if (landscape) {
      phoneWidget.setStylePrimaryName(iosSizesLandscape[idxPhoneSize]);
    } else {
      phoneWidget.setStylePrimaryName(iosSizesPortrait[idxPhoneSize]);
    }
    navigationBar.setStylePrimaryName("ode-SimpleMockFormNavigationBarIOS");
    phoneBar.setIconColor(blackIcons, idxPhoneSize);
    phoneBar.setSize(idxPhoneSize);
  }

  /*
   * Resizes the scrollPanel, responsivePanel, and formWidget based on the screen size.
   */
  private void resizePanel(int newWidth, int newHeight){
    screenWidth = newWidth;
    screenHeight = newHeight;
    int scrollbarWidth = 0;
    if (Boolean.parseBoolean(getPropertyValue(PROPERTY_NAME_SCROLLABLE))) {
      // only display space for a scrollbar if the form is scrollable
      scrollbarWidth = getVerticalScrollbarWidth();
    }

    if (landscape) {
      String val = editor.getProjectEditor().getProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW);
      if (val.equals("iOS")) {
        usableScreenWidth = screenWidth;
      } else {
        usableScreenWidth = screenWidth - navigationBar.getHeight();
      }
      usableScreenHeight = screenHeight - phoneBar.getHeight() - titleBar.getHeight();
    } else {
      usableScreenWidth = screenWidth;
      usableScreenHeight = screenHeight - phoneBar.getHeight() - titleBar.getHeight() - navigationBar.getHeight();
    }
    rootPanel.setPixelSize(usableScreenWidth, usableScreenHeight);
    // This margin is to ensure the mockform aligns to the left when there is space for a scrollbar
    rootPanel.getElement().getStyle().setProperty("marginRight", scrollbarWidth + "px");
    scrollPanel.setPixelSize(usableScreenWidth + scrollbarWidth, usableScreenHeight);
    formWidget.setPixelSize(screenWidth + scrollbarWidth, screenHeight);
    // Added width to phoneWidget to prevent it from expanding wider than intended
    // if the SimpleComponentsPanel menu is wider than the phonebar.
    phoneWidget.setWidth(usableScreenWidth + scrollbarWidth + "px");
    // Store properties
    if (hasProperty(PROPERTY_NAME_WIDTH)) {  // Not true when project initializing
      changeProperty(PROPERTY_NAME_WIDTH, "" + usableScreenWidth);
    }
    boolean scrollable = Boolean.parseBoolean(getPropertyValue(PROPERTY_NAME_SCROLLABLE));
    if (!scrollable) {
      if (hasProperty(PROPERTY_NAME_HEIGHT)) {  // Not true when project initializing
        changeProperty(PROPERTY_NAME_HEIGHT, "" + usableScreenHeight);
      }
    }
  }

  /*
   * Changes the preview of MockForm based on Android Holo, Android Material and iOS styles
   */
  private void changePreview() {
    // this condition prevents adding multiple phoneBars and titleBars
    if (changePreviewFlag)  {
      responsivePanel.remove(phoneBar);
      responsivePanel.remove(titleBar);
    }

    if (idxPhonePreviewStyle == -1) {
      phoneBar = new PhoneBar();
      formWidget.removeStyleDependentName("AndroidMaterial");
      formWidget.removeStyleDependentName("iOS");
      formWidget.removeStyleDependentName("AndroidHolo");
    } else if (idxPhonePreviewStyle == 0) {
      phoneBar = new PhoneBar(primaryDarkColor);
      formWidget.removeStyleDependentName("AndroidHolo");
      formWidget.removeStyleDependentName("iOS");
      formWidget.addStyleDependentName("AndroidMaterial");
    } else if (idxPhonePreviewStyle == 1) {
      phoneBar = new PhoneBar();
      formWidget.removeStyleDependentName("AndroidMaterial");
      formWidget.removeStyleDependentName("iOS");
      formWidget.addStyleDependentName("AndroidHolo");
    } else if (idxPhonePreviewStyle == 2) {
      phoneBar = new PhoneBar(blackIcons, idxPhoneSize, primaryColor);
      formWidget.removeStyleDependentName("AndroidMaterial");
      formWidget.removeStyleDependentName("AndroidHolo");
      formWidget.addStyleDependentName("iOS");
    }

    // updating changes to the MockForm
    if (changePreviewFlag) {
      formWidget.remove(responsivePanel);
      formWidget.remove(navigationBar);

      ///////////////////////////////////////////////////////////////////
      // Always need to add the phoneBar to the designer, then set the /
      // visibility accordingly!                                       /
      ///////////////////////////////////////////////////////////////////

      responsivePanel.add(phoneBar);
      phoneBar.setVisible(showStatusBar);
      phoneBar.setVisibility(showStatusBar);
      responsivePanel.add(titleBar);
      responsivePanel.add(scrollPanel);
      formWidget.add(responsivePanel);
      formWidget.add(navigationBar);
      if (idxPhonePreviewStyle != 2) {
        titleBar.setActionBar(actionBar, false);
      } else {
        titleBar.setActionBar(true, true);
        titleBar.changeBookmarkIcon(blackIcons);
      }
    }
    changePreviewFlag = false;
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
        "Appearance", PropertyTypeConstants.PROPERTY_TYPE_LENGTH, null,
        new YoungAndroidLengthPropertyEditor());
    addProperty(PROPERTY_NAME_HEIGHT, "" + LENGTH_PREFERRED, null,
        "Appearance", PropertyTypeConstants.PROPERTY_TYPE_LENGTH, null,
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
    switch (propertyName) {
      case PROPERTY_NAME_WIDTH:
      case PROPERTY_NAME_HEIGHT:
      case PROPERTY_NAME_ACTIONBAR: {
        return false;
      }

      // The Icon property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_ICON:
      // The VersionName property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_VNAME:
      // The VersionCode property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_VCODE:
      // The Sizing property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_SIZING:
      // The AppName property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_ANAME:
      // The ShowListsAsJson property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_SHOW_LISTS_AS_JSON:
      // The TutorialURL property actually applies to the application and is only visible on Screen1.
      case PROPERTY_NAME_TUTORIAL_URL:
      case PROPERTY_NAME_BLOCK_SUBSET:
      case PROPERTY_NAME_PRIMARY_COLOR:
      case PROPERTY_NAME_PRIMARY_COLOR_DARK:
      case PROPERTY_NAME_ACCENT_COLOR:
      case PROPERTY_NAME_THEME:
      case PROPERTY_NAME_DEFAULTFILESCOPE: {
        return false;
      }

      default: {
        return super.isPropertyVisible(propertyName);
      }
    }
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
    String val = editor.getProjectEditor().getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW);
    if (hasProperty(PROPERTY_NAME_WIDTH) && hasProperty(PROPERTY_NAME_HEIGHT)
        && hasProperty(PROPERTY_NAME_SCROLLABLE)) {
      if (text.equalsIgnoreCase("landscape")) {
        if (val.equals("iOS") && idxPhoneSize == 0) {
          screenWidth = PHONE_LANDSCAPE_WIDTH_iPHONE;
          screenHeight = PHONE_LANDSCAPE_HEIGHT_iPHONE;
        } else {
          screenWidth = LANDSCAPE_WIDTH;
          screenHeight = LANDSCAPE_HEIGHT;
        }
        landscape = true;
      } else {
        if (val.equals("iOS") && idxPhoneSize == 0) {
          screenWidth = PHONE_PORTRAIT_WIDTH_iPHONE;
          screenHeight = PHONE_PORTRAIT_HEIGHT_iPHONE;
        } else {
          screenWidth = PORTRAIT_WIDTH;
          screenHeight = PORTRAIT_HEIGHT;
        }
        landscape = false;
      }
      setPhoneStyle();
      if (landscape) {
        if (val.equals("iOS")) {
          usableScreenWidth = screenWidth;
        } else {
          usableScreenWidth = screenWidth - navigationBar.getHeight();
        }
        usableScreenHeight = screenHeight - phoneBar.getHeight() - titleBar.getHeight();
      } else {
        usableScreenWidth = screenWidth;
        usableScreenHeight = screenHeight - phoneBar.getHeight() - titleBar.getHeight() - navigationBar.getHeight();
      }
      resizePanel(screenWidth, screenHeight);

      changeProperty(PROPERTY_NAME_WIDTH, "" + usableScreenWidth);
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
    resizePanel(screenWidth, screenHeight);
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

  private void setShowListsAsJsonProperty(String asJson) {
    // This property actually applies to the application and is only visible on
    // Screen1. When we load a form that is not Screen1, this method will be called with the
    // default value for ShowListsAsJsonProperty (false). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON, asJson);
    }
  }

  private void setShowStatusBarProperty(String text) {
    showStatusBar = Boolean.parseBoolean(text);
    phoneBar.setVisible(showStatusBar);
    phoneBar.setVisibility(showStatusBar);
    if (screenWidth == 0 || screenHeight == 0) { // This happens when a project is loaded
      return;                                    // so don't attempt to resize to 0,0
    }
    resizePanel(screenWidth,screenHeight); // update the MockForm size
  }

  private void setTutorialURLProperty(String asJson) {
    // This property actually applies to the application and is only visible on
    // Screen1. When we load a form that is not Screen1, this method will be called with the
    // default value for TutorialURL (""). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL, asJson);
    }
  }

  private void setBlockSubsetProperty(String asJson) {
    //This property applies to the application and is only visible on Screen1. When we load a form that is
    //not Screen1, this method will be called with the default value for SubsetJson (""). We need to ignore that.
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET, asJson);
    }
    
    if (editor.isLoadComplete()) {
      ((YaFormEditor)editor).reloadComponentPalette(asJson);
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

  private void setTitleVisibleProperty(String text) {
    boolean visible = Boolean.parseBoolean(text);
    titleBar.setVisible(visible);
  }

  private void setActionBarProperty(String actionBar) {
    this.actionBar = Boolean.parseBoolean(actionBar);
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR, actionBar);
    }
    titleBar.setActionBar(this.actionBar, false);
    if (initialized) {
      resizePanel(screenWidth, screenHeight);  // update screen due to titlebar size change.
    }
  }

  private void setPrimaryColor(String color) {
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR, color);
    }
    if (color.equals("&H00000000")) {
      // Replace Default with actual default color
      color = ComponentConstants.DEFAULT_PRIMARY_COLOR;
    }
    titleBar.setBackgroundColor(color);
    primaryColor = color;
  }

  private void setPrimaryColorDark(String color) {
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK, color);
    }
    primaryDarkColor= color;
  }

  private void setAccentColor(String color) {
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR, color);
    }
  }

  private void setTheme(String theme) {
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME, theme);
    }
    if (theme.equals("AppTheme.Light")) {
      final String newColor = "&HFF000000";
      blackIcons = true;
      MockComponentsUtil.setWidgetTextColor(titleBar.bar, newColor);
      MockComponentsUtil.setWidgetTextColor(titleBar.menuButton, newColor);
      MockComponentsUtil.setWidgetTextColor(titleBar.title, newColor);
    } else {
      final String newColor = "&HFFFFFFFF";
      blackIcons = false;
      MockComponentsUtil.setWidgetTextColor(titleBar.bar, newColor);
      MockComponentsUtil.setWidgetTextColor(titleBar.menuButton, newColor);
      MockComponentsUtil.setWidgetTextColor(titleBar.title, newColor);
    }
    if (theme.equals("AppTheme")) {
      blackIcons = false;
      formWidget.setStylePrimaryName("ode-SimpleMockFormDark");
    } else {
      formWidget.setStylePrimaryName("ode-SimpleMockForm");
    }

    // Resetting the MockForm with new preview styles
    if (idxPhonePreviewStyle == 0) {
      formWidget.removeStyleDependentName("AndroidHolo");
      formWidget.removeStyleDependentName("iOS");
      formWidget.addStyleDependentName("AndroidMaterial");
    } else if (idxPhonePreviewStyle == 1) {
      formWidget.removeStyleDependentName("AndroidMaterial");
      formWidget.removeStyleDependentName("iOS");
      formWidget.addStyleDependentName("AndroidHolo");
    } else if (idxPhonePreviewStyle == 2) {
      formWidget.removeStyleDependentName("AndroidMaterial");
      formWidget.removeStyleDependentName("AndroidHolo");
      formWidget.addStyleDependentName("iOS");
    }

    if (idxPhonePreviewStyle == 2) {
      phoneBar.setIconColor(blackIcons, idxPhoneSize);
      titleBar.changeBookmarkIcon(blackIcons);
    }

  }

  private void setDefaultFileScope(String defaultFileScope) {
    if (editor.isScreen1()) {
      editor.getProjectEditor().changeProjectSettingsProperty(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
          SettingsConstants.YOUNG_ANDROID_SETTINGS_DEFAULTFILESCOPE, defaultFileScope);
    }
  }

  /**
   * Forces a re-layout of the child components of the container.
   *
   * Each components onPropertyChange listener calls us. This is
   * reasonable during interactive editing because we have to make
   * sure the screen reflects what the user is doing.  However during
   * project load we will be called many times, when really we should
   * only be called after the project's UI is really finished loading.
   *
   * We could add a bunch of complicated code to inhibit refreshes
   * until we know the project's UI is loaded and stable. However that
   * is a change that will be spread over several modules, making it
   * hard to understand what is going on.
   *
   * Instead, I am opting to keep this change self contained within
   * this module. The idea is to see how quickly we are being
   * called. If we receive a call which is close in time (within
   * seconds) of a previous call, we set a timer to fire in the
   * reasonable future (say 2 seconds). While this timer is counting
   * down, we ignore any other calls to refresh. Whatever refreshing
   * they would do will be handled by the call done when the timer
   * fires. This approach does not reduce the number of calls to
   * refresh during project loading to 1. But it significantly reduces
   * the number of calls and gets us out of the exponential explosion
   * in time and memory that we see with projects with hundreds of
   * design elements (yes, people do that, and I have seen at least
   * one project that was this big and reasonable!).  -Jeff Schiller
   * (jis@mit.edu).
   *
   */

  private Timer refreshTimer = null;
  public final void refresh() {
    if (refreshTimer != null) return;
    refreshTimer = new Timer() {
      @Override
      public void run() {
        doRefresh();
        refreshTimer = null;
      }
    };
    refreshTimer.schedule(0);
  }

  public void refresh(boolean force) {
    if (force) {
      // If we are forcing a refresh, cancel any pending refresh timer.
      if (refreshTimer != null) {
        refreshTimer.cancel();
        refreshTimer = null;
      }
      doRefresh();
    } else {
      // Otherwise, we use the throttled refresh.
      refresh();
    }
  }

  /*
   * Do the actual refresh.
   *
   * This method is public because it is called directly from MockComponent for refreshes
   * which bypass throttling.
   *
   */

  public final void doRefresh() {
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
   * Adds an {@link DesignPreviewChangeListener} to the listener set if it isn't already
   * there.
   *
   * @param listener the {@code DesignPreviewChangeListener} to be added
   */
  public void addDesignPreviewChangeListener(DesignPreviewChangeListener listener) {
    designPreviewChangeListeners.add(listener);
  }

  /**
   * Removes an {@link DesignPreviewChangeListener} from the listener list.
   *
   * @param listener the {@code DesignPreviewChangeListener} to be removed.
   */

  public void removeDesignPreviewChangeListener(DesignPreviewChangeListener listener) {
    designPreviewChangeListeners.remove(listener);
  }

  /**
   * Triggers the DesignChangePreviewChange listeners
   */
  protected void fireDesignPreviewChange() {
    for (DesignPreviewChangeListener listener : designPreviewChangeListeners) {
      listener.onDesignPreviewChanged();
    }
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
        ((YaVisibleComponentsPanel) editor.getVisibleComponentsPanel()).enableTabletPreviewCheckBox(false);
      }
      else {
        ((YaVisibleComponentsPanel) editor.getVisibleComponentsPanel()).enableTabletPreviewCheckBox(true);
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
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_LISTS_AS_JSON)) {
      setShowListsAsJsonProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_STATUS_BAR)) {
      setShowStatusBarProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_TUTORIAL_URL)) {
      setTutorialURLProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_BLOCK_SUBSET)) {
      setBlockSubsetProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_ACTIONBAR)) {
      setActionBarProperty(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_THEME)) {
      if ("Classic".equals(newValue)) {
        ((YaVisibleComponentsPanel) editor.getVisibleComponentsPanel()).enablePhonePreviewCheckBox(false);
        getProperties().getExistingProperty(PROPERTY_NAME_ACTIONBAR).setValue("False");
      } else {
        ((YaVisibleComponentsPanel) editor.getVisibleComponentsPanel()).enablePhonePreviewCheckBox(true);
        getProperties().getExistingProperty(PROPERTY_NAME_ACTIONBAR).setValue("True");
      }
      setTheme(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_DEFAULTFILESCOPE)) {
      setDefaultFileScope(newValue);
    } else if (propertyName.equals(PROPERTY_NAME_PRIMARY_COLOR)) {
      setPrimaryColor(newValue);
      if (idxPhonePreviewStyle == 2) {
        MockComponentsUtil.setWidgetBackgroundColor(phoneBar, newValue);
      }
    } else if (propertyName.equals(PROPERTY_NAME_PRIMARY_COLOR_DARK)) {
      setPrimaryColorDark(newValue);
      if (idxPhonePreviewStyle == 0) {
        MockComponentsUtil.setWidgetBackgroundColor(phoneBar, newValue);
      }
    } else if (propertyName.equals(PROPERTY_NAME_ACCENT_COLOR)) {
      setAccentColor(newValue);
      fireDesignPreviewChange();
    } else if (propertyName.equals(PROPERTY_NAME_HORIZONTAL_ALIGNMENT)) {
      myLayout.setHAlignmentFlags(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_VERTICAL_ALIGNMENT)) {
      myLayout.setVAlignmentFlags(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TITLEVISIBLE)) {
      setTitleVisibleProperty(newValue);
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

  public void projectPropertyChanged() {
    ((YaFormEditor) editor).refreshCurrentPropertiesPanel();
  }

  @Override
  public EditableProperties getProperties() {
    // Before we return the Properties object, we make sure that the
    // Sizing, ShowListsAsJson and TutorialURL properties have the
    // value from the project's properties this is because these are
    // per project, not per Screen(Form) We only have to do this on
    // screens other then screen1 because screen1's value is
    // definitive.
    if (!editor.isScreen1()) {
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_BLOCK_SUBSET));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK));
      properties.changePropertyValue(SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR,
          editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR));
    }
    return properties;
  }

}
