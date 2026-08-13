/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Object holding the names of the default shortcut items.
 */
export declare enum names {
    ESCAPE = "escape",
    DELETE = "delete",
    COPY = "copy",
    CUT = "cut",
    PASTE = "paste",
    UNDO = "undo",
    REDO = "redo",
    MENU = "menu",
    FOCUS_WORKSPACE = "focus_workspace",
    FOCUS_TOOLBOX = "focus_toolbox",
    START_MOVE = "start_move",
    START_MOVE_STACK = "start_move_stack",
    FINISH_MOVE = "finish_move",
    ABORT_MOVE = "abort_move",
    MOVE_UP = "move_up",
    MOVE_DOWN = "move_down",
    MOVE_LEFT = "move_left",
    MOVE_RIGHT = "move_right",
    NAVIGATE_RIGHT = "right",
    NAVIGATE_LEFT = "left",
    NAVIGATE_UP = "up",
    NAVIGATE_DOWN = "down",
    DISCONNECT = "disconnect",
    NEXT_STACK = "next_stack",
    PREVIOUS_STACK = "previous_stack",
    INFORMATION = "information",
    EXTENDED_INFORMATION = "extended_information",
    PERFORM_ACTION = "perform_action",
    DUPLICATE = "duplicate",
    CLEANUP = "cleanup",
    SHOW_TOOLTIP = "show_tooltip",
    NEXT_HEADING = "next_heading",
    PREVIOUS_HEADING = "previous_heading",
    TOGGLE_SCREENREADER = "toggle_screenreader",
    JUMP_TOP_STACK = "jump_to_top_of_stack",
    JUMP_BOTTOM_STACK = "jump_to_bottom_of_stack",
    JUMP_BLOCK_START = "jump_to_block_start",
    JUMP_BLOCK_END = "jump_to_block_end",
    JUMP_FIRST_BLOCK = "jump_to_first_block",
    JUMP_LAST_BLOCK = "jump_to_last_block"
}
/**
 * Keyboard shortcut to hide chaff on escape.
 */
export declare function registerEscape(): void;
/**
 * Keyboard shortcut to delete a block on delete or backspace
 */
export declare function registerDelete(): void;
/**
 * Keyboard shortcut to copy a block on ctrl+c, cmd+c, or alt+c.
 */
export declare function registerCopy(): void;
/**
 * Keyboard shortcut to copy and delete a block on ctrl+x, cmd+x, or alt+x.
 */
export declare function registerCut(): void;
/**
 * Keyboard shortcut to paste a block on ctrl+v, cmd+v, or alt+v.
 */
export declare function registerPaste(): void;
/**
 * Keyboard shortcut to undo the previous action on ctrl+z, cmd+z, or alt+z.
 */
export declare function registerUndo(): void;
/**
 * Keyboard shortcut to redo the previous action on ctrl+shift+z, cmd+shift+z,
 * or alt+shift+z.
 */
export declare function registerRedo(): void;
/**
 * Registers keyboard shortcuts for keyboard-driven movement of workspace
 * elements.
 */
export declare function registerMovementShortcuts(): void;
/**
 * Keyboard shortcut to show the context menu on ctrl/cmd+Enter, shift+F10, and
 * the menu key.
 */
export declare function registerShowContextMenu(): void;
/**
 * Registers keyboard shortcuts to navigate around the Blockly interface.
 */
export declare function registerArrowNavigation(): void;
/**
 * Registers keyboard shortcut to focus the workspace.
 */
export declare function registerFocusWorkspace(): void;
/**
 * Registers keyboard shortcut to focus the toolbox.
 */
export declare function registerFocusToolbox(): void;
/**
 * Registers keyboard shortcut to announce information about the focused
 * element.
 */
export declare function registerReadInformation(): void;
/**
 * Registers keyboard shortcut to announce an extended description of the
 * focused element.
 */
export declare function registerReadExtendedInformation(): void;
/**
 * Registers keyboard shortcut to disconnect the focused block.
 */
export declare function registerDisconnectBlock(): void;
/**
 * Registers keyboard shortcuts to jump between stacks/top-level items on the
 * workspace.
 */
export declare function registerStackNavigation(): void;
/**
 * Registers keyboard shortcuts to jump between headings (labels) in a flyout.
 *
 * Pressing H moves focus to the next heading; Shift+H moves focus to the
 * previous heading. The shortcut only activates when focus is already inside
 * the flyout; otherwise it returns false so other handlers may take over.
 */
export declare function registerHeadingNavigation(): void;
/**
 * Registers keyboard shortcut to perform an action on the focused element.
 */
export declare function registerPerformAction(): void;
/**
 * Registers keyboard shortcut to duplicate a block or workspace comment.
 */
export declare function registerDuplicate(): void;
/**
 * Registers keyboard shortcut to clean up the workspace.
 */
export declare function registerCleanup(): void;
/**
 * Registers keyboard shortcut to display the tooltip for the focused element.
 */
export declare function registerShowTooltip(): void;
/**
 * Registers keyboard shortcut to toggle on or off various behaviors that
 * improve the experience for individuals using screenreaders.
 */
export declare function registerToggleScreenreaderMode(): void;
/**
 * Registers a keyboard shortcut that sets the focus to the block
 * that owns the current focused node.
 */
export declare function registerJumpBlockStart(): void;
/**
 * Registers a keyboard shortcut that sets the focus to the
 * last input of the block that owns the current focused node.
 */
export declare function registerJumpBlockEnd(): void;
/**
 * Registers a keyboard shortcut that sets the focus to the top block
 * in the current stack.
 */
export declare function registerJumpTopStack(): void;
/**
 * Registers a keyboard shortcut that sets the focus to the bottom block
 * in the current stack.
 */
export declare function registerJumpBottomStack(): void;
/**
 * Registers a keyboard shortcut that sets the focus to the first
 * block in the workspace.
 */
export declare function registerJumpFirstBlock(): void;
/**
 * Registers a keyboard shortcut that sets the focus to the last
 * block in the workspace.
 */
export declare function registerJumpLastBlock(): void;
/**
 * Registers all default keyboard shortcut item. This should be called once per
 * instance of KeyboardShortcutRegistry.
 *
 * @internal
 */
export declare function registerDefaultShortcuts(): void;
/**
 * Registers an extended set of keyboard shortcuts used to support deep keyboard
 * navigation of Blockly.
 */
export declare function registerKeyboardNavigationShortcuts(): void;
/**
 * Registers keyboard shortcuts used to announce screen reader information.
 */
export declare function registerScreenReaderShortcuts(): void;
/**
 * Registers keyboard shortcuts used to jump between blocks and stacks in the workspace.
 * Note these are not registered by default, so call this function to enable them if desired.
 */
export declare function registerNavigationShortcuts(): void;
//# sourceMappingURL=shortcut_items.d.ts.map