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
  private String currentView;
  private String screenComponentsJson;
  private String projectSnapshot;
  private String blockWarnings;

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

  /**
   * Creates a new AI agent request with blocks YAIL and current editor view.
   *
   * @param userMessage the natural language request from the user
   * @param projectId the current project ID
   * @param screenName the currently visible screen name
   * @param blocksYail YAIL representation of the current screen's blocks
   * @param currentView the active editor view ("Designer" or "Blocks")
   */
  public AIAgentRequest(String userMessage, long projectId, String screenName,
      String blocksYail, String currentView) {
    this.userMessage = userMessage;
    this.projectId = projectId;
    this.screenName = screenName;
    this.blocksYail = blocksYail;
    this.currentView = currentView;
  }

  /**
   * Creates a new AI agent request with all fields including client-side context.
   *
   * @param userMessage the natural language request from the user
   * @param projectId the current project ID
   * @param screenName the currently visible screen name
   * @param blocksYail YAIL representation of the current screen's blocks
   * @param currentView the active editor view ("Designer" or "Blocks")
   * @param screenComponentsJson live component tree JSON from the designer
   * @param projectSnapshot JSON with project metadata from the client
   */
  public AIAgentRequest(String userMessage, long projectId, String screenName,
      String blocksYail, String currentView,
      String screenComponentsJson, String projectSnapshot) {
    this.userMessage = userMessage;
    this.projectId = projectId;
    this.screenName = screenName;
    this.blocksYail = blocksYail;
    this.currentView = currentView;
    this.screenComponentsJson = screenComponentsJson;
    this.projectSnapshot = projectSnapshot;
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

  /**
   * Returns the active editor view ("Designer" or "Blocks").
   * Defaults to "Designer" when not set (backward compatibility).
   */
  public String getCurrentView() {
    return currentView != null ? currentView : "Designer";
  }

  public void setCurrentView(String currentView) {
    this.currentView = currentView;
  }

  /**
   * Returns the live component tree JSON from the current screen's designer.
   * This is the inner Properties object from the SCM format.
   */
  public String getScreenComponentsJson() {
    return screenComponentsJson;
  }

  public void setScreenComponentsJson(String screenComponentsJson) {
    this.screenComponentsJson = screenComponentsJson;
  }

  /**
   * Returns the project metadata snapshot JSON built client-side.
   */
  public String getProjectSnapshot() {
    return projectSnapshot;
  }

  public void setProjectSnapshot(String projectSnapshot) {
    this.projectSnapshot = projectSnapshot;
  }

  /**
   * Returns the block warnings/errors JSON collected from the Blockly WarningHandler.
   */
  public String getBlockWarnings() {
    return blockWarnings;
  }

  public void setBlockWarnings(String blockWarnings) {
    this.blockWarnings = blockWarnings;
  }
}
