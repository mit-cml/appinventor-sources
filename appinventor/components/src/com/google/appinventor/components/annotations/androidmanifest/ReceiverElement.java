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
 * Annotation to describe a <receiver> element required by a component so that
 * it can be added to AndroidManifest.xml. <receiver> elements indicate that
 * a component is a broadcast receiver. <receiver> element attributes that are not
 * set explicitly default to "" or {} and are ignored when the element is created
 * in the manifest.
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
public @interface ReceiverElement {

  /**
   * An array containing any intent filters used by this <receiver> element.
   *
   * @return  an array containing the <intent-filter> subelements for this
   *          <receiver> element
   */
  IntentFilterElement[] intentFilters() default {};

  /**
   * An array containing any meta data used by this <receiver> element.
   *
   * @return  an array containing the <meta-data> subelements for this
   *          <receiver> element
   */
  MetaDataElement[] metaDataElements() default {};

  /**
   * The name of the class that implements the broadcast receiver, a subclass
   * of {@link android.content.BroadcastReceiver}. This should be a fully
   * qualified class name (such as, "com.example.project.ReportReceiver").
   * The name attribute is required in any @ReceiverElement annotation and
   * hence has no default value.
   *
   * @return  the broadcast receiver class name
   */
  String name();

  /**
   * Whether or not the broadcast receiver can be instantiated by the system.
   * "true" if it can be, and "false" if not. The default value is "true".
   *
   * The <application> element has its own enabled attribute that applies
   * to all application components, including broadcast receivers. The
   * <application> and <receiver> attributes must both be "true" for the
   * broadcast receiver to be enabled. If either is "false", it is disabled;
   * it cannot be instantiated.
   *
   * @return  the receiver enabled attribute
   */
  String enabled() default "";

  /**
   * Whether or not the broadcast receiver can receive messages from sources
   * outside its application — "true" if it can, and "false" if not. If "false",
   * the only messages the broadcast receiver can receive are those sent by
   * components of the same application or applications with the same user ID.
   * For our purposes, those components are other broadcast receivers and
   * activities.
   *
   * The default value depends on whether the broadcast receiver contains intent
   * filters. The absence of any filters means that it can be invoked only by
   * Intent objects that specify its exact class name. This implies that the
   * receiver is intended only for application-internal use (since others would
   * not normally know the class name). So in this case, the default value is
   * "false". On the other hand, the presence of at least one filter implies
   * that the broadcast receiver is intended to receive intents broadcast by
   * the system or other applications, so the default value is "true".
   *
   * This attribute is not the only way to limit a broadcast receiver's external
   * exposure. You can also use a permission to limit the external entities that
   * can send it messages (see the {@link #permission()} attribute).
   *
   * @return  the receiver exported attribute
   */
  String exported() default "";

  /**
   * An icon representing the broadcast receiver. This attribute must be set as
   * a reference to a drawable resource containing the image definition. If it is
   * not set, the icon specified for the application as a whole is used instead.
   *
   * The broadcast receiver's icon — whether set here or by the <application>
   * element — is also the default icon for all the receiver's intent filters
   * (see the {@link IntentFilterElement#icon()} attribute).
   *
   * @return  the receiver icon attribute
   */
  String icon() default "";

  /**
   * A user-readable label for the broadcast receiver. If this attribute is not
   * set, the label set for the application as a whole is used instead. The
   * broadcast receiver's label — whether set here or by the <application>
   * element — is also the default label for all the receiver's intent filters
   * (see the {@link IntentFilterElement#label()} attribute).
   *
   * The label should be set as a reference to a string resource, so that it can
   * be localized like other strings in the user interface. However, as a
   * convenience while you're developing the application, it can also be set as
   * a raw string.
   *
   * @return  the receiver label attribute
   */
  String label() default "";

  /**
   * The name of a permission that broadcasters must have to send a message to
   * the broadcast receiver. If this attribute is not set, the permission set
   * by the <application> element's permission attribute applies to the broadcast
   * receiver. If neither attribute is set, the receiver is not protected by a
   * permission.
   *
   * @return  the receiver permission attribute
   */
  String permission() default "";

  /**
   * The name of the process in which the broadcast receiver should run. Normally,
   * all components of an application run in the default process created for the
   * application. For our purposes, those components are broadcast receivers and
   * activities. It has the same name as the application package. Each component
   * can override the default with its own process attribute, allowing you to
   * spread your application across multiple processes.
   *
   * If the name assigned to this attribute begins with a colon (':'), a new
   * process, private to the application, is created when it's needed and the
   * broadcast receiver runs in that process. If the process name begins with
   * a lowercase character, the receiver will run in a global process of that
   * name, provided that it has permission to do so. This allows components
   * (broadcast receivers and activities) in different applications to share
   * a process, reducing resource usage.
   *
   * @return  the receiver process attribute
   */
  String process() default "";
}
