// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.appinventor.client.Ode;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsType;

public class ShortcutRegistry {

  public interface KeyboardShortcut {
    ShortcutCallback<?, ?> getCallback();
    String getName();
    JsArrayMixed getKeyCodes();
    boolean allowCollision();
    String getDisplayText();
  }

  public static class NativeKeyboardShortcut extends JavaScriptObject implements KeyboardShortcut {

    protected NativeKeyboardShortcut() {
    }

    @Override
    public native final ShortcutCallback<?, ?> getCallback()/*-{
      return this.callback;
    }-*/;

    @Override
    public native final String getName()/*-{
      return this.name;
    }-*/;

    @Override
    public native final JsArrayMixed getKeyCodes()/*-{
      return this.keyCodes;
    }-*/;

    @Override
    public native final boolean allowCollision()/*-{
      return this.allowCollision;
    }-*/;

    @Override
    public native final String getDisplayText()/*-{
      if (typeof this.displayText === 'function') {
        return this.displayText();
      } else {
        return this.displayText;
      }
    }-*/;
  }

  @JsFunction
  public interface ShortcutCallback<T, S> {
    boolean accept(T target, NativeEvent e, KeyboardShortcut shortcut, S scope);
  }

  public static class KeyCode extends JavaScriptObject {
    protected KeyCode() {}
  }

  private static final ShortcutRegistry instance = new ShortcutRegistry();
  private final Map<Class<?>, Map<String, Consumer<Widget>>> registry =  new HashMap<Class<?>, Map<String, Consumer<Widget>>>();

  public static ShortcutRegistry getInstance() {
    return instance;
  }

  public void registerViewShortcut(Class<?> viewClass, String name, String displayName, int keyCode, int[] modifiers, Consumer<Widget> callback) {
    Map<String, Consumer<Widget>> viewShortcuts = registry.get(viewClass);
    if (viewShortcuts == null) {
      viewShortcuts = new HashMap<String, Consumer<Widget>>();
      registry.put(viewClass, viewShortcuts);
    }
    viewShortcuts.put(createSerializedKey(keyCode, modifiers), callback);
  }

  public void onKeyDown(Widget view, KeyDownEvent event) {
    Class<?> viewClass = view.getClass();
    Map<String, Consumer<Widget>> viewShortcuts = null;
    while (viewShortcuts == null && viewClass != Widget.class) {
      viewShortcuts = registry.get(viewClass);
      viewClass = viewClass.getSuperclass();
    }
    if (viewShortcuts != null) {
      String serializedKey = serializeKeyEvent(event.getNativeEvent());
      Ode.CLog("ShortcutRegistry onKeyDown: serializedKey = " + serializedKey);
      Consumer<Widget> callback = viewShortcuts.get(serializedKey);
      if (callback != null) {
        callback.accept(view);
        event.preventDefault();
      }
    }
  }

  public static native int getCtrlCmd()/*-{
    return $wnd.Blockly.utils.KeyCodes.CTRL_CMD;
  }-*/;

  private static native JsArrayString getBlocklyShortcutNames()/*-{
    return $wnd.Blockly.ShortcutRegistry.registry.shortcuts.keys().toArray();
  }-*/;

  public static native String createSerializedKey(int keyCode, int[] modifiers)/*-{
    return $wnd.Blockly.ShortcutRegistry.registry.createSerializedKey(keyCode, modifiers);
  }-*/;

  public List<KeyboardShortcut> getKeyboardShortcuts() {
    List<KeyboardShortcut> result = new ArrayList<KeyboardShortcut>();
    getBlocklyShortcuts(result);
    return result;
  }

  private static native void getBlocklyShortcuts(List<KeyboardShortcut> shortcuts)/*-{
    $wnd.Blockly.ShortcutRegistry.registry.shortcuts.values().toArray().forEach(function(shortcut) {
      shortcuts.@java.util.List::add(Ljava/lang/Object;)(shortcut);
    });
  }-*/;

  private static native String serializeKeyEvent(NativeEvent e)/*-{
    // Shamelessly stolen from the Blockly sources.
    var serializedKey = '';
    ["Shift", "Control", "Alt", "Meta"].forEach(function(modifier) {
      if (e.getModifierState(modifier)) {
        if (serializedKey !== '') {
          serializedKey += '+';
        }
        serializedKey += modifier;
      }
    });
    if (serializedKey !== '' && e.keyCode) {
      serializedKey += '+' + e.keyCode;
    } else if (e.keyCode) {
      serializedKey = String(e.keyCode);
    }
    return serializedKey;
  }-*/;
}
