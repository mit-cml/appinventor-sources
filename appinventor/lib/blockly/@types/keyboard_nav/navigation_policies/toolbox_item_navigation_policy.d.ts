/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from '../../interfaces/i_focusable_node.js';
import type { INavigationPolicy } from '../../interfaces/i_navigation_policy.js';
import { type IToolboxItem } from '../../interfaces/i_toolbox_item.js';
/**
 * Set of rules controlling keyboard navigation from a toolbox item.
 */
export declare class ToolboxItemNavigationPolicy implements INavigationPolicy<IToolboxItem> {
    /**
     * Returns the first child of the given toolbox item.
     *
     * @param current The toolbox item to return the first child of.
     * @returns The child item of a collapsible toolbox item, otherwise null.
     */
    getFirstChild(current: IToolboxItem): IFocusableNode | null;
    /**
     * Returns the parent of the given toolbox item.
     *
     * @param current The toolbox item to return the parent of.
     * @returns The parent toolbox item of the given toolbox item, if any.
     */
    getParent(current: IToolboxItem): IFocusableNode | null;
    /**
     * Returns the next sibling of the given toolbox item.
     *
     * @param current The toolbox item to return the next sibling of.
     * @returns The next toolbox item, or null.
     */
    getNextSibling(current: IToolboxItem): IFocusableNode | null;
    /**
     * Returns the previous sibling of the given toolbox item.
     *
     * @param current The toolbox item to return the previous sibling of.
     * @returns The previous toolbox item, or null.
     */
    getPreviousSibling(current: IToolboxItem): IFocusableNode | null;
    /**
     * Returns the row ID of the given toolbox item.
     *
     * @param current The toolbox item to retrieve the row ID of.
     * @returns The row ID of the given toolbox item.
     */
    getRowId(current: IToolboxItem): string;
    /**
     * Returns whether or not the given toolbox item can be navigated to.
     *
     * @param current The instance to check for navigability.
     * @returns True if the given toolbox item can be focused.
     */
    isNavigable(current: IToolboxItem): boolean;
    private allParentsExpanded;
    /**
     * Returns whether the given object can be navigated from by this policy.
     *
     * @param current The object to check if this policy applies to.
     * @returns True if the object is an IToolboxItem.
     */
    isApplicable(current: any): current is IToolboxItem;
}
//# sourceMappingURL=toolbox_item_navigation_policy.d.ts.map