# Tutorial-Aware AI Agent

## Problem

The AI agent has zero awareness of tutorials. When a user is following a guided tutorial (TutorialURL is set, iframe sidebar visible), the AI doesn't know a tutorial is active, what it's about, or what step the user is on. It behaves the same whether the user is building freeform or following a step-by-step guide -- offering to do everything at once rather than guiding the student through each step.

## Solution

When a project has a `TutorialURL` set, the server fetches the tutorial page content via HTTP, caches it in memory (8h TTL), and includes it as an additional LLM context message with pedagogical instructions. The entire feature is gated by a static boolean flag so it can be disabled without code changes.

## Design Decisions

- **Server-side HTTP fetch** over client-side extraction: the tutorial content lives on external servers (MIT). The server fetches and caches it, avoiding any changes to tutorial pages or the postMessage protocol.
- **Full text, no summarization**: tutorials are 2,000-3,000 words -- small enough to include in full. The LLM gets maximum fidelity to reference exact steps.
- **Piggyback on projectSnapshot**: the `tutorialURL` is added to the existing `projectSnapshot` JSON field that `AIContextCollector` already builds. No changes to `AIAgentRequest`, `ContextParams`, or the `buildContextMessages` signature.
- **Dedicated cache class + formatter module**: follows Approach C -- `TutorialContentCache` handles I/O and caching, `TutorialModule` handles formatting. Each class has one job.
- **Pedagogical instructions in a resource file**: `tutorial_instructions.md` is easier to iterate on than hardcoded strings.
- **Static boolean flag**: `AIContextBuilder.INCLUDE_TUTORIAL_CONTEXT` gates the entire feature. When `false`, no fetch happens and no tutorial context is sent.
- **URL allowlist validation**: the server only fetches URLs that pass the same allowlist used client-side (`StorageIo.getTutorialsUrlAllowed()`), preventing SSRF attacks via crafted TutorialURL values.
- **Bounded cache**: max 100 entries with oldest-first eviction to prevent unbounded memory growth.

## Architecture

### Data Flow

```
projectSnapshot JSON (client adds tutorialURL)
        |
        v
+----------------+        +-----------------------+
| TutorialModule |--get-->| TutorialContentCache  |
|  (formatter)   |<-text--| (fetch + 24h cache)   |
+-------+--------+        +-----------+-----------+
        |                              |
        | context message              | HTTP GET + HTML->text
        v                              v
    LLM prompt                 External tutorial page
```

### Context Message Ordering

```
Message 1: ModeModule        (mode instructions + view rules)
Message 2: ProjectModule     (metadata, screens, assets)
Message 3: ScreenModule      (component tree + blocks YAIL)
Message 4: TutorialModule    (tutorial instructions + content)  <-- NEW
```

Message 4 is only added when `INCLUDE_TUTORIAL_CONTEXT` is `true` AND the project has a non-empty `TutorialURL` AND the fetch succeeds.

## Components

### AIContextCollector (modified)

**File:** `client/editor/youngandroid/aiagent/AIContextCollector.java`

In `buildProjectSnapshot()`, after the existing color properties and before the screen names section, read the `TutorialURL` from project settings and add it to the snapshot JSON:

```java
String tutorialURL = projectEditor.getProjectSettingsProperty(
    SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
    SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
if (tutorialURL != null && !tutorialURL.isEmpty()) {
  json.append(",\"tutorialURL\":").append(AIJsonUtils.jsonString(tutorialURL));
}
```

### TutorialContentCache (new)

**File:** `server/aiagent/TutorialContentCache.java`

Fetches tutorial page HTML via HTTP, strips HTML to plain text, and caches results in memory with a 8-hour TTL.

**Public API:**

```java
public class TutorialContentCache {
  /** Returns plain text content for the URL, or null on failure. */
  public String get(String url);

  /** Checks the URL against the tutorial allowlist. */
  public boolean isUrlAllowed(String url, List<String> allowedPrefixes);
}
```

**Constructor** takes a `StorageIo` reference to access the tutorial URL allowlist via `storageIo.getTutorialsUrlAllowed()`.

**Internals:**

- `ConcurrentHashMap<String, CachedEntry>` where `CachedEntry` holds the extracted text and a creation timestamp. **Max 100 entries** -- when the limit is reached, the oldest entry (by timestamp) is evicted before inserting a new one.
- `get(url)` first validates the URL against the allowlist (`StorageIo.getTutorialsUrlAllowed()`). If not allowed, returns `null`. Otherwise returns cached text if fresh (< 8h), or fetches, strips, caches, and returns.
- `fetchUrl(url)` uses `java.net.HttpURLConnection` with 5s connect timeout and 10s read timeout. Follows redirects (the MIT tutorial URLs 301-redirect to `mit-cml.github.io`). Returns `null` on any `IOException`. Logs a warning on failure.
- `stripHtmlForTutorial(html)` is a richer variant than the existing `ContextUtils.stripHtml()` (which collapses all whitespace to single spaces). This method: (1) removes `<head>`, `<script>`, `<style>`, `<nav>`, `<footer>`, and `<header>` elements and their contents (these carry site chrome, not tutorial content), (2) removes all remaining HTML tags (keeping text -- `<h1>`-`<h6>`, `<p>`, `<li>`, etc. are stripped to text), (3) decodes common HTML entities, (4) collapses runs of 3+ newlines to 2 (preserving paragraph structure). This lives in `ContextUtils` as a new static method to keep all HTML processing centralized.
- Failed fetches are NOT cached -- next request retries.
- Thread safety via `ConcurrentHashMap`. Two concurrent fetches of the same URL may both execute (harmless), and the cache converges immediately.
- The cache is per-servlet-instance (per `AIAgentEngine`), not globally shared. This is by design -- each instance independently caches, which is correct and avoids cross-instance coordination.

### TutorialModule (new)

**File:** `server/aiagent/context/TutorialModule.java`

Extends `ContextModule`. Reads the `tutorialURL` from the `projectSnapshot` JSON, calls the cache, and assembles the context message.

**Output format:**

```
[Active Tutorial Context]

## Active Tutorial

{contents of tutorial_instructions.md}

### Tutorial Content

{fetched page text}
```

Returns `null` when:
- No `projectSnapshot` in params
- No `tutorialURL` in the snapshot
- `tutorialURL` is empty
- Cache returns `null` (fetch failed)

The constructor takes a `TutorialContentCache` reference.

### tutorial_instructions.md (new)

**File:** `server/aiagent/resources/tutorial_instructions.md`

Pedagogical instructions loaded once by `TutorialModule`:

```markdown
The user is following a guided tutorial. Adapt your behavior:

- Act as a tutor, not an autopilot. Guide the user through steps rather than doing everything at once.
- If the user asks for help, give hints before full solutions.
- Encourage the user to understand each step before moving on.
- When the user asks you to do something that aligns with a tutorial step, confirm they understand what is happening as you apply changes.
- Reference specific tutorial steps when relevant.
```

### AIContextBuilder (modified)

**File:** `server/aiagent/AIContextBuilder.java`

Three changes:

1. **Static flag:**

```java
/**
 * Controls whether tutorial context is included in LLM requests.
 * When true, if the project has a TutorialURL set, the tutorial page
 * content is fetched and included as an additional context message.
 */
static final boolean INCLUDE_TUTORIAL_CONTEXT = true;
```

2. **New fields** initialized in constructor (which already receives `StorageIo`):

```java
private final TutorialContentCache tutorialContentCache;
private final TutorialModule tutorialModule;

public AIContextBuilder(StorageIo storageIo) {
  // ... existing code ...
  this.tutorialContentCache = new TutorialContentCache(storageIo);
  this.tutorialModule = new TutorialModule(tutorialContentCache);
}
```

3. **Message 4** in `buildContextMessages()`:

```java
if (INCLUDE_TUTORIAL_CONTEXT) {
  String tutorialCtx = tutorialModule.build(params);
  if (tutorialCtx != null) {
    messages.add(tutorialCtx);
    AIDebug.log(LOG, "Context message 4 (tutorial): " + tutorialCtx.length() + " chars");
  }
}
```

## Behavior Matrix

| Flag | TutorialURL | Fetch | Result |
|------|-------------|-------|--------|
| `true` | set, allowed | succeeds | Tutorial context included as Message 4 |
| `true` | set, allowed | fails (timeout/404) | Message 4 silently omitted, next request retries |
| `true` | set, not in allowlist | N/A | No fetch, no Message 4 |
| `true` | set, not a valid URL | N/A | Fetch fails, silently omitted |
| `true` | empty/absent | N/A | No fetch, no Message 4 |
| `false` | any | N/A | Feature fully disabled, no fetch, no Message 4 |

## Files Changed

| File | Change | Type |
|------|--------|------|
| `client/.../aiagent/AIContextCollector.java` | Add `tutorialURL` to project snapshot JSON | Modified |
| `server/aiagent/TutorialContentCache.java` | HTTP fetch + URL allowlist + in-memory 24h bounded cache | New |
| `server/aiagent/context/TutorialModule.java` | Reads URL from snapshot, calls cache, formats context | New |
| `server/aiagent/context/ContextUtils.java` | Add `stripHtmlForTutorial()` method (preserves paragraph structure) | Modified |
| `server/aiagent/resources/tutorial_instructions.md` | Pedagogical instructions for the LLM | New |
| `server/aiagent/AIContextBuilder.java` | Static boolean flag, instantiate cache + module, add Message 4 | Modified |

## Not Changed

- `AIAgentRequest` -- no new fields
- `ContextParams` -- no new fields
- `AIAgentEngine` -- no changes to processRequest/continueRequest/reportExecutionErrors signatures
- `buildContextMessages` signature -- no new parameters
- `buildTools` -- no new tools
- System prompt (static layers) -- untouched
- `CONTRIBUTING_AI.md` -- should be updated after implementation to document the new module
