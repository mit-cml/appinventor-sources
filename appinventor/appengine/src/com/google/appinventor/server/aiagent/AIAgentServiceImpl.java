// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thin servlet layer for the AI Agent RPC service.
 *
 * <p>Handles authentication, input validation, and rate limiting, then
 * delegates to {@link AIAgentEngine} for all business logic.
 */
public class AIAgentServiceImpl extends OdeRemoteServiceServlet
    implements AIAgentService {

  private static final int MAX_MESSAGE_LENGTH = 2000;
  private static final long RATE_WINDOW_MS = 60_000; // 1 minute

  private static final Flag<Integer> RATE_LIMIT_FLAG =
      Flag.createFlag("ai.agent.rate.limit", 10);

  private final transient StorageIo storageIo;
  private final transient AIAgentEngine engine;

  // Rate limiting: userId -> list of request timestamps
  private static final ConcurrentHashMap<String, List<Long>> rateLimitMap =
      new ConcurrentHashMap<>();

  public AIAgentServiceImpl() {
    StorageIo storageIo = StorageIoInstanceHolder.getInstance();
    this.storageIo = storageIo;
    this.engine = new AIAgentEngine(storageIo);
  }

  // ---------- RPC methods ----------

  @Override
  public AIAgentResponse processRequest(AIAgentRequest request) {
    long projectId = request.getProjectId();
    String userMessage = request.getUserMessage();

    // Input validation
    if (userMessage == null || userMessage.trim().isEmpty()) {
      return AIAgentEngine.errorResponse("Message cannot be empty.");
    }
    userMessage = sanitize(userMessage);
    if (userMessage.length() > MAX_MESSAGE_LENGTH) {
      return AIAgentEngine.errorResponse(
          "Message too long (max " + MAX_MESSAGE_LENGTH + " characters).");
    }

    RequestContext ctx = validateRequest(projectId);
    if (ctx.error != null) return ctx.error;

    // Rate limiting
    if (!checkRateLimit(ctx.userId)) {
      return AIAgentEngine.errorResponse(
          "Rate limit exceeded. Please wait before sending another message.");
    }

    return engine.processRequest(ctx.userId, projectId, request.getScreenName(),
        userMessage, request.getBlocksYail(), request.getCurrentView(), ctx.mode,
        request.getScreenComponentsJson(), request.getProjectSnapshot());
  }

  @Override
  public AIAgentResponse continueRequest(AIAgentRequest request) {
    long projectId = request.getProjectId();
    RequestContext ctx = validateRequest(projectId);
    if (ctx.error != null) return ctx.error;

    return engine.continueRequest(ctx.userId, projectId, request.getScreenName(),
        request.getBlocksYail(), request.getCurrentView(), ctx.mode,
        request.getScreenComponentsJson(), request.getProjectSnapshot());
  }

  @Override
  public AIAgentResponse reportExecutionErrors(AIAgentRequest request, List<String> errors) {
    long projectId = request.getProjectId();
    RequestContext ctx = validateRequest(projectId);
    if (ctx.error != null) return ctx.error;

    if (errors == null || errors.isEmpty()) {
      return AIAgentEngine.errorResponse("No errors to report.");
    }

    return engine.reportExecutionErrors(ctx.userId, projectId, request.getScreenName(),
        errors, request.getBlocksYail(), request.getCurrentView(), ctx.mode,
        request.getScreenComponentsJson(), request.getProjectSnapshot());
  }

  @Override
  public void clearConversation(long projectId) {
    String userId = userInfoProvider.getUserId();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      throw new SecurityException("You do not have access to this project.");
    }
    engine.clearConversation(projectId);
  }

  @Override
  public List<AIConversationMessage> getConversationHistory(long projectId) {
    String userId = userInfoProvider.getUserId();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return Collections.emptyList();
    }
    return engine.getConversationHistory(projectId);
  }

  @Override
  public String getRequestStatus(long projectId) {
    return engine.getRequestStatus(projectId);
  }

  // ---------- Request validation ----------

  private static class RequestContext {
    final String userId;
    final String mode;
    final AIAgentResponse error;

    private RequestContext(String userId, String mode, AIAgentResponse error) {
      this.userId = userId;
      this.mode = mode;
      this.error = error;
    }

    static RequestContext ok(String userId, String mode) {
      return new RequestContext(userId, mode, null);
    }

    static RequestContext fail(String message) {
      return new RequestContext(null, null, AIAgentEngine.errorResponse(message));
    }
  }

  private RequestContext validateRequest(long projectId) {
    String userId = userInfoProvider.getUserId();
    engine.getConversationManager().resetSequence();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return RequestContext.fail("You do not have access to this project.");
    }
    String mode = engine.getProjectAIMode(userId, projectId);
    if ("Off".equals(mode)) {
      return RequestContext.fail("AI agent is disabled for this project.");
    }
    return RequestContext.ok(userId, mode);
  }

  // ---------- Rate limiting ----------

  private boolean checkRateLimit(String userId) {
    int limit = RATE_LIMIT_FLAG.get();
    long now = System.currentTimeMillis();

    rateLimitMap.putIfAbsent(userId, Collections.synchronizedList(new ArrayList<Long>()));
    List<Long> timestamps = rateLimitMap.get(userId);

    synchronized (timestamps) {
      // Remove old timestamps
      while (!timestamps.isEmpty() && timestamps.get(0) < now - RATE_WINDOW_MS) {
        timestamps.remove(0);
      }
      if (timestamps.size() >= limit) {
        return false;
      }
      timestamps.add(now);
      return true;
    }
  }

  // ---------- Input sanitization ----------

  private String sanitize(String input) {
    // Strip control characters except newline and tab
    StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c >= 32 || c == '\n' || c == '\t') {
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }
}
