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
 * <p>Conversation state is managed server-side (one conversation per project,
 * lazy-created on first message, 24h TTL). No conversationId from client.</p>
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
   * Clear the current conversation for a project.
   * Deletes the Memcache entry and all Datastore ConversationMessageData
   * entities. The next processRequest() will start fresh.
   *
   * @param projectId the project whose conversation should be cleared
   */
  void clearConversation(long projectId);

  /**
   * Load the conversation history for a project. Returns text-only messages
   * (no operations) for display in the chat dialog. Used after page reload
   * to restore the chat UI.
   *
   * @param projectId the project whose history to load
   * @return list of conversation messages, empty if no conversation exists
   */
  List<AIConversationMessage> getConversationHistory(long projectId);

  /**
   * Poll for progress of a running processRequest() call.
   * Lightweight -- reads from Memcache only, no Datastore access.
   *
   * @param projectId the project to check status for
   * @return current status message, or null/empty if no request is in progress
   */
  String getRequestStatus(long projectId);

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
