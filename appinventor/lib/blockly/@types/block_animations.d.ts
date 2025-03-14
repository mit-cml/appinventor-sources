/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from './block_svg.js';
/**
 * Play some UI effects (sound, animation) when disposing of a block.
 *
 * @param block The block being disposed of.
 * @internal
 */
export declare function disposeUiEffect(block: BlockSvg): void;
/**
 * Play some UI effects (sound, ripple) after a connection has been established.
 *
 * @param block The block being connected.
 * @internal
 */
export declare function connectionUiEffect(block: BlockSvg): void;
/**
 * Play some UI effects (sound, animation) when disconnecting a block.
 *
 * @param block The block being disconnected.
 * @internal
 */
export declare function disconnectUiEffect(block: BlockSvg): void;
/**
 * Stop the disconnect UI animation immediately.
 *
 * @internal
 */
export declare function disconnectUiStop(): void;
//# sourceMappingURL=block_animations.d.ts.map