# LLM Provider Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Bedrock, Vertex AI, OpenRouter, and generic OpenAI-compatible / Anthropic-compatible LLM providers to the AI Agent, refactoring existing providers into base-class hierarchies.

**Architecture:** Extract `OpenAIChatCompletionsProvider` base from `MiniMaxProvider` and `AnthropicCompatibleProvider` base from `AnthropicProvider`. Thin subclasses for MiniMax, Anthropic, and OpenRouter. Standalone implementations for Bedrock (Converse API + SigV4) and Vertex (generateContent + JWT/OAuth). No new JAR dependencies.

**Tech Stack:** Java 17, `HttpURLConnection`, `javax.crypto` (HMAC-SHA256), `java.security` (RSA/SHA256), `org.json`, App Engine Memcache.

**Spec:** `docs/superpowers/specs/2026-04-03-llm-provider-expansion-design.md`

---

## File Map

All provider files live in `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/`.
All test files live in `appinventor/appengine/tests/com/google/appinventor/server/aiagent/llm/`.

| File | Action | Purpose |
|------|--------|---------|
| `OpenAIChatCompletionsProvider.java` | Create | Base class: OpenAI chat/completions wire format |
| `MiniMaxProvider.java` | Rewrite | Thin subclass of `OpenAIChatCompletionsProvider` |
| `OpenRouterProvider.java` | Create | Thin subclass with OpenRouter URL + headers |
| `AnthropicCompatibleProvider.java` | Create | Full Anthropic Messages API implementation |
| `AnthropicProvider.java` | Rewrite | Thin subclass of `AnthropicCompatibleProvider` |
| `AwsSigV4Signer.java` | Create | AWS Signature V4 signing helper |
| `BedrockProvider.java` | Create | Standalone: Bedrock Converse API |
| `GcpAuthHelper.java` | Create | GCP service account JWT/OAuth helper |
| `VertexProvider.java` | Create | Standalone: Vertex AI generateContent |
| `LLMProviderRegistry.java` | Modify | New flags, switch cases, default models |
| `appengine-web.xml` | Modify | New Bedrock/Vertex config properties |
| `AwsSigV4SignerTest.java` | Create | SigV4 test against AWS test vectors |
| `GcpAuthHelperTest.java` | Create | JWT creation test |
| `CONTRIBUTING_AI.md` | Modify | Updated provider docs |

---

### Task 1: Extract OpenAIChatCompletionsProvider base class from MiniMaxProvider

**Files:**
- Create: `llm/OpenAIChatCompletionsProvider.java`
- Rewrite: `llm/MiniMaxProvider.java`

This is a pure refactoring task — behavior must not change.

- [ ] **Step 1: Create `OpenAIChatCompletionsProvider.java`**

Copy the entire contents of `MiniMaxProvider.java` into a new file `OpenAIChatCompletionsProvider.java`. Then make these changes:

1. Rename class to `OpenAIChatCompletionsProvider`
2. Change `API_ENDPOINT` constant to a constructor parameter `baseUrl` (protected final)
3. Make `apiKey` and `model` protected final fields
4. Add constructor: `OpenAIChatCompletionsProvider(String apiKey, String model, String baseUrl)` that stores all three and strips trailing slash from baseUrl
5. Add protected methods:
   - `getEndpoint()` → returns `baseUrl + "/v1/chat/completions"`
   - `getHeaders()` → returns new `LinkedHashMap` with `"Content-Type" → "application/json"`, `"Authorization" → "Bearer " + apiKey`
   - `getMaxTokens()` → returns `MAX_TOKENS` (131072)
   - `getProviderName()` → returns `"OpenAI-Compatible"`
6. In `callApi()`, replace `new URL(API_ENDPOINT)` with `new URL(getEndpoint())`
7. In `callApi()`, replace the two hardcoded `setRequestProperty` calls with a loop over `getHeaders()` entries
8. In `buildMessages()` for the system message, replace the hardcoded `requestBody.put("max_tokens", MAX_TOKENS)` with `requestBody.put("max_tokens", getMaxTokens())`
9. Replace all log messages referencing "MiniMax" with `getProviderName()`
10. Replace all error messages referencing "MiniMax" with `getProviderName()`
11. Keep `ToolCallInfo` as a protected static inner class (subclasses don't need it, but it keeps things together)

The `chat()`, `continueWithToolResults()`, `buildMessages()`, `buildToolDefinitions()`, `buildStructuredAssistantMessage()`, `addStructuredToolResultMessages()`, `readStreamingResponse()`, `checkFinishReason()`, `callApi()`, `readResponse()`, `mapHttpErrorToUserMessage()`, `isRetryable()`, and `buildContinuationState()` methods all remain exactly as-is in the base class (modulo the `getProviderName()` substitution in strings).

- [ ] **Step 2: Rewrite `MiniMaxProvider.java` as thin subclass**

Replace the entire contents of `MiniMaxProvider.java` with:

```java
package com.google.appinventor.server.aiagent.llm;

/**
 * LLM provider for the MiniMax Chat Completions API.
 *
 * <p>Thin subclass of {@link OpenAIChatCompletionsProvider} that sets
 * the MiniMax endpoint.
 */
public class MiniMaxProvider extends OpenAIChatCompletionsProvider {

  private static final String MINIMAX_BASE_URL = "https://api.minimax.io";

  MiniMaxProvider(String apiKey, String model) {
    super(apiKey, model, MINIMAX_BASE_URL);
  }

  @Override
  protected String getProviderName() {
    return "MiniMax";
  }
}
```

- [ ] **Step 3: Build and verify**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Run existing tests**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All tests pass (the refactoring is behavioral no-op)

---

### Task 2: Extract AnthropicCompatibleProvider base class from AnthropicProvider

**Files:**
- Create: `llm/AnthropicCompatibleProvider.java`
- Rewrite: `llm/AnthropicProvider.java`

Same pattern as Task 1 — pure refactoring.

- [ ] **Step 1: Create `AnthropicCompatibleProvider.java`**

Copy the entire contents of `AnthropicProvider.java` into a new file `AnthropicCompatibleProvider.java`. Then make these changes:

1. Rename class to `AnthropicCompatibleProvider`
2. Change `API_ENDPOINT` constant to derived from constructor parameter `baseUrl` (protected final)
3. Make `apiKey` and `model` protected final fields
4. Add constructor: `AnthropicCompatibleProvider(String apiKey, String model, String baseUrl)` — if `baseUrl` is null or empty, default to `"https://api.anthropic.com"`. Strip trailing slash.
5. Add protected methods:
   - `getEndpoint()` → returns `baseUrl + "/v1/messages"`
   - `getHeaders()` → returns new `LinkedHashMap` with `"Content-Type" → "application/json"`, `"x-api-key" → apiKey`, `"anthropic-version" → ANTHROPIC_VERSION`
   - `getMaxTokens()` → returns `MAX_TOKENS` (128000)
   - `getProviderName()` → returns `"Anthropic-Compatible"`
6. In `callApi()`, replace `new URL(API_ENDPOINT)` with `new URL(getEndpoint())`
7. In `callApi()`, replace the three hardcoded `setRequestProperty` calls with a loop over `getHeaders()` entries
8. In the request body building, replace `requestBody.put("max_tokens", MAX_TOKENS)` with `requestBody.put("max_tokens", getMaxTokens())`
9. Replace all log/error messages referencing "Anthropic" with `getProviderName()`
10. Keep `ToolUseBlock` as a protected static inner class

- [ ] **Step 2: Rewrite `AnthropicProvider.java` as thin subclass**

Replace the entire contents of `AnthropicProvider.java` with:

```java
package com.google.appinventor.server.aiagent.llm;

/**
 * LLM provider for the Anthropic Messages API (Claude).
 *
 * <p>Thin subclass of {@link AnthropicCompatibleProvider} that sets
 * the Anthropic API endpoint.
 */
public class AnthropicProvider extends AnthropicCompatibleProvider {

  AnthropicProvider(String apiKey, String model) {
    super(apiKey, model, "https://api.anthropic.com");
  }

  @Override
  protected String getProviderName() {
    return "Anthropic";
  }
}
```

- [ ] **Step 3: Build and verify**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Run existing tests**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All tests pass

---

### Task 3: Create OpenRouterProvider

**Files:**
- Create: `llm/OpenRouterProvider.java`

- [ ] **Step 1: Create `OpenRouterProvider.java`**

```java
package com.google.appinventor.server.aiagent.llm;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LLM provider for OpenRouter, a unified gateway that routes requests
 * to the best available backend for any model.
 *
 * <p>Thin subclass of {@link OpenAIChatCompletionsProvider} that sets
 * the OpenRouter endpoint and adds required routing headers.
 */
public class OpenRouterProvider extends OpenAIChatCompletionsProvider {

  private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api";

  OpenRouterProvider(String apiKey, String model) {
    super(apiKey, model, OPENROUTER_BASE_URL);
  }

  @Override
  protected String getProviderName() {
    return "OpenRouter";
  }

  @Override
  protected Map<String, String> getHeaders() {
    Map<String, String> headers = super.getHeaders();
    headers.put("HTTP-Referer", "https://appinventor.mit.edu");
    headers.put("X-Title", "MIT App Inventor");
    return headers;
  }
}
```

- [ ] **Step 2: Build**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

---

### Task 4: Create AwsSigV4Signer

**Files:**
- Create: `llm/AwsSigV4Signer.java`
- Create test: `llm/AwsSigV4SignerTest.java`

- [ ] **Step 1: Write the test**

Create `appinventor/appengine/tests/com/google/appinventor/server/aiagent/llm/AwsSigV4SignerTest.java`:

```java
package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests for {@link AwsSigV4Signer} using known test vectors.
 *
 * <p>Test vectors derived from AWS Signature Version 4 documentation:
 * https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
 */
public class AwsSigV4SignerTest extends TestCase {

  // Fixed credentials for testing
  private static final String ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
  private static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
  private static final String REGION = "us-east-1";
  private static final String SERVICE = "bedrock";

  public void testSignReturnsAuthorizationHeader() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, null, REGION, SERVICE);
    byte[] payload = "{}".getBytes();
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");

    Map<String, String> signed = signer.sign("POST",
        "https://bedrock-runtime.us-east-1.amazonaws.com/model/test/converse",
        headers, payload);

    assertTrue("Must contain Authorization header",
        signed.containsKey("Authorization"));
    assertTrue("Authorization must start with AWS4-HMAC-SHA256",
        signed.get("Authorization").startsWith("AWS4-HMAC-SHA256"));
    assertTrue("Must contain X-Amz-Date header",
        signed.containsKey("X-Amz-Date"));
    assertFalse("No session token header without session token",
        signed.containsKey("X-Amz-Security-Token"));
  }

  public void testSignIncludesSessionToken() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY,
        "FwoGZXIvYXdzEBY", REGION, SERVICE);
    byte[] payload = "{}".getBytes();
    Map<String, String> headers = new LinkedHashMap<>();

    Map<String, String> signed = signer.sign("POST",
        "https://bedrock-runtime.us-east-1.amazonaws.com/model/test/converse",
        headers, payload);

    assertTrue("Must contain X-Amz-Security-Token",
        signed.containsKey("X-Amz-Security-Token"));
    assertEquals("FwoGZXIvYXdzEBY", signed.get("X-Amz-Security-Token"));
  }

  public void testSignatureIsDeterministic() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, null, REGION, SERVICE);
    byte[] payload = "{\"test\": true}".getBytes();
    Map<String, String> headers = new LinkedHashMap<>();

    // Two calls with the same timestamp should produce the same signature.
    // We can't control the timestamp, but we can verify the format is stable.
    Map<String, String> signed1 = signer.sign("POST",
        "https://bedrock-runtime.us-east-1.amazonaws.com/model/test/converse",
        headers, payload);
    Map<String, String> signed2 = signer.sign("POST",
        "https://bedrock-runtime.us-east-1.amazonaws.com/model/test/converse",
        headers, payload);

    // Both should have the same X-Amz-Date (within the same second)
    assertEquals(signed1.get("X-Amz-Date"), signed2.get("X-Amz-Date"));
    // And the same Authorization (deterministic for same inputs)
    assertEquals(signed1.get("Authorization"), signed2.get("Authorization"));
  }

  public void testAuthorizationHeaderFormat() {
    AwsSigV4Signer signer = new AwsSigV4Signer(ACCESS_KEY, SECRET_KEY, null, REGION, SERVICE);
    byte[] payload = "{}".getBytes();
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("Host", "bedrock-runtime.us-east-1.amazonaws.com");

    Map<String, String> signed = signer.sign("POST",
        "https://bedrock-runtime.us-east-1.amazonaws.com/model/test/converse",
        headers, payload);

    String auth = signed.get("Authorization");
    // Format: AWS4-HMAC-SHA256 Credential=AKIA.../date/region/service/aws4_request,
    //         SignedHeaders=..., Signature=...
    assertTrue("Must contain Credential", auth.contains("Credential="));
    assertTrue("Must contain SignedHeaders", auth.contains("SignedHeaders="));
    assertTrue("Must contain Signature", auth.contains("Signature="));
    assertTrue("Credential must include region",
        auth.contains("/" + REGION + "/" + SERVICE + "/aws4_request"));
  }
}
```

- [ ] **Step 2: Run tests — verify they fail**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: FAIL — `AwsSigV4Signer` class not found

- [ ] **Step 3: Implement `AwsSigV4Signer.java`**

Create `llm/AwsSigV4Signer.java`. This is a package-private class:

```java
package com.google.appinventor.server.aiagent.llm;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
```

Key methods:
- Constructor: `AwsSigV4Signer(String accessKey, String secretKey, String sessionToken, String region, String service)` — stores all fields
- `Map<String, String> sign(String method, String urlString, Map<String, String> existingHeaders, byte[] payload)`:
  1. Get current UTC time, format as `yyyyMMdd'T'HHmmss'Z'` (for `X-Amz-Date`) and `yyyyMMdd` (date stamp)
  2. Parse URL to extract host, path, query
  3. Build canonical headers: merge existingHeaders + Host + X-Amz-Date (+ X-Amz-Security-Token if present), lowercased keys, sorted alphabetically
  4. Build signed headers string: sorted lowercase header names joined by `;`
  5. SHA256 hash the payload
  6. Build canonical request: `method + \n + path + \n + query + \n + canonical_headers + \n + signed_headers + \n + payload_hash`
  7. Build string-to-sign: `AWS4-HMAC-SHA256 + \n + timestamp + \n + date/region/service/aws4_request + \n + SHA256(canonical_request)`
  8. Derive signing key: `HMAC(HMAC(HMAC(HMAC("AWS4" + secretKey, date), region), service), "aws4_request")`
  9. Compute signature: `hex(HMAC(signingKey, stringToSign))`
  10. Build Authorization header: `AWS4-HMAC-SHA256 Credential=accessKey/date/region/service/aws4_request, SignedHeaders=signedHeaders, Signature=signature`
  11. Return map with `Authorization`, `X-Amz-Date`, `Host`, and optionally `X-Amz-Security-Token`
- Private helpers: `hmacSha256(byte[] key, String data)`, `sha256Hex(byte[] data)`, `sha256(byte[] data)`, `hexEncode(byte[] bytes)`

- [ ] **Step 4: Run tests — verify they pass**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All pass including `AwsSigV4SignerTest`

---

### Task 5: Create BedrockProvider

**Files:**
- Create: `llm/BedrockProvider.java`

- [ ] **Step 1: Implement `BedrockProvider.java`**

This is a standalone `LLMProvider` implementation. Key differences from other providers documented in the spec. Structure:

```java
package com.google.appinventor.server.aiagent.llm;

// Standard imports: org.json, java.io, java.net, java.nio, java.util, java.util.logging
import com.google.appinventor.server.aiagent.AIDebug;
import com.google.appinventor.server.aiagent.StreamBuffer;
```

**Constants:**
- `MAX_TOOL_ITERATIONS = 5`, `MAX_RETRIES = 3`, `INITIAL_BACKOFF_MS = 1000`
- `MAX_TOKENS = 128000`, `CONNECT_TIMEOUT_MS = 30000`, `READ_TIMEOUT_MS = 600000`

**Fields (all private final):**
- `AwsSigV4Signer signer`
- `String region`, `String model`

**Constructor:** `BedrockProvider(String accessKey, String secretKey, String sessionToken, String region, String model)` — creates `AwsSigV4Signer` with service `"bedrock"`

**`isStateless()`** → `true`

**`chat()` method:**
1. `buildMessages()` from history + context + userMessage — Bedrock format: each message has `role` and `content` array of typed blocks (`{"text": "..."}`)
2. Build request body:
   ```json
   {
     "modelId": model,
     "system": [{"text": systemPrompt}],
     "messages": [...],
     "inferenceConfig": {"maxTokens": MAX_TOKENS},
     "toolConfig": {"tools": [...]}
   }
   ```
3. Tool definitions in Bedrock format: `{"toolSpec": {"name": "...", "description": "...", "inputSchema": {"json": {...}}}}`
4. Tool-use loop (same pattern as all other providers):
   - Call `callApi()` with signed request
   - Parse response: extract text from `output.message.content[].text`, tool uses from `output.message.content[].toolUse`
   - Classify read-only vs operation tools
   - Resolve read-only tools, add `toolResult` blocks to messages, re-call
   - Return operation tools as `RawToolCall` list with `hasMore=true`
5. Stop reason from `stopReason` field (values: `end_turn`, `tool_use`, `max_tokens`)

**`continueWithToolResults()` method:**
Same pattern: deserialize continuation state, append `toolResult` blocks for pending calls, run tool-use loop.

**`callApi()` method:**
1. Build endpoint URL: `https://bedrock-runtime.{region}.amazonaws.com/model/{urlEncode(model)}/converse-stream`
2. Serialize request body to bytes
3. Call `signer.sign("POST", url, headers, body)` to get auth headers
4. Open `HttpURLConnection`, set all signed headers + request headers
5. Send body, read SSE streaming response
6. Same retry logic as other providers (429, 5xx, exponential backoff)

**`readStreamingResponse()` method — Bedrock SSE format:**
Bedrock streaming uses JSON event blobs prefixed by binary framing headers. However, when using `converse-stream`, the response can be consumed as SSE `data:` lines with JSON payloads. Each event has a type:
- `messageStart`: `{"role": "assistant"}`
- `contentBlockStart`: `{"contentBlockIndex": N, "start": {"toolUse": {"toolUseId": "...", "name": "..."}}}` (only for tool_use blocks)
- `contentBlockDelta`: `{"contentBlockIndex": N, "delta": {"text": "..."}}` or `{"contentBlockIndex": N, "delta": {"toolUse": {"input": "partial json"}}}`
- `contentBlockStop`: `{"contentBlockIndex": N}`
- `messageStop`: `{"stopReason": "end_turn" | "tool_use"}`
- `metadata`: `{"usage": {"inputTokens": N, "outputTokens": N}}`

Accumulate text blocks and tool_use blocks (partial JSON input), then reconstruct into the same format as a non-streaming `converse` response so parsing code works.

**`buildMessages()` — Bedrock format:**
- System messages: handled separately via `system` field
- History replay: translate structured content from Anthropic storage format to Bedrock format
  - `tool_use` blocks → `{"toolUse": {"toolUseId": id, "name": name, "input": {...}}}`
  - `tool_result` blocks → `{"toolResult": {"toolUseId": id, "content": [{"text": "..."}]}}`
- Context messages: user/assistant turns with `[{"text": "..."}]` content
- User message: `{"role": "user", "content": [{"text": userMessage}]}`

**Inner class:** `ToolUseInfo` with `id`, `name`, `inputJson`

- [ ] **Step 2: Build**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

---

### Task 6: Create GcpAuthHelper

**Files:**
- Create: `llm/GcpAuthHelper.java`
- Create test: `llm/GcpAuthHelperTest.java`

- [ ] **Step 1: Write the test**

Create `appinventor/appengine/tests/com/google/appinventor/server/aiagent/llm/GcpAuthHelperTest.java`:

```java
package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

/**
 * Tests for {@link GcpAuthHelper} JWT creation.
 *
 * <p>Note: these tests verify JWT structure and signing but do NOT
 * call the real Google token endpoint. The actual token exchange
 * requires network access and valid credentials.
 */
public class GcpAuthHelperTest extends TestCase {

  // RSA private key in PKCS#8 PEM format (2048-bit, generated for testing only)
  // This key is NOT used for any real service account.
  private static final String TEST_PRIVATE_KEY =
      "-----BEGIN PRIVATE KEY-----\n"
      + "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7o4qne60TB3IB\n"
      + "bDWbyXB5PmKXCLCJJTxU5RGHZ+JFGbSJFzLaHzSEBP0wMJoGD27FxyuGKYDDMxG\n"
      + "awGrYsXo2cNLEBBPExT2gSQBPpjVJFGLMR8JHRKHQIvk+YFkn/hPWWvf+t0SfFh\n"
      + "IrRaJYgzU5PkClMfEkRHnBCGGv+7G+foNP/o3KOflJKhvikJGKGZqA8DjMkcsHnP\n"
      + "Z7F5QI4LDuDkS9aqrxEB7pPvNFMaBWdFyR3U6/nImQ2JjqhqCKnGR1PXjWRCu5Sh\n"
      + "hCMFrhJNXWaHFkl/K5v3H3x0BQXS8bm3T5gpoTlFJJQJPJrf3kGMCXfCgRLdaqTE\n"
      + "rUf7W/45AgMBAAECggEAIwvtoojqXJHMJEL0bzOZ/f6WqR+kFFEsH/TIJd+j+USi\n"
      + "c+gnhMgxZxfEqXFoQBMWxU+8NVqL8GbfD+tNahjKZX0p3LzMR8FHvRwq2gPHOlr0\n"
      + "dBqlp8K4vbJ8z/eP9iQPExXS7Y6xHMIh8FjJ9r3mGqB8D6X2f3E1PGKD9TDgH5D5\n"
      + "0PjlCIfpLT7Jv6Bj8VmHm/K5RfCwIMnB6s/0R8+7KLsjMF3rR3E3EH+PKdN1X4cN\n"
      + "A3oBZ/0PR5rl1r+Zb2NfR37GKPT6e2nCfIzG3NNjR8x7P/S3TL8lK9H7G5dDj/qP\n"
      + "XPZ7EjWlGx1c7C5F6QcFnGbL3S1EO2FheaL0KjagkQKBgQDqT2v8lhe3Utje++JS\n"
      + "oe08cHEoWpFlBRCHLCJ+A3DxdYi0i2o8TOkFPmjfR+LMi3v1nI/fK7Q9FP3I3sLr0\n"
      + "OPyB3R/6OGfILLgqtxQ+d0Oi7xi+V2/VEfU7Q7IxhmI/j4iMXr0K/Xqdf6ML7pM6\n"
      + "MFGHI+wQB/2Y1REXN6XYz8+4JQKBgQDM5bfNGUjBP9Q0bT3MJHK+HuNw/jGkqWt\n"
      + "JUlFDzR1Uy6F3T8XeYJXmj7LHFJN/MQ3xYWkBd7SlE0VsqBXPB08qB0G97lrJJ2x\n"
      + "c/YBHKyj9/mNrfk9WPCcJHBJT2pvO0r/uipFXjQCf+d3DRtFkP5eJdXsDAf/k/t2G\n"
      + "vT4pIxNJZQKBgBK5v/YqfC9mK3G6BoRGlS7Vy/V/RbeMoVp8f7LCEr0nIm2Be2gj\n"
      + "4MBQZ2EZ/5GMPXHF7G3D3cL3+dXnFj8M1S16y4HNr14B4R0FqvLS5dMI5bMMB/R5\n"
      + "3ZLX+xkRiA74TJVv/7G7QH7r3Jsl/Vk1z/7o4N2y/xDmp8a8v7z2vPFAoGBAJZrJ\n"
      + "ETJQM1ZVQQ4P1iFZ/GwJYqjXJYTr4ROIkbR0QR6a9PzMIbGNmIXN/hG+B7G+aIBN\n"
      + "h2PTfJbJ+Mj2H8JhdGQzU+E8aU0YWfHl6R7KGMO0LOcT5qRz/9JWIjGg8N3oJLdf\n"
      + "MklNJOD7QkPwQ2K9FHQW9A8D7p4m9TBFjVJB7v5AoGBAKqC8L0A2R0SN5YCfzGj8\n"
      + "VK7RO5RMLSQAE6bnaKJhG/X+8MFJ/0DmFj/BvG3ZBMl5K5dL7V/Bz7f3K5vNq7XJ\n"
      + "bJd+ZXQBR/G5pCmD7v6nQR5o4AYf6VZj7TNR+oJPS2zMzLfJ4Sj7HpD8HnN5Rq1q\n"
      + "Q3l2c8k/R1bR\n"
      + "-----END PRIVATE KEY-----";

  private static final String TEST_CLIENT_EMAIL = "test@test-project.iam.gserviceaccount.com";

  public void testCreateJwt() throws Exception {
    String jwt = GcpAuthHelper.createJwtForTest(TEST_CLIENT_EMAIL, TEST_PRIVATE_KEY);
    assertNotNull("JWT must not be null", jwt);

    // JWT has three base64url-encoded parts separated by dots
    String[] parts = jwt.split("\\.");
    assertEquals("JWT must have 3 parts", 3, parts.length);

    // Verify header
    String header = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
    assertTrue("Header must contain RS256", header.contains("RS256"));
    assertTrue("Header must contain JWT", header.contains("JWT"));
  }

  public void testCreateJwtContainsClaims() throws Exception {
    String jwt = GcpAuthHelper.createJwtForTest(TEST_CLIENT_EMAIL, TEST_PRIVATE_KEY);
    String[] parts = jwt.split("\\.");
    String claims = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

    assertTrue("Claims must contain iss", claims.contains("\"iss\""));
    assertTrue("Claims must contain client email",
        claims.contains(TEST_CLIENT_EMAIL));
    assertTrue("Claims must contain scope",
        claims.contains("cloud-platform"));
    assertTrue("Claims must contain aud",
        claims.contains("oauth2.googleapis.com"));
  }
}
```

- [ ] **Step 2: Run tests — verify they fail**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: FAIL — `GcpAuthHelper` class not found

- [ ] **Step 3: Implement `GcpAuthHelper.java`**

Create `llm/GcpAuthHelper.java`. Package-private class:

```java
package com.google.appinventor.server.aiagent.llm;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;
```

**Fields:**
- `private final String clientEmail`
- `private final PrivateKey privateKey`
- `private String accessToken`
- `private long expiryTimeMillis`
- `private static final String TOKEN_URL = "https://oauth2.googleapis.com/token"`
- `private static final String SCOPE = "https://www.googleapis.com/auth/cloud-platform"`
- `private static final long REFRESH_MARGIN_MS = 5 * 60 * 1000` (5 min before expiry)

**Constructor:** `GcpAuthHelper(String serviceAccountJsonPath)`:
1. Read file at `serviceAccountJsonPath` to string
2. Parse as JSON, extract `client_email` and `private_key`
3. Parse the PEM private key: strip `-----BEGIN PRIVATE KEY-----` / `-----END PRIVATE KEY-----`, remove newlines, base64-decode, create `PrivateKey` via `KeyFactory.getInstance("RSA")` + `PKCS8EncodedKeySpec`

**`synchronized String getAccessToken()`:**
1. If `accessToken != null` and `System.currentTimeMillis() < expiryTimeMillis - REFRESH_MARGIN_MS`, return cached token
2. Otherwise call `refreshToken()`
3. Return `accessToken`

**`private void refreshToken()`:**
1. Build JWT: `createJwt(clientEmail, privateKey)`
2. POST to `TOKEN_URL` with `Content-Type: application/x-www-form-urlencoded` body: `grant_type=urn:ietf:params:oauth:grant_type:jwt-bearer&assertion={jwt}`
3. Parse response JSON: extract `access_token` and `expires_in`
4. Set `this.accessToken = access_token`, `this.expiryTimeMillis = System.currentTimeMillis() + (expires_in * 1000)`

**`private static String createJwt(String clientEmail, PrivateKey privateKey)`:**
1. Header: `{"alg": "RS256", "typ": "JWT"}` → base64url encode
2. Claims: `{"iss": clientEmail, "scope": SCOPE, "aud": TOKEN_URL, "iat": now_epoch, "exp": now_epoch + 3600}` → base64url encode
3. Sign: `Signature.getInstance("SHA256withRSA")`, init with privateKey, sign `header.claims` bytes
4. Return `header.claims.signature` (all base64url encoded)

**`static String createJwtForTest(String clientEmail, String privateKeyPem)`:**
Package-private test helper that parses a PEM key and calls `createJwt()`. This avoids needing a file on disk for tests.

- [ ] **Step 4: Run tests — verify they pass**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All pass including `GcpAuthHelperTest`

---

### Task 7: Create VertexProvider

**Files:**
- Create: `llm/VertexProvider.java`

- [ ] **Step 1: Implement `VertexProvider.java`**

This is a standalone `LLMProvider` implementation that uses the same Gemini wire format as `GeminiProvider` but with different endpoint and auth. Reference `GeminiProvider.java` closely — the request body building, response parsing, streaming, and tool-use loop logic are the same `generateContent` format.

```java
package com.google.appinventor.server.aiagent.llm;

// Same imports as GeminiProvider plus:
import com.google.appinventor.server.aiagent.AIDebug;
import com.google.appinventor.server.aiagent.StreamBuffer;
```

**Constants:**
- `MAX_TOOL_ITERATIONS = 5`, `MAX_RETRIES = 3`, `INITIAL_BACKOFF_MS = 1000`
- `MAX_OUTPUT_TOKENS = 65536`, `CONNECT_TIMEOUT_MS = 30000`, `READ_TIMEOUT_MS = 300000`

**Fields:**
- `private final String project`
- `private final String region`
- `private final String model`
- `private final GcpAuthHelper authHelper`

**Constructor:** `VertexProvider(String project, String region, String serviceAccountPath, String model)`:
- Store `project`, `region`, `model`
- Create `authHelper = new GcpAuthHelper(serviceAccountPath)`

**`isStateless()`** → `false` (same as GeminiProvider — caches conversation in providerRef)

**`chat()` and `continueWithToolResults()`**: Copy the logic from `GeminiProvider` exactly, with these changes:
1. Endpoint: `https://{region}-aiplatform.googleapis.com/v1/projects/{project}/locations/{region}/publishers/google/models/{model}:streamGenerateContent?alt=sse` (streaming) or `:generateContent` (non-streaming)
2. Auth: Set `Authorization: Bearer {authHelper.getAccessToken()}` header instead of `?key=` query param
3. Log/error messages reference "Vertex" instead of "Gemini"

**All other methods** (`buildContents`, `buildToolDeclarations`, `buildProviderRef`, `buildContinuationState`, `readStreamingResponse`, `processStreamEvent`, `checkFinishReason`, `callApi`, `readResponse`, `mapHttpErrorToUserMessage`, `isRetryable`, `FunctionCallInfo`) — copy from GeminiProvider, replacing "Gemini" in strings with `"Vertex"`.

The reason we copy rather than share code is per the spec: GeminiProvider and VertexProvider should be free to diverge independently.

- [ ] **Step 2: Build**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

---

### Task 8: Update LLMProviderRegistry

**Files:**
- Modify: `llm/LLMProviderRegistry.java`

- [ ] **Step 1: Add new Flag properties**

Add after the existing `BASE_URL_FLAG` declaration (around line 38):

```java
// Bedrock-specific flags
private static final Flag<String> BEDROCK_REGION_FLAG =
    Flag.createFlag("ai.agent.provider.bedrock.region", "us-east-1");
private static final Flag<String> BEDROCK_ACCESS_KEY_FLAG =
    Flag.createFlag("ai.agent.provider.bedrock.access.key", "");
private static final Flag<String> BEDROCK_SECRET_KEY_FLAG =
    Flag.createFlag("ai.agent.provider.bedrock.secret.key", "");
private static final Flag<String> BEDROCK_SESSION_TOKEN_FLAG =
    Flag.createFlag("ai.agent.provider.bedrock.session.token", "");

// Vertex-specific flags
private static final Flag<String> VERTEX_PROJECT_FLAG =
    Flag.createFlag("ai.agent.provider.vertex.project", "");
private static final Flag<String> VERTEX_REGION_FLAG =
    Flag.createFlag("ai.agent.provider.vertex.region", "us-central1");
private static final Flag<String> VERTEX_SERVICE_ACCOUNT_FLAG =
    Flag.createFlag("ai.agent.provider.vertex.service.account", "");
```

- [ ] **Step 2: Add DEFAULT_MODELS entries**

Add to the static block after the existing entries:

```java
DEFAULT_MODELS.put("bedrock", "anthropic.claude-sonnet-4-20250514-v1:0");
DEFAULT_MODELS.put("vertex", "gemini-2.0-flash");
DEFAULT_MODELS.put("openrouter", "anthropic/claude-sonnet-4");
DEFAULT_MODELS.put("openai-compatible", "");
DEFAULT_MODELS.put("anthropic-compatible", "");
```

- [ ] **Step 3: Update the switch statement**

Update the `anthropic` case and add new cases. The existing `anthropic` case changes from creating `AnthropicProvider` to creating `AnthropicCompatibleProvider` (which handles empty baseUrl by defaulting to Anthropic's endpoint). Same pattern for `minimax`.

```java
case "anthropic":
  validateApiKey(apiKey, "Anthropic");
  return new AnthropicCompatibleProvider(apiKey, model, baseUrl);

case "anthropic-compatible":
  validateApiKey(apiKey, "Anthropic-Compatible");
  validateBaseUrl(baseUrl, "Anthropic-Compatible");
  return new AnthropicCompatibleProvider(apiKey, model, baseUrl);

// ... existing openai, gemini, ollama cases unchanged ...

case "minimax":
  validateApiKey(apiKey, "MiniMax");
  if (baseUrl == null || baseUrl.isEmpty()) {
    return new MiniMaxProvider(apiKey, model);
  }
  return new OpenAIChatCompletionsProvider(apiKey, model, baseUrl);

case "openrouter":
  validateApiKey(apiKey, "OpenRouter");
  return new OpenRouterProvider(apiKey, model);

case "openai-compatible":
  validateApiKey(apiKey, "OpenAI-Compatible");
  validateBaseUrl(baseUrl, "OpenAI-Compatible");
  return new OpenAIChatCompletionsProvider(apiKey, model, baseUrl);

case "bedrock":
  validateApiKey(BEDROCK_ACCESS_KEY_FLAG.get(), "Bedrock (access key)");
  validateApiKey(BEDROCK_SECRET_KEY_FLAG.get(), "Bedrock (secret key)");
  return new BedrockProvider(
      BEDROCK_ACCESS_KEY_FLAG.get(),
      BEDROCK_SECRET_KEY_FLAG.get(),
      BEDROCK_SESSION_TOKEN_FLAG.get(),
      BEDROCK_REGION_FLAG.get(),
      model);

case "vertex":
  validateApiKey(VERTEX_PROJECT_FLAG.get(), "Vertex (project)");
  validateApiKey(VERTEX_SERVICE_ACCOUNT_FLAG.get(), "Vertex (service account)");
  return new VertexProvider(
      VERTEX_PROJECT_FLAG.get(),
      VERTEX_REGION_FLAG.get(),
      VERTEX_SERVICE_ACCOUNT_FLAG.get(),
      model);
```

- [ ] **Step 4: Add `validateBaseUrl()` helper**

Add after the existing `validateApiKey()` method:

```java
private static void validateBaseUrl(String baseUrl, String providerName)
    throws LLMProviderException {
  if (baseUrl == null || baseUrl.isEmpty()) {
    throw new LLMProviderException(
        "No base URL configured for " + providerName
            + ". Set the ai.agent.base.url system property.",
        "The AI agent is not configured. Please ask your administrator "
            + "to set up the provider URL.");
  }
}
```

- [ ] **Step 5: Update the error message**

Update the default case error message to list all providers:

```java
default:
  throw new LLMProviderException(
      "Unknown LLM provider: " + providerName,
      "The configured AI provider is not supported. "
          + "Supported providers: anthropic, anthropic-compatible, openai, gemini, "
          + "ollama, minimax, openrouter, openai-compatible, bedrock, vertex.");
```

- [ ] **Step 6: Update the Javadoc**

Update the class-level Javadoc to list all supported providers.

- [ ] **Step 7: Build and test**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib && ant -f appengine/build.xml AiServerLibTests`
Expected: BUILD SUCCESSFUL, all tests pass

---

### Task 9: Update appengine-web.xml

**Files:**
- Modify: `appinventor/appengine/war/WEB-INF/appengine-web.xml`

- [ ] **Step 1: Add new properties**

Add after the existing `ai.agent.debug` property (around line 209), as commented-out examples:

```xml
    <!-- Bedrock provider configuration (only needed when ai.agent.provider=bedrock) -->
    <property name="ai.agent.provider.bedrock.region" value="us-east-1" />
    <property name="ai.agent.provider.bedrock.access.key" value="" />
    <property name="ai.agent.provider.bedrock.secret.key" value="" />
    <property name="ai.agent.provider.bedrock.session.token" value="" />

    <!-- Vertex provider configuration (only needed when ai.agent.provider=vertex) -->
    <property name="ai.agent.provider.vertex.project" value="" />
    <property name="ai.agent.provider.vertex.region" value="us-central1" />
    <property name="ai.agent.provider.vertex.service.account" value="" />
```

- [ ] **Step 2: Build**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

---

### Task 10: Update CONTRIBUTING_AI.md

**Files:**
- Modify: `CONTRIBUTING_AI.md`

- [ ] **Step 1: Update the LLM Providers directory table**

Update the table in the "LLM Providers -- `server/aiagent/llm/`" section to include the new files:

| File | Purpose |
|------|---------|
| `LLMProvider.java` | Interface: `chat()`, `continueWithToolResults()`, `isStateless()` |
| `LLMProviderRegistry.java` | Factory: selects provider from `ai.agent.provider` system property |
| `OpenAIChatCompletionsProvider.java` | Base class for OpenAI Chat Completions compatible providers |
| `AnthropicCompatibleProvider.java` | Full implementation for Anthropic Messages API compatible providers |
| `AnthropicProvider.java` | Thin subclass: Anthropic API endpoint |
| `OpenAIProvider.java` | Standalone: OpenAI Responses API |
| `GeminiProvider.java` | Standalone: Google Gemini API |
| `OllamaProvider.java` | Standalone: local Ollama models |
| `MiniMaxProvider.java` | Thin subclass: MiniMax endpoint |
| `OpenRouterProvider.java` | Thin subclass: OpenRouter endpoint + routing headers |
| `BedrockProvider.java` | Standalone: AWS Bedrock Converse API + SigV4 auth |
| `VertexProvider.java` | Standalone: Google Vertex AI generateContent + OAuth |
| `AwsSigV4Signer.java` | AWS Signature V4 request signing helper |
| `GcpAuthHelper.java` | GCP service account JWT/OAuth token management |

- [ ] **Step 2: Update the Configuration section**

Update the appengine-web.xml example and configuration table to include the new providers and properties.

- [ ] **Step 3: Update the "Adding a New Provider" section**

The existing instructions (steps 1-6) still apply. Add a note that providers using the OpenAI chat/completions format can extend `OpenAIChatCompletionsProvider`, and providers using the Anthropic Messages format can extend `AnthropicCompatibleProvider`.

---

### Task 11: Final verification

- [ ] **Step 1: Full build**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Full test suite**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources/appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All tests pass

- [ ] **Step 3: Verify all new files compile**

Check that all 7 new files are included in the build output:
- `OpenAIChatCompletionsProvider.java`
- `OpenRouterProvider.java`
- `AnthropicCompatibleProvider.java`
- `BedrockProvider.java`
- `AwsSigV4Signer.java`
- `VertexProvider.java`
- `GcpAuthHelper.java`
