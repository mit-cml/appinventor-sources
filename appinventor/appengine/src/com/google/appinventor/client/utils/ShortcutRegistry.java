// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.appinventor.client.Ode;
import com.google.gwt.core.client.JavaScriptObject;
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
import java.util.function.Function;
import jsinterop.annotations.JsFunction;

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
  private final Map<Class<?>, Map<String, Function<?, Boolean>>> registry =  new HashMap<Class<?>, Map<String, Function<?, Boolean>>>();

  public static ShortcutRegistry getInstance() {
    return instance;
  }

  public <T extends Widget> void registerViewShortcut(Class<T> viewClass, String name, String displayName, int keyCode,
      int[] modifiers, Function<T, Boolean> callback) {
    Map<String, Function<?, Boolean>> viewShortcuts = registry.get(viewClass);
    if (viewShortcuts == null) {
      viewShortcuts = new HashMap<String, Function<?, Boolean>>();
      registry.put(viewClass, viewShortcuts);
    }
    viewShortcuts.put(createSerializedKey(keyCode, modifiers), callback);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void onKeyDown(Widget view, KeyDownEvent event) {
    Class<?> viewClass = view.getClass();
    Map<String, Function<?, Boolean>> viewShortcuts = null;
    while (viewShortcuts == null && viewClass != Widget.class) {
      viewShortcuts = registry.get(viewClass);
      viewClass = viewClass.getSuperclass();
    }
    if (viewShortcuts != null) {
      String serializedKey = serializeKeyEvent(event.getNativeEvent());
      Ode.CLog("ShortcutRegistry onKeyDown: serializedKey = " + serializedKey);
      Function callback = viewShortcuts.get(serializedKey);
      if (callback != null) {
        Boolean result = (Boolean) callback.apply(view);
        if (result != null && result) {
          event.preventDefault();
        }
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

  public static native String getShortcutKeys(String shortcut)/*-{
    // Shamelessly stolen from the Blockly sources
    var shortcutAsParts = shortcut.split('+');
    var modifierOrdering = ['Meta', 'Control', 'Alt', 'Shift'];
    var shortModifierNames = {
      'Control': $wnd.Blockly.Msg['CONTROL_KEY'],
      'Meta': '⌘',
      'Alt': $wnd.Blockly.utils.userAgent.APPLE ? '⌥' : $wnd.Blockly.Msg['ALT_KEY']
    };
    function modifierOrder(modifier) {
      var index = modifierOrdering.indexOf(modifier);
      return index === -1 ? Number.MAX_VALUE : index;
    }
    function getKeyName(keyCode) {
      if (keyCode >= 65 && keyCode <= 90) {
        return String.fromCharCode(keyCode);
      }
      var keyNames = {
        8: $wnd.Blockly.Msg['BACKSPACE_KEY'],
        9: $wnd.Blockly.Msg['TAB_KEY'],
        13: $wnd.Blockly.Msg['ENTER_KEY'],
        16: $wnd.Blockly.Msg['SHIFT_KEY'],
        17: $wnd.Blockly.Msg['CONTROL_KEY'],
        18: $wnd.Blockly.Msg['ALT_KEY'],
        19: $wnd.Blockly.Msg['PAUSE_KEY'],
        20: $wnd.Blockly.Msg['CAPS_LOCK_KEY'],
        27: $wnd.Blockly.Msg['ESCAPE'],
        32: $wnd.Blockly.Msg['SPACE_KEY'],
        33: $wnd.Blockly.Msg['PAGE_UP_KEY'],
        34: $wnd.Blockly.Msg['PAGE_DOWN_KEY'],
        35: $wnd.Blockly.Msg['END_KEY'],
        36: $wnd.Blockly.Msg['HOME_KEY'],
        37: '←',
        38: '↑',
        39: '→',
        40: '↓',
        45: $wnd.Blockly.Msg['INSERT_KEY'],
        46: $wnd.Blockly.Msg['DELETE_KEY'],
        48: '0',
        49: '1',
        50: '2',
        51: '3',
        52: '4',
        53: '5',
        54: '6',
        55: '7',
        56: '8',
        57: '9',
        59: ';',
        61: '=',
        93: $wnd.Blockly.Msg['CONTEXT_MENU_KEY'],
        96: '0',
        97: '1',
        98: '2',
        99: '3',
        100: '4',
        101: '5',
        102: '6',
        103: '7',
        104: '8',
        105: '9',
        106: '×',
        107: '+',
        109: '−',
        110: '.',
        111: '÷',
        112: 'F1',
        113: 'F2',
        114: 'F3',
        115: 'F4',
        116: 'F5',
        117: 'F6',
        118: 'F7',
        119: 'F8',
        120: 'F9',
        121: 'F10',
        122: 'F11',
        123: 'F12',
        186: ';',
        187: '=',
        189: '-',
        188: ',',
        190: '.',
        191: '/',
        192: '`',
        219: '[',
        220: '\\',
        221: ']',
        222: "'",
        224: '⌘'
      };
      var keyName = keyNames[keyCode];
      return keyName ? keyName : String.fromCharCode(keyCode);
    }
    shortcutAsParts.sort(function(a, b) {
      var aValue = modifierOrder(a);
      var bValue = modifierOrder(b);
      return aValue - bValue;
    })
    return shortcutAsParts.map(function(s) {
      return Number.isFinite(+s) ? getKeyName(+s) : s;
    }).map(function(s) {
      return s in shortModifierNames ? shortModifierNames[s] : s;
    }).join(' ');
  }-*/;
}
