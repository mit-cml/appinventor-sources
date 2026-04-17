# Tutorial-Aware AI Agent Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the AI agent aware of active tutorials so it guides users through steps rather than doing everything at once.

**Architecture:** A `TutorialContentCache` fetches and caches tutorial HTML pages, a new `ContextUtils.stripHtmlForTutorial()` extracts clean text, and a `TutorialModule` context module assembles it with pedagogical instructions into a new LLM context message. The feature is gated by a static boolean in `AIContextBuilder`.

**Tech Stack:** Java (GWT client + App Engine server), JUnit 3, Ant build

**Spec:** `docs/superpowers/specs/2026-04-02-tutorial-aware-ai-agent-design.md`

---

### Task 1: Add `stripHtmlForTutorial()` to ContextUtils

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/context/ContextUtils.java`
- Create: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/ContextUtilsStripHtmlTest.java`

This is the HTML-to-text extraction method. It lives in `ContextUtils` alongside the existing `stripHtml()`. The key difference: this variant removes `<head>`, `<script>`, `<style>`, `<nav>`, `<footer>`, and `<header>` elements **with their contents**, then strips remaining tags, decodes entities, and preserves paragraph structure.

- [ ] **Step 1: Write the test class**

Create `appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/ContextUtilsStripHtmlTest.java`:

```java
package com.google.appinventor.server.aiagent.context;

import junit.framework.TestCase;

public class ContextUtilsStripHtmlTest extends TestCase {

  public void testStripsScriptElementsWithContents() {
    String html = "<p>Hello</p><script>var x = 1;</script><p>World</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Hello"));
    assertTrue(result.contains("World"));
    assertFalse(result.contains("var x"));
  }

  public void testStripsStyleElementsWithContents() {
    String html = "<p>Hello</p><style>.foo { color: red; }</style><p>World</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Hello"));
    assertFalse(result.contains("color"));
  }

  public void testStripsHeadElement() {
    String html = "<html><head><title>Test</title><meta charset='utf-8'></head>"
        + "<body><p>Content</p></body></html>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Content"));
    assertFalse(result.contains("Test"));
    assertFalse(result.contains("charset"));
  }

  public void testStripsNavFooterHeader() {
    String html = "<header><a>Site Logo</a></header>"
        + "<nav><a>Home</a><a>About</a></nav>"
        + "<main><p>Tutorial step 1</p></main>"
        + "<footer><p>Copyright 2025</p></footer>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Tutorial step 1"));
    assertFalse(result.contains("Site Logo"));
    assertFalse(result.contains("Home"));
    assertFalse(result.contains("Copyright"));
  }

  public void testPreservesParagraphStructure() {
    String html = "<p>First paragraph</p><p>Second paragraph</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("First paragraph"));
    assertTrue(result.contains("Second paragraph"));
    // Should not collapse everything to one line
    assertTrue(result.contains("\n"));
  }

  public void testCollapsesExcessiveBlankLines() {
    String html = "<p>A</p>\n\n\n\n\n<p>B</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertFalse(result.contains("\n\n\n"));
  }

  public void testDecodesHtmlEntities() {
    String html = "<p>Tom &amp; Jerry &lt;3&gt; &quot;friends&quot;</p>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Tom & Jerry <3> \"friends\""));
  }

  public void testStripsRemainingHtmlTags() {
    String html = "<div><h1>Title</h1><ul><li>Item 1</li><li>Item 2</li></ul></div>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Title"));
    assertTrue(result.contains("Item 1"));
    assertFalse(result.contains("<"));
  }

  public void testCaseInsensitiveTagRemoval() {
    String html = "<SCRIPT>alert('xss')</SCRIPT><P>Content</P>";
    String result = ContextUtils.stripHtmlForTutorial(html);
    assertTrue(result.contains("Content"));
    assertFalse(result.contains("alert"));
  }

  public void testNullAndEmptyInput() {
    assertEquals("", ContextUtils.stripHtmlForTutorial(null));
    assertEquals("", ContextUtils.stripHtmlForTutorial(""));
  }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: Compilation error -- `stripHtmlForTutorial` does not exist yet.

- [ ] **Step 3: Implement `stripHtmlForTutorial()`**

Add to `appinventor/appengine/src/com/google/appinventor/server/aiagent/context/ContextUtils.java`, after the existing `stripHtml()` method (after line 141):

```java
/**
 * Strip HTML for tutorial content extraction. Unlike {@link #stripHtml},
 * this method removes {@code <head>}, {@code <script>}, {@code <style>},
 * {@code <nav>}, {@code <footer>}, and {@code <header>} elements with
 * their contents, then strips remaining tags while preserving paragraph
 * structure.
 */
public static String stripHtmlForTutorial(String html) {
  if (html == null || html.isEmpty()) {
    return "";
  }
  // 1. Remove elements that carry site chrome or non-content data.
  //    Pattern.DOTALL so . matches newlines within elements.
  String text = html;
  for (String tag : new String[]{"head", "script", "style", "nav", "footer", "header"}) {
    text = text.replaceAll("(?is)<" + tag + "[^>]*>.*?</" + tag + ">", "\n");
  }
  // 2. Replace block-level closing tags with newlines to preserve structure.
  text = text.replaceAll("(?i)</(p|div|li|tr|h[1-6]|blockquote|section|article|main)>", "\n");
  text = text.replaceAll("(?i)<br\\s*/?>", "\n");
  // 3. Remove all remaining HTML tags.
  text = text.replaceAll("<[^>]+>", " ");
  // 4. Decode common HTML entities.
  text = text.replace("&amp;", "&")
             .replace("&lt;", "<")
             .replace("&gt;", ">")
             .replace("&quot;", "\"")
             .replace("&#39;", "'")
             .replace("&nbsp;", " ");
  // 5. Collapse runs of whitespace within lines, then collapse 3+ newlines to 2.
  text = text.replaceAll("[ \\t]+", " ");
  text = text.replaceAll(" *\\n *", "\n");
  text = text.replaceAll("\\n{3,}", "\n\n");
  return text.trim();
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All `ContextUtilsStripHtmlTest` tests PASS.

- [ ] **Step 5: Commit**

```
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/context/ContextUtils.java \
       appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/ContextUtilsStripHtmlTest.java
git commit -m "Add stripHtmlForTutorial() to ContextUtils

Extracts tutorial text from HTML by removing head, script, style, nav,
footer, and header elements with their contents, then stripping remaining
tags while preserving paragraph structure."
```

---

### Task 2: Create TutorialContentCache

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/TutorialContentCache.java`
- Create: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/TutorialContentCacheTest.java`

The cache handles URL validation, HTTP fetching, HTML stripping, and bounded in-memory caching (max 100 entries, 8h TTL).

- [ ] **Step 1: Write the test class**

Create `appinventor/appengine/tests/com/google/appinventor/server/aiagent/TutorialContentCacheTest.java`:

```java
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
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: Compilation error -- `TutorialContentCache` does not exist yet.

- [ ] **Step 3: Implement TutorialContentCache**

Create `appinventor/appengine/src/com/google/appinventor/server/aiagent/TutorialContentCache.java`:

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.context.ContextUtils;
import com.google.appinventor.server.storage.StorageIo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Fetches tutorial page HTML, strips it to plain text, and caches results
 * in memory with a 8-hour TTL. Max 100 entries with oldest-first eviction.
 *
 * <p>URLs are validated against the tutorial allowlist from
 * {@link StorageIo#getTutorialsUrlAllowed()} before any HTTP request is made.
 */
public class TutorialContentCache {

  private static final Logger LOG = Logger.getLogger(TutorialContentCache.class.getName());

  static final int CONNECT_TIMEOUT_MS = 5000;
  static final int READ_TIMEOUT_MS = 10000;
  static final long TTL_MS = 8 * 60 * 60 * 1000; // 8 hours
  static final int MAX_ENTRIES = 100;

  private final StorageIo storageIo;
  private final ConcurrentHashMap<String, CachedEntry> cache = new ConcurrentHashMap<>();

  static class CachedEntry {
    final String text;
    final long createdAt;

    CachedEntry(String text) {
      this.text = text;
      this.createdAt = System.currentTimeMillis();
    }

    boolean isExpired() {
      return System.currentTimeMillis() - createdAt > TTL_MS;
    }
  }

  public TutorialContentCache(StorageIo storageIo) {
    this.storageIo = storageIo;
  }

  /**
   * Returns plain text content for the tutorial URL, or {@code null} if
   * the URL is not in the allowlist, the fetch fails, or the URL is empty.
   */
  public String get(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    // Validate against allowlist
    List<String> allowed = storageIo.getTutorialsUrlAllowed();
    if (!isUrlAllowed(url, allowed)) {
      AIDebug.log(LOG, "Tutorial URL not in allowlist: " + url);
      return null;
    }

    // Check cache
    CachedEntry entry = cache.get(url);
    if (entry != null && !entry.isExpired()) {
      return entry.text;
    }

    // Fetch, strip, cache
    String html = fetchUrl(url);
    if (html == null) {
      return null;
    }

    String text = ContextUtils.stripHtmlForTutorial(html);
    evictIfNeeded();
    cache.put(url, new CachedEntry(text));
    return text;
  }

  /**
   * Checks whether a URL matches any of the allowed tutorial URL prefixes.
   */
  static boolean isUrlAllowed(String url, List<String> allowedPrefixes) {
    if (url == null || url.isEmpty() || allowedPrefixes == null) {
      return false;
    }
    for (String prefix : allowedPrefixes) {
      if (url.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Fetches the URL content as a string. Follows redirects. Returns
   * {@code null} on any failure.
   */
  String fetchUrl(String url) {
    try {
      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
      conn.setReadTimeout(READ_TIMEOUT_MS);
      conn.setInstanceFollowRedirects(true);
      conn.setRequestProperty("User-Agent", "AppInventor-AIAgent/1.0");

      int status = conn.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        LOG.warning("Tutorial fetch failed: HTTP " + status + " for " + url);
        return null;
      }

      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int read;
        while ((read = reader.read(buf)) != -1) {
          sb.append(buf, 0, read);
        }
        return sb.toString();
      }
    } catch (IOException e) {
      LOG.warning("Tutorial fetch error for " + url + ": " + e.getMessage());
      return null;
    }
  }

  /**
   * Evicts the oldest entry if the cache has reached {@link #MAX_ENTRIES}.
   */
  private void evictIfNeeded() {
    if (cache.size() < MAX_ENTRIES) {
      return;
    }
    String oldestKey = null;
    long oldestTime = Long.MAX_VALUE;
    for (Map.Entry<String, CachedEntry> e : cache.entrySet()) {
      if (e.getValue().createdAt < oldestTime) {
        oldestTime = e.getValue().createdAt;
        oldestKey = e.getKey();
      }
    }
    if (oldestKey != null) {
      cache.remove(oldestKey);
    }
  }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All `TutorialContentCacheTest` tests PASS.

- [ ] **Step 5: Commit**

```
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/TutorialContentCache.java \
       appinventor/appengine/tests/com/google/appinventor/server/aiagent/TutorialContentCacheTest.java
git commit -m "Add TutorialContentCache for tutorial page fetching

Fetches tutorial HTML via HTTP, validates URLs against the tutorial
allowlist, strips HTML to text via ContextUtils.stripHtmlForTutorial(),
and caches results in memory (8h TTL, max 100 entries)."
```

---

### Task 3: Create tutorial_instructions.md resource

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/tutorial_instructions.md`

The pedagogical instructions loaded by TutorialModule. Kept in a resource file so it's easy to adjust without code changes. The build system already copies `*.md` from this directory to the classpath.

- [ ] **Step 1: Create the resource file**

Create `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/tutorial_instructions.md`:

```markdown
The user is following a guided tutorial. Adapt your behavior:

- Act as a tutor, not an autopilot. Guide the user through steps rather than doing everything at once.
- If the user asks for help, give hints before full solutions.
- Encourage the user to understand each step before moving on.
- When the user asks you to do something that aligns with a tutorial step, confirm they understand what is happening as you apply changes.
- Reference specific tutorial steps when relevant.
```

- [ ] **Step 2: Verify the resource loads**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: Build succeeds (the resource is copied to classpath automatically by the existing `includes="**/*.md"` in the build config).

- [ ] **Step 3: Commit**

```
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/tutorial_instructions.md
git commit -m "Add tutorial pedagogical instructions resource

LLM instructions for tutorial-aware mode, loaded by TutorialModule."
```

---

### Task 4: Create TutorialModule

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/context/TutorialModule.java`
- Create: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/TutorialModuleTest.java`

The context module that reads the tutorial URL from the project snapshot, calls the cache, and assembles the context message. Follows the same pattern as `ProjectModule` and `ScreenModule`.

- [ ] **Step 1: Write the test class**

Create `appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/TutorialModuleTest.java`:

```java
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
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: Compilation error -- `TutorialModule` does not exist yet.

- [ ] **Step 3: Implement TutorialModule**

Create `appinventor/appengine/src/com/google/appinventor/server/aiagent/context/TutorialModule.java`:

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import com.google.appinventor.server.aiagent.TutorialContentCache;

import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Builds tutorial context for the LLM when the project has an active
 * tutorial (TutorialURL is set). Loads pedagogical instructions from
 * {@code tutorial_instructions.md} and appends the fetched tutorial
 * page content.
 */
public class TutorialModule extends ContextModule {

  private static final Logger LOG = Logger.getLogger(TutorialModule.class.getName());

  private static volatile String cachedInstructions;

  private final TutorialContentCache cache;

  public TutorialModule(TutorialContentCache cache) {
    this.cache = cache;
  }

  @Override
  public String build(ContextParams params) {
    String snapshotJson = params.getProjectSnapshot();
    if (snapshotJson == null || snapshotJson.isEmpty()) {
      return null;
    }

    String url;
    try {
      JSONObject snapshot = new JSONObject(snapshotJson);
      url = snapshot.optString("tutorialURL", "");
    } catch (Exception e) {
      LOG.warning("Failed to parse projectSnapshot for tutorialURL: " + e.getMessage());
      return null;
    }

    if (url.isEmpty()) {
      return null;
    }

    String content = cache.get(url);
    if (content == null) {
      return null;
    }

    if (cachedInstructions == null) {
      cachedInstructions = ContextUtils.loadResource("tutorial_instructions.md");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[Active Tutorial Context]\n\n");
    sb.append("## Active Tutorial\n\n");
    sb.append(cachedInstructions).append("\n");
    sb.append("### Tutorial Content\n\n");
    sb.append(content);
    return sb.toString();
  }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All `TutorialModuleTest` tests PASS.

- [ ] **Step 5: Commit**

```
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/context/TutorialModule.java \
       appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/TutorialModuleTest.java
git commit -m "Add TutorialModule context module

Reads tutorialURL from project snapshot, fetches content via
TutorialContentCache, and assembles tutorial context message
with pedagogical instructions from tutorial_instructions.md."
```

---

### Task 5: Wire into AIContextBuilder

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java`

Add the static boolean flag, instantiate the cache and module, and add Message 4 to `buildContextMessages()`.

- [ ] **Step 1: Add the static flag and new fields**

In `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java`, add after the `LOG` declaration (around line 57):

```java
/**
 * Controls whether tutorial context is included in LLM requests.
 * When {@code true}, if the project has a TutorialURL set, the tutorial
 * page content is fetched and included as an additional context message.
 */
static final boolean INCLUDE_TUTORIAL_CONTEXT = true;
```

Add the new fields alongside the existing module fields (around line 66):

```java
private final TutorialContentCache tutorialContentCache;
private final TutorialModule tutorialModule;
```

Add the import at the top:

```java
import com.google.appinventor.server.aiagent.context.TutorialModule;
```

- [ ] **Step 2: Initialize in constructor**

In the existing constructor `public AIContextBuilder(StorageIo storageIo)`, add after `this.storageIo = storageIo;`:

```java
this.tutorialContentCache = new TutorialContentCache(storageIo);
this.tutorialModule = new TutorialModule(tutorialContentCache);
```

- [ ] **Step 3: Add Message 4 to buildContextMessages()**

In `buildContextMessages()`, after the Message 3 block (after line 167 `messages.add(screenCtx);`), add:

```java
// Message 4: Tutorial context (if enabled and active)
if (INCLUDE_TUTORIAL_CONTEXT) {
  String tutorialCtx = tutorialModule.build(params);
  if (tutorialCtx != null) {
    messages.add(tutorialCtx);
    AIDebug.log(LOG, "Context message 4 (tutorial): " + tutorialCtx.length() + " chars");
  }
}
```

- [ ] **Step 4: Build to verify compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: Build succeeds.

- [ ] **Step 5: Commit**

```
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java
git commit -m "Wire TutorialModule into AIContextBuilder

Add INCLUDE_TUTORIAL_CONTEXT static flag, instantiate
TutorialContentCache and TutorialModule, and append tutorial
context as Message 4 in buildContextMessages() when enabled."
```

---

### Task 6: Add tutorialURL to client project snapshot

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java`

Add the TutorialURL from project settings into the project snapshot JSON that gets sent to the server on every request.

- [ ] **Step 1: Add tutorialURL to buildProjectSnapshot()**

In `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java`, in the `buildProjectSnapshot()` method, add after the `accentColor` block (after line 282) and before the `// Screen names` comment (line 285):

```java
// Tutorial URL
String tutorialURL = projectEditor.getProjectSettingsProperty(
    SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
    SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
if (tutorialURL != null && !tutorialURL.isEmpty()) {
  json.append(",\"tutorialURL\":").append(AIJsonUtils.jsonString(tutorialURL));
}
```

No new imports needed -- `SettingsConstants` and `AIJsonUtils` are already imported.

- [ ] **Step 2: Build the client to verify compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: Build succeeds.

- [ ] **Step 3: Commit**

```
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java
git commit -m "Add tutorialURL to project snapshot for AI context

Reads TutorialURL from project settings and includes it in the
projectSnapshot JSON sent to the server on every AI agent request."
```

---

### Task 7: Run full test suite and verify

**Files:** None (verification only)

- [ ] **Step 1: Run all AI agent tests**

Run: `cd appinventor && ant -f appengine/build.xml tests`
Expected: All tests PASS, including the new `ContextUtilsStripHtmlTest`, `TutorialContentCacheTest`, and `TutorialModuleTest`.

- [ ] **Step 2: Run full server build**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib AiClientLib`
Expected: Build succeeds with no warnings related to tutorial classes.

- [ ] **Step 3: Commit any remaining fixes if needed**
