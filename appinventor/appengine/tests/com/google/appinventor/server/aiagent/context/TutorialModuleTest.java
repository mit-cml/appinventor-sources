package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.server.aiagent.TutorialContentCache;

import junit.framework.TestCase;

/**
 * Tests for TutorialModule. Uses a fake cache to avoid real HTTP calls.
 */
public class TutorialModuleTest extends TestCase {

  /** Fake cache that returns a fixed string for any URL. */
  private static class FakeTutorialContentCache extends TutorialContentCache {
    private final String fixedContent;

    FakeTutorialContentCache(String fixedContent) {
      super(null);  // No real StorageIo needed
      this.fixedContent = fixedContent;
    }

    @Override
    public String get(String url) {
      if (url == null || url.isEmpty()) {
        return null;
      }
      return fixedContent;
    }
  }

  public void testBuildWithTutorialUrl() {
    TutorialModule module = new TutorialModule(
        new FakeTutorialContentCache("Step 1: Add a button."));
    ContextParams params = new ContextParams(
        "user1", 1L, "Screen1", "ScreenEditor", "", "Designer",
        "", "{\"tutorialURL\":\"https://appinventor.mit.edu/test\"}",
        "", null, null);
    String result = module.build(params);
    assertNotNull(result);
    assertTrue(result.contains("[Active Tutorial Context]"));
    assertTrue(result.contains("Act as a tutor"));
    assertTrue(result.contains("Step 1: Add a button."));
  }

  public void testBuildWithNoTutorialUrl() {
    TutorialModule module = new TutorialModule(
        new FakeTutorialContentCache("content"));
    ContextParams params = new ContextParams(
        "user1", 1L, "Screen1", "ScreenEditor", "", "Designer",
        "", "{\"projectName\":\"Test\"}",
        "", null, null);
    assertNull(module.build(params));
  }

  public void testBuildWithEmptyTutorialUrl() {
    TutorialModule module = new TutorialModule(
        new FakeTutorialContentCache("content"));
    ContextParams params = new ContextParams(
        "user1", 1L, "Screen1", "ScreenEditor", "", "Designer",
        "", "{\"tutorialURL\":\"\"}",
        "", null, null);
    assertNull(module.build(params));
  }

  public void testBuildWithNullSnapshot() {
    TutorialModule module = new TutorialModule(
        new FakeTutorialContentCache("content"));
    ContextParams params = new ContextParams(
        "user1", 1L, "Screen1", "ScreenEditor", "", "Designer",
        "", null, "", null, null);
    assertNull(module.build(params));
  }

  public void testBuildWithCacheReturningNull() {
    TutorialModule module = new TutorialModule(
        new FakeTutorialContentCache(null));
    ContextParams params = new ContextParams(
        "user1", 1L, "Screen1", "ScreenEditor", "", "Designer",
        "", "{\"tutorialURL\":\"https://appinventor.mit.edu/test\"}",
        "", null, null);
    assertNull(module.build(params));
  }
}
