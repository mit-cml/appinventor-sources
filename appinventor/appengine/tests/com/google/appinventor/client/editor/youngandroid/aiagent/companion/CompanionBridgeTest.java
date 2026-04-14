// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.companion;

import com.google.gwt.junit.client.GWTTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT client-side unit tests for {@link CompanionBridge}.
 *
 * <p>Tests use a fake {@link ReplTransport} and fake {@link CompanionBridge.Clock} so that no
 * real Companion connection is needed. The bridge is constructed directly (package-private
 * constructor) rather than via {@link CompanionBridge#getInstance()}, so the singleton is
 * not affected.</p>
 */
public class CompanionBridgeTest extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "com.google.appinventor.YaClient";
  }

  // ---- Fakes ----

  /** Records every (schemeBody, blockId) pair sent through the transport. */
  static class RecordingTransport implements ReplTransport {
    static class Entry {
      final String scheme;
      final String blockId;
      Entry(String scheme, String blockId) {
        this.scheme = scheme;
        this.blockId = blockId;
      }
    }
    final List<Entry> sent = new ArrayList<Entry>();

    @Override
    public void send(String schemeBody, String syntheticBlockId) {
      sent.add(new Entry(schemeBody, syntheticBlockId));
    }
  }

  /** Controllable fake clock. */
  static class FakeClock implements CompanionBridge.Clock {
    long time = 1_000_000L;

    @Override
    public long now() {
      return time;
    }

    void advance(long ms) {
      time += ms;
    }
  }

  /** Records success / failure callbacks. */
  static class RecordingCallback implements CompanionBridge.Callback {
    String successValue;
    String failureError;
    int successCount = 0;
    int failureCount = 0;

    @Override
    public void onSuccess(String value) {
      successCount++;
      successValue = value;
    }

    @Override
    public void onFailure(String error) {
      failureCount++;
      failureError = error;
    }

    boolean succeeded() { return successCount > 0; }
    boolean failed()    { return failureCount > 0; }
  }

  // ---- Fixtures ----

  private RecordingTransport transport;
  private FakeClock clock;
  private CompanionBridge bridge;

  @Override
  public void gwtSetUp() {
    transport = new RecordingTransport();
    clock = new FakeClock();
    bridge = new CompanionBridge(transport, clock);
  }

  // ---- Tests: readComponentProperty ----

  public void testReadComponentPropertySendsCorrectScheme() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Button1", "Text", cb);

    assertEquals("Should send exactly one Scheme expression", 1, transport.sent.size());
    String scheme = transport.sent.get(0).scheme;
    assertTrue("Scheme must be a bare get-property call (process-repl-input wraps it)",
        scheme.equals("(get-property 'Button1 'Text)"));
    assertFalse("Scheme must NOT wrap in get-display-representation — the macro does so",
        scheme.contains("get-display-representation"));
    assertTrue("BlockId must use ai-read- prefix",
        transport.sent.get(0).blockId.startsWith(CompanionBridge.BLOCK_ID_PREFIX));
  }

  public void testReadVariableSendsCorrectScheme() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readVariable("myCounter", cb);

    assertEquals(1, transport.sent.size());
    String scheme = transport.sent.get(0).scheme;
    assertTrue("Scheme must be a bare get-var call (process-repl-input wraps it)",
        scheme.equals("(get-var g$myCounter)"));
    assertFalse("Scheme must NOT wrap in get-display-representation — the macro does so",
        scheme.contains("get-display-representation"));
  }

  // ---- Tests: resolvePending ----

  public void testResolveOkCallsOnSuccess() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Label1", "Text", cb);
    String blockId = transport.sent.get(0).blockId;

    bridge.resolvePending(blockId, "OK", "Hello World");

    assertTrue("onSuccess should have been called", cb.succeeded());
    assertEquals("Hello World", cb.successValue);
    assertFalse("onFailure should not have been called", cb.failed());
  }

  public void testResolveErrorCallsOnFailure() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Label1", "Text", cb);
    String blockId = transport.sent.get(0).blockId;

    bridge.resolvePending(blockId, "ERROR", "Component not found");

    assertTrue("onFailure should have been called", cb.failed());
    assertEquals("Component not found", cb.failureError);
    assertFalse("onSuccess should not have been called", cb.succeeded());
  }

  public void testResolveUnknownBlockIdIsIgnored() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Label1", "Text", cb);

    // Resolve with wrong blockId — no callback should fire.
    bridge.resolvePending("ai-read-deadbeef", "OK", "value");

    assertFalse(cb.succeeded());
    assertFalse(cb.failed());
  }

  public void testResolveOkWithNullValueYieldsEmptyString() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Label1", "Text", cb);
    String blockId = transport.sent.get(0).blockId;

    bridge.resolvePending(blockId, "OK", null);

    assertTrue(cb.succeeded());
    assertEquals("", cb.successValue);
  }

  public void testResolveCannotFireTwice() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Label1", "Text", cb);
    String blockId = transport.sent.get(0).blockId;

    bridge.resolvePending(blockId, "OK", "first");
    bridge.resolvePending(blockId, "OK", "second");  // second call should be dropped

    assertEquals("onSuccess should only be called once", 1, cb.successCount);
    assertEquals("first", cb.successValue);
  }

  // ---- Tests: identifier validation ----

  public void testInvalidComponentNameRejectsImmediately() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("123bad!", "Text", cb);

    assertTrue("Should fail on invalid component name", cb.failed());
    assertTrue(cb.failureError.contains("component_name"));
    assertEquals("Nothing should be sent", 0, transport.sent.size());
  }

  public void testInvalidPropertyNameRejectsImmediately() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty("Button1", "bad-prop", cb);

    assertTrue("Should fail on invalid property name", cb.failed());
    assertTrue(cb.failureError.contains("property_name"));
    assertEquals(0, transport.sent.size());
  }

  public void testNullComponentNameRejectsImmediately() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readComponentProperty(null, "Text", cb);

    assertTrue(cb.failed());
    assertEquals(0, transport.sent.size());
  }

  public void testInvalidVariableNameRejectsImmediately() {
    RecordingCallback cb = new RecordingCallback();
    bridge.readVariable("bad var!", cb);

    assertTrue(cb.failed());
    assertTrue(cb.failureError.contains("variable_name"));
    assertEquals(0, transport.sent.size());
  }

  // ---- Tests: per-turn budget ----

  public void testPerTurnBudgetEnforcedAt10() {
    // Dispatch 10 reads — all should succeed (be sent).
    for (int i = 0; i < 10; i++) {
      RecordingCallback cb = new RecordingCallback();
      bridge.readVariable("v" + i, cb);
      assertFalse("Read " + i + " should not fail immediately", cb.failed());
    }
    assertEquals("All 10 reads should be dispatched", 10, transport.sent.size());

    // 11th read must be rejected.
    RecordingCallback eleventh = new RecordingCallback();
    bridge.readVariable("extra", eleventh);
    assertTrue("11th read should be rejected by per-turn cap", eleventh.failed());
    assertTrue(eleventh.failureError.contains("budget exceeded"));
    assertEquals("No 11th send should occur", 10, transport.sent.size());
  }

  public void testResetTurnBudgetAllowsMoreReads() {
    for (int i = 0; i < 10; i++) {
      bridge.readVariable("v" + i, new RecordingCallback());
    }

    bridge.resetTurnBudget();

    RecordingCallback cb = new RecordingCallback();
    bridge.readVariable("afterReset", cb);
    assertFalse("Read after reset should not fail", cb.failed());
    assertEquals(11, transport.sent.size());
  }

  // ---- Tests: per-minute budget ----

  public void testPerMinuteBudgetEnforcedAt30() {
    // Use 3 turns of 10 reads each, at increasing timestamps within the window.
    for (int turn = 0; turn < 3; turn++) {
      bridge.resetTurnBudget();
      clock.advance(1000); // stay within the 60s window
      for (int i = 0; i < 10; i++) {
        bridge.readVariable("v" + (turn * 10 + i), new RecordingCallback());
      }
    }
    assertEquals("30 reads should be dispatched", 30, transport.sent.size());

    // 31st read in the same minute window must be rejected.
    bridge.resetTurnBudget();
    RecordingCallback extra = new RecordingCallback();
    bridge.readVariable("extra", extra);
    assertTrue("31st read should be rejected by per-minute cap", extra.failed());
    assertTrue(extra.failureError.contains("rate limit"));
  }

  public void testOldTimestampsAreEvictedFromWindow() {
    // Fill up 30 reads.
    for (int turn = 0; turn < 3; turn++) {
      bridge.resetTurnBudget();
      for (int i = 0; i < 10; i++) {
        bridge.readVariable("v" + (turn * 10 + i), new RecordingCallback());
      }
    }

    // Advance past the 60-second window so all timestamps expire.
    clock.advance(61_000L);
    bridge.resetTurnBudget();

    RecordingCallback cb = new RecordingCallback();
    bridge.readVariable("afterWindow", cb);
    assertFalse("Read after window expiry should succeed", cb.failed());
    assertEquals(31, transport.sent.size());
  }
}
