// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark properties to be visible in the ODE visual designer.
 *
 * <p>Only the setter method of the property must be marked with this
 * annotation.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DesignerProperty {

  /**
   * Property editor types.
   */
  static final String PROPERTY_TYPE_ASSET = "asset";
  static final String PROPERTY_TYPE_BLUETOOTHCLIENT = "BluetoothClient";
  static final String PROPERTY_TYPE_BOOLEAN = "boolean";
  static final String PROPERTY_TYPE_COLOR = "color";
  static final String PROPERTY_TYPE_COMPONENT = "component";
  static final String PROPERTY_TYPE_FLOAT = "float";
  static final String PROPERTY_TYPE_INTEGER = "integer";
  static final String PROPERTY_TYPE_LEGO_NXT_SENSOR_PORT = "lego_nxt_sensor_port";
  static final String PROPERTY_TYPE_LEGO_NXT_GENERATED_COLOR = "lego_nxt_generated_color";
  static final String PROPERTY_TYPE_NON_NEGATIVE_FLOAT = "non_negative_float";
  static final String PROPERTY_TYPE_NON_NEGATIVE_INTEGER = "non_negative_integer";
  static final String PROPERTY_TYPE_SCREEN_ORIENTATION = "screen_orientation";
  static final String PROPERTY_TYPE_STRING = "string";
  static final String PROPERTY_TYPE_TEXT = "text";
  static final String PROPERTY_TYPE_TEXTALIGNMENT = "textalignment";
  static final String PROPERTY_TYPE_TYPEFACE = "typeface";

  /**
   * Determines the property editor used in the designer.
   *
   * @return  property type
   */
  String editorType() default PROPERTY_TYPE_TEXT;

  /**
   * Default value of property.
   *
   * @return  default property value
   */
  String defaultValue() default "";
}
