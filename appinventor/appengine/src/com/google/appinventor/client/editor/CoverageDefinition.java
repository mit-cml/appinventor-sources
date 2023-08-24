// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor;

import java.util.List;
import java.util.Map;

public class CoverageDefinition {

  private final String name;

  private final Map<String, Integer> android;

  private final List<String> events;

  private final Map<String, Integer> ios;

  private final List<String> methods;

  private final List<String> propertyGetters;

  private final List<String> propertySetters;

  private final String covergeJson;

  public CoverageDefinition(String name, Map<String, Integer> android, List<String> events, Map<String, Integer> ios, List<String> methods, List<String> propertyGetters, List<String> propertySetters, String covergeJson) {
    this.name = name;
    this.android = android;
    this.events = events;
    this.ios = ios;
    this.methods = methods;
    this.propertyGetters = propertyGetters;
    this.propertySetters = propertySetters;
    this.covergeJson = covergeJson;
  }

  public String getName() {
    return name;
  }

  public Map<String, Integer> getAndroid() {
    return android;
  }

  public List<String> getEvents() {
    return events;
  }

  public Map<String, Integer> getIos() {
    return ios;
  }

  public List<String> getMethods() {
    return methods;
  }

  public List<String> getPropertyGetters() {
    return propertyGetters;
  }

  public List<String> getPropertySetters() {
    return propertySetters;
  }

  public String getCovergeJson() {
    return covergeJson;
  }
}
