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
  private String locale;
  private String languageDisplayName;
  private int retryAttempt;
  private int totalTools;
  private boolean platformMessage;
  private boolean orchestrationMode;
  private String targetScreen;
  private boolean planExecuteMode;
  private boolean executionPhase;

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

  /**
   * Returns the user's interface locale code (e.g., "es_ES", "pt_BR", "ja").
   */
  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  /**
   * Returns the native display name of the user's interface language
   * (e.g., "Español", "日本語").
   */
  public String getLanguageDisplayName() {
    return languageDisplayName;
  }

  public void setLanguageDisplayName(String languageDisplayName) {
    this.languageDisplayName = languageDisplayName;
  }

  /**
   * Returns the retry attempt number (1-based) for error retry requests.
   * Returns 0 for non-retry requests.
   */
  public int getRetryAttempt() {
    return retryAttempt;
  }

  public void setRetryAttempt(int retryAttempt) {
    this.retryAttempt = retryAttempt;
  }

  /**
   * Returns the original total number of tools in the batch before retries.
   * On subsequent retries the results list shrinks (only failed/skipped ops
   * are re-emitted), so this preserves the original denominator for status
   * messages like "3 out of 5 tools failed". Returns 0 when not set.
   */
  public int getTotalTools() {
    return totalTools;
  }

  public void setTotalTools(int totalTools) {
    this.totalTools = totalTools;
  }

  /**
   * Returns true if this message is a system-generated platform notification
   * (e.g. rejection feedback) rather than direct user input. When true, the
   * server wraps the message for the LLM but stores the raw text in history.
   */
  public boolean isPlatformMessage() {
    return platformMessage;
  }

  public void setPlatformMessage(boolean platformMessage) {
    this.platformMessage = platformMessage;
  }

  public boolean isOrchestrationMode() { return orchestrationMode; }
  public void setOrchestrationMode(boolean orchestrationMode) { this.orchestrationMode = orchestrationMode; }
  public String getTargetScreen() { return targetScreen; }
  public void setTargetScreen(String targetScreen) { this.targetScreen = targetScreen; }
  public boolean isPlanExecuteMode() { return planExecuteMode; }
  public void setPlanExecuteMode(boolean planExecuteMode) { this.planExecuteMode = planExecuteMode; }
  public boolean isExecutionPhase() { return executionPhase; }
  public void setExecutionPhase(boolean executionPhase) { this.executionPhase = executionPhase; }

  /**
   * Wraps a system-generated message in {@code <platform_message>} tags so
   * the LLM knows it is automated platform context, not user input.
   */
  public static String wrapPlatformMessage(String content) {
    return "<system>\n" + content + "\n</system>";
  }
}
