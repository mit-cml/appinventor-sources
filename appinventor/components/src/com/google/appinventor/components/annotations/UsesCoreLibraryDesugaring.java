// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a component requires Java 8+ API desugaring.
 *
 * When a component is marked with this annotation, the build server will enable
 * 'coreLibraryDesugaring' during the project's build process. This allows the
 * use of modern Java APIs (such as java.time, java.util.stream,function)
 * while maintaining compatibility with older Android versions by
 * backporting those libraries into the final APK.
 *
 * @author https://github.com/jewelshkjony (Jewel Shikder Jony)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesCoreLibraryDesugaring {
}
