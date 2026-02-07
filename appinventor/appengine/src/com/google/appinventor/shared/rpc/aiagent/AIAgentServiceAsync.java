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
   * @see AIAgentService#clearConversation(long)
   */
  void clearConversation(long projectId, AsyncCallback<Void> callback);

  /**
   * @see AIAgentService#getConversationHistory(long)
   */
  void getConversationHistory(long projectId,
      AsyncCallback<List<AIConversationMessage>> callback);

  /**
   * @see AIAgentService#getRequestStatus(long)
   */
  void getRequestStatus(long projectId, AsyncCallback<String> callback);
}
