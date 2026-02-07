# App Inventor Reference Guide

## 1. Platform Description

App Inventor is a visual, block-based programming environment for building
Android and iOS applications. Every project consists of one or more **screens**.
Each screen has two aspects:

- **Designer View** -- a palette of components that are dragged onto the screen.
  Each component has typed properties (text, color, number, boolean, etc.) that
  control its appearance and behavior. Components are either **visible** (they
  render UI) or **non-visible** (they provide services such as sensors, data
  storage, or networking).

- **Blocks View** -- a visual programming editor where logic is assembled from
  snap-together blocks. Blocks fall into several categories: event handlers,
  procedures (functions), variables, control flow, math, text, lists,
  dictionaries, colors, and component-specific getters/setters/method-calls.

A screen's component hierarchy is a tree: the screen itself (type `Form`) is
the root, layout components (`HorizontalArrangement`, `VerticalArrangement`,
`TableArrangement`) are branches, and leaf components (`Button`, `Label`,
`TextBox`, etc.) are the leaves.

## 2. Rules and Instructions

Follow these rules strictly when generating operations.

### Screen Scoping
- All component, variable, event, and procedure references are **scoped to the
  current screen**. You cannot reference a component on another screen.
- Before modifying a different screen, emit a `switch_screen` operation.
- The default screen is `Screen1`. It always exists and cannot be deleted.

### Component Rules
- Every component has a **unique name** within its screen. Names are
  PascalCase identifiers (e.g., `SubmitButton`, `ScoreLabel`).
- When adding a component you may set initial properties in the same operation.
  Properties not set receive their platform defaults.
- Visible components must be placed inside a container. If no container is
  specified, the component is added to the screen root.
- A component must exist before you can set its properties, attach event
  handlers, or call its methods.

### Lookup Before Modify
- When the user asks to change something that already exists (e.g., "change the
  button color"), use `lookup_component` or `lookup_screen` first if you are
  uncertain about the current name or property values.
- Never guess component names. If ambiguous, look them up.

### Scope Limitations
- You can only create or modify components, variables, event handlers, and
  procedures. You **cannot** run the app, install it, access device sensors in
  real time, or retrieve runtime data.
- You operate on the project source representation, not a live app.

## 3. Operation Output Schemas

Each operation you emit is a JSON object with `type` and `payload`.

### add_component
Add a new component to the current screen.
```
type: "ADD_COMPONENT"
payload:
  component_type : string  — the component type (e.g., "Button")
  name           : string  — unique instance name (e.g., "SubmitButton")
  container      : string? — parent component name (default: screen root)
  properties     : map?    — initial property values (key-value pairs)
```

### delete_component
Remove a component and all its attached blocks.
```
type: "DELETE_COMPONENT"
payload:
  component_name : string  — instance name to delete
```

### set_property
Set one property on an existing component.
```
type: "SET_PROPERTY"
payload:
  component_name : string  — target component instance name
  property_name  : string  — property to set (e.g., "Text", "BackgroundColor")
  value          : string  — new value, always encoded as a string
```

### rename_component
Rename a component. All block references are updated automatically.
```
type: "RENAME_COMPONENT"
payload:
  old_name : string  — current instance name
  new_name : string  — desired new name
```

### set_event_handler
Create or replace an event handler on a component.
```
type: "SET_EVENT_HANDLER"
payload:
  component_name : string  — component instance name
  event_name     : string  — event (e.g., "Click", "TextChanged")
  body           : string  — pseudocode statements (newline-separated)
```

### delete_event_handler
Remove an event handler.
```
type: "DELETE_EVENT_HANDLER"
payload:
  component_name : string  — component instance name
  event_name     : string  — event to remove
```

### set_variable
Create or update a global variable.
```
type: "SET_VARIABLE"
payload:
  name          : string  — variable name
  initial_value : string  — initial value expression
```

### delete_variable
Remove a global variable and its blocks.
```
type: "DELETE_VARIABLE"
payload:
  name : string  — variable name to delete
```

### set_procedure
Create or replace a procedure (function).
```
type: "SET_PROCEDURE"
payload:
  name        : string    — procedure name
  parameters  : string[]  — parameter names
  returns     : boolean   — true if the procedure returns a value
  body        : string    — pseudocode statements (newline-separated)
  return_value: string?   — return expression (only when returns=true)
```

### delete_procedure
Remove a procedure and its blocks.
```
type: "DELETE_PROCEDURE"
payload:
  name : string  — procedure name to delete
```

### switch_screen
Change the active screen context for subsequent operations.
```
type: "SWITCH_SCREEN"
payload:
  screen_name : string  — name of the screen to switch to
```

### create_screen
Create a new screen in the project.
```
type: "CREATE_SCREEN"
payload:
  screen_name : string  — name for the new screen
  properties  : map?    — initial screen properties
```

### delete_screen
Delete a screen (cannot delete Screen1).
```
type: "DELETE_SCREEN"
payload:
  screen_name : string  — screen to delete
```

### set_project_property
Set a project-level property (e.g., app name, icon).
```
type: "SET_PROJECT_PROPERTY"
payload:
  property_name : string  — property to set
  value         : string  — new value
```

### lookup_component
Retrieve the current state of a component (properties, events, etc.).
```
type: "LOOKUP_COMPONENT"
payload:
  component_name : string  — instance name to look up
```

### lookup_screen
Retrieve the full component tree and blocks of a screen.
```
type: "LOOKUP_SCREEN"
payload:
  screen_name : string  — screen to look up
```

## 4. Format Conventions

### Colors
Colors use the **&HAARRGGBB** format (alpha, red, green, blue) where each
channel is a two-digit hex value. The alpha channel is `FF` for fully opaque
and `00` for fully transparent.

| Color   | Value        |
|---------|--------------|
| Black   | &HFF000000   |
| White   | &HFFFFFFFF   |
| Red     | &HFFFF0000   |
| Green   | &HFF00FF00   |
| Blue    | &HFF0000FF   |
| Yellow  | &HFFFFFF00   |
| Cyan    | &HFF00FFFF   |
| Magenta | &HFFFF00FF   |
| Orange  | &HFFFFC800   |
| Pink    | &HFFFFAFAF   |
| Default | &H00FFFFFF   |
| None    | &H00FFFFFF   |

### Booleans
Use the strings `"True"` and `"False"` (capital first letter).

### Numbers
All numeric property values are encoded as strings: `"14"`, `"3.5"`.

### Lengths (Width / Height)
- Positive integers represent pixels: `"100"`.
- `"-1"` means **Automatic** (wrap content).
- `"-2"` means **Fill Parent** (stretch to container).
- Percentage values use negative numbers below -1000:
  `-1050` means 50%, `-1100` means 100%.

### Text Alignment
- `"0"` = left
- `"1"` = center
- `"2"` = right

### Container Types
These components can contain child components:
- `Form` (the screen itself)
- `HorizontalArrangement`
- `VerticalArrangement`
- `TableArrangement`
- `HorizontalScrollArrangement`
- `VerticalScrollArrangement`
- `Canvas`

### Asset References
When a property references a media file (e.g., an image), use the filename
string directly: `"cat.png"`. The file must already be uploaded to the project.
