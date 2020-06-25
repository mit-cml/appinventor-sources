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
 * Annotation to describe a <provider> element required by a component so that
 * it can be added to AndroidManifest.xml. <provider> elements indicate that
 * a component is a content provider. <provider> element attributes that are not
 * set explicitly default to "" or {} and are ignored when the element is created
 * in the manifest.
 * 
 * Note: Most of this documentation is adapted from the Android framework specification
 *       linked below. That documentation is licensed under the
 *       {@link <a href="https://creativecommons.org/licenses/by/2.5/">
 *         Creative Commons Attribution license v2.5
 *       </a>}.
 * 
 * See {@link <a href="https://developer.android.com/guide/topics/manifest/provider-element">
 *              https://developer.android.com/guide/topics/manifest/provider-element
 *            </a>}.
 * 
 * @author https://github.com/ShreyashSaitwal (Shreyash Saitwal)
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProviderElement {

   /**
   * An array containing any meta data used by this <provider> element.
   *
   * @return  an array containing the <meta-data> subelements for this
   *          <provider> element
   */
   MetaDataElement[] metaDataElements() default {};

   /**
   * An array containing any path permissions used by this <provider> element.
   *
   * @return  an array containing the <path-permission> subelements for this
   *          <provider> element
   */
   PathPermissionElement[] pathPermissionElement() default {};

   /**
   * An array containing any path permissions used by this <provider> element.
   *
   * @return  an array containing the <path-permission> subelements for this
   *          <provider> element
   */
   GrantUriPermissionElement[] grantUriPermissionElement() default {};

   /**
    * A list of one or more URI authorities that identify data offered by the content provider. 
    * Multiple authorities are listed by separating their names with a semicolon. To avoid conflicts, 
    * authority names should use a Java-style naming convention (such as com.example.provider.cartoonprovider). 
    * Typically, it's the name of the ContentProvider subclass that implements the provider
    *
    * There is no default. At least one authority must be specified.
    *
    * @return  the provider authorities attribute
    */
   String authorities();

   /**
    * Whether or not the content provider can be instantiated by the system — "true" if it can be, 
    * and "false" if not. The default value is "true".
    * The <application> element has its own enabled attribute that applies to all application components, 
    * including content providers. The <application> and <provider> attributes must both be "true" (as 
    * they both are by default) for the content provider to be enabled. If either is "false", the provider 
    * is disabled; it cannot be instantiated.
    *
    * @return  the provider enabled attribute
    */
   String enabled() default "";

   /**
    * Whether or not the content provider is direct-boot aware; that is, whether or not it can run 
    * before the user unlocks the device.
    *
    * The default value is "false".
    * 
    * @return  the provider directBootAware attribute
    */
   String directBootAware() default "false";

   /**
    * Whether the content provider is available for other applications to use:
    *    - true: The provider is available to other applications. Any application can use the provider's 
    *            content URI to access it, subject to the permissions specified for the provider.
    *    - false: The provider is not available to other applications. Set android:exported="false" to 
    *             limit access to the provider to your applications. Only applications that have the same 
    *             user ID (UID) as the provider, or applications that have been temporarily granted access 
    *             to the provider through the android:grantUriPermissions element, have access to it.
    *
    * Because this attribute was introduced in API level 17, all devices running API level 16 and lower 
    * behave as though this attribute is set "true". If you set android:targetSdkVersion to 17 or higher, 
    * then the default value is "false" for devices running API level 17 and higher.

    * You can set android:exported="false" and still limit access to your provider by setting permissions 
    * with the {@link #permission()} attribute.
    * 
    * @return  the provider exported attribute
    */
   String exported() default "";

   /**
    * Whether or not those who ordinarily would not have permission to access the content 
    * provider's data can be granted permission to do so, temporarily overcoming the restriction 
    * imposed by the readPermission, writePermission, permission, and exported attributes — "true" 
    * if permission can be granted, and "false" if not. If "true", permission can be granted to 
    * any of the content provider's data. If "false", permission can be granted only to the data 
    * subsets listed in <grant-uri-permission> subelements, if any. The default value is "false".
    *
    * Granting permission is a way of giving an application component one-time access to data 
    * protected by a permission. For example, when an e-mail message contains an attachment, the 
    * mail application may call upon the appropriate viewer to open it, even though the viewer 
    * doesn't have general permission to look at all the content provider's data.

    * In such cases, permission is granted by FLAG_GRANT_READ_URI_PERMISSION and FLAG_GRANT_WRITE_URI_PERMISSION 
    * flags in the Intent object that activates the component. For example, the mail application 
    * might put FLAG_GRANT_READ_URI_PERMISSION in the Intent passed to Context.startActivity(). 
    * The permission is specific to the URI in the Intent.

    * If you enable this feature, either by setting this attribute to "true" or by defining 
    * <grant-uri-permission> subelements, you must call Context.revokeUriPermission() when a 
    * covered URI is deleted from the provider.

    * See also the {@link #GrantUriPermissionElement}.
    *
    * @return  the provider grantUriPermission attribute
    */
   String grantUriPermissions() default "";

   /**
    * An icon representing the content provider. This attribute must be set as a reference to a 
    * drawable resource containing the image definition. If it is not set, the icon specified 
    * for the application as a whole is used instead. 
    *
    * @return  the provider icon attribute
    */
   String icon() default "";

   /**
    * The order in which the content provider should be instantiated, relative to other content
    * providers hosted by the same process. When there are dependencies among content providers,
    * setting this attribute for each of them ensures that they are created in the order required
    * by those dependencies. The value is a simple integer, with higher numbers being initialized
    * first.
    * 
    * @return  the provider initOrder attribute
    */
   String initOrder() default "";  

   /**
    * A user-readable label for the content provided. If this attribute is not set, the label 
    * set for the application as a whole is used instead (see the <application> element's label
    * attribute).
    *
    * The label should be set as a reference to a string resource, so that it can be localized
    * like other strings in the user interface. However, as a convenience while you're developing
    * the application, it can also be set as a raw string.
    * 
    * @return  the provider label attribute
    */
   String label() default "";

   /**
    * If the app runs in multiple processes, this attribute determines whether multiple instances 
    * of the content provider are created. If true, each of the app's processes has its own content
    * provider object. If false, the app's processes share only one content provider object. The
    * default value is false.
    * Setting this flag to true may improve performance by reducing the overhead of interprocess
    * communication, but it also increases the memory footprint of each process.
    * 
    * @return  the provider multiprocess attribute
    */
   String multiprocess() default "";

   /**
    * The name of the class that implements the content provider, a subclass of ContentProvider. 
    * This should be a fully qualified class name (such as, "com.example.project.TransportationProvider").
    * However, as a shorthand, if the first character of the name is a period, it is appended to
    * the package name specified in the <manifest> element.
    * 
    * There is no default. The name must be specified.
    * 
    * @return  the provider class name
    */
   String name();

   /**
    * The name of a permission that clients must have to read or write the content provider's data.
    * This attribute is a convenient way of setting a single permission for both reading and writing.
    * However, the {@link #readPermission()}, {@link #writePermission()}, and {@link #grantUriPermissions()}
    * attributes take precedence over this one. If the {@link #readPermission()} attribute is also set,
    * it controls access for querying the content provider. And if the writePermission attribute is set,
    * it controls access for modifying the provider's data.
    * 
    * @return  the provider permission attribute
    */
   String permission() default "";

   /**
    * The name of the process in which the content provider should run. Normally, all components of an 
    * application run in the default process created for the application. It has the same name as the
    * application package. The <application> element's process attribute can set a different default
    * for all components. But each component can override the default with its own process attribute,
    * allowing you to spread your application across multiple processes.
    * 
    * If the name assigned to this attribute begins with a colon (':'), a new process, private to the
    * application, is created when it's needed and the activity runs in that process. If the process
    * name begins with a lowercase character, the activity will run in a global process of that name,
    * provided that it has permission to do so. This allows components in different applications to
    * share a process, reducing resource usage.
    * 
    * @return  the provider process attribute
    */
   String process() default "";

   /**
    * A permission that clients must have to query the content provider.
    * 
    * If the provider sets android:grantUriPermissions to true, or if a given client satisfies the
    * conditions of a <grant-uri-permission> subelement, the client can gain temporary read access
    * to the content provider's data.
    * 
    * See also the {@link #permission()} and {@link #writePermission()} attributes.
    * 
    * @return  the provider readPermission attribute
    */
   String readPermission() default "";

   /**
    * Whether or not the data under the content provider's control is to be synchronized with data on
    * a server — "true" if it is to be synchronized, and "false" if not.
    * 
    * @return  the provider syncable attribute
    */
   String syncable() default "";

   /**
    * A permission that clients must have to make changes to the data controlled by the content provider.

    * If the provider sets android:grantUriPermissions to true, or if a given client satisfies the conditions
    * of a <grant-uri-permission> subelement, the client can gain temporary write access to modify the content
    * provider's data.

    * See also the {@link #permission()} and {@link #readPermission()} attributes.
    * 
    * @return
    */
   String writePermission() default "";
}
