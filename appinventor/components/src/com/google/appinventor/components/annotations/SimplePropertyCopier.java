// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark Simple property copiers.  Usually we can copy properties
 * using their setter and getter but sometimes we those methods don't quite
 * do what we need and we have a separate copier method marked with this
 * annotation and with the name "Copy<property name>".
 *
 * @author markf@google.com (Mark Friedman)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SimplePropertyCopier {
}