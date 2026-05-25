package com.google.appinventor.server.aiagent;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TutorialContentCacheTest extends TestCase {

  public void testIsUrlAllowedWithMatchingPrefix() {
    List<String> allowed = Arrays.asList(
        "https://appinventor.mit.edu/", "https://mit-cml.github.io/");
    assertTrue(TutorialContentCache.isUrlAllowed(
        "https://appinventor.mit.edu/explore/ai2/hellopurr", allowed));
    assertTrue(TutorialContentCache.isUrlAllowed(
        "https://mit-cml.github.io/yrtoolkit/yr/tutorials/SimpleChatBot.html", allowed));
  }

  public void testIsUrlAllowedRejectsNonMatchingUrl() {
    List<String> allowed = Arrays.asList("https://appinventor.mit.edu/");
    assertFalse(TutorialContentCache.isUrlAllowed(
        "https://evil.com/tutorial", allowed));
    assertFalse(TutorialContentCache.isUrlAllowed(
        "http://localhost:8080/admin", allowed));
    assertFalse(TutorialContentCache.isUrlAllowed(
        "http://169.254.169.254/metadata", allowed));
  }

  public void testIsUrlAllowedRejectsNullAndEmpty() {
    List<String> allowed = Arrays.asList("https://appinventor.mit.edu/");
    assertFalse(TutorialContentCache.isUrlAllowed(null, allowed));
    assertFalse(TutorialContentCache.isUrlAllowed("", allowed));
  }

  public void testIsUrlAllowedWithEmptyAllowlist() {
    assertFalse(TutorialContentCache.isUrlAllowed(
        "https://appinventor.mit.edu/test", Collections.<String>emptyList()));
  }
}
