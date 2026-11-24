// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Checks that {@link ShortcutRegistry} applies key overrides to live dispatch,
 * regardless of whether the override is known before or after a shortcut is
 * registered, and that same-scope key collisions are detected.
 *
 * <p>Registration here goes through {@link ShortcutRegistry#registerWithDefaultKey}
 * rather than the public {@code registerViewShortcut}, since the latter computes
 * its default key via a JSNI call into Blockly that has no JVM body outside a
 * browser/GWTTestCase environment. The override/dispatch/conflict logic under
 * test here is plain Java with no such dependency.
 *
 * <p>Tests scope shortcuts to concrete {@code Widget} subclasses ({@link Label},
 * {@link Composite}) rather than {@code Widget.class} itself, matching how
 * production code always scopes registrations to a specific view class —
 * {@code Widget.class} is deliberately excluded from the dispatch walk's own
 * loop condition and would never resolve.
 */
public class ShortcutRegistryTest extends TestCase {

  private ShortcutRegistry registry;

  @Override
  protected void setUp() {
    registry = new ShortcutRegistry();
  }

  private static <T> Function<T, Boolean> noop() {
    return t -> true;
  }

  private static Map<String, String> overridesOf(String actionId, String key) {
    Map<String, String> overrides = new HashMap<String, String>();
    overrides.put(actionId, key);
    return overrides;
  }

  public void testOverrideKnownBeforeRegistrationIsUsedImmediately() {
    registry.applyKeyOverrides(overridesOf("focus_tree", "Alt+84"));
    registry.registerWithDefaultKey(Label.class, "focus_tree", "Alt+70", ShortcutRegistryTest.<Label>noop());

    assertNotNull(registry.resolveDispatch(Label.class, "Alt+84"));
    assertNull(registry.resolveDispatch(Label.class, "Alt+70"));
  }

  public void testOverrideAppliedAfterRegistrationMovesDispatchEntry() {
    registry.registerWithDefaultKey(Label.class, "focus_tree", "Alt+70", ShortcutRegistryTest.<Label>noop());
    assertNotNull(registry.resolveDispatch(Label.class, "Alt+70"));

    registry.applyKeyOverrides(overridesOf("focus_tree", "Alt+84"));

    assertNotNull(registry.resolveDispatch(Label.class, "Alt+84"));
    assertNull(registry.resolveDispatch(Label.class, "Alt+70"));
  }

  public void testDroppingOverrideRevertsToDefaultKey() {
    registry.registerWithDefaultKey(Label.class, "focus_tree", "Alt+70", ShortcutRegistryTest.<Label>noop());
    registry.applyKeyOverrides(overridesOf("focus_tree", "Alt+84"));
    assertNotNull(registry.resolveDispatch(Label.class, "Alt+84"));

    // A diff-only overrides map with no entry for focus_tree means "back to default".
    registry.applyKeyOverrides(new HashMap<String, String>());

    assertNull(registry.resolveDispatch(Label.class, "Alt+84"));
    assertNotNull(registry.resolveDispatch(Label.class, "Alt+70"));
  }

  public void testFindConflictDetectsSameScopeCollision() {
    registry.registerWithDefaultKey(Label.class, "focus_tree", "Alt+84", ShortcutRegistryTest.<Label>noop());
    registry.registerWithDefaultKey(Label.class, "focus_viewer", "Alt+86", ShortcutRegistryTest.<Label>noop());

    assertEquals("focus_tree", registry.findConflict("focus_viewer", "Alt+84"));
    assertNull(registry.findConflict("focus_viewer", "Alt+86")); // its own current key
    assertNull(registry.findConflict("focus_viewer", "Alt+99")); // unused key
  }

  public void testFindConflictIgnoresDifferentScope() {
    registry.registerWithDefaultKey(Label.class, "focus_tree", "Alt+84", ShortcutRegistryTest.<Label>noop());
    registry.registerWithDefaultKey(Composite.class, "select_all_projects", "Alt+84", ShortcutRegistryTest.<Composite>noop());

    assertNull(registry.findConflict("select_all_projects", "Alt+84"));
  }

  public void testFindConflictReturnsNullForUnregisteredAction() {
    registry.registerWithDefaultKey(Label.class, "focus_tree", "Alt+84", ShortcutRegistryTest.<Label>noop());

    // "refresh_companion" isn't registered (not yet migrated onto the registry),
    // so remapping it can't conflict with anything live.
    assertNull(registry.findConflict("refresh_companion", "Alt+84"));
  }
}
