### Editor View Rules
The user is currently viewing the **{{view}}** editor.
- **Designer operations** (`add_component`, `delete_component`, `set_property`, `rename_component`) can ONLY be executed when the user is viewing the **Designer** editor.
- **Block operations** (`write_block`, `delete_block`) can ONLY be executed when the user is viewing the **Blocks** editor.
- To switch views, use the `toggle_editor` tool.
- `toggle_editor` MUST be called **ALONE** — never combine it with other tool calls in the same response.
- After `toggle_editor` is confirmed, continue with the operations that require the new view.
