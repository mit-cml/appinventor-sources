// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import java.io.Serializable;

/**
 * Memcache-backed state for an AI agent conversation, keyed by project ID.
 *
 * <p>Holds the provider name, the conversation UUID (which links to
 * {@link StoredData.ConversationMessageData} rows in the Datastore), and an
 * opaque provider reference that stateful providers use to resume the LLM
 * session (continuation state for multi-step responses).
 */
public class AIConversationState implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String providerName;
  private final String conversationId;
  private final String providerRef;

  public AIConversationState(String providerName, String conversationId,
      String providerRef) {
    this.providerName = providerName;
    this.conversationId = conversationId;
    this.providerRef = providerRef;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getConversationId() {
    return conversationId;
  }

  public String getProviderRef() {
    return providerRef;
  }
}
