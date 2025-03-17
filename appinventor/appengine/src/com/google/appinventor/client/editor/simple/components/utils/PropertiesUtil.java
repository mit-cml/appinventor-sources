// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components.utils;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;

import com.google.appinventor.client.editor.youngandroid.YaFormEditor;

import com.google.appinventor.client.editor.youngandroid.palette.YoungAndroidPalettePanel;

import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAccelerometerSensitivityChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAssetSelectorPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidBestFitModelPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidBooleanPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidButtonShapeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidChartLineTypeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidChartPointShapeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidChartTypeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidChatbotModelPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidChatbotProviderPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidColorChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidComponentSelectorPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidDataColumnSelectorProperty;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidDefaultURLPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFileScopePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFloatRangePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFontTypefaceChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidGeoJSONPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidGeographicPointPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidIntegerRangePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3SensorPortChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoEv3UltrasonicSensorModeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoNxtSensorPortChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLengthPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidListViewAddDataPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidListViewLayoutChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidMapScaleUnitsPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidMapTypePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidMapCustomUrlPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidNavigationMethodChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidRecyclerViewOrientationPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidScreenAnimationChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidScreenOrientationChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSensorDistIntervalChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSensorTimeIntervalChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSizingChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidSpriteOriginPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidStrokeStylePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidTextReceivingPropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidThemeChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidToastLengthChoicePropertyEditor;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVerticalAlignmentChoicePropertyEditor;

import com.google.appinventor.client.properties.BadPropertyEditorException;
import com.google.appinventor.client.properties.Property;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.CountryChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.FloatPropertyEditor;
import com.google.appinventor.client.widgets.properties.IntegerPropertyEditor;
import com.google.appinventor.client.widgets.properties.LanguageChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.NonNegativeFloatPropertyEditor;
import com.google.appinventor.client.widgets.properties.NonNegativeIntegerPropertyEditor;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.ScalingChoicePropertyEditor;
import com.google.appinventor.client.widgets.properties.StringPropertyEditor;
import com.google.appinventor.client.widgets.properties.SubsetJSONPropertyEditor;
import com.google.appinventor.client.widgets.properties.TextAreaPropertyEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;

import com.google.appinventor.components.common.PropertyTypeConstants;

import com.google.appinventor.shared.simple.ComponentDatabaseInterface;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Provides utility functions for dealing with the UI for component properties.
 *
 * These functions are called by descendants of MockComponent with its protected
 * field "properties" as an argument. This EditableProperties object is populated
 * by {@link YoungAndroidPalettePanel#configureComponent(MockComponent)}, which
 * calls {@link MockComponent#addProperty(String, String, String, PropertyEditor)}
 * for each of the MockComponent's properties. The addProperty method in turn calls
 * {@link EditableProperties#addProperty(String, String, String, PropertyEditor, int)},
 * adding the property to "properties".
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class PropertiesUtil {
  // Construct a HashSet of acceptable Chart Data Source components
  private static final HashSet<String> CHART_DATA_SOURCES = new HashSet<String>() {{
      add("DataFile");
      add("TinyDB");
      add("CloudDB");
      add("AccelerometerSensor");
      add("BluetoothClient");
      add("Web");
      add("GyroscopeSensor");
      add("OrientationSensor");
      add("ProximitySensor");
      add("Pedometer");
      add("LocationSensor");
      add("Spreadsheet");
    }};


  /**
   * Prevent instantiation.
   */
  private PropertiesUtil() {
  }

  /**
   * Populate properties of a MockComponent, given a list of property definitions
   * @param mockComponent
   * @param propertyDefinitions
   */
  public static void populateProperties(MockComponent mockComponent, List<ComponentDatabaseInterface.PropertyDefinition> propertyDefinitions,
                                        YaFormEditor editor) {
    // Configure properties
    for (ComponentDatabaseInterface.PropertyDefinition property : propertyDefinitions) {
      mockComponent.addProperty(property.getName(), property.getDefaultValue(),
          property.getCaption(), property.getCategory(), property.getEditorType(), property.getEditorArgs(),
          PropertiesUtil.createPropertyEditor(property.getEditorType(), property.getDefaultValue(), editor,
          property.getEditorArgs()));
      /*OdeLog.log("Property Caption: " + property.getCaption() + ", "
          + TranslationComponentProperty.getName(property.getCaption()));*/
    }
  }

  // Use individual methods for each property since we can't write the generic
  // getExistingPropertyEditor due to type safety issues - see below.

  /**
   * Retrieves the property editor for Horizontal Alignment.
   *
   * @param  properties
   * @return the property editor for Horizontal Alignment
   * @throws BadPropertyEditorException
   */
  public static  YoungAndroidHorizontalAlignmentChoicePropertyEditor
  getHAlignmentEditor(EditableProperties properties)
      throws BadPropertyEditorException {

    PropertyEditor propEditor = null;

    // Get the property with the specified name from the properties
    // object. This works because an EditableProperties contains a
    // TreeMap<String, T extends Property> that maps a property's name to its instance.
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_HORIZONTAL_ALIGNMENT);

    if (prop != null) {
      if (prop instanceof EditableProperty) {
        // Cast prop to an EditableProperty and get its editor
        propEditor = ((EditableProperty) prop).getEditor();

        // Make sure we have the right editor
        if (propEditor != null) {
          if (! (propEditor instanceof YoungAndroidHorizontalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }

    // Cast propEditor to the appropriate type
    return (YoungAndroidHorizontalAlignmentChoicePropertyEditor)propEditor;
  }

  /**
   * Retrieves the property editor for Vertical Alignment.
   * Note that this is almost identical to {@link #getHAlignmentEditor(EditableProperties)}
   * in terms of implementation.
   *
   * @param  properties
   * @return the property editor for Vertical Alignment
   * @throws BadPropertyEditorException
   */
  public static  YoungAndroidVerticalAlignmentChoicePropertyEditor
  getVAlignmentEditor(EditableProperties properties)
      throws BadPropertyEditorException {

    PropertyEditor propEditor = null;
    Property prop = properties.getProperty(MockForm.PROPERTY_NAME_VERTICAL_ALIGNMENT);
    if (prop != null) {
      if (prop instanceof EditableProperty) {
        propEditor = ((EditableProperty) prop).getEditor();
        if (propEditor != null) {
          if (! (propEditor instanceof YoungAndroidVerticalAlignmentChoicePropertyEditor))
            throw new BadPropertyEditorException("Bad property editor");
        }
      }
    }
    return (YoungAndroidVerticalAlignmentChoicePropertyEditor)propEditor;
  }

  /*
   * Creates a new property editor.
   */
  public static PropertyEditor createPropertyEditor(String editorType, String defaultValue, YaFormEditor editor, String[] editorArgs) {
    if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_HORIZONTAL_ALIGNMENT)) {
      return new YoungAndroidHorizontalAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_VERTICAL_ALIGNMENT)) {
      return new YoungAndroidVerticalAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ASSET)) {
      return new YoungAndroidAssetSelectorPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BEST_FIT_MODEL)) {
      return new YoungAndroidBestFitModelPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BLUETOOTHCLIENT)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor,
          // Pass the set of component types that will be shown in the property editor,
          // in this case, just "BluetoothClient".
          Collections.singleton("BluetoothClient"));
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN)) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_BUTTON_SHAPE)) {
      return new YoungAndroidButtonShapeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_COLOR)) {
      return new YoungAndroidColorChoicePropertyEditor(defaultValue);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FILESCOPE)) {
      return new YoungAndroidFileScopePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FLOAT)) {
      return new FloatPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_GEOGRAPHIC_POINT)) {
      return new YoungAndroidGeographicPointPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_GEOJSON_TYPE)) {
      return new YoungAndroidGeoJSONPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SUBSET_JSON)) {
      return new SubsetJSONPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_INTEGER)) {
      return new IntegerPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LATITUDE)) {
      return new YoungAndroidFloatRangePropertyEditor(-90, 90);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_SENSOR_PORT)) {
      return new YoungAndroidLegoEv3SensorPortChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_COLOR_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3ColorSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_ULTRASONIC_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3UltrasonicSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_EV3_GYRO_SENSOR_MODE)) {
      return new YoungAndroidLegoEv3GyroSensorModeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHOICES)) {
      return new ChoicePropertyEditor(editorArgs);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_SENSOR_PORT)) {
      return new YoungAndroidLegoNxtSensorPortChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LEGO_NXT_GENERATED_COLOR)) {
      return new YoungAndroidColorChoicePropertyEditor(
          YoungAndroidColorChoicePropertyEditor.NXT_GENERATED_COLORS, defaultValue);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LENGTH)) {
      return new YoungAndroidLengthPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LONGITUDE)) {
      return new YoungAndroidFloatRangePropertyEditor(-180, 180);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAP_TYPE)) {
      return new YoungAndroidMapTypePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAP_CUSTOMURL)) {
      return new YoungAndroidMapCustomUrlPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAP_UNIT_SYSTEM)) {
      return new YoungAndroidMapScaleUnitsPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_MAP_ZOOM)) {
      return new YoungAndroidIntegerRangePropertyEditor(1, 18);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NAVIGATION_METHOD)) {
      return new YoungAndroidNavigationMethodChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT)) {
      return new NonNegativeFloatPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER)) {
      return new NonNegativeIntegerPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ORIENTATION)) {
      return new YoungAndroidScreenOrientationChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCREEN_ANIMATION)) {
      return new YoungAndroidScreenAnimationChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SENSOR_DIST_INTERVAL)) {
      return new YoungAndroidSensorDistIntervalChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SENSOR_TIME_INTERVAL)) {
      return new YoungAndroidSensorTimeIntervalChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_STRING)) {
      return new StringPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_STROKE_STYLE)) {
      return new YoungAndroidStrokeStylePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT)) {
      return new YoungAndroidAlignmentChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA)) {
      return new TextAreaPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_THEME)) {
      return new YoungAndroidThemeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TOAST_LENGTH)) {
      return new YoungAndroidToastLengthChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE)) {
      return new YoungAndroidFontTypefaceChoicePropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_VISIBILITY)) {
      return new YoungAndroidBooleanPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_RECEIVING)) {
      return new YoungAndroidTextReceivingPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ACCELEROMETER_SENSITIVITY)) {
      return new YoungAndroidAccelerometerSensitivityChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_COUNTRIES)) {
      return new CountryChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_TEXT_TO_SPEECH_LANGUAGES)) {
      return new LanguageChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SIZING)) {
      return new YoungAndroidSizingChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_SCALING)) {
      return new ScalingChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_FIREBASE_URL)) {
      return new YoungAndroidDefaultURLPropertyEditor("DEFAULT");
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_UNIT_COORDINATE)) {
      return new YoungAndroidFloatRangePropertyEditor(0, 1);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_ORIGIN)) {
      return new YoungAndroidSpriteOriginPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_ADD_DATA)) {
      return new YoungAndroidListViewAddDataPropertyEditor(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_LISTVIEW_LAYOUT)) {
      return new YoungAndroidListViewLayoutChoicePropertyEditor();
    } else if (editorType.startsWith(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT + ":")) {
      String type = editorType.substring(PropertyTypeConstants.PROPERTY_TYPE_COMPONENT.length() + 2);
      type = type.substring(type.lastIndexOf('.') + 1);
      return new YoungAndroidComponentSelectorPropertyEditor(editor, Collections.singleton(type));
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHART_TYPE)) {
      return new YoungAndroidChartTypeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_DATA_FILE_COLUMN)) {
      return new YoungAndroidDataColumnSelectorProperty(editor);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHART_DATA_SOURCE)) {
      return new YoungAndroidComponentSelectorPropertyEditor(editor, CHART_DATA_SOURCES);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHART_PIE_RADIUS)) {
      return new YoungAndroidIntegerRangePropertyEditor(0, 100);
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHART_POINT_SHAPE)) {
      return new YoungAndroidChartPointShapeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHART_LINE_TYPE)) {
      return new YoungAndroidChartLineTypeChoicePropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_RECYCLERVIEW_ORIENTATION)) {
      return new YoungAndroidRecyclerViewOrientationPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHATBOT_PROVIDER)) {
      return new YoungAndroidChatbotProviderPropertyEditor();
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_CHATBOT_MODEL)) {
      return new YoungAndroidChatbotModelPropertyEditor();
    } else {
      return new TextPropertyEditor();
    }
  }




  // TODO(hal):  The individual property methods above should be replaced by a single generic
  // method getExistingPropertyEditor that
  // retrieves the property editor for the named property and checks its type.  Here's how the
  // code should go.  While this compiles in Java, it sadly
  // does not compile in GWT, which has not yet implemented type-safe generic casting.
  // Sigh.  Well, maybe someday.

// /**
//  * Returns the existing property editor for the given name.
//  *
//  * @throws IllegalStateException  if no such property editor exists, or if
//  * the editor does not have the required class
//  * @param name  property name
//  * @param C the required class of class of the property editor
//  */

// public <T extends PropertyEditor>T getExistingPropertyEditor(String name, Class<T> C)
//     throws BadPropertyEditorException {
//   PropertyEditor propEditor= null;
//   Property prop = getProperty(name);
//   if (! (prop == null)) {
//     if (prop instanceof EditableProperty) {
//       propEditor = ((EditableProperty) prop).getEditor();
//       if (! (propEditor == null)) {
//         if (! (C.isAssignableFrom(propEditor.getClass()))) {
//           throw new BadPropertyEditorException("Bad property editor");
//         }
//       }
//     }
//   }
//   return C.cast(propEditor);
// }


}
