# LLM Provider Expansion: Bedrock, Vertex, OpenRouter & Compatible Providers

**Date**: 2026-04-03
**Branch**: ai-agent
**Status**: Approved

---

## Summary

Add 5 new LLM provider options to the AI Agent: Amazon Bedrock, Google Vertex AI, OpenRouter, and generic OpenAI-compatible / Anthropic-compatible endpoints. Simultaneously refactor `MiniMaxProvider` and `AnthropicProvider` into base-class/subclass hierarchies to eliminate duplication across providers sharing the same wire format.

**No new JAR dependencies.** All authentication (AWS SigV4, GCP service account JWT/OAuth2) is implemented from scratch using JDK builtins (`javax.crypto`, `java.security`, `HttpURLConnection`).

---

## Architecture

### Provider Hierarchy

```
LLMProvider (interface)
  |
  +-- OpenAIChatCompletionsProvider (base class, configurable URL)
  |     +-- MiniMaxProvider (thin: sets MiniMax endpoint)
  |     +-- OpenRouterProvider (thin: sets OpenRouter endpoint + extra headers)
  |
  +-- AnthropicCompatibleProvider (full implementation, configurable URL)
  |     +-- AnthropicProvider (thin: sets Anthropic endpoint)
  |
  +-- BedrockProvider (standalone: Converse API + SigV4)
  +-- VertexProvider (standalone: generateContent + JWT/OAuth)
  +-- OpenAIProvider (standalone, unchanged: Responses API)
  +-- GeminiProvider (standalone, unchanged)
  +-- OllamaProvider (standalone, unchanged)
```

### Registry Mapping

| `ai.agent.provider` | Class | Default base URL | `base.url` behavior |
|---|---|---|---|
| `anthropic` | `AnthropicCompatibleProvider` | `https://api.anthropic.com` | Overrides if set |
| `anthropic-compatible` | `AnthropicCompatibleProvider` | *required* | Must be set |
| `openai` | `OpenAIProvider` | `https://api.openai.com` | Unchanged |
| `gemini` | `GeminiProvider` | `https://generativelanguage.googleapis.com` | Unchanged |
| `ollama` | `OllamaProvider` | `http://localhost:11434` | Overrides if set |
| `minimax` | `OpenAIChatCompletionsProvider` | `https://api.minimax.io` | Overrides if set |
| `openrouter` | `OpenRouterProvider` | `https://openrouter.ai/api` | Ignored |
| `openai-compatible` | `OpenAIChatCompletionsProvider` | *required* | Must be set |
| `bedrock` | `BedrockProvider` | Derived from region | N/A |
| `vertex` | `VertexProvider` | Derived from project/region | N/A |

---

## Component Designs

### 1. OpenAIChatCompletionsProvider (base class)

**Extracted from**: `MiniMaxProvider`

Contains the full OpenAI Chat Completions wire format implementation:
- `buildMessages()` — system/history/context/user message assembly
- `buildToolDefinitions()` — `{type: "function", function: {name, description, parameters}}`
- `buildStructuredAssistantMessage()` / `addStructuredToolResultMessages()` — history replay
- `callApi()` — HTTP POST with retry/backoff (3 retries, exponential backoff from 1s)
- `readStreamingResponse()` — SSE with `[DONE]` sentinel, incremental tool call accumulation
- Tool-use loop — read-only classification, resolve, continue (max 5 iterations)
- `continueWithToolResults()` — continuation state with `messages` + `pendingToolCalls`
- `checkFinishReason()` — throws on `finish_reason: "length"`

**Protected extension points:**
- `getEndpoint()` — returns the full API URL for the chat completions endpoint
- `getHeaders()` — returns `Map<String, String>` of request headers. Default: `{"Authorization": "Bearer {apiKey}", "Content-Type": "application/json"}`
- `getMaxTokens()` — default 131072
- `getProviderName()` — for log/error messages (default: `"OpenAI-Compatible"`)

**Constructor:** `OpenAIChatCompletionsProvider(String apiKey, String model, String baseUrl)`
- `baseUrl` is stored and used by `getEndpoint()` to derive `{baseUrl}/v1/chat/completions`
- All three fields are `protected final`

**Stateless:** `true`

### 2. MiniMaxProvider (thin subclass)

```java
public class MiniMaxProvider extends OpenAIChatCompletionsProvider {
  MiniMaxProvider(String apiKey, String model) {
    super(apiKey, model, "https://api.minimax.io");
  }
  @Override protected String getProviderName() { return "MiniMax"; }
}
```

### 3. OpenRouterProvider (thin subclass)

```java
public class OpenRouterProvider extends OpenAIChatCompletionsProvider {
  OpenRouterProvider(String apiKey, String model) {
    super(apiKey, model, "https://openrouter.ai/api");
  }
  @Override protected String getProviderName() { return "OpenRouter"; }
  @Override protected Map<String, String> getHeaders() {
    Map<String, String> headers = super.getHeaders();
    headers.put("HTTP-Referer", "https://appinventor.mit.edu");
    headers.put("X-Title", "MIT App Inventor");
    return headers;
  }
}
```

### 4. AnthropicCompatibleProvider (full implementation)

**Moved from**: `AnthropicProvider` (all code moves here)

Contains the full Anthropic Messages API implementation:
- `buildMessages()` — history replay with structured content blocks (`tool_use`, `tool_result`)
- `buildToolDefinitions()` — `{name, description, input_schema}`
- `callApi()` — HTTP POST with retry/backoff, SSE streaming
- `readStreamingResponse()` — Anthropic SSE events (`message_start`, `content_block_start`, `content_block_delta`, `message_delta`, `message_stop`)
- Tool-use loop with `ToolUseBlock` classification
- `continueWithToolResults()` — continuation state with `messages` + `systemPrompt` + `pendingToolUseIds`
- `checkStopReason()` — throws on `stop_reason: "max_tokens"`

**Protected extension points:**
- `getEndpoint()` — returns `{baseUrl}/v1/messages`
- `getHeaders()` — returns `{"x-api-key": apiKey, "anthropic-version": "2023-06-01", "Content-Type": "application/json"}`
- `getMaxTokens()` — default 128000
- `getProviderName()` — default `"Anthropic-Compatible"`

**Constructor:** `AnthropicCompatibleProvider(String apiKey, String model, String baseUrl)`
- `baseUrl` defaults to `https://api.anthropic.com` if empty/null

**Stateless:** `true`

### 5. AnthropicProvider (thin subclass)

```java
public class AnthropicProvider extends AnthropicCompatibleProvider {
  AnthropicProvider(String apiKey, String model) {
    super(apiKey, model, "https://api.anthropic.com");
  }
  @Override protected String getProviderName() { return "Anthropic"; }
}
```

### 6. BedrockProvider (standalone)

**API**: AWS Bedrock Converse API
**Endpoint**: `https://bedrock-runtime.{region}.amazonaws.com/model/{modelId}/converse-stream`

**Request format** (Bedrock Converse):
```json
{
  "system": [{"text": "..."}],
  "messages": [
    {"role": "user", "content": [{"text": "..."}]},
    {"role": "assistant", "content": [{"text": "..."}, {"toolUse": {"toolUseId": "...", "name": "...", "input": {...}}}]},
    {"role": "user", "content": [{"toolResult": {"toolUseId": "...", "content": [{"text": "..."}]}}]}
  ],
  "inferenceConfig": {"maxTokens": 128000},
  "toolConfig": {"tools": [{"toolSpec": {"name": "...", "description": "...", "inputSchema": {"json": {...}}}}]}
}
```

**Streaming**: SSE with event types `messageStart`, `contentBlockStart`, `contentBlockDelta`, `contentBlockStop`, `messageStop`, `metadata`. Text deltas in `contentBlockDelta` with `delta.text`. Tool use input in `contentBlockDelta` with `delta.toolUse.input` (partial JSON).

**Auth**: AWS Signature V4 via `AwsSigV4Signer` helper (see below).

**Constructor**: `BedrockProvider(String accessKey, String secretKey, String sessionToken, String region, String model)`

**Stateless**: `true`

**Key differences from other providers:**
- Content is structured as arrays of typed blocks (`{"text": "..."}`, `{"toolUse": {...}}`, `{"toolResult": {...}}`)
- Tool definitions use `toolSpec` with `inputSchema.json`
- Stop reason: `stopReason` field in response metadata (values: `end_turn`, `tool_use`, `max_tokens`)
- History replay translates structured content from Anthropic's format to Bedrock's format

### 7. AwsSigV4Signer (helper)

Package-private utility class for AWS Signature V4 request signing.

**Input**: HTTP method, URL, headers map, request body bytes, access key, secret key, region, service name, optional session token.

**Algorithm**:
1. Build canonical request: `{method}\n{path}\n{query}\n{canonical_headers}\n{signed_headers}\n{SHA256(payload)}`
2. Build string-to-sign: `AWS4-HMAC-SHA256\n{ISO8601 timestamp}\n{date}/{region}/{service}/aws4_request\n{SHA256(canonical_request)}`
3. Derive signing key: chain of HMAC-SHA256 — `HMAC(HMAC(HMAC(HMAC("AWS4"+secretKey, date), region), service), "aws4_request")`
4. Compute signature: `Hex(HMAC(signing_key, string_to_sign))`
5. Return `Authorization` header value + `X-Amz-Date` header + optional `X-Amz-Security-Token`

**Dependencies**: `javax.crypto.Mac`, `javax.crypto.spec.SecretKeySpec`, `java.security.MessageDigest` — all JDK builtins.

### 8. VertexProvider (standalone)

**API**: Vertex AI Gemini API (same wire format as `GeminiProvider`)
**Endpoint**: `https://{region}-aiplatform.googleapis.com/v1/projects/{project}/locations/{region}/publishers/google/models/{model}:streamGenerateContent?alt=sse`

**Request/response format**: Identical to `GeminiProvider` — `contents`, `systemInstruction`, `tools[{functionDeclarations}]`, `generationConfig: {maxOutputTokens: 65536}`.

**Streaming**: Same as `GeminiProvider` — SSE or JSON array with fallback.

**Auth**: OAuth2 bearer token obtained from service account JWT exchange via `GcpAuthHelper`.

**Constructor**: `VertexProvider(String project, String region, String serviceAccountPath, String model)`

**Stateless**: `false` (same as `GeminiProvider` — caches conversation contents in `providerRef`)

**Key differences from GeminiProvider:**
- Endpoint uses project/region path instead of simple model path
- Auth is `Authorization: Bearer {token}` instead of `?key=` query param
- No API key needed — token is obtained from service account

### 9. GcpAuthHelper (helper)

Package-private utility class for Google Cloud service account authentication.

**Token acquisition flow:**
1. Read service account JSON file → extract `client_email` and `private_key`
2. Strip PEM headers/footers from `private_key`, base64-decode to raw PKCS8 bytes
3. Build JWT header: `{"alg": "RS256", "typ": "JWT"}`
4. Build JWT claims: `{"iss": client_email, "scope": "https://www.googleapis.com/auth/cloud-platform", "aud": "https://oauth2.googleapis.com/token", "iat": now_epoch, "exp": now_epoch + 3600}`
5. Sign: `Base64URL(header) + "." + Base64URL(claims)` → SHA256withRSA using `java.security.Signature`
6. POST to `https://oauth2.googleapis.com/token` with `grant_type=urn:ietf:params:oauth:grant_type:jwt-bearer&assertion={signed_jwt}`
7. Parse response: `{"access_token": "ya29...", "token_type": "Bearer", "expires_in": 3600}`

**Token caching:**
- Store `accessToken` + `expiryTimeMillis` as instance fields
- `getAccessToken()` checks if current token expires within 5 minutes; if so, refreshes
- Thread-safe via `synchronized` on the refresh method
- Service account JSON is parsed once in the constructor, private key is cached

**Dependencies**: `java.security.KeyFactory`, `java.security.spec.PKCS8EncodedKeySpec`, `java.security.Signature`, `java.util.Base64`, `HttpURLConnection` — all JDK builtins.

---

## Configuration

### New Flag Properties in LLMProviderRegistry

```java
// Bedrock
Flag<String> BEDROCK_REGION_FLAG       = Flag.createFlag("ai.agent.provider.bedrock.region", "us-east-1");
Flag<String> BEDROCK_ACCESS_KEY_FLAG   = Flag.createFlag("ai.agent.provider.bedrock.access.key", "");
Flag<String> BEDROCK_SECRET_KEY_FLAG   = Flag.createFlag("ai.agent.provider.bedrock.secret.key", "");
Flag<String> BEDROCK_SESSION_TOKEN_FLAG = Flag.createFlag("ai.agent.provider.bedrock.session.token", "");

// Vertex
Flag<String> VERTEX_PROJECT_FLAG         = Flag.createFlag("ai.agent.provider.vertex.project", "");
Flag<String> VERTEX_REGION_FLAG          = Flag.createFlag("ai.agent.provider.vertex.region", "us-central1");
Flag<String> VERTEX_SERVICE_ACCOUNT_FLAG = Flag.createFlag("ai.agent.provider.vertex.service.account", "");
```

### DEFAULT_MODELS Additions

```java
DEFAULT_MODELS.put("bedrock", "anthropic.claude-sonnet-4-20250514-v1:0");
DEFAULT_MODELS.put("vertex", "gemini-2.0-flash");
DEFAULT_MODELS.put("openrouter", "anthropic/claude-sonnet-4");
DEFAULT_MODELS.put("openai-compatible", "");
DEFAULT_MODELS.put("anthropic-compatible", "");
```

### Registry Switch Cases

| Case | Validation | Instantiation |
|---|---|---|
| `anthropic` | API key | `new AnthropicCompatibleProvider(apiKey, model, baseUrl)` — `baseUrl` empty defaults internally |
| `anthropic-compatible` | API key + base URL | `new AnthropicCompatibleProvider(apiKey, model, baseUrl)` |
| `minimax` | API key | `new OpenAIChatCompletionsProvider(apiKey, model, baseUrl)` — defaults to MiniMax URL if empty. `getProviderName()` returns `"MiniMax"` via anonymous subclass or constructor param |
| `openrouter` | API key | `new OpenRouterProvider(apiKey, model)` |
| `openai-compatible` | API key + base URL | `new OpenAIChatCompletionsProvider(apiKey, model, baseUrl)` |
| `bedrock` | Access key + secret key | `new BedrockProvider(accessKey, secretKey, sessionToken, region, model)` |
| `vertex` | Project + service account | `new VertexProvider(project, region, serviceAccountPath, model)` |

### appengine-web.xml

New properties added with empty defaults inside XML comments showing usage examples:

```xml
<!-- Bedrock provider configuration (only needed if ai.agent.provider=bedrock) -->
<!-- <property name="ai.agent.provider.bedrock.region" value="us-east-1" /> -->
<!-- <property name="ai.agent.provider.bedrock.access.key" value="" /> -->
<!-- <property name="ai.agent.provider.bedrock.secret.key" value="" /> -->
<!-- <property name="ai.agent.provider.bedrock.session.token" value="" /> -->

<!-- Vertex provider configuration (only needed if ai.agent.provider=vertex) -->
<!-- <property name="ai.agent.provider.vertex.project" value="" /> -->
<!-- <property name="ai.agent.provider.vertex.region" value="us-central1" /> -->
<!-- <property name="ai.agent.provider.vertex.service.account" value="" /> -->
```

---

## File Inventory

### New Files (all in `server/aiagent/llm/`)

| File | Lines (est.) | Purpose |
|---|---|---|
| `OpenAIChatCompletionsProvider.java` | ~700 | Base class — full chat/completions implementation |
| `OpenRouterProvider.java` | ~30 | Thin subclass — OpenRouter endpoint + headers |
| `AnthropicCompatibleProvider.java` | ~750 | Full Anthropic Messages implementation |
| `BedrockProvider.java` | ~500 | Standalone — Converse API |
| `AwsSigV4Signer.java` | ~250 | AWS Signature V4 signing |
| `VertexProvider.java` | ~500 | Standalone — Vertex AI generateContent |
| `GcpAuthHelper.java` | ~150 | Service account JWT/OAuth token management |

### Modified Files

| File | Changes |
|---|---|
| `LLMProviderRegistry.java` | New Flags, DEFAULT_MODELS entries, switch cases, `validateBaseUrl()` |
| `AnthropicProvider.java` | Gutted to thin subclass of `AnthropicCompatibleProvider` |
| `MiniMaxProvider.java` | Gutted to thin subclass of `OpenAIChatCompletionsProvider` |
| `appengine-web.xml` | New Bedrock/Vertex properties (commented out) |
| `CONTRIBUTING_AI.md` | Updated provider table and directory listing |

### Unchanged Files

| File | Why |
|---|---|
| `LLMProvider.java` | Interface unchanged |
| `OpenAIProvider.java` | Different API (Responses), standalone |
| `GeminiProvider.java` | Standalone, Vertex is independent |
| `OllamaProvider.java` | Standalone |
| All DTOs (`LLMResponse`, `ChatMessage`, etc.) | No changes needed |
| All client code | Providers are server-side only |

---

## Testing

- **Base class refactoring**: Verify that `anthropic` and `minimax` providers still work identically after extraction by running `AiServerLibTests` and manual testing.
- **SigV4 signing**: Unit test `AwsSigV4Signer` against AWS's published test vectors (documented in AWS docs).
- **GCP auth**: Unit test `GcpAuthHelper` JWT creation with a test service account key, mock the token endpoint.
- **New providers**: Each should have a unit test that mocks HTTP calls and verifies correct request format, headers, and auth.
- **No real API calls in tests** — all HTTP interactions mocked.
