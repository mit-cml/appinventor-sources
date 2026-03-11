/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from '../block_svg.js';
import type { IFocusableNode } from '../interfaces/i_focusable_node.js';
import type { INavigationPolicy } from '../interfaces/i_navigation_policy.js';
import { RenderedConnection } from '../rendered_connection.js';
/**
 * Set of rules controlling keyboard navigation from a connection.
 */
export declare class ConnectionNavigationPolicy implements INavigationPolicy<RenderedConnection> {
    /**
     * Returns the first child of the given connection.
     *
     * @param current The connection to return the first child of.
     * @returns The connection's first child element, or null if not none.
     */
    getFirstChild(current: RenderedConnection): IFocusableNode | null;
    /**
     * Returns the parent of the given connection.
     *
     * @param current The connection to return the parent of.
     * @returns The given connection's parent connection or block.
     */
    getParent(current: RenderedConnection): IFocusableNode | null;
    /**
     * Returns the next element following the given connection.
     *
     * @param current The connection to navigate from.
     * @returns The field, input connection or block following this connection.
     */
    getNextSibling(current: RenderedConnection): IFocusableNode | null;
    /**
     * Returns the element preceding the given connection.
     *
     * @param current The connection to navigate from.
     * @returns The field, input connection or block preceding this connection.
     */
    getPreviousSibling(current: RenderedConnection): IFocusableNode | null;
    /**
     * Gets the parent connection on a block.
     * This is either an output connection, previous connection or undefined.
     * If both connections exist return the one that is actually connected
     * to another block.
     *
     * @param block The block to find the parent connection on.
     * @returns The connection connecting to the parent of the block.
     */
    protected getParentConnection(block: BlockSvg): RenderedConnection;
    /**
     * Returns whether or not the given connection can be navigated to.
     *
     * @param current The instance to check for navigability.
     * @returns True if the given connection can be focused.
     */
    isNavigable(current: RenderedConnection): boolean;
    /**
     * Returns whether the given object can be navigated from by this policy.
     *
     * @param current The object to check if this policy applies to.
     * @returns True if the object is a RenderedConnection.
     */
    isApplicable(current: any): current is RenderedConnection;
}
//# sourceMappingURL=connection_navigation_policy.d.ts.map