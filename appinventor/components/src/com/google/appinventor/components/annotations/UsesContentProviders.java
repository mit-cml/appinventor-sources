// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.annotations.androidmanifest.ProviderElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate any content providers used by
 * a component so that corresponding <provider> elements can be
 * created in AndroidManifest.xml.
 *
 * @author https://github.com/ShreyashSaitwal (Shreyash Saitwal)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface UsesContentProviders {

  /**
   * An array containing each {@link ProviderElement}
   * that is required by the component.
   *
   * @return  the array containing the relevant providers
   */
  ProviderElement[] providers();
}
