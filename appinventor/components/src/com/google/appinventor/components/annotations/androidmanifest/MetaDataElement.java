// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe a <meta-data> element. A <meta-data> element consists
 *  of a name-value pair for an item of additional, arbitrary data that can be
 * supplied to the parent component. For our purposes, the parent component is
 * either an <activity> or a <receiver>. A component element can contain any
 * number of <meta-data> subelements. The values from all of them are collected
 * in a single Bundle object and made available to the component as the
 * {@link android.content.pm.PackageItemInfo#metaData} field.
 *
 * Ordinary values are specified through the value attribute. However, to
 * assign a resource ID as the value, use the resource attribute instead.
 *
 * When using a MetaDataElement, the {@link #name} attribute must be specified along
 * with either the {@link #resource()} or the {@link #value()} attribute.
 * <meta-data> element attributes that are not set explicitly default to "" and
 * are ignored when the element is created in the manifest.
 *
 * Note: Some of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 *
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/meta-data-element.html">
 *              https://developer.android.com/guide/topics/manifest/meta-data-element.html
 *            </a>}.
 *
 * @author will2596@gmail.com (William Byrne)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MetaDataElement {

  /**
   * A unique name for the data item. By convention, this name should follow
   * the Java package name format, e.g. "com.example.project.activity.data".
   * The name attribute is required in any @MetaDataElement annotation and
   * hence has no default value.
   *
   * @return  the name of the data item
   */
  String name();

  /**
   * A reference to a resource. The ID of the resource is the value assigned
   * to the data item. The ID can be retrieved from the meta-data Bundle by the
   * {@link android.os.BaseBundle#getInt(String)} method.
   *
   * @return  a reference to the specified resource
   */
  String resource() default "";

  /**
   * The value assigned to the item. The data types that can be assigned as
   * values and the {@link android.os.Bundle} methods that components use to
   * retrieve those values are detailed below:
   *
   * -> A String value, using double backslashes (\\) to escape characters,
   *    such as "\\n" and "\\uxxxxx" for a Unicode character, can be accessed
   *    using {@link android.os.Bundle#getString(String)}.
   *
   * -> An Integer value, such as "100", can be accessed using
   *    {@link android.os.Bundle#getInt(String)}.
   *
   * -> A Boolean value, either "true" or "false", can be accessed using
   *    {@link android.os.Bundle#getBoolean(String)}.
   *
   * -> A Color value, in the form "#rgb", "#argb", "#rrggbb", or "#aarrggbb",
   *    can be accessed using {@link android.os.Bundle#getInt(String)}.
   *
   * -> A Float value, such as "1.23", can be accessed using
   *    {@link android.os.Bundle#getFloat(String)}
   *
   * @return  the value to be assigned to this data item
   */
  String value() default "";
}
