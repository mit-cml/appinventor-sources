// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_ADVISOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

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
          AIOperation.Type.SWITCH_SCREEN,
          AIOperation.Type.CREATE_SCREEN,
          AIOperation.Type.DELETE_SCREEN,
          AIOperation.Type.SET_PROJECT_PROP,
          AIOperation.Type.PROPOSE_PLAN)));

  /** Project-level operations only allowed in ProjectEditor mode. */
  static final Set<AIOperation.Type> PROJECT_LEVEL_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.SWITCH_SCREEN,
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
      String mode, String currentView, EnforcementContext context,
      List<String> errors) {
    if (operations.isEmpty()) {
      return operations;
    }

    if (context == EnforcementContext.PLANNING) {
      List<AIOperation> planOnly = new ArrayList<>();
      for (AIOperation op : operations) {
        if (op.getType() == AIOperation.Type.PROPOSE_PLAN) {
          planOnly.add(op);
        } else if (WRITE_OPS.contains(op.getType())) {
          errors.add("Operation " + op.getType() + " is not allowed during planning.");
        } else {
          planOnly.add(op);
        }
      }
      return planOnly;
    }

    if (context == EnforcementContext.CHILD_EXECUTION) {
      List<AIOperation> screenOnly = new ArrayList<>();
      for (AIOperation op : operations) {
        if (op.getType() == AIOperation.Type.PROPOSE_PLAN
            || PROJECT_LEVEL_OPS.contains(op.getType())) {
          errors.add("Operation " + op.getType() + " is not allowed for child agents.");
        } else {
          screenOnly.add(op);
        }
      }
      operations = screenOnly;
      // Fall through to existing mode/view enforcement below
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

    // Tracks the identity of the first accepted WRITE_BLOCK / DELETE_BLOCK
    // per block identity, so we can reject any later op in the same batch
    // that targets the same identity.  Preventing a mixed write+delete on
    // one identity avoids a destructive interaction with the WRITE_BLOCK
    // upsert path (the write replaces one duplicate, then the delete
    // consumes the newly-written block).  We apply the same rule to
    // write+write and delete+delete for consistency: one mutation per
    // identity per batch.
    Map<String, AIOperation.Type> seenBlockIdentities = new HashMap<>();

    List<AIOperation> accepted = new ArrayList<>();
    for (AIOperation op : operations) {
      boolean rejected = false;

      // Mode enforcement
      if (AI_AGENT_MODE_ADVISOR.equals(mode) && WRITE_OPS.contains(op.getType())) {
        errors.add("Advisor mode does not allow write operations. Rejected: "
            + op.getType());
        rejected = true;
      } else if (AI_AGENT_MODE_SCREEN_EDITOR.equals(mode)
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

      // Block-identity conflict enforcement
      if (!rejected && BLOCKS_OPS.contains(op.getType())) {
        String identity = extractBlockIdentity(op);
        if (identity != null) {
          AIOperation.Type prior = seenBlockIdentities.get(identity);
          if (prior != null) {
            errors.add("Block identity '" + identity + "' is already targeted by "
                + "a prior " + prior + " in this batch. Each block may be "
                + "mutated at most once per batch — issue additional changes "
                + "in a follow-up turn. Rejected: " + op.getType());
            rejected = true;
          } else {
            seenBlockIdentities.put(identity, op.getType());
          }
        }
      }

      if (!rejected) {
        accepted.add(op);
      }
    }

    AIDebug.log(LOG, "Mode enforcement (" + mode + ", view=" + currentView + "): accepted="
        + accepted.size() + ", rejected=" + (operations.size() - accepted.size()));
    return accepted;
  }

  /**
   * Extract the canonical block identity from a {@link AIOperation.Type#WRITE_BLOCK}
   * or {@link AIOperation.Type#DELETE_BLOCK} payload.  Returns {@code null}
   * for unrecognized payloads — callers should then skip the conflict
   * check (parse errors are surfaced separately in {@link LLMResponseParser}).
   */
  private static String extractBlockIdentity(AIOperation op) {
    try {
      JSONObject payload = new JSONObject(op.getPayload());
      if (op.getType() == AIOperation.Type.WRITE_BLOCK) {
        return BlockIdentity.fromWriteYail(payload.optString("yail", null));
      }
      if (op.getType() == AIOperation.Type.DELETE_BLOCK) {
        return BlockIdentity.fromDeleteIdentifier(payload.optString("block", null));
      }
    } catch (JSONException e) {
      // Malformed payloads are caught by the parser stage — fall through.
    }
    return null;
  }
}
