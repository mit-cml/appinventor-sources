/**
 * @license
 * Copyright 2011 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from './block.js';
import type { BlockSvg } from './block_svg.js';
import type { ContextMenuOption, LegacyContextMenuOption } from './contextmenu_registry.js';
import * as serializationBlocks from './serialization/blocks.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Gets the block the context menu is currently attached to.
 *
 * @returns The block the context menu is attached to.
 */
export declare function getCurrentBlock(): Block | null;
/**
 * Sets the block the context menu is currently attached to.
 *
 * @param block The block the context menu is attached to.
 */
export declare function setCurrentBlock(block: Block | null): void;
/**
 * Construct the menu based on the list of options and show the menu.
 *
 * @param e Mouse event.
 * @param options Array of menu options.
 * @param rtl True if RTL, false if LTR.
 * @param workspace The workspace associated with the context menu, if any.
 */
export declare function show(e: PointerEvent, options: (ContextMenuOption | LegacyContextMenuOption)[], rtl: boolean, workspace?: WorkspaceSvg): void;
/**
 * Hide the context menu.
 */
export declare function hide(): void;
/**
 * Dispose of the menu.
 */
export declare function dispose(): void;
/**
 * Create a callback function that creates and configures a block,
 *   then places the new block next to the original and returns it.
 *
 * @param block Original block.
 * @param state XML or JSON object representation of the new block.
 * @returns Function that creates a block.
 */
export declare function callbackFactory(block: Block, state: Element | serializationBlocks.State): () => BlockSvg;
//# sourceMappingURL=contextmenu.d.ts.map