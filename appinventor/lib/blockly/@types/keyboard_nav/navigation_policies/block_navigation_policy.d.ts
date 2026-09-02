/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from '../../block_svg.js';
import type { IFocusableNode } from '../../interfaces/i_focusable_node.js';
import type { INavigationPolicy } from '../../interfaces/i_navigation_policy.js';
/**
 * Set of rules controlling keyboard navigation from a block.
 */
export declare class BlockNavigationPolicy implements INavigationPolicy<BlockSvg> {
    /**
     * Returns the first child of the given block.
     *
     * @param current The block to return the first child of.
     * @returns The first icon, field, or input of the given block, if any.
     */
    getFirstChild(current: BlockSvg): IFocusableNode | null;
    /**
     * Returns the parent of the given block.
     *
     * @param current The block to return the parent of.
     * @returns The top block of the given block's stack, or the connection to
     *     which it is attached.
     */
    getParent(current: BlockSvg): IFocusableNode | null;
    /**
     * Returns the next peer node of the given block.
     *
     * @param current The block to find the following element of.
     * @returns The block's next connection, or the next peer on its parent block,
     *     otherwise null.
     */
    getNextSibling(current: BlockSvg): IFocusableNode | null;
    /**
     * Returns the previous peer node of the given block.
     *
     * @param current The block to find the preceding element of.
     * @returns The block's previous connection, or the previous peer on its
     *     parent block, otherwise null.
     */
    getPreviousSibling(current: BlockSvg): IFocusableNode | null;
    /**
     * Returns the visual row ID of the given block.
     *
     * @param current The block to retrieve the row ID of.
     * @returns The row ID of the given block.
     */
    getRowId(current: BlockSvg): string;
    /**
     * Returns whether or not the given block can be navigated to.
     *
     * @param current The instance to check for navigability.
     * @returns True if the given block can be focused.
     */
    isNavigable(current: BlockSvg): boolean;
    /**
     * Returns whether the given object can be navigated from by this policy.
     *
     * @param current The object to check if this policy applies to.
     * @returns True if the object is a BlockSvg.
     */
    isApplicable(current: any): current is BlockSvg;
}
/**
 * Returns the next navigable item relative to the provided block child.
 *
 * @param block The block whose children should be navigated.
 * @param current The navigable block child item to navigate relative to.
 * @param delta The difference in index to navigate; positive values navigate
 *     forward by n, while negative values navigate backwards by n.
 * @returns The navigable block child offset by `delta` relative to `current`.
 */
export declare function navigateBlock(block: BlockSvg, current: IFocusableNode, delta: number): IFocusableNode | null;
//# sourceMappingURL=block_navigation_policy.d.ts.map