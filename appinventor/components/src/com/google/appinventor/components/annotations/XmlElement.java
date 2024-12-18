// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation describing the elements necessary to create an xml file in the application
 * resources, such as the directory, file name, and file contents.
 *
 * @author https://github.com/patryk84a (Patryk Fraczek)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlElement {

  /**
   * The name of the resource directory for the xml file being created. One of those permitted:
   * layout|values|drawable|mipmap|xml|color|menu|animator|anim
   *
   * @return the name of the dir item
   */
  String dir();

  /**
   * The name of the xml file being created, along with the file extension.
   * e.g. automotive_app_desc.xml
   *
   * @return the name of the data item
   */
  String name();

  /**
   * Contents of the xml file.
   *
   * @return a reference to the specified resource
   */
  String content();
}
