# Task 03: Server Service Core

## Status: Not Started

## Plan Reference
Steps 3, 10e from plan.md

## Files to Create
- `server/aiagent/AIAgentServiceImpl.java` — Main service extending OdeRemoteServiceServlet

Path relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
- `server/storage/StoredData.java` — Add ConversationMessageData entity
- `server/storage/ObjectifyStorageIo.java` — Register ConversationMessageData entity

## Dependencies
- Task 01: DTO types (AIAgentRequest, AIAgentResponse, AIOperation, AIConversationMessage)
- Task 02: LLMProvider interface and LLMProviderRegistry
- Task 04: PseudocodeParser (for block operation XML generation)
- Task 06: AIContextBuilder (for building system prompt)
- Task 12: LLMResponseParser + AIOperationValidator (for validation pipeline)

## Acceptance Criteria
- [ ] AIAgentServiceImpl extends OdeRemoteServiceServlet
- [ ] processRequest() flow: auth check → conversation lookup → context build → LLM call → validation → retry → response
- [ ] Conversation management: Memcache maps projectId → provider:conversationId:providerRef with 24h TTL
- [ ] Conversation lifecycle: lazy creation, continuation, expiry (24h), explicit clear
- [ ] Provider change detection: if configured provider differs from cached, clear old conversation
- [ ] Stateless providers (Anthropic, Ollama): load history from Datastore for LLM context
- [ ] Stateful providers (OpenAI, Gemini): pass providerRef, don't send Datastore history
- [ ] ConversationMessageData entity: stores all messages in Datastore (both providers)
- [ ] All 4 RPC methods implemented: processRequest, clearConversation, getConversationHistory, getRequestStatus
- [ ] Rate limiting: ConcurrentHashMap<String, RateLimiter>, 10 req/min default
- [ ] Input validation: max 2000 char message length, sanitize control characters
- [ ] Progress status: updateStatus/clearStatus via Memcache, polled by getRequestStatus
- [ ] ReadOnlyToolResolver: resolves lookup_component (from component DB) and lookup_screen (from storageIo)
- [ ] Validation pipeline: Stage 1 (structural) → Stage 2 (mode + semantic) → one retry on failure
- [ ] Project ownership: storageIo.assertUserHasProject() before any operation
- [ ] Advisor belt-and-suspenders: strip any surviving write ops in Advisor mode
- [ ] Error response: zero operations + error list when validation fails after retry

## Progress Log
