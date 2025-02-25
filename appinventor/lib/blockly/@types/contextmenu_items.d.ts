/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Option to undo previous action.
 */
export declare function registerUndo(): void;
/**
 * Option to redo previous action.
 */
export declare function registerRedo(): void;
/**
 * Option to clean up blocks.
 */
export declare function registerCleanup(): void;
/**
 * Option to collapse all blocks.
 */
export declare function registerCollapse(): void;
/**
 * Option to expand all blocks.
 */
export declare function registerExpand(): void;
/**
 * Option to delete all blocks.
 */
export declare function registerDeleteAll(): void;
/**
 * Option to duplicate a block.
 */
export declare function registerDuplicate(): void;
/**
 * Option to add or remove block-level comment.
 */
export declare function registerComment(): void;
/**
 * Option to inline variables.
 */
export declare function registerInline(): void;
/**
 * Option to collapse or expand a block.
 */
export declare function registerCollapseExpandBlock(): void;
/**
 * Option to disable or enable a block.
 */
export declare function registerDisable(): void;
/**
 * Option to delete a block.
 */
export declare function registerDelete(): void;
/**
 * Option to open help for a block.
 */
export declare function registerHelp(): void;
/**
 * Registers all default context menu items. This should be called once per
 * instance of ContextMenuRegistry.
 *
 * @internal
 */
export declare function registerDefaultOptions(): void;
//# sourceMappingURL=contextmenu_items.d.ts.map