// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import com.google.appinventor.components.annotations.androidmanifest.MetaDataElement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate any additional metadata required by
 * a component so that corresponding <meta-data> elements can be added
 * to AndroidManifest.xml.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UsesActivityMetadata {

    /**
     * An array containing each {@link MetaDataElement}
     * that is required by the component.
     *
     * @return  the array containing the relevant metadata
     */
    MetaDataElement[] metaDataElements();
}
