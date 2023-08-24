// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import java.util.Map;

public interface ComponentCoverageInterface {

  /**
   * Returns the name of the component
   *
   * @param componentType type of component to query
   * @return name of the component
   */
  String getComponentName(String componentType);

  /**
   * Returns the count of properties, methods, and events of a component in Android
   *
   * @param componentName
   * @return
   */
  Map<String, Integer> getAndroidCount(String componentName);

  /**
   * Return the count of properties, methods, and events of a component in iOS
   *
   * @param componentName
   * @return
   */
  Map<String, Integer> getIosCount(String componentName);
}
