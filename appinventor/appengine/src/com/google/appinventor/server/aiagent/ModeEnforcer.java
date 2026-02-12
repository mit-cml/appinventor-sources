// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Enforces AI agent mode, view, and solo-operation restrictions on operations.
 *
 * <p>This is a stateless utility class (all static, no instantiation).
 */
public final class ModeEnforcer {

  private static final Logger LOG = Logger.getLogger(ModeEnforcer.class.getName());

  private ModeEnforcer() {
    // Utility class — no instantiation.
  }

  /** All write operation types (everything except read-only lookups and navigation). */
  static final Set<AIOperation.Type> WRITE_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.ADD_COMPONENT,
          AIOperation.Type.DELETE_COMPONENT,
          AIOperation.Type.SET_PROPERTY,
          AIOperation.Type.RENAME_COMPONENT,
          AIOperation.Type.WRITE_BLOCK,
          AIOperation.Type.DELETE_BLOCK,
          AIOperation.Type.CREATE_SCREEN,
          AIOperation.Type.DELETE_SCREEN,
          AIOperation.Type.SET_PROJECT_PROP)));

  /** Project-level operations only allowed in ProjectEditor mode. */
  static final Set<AIOperation.Type> PROJECT_LEVEL_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.CREATE_SCREEN,
          AIOperation.Type.DELETE_SCREEN,
          AIOperation.Type.SET_PROJECT_PROP)));

  /** Operations that require Designer view. */
  static final Set<AIOperation.Type> DESIGNER_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.ADD_COMPONENT,
          AIOperation.Type.DELETE_COMPONENT,
          AIOperation.Type.SET_PROPERTY,
          AIOperation.Type.RENAME_COMPONENT)));

  /** Operations that require Blocks view. */
  static final Set<AIOperation.Type> BLOCKS_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.WRITE_BLOCK,
          AIOperation.Type.DELETE_BLOCK)));

  /** Operations that must appear alone (no other ops in same batch). */
  static final Set<AIOperation.Type> SOLO_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.TOGGLE_EDITOR,
          AIOperation.Type.SWITCH_SCREEN,
          AIOperation.Type.CREATE_SCREEN)));

  /**
   * Enforce mode, view, and solo-op restrictions on operations.
   * Returns the filtered list; rejected operations are reported in {@code errors}.
   */
  public static List<AIOperation> enforce(List<AIOperation> operations,
      String mode, String currentView, List<String> errors) {
    if (operations.isEmpty()) {
      return operations;
    }

    // Solo-op detection: if any solo op is present alongside other ops,
    // keep only the solo op(s) and reject the rest.
    boolean hasSoloOp = false;
    boolean hasOtherOp = false;
    for (AIOperation op : operations) {
      if (SOLO_OPS.contains(op.getType())) {
        hasSoloOp = true;
      } else {
        hasOtherOp = true;
      }
    }

    List<AIOperation> accepted = new ArrayList<>();
    for (AIOperation op : operations) {
      boolean rejected = false;

      // Mode enforcement
      if ("Advisor".equals(mode) && WRITE_OPS.contains(op.getType())) {
        errors.add("Advisor mode does not allow write operations. Rejected: "
            + op.getType());
        rejected = true;
      } else if ("ScreenEditor".equals(mode)
          && PROJECT_LEVEL_OPS.contains(op.getType())) {
        errors.add("ScreenEditor mode does not allow project-level operations. "
            + "Rejected: " + op.getType());
        rejected = true;
      }

      // View enforcement
      if (!rejected && "Designer".equals(currentView)
          && BLOCKS_OPS.contains(op.getType())) {
        errors.add("Block operations require Blocks view. Currently in Designer. "
            + "Rejected: " + op.getType());
        rejected = true;
      }
      if (!rejected && "Blocks".equals(currentView)
          && DESIGNER_OPS.contains(op.getType())) {
        errors.add("Designer operations require Designer view. Currently in Blocks. "
            + "Rejected: " + op.getType());
        rejected = true;
      }

      // Solo-op enforcement
      if (!rejected && hasSoloOp && hasOtherOp && !SOLO_OPS.contains(op.getType())) {
        errors.add("toggle_editor/switch_screen/create_screen must be the only operation. "
            + "Rejected: " + op.getType());
        rejected = true;
      }

      if (!rejected) {
        accepted.add(op);
      }
    }

    AIDebug.log(LOG, "Mode enforcement (" + mode + ", view=" + currentView + "): accepted="
        + accepted.size() + ", rejected=" + (operations.size() - accepted.size()));
    return accepted;
  }
}
