// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.annotations.androidmanifest.ApplicationAttribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a component requires specific attributes to be
 * added to the {@code <application>} tag in the generated AndroidManifest.xml.
 * <p>
 * This allows extension and component developers to modify application-level
 * settings such as {@code android:largeHeap}, {@code android:appCategory}, or
 * {@code android:usesCleartextTraffic} directly from the component's Java
 * source.
 * </p>
 * Note: If multiple components define the same attribute, we'll take only the
 * first one.
 *
 * @author https://github.com/jewelshkjony (Jewel Shikder Jony)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesApplicationAttributes {

  /**
   * An array of {@link ApplicationAttribute} annotations, each representing
   * a single attribute-value pair to be injected into the manifest's
   * {@code <application>} element.
   *
   * @return the array of application attribute data
   */
  ApplicationAttribute[] attributes();
}
