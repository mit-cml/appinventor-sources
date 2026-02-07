# Task 07: Chat Dialog UI

## Status: Not Started

## Plan Reference
Step 5 from plan.md

## Files to Create
- `client/editor/youngandroid/AIChatDialog.java` — Floating draggable/resizable chat dialog

Path relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
- `client/Ode.java` — Add AIChatDialog field, lazy initialization, toggleAIChatDialog() method, two-layer enablement check
- `client/OdeMessages.java` — Add ~30 i18n message definitions (see Steps 5, 10d, 10e in plan)
- `client/style/neo/DesignToolbarNeo.java` — Add "AI Assistant" toolbar button that calls Ode.getInstance().toggleAIChatDialog()

## Dependencies
- Task 01: AIAgentService/Async interfaces for RPC calls
- Task 10: AIAgentMode property (toolbar button visibility depends on mode and ai.agent.available config)

## Acceptance Criteria

### Dialog structure
- [ ] Extends GWT DialogBox (draggable)
- [ ] ScrollPanel with FlowPanel chatHistory — user messages right-aligned, AI messages left-aligned
- [ ] FlowPanel operationPreview — human-readable operation summary
- [ ] TextArea inputArea + Button sendButton — user input
- [ ] Button applyButton / Button rejectButton — confirm or discard operations
- [ ] Resizable via CSS resize: both
- [ ] Remembers position/size across open/close via Ode user settings

### RPC integration
- [ ] Creates AIAgentServiceAsync via GWT.create()
- [ ] Configures 2-minute RPC timeout via RpcRequestBuilder
- [ ] sendMessage(): sends AIAgentRequest, disables input while waiting

### Polling progress feedback
- [ ] Timer polls getRequestStatus() every 1 second while processRequest is in flight
- [ ] Status indicator: pulsing text label below user's message (CSS animation)
- [ ] Text updates from server status messages (e.g., "Looking up Button...")
- [ ] Stops polling on success or failure

### Conversation management
- [ ] loadExistingConversation(): calls getConversationHistory on dialog open/show
- [ ] Populates chat with restored messages if non-empty
- [ ] "New Conversation" button: calls clearConversation RPC, clears dialog
- [ ] Handles isNewConversation flag: shows expiry/expired notices

### Operation preview
- [ ] Shows human-readable summary of operations (add, set, delete, etc.)
- [ ] Apply button: triggers AIOperationExecutor
- [ ] Reject button: discards operations, sends feedback to AI
- [ ] Error display: shows failed/skipped operations with error details
- [ ] Validation error display: shows server-side errors with hint to rephrase

### Mode selection
- [ ] When AI mode is Off and user clicks toolbar button, show mode selection dialog
- [ ] Mode choices: Advisor, Screen Editor, Project Editor with descriptions
- [ ] Warning about destructive changes
- [ ] Sets AIAgentMode on Screen1 and opens chat

### Toolbar integration
- [ ] "AI Assistant" button in DesignToolbarNeo
- [ ] Button visible only when Ode.getSystemConfig().getAiAgentAvailable() is true
- [ ] Button state reflects current AIAgentMode (hidden/greyed when Off)

## Progress Log
