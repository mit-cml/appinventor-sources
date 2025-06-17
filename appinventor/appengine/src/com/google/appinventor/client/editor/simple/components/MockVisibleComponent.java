// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.SimpleNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * Abstract superclass for components with a visual representation.
 *
 */
public abstract class MockVisibleComponent extends MockComponent {

  // Common property names (not all components support all properties).
  protected static final String PROPERTY_NAME_TEXTALIGNMENT = "TextAlignment";
  protected static final String PROPERTY_NAME_BUTTONSHAPE= "Shape";
  protected static final String PROPERTY_NAME_BACKGROUNDCOLOR = "BackgroundColor";
  protected static final String PROPERTY_NAME_BACKGROUNDIMAGE = "BackgroundImage";
  protected static final String PROPERTY_NAME_THUMBCOLORACTIVE = "ThumbColorActive";
  protected static final String PROPERTY_NAME_THUMBCOLORINACTIVE = "ThumbColorInactive";
  protected static final String PROPERTY_NAME_TRACKCOLORACTIVE = "TrackColorActive";
  protected static final String PROPERTY_NAME_TRACKCOLORINACTIVE = "TrackColorInactive";
  protected static final String PROPERTY_NAME_ENABLED = "Enabled";
  protected static final String PROPERTY_NAME_FONTBOLD = "FontBold";
  protected static final String PROPERTY_NAME_FONTITALIC = "FontItalic";
  protected static final String PROPERTY_NAME_FONTSIZE = "FontSize";
  protected static final String PROPERTY_NAME_FONTTYPEFACE = "FontTypeface";
  protected static final String PROPERTY_NAME_TEXT = "Text";
  protected static final String PROPERTY_NAME_LISTVIEW = "ElementsFromString";
  protected static final String PROPERTY_NAME_LISTVIEW_IMAGES = "Picture";
  protected static final String PROPERTY_NAME_SHOW_FILTER_BAR = "ShowFilterBar";
  protected static final String PROPERTY_NAME_TEXTCOLOR = "TextColor";
  // to set color for secondary text of listview items
  protected static final String PROPERTY_NAME_DETAILTEXTCOLOR = "TextColorDetail";
  protected static final String PROPERTY_NAME_FONTSIZEDETAIL = "FontSizeDetail";
  protected static final String PROPERTY_NAME_FONTTYPEFACEDETAIL = "FontTypefaceDetail";
  protected static final String PROPERTY_NAME_ELEMENTCOLOR = "ElementColor";
  protected static final String PROPERTY_NAME_DIVIDERCOLOR = "DividerColor";
  protected static final String PROPERTY_NAME_DIVIDERTHICKNESS = "DividerThickness";
  protected static final String PROPERTY_NAME_ELEMENTCORNERRADIUS = "ElementCornerRadius";
  protected static final String PROPERTY_NAME_ELEMENTMARGINSWIDTH = "ElementMarginsWidth";
  protected static final String PROPERTY_NAME_ORIENTATION = "Orientation";
  protected static final String PROPERTY_NAME_IMAGEHEIGHT = "ImageHeight";
  protected static final String PROPERTY_NAME_IMAGEWIDTH = "ImageWidth";
  protected static final String PROPERTY_NAME_CHECKED = "Checked"; // checkbox and radio button
  protected static final String PROPERTY_NAME_ON = "On"; // toggle switch
  protected static final String PROPERTY_NAME_HINT = "HintText";
  protected static final String PROPERTY_NAME_HTMLFORMAT = "HTMLFormat";
  protected static final String PROPERTY_NAME_VISIBLE = "Visible";
  protected static final String PROPERTY_NAME_WIDTH = "Width";
  protected static final String PROPERTY_NAME_HEIGHT = "Height";
  public static final String PROPERTY_NAME_COLUMN = "Column";
  public static final String PROPERTY_NAME_ROW = "Row";
  protected static final String PROPERTY_NAME_LEFT = "Left";
  protected static final String PROPERTY_NAME_TOP = "Top";
  protected static final String PROPERTY_NAME_LISTVIEW_ADD_DATA = "ListData";
  protected static final String PROPERTY_NAME_LISTVIEW_LAYOUT = "ListViewLayout";

  // Note: the values below are duplicated in Component.java
  // If you change them here, change them there!

  // Length values for width and height
  // A value >= 0 specifies an explicit size.
  public static final int LENGTH_PREFERRED = -1;
  public static final int LENGTH_FILL_PARENT = -2;
  // If the length is <= -1000 then add 1000 and change the sign to
  // get the length is percent of Screen1
  public static final int LENGTH_PERCENT_TAG = -1000;

  public static final int FONT_DEFAULT_SIZE = 14;

  // Useful colors
  protected static final String COLOR_NONE = "00FFFFFF";
  protected static final String COLOR_DEFAULT = "00000000";

  // to be used to check whether we want to show the x and y coordinate
  // properties or not
  private boolean coordPropertiesVisible = false;

  // Stored Settings
  protected String phonePreview = editor.getProjectEditor().getProjectSettingsProperty(
      SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_PHONE_PREVIEW);
  protected String colorAccent = editor.getProjectEditor().getProjectSettingsProperty(
      SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
      SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR);

  /**
   * Creates a new instance of a visible component.
   *
   * @param editor  editor of source file the component belongs to
   */
  MockVisibleComponent(SimpleEditor editor, String type, ImageResource icon) {
    super(editor, type, new Image(icon));
  }

  /**
   * Creates a text property editor that throws an exception if an invalid
   * number is entered.
   *
   * @return a text property editor object with an overridden validate method
   */
  private static TextPropertyEditor makeCoordTextPropertyEditor() {
    return new TextPropertyEditor() {
      @Override
      protected void validate(String text) throws InvalidTextException {
        try {
          Integer.valueOf(text);
        } catch (NumberFormatException e) {
          throw new InvalidTextException("invalid coordinate provided: " + text);
        }
      }
    };
  }

  @Override
  public final void initComponent(Widget widget) {
    super.initComponent(widget);

    // Add standard per-child layout properties
    // NOTE: Not all layouts use these properties
    addProperty(PROPERTY_NAME_COLUMN, "" + ComponentConstants.DEFAULT_ROW_COLUMN, null,
        null, "Appearance", new TextPropertyEditor());
    addProperty(PROPERTY_NAME_ROW, "" + ComponentConstants.DEFAULT_ROW_COLUMN, null,
        null, "Appearance", new TextPropertyEditor());
    addWidthHeightProperties();
  }

  protected void addWidthHeightProperties() {
    addProperty(PROPERTY_NAME_WIDTH, "" + LENGTH_PREFERRED, MESSAGES.widthPropertyCaption(),
        "Appearance", PropertyTypeConstants.PROPERTY_TYPE_LENGTH, null,
        new YoungAndroidLengthPropertyEditor());
    addProperty(PROPERTY_NAME_HEIGHT, "" + LENGTH_PREFERRED, MESSAGES.heightPropertyCaption(),
        "Appearance", PropertyTypeConstants.PROPERTY_TYPE_LENGTH, null,
        new YoungAndroidLengthPropertyEditor());
  }

  @Override
  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_COLUMN)
        || propertyName.equals(PROPERTY_NAME_ROW)) {
      return false;
    } else if (propertyName.equals(PROPERTY_NAME_LEFT)
        || propertyName.equals(PROPERTY_NAME_TOP)) {
      // the visibility of x and y coordinates strictly depends on whether the component
      // is placed inside an absolute arrangement or not
      return this.coordPropertiesVisible;
    }
    return super.isPropertyVisible(propertyName);
  }

  /**
   * {@inheritDoc}
   *
   * This is always {@code true} for subclasses of this class.
   */
  @Override
  public final boolean isVisibleComponent() {
    return true;
  }

  private void setVisibleProperty(String text) {
    boolean visible = Boolean.parseBoolean(text);
    if (!visible && !editor.isLoadComplete()) {
      // As we are loading the scm file and encounter a visble property being set to false, set the
      // expanded field to false. This will make that branch of the components tree initially
      // collapsed.
      expanded = false;
    }
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);

    if (propertyName.equals(PROPERTY_NAME_WIDTH)) {
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_HEIGHT)) {
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_VISIBLE)) {
      setVisibleProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_LEFT)) {
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TOP)) {
      refreshForm();
    }
  }
  
  public SimpleNonVisibleComponentsPanel getNonVisibleComponentsPanel() {
    return editor.getNonVisibleComponentsPanel();
  }

  /**
   * Sets the visibility of x and y coordinate properties.
   *
   * @param value true or false
   */
  public void setCoordPropertiesVisible(boolean value) {
    EditableProperty x = properties.getProperty(PROPERTY_NAME_LEFT);
    EditableProperty y = properties.getProperty(PROPERTY_NAME_TOP);

    if (x == null || y == null) {
      // The subclass hasn't yet been added to an arrangement so it doesn't have positioning
      return;
    }

    this.coordPropertiesVisible = value;
    int type = value ? EditableProperty.TYPE_NORMAL : EditableProperty.TYPE_INVISIBLE;
    x.setType(type);
    y.setType(type);
  }

  /**
   * Returns the visibility of the coordinate properties.
   *
   * @return true iff x and y coordinate properties are visible
   */
  public boolean coordPropertiesVisible() {
    return this.coordPropertiesVisible;
  }
}
