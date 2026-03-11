/**
 * @license
 * Copyright 2011 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from './block.js';
import type { BlockSvg } from './block_svg.js';
import type { ContextMenuOption, LegacyContextMenuOption } from './contextmenu_registry.js';
import * as serializationBlocks from './serialization/blocks.js';
import { Coordinate } from './utils/coordinate.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Gets the block the context menu is currently attached to.
 * It is not recommended that you use this function; instead,
 * use the scope object passed to the context menu callback.
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
 * @param menuOpenEvent Event that caused the menu to open.
 * @param options Array of menu options.
 * @param rtl True if RTL, false if LTR.
 * @param workspace The workspace associated with the context menu, if any.
 * @param location The screen coordinates at which to show the menu.
 */
export declare function show(menuOpenEvent: Event, options: (ContextMenuOption | LegacyContextMenuOption)[], rtl: boolean, workspace?: WorkspaceSvg, location?: Coordinate): void;
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