// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.mit.appinventor.webemu;

import com.google.appinventor.components.runtime.Button;
import com.google.appinventor.components.runtime.ChatBot;
import com.google.appinventor.components.runtime.CheckBox;
import com.google.appinventor.components.runtime.Clock;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.File;
import com.google.appinventor.components.runtime.Image;
import com.google.appinventor.components.runtime.ImageBot;
import com.google.appinventor.components.runtime.ImagePicker;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.ListPicker;
import com.google.appinventor.components.runtime.ListView;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.PasswordTextBox;
import com.google.appinventor.components.runtime.Player;
import com.google.appinventor.components.runtime.Sound;
import com.google.appinventor.components.runtime.SpeechRecognizer;
import com.google.appinventor.components.runtime.TextToSpeech;
import com.google.appinventor.components.runtime.TextBox;
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
    LOOKUP.put("CheckBox", CheckBox::new);
    LOOKUP.put("Clock", Clock::new);

    LOOKUP.put(key:"File", File::new);

    LOOKUP.put("Image", Image::new);
    LOOKUP.put("ImageBot", ImageBot::new);
    LOOKUP.put("ImagePicker", ImagePicker::new);
    LOOKUP.put("Label", Label::new);
    LOOKUP.put("ListPicker", ListPicker::new);
    LOOKUP.put("ListView", ListView::new);
    LOOKUP.put("Notifier", Notifier::new);
    LOOKUP.put("PasswordTextBox", PasswordTextBox::new);
    LOOKUP.put("Player", Player::new);
    LOOKUP.put("Sound", Sound::new);
    LOOKUP.put("SpeechRecognizer", SpeechRecognizer::new);
    LOOKUP.put("TextToSpeech", TextToSpeech::new);
    LOOKUP.put("TextBox", TextBox::new);
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
