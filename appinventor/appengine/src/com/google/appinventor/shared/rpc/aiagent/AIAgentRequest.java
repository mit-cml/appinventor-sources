// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Request DTO sent from the client to the AI agent service.
 * Conversation state is managed entirely server-side (Memcache keyed
 * by projectId), so no conversationId is needed from the client.
 */
public class AIAgentRequest implements IsSerializable, Serializable {

  private String userMessage;
  private long projectId;
  private String screenName;
  private String blocksYail;

  /**
   * No-arg constructor required for GWT serialization.
   */
  public AIAgentRequest() {
  }

  /**
   * Creates a new AI agent request.
   *
   * @param userMessage the natural language request from the user
   * @param projectId the current project ID
   * @param screenName the currently visible screen name
   */
  public AIAgentRequest(String userMessage, long projectId, String screenName) {
    this.userMessage = userMessage;
    this.projectId = projectId;
    this.screenName = screenName;
  }

  /**
   * Creates a new AI agent request with blocks YAIL.
   *
   * @param userMessage the natural language request from the user
   * @param projectId the current project ID
   * @param screenName the currently visible screen name
   * @param blocksYail YAIL representation of the current screen's blocks
   */
  public AIAgentRequest(String userMessage, long projectId, String screenName,
      String blocksYail) {
    this.userMessage = userMessage;
    this.projectId = projectId;
    this.screenName = screenName;
    this.blocksYail = blocksYail;
  }

  public String getUserMessage() {
    return userMessage;
  }

  public void setUserMessage(String userMessage) {
    this.userMessage = userMessage;
  }

  public long getProjectId() {
    return projectId;
  }

  public void setProjectId(long projectId) {
    this.projectId = projectId;
  }

  public String getScreenName() {
    return screenName;
  }

  public void setScreenName(String screenName) {
    this.screenName = screenName;
  }

  /**
   * Returns the YAIL representation of the current screen's blocks.
   * Generated client-side by the Blockly YAIL generators.
   */
  public String getBlocksYail() {
    return blocksYail;
  }

  public void setBlocksYail(String blocksYail) {
    this.blocksYail = blocksYail;
  }
}
