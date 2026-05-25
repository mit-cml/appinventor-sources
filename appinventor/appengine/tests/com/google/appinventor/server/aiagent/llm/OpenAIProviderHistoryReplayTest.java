// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit tests for {@link OpenAIProvider#appendHistoryTurnToInput}, which builds
 * the Responses API {@code input} array entries for the cold-memcache
 * history-replay fallback path.
 */
public class OpenAIProviderHistoryReplayTest extends TestCase {

  public void testUserPlainTextBecomesInputText() {
    JSONArray input = new JSONArray();
    OpenAIProvider.appendHistoryTurnToInput(input,
        new ChatMessage("user", "hello", null, true, 1L));
    assertEquals(1, input.length());
    JSONObject entry = input.getJSONObject(0);
    assertEquals("user", entry.getString("role"));
    JSONArray content = entry.getJSONArray("content");
    assertEquals("input_text", content.getJSONObject(0).getString("type"));
    assertEquals("hello", content.getJSONObject(0).getString("text"));
  }

  public void testAssistantPlainTextBecomesOutputText() {
    JSONArray input = new JSONArray();
    OpenAIProvider.appendHistoryTurnToInput(input,
        new ChatMessage("assistant", "hi there", null, true, 2L));
    assertEquals(1, input.length());
    JSONObject entry = input.getJSONObject(0);
    assertEquals("assistant", entry.getString("role"));
    assertEquals("output_text",
        entry.getJSONArray("content").getJSONObject(0).getString("type"));
  }

  public void testAssistantToolUseBecomesFunctionCall() {
    String structured = new JSONArray()
        .put(new JSONObject().put("type", "text").put("text", "I'll add it."))
        .put(new JSONObject()
            .put("type", "tool_use")
            .put("id", "tc_abc")
            .put("name", "add_component")
            .put("input", new JSONObject().put("name", "Button1")))
        .toString();
    JSONArray input = new JSONArray();
    OpenAIProvider.appendHistoryTurnToInput(input,
        new ChatMessage("assistant", "I'll add it.", structured, true, 3L));
    // Expect: leading "message" entry with output_text, then function_call entry.
    assertEquals(2, input.length());
    JSONObject msg = input.getJSONObject(0);
    assertEquals("assistant", msg.getString("role"));
    assertEquals("output_text",
        msg.getJSONArray("content").getJSONObject(0).getString("type"));
    JSONObject fc = input.getJSONObject(1);
    assertEquals("function_call", fc.getString("type"));
    assertEquals("tc_abc", fc.getString("call_id"));
    assertEquals("add_component", fc.getString("name"));
    // arguments is a JSON string
    JSONObject args = new JSONObject(fc.getString("arguments"));
    assertEquals("Button1", args.getString("name"));
  }

  public void testToolResultBecomesFunctionCallOutput() {
    String structured = new JSONArray()
        .put(new JSONObject()
            .put("type", "tool_result")
            .put("tool_use_id", "tc_abc")
            .put("tool_name", "add_component")
            .put("content", "Done."))
        .toString();
    JSONArray input = new JSONArray();
    OpenAIProvider.appendHistoryTurnToInput(input,
        new ChatMessage("tool_result", "[Tool results applied]", structured,
            false, 4L));
    assertEquals(1, input.length());
    JSONObject out = input.getJSONObject(0);
    assertEquals("function_call_output", out.getString("type"));
    assertEquals("tc_abc", out.getString("call_id"));
    assertEquals("Done.", out.getString("output"));
  }

  public void testHistoryReplayPreservesOrder() {
    // Typical 2-message history: user -> assistant. Assert the outbound
    // input array starts with those two, in order.
    JSONArray input = new JSONArray();
    OpenAIProvider.appendHistoryTurnToInput(input,
        new ChatMessage("user", "hello", null, true, 1L));
    OpenAIProvider.appendHistoryTurnToInput(input,
        new ChatMessage("assistant", "hi", null, true, 2L));
    // Simulated current user message appended after history.
    input.put(new JSONObject().put("role", "user").put("content", "what's up"));

    assertEquals(3, input.length());
    assertEquals("user", input.getJSONObject(0).getString("role"));
    assertEquals("assistant", input.getJSONObject(1).getString("role"));
    assertEquals("user", input.getJSONObject(2).getString("role"));
  }
}
