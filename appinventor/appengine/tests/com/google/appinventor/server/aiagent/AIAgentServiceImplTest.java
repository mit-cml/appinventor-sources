// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.LocalUser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData.MessageRole;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIConversationSummary;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.user.User;

import java.util.List;

/**
 * Tests for the conversation-CRUD RPC surface on {@link AIAgentServiceImpl}:
 * {@code listConversations}, {@code renameConversation},
 * {@code deleteConversation}, and {@code getConversationHistory(String)}.
 *
 * <p>Auth is mocked by setting {@link LocalUser}'s ThreadLocal directly —
 * {@code AIAgentServiceImpl.userInfoProvider} resolves to
 * {@code LocalUser.getInstance()}.</p>
 */
public class AIAgentServiceImplTest extends LocalDatastoreTestCase {

  private static final String USER_A = "user-a";
  private static final String USER_B = "user-b";
  private static final String EMAIL_A = "a@test.com";
  private static final String EMAIL_B = "b@test.com";

  private StorageIo storage;
  private AIAgentServiceImpl service;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Use whatever StorageIoInstanceHolder hands back; the servlet resolves
    // it via the same holder. LocalDatastoreTestCase resets the datastore
    // between tests, so this is safe.
    storage = StorageIoInstanceHolder.getInstance();

    storage.getUser(USER_A, EMAIL_A);
    storage.getUser(USER_B, EMAIL_B);
    service = new AIAgentServiceImpl();
  }

  private void setCurrentUser(String userId) {
    LocalUser.getInstance().set(
        new User(userId, userId + "@test.com", true, false, "test-session"));
  }

  private long seedProject(String userId, String name) {
    Project project = new Project(name);
    project.setProjectType("FakeProjectType");
    project.addTextFile(new TextFile("src/Screen1.scm", "content"));
    return storage.createProject(userId, project, "{}");
  }

  public void testListConversationsReturnsOnlyOwnProjectConversations() {
    long projectA1 = seedProject(USER_A, "A1");
    long projectA2 = seedProject(USER_A, "A2");
    long projectB1 = seedProject(USER_B, "B1");

    String convA1 = storage.createConversation(USER_A, projectA1);
    storage.createConversation(USER_A, projectA2);
    storage.createConversation(USER_B, projectB1);

    setCurrentUser(USER_A);
    List<AIConversationSummary> rows = service.listConversations(projectA1);
    assertEquals(1, rows.size());
    assertEquals(convA1, rows.get(0).getConversationId());
  }

  public void testListConversationsRejectsProjectNotOwned() {
    long projectB1 = seedProject(USER_B, "B1");
    storage.createConversation(USER_B, projectB1);

    setCurrentUser(USER_A);
    try {
      service.listConversations(projectB1);
      fail("Expected SecurityException when listing someone else's project");
    } catch (SecurityException expected) {
      // OK
    }
  }

  public void testRenameConversationRejectsOtherUser() {
    long projectA1 = seedProject(USER_A, "A1");
    String convA1 = storage.createConversation(USER_A, projectA1);

    setCurrentUser(USER_B);
    try {
      service.renameConversation(convA1, "hacked");
      fail("Expected SecurityException when renaming someone else's conversation");
    } catch (SecurityException expected) {
      // OK
    }
    // Ensure the title wasn't mutated.
    assertNull(storage.getConversationMetadata(convA1).title);
  }

  public void testRenameConversationSucceedsForOwner() {
    long projectA1 = seedProject(USER_A, "A1");
    String convA1 = storage.createConversation(USER_A, projectA1);

    setCurrentUser(USER_A);
    AIConversationSummary updated = service.renameConversation(convA1, "My chat");
    assertEquals(convA1, updated.getConversationId());
    assertEquals("My chat", updated.getTitle());
    assertEquals("My chat", storage.getConversationMetadata(convA1).title);
  }

  public void testDeleteConversationRejectsOtherUser() {
    long projectA1 = seedProject(USER_A, "A1");
    String convA1 = storage.createConversation(USER_A, projectA1);

    setCurrentUser(USER_B);
    try {
      service.deleteConversation(convA1);
      fail("Expected SecurityException when deleting someone else's conversation");
    } catch (SecurityException expected) {
      // OK
    }
    // Still there.
    assertNotNull(storage.getConversationMetadata(convA1));
  }

  public void testDeleteConversationSucceedsForOwner() {
    long projectA1 = seedProject(USER_A, "A1");
    String convA1 = storage.createConversation(USER_A, projectA1);

    setCurrentUser(USER_A);
    service.deleteConversation(convA1);
    assertNull(storage.getConversationMetadata(convA1));
  }

  public void testGetConversationHistoryByConvIdReturnsDisplayMessages() {
    long projectA1 = seedProject(USER_A, "A1");
    String convA1 = storage.createConversation(USER_A, projectA1);
    long t0 = 1_700_000_000_000L;
    storage.storeAIConversationMessage(convA1, t0, 0, MessageRole.USER, "hi", true);
    storage.storeAIConversationMessage(convA1, t0 + 1, 1, MessageRole.ASSISTANT, "yo", true);
    // Non-display message should be filtered out.
    storage.storeAIConversationMessage(convA1, t0 + 2, 2, MessageRole.TOOL_RESULT,
        "[tool]", false);

    setCurrentUser(USER_A);
    List<AIConversationMessage> history = service.getConversationHistory(convA1);
    assertEquals(2, history.size());
    assertEquals("user", history.get(0).getRole());
    assertEquals("hi", history.get(0).getText());
    assertEquals(t0, history.get(0).getTimestamp());
    assertEquals("assistant", history.get(1).getRole());
    assertEquals("yo", history.get(1).getText());
    assertEquals(t0 + 1, history.get(1).getTimestamp());
  }

  public void testGetConversationHistoryRejectsOtherUser() {
    long projectA1 = seedProject(USER_A, "A1");
    String convA1 = storage.createConversation(USER_A, projectA1);

    setCurrentUser(USER_B);
    try {
      service.getConversationHistory(convA1);
      fail("Expected SecurityException when reading someone else's history");
    } catch (SecurityException expected) {
      // OK
    }
  }

  public void testRejectUnknownConversationId() {
    setCurrentUser(USER_A);
    try {
      service.renameConversation("does-not-exist", "x");
      fail("Expected SecurityException for unknown conversation id");
    } catch (SecurityException expected) {
      // OK
    }
    try {
      service.deleteConversation("does-not-exist");
      fail("Expected SecurityException for unknown conversation id");
    } catch (SecurityException expected) {
      // OK
    }
    try {
      service.getConversationHistory("does-not-exist");
      fail("Expected SecurityException for unknown conversation id");
    } catch (SecurityException expected) {
      // OK
    }
  }
}
