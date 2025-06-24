// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.mit.appinventor.webemu;

import com.google.appinventor.components.runtime.Button;
import com.google.appinventor.components.runtime.ChatBot;
import com.google.appinventor.components.runtime.Clock;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.ListPicker;
import java.util.HashMap;
import java.util.Map;
import jsinterop.annotations.JsType;

@JsType(namespace = "appinventor")
public class ComponentFactory {
  @FunctionalInterface
  interface ComponentConstructor<T> {
    T create(ComponentContainer parent);
  }
  private static final Map<String, ComponentConstructor<? extends Component>> LOOKUP = new HashMap<>();

  static {
    LOOKUP.put("Button", Button::new);
    LOOKUP.put("ChatBot", ChatBot::new);
//    LOOKUP.put("Clock", Clock::new);
    LOOKUP.put("Label", Label::new);
    LOOKUP.put("ListPicker", ListPicker::new);
  }
  public static Component create(ComponentContainer parent, String type) {
    if (type.startsWith("com.google.appinventor.components.runtime.")) {
      type = type.substring("com.google.appinventor.components.runtime.".length());
    }
    ComponentConstructor<? extends Component> constructor = LOOKUP.get(type);
    if (constructor == null) {
      throw new IllegalArgumentException("Unsupported component type: " + type);
    }
    return constructor.create(parent);
  }
}
