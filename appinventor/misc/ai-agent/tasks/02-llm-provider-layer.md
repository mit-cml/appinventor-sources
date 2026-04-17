# Task 02: LLM Provider Layer

## Status: Not Started

## Plan Reference
Step 2 from plan.md

## Files to Create
- `server/aiagent/llm/LLMProvider.java` — Provider interface: chat(systemPrompt, userMessage, tools, providerRef, history, resolver)
- `server/aiagent/llm/LLMProviderException.java` — Checked exception for LLM API failures with getUserFacingMessage()
- `server/aiagent/llm/ReadOnlyToolResolver.java` — Callback interface for resolving read-only tools (isReadOnly, resolve)
- `server/aiagent/llm/ReadOnlyToolException.java` — Checked exception for invalid read-only tool calls
- `server/aiagent/llm/LLMProviderRegistry.java` — Factory that selects provider based on config
- `server/aiagent/llm/AnthropicProvider.java` — Claude API (Messages API with tool_use), stateless
- `server/aiagent/llm/OpenAIProvider.java` — OpenAI Chat Completions with function calling, stateful (previous_response_id)
- `server/aiagent/llm/GeminiProvider.java` — Gemini function calling, stateful (previous_interaction_id)
- `server/aiagent/llm/OllamaProvider.java` — Ollama /api/chat with tool calling, stateless
- `server/aiagent/llm/LLMTool.java` — Tool definition (name, description, parameter schema)
- `server/aiagent/llm/LLMResponse.java` — Provider response (text, raw tool calls, providerRef)
- `server/aiagent/llm/ChatMessage.java` — Role + text pair for conversation history
- `server/aiagent/llm/RawToolCall.java` — Unparsed tool call from provider (name, arguments JSON string)

All paths relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
- Task 01: Uses AIOperation types for mapping tool calls to operations

## Acceptance Criteria
- [ ] LLMProvider interface compiles with chat() method signature
- [ ] Each provider (Anthropic, OpenAI, Gemini, Ollama) implements LLMProvider
- [ ] Each provider translates common LLMTool definitions into its own native format
- [ ] Each provider runs the internal tool-use loop: call LLM → check for read-only tools → resolve via ReadOnlyToolResolver → re-call LLM (max 5 iterations)
- [ ] Mixed responses (read-only + operation tool calls) handled: resolve read-only, re-call LLM
- [ ] LLMProviderRegistry.get(providerName) returns correct provider instance
- [ ] isStateless() returns true for Anthropic/Ollama, false for OpenAI/Gemini
- [ ] LLMProviderException wraps API errors with sanitized user-facing messages (no API keys or stack traces)
- [ ] ReadOnlyToolException message is suitable for injection as tool result
- [ ] OllamaProvider configurable base URL via ai.agent.base.url, optional API key
- [ ] Safety limit: max 5 tool-use loop iterations per chat() call

## Progress Log
