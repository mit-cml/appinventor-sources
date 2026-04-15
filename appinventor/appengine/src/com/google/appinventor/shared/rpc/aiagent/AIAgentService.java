// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;

/**
 * GWT-RPC service interface for the AI agent.
 *
 * <p>Conversations are addressed by {@code conversationId} (UUID). A
 * conversation is minted server-side on the first {@code processRequest}
 * call when the client passes a blank id, and the server echoes the new
 * id back on the response so the client can track it. Conversations are
 * retained until explicitly deleted or until the owning project is
 * deleted.</p>
 */
@RemoteServiceRelativePath(ServerLayout.AI_AGENT_SERVICE)
public interface AIAgentService extends RemoteService {

  /**
   * Send a user message to the AI agent.
   *
   * @param request the request containing user message, project ID, and screen name
   * @return the AI agent response with message, operations, and any errors
   */
  AIAgentResponse processRequest(AIAgentRequest request);

  /**
   * Continue a multi-step AI response. Called after the client has applied
   * operations from a response where {@link AIAgentResponse#hasMore()} was
   * {@code true}. Sends synthetic tool results to the LLM and returns the
   * next batch of operations.
   *
   * @param request the request containing project ID, screen name, blocks YAIL,
   *                current view, and client-side context snapshots
   * @return the next batch of AI operations (may also have hasMore=true)
   */
  AIAgentResponse continueRequest(AIAgentRequest request);

  /**
   * List conversations owned by the current user on the given project,
   * most recently updated first.
   *
   * @param projectId the project whose conversations to list
   * @return list of conversation summaries
   */
  List<AIConversationSummary> listConversations(long projectId);

  /**
   * Rename a conversation. Trims whitespace; empty/null clears the title
   * (the client will fall back to a date-based label).
   *
   * @param conversationId the conversation to rename
   * @param newTitle       the new title, or null/empty to clear
   * @return the updated summary
   */
  AIConversationSummary renameConversation(String conversationId, String newTitle);

  /**
   * Delete a conversation: metadata row, all messages, and the Memcache
   * state entry.
   *
   * @param conversationId the conversation to delete
   */
  void deleteConversation(String conversationId);

  /**
   * Load the display-only message history for a specific conversation.
   *
   * @param conversationId the conversation whose history to load
   * @return list of conversation messages; empty if the conversation does
   *         not exist or the user does not own it
   */
  List<AIConversationMessage> getConversationHistory(String conversationId);

  /**
   * Poll for progress of a running processRequest() call.
   * Lightweight -- reads from Memcache only, no Datastore access.
   *
   * @param projectId the project to check status for
   * @return current stream status with text deltas and done flag
   */
  AIStreamStatus getRequestStatus(long projectId);

  /**
   * Poll for progress of a running processRequest() for a specific screen
   * (orchestration child agent).
   *
   * @param projectId    the project to check status for
   * @param targetScreen the target screen name (null for parent conversation)
   * @return current stream status with text deltas and done flag
   */
  AIStreamStatus getRequestStatus(long projectId, String targetScreen);

  /**
   * Cancel an in-flight AI request for a project. Sets a cancellation flag
   * in Memcache that the LLM provider checks during streaming to abort early.
   * Best-effort: the request may complete before the flag is checked.
   *
   * @param projectId the project whose request should be cancelled
   */
  void cancelRequest(long projectId);

  /**
   * Cancel an in-flight AI request for a specific screen (orchestration
   * child agent).
   *
   * @param projectId    the project whose request should be cancelled
   * @param targetScreen the target screen name (null for parent conversation)
   */
  void cancelRequest(long projectId, String targetScreen);

  /**
   * Report client-side execution or validation results to the server so
   * the LLM can be retried with structured feedback.
   *
   * <p>Called by the client after {@code AIOperationExecutor} encounters
   * failures when applying operations, or after client-side YAIL
   * validation rejects block operations before execution.
   *
   * @param request the request containing project ID, screen name, blocks YAIL,
   *                current view, and client-side context snapshots
   * @param results per-operation results (succeeded, failed, or skipped)
   * @return an updated AI response with corrected operations, or errors
   */
  AIAgentResponse reportExecutionErrors(AIAgentRequest request,
      List<AIOperationResult> results);
}
