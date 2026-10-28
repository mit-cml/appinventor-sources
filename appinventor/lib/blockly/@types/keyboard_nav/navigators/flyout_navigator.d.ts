/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFlyout } from '../../interfaces/i_flyout.js';
import type { IFocusableNode } from '../../interfaces/i_focusable_node.js';
import { Navigator } from './navigator.js';
/**
 * Navigator that handles keyboard navigation within a flyout.
 */
export declare class FlyoutNavigator extends Navigator {
    protected flyout: IFlyout;
    constructor(flyout: IFlyout);
    /**
     * Returns the parent toolbox item or previous flyout item when navigating out
     * (left arrow) from a flyout.
     *
     * @param node The flyout item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     flyout's layout; false (default) to take it into account.
     */
    getOutNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns the parent toolbox item or next flyout item when navigating in
     * (right arrow) from a flyout.
     *
     * @param node The flyout item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     flyout's layout; false (default) to take it into account.
     */
    getInNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns the parent toolbox item or next flyout item when navigating next
     * (down arrow) from a flyout.
     *
     * @param node The flyout item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     flyout's layout; false (default) to take it into account.
     */
    getNextNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns the parent toolbox item or previous flyout item when navigating
     * previous (up arrow) from a flyout.
     *
     * @param node The flyout item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     flyout's layout; false (default) to take it into account.
     */
    getPreviousNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns a list of top-level navigable flyout items.
     */
    protected getTopLevelItems(): IFocusableNode[];
    /**
     * Returns whether or not the given node is navigable.
     *
     * @param node A focusable node to check the navigability of.
     * @returns True if the node is navigable, otherwise false.
     */
    protected isNavigable(node: IFocusableNode): boolean | undefined;
}
//# sourceMappingURL=flyout_navigator.d.ts.map