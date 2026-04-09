// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.llm.ChatMessage;
import com.google.appinventor.server.aiagent.llm.RawToolCall;
import com.google.appinventor.server.storage.AIConversationState;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StoredData.MessageRole;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

/**
 * Manages AI conversation persistence: message storage, conversation state,
 * status updates, and structured content serialisation.
 */
public class ConversationManager {

  private final StorageIo storageIo;

  /**
   * Per-request sequence counter. Ensures unique ordering when multiple messages
   * are stored within the same millisecond (user + assistant in one request).
   * Reset to 0 at the start of each RPC method that stores messages.
   * ThreadLocal to avoid races when concurrent requests share this servlet.
   */
  private static final ThreadLocal<Integer> messageSequence =
      new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
          return 0;
        }
      };

  public ConversationManager(StorageIo storageIo) {
    this.storageIo = storageIo;
  }

  // ---------- Sequence ----------

  public void resetSequence() {
    messageSequence.set(0);
  }

  // ---------- Conversation state ----------

  public AIConversationState getConversation(long projectId) {
    return storageIo.getAIConversationState(projectId);
  }

  public void saveConversation(long projectId, AIConversationState state) {
    storageIo.saveAIConversationState(projectId, state);
  }

  public void clearConversation(long projectId) {
    AIConversationState conv = getConversation(projectId);
    storageIo.clearAIConversationState(projectId);
    storageIo.clearAIStreamBuffer(projectId);

    // Delete stored messages from Datastore
    if (conv != null) {
      storageIo.deleteAIConversationMessages(conv.getConversationId());
    }
  }

  /** Gets conversation state for a specific screen (child agent). */
  public AIConversationState getConversation(long projectId, String screenName) {
    return storageIo.getAIConversationState(projectId, screenName);
  }

  /** Saves conversation state for a specific screen (child agent). */
  public void saveConversation(long projectId, String screenName, AIConversationState state) {
    storageIo.saveAIConversationState(projectId, screenName, state);
  }

  /** Clears conversation state for a specific screen (child agent). */
  public void clearConversation(long projectId, String screenName) {
    storageIo.clearAIConversationState(projectId, screenName);
    storageIo.clearAIStreamBuffer(projectId, screenName);
    // No message deletion — child conversations are ephemeral (no Datastore messages)
  }

  // ---------- Message persistence ----------

  public void storeMessage(String conversationId, MessageRole role, String text,
      boolean display) {
    storeMessage(conversationId, role, text, null, display);
  }

  public void storeMessage(String conversationId, MessageRole role, String text,
      String structuredContent, boolean display) {
    long now = System.currentTimeMillis();
    int seq = messageSequence.get();
    messageSequence.set(seq + 1);
    storageIo.storeAIConversationMessage(conversationId, now, seq, role, text,
        structuredContent, display);
    storageIo.cleanupConversationMessages();
  }

  public List<ChatMessage> loadConversation(String conversationId) {
    return storageIo.loadAIConversationMessages(conversationId);
  }

  // ---------- Operation summaries (static) ----------

  /**
   * Build a brief summary of operations when the LLM returns no text.
   */
  public static String summarizeOperations(List<AIOperation> operations) {
    StringBuilder sb = new StringBuilder();
    for (AIOperation op : operations) {
      try {
        JSONObject payload = new JSONObject(op.getPayload());
        switch (op.getType()) {
          case ADD_COMPONENT:
            sb.append("Added ").append(payload.optString("component_type"))
                .append(" '").append(payload.optString("name")).append("'\n");
            break;
          case DELETE_COMPONENT:
            sb.append("Deleted component '").append(payload.optString("name")).append("'\n");
            break;
          case SET_PROPERTY:
            sb.append("Set ").append(payload.optString("component_name"))
                .append(".").append(payload.optString("property_name"))
                .append(" to ").append(payload.optString("value")).append("\n");
            break;
          case RENAME_COMPONENT:
            sb.append("Renamed '").append(payload.optString("old_name"))
                .append("' to '").append(payload.optString("new_name")).append("'\n");
            break;
          case WRITE_BLOCK:
            String yail = payload.optString("yail", "");
            sb.append("Wrote block: ").append(summarizeYailHead(yail)).append("\n");
            break;
          case DELETE_BLOCK:
            sb.append("Deleted block: ").append(payload.optString("block")).append("\n");
            break;
          case CREATE_SCREEN:
            sb.append("Created screen '").append(payload.optString("screen_name")).append("'\n");
            break;
          case DELETE_SCREEN:
            sb.append("Deleted screen '").append(payload.optString("screen_name")).append("'\n");
            break;
          case SWITCH_SCREEN:
            sb.append("Switched to screen '").append(payload.optString("screen_name"))
                .append("'\n");
            break;
          case SET_PROJECT_PROP:
            sb.append("Set project property ").append(payload.optString("property"))
                .append(" to ").append(payload.optString("value")).append("\n");
            break;
          case TOGGLE_EDITOR:
            sb.append("Switched to ").append(payload.optString("view")).append(" view\n");
            break;
          default:
            sb.append(op.getType().name()).append("\n");
            break;
        }
      } catch (Exception e) {
        sb.append(op.getType().name()).append("\n");
      }
    }
    return sb.toString().trim();
  }

  /**
   * Extract a human-readable summary from the head of a YAIL S-expression.
   * E.g., "(define-event Button1 Click ...)" -> "define-event Button1 Click"
   */
  static String summarizeYailHead(String yail) {
    if (yail == null || yail.isEmpty()) {
      return "(unknown)";
    }
    // Strip leading whitespace and opening parens
    String trimmed = yail.trim();
    if (trimmed.startsWith("(")) {
      trimmed = trimmed.substring(1).trim();
    }
    // Take the first few space-separated tokens
    String[] tokens = trimmed.split("\\s+", 5);
    int tokenCount = Math.min(tokens.length, 4);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tokenCount; i++) {
      if (i > 0) sb.append(" ");
      String token = tokens[i].replaceAll("[()]", "");
      if (token.isEmpty()) continue;
      sb.append(token);
    }
    return sb.toString();
  }

  // ---------- Structured content helpers ----------

  /**
   * Builds a provider-agnostic JSON pair from an LLM response that includes
   * tool calls.  Returns a two-element array:
   * <ul>
   *   <li>[0] = assistant structured content (text + tool_use parts)</li>
   *   <li>[1] = tool_result structured content (matching result parts)</li>
   * </ul>
   *
   * @param text      the assistant's text reply (may be null or empty)
   * @param toolCalls the raw tool calls from the LLM response
   * @return [assistantContent, toolResultContent] JSON strings
   */
  public static String[] buildStructuredContentPair(String text,
      List<RawToolCall> toolCalls, List<ToolCallStatus> statuses) {
    JSONArray assistantParts = new JSONArray();
    JSONArray resultParts = new JSONArray();

    if (text != null && !text.isEmpty()) {
      assistantParts.put(new JSONObject()
          .put("type", "text")
          .put("text", text));
    }
    for (int i = 0; i < toolCalls.size(); i++) {
      RawToolCall tc = toolCalls.get(i);
      String toolUseId = "tc_" + UUID.randomUUID().toString().substring(0, 8);

      // Assistant's tool_use block (always included -- the LLM did emit this call)
      JSONObject usePart = new JSONObject();
      usePart.put("type", "tool_use");
      usePart.put("id", toolUseId);
      usePart.put("name", tc.getName());
      try {
        usePart.put("input", new JSONObject(tc.getArgumentsJson()));
      } catch (Exception e) {
        usePart.put("input", new JSONObject());
      }
      assistantParts.put(usePart);

      // Tool result: use per-call status when available.
      // ACCEPTED operations get a neutral "Pending client execution." because
      // at history-storage time we don't yet know if the client will succeed.
      // REJECTED operations get the error so the LLM sees what went wrong.
      String resultContent = "Pending client execution.";
      if (statuses != null && i < statuses.size()) {
        ToolCallStatus status = statuses.get(i);
        if (status != null && status.getOutcome() != ToolCallOutcome.ACCEPTED) {
          resultContent = "REJECTED: " + status.getErrorMessage();
        }
      }

      JSONObject resultPart = new JSONObject();
      resultPart.put("type", "tool_result");
      resultPart.put("tool_use_id", toolUseId);
      resultPart.put("tool_name", tc.getName());
      resultPart.put("content", resultContent);
      resultParts.put(resultPart);
    }
    return new String[] { assistantParts.toString(), resultParts.toString() };
  }
}
