// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tests for CompanionModule.
 */
public class CompanionModuleTest extends TestCase {

  private CompanionModule module;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    module = new CompanionModule();
  }

  /** Helper to build a ContextParams with only the companionSnapshot set. */
  private static ContextParams paramsWithSnapshot(String snapshot) {
    return new ContextParams(
        null, 0L, null, null, null, null, null, null, null, null, null,
        snapshot, null);
  }

  public void testNullSnapshotReturnsNull() {
    ContextParams params = paramsWithSnapshot(null);
    assertNull(module.build(params));
  }

  public void testEmptySnapshotReturnsNull() {
    ContextParams params = paramsWithSnapshot("");
    assertNull(module.build(params));
  }

  public void testMalformedJsonReturnsNull() {
    ContextParams params = paramsWithSnapshot("not json");
    assertNull(module.build(params));
  }

  public void testPopulatedSnapshotIncludesAllSections() {
    JSONObject snapshot = new JSONObject();
    snapshot.put("connectionKind", "webrtc");
    snapshot.put("activeScreen", "Screen1");

    JSONArray logs = new JSONArray();
    JSONObject log = new JSONObject();
    log.put("level", "info");
    log.put("text", "hello world");
    log.put("timestamp", 1000);
    logs.put(log);
    snapshot.put("logs", logs);

    JSONArray errors = new JSONArray();
    JSONObject error = new JSONObject();
    error.put("message", "Select list item: Attempting to get item 4 of length 3");
    error.put("blockId", "block1");
    error.put("componentName", "Button1");
    error.put("timestamp", 2000);
    errors.put(error);
    snapshot.put("errors", errors);

    ContextParams params = paramsWithSnapshot(snapshot.toString());
    String result = module.build(params);

    assertNotNull(result);
    assertTrue(result.contains("Companion runtime state"));
    assertTrue(result.contains("webrtc"));
    assertTrue(result.contains("Screen1"));
    assertTrue(result.contains("hello world"));
    assertTrue(result.contains("Button1"));
    assertTrue(result.contains("Select list item"));
  }

  public void testTruncationAtDeclaredBounds() {
    JSONObject snapshot = new JSONObject();
    snapshot.put("connectionKind", "http");
    snapshot.put("activeScreen", "Screen1");

    JSONArray logs = new JSONArray();
    for (int i = 0; i < 20; i++) {
      JSONObject log = new JSONObject();
      log.put("level", "info");
      log.put("text", "log entry " + i);
      log.put("timestamp", i);
      logs.put(log);
    }
    snapshot.put("logs", logs);

    JSONArray errors = new JSONArray();
    for (int i = 0; i < 5; i++) {
      JSONObject error = new JSONObject();
      error.put("message", "error message " + i);
      error.put("blockId", "block" + i);
      error.put("componentName", "Label" + i);
      error.put("timestamp", i);
      errors.put(error);
    }
    snapshot.put("errors", errors);

    ContextParams params = paramsWithSnapshot(snapshot.toString());
    String result = module.build(params);

    assertNotNull(result);

    // Count log lines: each rendered as "- [info] log entry N"
    int logCount = countOccurrences(result, "- [info]");
    assertEquals(10, logCount);

    // Count error lines: each rendered as "- [LabelN] error message N"
    int errorCount = countOccurrences(result, "- [Label");
    assertEquals(3, errorCount);
  }

  public void testEmptyArraysSkipSections() {
    JSONObject snapshot = new JSONObject();
    snapshot.put("connectionKind", "webrtc");
    snapshot.put("activeScreen", "Screen1");
    snapshot.put("logs", new JSONArray());
    snapshot.put("errors", new JSONArray());

    ContextParams params = paramsWithSnapshot(snapshot.toString());
    String result = module.build(params);

    assertNotNull(result);
    assertFalse(result.contains("Recent errors"));
    assertFalse(result.contains("Recent logs"));
    // Connection line should still be present
    assertTrue(result.contains("webrtc"));
  }

  public void testMissingOptionalFieldsRender() {
    JSONObject snapshot = new JSONObject();
    // No connectionKind, no activeScreen, no errors — only logs
    JSONArray logs = new JSONArray();
    JSONObject log = new JSONObject();
    log.put("level", "warn");
    log.put("text", "something happened");
    log.put("timestamp", 999);
    logs.put(log);
    snapshot.put("logs", logs);

    ContextParams params = paramsWithSnapshot(snapshot.toString());
    String result = module.build(params);

    assertNotNull(result);
    assertTrue(result.contains("something happened"));
    // Should not throw; absent sections simply omitted
    assertFalse(result.contains("Recent errors"));
  }

  // ---------- helpers ----------

  private static int countOccurrences(String text, String sub) {
    int count = 0;
    int idx = 0;
    while ((idx = text.indexOf(sub, idx)) != -1) {
      count++;
      idx += sub.length();
    }
    return count;
  }
}
