// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.LLMResponseParser.ParseResult;
import com.google.appinventor.server.aiagent.LLMResponseParser.RawToolCall;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link LLMResponseParser} Companion runtime-read tool handling
 * ({@code read_component_property}, {@code read_variable}, {@code read_recent_logs}).
 */
public class LLMResponseParserCompanionTest extends TestCase {

  private LLMResponseParser parser;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    parser = new LLMResponseParser();
  }

  // ---- read_component_property ----

  public void testReadComponentPropertyParsesToReadRuntime() throws Exception {
    RawToolCall call = new RawToolCall("read_component_property",
        "{\"component_name\":\"Button1\",\"property_name\":\"Text\"}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertFalse("Expected no errors but got: " + result.getErrors(), result.hasErrors());
    assertEquals(1, result.getOperations().size());

    AIOperation op = result.getOperations().get(0);
    assertEquals(AIOperation.Type.READ_RUNTIME, op.getType());

    JSONObject payload = new JSONObject(op.getPayload());
    assertEquals("read_component_property", payload.getString("tool"));
    JSONObject args = payload.getJSONObject("args");
    assertEquals("Button1", args.getString("component_name"));
    assertEquals("Text", args.getString("property_name"));
  }

  // ---- read_variable ----

  public void testReadVariableParsesToReadRuntime() throws Exception {
    RawToolCall call = new RawToolCall("read_variable",
        "{\"variable_name\":\"score\"}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertFalse("Expected no errors but got: " + result.getErrors(), result.hasErrors());
    assertEquals(1, result.getOperations().size());

    AIOperation op = result.getOperations().get(0);
    assertEquals(AIOperation.Type.READ_RUNTIME, op.getType());

    JSONObject payload = new JSONObject(op.getPayload());
    assertEquals("read_variable", payload.getString("tool"));
    JSONObject args = payload.getJSONObject("args");
    assertEquals("score", args.getString("variable_name"));
  }

  // ---- read_recent_logs ----

  public void testReadRecentLogsParsesToReadRuntime() throws Exception {
    RawToolCall call = new RawToolCall("read_recent_logs", "{\"n\":15}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertFalse("Expected no errors but got: " + result.getErrors(), result.hasErrors());
    assertEquals(1, result.getOperations().size());

    AIOperation op = result.getOperations().get(0);
    assertEquals(AIOperation.Type.READ_RUNTIME, op.getType());

    JSONObject payload = new JSONObject(op.getPayload());
    assertEquals("read_recent_logs", payload.getString("tool"));
    JSONObject args = payload.getJSONObject("args");
    assertEquals(15, args.getInt("n"));
  }

  public void testReadRecentLogsWithoutNStillParses() throws Exception {
    RawToolCall call = new RawToolCall("read_recent_logs", "{}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertFalse("Expected no errors but got: " + result.getErrors(), result.hasErrors());
    assertEquals(1, result.getOperations().size());

    AIOperation op = result.getOperations().get(0);
    assertEquals(AIOperation.Type.READ_RUNTIME, op.getType());

    JSONObject payload = new JSONObject(op.getPayload());
    assertEquals("read_recent_logs", payload.getString("tool"));
    JSONObject args = payload.getJSONObject("args");
    assertFalse("Args should be empty when n is omitted", args.keys().hasNext());
  }

  // ---- validation errors ----

  public void testReadComponentPropertyMissingComponentNameReportsError() {
    RawToolCall call = new RawToolCall("read_component_property",
        "{\"property_name\":\"Text\"}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertEquals(0, result.getOperations().size());
    assertEquals(1, result.getErrors().size());
    String error = result.getErrors().get(0);
    assertTrue("Error should mention 'component_name': " + error,
        error.contains("component_name"));
    assertTrue("Error should mention 'read_component_property': " + error,
        error.contains("read_component_property"));
  }

  public void testReadComponentPropertyMissingPropertyNameReportsError() {
    RawToolCall call = new RawToolCall("read_component_property",
        "{\"component_name\":\"Button1\"}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertEquals(0, result.getOperations().size());
    assertEquals(1, result.getErrors().size());
    String error = result.getErrors().get(0);
    assertTrue("Error should mention 'property_name': " + error,
        error.contains("property_name"));
    assertTrue("Error should mention 'read_component_property': " + error,
        error.contains("read_component_property"));
  }

  public void testReadVariableMissingVariableNameReportsError() {
    RawToolCall call = new RawToolCall("read_variable", "{}");

    ParseResult result = parser.parseToolCalls(Collections.singletonList(call));

    assertEquals(0, result.getOperations().size());
    assertEquals(1, result.getErrors().size());
    String error = result.getErrors().get(0);
    assertTrue("Error should mention 'variable_name': " + error,
        error.contains("variable_name"));
    assertTrue("Error should mention 'read_variable': " + error,
        error.contains("read_variable"));
  }
}
