// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO returned from the AI agent service to the client.
 * Contains the AI's natural language response, a list of structured
 * operations to apply, and any validation errors.
 */
public class AIAgentResponse implements IsSerializable, Serializable {

  private String aiMessage;
  private List<AIOperation> operations;
  private boolean isNewConversation;
  private boolean hasMore;
  private List<String> errors;

  /**
   * No-arg constructor required for GWT serialization.
   */
  public AIAgentResponse() {
    this.operations = new ArrayList<>();
    this.errors = new ArrayList<>();
  }

  /**
   * Creates a new AI agent response.
   *
   * @param aiMessage natural language explanation from the AI
   * @param operations structured operations to apply
   * @param isNewConversation true when this message started a new conversation
   * @param errors validation errors (non-empty means zero operations should be applied)
   */
  public AIAgentResponse(String aiMessage, List<AIOperation> operations,
      boolean isNewConversation, List<String> errors) {
    this.aiMessage = aiMessage;
    this.operations = operations != null ? operations : new ArrayList<>();
    this.isNewConversation = isNewConversation;
    this.errors = errors != null ? errors : new ArrayList<>();
  }

  public String getAiMessage() {
    return aiMessage;
  }

  public void setAiMessage(String aiMessage) {
    this.aiMessage = aiMessage;
  }

  public List<AIOperation> getOperations() {
    return operations;
  }

  public void setOperations(List<AIOperation> operations) {
    this.operations = operations;
  }

  public boolean isNewConversation() {
    return isNewConversation;
  }

  public void setNewConversation(boolean newConversation) {
    this.isNewConversation = newConversation;
  }

  public boolean hasMore() {
    return hasMore;
  }

  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
}
