// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

public enum DefaultValueType {
  Text("text"),
  Number("number"),
  Boolean("boolean"),
  Color("color"),
  List("list"),
  Dictionary("dictionary"),
  Unknown("");

  private final String type;

  DefaultValueType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
