// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.executor;

import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * Executes synchronous block operations: WRITE_BLOCK, DELETE_BLOCK.
 */
final class AIBlockOperations {

  private AIBlockOperations() {}

  static void executeWriteBlock(JSONObject json) {
    String yail = AIJsonUtils.getStringField(json, "yail");

    YaBlocksEditor blocksEditor = AIEditorState.getCurrentBlocksEditor();
    String resultJson = blocksEditor.writeBlock(yail);

    JSONObject result = JSONParser.parseStrict(resultJson).isObject();
    JSONValue successVal = result.get("success");
    boolean success = successVal != null && successVal.isBoolean() != null
        && successVal.isBoolean().booleanValue();
    if (!success) {
      String error = AIJsonUtils.getStringField(result, "error");
      throw new RuntimeException(
          "WRITE_BLOCK failed: " + (error != null ? error : "unknown error"));
    }
  }

  static void executeDeleteBlock(JSONObject json) {
    String block = AIJsonUtils.getStringField(json, "block");

    YaBlocksEditor blocksEditor = AIEditorState.getCurrentBlocksEditor();
    String resultJson = blocksEditor.deleteBlockByYailId(block);

    JSONObject result = JSONParser.parseStrict(resultJson).isObject();
    JSONValue successVal = result.get("success");
    boolean success = successVal != null && successVal.isBoolean() != null
        && successVal.isBoolean().booleanValue();
    if (!success) {
      String error = AIJsonUtils.getStringField(result, "error");
      throw new RuntimeException(
          "DELETE_BLOCK failed: " + (error != null ? error : "unknown error"));
    }
  }
}
