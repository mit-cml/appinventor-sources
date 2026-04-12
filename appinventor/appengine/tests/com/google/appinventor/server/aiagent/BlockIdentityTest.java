// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import junit.framework.TestCase;

/**
 * Tests for {@link BlockIdentity}, the YAIL identity extractor used by
 * {@link ModeEnforcer} to detect duplicate-mutation batches.
 */
public class BlockIdentityTest extends TestCase {

  // ---- fromWriteYail ----

  public void testWriteEventIdentity() {
    assertEquals("define-event Button1 Click",
        BlockIdentity.fromWriteYail(
            "(define-event Button1 Click ()\n  (set-this-form))"));
  }

  public void testWriteEventIdentityWithLeadingWhitespace() {
    assertEquals("define-event Button1 Click",
        BlockIdentity.fromWriteYail(
            "  \n  (define-event Button1 Click ()\n  (set-this-form))"));
  }

  public void testWriteGenericEventIdentity() {
    assertEquals("define-generic-event Button Click",
        BlockIdentity.fromWriteYail(
            "(define-generic-event Button Click ($component $notAlready) ...)"));
  }

  public void testWriteGlobalVariableIdentity() {
    assertEquals("def g$score",
        BlockIdentity.fromWriteYail("(def g$score 0)"));
  }

  public void testWriteProcedureIdentity() {
    assertEquals("def p$factorial",
        BlockIdentity.fromWriteYail(
            "(def (p$factorial $n) (if (= $n 0) 1 (* $n (p$factorial (- $n 1)))))"));
  }

  public void testWriteProcedureWithReturnIdentity() {
    // def-return is normalized to def so identities compare across both forms.
    assertEquals("def p$double",
        BlockIdentity.fromWriteYail("(def-return (p$double $x) (* $x 2))"));
  }

  public void testWriteNullYail() {
    assertNull(BlockIdentity.fromWriteYail(null));
  }

  public void testWriteEmptyYail() {
    assertNull(BlockIdentity.fromWriteYail(""));
  }

  public void testWriteUnknownForm() {
    assertNull(BlockIdentity.fromWriteYail("(set-var! x 5)"));
  }

  public void testWriteMissingComponentToken() {
    assertNull(BlockIdentity.fromWriteYail("(define-event)"));
  }

  // ---- fromDeleteIdentifier ----

  public void testDeleteEventIdentity() {
    assertEquals("define-event Button1 Click",
        BlockIdentity.fromDeleteIdentifier("define-event Button1 Click"));
  }

  public void testDeleteGenericEventIdentity() {
    assertEquals("define-generic-event Button Click",
        BlockIdentity.fromDeleteIdentifier("define-generic-event Button Click"));
  }

  public void testDeleteGlobalIdentity() {
    assertEquals("def g$score",
        BlockIdentity.fromDeleteIdentifier("def g$score"));
  }

  public void testDeleteProcedureIdentity() {
    assertEquals("def p$factorial",
        BlockIdentity.fromDeleteIdentifier("def p$factorial"));
  }

  public void testDeleteReturnProcedureNormalizedToDef() {
    // Matches the def/def-return normalization on the write side so a
    // write_block for (def-return ...) and delete_block for
    // "def-return p$x" are recognized as the same identity.
    assertEquals("def p$double",
        BlockIdentity.fromDeleteIdentifier("def-return p$double"));
  }

  public void testDeleteIdentifierExtraWhitespaceTolerated() {
    assertEquals("define-event Button1 Click",
        BlockIdentity.fromDeleteIdentifier("  define-event   Button1   Click  "));
  }

  public void testDeleteNull() {
    assertNull(BlockIdentity.fromDeleteIdentifier(null));
  }

  public void testDeleteBlank() {
    assertNull(BlockIdentity.fromDeleteIdentifier("   "));
  }

  public void testDeleteUnknownForm() {
    assertNull(BlockIdentity.fromDeleteIdentifier("call-yail-primitive foo"));
  }

  // ---- cross-check: write and delete produce matching identities ----

  public void testWriteAndDeleteProduceSameEventIdentity() {
    String writeId = BlockIdentity.fromWriteYail(
        "(define-event Btn Click () (set-this-form))");
    String deleteId = BlockIdentity.fromDeleteIdentifier(
        "define-event Btn Click");
    assertEquals(writeId, deleteId);
  }

  public void testWriteAndDeleteProduceSameProcedureIdentity() {
    String writeId = BlockIdentity.fromWriteYail(
        "(def-return (p$square $x) (* $x $x))");
    String deleteId = BlockIdentity.fromDeleteIdentifier("def-return p$square");
    assertEquals(writeId, deleteId);
  }
}
