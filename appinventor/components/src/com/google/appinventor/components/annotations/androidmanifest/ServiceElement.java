// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe a <service> element required by a component so that
 * it can be added to AndroidManifest.xml. <service> elements indicate that
 * a component is a service. <service> element attributes that are not
 * set explicitly default to "" or {} and are ignored when the element is created
 * in the manifest.
 *
 * Note: Most of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 *
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/service-element">
 *              https://developer.android.com/guide/topics/manifest/service-element
 *            </a>}.
 *
 * @author https://github.com/ShreyashSaitwal (Shreyash Saitwal)
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceElement {
/**
   * An array containing any intent filters used by this <service> element.
   *
   * @return  an array containing the <intent-filter> subelements for this
   *          <service> element
   */
  IntentFilterElement[] intentFilters() default {};

  /**
   * An array containing any meta data used by this <service> element.
   *
   * @return  an array containing the <meta-data> subelements for this
   *          <service> element
   */
  MetaDataElement[] metaDataElements() default {};

  /**
   * The name of the class that implements the service. This should
   * be a fully qualified class name (such as, "com.example.project.RoomService").
   * However, as a shorthand, if the first character of the name is
   * a period (for example, ".RoomService"), it is appended to the
   * package name of the application.
   *
   * @return  the Service class name
   */
  String name();

  /**
   * If set to true, this service will run under a special process that
   * is isolated from the rest of the system and has no permissions
   * of its own. The only communication with it is through the Service
   * API (binding and starting).
   *
   * @return  the service isolatedProcess attribute
   */
  String isolatedProcess() default "";

  /**
   * Specify that the service is a foreground service that satisfies a
   * particular use case. For example, a foreground service type of
   * "location" indicates that an app is getting the device's current
   * location, usually to continue a user-initiated action related to
   * device location.
   * You can assign multiple foreground service types to a particular service.
   *
   * Note: Starting in Android 14, this property is required on all services.
   *
   * @return  the service foregroundServiceType attribute
   */
  String foregroundServiceType() default "";

  /**
   * A string that describes the service to users. The label should be
   * set as a reference to a string resource, so that it can be localized
   * like other strings in the user interface.
   *
   * @return  the service desciption attribute
   */
  String description() default "";

  /**
   * Whether or not the service is direct-boot aware; that is, whether or
   * not it can run before the user unlocks the device.
   *
   * @return  the service directBootAware attribute
   */
  String directBootAware() default "";

  /**
   * Whether or not the service can be instantiated by the system — "true"
   * if it can be, and "false" if not. The default value is "true".
   * The <application> element has its own enabled attribute that applies
   * to all application components, including services. The <application>
   * and <service> attributes must both be "true" (as they both are by default)
   * for the service to be enabled. If either is "false", the service is
   * disabled; it cannot be instantiated.
   *
   * @return  the service enabled attribute
   */
  String enabled() default "";

  /**
   * Whether or not components of other applications can invoke the service
   * or interact with it — "true" if they can, and "false" if not. When the
   * value is "false", only components of the same application or applications
   * with the same user ID can start the service or bind to it.
   *
   * The default value depends on whether the service contains intent filters.
   * The absence of any filters means that it can be invoked only by specifying
   * its exact class name. This implies that the service is intended only for
   * application-internal use (since others would not know the class name). So
   * in this case, the default value is "false". On the other hand, the presence
   * of at least one filter implies that the service is intended for external use,
   * so the default value is "true".

   * This attribute is not the only way to limit the exposure of a service to other
   * applications. You can also use a permission to limit the external entities that
   * can interact with the service.
   *
   * @return  the service exported attribute
   */
  String exported() default "true";

  /**
   * An icon representing the service. This attribute must be set as
   * a reference to a drawable resource containing the image definition. If it is
   * not set, the icon specified for the application as a whole is used instead.
   *
   * The  service's icon — whether set here or by the <application>
   * element — is also the default icon for all the service's intent filters
   * (see the {@link IntentFilterElement#icon()} attribute).
   *
   * @return  the service icon attribute
   */
  String icon() default "";

  /**
   * A name for the service that can be displayed to users. If this attribute
   * is not set, the label set for the application as a whole is used instead.
   *
   * The service's label — whether set here or by the <application> element —
   * is also the default label for all the service's intent filters.
   *
   * @return  the service label attribute
   */
  String label() default "";

  /**
   * The name of a permission that an entity must have in order to launch the
   * service or bind to it. If a caller of startService(), bindService(), or
   * stopService(), has not been granted this permission, the method will not
   * work and the Intent object will not be delivered to the service.
   *
   * If this attribute is not set, the permission set by the <application>
   * element's permission attribute applies to the service. If neither attribute
   * is set, the service is not protected by a permission.
   *
   * @return  the service permission attribute
   */
  String permission() default "";

  /**
   * The name of the process in which the service should run. Normally,
   * all components of an application run in the default process created for the
   * application. For our purposes, those components are  services and
   * activities. It has the same name as the application package. Each component
   * can override the default with its own process attribute, allowing you to
   * spread your application across multiple processes.
   *
   * If the name assigned to this attribute begins with a colon (':'), a new
   * process, private to the application, is created when it's needed and the
   *  service runs in that process. If the process name begins with
   * a lowercase character, the service will run in a global process of that
   * name, provided that it has permission to do so. This allows components
   * ( services and activities) in different applications to share
   * a process, reducing resource usage.
   *
   * @return  the service process attribute
   */
  String process() default "";
}
