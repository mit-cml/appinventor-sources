# AI Agent for App Inventor -- Educator Guide

## What is the AI Agent?

The AI Agent is a built-in assistant that helps users create mobile apps within App Inventor using natural language. Instead of manually dragging components and snapping blocks together, users can describe what they want in plain English (e.g., "Create a counter app with a button and a label"), and the AI will propose and apply the changes for them.

The AI understands the full context of the current project -- which components are on the screen, what blocks have been written, what screens exist -- and uses that knowledge to make informed changes. It works directly inside the App Inventor editor, modifying the same project the user sees on screen.

This is not a separate code generator. The AI operates within the editor itself, and every change it proposes is shown to the user for approval before being applied.

---

## How to Access the AI Agent

### The AI Button

When the feature is enabled on the server, an AI button (robot icon) appears in the design toolbar at the top of the editor. Clicking it opens a floating chat dialog.

### First-Time Setup: Choosing a Mode

The first time a user opens the AI chat in a project, a mode selection dialog appears. The user must choose one of three permission levels before they can start chatting:

| Mode | What the AI Can Do | Best For |
|------|-------------------|----------|
| **Advisor** | Answer questions and give suggestions. Cannot make any changes to the project. | Learning, exploring concepts, getting help without risk of unintended modifications |
| **Screen Editor** | Everything in Advisor, plus modify the current screen -- add/remove components, change properties, write/delete blocks | Focused work on a single screen with AI assistance |
| **Project Editor** | Everything in Screen Editor, plus create/delete screens, switch between screens, and change project-level settings (app name, theme, colors, etc.) | Building multi-screen apps with comprehensive AI help |

This mode is saved as a project property and can be changed later from the Project Properties dialog (under the "AI" category).

---

## The Chat Interface

The AI chat dialog is a floating, resizable window that stays open while the user works. It includes:

- **Chat history**: Shows the full conversation between the user and the AI, with messages formatted in Markdown (supporting code blocks, lists, bold text, etc.)
- **Status indicator**: Shows what the AI is currently doing ("Calling AI...", "Building context...", "Parsing response...")
- **Mode indicator**: Displays the current permission level (Advisor, Screen Editor, or Project Editor)
- **Text input**: Where the user types their request. Pressing Enter sends the message.
- **New Conversation button**: Clears the current conversation and starts fresh
- **Close button**: Hides the dialog (the conversation is preserved)

Conversations are remembered per project. If the user reloads the page, the chat history is restored. Conversations automatically expire after 24 hours of inactivity.

---

## How a Request Works (Step by Step)

Understanding the request lifecycle is useful for teaching students what happens "behind the scenes":

### 1. The user types a message

For example: "Add a button called ClickMe and make it say Hello World"

### 2. The system collects context

Before sending the message to the AI, the system automatically gathers a snapshot of the current project state:

- Which screen is open
- What components exist on that screen (and their properties)
- What blocks (code) exist on that screen
- What other screens exist in the project
- What assets (images, sounds) are uploaded
- Whether the user is in the Designer view or Blocks view
- Any warnings or errors in the current blocks

This context allows the AI to make changes that fit the existing project rather than working in a vacuum.

### 3. The AI processes the request

The message and context are sent to a large language model (LLM). The AI is given detailed knowledge about App Inventor's component system, property names, block syntax, and the rules for making valid changes. It uses this knowledge to plan a set of operations.

### 4. Operations are proposed (not immediately applied)

The AI returns two things:

- **A natural language explanation** of what it plans to do
- **A list of structured operations** -- specific, concrete changes like "add a Button named ClickMe" or "set the Text property of ClickMe to Hello World"

These operations are displayed in a **preview panel** before anything changes:

- Green items represent additions (new components, new blocks, new screens)
- Red items represent deletions
- Blue items represent modifications (property changes, renames)

### 5. The user reviews and decides

The user sees three buttons:

- **Apply**: Execute this batch of proposed changes
- **Apply & Accept All**: Execute this batch and automatically apply any subsequent batches (useful for complex requests that span multiple steps)
- **Reject**: Discard the proposal. The AI will acknowledge the rejection and can try a different approach

### 6. Changes are applied in the editor

If the user clicks Apply, the operations are executed directly in the live editor -- the same result as if the user had made the changes manually. Components appear in the Designer, blocks appear in the Blocks editor, properties are updated, screens are created, etc.

### 7. Multi-step workflows

Some requests require the AI to navigate before it can act (see "Screen Scoping and Navigate, Then Act" above). This happens in two common situations:

**Switching between Designer and Blocks views** -- e.g., "add a button that shows a notification when clicked":

1. First batch: Designer operations (add the button, set its text)
2. Second batch: Toggle to Blocks view (alone)
3. Third batch: Block operations (the click event handler)

**Working across screens** (Project Editor mode) -- e.g., "add a logout button to the Settings screen":

1. First batch: Switch to SettingsScreen (alone)
2. Second batch: Designer operations on SettingsScreen (add the button)
3. Third batch: Toggle to Blocks view (alone)
4. Fourth batch: Block operations (the logout logic)

The user may see multiple batches of proposals for a single request. The "Apply & Accept All" button streamlines this process by automatically approving subsequent batches.

---

## Key Concept: Screen Scoping and "Navigate, Then Act"

One of the most important things to understand about the AI agent is that **all editing operations are scoped to the current screen and the current editor view**. The AI can only add components, set properties, or write blocks on the screen that is currently open, and only in the view (Designer or Blocks) that is currently active.

This means:

- If the user is on Screen1 in the Designer view, the AI can add components and set properties on Screen1 -- but it cannot touch Screen2's components or write blocks until it navigates.
- If the user asks "add a button to LoginScreen and write its click handler," and the user is currently on Screen1 in the Designer view, the AI must perform **three separate steps**: (1) switch to LoginScreen, (2) add the button in Designer view, (3) toggle to Blocks view and write the click handler.

This "navigate, then act" rule applies in two dimensions:

| Dimension | Current State | To modify something else, the AI must... |
|-----------|--------------|------------------------------------------|
| **Screen** | Viewing Screen1 | Issue a **Switch Screen** operation to navigate to the target screen (Project Editor mode only) |
| **Editor View** | In Designer view | Issue a **Toggle Editor** operation to switch to Blocks view (or vice versa) |

Navigation operations (Switch Screen, Toggle Editor, Create Screen) must be issued **alone** -- the AI cannot combine them with other operations in the same batch. After the navigation is confirmed, the AI continues with the editing operations in a follow-up batch.

This is why complex requests result in **multiple rounds of proposals**. For example, "build a login screen with validation logic" might produce:

1. **Batch 1**: Create the new screen (alone)
2. **Batch 2**: Add components in Designer view (TextBoxes, Button, Label)
3. **Batch 3**: Toggle to Blocks view (alone)
4. **Batch 4**: Write the event handlers and validation logic

The user approves each batch (or uses "Apply & Accept All" to automate approval).

### How the AI Researches Before Acting: Lookups

Before making changes, the AI often needs to gather information. It has two **read-only lookup tools** that work in any mode (including Advisor):

- **Lookup Component**: The AI starts with only a brief catalog of component types (e.g., "Button -- a clickable element"). When it needs to know the exact property names, events, or methods of a component, it calls Lookup Component to retrieve the full specification. For example, before setting a Slider's properties, the AI might look up that Slider has `MinValue`, `MaxValue`, `ThumbPosition`, and `ColorLeft`/`ColorRight` properties. This ensures the AI uses correct property names rather than guessing.

- **Lookup Screen**: The AI can inspect the current state of any screen in the project -- what components exist, their property values, and what blocks are defined. This is how the AI "sees" the project. For example, if a user says "change the background color of the login screen," the AI first looks up that screen to find out which components are on it and what their current properties are.

These lookups happen automatically and invisibly -- the user does not see them in the chat. They happen on the server side before the AI formulates its response. An educator can think of them like the AI "reading the project files" before deciding what to change.

This lookup-before-act pattern is also why the AI sometimes takes a moment to respond -- it may be making several lookups to understand the project state before proposing changes.

---

## What the AI Can Do (Operations)

The AI interacts with the project through a fixed set of operations. It cannot do anything outside this list:

### Designer Operations (require Designer view, current screen only)

| Operation | Description | Example |
|-----------|-------------|---------|
| **Add Component** | Creates a new component on the current screen | Add a Button named "SubmitBtn" inside a HorizontalArrangement |
| **Delete Component** | Removes a component (and its children) from the screen | Remove the Label called "OldLabel" |
| **Set Property** | Changes a property value on an existing component | Set Button1's BackgroundColor to red |
| **Rename Component** | Changes a component's name | Rename "Button1" to "SubmitButton" |

### Block Operations (require Blocks view, current screen only)

| Operation | Description | Example |
|-----------|-------------|---------|
| **Write Block** | Creates or replaces a block (event handler, procedure, global variable) | Write a Button1.Click event that sets Label1.Text to "Hello" |
| **Delete Block** | Removes a specific block | Delete the Button1.Click event handler |

The AI writes blocks using YAIL (Young Android Intermediate Language), the internal code representation of App Inventor blocks. This is converted to visual blocks automatically -- students never need to see or understand YAIL.

### Navigation Operations

| Operation | Description |
|-----------|-------------|
| **Toggle Editor** | Switch between Designer and Blocks views |
| **Switch Screen** | Navigate to a different screen (Project Editor mode only) |

### Project-Level Operations (Project Editor mode only)

| Operation | Description |
|-----------|-------------|
| **Create Screen** | Add a new screen to the project |
| **Delete Screen** | Remove a screen (Screen1 cannot be deleted) |
| **Set Project Property** | Change a project-wide setting (app name, theme, colors, icon, etc.) |

### Read-Only Lookups (all modes, any screen)

The AI can look up information without making changes, and unlike editing operations, lookups are **not limited to the current screen**:

- **Lookup Component**: Retrieve the full specification of a component type -- all its properties, events, and methods. This is how the AI knows which property names are valid before proposing a Set Property operation.
- **Lookup Screen**: Inspect the state of any screen in the project -- its components, their properties, and its blocks. This works even for screens the user is not currently viewing.

These lookups happen automatically behind the scenes. See "How the AI Researches Before Acting" above for more detail.

---

## Safety and Guardrails

The AI agent has multiple layers of safety built in:

### Permission Modes

The three-mode system (Advisor / Screen Editor / Project Editor) ensures the AI can only do what the user has authorized. An Advisor-mode AI cannot accidentally modify anything.

### Preview Before Apply

Every proposed change is shown to the user before it takes effect. Nothing is applied without explicit approval (unless the user has opted into "Apply & Accept All" for multi-step workflows).

### Validation

Before changes are shown in the preview, the system validates them:

- Are the component names valid?
- Does the component already exist? (prevents duplicates)
- Is the block syntax correct? (blocks are tested against the Blockly engine before preview)
- Is the target screen/property real?

If validation catches problems, the system automatically sends the errors back to the AI for correction. This can happen up to 5 times for block syntax issues and 3 times for execution errors. The user typically sees none of this -- it happens behind the scenes and the preview only appears when valid operations are ready.

### View Enforcement

The system enforces that Designer operations only run in the Designer view and Block operations only run in the Blocks view. The AI cannot bypass this -- it must use the "toggle editor" operation to switch views first.

### Rate Limiting

Each user is limited to a configurable number of requests per minute (default: 10) to prevent abuse.

### No Runtime Access

The AI operates on the project source files, not a running app. It cannot access device sensors, network requests, or user data. It cannot run, install, or deploy the app.

---

## Teaching Scenarios

### Scenario 1: Guided Introduction (Advisor Mode)

**Goal**: Students learn what the AI is and how the chat works without risk.

1. Have students open a project and set the AI mode to **Advisor**
2. Ask students to type questions like:
   - "What components would I need for a calculator app?"
   - "How do I make a button change color when clicked?"
   - "What's the difference between a ListView and a Spinner?"
3. The AI will respond with explanations and suggestions but will not modify anything
4. Discuss: How is this similar to asking a teacher for help? What are the limitations?

### Scenario 2: Building with Assistance (Screen Editor Mode)

**Goal**: Students build an app with AI help, learning to review and approve changes.

1. Set the AI mode to **Screen Editor**
2. Give students a simple app specification: "Build a tip calculator with a TextBox for the bill amount, a Slider for tip percentage, and a Label showing the result"
3. Have students describe the app to the AI step by step:
   - First: "Add a TextBox for entering the bill amount"
   - Review the preview, discuss what's being added
   - Click Apply
   - Then: "Add a Slider for tip percentage from 0 to 30"
   - Continue building piece by piece
4. Teaching moments:
   - Compare what the AI proposes vs. what the student expected
   - When the AI makes a mistake, use Reject and rephrase
   - Discuss: Could you have done this faster manually? When is AI help most useful?

### Scenario 3: Understanding the Preview (Critical Evaluation)

**Goal**: Students learn to critically evaluate AI-proposed changes.

1. Ask the AI to make a complex change in Screen Editor mode
2. Before clicking Apply, have students:
   - Read every operation in the preview panel
   - Predict what the screen/blocks will look like after applying
   - Identify any operations they disagree with
3. Try both Apply and Reject paths to see the difference
4. Discuss: Why is it important to review AI output before accepting it?

### Scenario 4: Multi-Screen App (Project Editor Mode)

**Goal**: Students build a multi-screen app and see the AI navigate between screens.

1. Set the AI mode to **Project Editor**
2. Request: "Create a login screen with username and password fields, and a main screen that shows a welcome message"
3. Observe how the AI:
   - Creates a new screen
   - Switches between screens
   - Toggles between Designer and Blocks views
   - Builds components and logic across multiple steps
4. Discuss the multi-step workflow and how each batch builds on the previous one

### Scenario 5: Debugging with AI

**Goal**: Students learn to use the AI for understanding and fixing problems.

1. Give students a project with intentional bugs (missing event handler, wrong property value, misspelled component reference)
2. Have students describe the problem to the AI: "When I click the button nothing happens" or "The label shows the wrong text"
3. In Advisor mode: The AI can suggest fixes without making changes
4. In Screen Editor mode: The AI can propose the fix directly
5. Compare: How does AI-assisted debugging differ from manual debugging?

---

## Understanding the AI's Knowledge

The AI is not "magic." It has been given specific knowledge about App Inventor:

- **Component catalog**: It knows every built-in component type, its category, and a brief description. However, it only has brief summaries -- it must use the Lookup Component tool to discover the full list of properties, events, and methods before using them. This is by design: it prevents the AI from guessing property names and ensures it works with accurate information.
- **Block syntax**: It knows how to write the internal code that represents blocks (YAIL), which gets converted to visual blocks automatically.
- **Rules and constraints**: It knows that components need unique names, that visible components need containers, that certain operations require specific views, etc.
- **Few-shot examples**: It has been shown examples of common tasks (building a counter app, creating a login screen, etc.) to guide its responses.

The AI does **not** know:

- The student's learning objectives or skill level (unless told)
- What the app should look like visually (unless described)
- Runtime behavior or real device data
- APIs, web services, or external data sources beyond what App Inventor components expose
- Extensions that haven't been added to the project

---

## Conversation Management

- **Per-project conversations**: Each project has its own independent conversation. Switching projects loads a different chat history.
- **24-hour expiry**: Conversations expire after 24 hours of inactivity. Starting a new session after expiry begins a fresh conversation.
- **New Conversation button**: Users can manually clear the conversation at any time to start fresh. This is useful when changing topics or when the conversation has become confusing.
- **Context awareness**: Each message includes a fresh snapshot of the current project state, so the AI always sees the latest version of the project, even if the user has made manual changes between messages.

---

## Common Questions from Educators

### "Will this replace learning to code?"

No. The AI is a tool, not a replacement for understanding. Students who don't understand what a Button click event handler does will struggle to meaningfully direct the AI or evaluate its proposals. The preview-before-apply model is specifically designed so students must engage with what the AI proposes.

### "Can students cheat with this?"

The same way they could cheat with any reference material or tool. The key educational design decision is the **mode system** and the **preview panel**: students see exactly what changes are being made and must approve them. Educators can require Advisor mode for assessments (where the AI can only explain, not modify) or disable the AI entirely per-project by leaving the mode set to "Off."

### "What happens if the AI makes a mistake?"

The user can click Reject to discard the proposed changes (nothing is applied). The system also has automatic validation and retry mechanisms -- if the AI proposes invalid blocks, the system catches the error and asks the AI to fix it automatically before showing the preview. However, the AI can still make logical errors (e.g., putting a component in the wrong container or writing correct-but-wrong logic). This is a teaching opportunity about the importance of human review.

### "Does the AI see student data or personal information?"

The AI sees the project source files (component definitions, block code, screen names, project settings, and asset filenames). It does not see runtime data, personal information, or anything outside the project being edited. Conversations are stored on the server with a 24-hour time-to-live and then deleted.

### "Can the AI work with extensions?"

Yes, but with limitations. The AI can see which extensions are installed in the project and can look up their component information. However, its knowledge of extension-specific properties and methods depends on the extension providing documentation in its `components.json` file. For well-documented extensions, the AI can use them effectively.

### "How does the AI handle multiple screens?"

In Project Editor mode, the AI can create, switch between, and delete screens. However, all editing operations are screen-scoped: the AI can only modify components and blocks on the screen that is currently open. To edit a different screen, it must first issue a Switch Screen operation (which appears as its own batch for the user to approve), and then it can propose changes to that screen. For multi-screen requests, the AI works through the screens sequentially. It can, however, *look up* any screen's state without switching to it -- only editing requires being on the target screen. See "Screen Scoping and Navigate, Then Act" for a full explanation.

---

## Glossary for Educators

| Term | Definition |
|------|-----------|
| **AI Agent** | The built-in AI assistant that can chat with users and modify App Inventor projects |
| **Mode** | The permission level (Advisor / Screen Editor / Project Editor) that controls what the AI is allowed to do |
| **Operation** | A single concrete change the AI proposes (e.g., "add a Button," "set a property," "write a block") |
| **Preview Panel** | The color-coded list of proposed operations shown before they are applied |
| **Apply** | Approve and execute the proposed operations |
| **Reject** | Discard the proposed operations without making changes |
| **YAIL** | Young Android Intermediate Language -- the internal code representation that blocks get compiled to. The AI writes YAIL, which is automatically converted to visual blocks. Students do not need to know YAIL. |
| **Designer View** | The visual editor where components are placed and their properties are set |
| **Blocks View** | The visual programming editor where event handlers, procedures, and logic are built |
| **Screen Scoping** | The rule that all editing operations apply only to the currently open screen. The AI must switch screens before it can modify a different one. |
| **Lookup** | A read-only operation where the AI retrieves information (component specs or screen state) without making changes. Unlike editing, lookups can target any screen. |
| **Continuation** | When the AI needs multiple steps (e.g., switch screens, toggle views, then add blocks), each navigation step is a separate batch of operations |
| **Context** | The snapshot of the current project state that is sent to the AI with each message, allowing it to understand what already exists |
| **LLM** | Large Language Model -- the AI technology (like GPT or Claude) that powers the assistant |
