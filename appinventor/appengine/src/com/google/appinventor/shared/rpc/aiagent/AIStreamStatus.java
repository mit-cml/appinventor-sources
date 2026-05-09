package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Status update returned by {@link AIAgentService#getRequestStatus}.
 * Contains incremental text deltas and per-request runtime state.
 *
 * <p>Static feature flags ({@code ai.agent.features.*}, {@code ai.agent.debug})
 * are <strong>not</strong> carried on this DTO. They flow through
 * {@code Config} (populated once at login by
 * {@code UserInfoServiceImpl.getSystemConfig}) and are read on the client via
 * {@code Ode.getSystemConfig()}.
 */
public class AIStreamStatus implements IsSerializable, Serializable {
  private static final long serialVersionUID = 1L;

  private String statusText;
  private String textDelta;
  private String thinkingDelta;
  private boolean done;
  private boolean resetStreaming;

  // Per-request runtime bits (populated on every status poll)
  private String conversationId;

  // Required no-arg constructor for GWT serialization
  public AIStreamStatus() {}

  public AIStreamStatus(String statusText, String textDelta,
      String thinkingDelta, boolean done, boolean resetStreaming) {
    this.statusText = statusText;
    this.textDelta = textDelta;
    this.thinkingDelta = thinkingDelta;
    this.done = done;
    this.resetStreaming = resetStreaming;
  }

  public String getStatusText() { return statusText; }
  public String getTextDelta() { return textDelta; }
  public String getThinkingDelta() { return thinkingDelta; }
  public boolean isDone() { return done; }
  public boolean isResetStreaming() { return resetStreaming; }

  public String getConversationId() { return conversationId; }
  public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
