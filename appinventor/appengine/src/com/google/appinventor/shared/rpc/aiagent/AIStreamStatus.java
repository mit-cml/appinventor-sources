package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Status update returned by {@link AIAgentService#getRequestStatus}.
 * Contains incremental text deltas and status updates from the streaming LLM response.
 */
public class AIStreamStatus implements IsSerializable, Serializable {
  private static final long serialVersionUID = 1L;

  private String statusText;
  private String textDelta;
  private String thinkingDelta;
  private boolean done;
  private boolean resetStreaming;

  // AI agent configuration fields (populated on every status poll)
  private boolean debugEnabled;
  private String conversationId;
  private boolean orchestrationEnabled;

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

  public boolean isDebugEnabled() { return debugEnabled; }
  public void setDebugEnabled(boolean debugEnabled) { this.debugEnabled = debugEnabled; }
  public String getConversationId() { return conversationId; }
  public void setConversationId(String conversationId) { this.conversationId = conversationId; }
  public boolean isOrchestrationEnabled() { return orchestrationEnabled; }
  public void setOrchestrationEnabled(boolean enabled) { this.orchestrationEnabled = enabled; }
}
