/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from './block_svg.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Registers that the given block and all of its parents need to be rerendered,
 * and registers a callback to do so after a delay, to allowf or batching.
 *
 * @param block The block to rerender.
 * @returns A promise that resolves after the currently queued renders have been
 *     completed. Used for triggering other behavior that relies on updated
 *     size/position location for the block.
 * @internal
 */
export declare function queueRender(block: BlockSvg): Promise<void>;
/**
 * @returns A promise that resolves after the currently queued renders have
 *     been completed.
 */
export declare function finishQueuedRenders(): Promise<void>;
/**
 * Triggers an immediate render of all queued renders. Should only be used in
 * cases where queueing renders breaks functionality + backwards compatibility
 * (such as rendering icons).
 *
 * @param workspace If provided, only rerender blocks in this workspace.
 *
 * @internal
 */
export declare function triggerQueuedRenders(workspace?: WorkspaceSvg): void;
//# sourceMappingURL=render_management.d.ts.map