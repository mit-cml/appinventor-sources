// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.AIConversationState;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData.ConversationData;
import com.google.appinventor.server.storage.StoredData.MessageRole;

/**
 * Tests for {@link AIAgentEngine} routing / rehydration behaviour that does
 * not require a live LLM provider. The engine delegates real LLM work to
 * {@link com.google.appinventor.server.aiagent.llm.LLMProviderRegistry}
 * which is a static factory configured via system-property flags; that
 * makes full end-to-end engine tests infeasible without either live
 * credentials or a mock HTTP seam we don't have yet. These tests exercise
 * the conversation-resolution helpers and the storage-side effects that
 * the Phase 2 rehydration path depends on.
 */
public class AIAgentEngineTest extends LocalDatastoreTestCase {

  private static final String USER_A = "user-A";
  private static final String USER_B = "user-B";
  private static final long PROJECT_1 = 4242L;
  private static final long PROJECT_2 = 4343L;

  private StorageIo storage;
  private AIAgentEngine engine;
  private ConversationManager cm;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = StorageIoInstanceHolder.getInstance();
    engine = new AIAgentEngine(storage);
    cm = engine.getConversationManager();
  }

  /** Blank conversationId should mint a new conversation row. */
  public void testResolveOrCreateMintsWhenBlank() {
    String id = engine.resolveOrCreateConversationId(USER_A, PROJECT_1, null);
    assertNotNull(id);
    assertFalse(id.isEmpty());
    ConversationData meta = cm.getConversationMetadata(id);
    assertNotNull("metadata row should exist for newly created conversation", meta);
    assertEquals(USER_A, meta.userId);
    assertEquals(PROJECT_1, meta.projectId);
  }

  /** A valid (user+project-matching) requested id should be reused as-is. */
  public void testResolveOrCreateUsesProvidedConversationId() {
    String seeded = cm.createConversation(USER_A, PROJECT_1);
    String resolved = engine.resolveOrCreateConversationId(USER_A, PROJECT_1, seeded);
    assertEquals(seeded, resolved);
  }

  /** Requested id owned by a different user is silently replaced. */
  public void testResolveOrCreateRejectsMismatchedUser() {
    String seeded = cm.createConversation(USER_A, PROJECT_1);
    String resolved = engine.resolveOrCreateConversationId(USER_B, PROJECT_1, seeded);
    assertNotNull(resolved);
    assertFalse("mismatched user must not receive the other user's convId",
        seeded.equals(resolved));
  }

  /** Requested id for a different project is silently replaced. */
  public void testResolveOrCreateRejectsMismatchedProject() {
    String seeded = cm.createConversation(USER_A, PROJECT_1);
    String resolved = engine.resolveOrCreateConversationId(USER_A, PROJECT_2, seeded);
    assertNotNull(resolved);
    assertFalse(seeded.equals(resolved));
  }

  /**
   * Engine's main-conversation init should roundtrip convId-keyed state
   * through Memcache and preserve providerRef when present.
   */
  public void testInitMainConversationRoundtripsProviderRef() {
    String convId = cm.createConversation(USER_A, PROJECT_1);
    cm.saveConversation(convId,
        new AIConversationState("anthropic", convId, "ref-x"));
    AIAgentEngine.ConversationInit init = engine.initMainConversation(convId);
    assertFalse("should not be marked new when cached state exists", init.isNew);
    assertEquals(convId, init.conv.getConversationId());
    assertEquals("ref-x", init.conv.getProviderRef());
  }

  /**
   * The cold-memcache scenario: history survives in Datastore even after
   * {@link ConversationManager#clearConversationState} evicts the state.
   * This is the pre-condition that the providers' history-replay fallback
   * relies on.
   */
  public void testHistorySurvivesMemcacheEviction() {
    String convId = cm.createConversation(USER_A, PROJECT_1);
    cm.storeMessage(convId, MessageRole.USER, "hello", true);
    cm.storeMessage(convId, MessageRole.ASSISTANT, "hi", true);
    cm.saveConversation(convId,
        new AIConversationState("anthropic", convId, "ref-x"));

    cm.clearConversationState(convId);
    assertNull("state should be gone", cm.getConversation(convId));
    // But the persisted history is intact:
    assertEquals(2, cm.loadConversation(convId).size());
    // And the metadata row is intact too:
    assertNotNull(cm.getConversationMetadata(convId));
  }
}
