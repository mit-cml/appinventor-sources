// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.aiagent.llm.ChatMessage;
import com.google.appinventor.server.storage.StoredData.ConversationData;
import com.google.appinventor.server.storage.StoredData.MessageRole;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;

import java.util.List;

/**
 * Tests for multi-conversation CRUD and convId-keyed state in {@link ObjectifyStorageIo}.
 */
public class ObjectifyStorageIoConversationTest extends LocalDatastoreTestCase {

  private static final String USER_A = "user-A";
  private static final String USER_B = "user-B";
  private static final long PROJECT_1 = 1001L;
  private static final long PROJECT_2 = 2002L;

  private ObjectifyStorageIo storage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = new ObjectifyStorageIo();
  }

  public void testCreateAndGetConversation() {
    String convId = storage.createConversation(USER_A, PROJECT_1);
    assertNotNull(convId);
    ConversationData cd = storage.getConversationMetadata(convId);
    assertNotNull(cd);
    assertEquals(convId, cd.conversationId);
    assertEquals(PROJECT_1, cd.projectId);
    assertEquals(USER_A, cd.userId);
    assertNull(cd.title);
    assertTrue(cd.createdAt > 0);
    assertEquals(cd.createdAt, cd.updatedAt);
  }

  public void testGetConversationMetadataMissing() {
    assertNull(storage.getConversationMetadata("does-not-exist"));
  }

  public void testListConversationsSortedDescByUpdatedAt() throws Exception {
    String c1 = storage.createConversation(USER_A, PROJECT_1);
    Thread.sleep(2);
    String c2 = storage.createConversation(USER_A, PROJECT_1);
    Thread.sleep(2);
    String c3 = storage.createConversation(USER_A, PROJECT_1);

    // Touch c1 so it becomes the most recent.
    long now = System.currentTimeMillis() + 1000L;
    storage.touchConversation(c1, now);

    List<ConversationData> list = storage.listConversations(USER_A, PROJECT_1);
    assertEquals(3, list.size());
    assertEquals(c1, list.get(0).conversationId);
    assertEquals(c3, list.get(1).conversationId);
    assertEquals(c2, list.get(2).conversationId);
  }

  public void testListConversationsFiltersByUserAndProject() {
    String a1 = storage.createConversation(USER_A, PROJECT_1);
    String a2 = storage.createConversation(USER_A, PROJECT_2);
    String b1 = storage.createConversation(USER_B, PROJECT_1);

    List<ConversationData> listA1 = storage.listConversations(USER_A, PROJECT_1);
    assertEquals(1, listA1.size());
    assertEquals(a1, listA1.get(0).conversationId);

    List<ConversationData> listA2 = storage.listConversations(USER_A, PROJECT_2);
    assertEquals(1, listA2.size());
    assertEquals(a2, listA2.get(0).conversationId);

    List<ConversationData> listB1 = storage.listConversations(USER_B, PROJECT_1);
    assertEquals(1, listB1.size());
    assertEquals(b1, listB1.get(0).conversationId);

    List<ConversationData> listB2 = storage.listConversations(USER_B, PROJECT_2);
    assertTrue(listB2.isEmpty());
  }

  public void testRenameConversationAndFallbackToNull() {
    String convId = storage.createConversation(USER_A, PROJECT_1);

    storage.renameConversation(convId, "  Hello World  ");
    assertEquals("Hello World", storage.getConversationMetadata(convId).title);

    storage.renameConversation(convId, "   ");
    assertNull(storage.getConversationMetadata(convId).title);

    storage.renameConversation(convId, null);
    assertNull(storage.getConversationMetadata(convId).title);

    // Overlong title should be truncated to 120 chars.
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 150; i++) sb.append('x');
    storage.renameConversation(convId, sb.toString());
    assertEquals(120, storage.getConversationMetadata(convId).title.length());
  }

  public void testRenameMissingConversationIsNoop() {
    // Should not throw.
    storage.renameConversation("does-not-exist", "Anything");
  }

  public void testTouchConversationUpdatesTimestamp() throws Exception {
    String convId = storage.createConversation(USER_A, PROJECT_1);
    long original = storage.getConversationMetadata(convId).updatedAt;
    Thread.sleep(2);
    long later = System.currentTimeMillis() + 1000L;
    storage.touchConversation(convId, later);
    assertEquals(later, storage.getConversationMetadata(convId).updatedAt);
    assertTrue(storage.getConversationMetadata(convId).updatedAt > original);
  }

  public void testDeleteConversationRemovesMetadataMessagesAndState() {
    String convId = storage.createConversation(USER_A, PROJECT_1);

    // Seed some messages.
    long t0 = System.currentTimeMillis();
    storage.storeAIConversationMessage(convId, t0, 0, MessageRole.USER, "hello", true);
    storage.storeAIConversationMessage(convId, t0 + 1, 1, MessageRole.ASSISTANT, "hi", true);
    assertEquals(2, storage.loadAIConversationMessages(convId).size());

    // Seed convId-keyed state.
    AIConversationState state = new AIConversationState("test-provider", convId, null);
    storage.saveAIConversationStateByConvId(convId, state);
    assertNotNull(storage.getAIConversationStateByConvId(convId));

    storage.deleteConversation(convId);

    assertNull(storage.getConversationMetadata(convId));
    assertTrue(storage.loadAIConversationMessages(convId).isEmpty());
    assertNull(storage.getAIConversationStateByConvId(convId));
  }

  public void testConvIdKeyedStateRoundtrip() {
    String convId = storage.createConversation(USER_A, PROJECT_1);
    assertNull(storage.getAIConversationStateByConvId(convId));

    AIConversationState state = new AIConversationState("test-provider", convId, null);
    storage.saveAIConversationStateByConvId(convId, state);

    AIConversationState loaded = storage.getAIConversationStateByConvId(convId);
    assertNotNull(loaded);
    assertEquals(convId, loaded.getConversationId());
    assertEquals("test-provider", loaded.getProviderName());
  }

  public void testClearAIConversationStateByConvId() {
    String convId = storage.createConversation(USER_A, PROJECT_1);
    AIConversationState state = new AIConversationState("test-provider", convId, null);
    storage.saveAIConversationStateByConvId(convId, state);
    assertNotNull(storage.getAIConversationStateByConvId(convId));

    storage.clearAIConversationStateByConvId(convId);
    assertNull(storage.getAIConversationStateByConvId(convId));
  }

  public void testDeleteProjectCascadesToConversations() {
    final String userId = "cascade-user";
    storage.getUser(userId, "cascade@test.com");

    // Seed a minimal project so deleteProject has a real row to remove.
    Project project = new Project("CascadeProject");
    project.setProjectType("FakeProjectType");
    project.addTextFile(new TextFile("src/Screen1.scm", "content"));
    long projectId = storage.createProject(userId, project, "{}");

    // Seed conversations + a message.
    String convId1 = storage.createConversation(userId, projectId);
    String convId2 = storage.createConversation(userId, projectId);
    long t0 = System.currentTimeMillis();
    storage.storeAIConversationMessage(convId1, t0, 0, MessageRole.USER, "hello", true);
    storage.storeAIConversationMessage(convId1, t0 + 1, 1, MessageRole.ASSISTANT, "hi", true);
    storage.storeAIConversationMessage(convId2, t0, 0, MessageRole.USER, "other", true);

    // Sanity: both conversations and their messages are present.
    assertNotNull(storage.getConversationMetadata(convId1));
    assertNotNull(storage.getConversationMetadata(convId2));
    assertEquals(2, storage.loadAIConversationMessages(convId1).size());
    assertEquals(1, storage.loadAIConversationMessages(convId2).size());

    storage.deleteProject(userId, projectId);

    // Cascade removed all conversations and their messages.
    assertNull(storage.getConversationMetadata(convId1));
    assertNull(storage.getConversationMetadata(convId2));
    assertTrue(storage.loadAIConversationMessages(convId1).isEmpty());
    assertTrue(storage.loadAIConversationMessages(convId2).isEmpty());
  }

  public void testLoadAIConversationMessagesPreservesTimestamp() {
    String convId = storage.createConversation(USER_A, PROJECT_1);
    long t0 = 1_700_000_000_000L;
    storage.storeAIConversationMessage(convId, t0, 0, MessageRole.USER, "hi", true);
    storage.storeAIConversationMessage(convId, t0 + 5, 1, MessageRole.ASSISTANT, "yo", true);

    List<ChatMessage> msgs = storage.loadAIConversationMessages(convId);
    assertEquals(2, msgs.size());
    assertEquals(t0, msgs.get(0).getTimestamp());
    assertEquals(t0 + 5, msgs.get(1).getTimestamp());
  }
}
