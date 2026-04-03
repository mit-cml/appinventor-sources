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
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
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
   * Creates a property editor for Left/Top coordinates that:
   * - accepts plain integers ("80") or percent strings ("25%")
   * - displays percent-encoded stored values (e.g. -1025) as "25%" in the panel
   * - always commits the encoded integer form ("-1025") to the property, never "25%"
   *
   * Storing only encoded integers prevents an infinite recursion that would occur if "25%"
   * were stored: MockVisibleComponent.onPropertyChange would convert "25%" → "-1025", then
   * DesignerEditor.onPropertyChange would re-set the property back to "25%", cycling forever.
   */
  private static PropertyEditor makeCoordTextPropertyEditor() {
    return new PropertyEditor() {
      private final TextBox textBox = new TextBox();

      {
        textBox.addBlurHandler(new BlurHandler() {
          @Override public void onBlur(BlurEvent event) { commitValue(); }
        });
        textBox.addKeyUpHandler(new KeyUpHandler() {
          @Override public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
              updateValue();
              textBox.setFocus(false);
            }
          }
        });
        textBox.addKeyPressHandler(new KeyPressHandler() {
          @Override public void onKeyPress(KeyPressEvent event) {
            char c = event.getCharCode();
            if (c == KeyCodes.KEY_ENTER || c == KeyCodes.KEY_TAB) {
              event.preventDefault();
              textBox.setFocus(false);
            }
          }
        });
        initWidget(textBox);
        setHeight("2em");
      }

      @Override
      protected void updateValue() {
        if (property == null) return;
        String raw = property.getValue();
        if (raw == null || raw.isEmpty()) {
          textBox.setText("0");
          return;
        }
        try {
          int v = Integer.parseInt(raw);
          if (v <= LENGTH_PERCENT_TAG) {
            textBox.setText(-(v - LENGTH_PERCENT_TAG) + "%");
            return;
          }
        } catch (NumberFormatException e) {
          // fall through to default display
        }
        textBox.setText(raw);
      }

      /** Reads the text box, encodes to integer form, and stores to property. */
      private void commitValue() {
        if (property == null) return;
        String text = textBox.getText().trim();
        String encoded = encodeCoord(text);
        if (encoded != null) {
          property.setValue(encoded);
        } else {
          updateValue();  // restore on invalid input
        }
      }

      /**
       * Converts a user-typed coord string to its stored integer encoding, or returns null if
       * the text is not a valid coordinate.
       */
      private String encodeCoord(String text) {
        if (text.isEmpty()) return "0";
        if (text.endsWith("%")) {
          try {
            int pct = Integer.parseInt(text.substring(0, text.length() - 1));
            if (pct < 0 || pct > 100) return null;
            return "" + (LENGTH_PERCENT_TAG - pct);
          } catch (NumberFormatException e) {
            return null;
          }
        } else {
          try {
            Integer.parseInt(text);
            return text;
          } catch (NumberFormatException e) {
            return null;
          }
        }
      }

      @Override
      public void setAriaLabelledBy(String labelId) {
        if (labelId != null && !labelId.isEmpty()) {
          textBox.getElement().setAttribute("aria-labelledby", labelId);
        }
      }
    };
  }

  private void upgradeCoordPropertyEditors() {
    for (String propName : new String[]{PROPERTY_NAME_LEFT, PROPERTY_NAME_TOP}) {
      EditableProperty prop = properties.getProperty(propName);
      if (prop == null) continue;
      String currentValue = prop.getValue();
      properties.removeProperty(propName);
      addProperty(propName, "0", propName, "Appearance",
          PropertyTypeConstants.PROPERTY_TYPE_INTEGER, new String[0],
          makeCoordTextPropertyEditor());
      if (!currentValue.isEmpty() && !currentValue.equals("0")) {
        changeProperty(propName, currentValue);
      }
    }
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
    upgradeCoordPropertyEditors();
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
      if (newValue.endsWith("%")) {
        try {
          int pct = Integer.parseInt(newValue.substring(0, newValue.length() - 1));
          changeProperty(PROPERTY_NAME_LEFT, "" + (LENGTH_PERCENT_TAG - pct));
          return;
        } catch (NumberFormatException e) {
          // malformed — fall through to refreshForm
        }
      }
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_TOP)) {
      if (newValue.endsWith("%")) {
        try {
          int pct = Integer.parseInt(newValue.substring(0, newValue.length() - 1));
          changeProperty(PROPERTY_NAME_TOP, "" + (LENGTH_PERCENT_TAG - pct));
          return;
        } catch (NumberFormatException e) {
          // malformed — fall through to refreshForm
        }
      }
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
    // Toggle only the TYPE_INVISIBLE bit, preserving other bits (e.g. TYPE_DOYAIL).
    // Replacing the whole type would strip TYPE_DOYAIL, causing Left/Top to be omitted
    // from the YAIL sent to the companion after a drag-drop.
    if (value) {
      x.setType(x.getType() & ~EditableProperty.TYPE_INVISIBLE);
      y.setType(y.getType() & ~EditableProperty.TYPE_INVISIBLE);
    } else {
      x.setType(x.getType() | EditableProperty.TYPE_INVISIBLE);
      y.setType(y.getType() | EditableProperty.TYPE_INVISIBLE);
    }
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
