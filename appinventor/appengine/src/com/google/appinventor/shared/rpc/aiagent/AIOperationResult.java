// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Structured result for a single AI operation after client-side
 * validation or execution. Replaces the old string-prefix encoding
 * ({@code SUCCEEDED:}/{@code FAILED:}/{@code SKIPPED:}) that was
 * sent through {@code reportExecutionErrors}.
 */
public class AIOperationResult implements IsSerializable, Serializable {

  /**
   * Outcome of an individual operation on the client side.
   */
  public enum Status implements IsSerializable {
    /** Operation was applied successfully (do NOT re-emit). */
    SUCCEEDED,
    /** Operation failed during validation or execution (fix and retry). */
    FAILED,
    /** Operation was skipped because an earlier operation failed. */
    SKIPPED
  }

  private Status status;
  private String summary;
  private String errorDetail;

  /** No-arg constructor required for GWT serialization. */
  public AIOperationResult() {
  }

  public AIOperationResult(Status status, String summary, String errorDetail) {
    this.status = status;
    this.summary = summary;
    this.errorDetail = errorDetail;
  }

  public static AIOperationResult succeeded(String summary) {
    return new AIOperationResult(Status.SUCCEEDED, summary, null);
  }

  public static AIOperationResult failed(String summary, String errorDetail) {
    return new AIOperationResult(Status.FAILED, summary, errorDetail);
  }

  public static AIOperationResult skipped(String summary) {
    return new AIOperationResult(Status.SKIPPED, summary, null);
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getErrorDetail() {
    return errorDetail;
  }

  public void setErrorDetail(String errorDetail) {
    this.errorDetail = errorDetail;
  }
}
