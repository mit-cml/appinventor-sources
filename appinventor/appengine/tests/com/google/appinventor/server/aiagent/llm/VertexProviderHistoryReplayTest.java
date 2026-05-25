// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Unit tests for {@link VertexProvider#appendHistoryTurnToContents}. Vertex
 * reuses the Gemini wire format, but we keep a separate test so accidental
 * divergence is caught.
 */
public class VertexProviderHistoryReplayTest extends TestCase {

  public void testUserPlainBecomesUserRoleTextPart() {
    JSONArray contents = new JSONArray();
    VertexProvider.appendHistoryTurnToContents(contents,
        new ChatMessage("user", "hello", null, true, 1L));
    JSONObject c = contents.getJSONObject(0);
    assertEquals("user", c.getString("role"));
    assertEquals("hello", c.getJSONArray("parts").getJSONObject(0).getString("text"));
  }

  public void testAssistantPlainBecomesModelRoleTextPart() {
    JSONArray contents = new JSONArray();
    VertexProvider.appendHistoryTurnToContents(contents,
        new ChatMessage("assistant", "hi there", null, true, 2L));
    assertEquals("model", contents.getJSONObject(0).getString("role"));
  }

  public void testAssistantToolUseBecomesFunctionCallPart() {
    String structured = new JSONArray()
        .put(new JSONObject()
            .put("type", "tool_use")
            .put("id", "tc_abc")
            .put("name", "add_component")
            .put("input", new JSONObject().put("name", "Button1")))
        .toString();
    JSONArray contents = new JSONArray();
    VertexProvider.appendHistoryTurnToContents(contents,
        new ChatMessage("assistant", "", structured, true, 3L));
    JSONObject c = contents.getJSONObject(0);
    assertEquals("model", c.getString("role"));
    JSONObject fc = c.getJSONArray("parts").getJSONObject(0)
        .getJSONObject("functionCall");
    assertEquals("add_component", fc.getString("name"));
    assertEquals("Button1", fc.getJSONObject("args").getString("name"));
  }

  public void testToolResultBecomesFunctionRoleFunctionResponsePart() {
    String structured = new JSONArray()
        .put(new JSONObject()
            .put("type", "tool_result")
            .put("tool_use_id", "tc_abc")
            .put("tool_name", "add_component")
            .put("content", "Done."))
        .toString();
    JSONArray contents = new JSONArray();
    VertexProvider.appendHistoryTurnToContents(contents,
        new ChatMessage("tool_result", "[Tool results applied]", structured,
            false, 4L));
    JSONObject c = contents.getJSONObject(0);
    assertEquals("function", c.getString("role"));
    JSONObject fr = c.getJSONArray("parts").getJSONObject(0)
        .getJSONObject("functionResponse");
    assertEquals("add_component", fr.getString("name"));
    assertEquals("Done.", fr.getJSONObject("response").getString("content"));
  }
}
