// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the "one mutation per block identity per batch" rule enforced by
 * {@link ModeEnforcer}.  This prevents the destructive interaction where
 * a WRITE_BLOCK upsert plus a matching DELETE_BLOCK silently erase the
 * newly-written block.
 */
public class ModeEnforcerBlockIdentityTest extends TestCase {

  private static final String MODE_PROJECT_EDITOR = "ProjectEditor";
  private static final String VIEW_BLOCKS = "Blocks";

  private static AIOperation writeBlock(String yail) {
    return new AIOperation(AIOperation.Type.WRITE_BLOCK,
        "{\"yail\":" + quote(yail) + "}");
  }

  private static AIOperation deleteBlock(String blockId) {
    return new AIOperation(AIOperation.Type.DELETE_BLOCK,
        "{\"block\":" + quote(blockId) + "}");
  }

  private static String quote(String s) {
    // Minimal JSON escape for test fixtures — no embedded quotes/newlines here.
    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  public void testWriteAndDeleteSameIdentityRejectsDelete() {
    List<AIOperation> ops = new ArrayList<>(Arrays.asList(
        writeBlock("(define-event Button1 Click () (set-this-form))"),
        deleteBlock("define-event Button1 Click")));
    List<String> errors = new ArrayList<>();

    List<AIOperation> accepted = ModeEnforcer.enforce(
        ops, MODE_PROJECT_EDITOR, VIEW_BLOCKS,
        EnforcementContext.STANDARD, errors);

    assertEquals("Write should survive, delete should be dropped",
        1, accepted.size());
    assertEquals(AIOperation.Type.WRITE_BLOCK, accepted.get(0).getType());
    assertEquals(1, errors.size());
    assertTrue("Error should mention the conflicting identity: " + errors.get(0),
        errors.get(0).contains("define-event Button1 Click"));
  }

  public void testDeleteThenWriteSameIdentityRejectsWrite() {
    // Order matters: the first occurrence wins.
    List<AIOperation> ops = new ArrayList<>(Arrays.asList(
        deleteBlock("define-event Button1 Click"),
        writeBlock("(define-event Button1 Click () (set-this-form))")));
    List<String> errors = new ArrayList<>();

    List<AIOperation> accepted = ModeEnforcer.enforce(
        ops, MODE_PROJECT_EDITOR, VIEW_BLOCKS,
        EnforcementContext.STANDARD, errors);

    assertEquals(1, accepted.size());
    assertEquals(AIOperation.Type.DELETE_BLOCK, accepted.get(0).getType());
    assertEquals(1, errors.size());
  }

  public void testDoubleDeleteSameIdentityRejectsSecond() {
    // Duplicate-removal has to happen across turns now.
    List<AIOperation> ops = new ArrayList<>(Arrays.asList(
        deleteBlock("define-event Button1 Click"),
        deleteBlock("define-event Button1 Click")));
    List<String> errors = new ArrayList<>();

    List<AIOperation> accepted = ModeEnforcer.enforce(
        ops, MODE_PROJECT_EDITOR, VIEW_BLOCKS,
        EnforcementContext.STANDARD, errors);

    assertEquals(1, accepted.size());
    assertEquals(1, errors.size());
  }

  public void testDoubleWriteSameIdentityRejectsSecond() {
    List<AIOperation> ops = new ArrayList<>(Arrays.asList(
        writeBlock("(define-event Button1 Click () (set-this-form))"),
        writeBlock("(define-event Button1 Click () (set-this-form))")));
    List<String> errors = new ArrayList<>();

    List<AIOperation> accepted = ModeEnforcer.enforce(
        ops, MODE_PROJECT_EDITOR, VIEW_BLOCKS,
        EnforcementContext.STANDARD, errors);

    assertEquals(1, accepted.size());
    assertEquals(1, errors.size());
  }

  public void testDifferentIdentitiesCoexist() {
    // Baseline: the rule only fires on matching identities.
    List<AIOperation> ops = new ArrayList<>(Arrays.asList(
        writeBlock("(define-event Button1 Click () (set-this-form))"),
        deleteBlock("define-event Button2 Click"),
        writeBlock("(def g$score 0)")));
    List<String> errors = new ArrayList<>();

    List<AIOperation> accepted = ModeEnforcer.enforce(
        ops, MODE_PROJECT_EDITOR, VIEW_BLOCKS,
        EnforcementContext.STANDARD, errors);

    assertEquals(3, accepted.size());
    assertEquals(0, errors.size());
  }

  public void testWriteAndDeleteAcrossDefReturnNormalization() {
    // A write of (def-return ...) and a delete of "def p$x" must be
    // recognized as the same identity (both normalize to "def p$x").
    List<AIOperation> ops = new ArrayList<>(Arrays.asList(
        writeBlock("(def-return (p$double $x) (* $x 2))"),
        deleteBlock("def p$double")));
    List<String> errors = new ArrayList<>();

    List<AIOperation> accepted = ModeEnforcer.enforce(
        ops, MODE_PROJECT_EDITOR, VIEW_BLOCKS,
        EnforcementContext.STANDARD, errors);

    assertEquals(1, accepted.size());
    assertEquals(1, errors.size());
  }
}
