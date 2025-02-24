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
    REDO = "redo"
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
 * Registers all default keyboard shortcut item. This should be called once per
 * instance of KeyboardShortcutRegistry.
 *
 * @internal
 */
export declare function registerDefaultShortcuts(): void;
//# sourceMappingURL=shortcut_items.d.ts.map