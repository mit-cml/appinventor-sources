// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.validator;

import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.gwt.json.client.JSONObject;

/**
 * Validates block AI operations: WRITE_BLOCK, DELETE_BLOCK.
 */
final class BlockOperationValidator {

  private BlockOperationValidator() {}

  static String validateWriteBlock(JSONObject json) {
    String yail = AIJsonUtils.getStringField(json, "yail");
    if (yail == null || yail.isEmpty()) {
      return "WRITE_BLOCK: missing 'yail' field";
    }
    if (AIEditorState.getCurrentBlocksEditor() == null) {
      return "WRITE_BLOCK: no blocks editor available for current screen";
    }
    return null;
  }

  static String validateDeleteBlock(JSONObject json) {
    String block = AIJsonUtils.getStringField(json, "block");
    if (block == null || block.isEmpty()) {
      return "DELETE_BLOCK: missing 'block' field";
    }
    if (AIEditorState.getCurrentBlocksEditor() == null) {
      return "DELETE_BLOCK: no blocks editor available for current screen";
    }
    return null;
  }
}
