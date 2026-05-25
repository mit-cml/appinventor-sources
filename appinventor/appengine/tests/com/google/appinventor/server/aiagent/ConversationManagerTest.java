// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.aiagent.llm.ChatMessage;
import com.google.appinventor.server.storage.AIConversationState;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData.ConversationData;
import com.google.appinventor.server.storage.StoredData.MessageRole;

import java.util.List;

/**
 * Tests for {@link ConversationManager} in the multi-conversation model.
 * Covers convId-keyed state, metadata CRUD, message persistence (with
 * automatic {@code updatedAt} bumping), and the unchanged screen-scoped
 * overloads used by the orchestration child agents.
 */
public class ConversationManagerTest extends LocalDatastoreTestCase {

  private static final String USER_A = "user-A";
  private static final long PROJECT_1 = 4242L;

  private StorageIo storage;
  private ConversationManager manager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = StorageIoInstanceHolder.getInstance();
    manager = new ConversationManager(storage);
  }

  public void testCreateAndGetByConvId() {
    String convId = manager.createConversation(USER_A, PROJECT_1);
    assertNotNull(convId);
    ConversationData meta = manager.getConversationMetadata(convId);
    assertNotNull(meta);
    assertEquals(USER_A, meta.userId);
    assertEquals(PROJECT_1, meta.projectId);

    AIConversationState state = new AIConversationState("anthropic", convId, "ref-1");
    manager.saveConversation(convId, state);
    AIConversationState loaded = manager.getConversation(convId);
    assertNotNull(loaded);
    assertEquals("anthropic", loaded.getProviderName());
    assertEquals(convId, loaded.getConversationId());
    assertEquals("ref-1", loaded.getProviderRef());
  }

  public void testStoreMessageBumpsUpdatedAt() throws Exception {
    String convId = manager.createConversation(USER_A, PROJECT_1);
    long initialUpdatedAt = manager.getConversationMetadata(convId).updatedAt;

    // Sleep a few millis so the new timestamp is strictly greater.
    Thread.sleep(3);

    manager.storeMessage(convId, MessageRole.USER, "hello", true);
    long afterStore = manager.getConversationMetadata(convId).updatedAt;
    assertTrue("updatedAt should bump after storeMessage: before=" + initialUpdatedAt
        + " after=" + afterStore, afterStore > initialUpdatedAt);

    List<ChatMessage> history = manager.loadConversation(convId);
    assertEquals(1, history.size());
    assertEquals("hello", history.get(0).getText());
  }

  public void testDeleteConversationRemovesMetadataAndMessages() {
    String convId = manager.createConversation(USER_A, PROJECT_1);
    manager.storeMessage(convId, MessageRole.USER, "one", true);
    manager.storeMessage(convId, MessageRole.ASSISTANT, "two", true);
    assertNotNull(manager.getConversationMetadata(convId));
    assertEquals(2, manager.loadConversation(convId).size());

    manager.deleteConversation(convId);
    assertNull(manager.getConversationMetadata(convId));
    assertEquals(0, manager.loadConversation(convId).size());
  }

  public void testRenameUpdatesTitle() {
    String convId = manager.createConversation(USER_A, PROJECT_1);

    manager.renameConversation(convId, "  My Chat  ");
    assertEquals("My Chat", manager.getConversationMetadata(convId).title);

    manager.renameConversation(convId, "   ");
    assertNull(manager.getConversationMetadata(convId).title);

    manager.renameConversation(convId, "Another");
    assertEquals("Another", manager.getConversationMetadata(convId).title);

    manager.renameConversation(convId, null);
    assertNull(manager.getConversationMetadata(convId).title);
  }

  public void testScreenScopedMethodsUnchanged() {
    String screen = "Screen1";
    assertNull(manager.getConversation(PROJECT_1, screen));

    AIConversationState state = new AIConversationState("anthropic", "child-conv", "child-ref");
    manager.saveConversation(PROJECT_1, screen, state);
    AIConversationState loaded = manager.getConversation(PROJECT_1, screen);
    assertNotNull(loaded);
    assertEquals("child-conv", loaded.getConversationId());
    assertEquals("child-ref", loaded.getProviderRef());

    manager.clearConversation(PROJECT_1, screen);
    assertNull(manager.getConversation(PROJECT_1, screen));
  }
}
