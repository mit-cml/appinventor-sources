// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations.androidmanifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe an <uses-feature> element required by a component
 * so that it can be added to AndroidManifest.xml.
 *
 * @author https://github.com/jewelshkjony (Jewel Shikder Jony)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FeatureElement {

    /**
     * Specifies a single hardware or software feature used by the application as a descriptor string.
     * Valid attribute values are listed in the
     * <a href="https://developer.android.com/guide/topics/manifest/uses-feature-element#hw-features">Hardware features</a>
     * and <a href="https://developer.android.com/guide/topics/manifest/uses-feature-element#sw-features">Software features</a> sections.
     * These attribute values are case-sensitive.
     *
     * @return the name of the element
     */
    String name();

    /**
     * Boolean value that indicates whether the application requires the featureContents of the xml file.
     * Declaring true for a feature indicates that the application can't function,
     * or isn't designed to function, when the specified feature isn't present on the device.
     * Declaring false for a feature indicates that the application uses the feature if present on the device,
     * but that it is designed to function without the specified feature if necessary.
     *
     * @return Returns true if required otherwise false
     */
    boolean required() default true;

    /**
     * The OpenGL ES version required by the application. The higher 16 bits represent the major
     * number and the lower 16 bits represent the minor number. For example, to specify OpenGL
     * ES version 2.0, you set the value as "0x00020000", or to specify OpenGL ES 3.2,
     * you set the value as "0x00030002".
     *
     * @return the glEs version
     */
    int glEsVersion() default -1;
}