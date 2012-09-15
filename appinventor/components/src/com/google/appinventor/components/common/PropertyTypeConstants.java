package com.google.appinventor.components.common;

/**
 * Constants for specifying which
 * {@link com.google.appinventor.client.widgets.properties.PropertyEditor PropertyEditor}
 * should be used for modifying a property value within the Designer.  This is used within
 * {@link com.google.appinventor.components.annotations.DesignerProperty#editorType()}.
 */
public class PropertyTypeConstants {
  private PropertyTypeConstants() {}

  /**
   * User-uploaded assets.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAssetSelectorPropertyEditor
   */
  public static final String PROPERTY_TYPE_ASSET = "asset";

  /**
   * Instances of {@link com.google.appinventor.components.runtime.BluetoothClient}
   * in the current project.
   */
  public static final String PROPERTY_TYPE_BLUETOOTHCLIENT = "BluetoothClient";

  /**
   * Boolean values.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidBooleanPropertyEditor
   */
  public static final String PROPERTY_TYPE_BOOLEAN = "boolean";

  /**
   * Arrangement alignment.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidHorizontalAlignmentChoicePropertyEditor
   */
  public static final String PROPERTY_TYPE_HORIZONTAL_ALIGNMENT = "horizontal_alignment";
  public static final String PROPERTY_TYPE_VERTICAL_ALIGNMENT = "vertical_alignment";


  /**
   * Button shapes.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidButtonShapeChoicePropertyEditor
   */
  public static final String PROPERTY_TYPE_BUTTON_SHAPE = "button_shape";

  /**
   * Any of the colors specified in {@link
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidColorChoicePropertyEditor}.
   */
  public static final String PROPERTY_TYPE_COLOR = "color";

  /**
   * Component instances in the current project.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidComponentSelectorPropertyEditor
   */
  public static final String PROPERTY_TYPE_COMPONENT = "component";

  /**
   * Floating-point values.
   * @see com.google.appinventor.client.widgets.properties.FloatPropertyEditor
   */
  public static final String PROPERTY_TYPE_FLOAT = "float";

  /**
   * Integer values.
   * @see com.google.appinventor.client.widgets.properties.IntegerPropertyEditor
   */
  public static final String PROPERTY_TYPE_INTEGER = "integer";

  /**
   * Lego NXT sensor ports.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidLegoNxtSensorPortChoicePropertyEditor
   */
  public static final String PROPERTY_TYPE_LEGO_NXT_SENSOR_PORT = "lego_nxt_sensor_port";

  /**
   * Colors recognizable by Lego NXT sensors.
   * @see
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidColorChoicePropertyEditor#NXT_GENERATED_COLORS
   */
  public static final String PROPERTY_TYPE_LEGO_NXT_GENERATED_COLOR = "lego_nxt_generated_color";

  /**
   * Non-negative (positive or zero) floating-point values.
   * @see com.google.appinventor.client.widgets.properties.NonNegativeFloatPropertyEditor
   */
  public static final String PROPERTY_TYPE_NON_NEGATIVE_FLOAT = "non_negative_float";

  /**
   * Non-negative (positive or zero) integers.
   * @see com.google.appinventor.client.widgets.properties.NonNegativeIntegerPropertyEditor
   */
  public static final String PROPERTY_TYPE_NON_NEGATIVE_INTEGER = "non_negative_integer";

  /**
   * Choices of screen orientations offered by {@link
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidScreenOrientationChoicePropertyEditor}.
   */
  public static final String PROPERTY_TYPE_SCREEN_ORIENTATION = "screen_orientation";

   /**
    * Minimum distance interval, in meters, that the location sensor will try to use
    * for sending out location updates. See {@link com.google.appinventor.components.runtime.LocationSensor}.
    */
  public static final String PROPERTY_TYPE_SENSOR_DIST_INTERVAL = "sensor_dist_interval";

  /**
   * Minimum time interval, in milliseconds, that the location sensor use to send out
   * location updates. See {@link com.google.appinventor.components.runtime.LocationSensor}.
   */
  public static final String PROPERTY_TYPE_SENSOR_TIME_INTERVAL = "sensor_time_interval";

  /**
   * Strings.  This has the same effect as, but is preferred in component
   * definitions to, {@link #PROPERTY_TYPE_TEXT}).
   * @see com.google.appinventor.client.widgets.properties.StringPropertyEditor
   */
  public static final String PROPERTY_TYPE_STRING = "string";

 /**
  * Text.  This has the same effect as {@link #PROPERTY_TYPE_STRING}, which
  * is preferred everywhere except as the default value for {@link
  * com.google.appinventor.components.annotations.DesignerProperty#editorType}.
  * @see com.google.appinventor.client.widgets.properties.TextPropertyEditor
  */
  public static final String PROPERTY_TYPE_TEXT = "text";

  /**
   * Choices of text alignment (left, center, right) offered by {@link
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAlignmentChoicePropertyEditor}.
   */
  public static final String PROPERTY_TYPE_TEXTALIGNMENT = "textalignment";

  /**
   * Choices of typefaces offered by {@link
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidFontTypefaceChoicePropertyEditor}.
   */
  public static final String PROPERTY_TYPE_TYPEFACE = "typeface";

  /**
   * Choices of visibility for view components offered by {@link
   * com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidVisibilityChoicePropertyEditor}.
   */
  public static final String PROPERTY_TYPE_VISIBILITY = "visibility";
}
