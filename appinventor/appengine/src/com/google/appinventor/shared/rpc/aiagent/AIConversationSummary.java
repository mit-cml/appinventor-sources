// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Lightweight metadata for an AI Agent conversation. Used by the list view.
 */
public class AIConversationSummary implements IsSerializable, Serializable {
  private String conversationId;
  private String title;
  private long createdAt;
  private long updatedAt;

  public AIConversationSummary() {}

  public AIConversationSummary(String conversationId, String title,
      long createdAt, long updatedAt) {
    this.conversationId = conversationId;
    this.title = title;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String v) {
    this.conversationId = v;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String v) {
    this.title = v;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long v) {
    this.createdAt = v;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long v) {
    this.updatedAt = v;
  }
}
