// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.companion;

import java.util.List;

import junit.framework.TestCase;

/**
 * Unit tests for {@link BlockYailSymbolScanner}.
 *
 * <p>Pure Java — no GWT deps. Runs under AiClientLibTests.</p>
 */
public class BlockYailSymbolScannerTest extends TestCase {

  // ---- 1. Null / empty YAIL ----

  public void testEmptyYailReturnsEmpty() {
    assertTrue("null yail should return empty",
        BlockYailSymbolScanner.scan(null, 10).isEmpty());
    assertTrue("empty string should return empty",
        BlockYailSymbolScanner.scan("", 10).isEmpty());
  }

  // ---- 2. get-property ----

  public void testScanFindsGetProperty() {
    String yail = "(get-property 'Button1 'Text)";
    List<BlockYailSymbolScanner.Symbol> syms = BlockYailSymbolScanner.scan(yail, 10);
    assertEquals(1, syms.size());
    BlockYailSymbolScanner.Symbol s = syms.get(0);
    assertEquals(BlockYailSymbolScanner.Kind.PROPERTY, s.kind);
    assertEquals("Button1", s.componentOrVar);
    assertEquals("Text", s.propertyName);
  }

  // ---- 3. get-var ----

  public void testScanFindsGetVar() {
    String yail = "(get-var g$items)";
    List<BlockYailSymbolScanner.Symbol> syms = BlockYailSymbolScanner.scan(yail, 10);
    assertEquals(1, syms.size());
    BlockYailSymbolScanner.Symbol s = syms.get(0);
    assertEquals(BlockYailSymbolScanner.Kind.VARIABLE, s.kind);
    assertEquals("items", s.componentOrVar);
    assertNull(s.propertyName);
  }

  // ---- 4. Mixed YAIL ----

  public void testMixedYailReturnsBoth() {
    String yail = "(get-property 'Label1 'BackgroundColor) (get-var g$counter)";
    List<BlockYailSymbolScanner.Symbol> syms = BlockYailSymbolScanner.scan(yail, 10);
    assertEquals(2, syms.size());
    assertEquals(BlockYailSymbolScanner.Kind.PROPERTY, syms.get(0).kind);
    assertEquals(BlockYailSymbolScanner.Kind.VARIABLE, syms.get(1).kind);
  }

  // ---- 5. Deduplication ----

  public void testDedupes() {
    String yail = "(get-property 'Button1 'Text) (get-property 'Button1 'Text)";
    List<BlockYailSymbolScanner.Symbol> syms = BlockYailSymbolScanner.scan(yail, 10);
    assertEquals("duplicate references should be collapsed to one", 1, syms.size());
  }

  // ---- 6. Cap enforced ----

  public void testCapEnforced() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 15; i++) {
      sb.append("(get-property 'Button").append(i).append(" 'Text) ");
    }
    List<BlockYailSymbolScanner.Symbol> syms = BlockYailSymbolScanner.scan(sb.toString(), 10);
    assertEquals("cap=10 should return at most 10 symbols", 10, syms.size());
  }

  // ---- 7. Non-positive cap returns empty ----

  public void testNegativeCapReturnsEmpty() {
    String yail = "(get-property 'Button1 'Text)";
    assertTrue("cap=0 should return empty",
        BlockYailSymbolScanner.scan(yail, 0).isEmpty());
    assertTrue("cap=-1 should return empty",
        BlockYailSymbolScanner.scan(yail, -1).isEmpty());
  }

  // ---- 8. Procedure refs (p$) ignored ----

  public void testIgnoresProcedureRefs() {
    String yail = "(get-var p$factorial)";
    List<BlockYailSymbolScanner.Symbol> syms = BlockYailSymbolScanner.scan(yail, 10);
    assertTrue("p$ prefixed vars should not be matched by g$ pattern", syms.isEmpty());
  }

  // ---- 9. Symbol equality ----

  public void testEquality() {
    BlockYailSymbolScanner.Symbol a = BlockYailSymbolScanner.Symbol.property("Button1", "Text");
    BlockYailSymbolScanner.Symbol b = BlockYailSymbolScanner.Symbol.property("Button1", "Text");
    assertEquals("two property symbols with same args should be equal", a, b);
    assertEquals("equal symbols must have equal hash codes", a.hashCode(), b.hashCode());

    BlockYailSymbolScanner.Symbol v1 = BlockYailSymbolScanner.Symbol.variable("count");
    BlockYailSymbolScanner.Symbol v2 = BlockYailSymbolScanner.Symbol.variable("count");
    assertEquals(v1, v2);

    assertFalse("property and variable with same name should not be equal",
        a.equals(v1));
  }
}
