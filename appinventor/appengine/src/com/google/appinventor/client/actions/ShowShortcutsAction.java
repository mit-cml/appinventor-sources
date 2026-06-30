// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.utils.ShortcutRegistry;
import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.appinventor.client.widgets.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.ArrayList;
import java.util.List;

public class ShowShortcutsAction implements Command {

  static class ShortcutDef {
    final String actionId;
    final String displayName;
    final String defaultKey;
    final boolean remappable;
    String currentKey;

    ShortcutDef(String actionId, String displayName, String defaultKey, boolean remappable) {
      this.actionId = actionId;
      this.displayName = displayName;
      this.defaultKey = defaultKey;
      this.remappable = remappable;
      this.currentKey = defaultKey;
    }
  }

  // Shared across dialog opens so remaps persist within a session
  static final List<ShortcutDef> SHORTCUTS = new ArrayList<>();

  static {
    SHORTCUTS.add(new ShortcutDef("focus_search",        MESSAGES.shortcutFocusSearch(),        "191",              true));
    SHORTCUTS.add(new ShortcutDef("focus_tree",          MESSAGES.shortcutFocusTree(),           "84",               true));
    SHORTCUTS.add(new ShortcutDef("focus_viewer",        MESSAGES.shortcutFocusViewer(),         "86",               true));
    SHORTCUTS.add(new ShortcutDef("focus_properties",    MESSAGES.shortcutFocusProperties(),     "80",               true));
    SHORTCUTS.add(new ShortcutDef("focus_media",         MESSAGES.shortcutFocusMedia(),          "77",               true));
    SHORTCUTS.add(new ShortcutDef("toggle_view",         MESSAGES.shortcutToggleView(),          "Control+Alt",      false));
    SHORTCUTS.add(new ShortcutDef("rename_component",    MESSAGES.shortcutRenameComponent(),     "Alt+78",           true));
    SHORTCUTS.add(new ShortcutDef("delete_component",    MESSAGES.shortcutDeleteComponent(),     "Delete/Backspace", false));
    SHORTCUTS.add(new ShortcutDef("reset_connection",    MESSAGES.shortcutResetConnection(),     "Alt+Shift+82",     true));
    SHORTCUTS.add(new ShortcutDef("refresh_companion",   MESSAGES.shortcutRefreshCompanion(),    "Alt+82",           true));
    SHORTCUTS.add(new ShortcutDef("navigate_components", MESSAGES.shortcutNavigateComponents(),  "↑/↓",              false));
    SHORTCUTS.add(new ShortcutDef("listview_add_row", MESSAGES.shortcutListViewAddRow(), "13", false));
    SHORTCUTS.add(new ShortcutDef("listview_navigate_rows", MESSAGES.shortcutListViewNavigateRows(), "↑/↓", false));
    SHORTCUTS.add(new ShortcutDef("listview_navigate_fields", MESSAGES.shortcutListViewNavigateFields(), "Tab / Shift+Tab", false));
    SHORTCUTS.add(new ShortcutDef("show_shortcuts",      MESSAGES.shortcutOpenDialog(),          "Alt+191",          false));
  }

  // Guard: Alt+/ handler is global; only the first instance should register it.
  private static boolean handlerRegistered = false;

  private final DialogBox db;
  private FlexTable table;
  private Label[] keyLabels;   // one per SHORTCUTS row; updated in place, never replaced
  private TextButton editButton;
  private TextButton resetButton;
  private boolean editMode = false;
  private ShortcutDef activeCapture = null;
  private HandlerRegistration captureHandler;

  public ShowShortcutsAction() {
    db = new DialogBox(true, false);
    db.setText(MESSAGES.shortcutsDialogTitle());
    db.setStyleName("ode-DialogBox");
    db.setWidth("520px");
    db.setGlassEnabled(true);
    db.setAnimationEnabled(true);

    // ARIA: outer element is the dialog landmark
    db.getElement().setAttribute("role", "dialog");
    db.getElement().setAttribute("aria-modal", "true");
    db.getElement().setAttribute("aria-label", MESSAGES.shortcutsDialogTitle());
    // GWT's DecoratedPopupPanel renders a 3x3 <table> for decoration; hide it from AT
    db.getElement().getFirstChildElement().setAttribute("role", "presentation");

    if (!handlerRegistered) {
      handlerRegistered = true;
      registerGlobalHandler();
    }
  }

  @Override
  public void execute() {
    cancelCapture();
    activeCapture = null;
    editMode = false;

    table = buildTable();

    editButton = new TextButton(MESSAGES.shortcutsEditButton());
    editButton.addClickHandler(event -> {
      if (editMode) {
        exitEditMode();
      } else {
        enterEditMode();
      }
    });

    resetButton = new TextButton(MESSAGES.shortcutsResetAllButton());
    resetButton.setEnabled(hasOverrides());
    resetButton.addClickHandler(event -> {
      cancelCapture();
      activeCapture = null;
      for (ShortcutDef s : SHORTCUTS) {
        s.currentKey = s.defaultKey;
      }
      resetButton.setEnabled(false);
      updateAllKeyCells();
    });

    TextButton ok = new TextButton(MESSAGES.okButton());
    ok.addClickHandler(event -> {
      cancelCapture();
      exitEditMode();
      db.hide();
    });

    FlowPanel buttons = new FlowPanel();
    buttons.addStyleName("ode-shortcut-buttons");
    buttons.add(resetButton);
    buttons.add(editButton);
    buttons.add(ok);

    FlowPanel panel = new FlowPanel();
    panel.add(table);
    panel.add(buttons);

    db.setWidget(panel);
    db.center();
    db.show();
  }

  private FlexTable buildTable() {
    FlexTable t = new FlexTable();
    t.setBorderWidth(1);
    t.setCellPadding(8);
    t.setCellSpacing(0);
    t.setWidth("100%");
    t.getElement().getStyle().setProperty("tableLayout", "fixed");

    t.setText(0, 0, "");
    t.setText(0, 1, "");
    t.getRowFormatter().addStyleName(0, "ode-table-header");
    // FlexTable always creates <td>; replace with <th scope="col"> for accessibility
    promoteToColumnHeader(t, 0, MESSAGES.shortcutsColumnAction(), "70%");
    promoteToColumnHeader(t, 1, MESSAGES.shortcutsColumnKeys(),   "30%");

    keyLabels = new Label[SHORTCUTS.size()];
    for (int i = 0; i < SHORTCUTS.size(); i++) {
      ShortcutDef s = SHORTCUTS.get(i);
      int row = i + 1;
      t.setText(row, 0, s.displayName);

      Label keyLabel = new Label(toDisplayString(s.currentKey));
      final ShortcutDef shortcut = s;
      keyLabel.addClickHandler(event -> {
        if (editMode && shortcut.remappable && activeCapture == null) {
          startCaptureForRow(shortcut, row);
        }
      });
      t.setWidget(row, 1, keyLabel);
      keyLabels[i] = keyLabel;
    }

    return t;
  }

  private void enterEditMode() {
    editMode = true;
    editButton.setText(MESSAGES.shortcutsEditDoneButton());
    updateAllKeyCells();
  }

  private void exitEditMode() {
    cancelCapture();
    activeCapture = null;
    editMode = false;
    if (editButton != null) {
      editButton.setText(MESSAGES.shortcutsEditButton());
    }
    updateAllKeyCells();
  }

  // Updates every key cell to match the current editMode / activeCapture state.
  private void updateAllKeyCells() {
    int row = 1;
    for (ShortcutDef s : SHORTCUTS) {
      updateKeyCell(s, row);
      row++;
    }
  }

  // Updates the label text and style for one row — never replaces the widget.
  private void updateKeyCell(ShortcutDef s, int row) {
    Label label = keyLabels[row - 1];
    label.removeStyleName("ode-shortcut-capturing");
    label.removeStyleName("ode-shortcut-editable");
    label.removeStyleName("ode-shortcut-locked");
    if (s == activeCapture) {
      label.setText(MESSAGES.shortcutsPressKey());
      label.addStyleName("ode-shortcut-capturing");
    } else {
      label.setText(toDisplayString(s.currentKey));
      if (editMode && s.remappable) {
        label.addStyleName("ode-shortcut-editable");
      } else if (editMode && !s.remappable) {
        label.addStyleName("ode-shortcut-locked");
      }
    }
  }

  private void startCaptureForRow(ShortcutDef s, int row) {
    cancelCapture();
    activeCapture = s;
    updateKeyCell(s, row);

    captureHandler = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        if (event.getTypeInt() != Event.ONKEYDOWN) {
          return;
        }
        NativeEvent nativeEvent = event.getNativeEvent();
        int keyCode = nativeEvent.getKeyCode();

        // Ignore standalone modifier key presses
        if (keyCode == 16 || keyCode == 17 || keyCode == 18 || keyCode == 91 || keyCode == 93) {
          return;
        }

        // Escape is handled by the global handler (registered first, fires first)
        if (keyCode == KeyCodes.KEY_ESCAPE) {
          return;
        }

        event.cancel();
        s.currentKey = buildSerializedKey(nativeEvent);
        activeCapture = null;
        cancelCapture();
        updateKeyCell(s, row);
        resetButton.setEnabled(hasOverrides());
      }
    });
  }

  private void cancelCapture() {
    if (captureHandler != null) {
      captureHandler.removeHandler();
      captureHandler = null;
    }
  }

  // Replaces a FlexTable <td> in row 0 with a <th scope="col"> for accessibility.
  private static void promoteToColumnHeader(FlexTable t, int col, String text, String width) {
    Element th = Document.get().createElement("th");
    th.setAttribute("scope", "col");
    th.getStyle().setProperty("width", width);
    th.setInnerText(text);
    Element td = t.getCellFormatter().getElement(0, col);
    td.getParentElement().replaceChild(th, td);
  }

  private static boolean hasOverrides() {
    for (ShortcutDef s : SHORTCUTS) {
      if (!s.currentKey.equals(s.defaultKey)) {
        return true;
      }
    }
    return false;
  }

  // Converts a Blockly-style serialized key string to a human-readable label.
  static String toDisplayString(String key) {
    if (key == null || key.isEmpty()) {
      return "";
    }
    // Pass through display-only strings that are not valid serialized keys
    if (key.contains("/") || key.contains("↑") || key.contains("↓")
        || key.contains("←") || key.contains("→")) {
      return key;
    }
    return ShortcutRegistry.getShortcutKeys(key);
  }

  // Builds a Blockly-compatible serialized key string from a keydown event.
  private static String buildSerializedKey(NativeEvent event) {
    StringBuilder sb = new StringBuilder();
    if (event.getShiftKey()) {
      sb.append("Shift");
    }
    if (event.getCtrlKey()) {
      if (sb.length() > 0) sb.append("+");
      sb.append("Control");
    }
    if (event.getAltKey()) {
      if (sb.length() > 0) sb.append("+");
      sb.append("Alt");
    }
    if (event.getMetaKey()) {
      if (sb.length() > 0) sb.append("+");
      sb.append("Meta");
    }
    int keyCode = event.getKeyCode();
    if (keyCode != 0) {
      if (sb.length() > 0) sb.append("+");
      sb.append(keyCode);
    }
    return sb.toString();
  }

  private void registerGlobalHandler() {
    Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        NativeEvent nativeEvent = event.getNativeEvent();
        if (event.getTypeInt() == Event.ONKEYDOWN) {
          int keyCode = nativeEvent.getKeyCode();
          if (keyCode == 191 && nativeEvent.getAltKey() && !db.isShowing()) {
            execute();
          } else if (keyCode == KeyCodes.KEY_ESCAPE && db.isShowing()) {
            if (activeCapture != null) {
              // Cancel capture and restore that row's label
              int row = SHORTCUTS.indexOf(activeCapture) + 1;
              ShortcutDef was = activeCapture;
              activeCapture = null;
              cancelCapture();
              updateKeyCell(was, row);
            } else {
              exitEditMode();
              db.hide();
            }
          }
        }
      }
    });
  }
}
