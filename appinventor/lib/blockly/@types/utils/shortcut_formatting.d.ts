/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Find the primary shortcut for this platform and return it as single string
 * in a short user facing format.
 *
 * @param shortcutName The keyboard shortcut name, e.g. "cut".
 * @returns The formatted shortcut.
 */
export declare function getShortcutKeysShort(shortcutName: string): string;
/**
 * Find the relevant shortcuts for the given shortcut for the current platform.
 * Keys are returned in a long user facing format, e.g. "Command ⌘ Option ⌥ C"
 *
 * @param shortcutName The keyboard shortcut name, e.g. "cut".
 * @returns The formatted shortcuts as individual keys.
 */
export declare function getShortcutKeysLong(shortcutName: string): string[][];
//# sourceMappingURL=shortcut_formatting.d.ts.map