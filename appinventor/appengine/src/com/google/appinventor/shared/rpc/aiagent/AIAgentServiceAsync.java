// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

/**
 * Async interface for {@link AIAgentService}. Required by GWT-RPC.
 * All declarations mirror {@link AIAgentService}.
 *
 * @see AIAgentService
 */
public interface AIAgentServiceAsync {

  /**
   * @see AIAgentService#processRequest(AIAgentRequest)
   */
  void processRequest(AIAgentRequest request, AsyncCallback<AIAgentResponse> callback);

  /**
   * @see AIAgentService#continueRequest(AIAgentRequest)
   */
  void continueRequest(AIAgentRequest request, AsyncCallback<AIAgentResponse> callback);

  /**
   * @see AIAgentService#listConversations(long)
   */
  void listConversations(long projectId,
      AsyncCallback<List<AIConversationSummary>> callback);

  /**
   * @see AIAgentService#renameConversation(String, String)
   */
  void renameConversation(String conversationId, String newTitle,
      AsyncCallback<AIConversationSummary> callback);

  /**
   * @see AIAgentService#deleteConversation(String)
   */
  void deleteConversation(String conversationId, AsyncCallback<Void> callback);

  /**
   * @see AIAgentService#getConversationHistory(String)
   */
  void getConversationHistory(String conversationId,
      AsyncCallback<List<AIConversationMessage>> callback);

  /**
   * @see AIAgentService#getRequestStatus(long)
   */
  void getRequestStatus(long projectId, AsyncCallback<AIStreamStatus> callback);

  /**
   * @see AIAgentService#getRequestStatus(long, String)
   */
  void getRequestStatus(long projectId, String targetScreen,
      AsyncCallback<AIStreamStatus> callback);

  /**
   * @see AIAgentService#cancelRequest(long)
   */
  void cancelRequest(long projectId, AsyncCallback<Void> callback);

  /**
   * @see AIAgentService#cancelRequest(long, String)
   */
  void cancelRequest(long projectId, String targetScreen, AsyncCallback<Void> callback);

  /**
   * @see AIAgentService#reportExecutionErrors(AIAgentRequest, List)
   */
  void reportExecutionErrors(AIAgentRequest request, List<AIOperationResult> results,
      AsyncCallback<AIAgentResponse> callback);
}
