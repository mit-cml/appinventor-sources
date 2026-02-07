# Task 01: Shared DTOs & RPC Interfaces

## Status: Not Started

## Plan Reference
Step 1 from plan.md

## Files to Create
- `shared/rpc/aiagent/AIAgentService.java` — GWT-RPC service interface with processRequest, clearConversation, getConversationHistory, getRequestStatus methods
- `shared/rpc/aiagent/AIAgentServiceAsync.java` — Async interface (required by GWT-RPC)
- `shared/rpc/aiagent/AIAgentRequest.java` — Request DTO (userMessage, projectId, screenName)
- `shared/rpc/aiagent/AIAgentResponse.java` — Response DTO (aiMessage, operations, isNewConversation, errors)
- `shared/rpc/aiagent/AIOperation.java` — Single operation: type enum + JSON payload string
- `shared/rpc/aiagent/AIConversationMessage.java` — Lightweight DTO for conversation history (role + text)

All paths relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
None — this is the foundation task that everything else depends on.

## Acceptance Criteria
- [ ] All 6 Java files compile with the GWT compiler
- [ ] AIAgentService declares: processRequest, clearConversation, getConversationHistory, getRequestStatus
- [ ] AIAgentServiceAsync mirrors each method with AsyncCallback parameter
- [ ] AIAgentRequest has fields: userMessage (String), projectId (long), screenName (String) with getters/setters
- [ ] AIAgentResponse has fields: aiMessage (String), operations (List<AIOperation>), isNewConversation (boolean), errors (List<String>)
- [ ] AIOperation has type enum (ADD_COMPONENT, DELETE_COMPONENT, SET_PROPERTY, RENAME_COMPONENT, SET_EVENT_HANDLER, DELETE_EVENT_HANDLER, SET_VARIABLE, DELETE_VARIABLE, SET_PROCEDURE, DELETE_PROCEDURE, SWITCH_SCREEN, CREATE_SCREEN, DELETE_SCREEN, SET_PROJECT_PROP) and payload (String, JSON)
- [ ] AIConversationMessage has role (String) and text (String)
- [ ] All DTOs implement Serializable (GWT-RPC requirement)
- [ ] @RemoteServiceRelativePath("aiagent") annotation on AIAgentService

## Progress Log
