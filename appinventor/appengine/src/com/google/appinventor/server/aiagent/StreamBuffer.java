package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;

import java.util.List;

/**
 * Per-request buffer for streaming LLM text tokens to the client via Memcache.
 * Delegates all storage to {@link StorageIo} stream buffer methods.
 *
 * <p>Chunks are prefixed: "t:" for text deltas, "s:" for status updates.
 */
public class StreamBuffer {
  private final StorageIo storageIo;
  private final long projectId;

  public StreamBuffer(StorageIo storageIo, long projectId) {
    this.storageIo = storageIo;
    this.projectId = projectId;
  }

  /** Initialize or reset the buffer. Call once at the start of an LLM request. */
  public void init() {
    storageIo.initAIStreamBuffer(projectId);
    storageIo.clearAIStreamCancelled(projectId);
  }

  /** Append a text delta from the LLM. */
  public void appendText(String text) {
    if (text != null && !text.isEmpty()) {
      storageIo.appendAIStreamChunk(projectId, "t:" + text);
    }
  }

  /** Append a thinking/reasoning delta from the LLM. */
  public void appendThinking(String text) {
    if (text != null && !text.isEmpty()) {
      storageIo.appendAIStreamChunk(projectId, "k:" + text);
    }
  }

  /** Append a status update (e.g., "Building context..."). */
  public void appendStatus(String status) {
    if (status != null && !status.isEmpty()) {
      storageIo.appendAIStreamChunk(projectId, "s:" + status);
    }
  }

  /** Emit a reset signal that tells the client to clear its streaming
   * bubble (accumulated text, thinking, and typing indicator).
   * Used before a narration retry so the retry streams into a clean slate. */
  public void resetStreaming() {
    storageIo.appendAIStreamChunk(projectId, "r:");
  }

  /** Mark the stream as done (LLM response fully received). */
  public void markDone() {
    storageIo.markAIStreamDone(projectId);
  }

  /**
   * Unchecked exception thrown when a cancellation is detected.
   * Providers throw this from SSE loops; the engine catches it
   * to return an empty response. Extends RuntimeException so it
   * propagates without modifying the LLMProvider interface or
   * any provider method signatures.
   */
  public static class CancelledException extends RuntimeException {
    public CancelledException() {
      super("Request cancelled by user");
    }
  }

  /** Marks this request as cancelled in Memcache. */
  public void setCancelled() {
    storageIo.setAIStreamCancelled(projectId);
  }

  /** Returns true if this request has been cancelled. */
  public boolean isCancelled() {
    return storageIo.isAIStreamCancelled(projectId);
  }

  /**
   * Throws {@link CancelledException} if this request has been cancelled.
   * Call this at check points in long-running operations.
   */
  public void checkCancelled() {
    if (isCancelled()) {
      throw new CancelledException();
    }
  }

  /** Clean up all buffer keys. Call after the RPC response is sent. */
  public void clear() {
    storageIo.clearAIStreamBuffer(projectId);
  }

  /**
   * Consume all pending chunks and return them as an {@link AIStreamStatus}.
   * Text deltas are concatenated into {@code textDelta}. The last status
   * update becomes {@code statusText}. Checks the done flag.
   */
  public AIStreamStatus consume() {
    List<String> chunks = storageIo.consumeAIStreamChunks(projectId);
    boolean done = storageIo.isAIStreamDone(projectId);

    StringBuilder textBuilder = null;
    StringBuilder thinkingBuilder = null;
    String lastStatus = null;
    boolean reset = false;

    for (String chunk : chunks) {
      if (chunk.startsWith("t:")) {
        if (textBuilder == null) {
          textBuilder = new StringBuilder();
        }
        textBuilder.append(chunk.substring(2));
      } else if (chunk.startsWith("k:")) {
        if (thinkingBuilder == null) {
          thinkingBuilder = new StringBuilder();
        }
        thinkingBuilder.append(chunk.substring(2));
      } else if (chunk.startsWith("r:")) {
        reset = true;
        textBuilder = null;
        thinkingBuilder = null;
      } else if (chunk.startsWith("s:")) {
        lastStatus = chunk.substring(2);
      }
    }

    return new AIStreamStatus(
        lastStatus,
        textBuilder != null ? textBuilder.toString() : null,
        thinkingBuilder != null ? thinkingBuilder.toString() : null,
        done,
        reset
    );
  }
}
