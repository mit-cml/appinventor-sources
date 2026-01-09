/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFlyout } from '../interfaces/i_flyout.js';
import type { IFocusableNode } from '../interfaces/i_focusable_node.js';
import type { INavigationPolicy } from '../interfaces/i_navigation_policy.js';
/**
 * Generic navigation policy that navigates between items in the flyout.
 */
export declare class FlyoutNavigationPolicy<T> implements INavigationPolicy<T> {
    private policy;
    private flyout;
    /**
     * Creates a new FlyoutNavigationPolicy instance.
     *
     * @param policy The policy to defer to for parents/children.
     * @param flyout The flyout this policy will control navigation in.
     */
    constructor(policy: INavigationPolicy<T>, flyout: IFlyout);
    /**
     * Returns null to prevent navigating into flyout items.
     *
     * @param _current The flyout item to navigate from.
     * @returns Null to prevent navigating into flyout items.
     */
    getFirstChild(_current: T): IFocusableNode | null;
    /**
     * Returns the parent of the given flyout item.
     *
     * @param current The flyout item to navigate from.
     * @returns The parent of the given flyout item.
     */
    getParent(current: T): IFocusableNode | null;
    /**
     * Returns the next item in the flyout relative to the given item.
     *
     * @param current The flyout item to navigate from.
     * @returns The flyout item following the given one.
     */
    getNextSibling(current: T): IFocusableNode | null;
    /**
     * Returns the previous item in the flyout relative to the given item.
     *
     * @param current The flyout item to navigate from.
     * @returns The flyout item preceding the given one.
     */
    getPreviousSibling(current: T): IFocusableNode | null;
    /**
     * Returns whether or not the given flyout item can be navigated to.
     *
     * @param current The instance to check for navigability.
     * @returns True if the given flyout item can be focused.
     */
    isNavigable(current: T): boolean;
    /**
     * Returns whether the given object can be navigated from by this policy.
     *
     * @param current The object to check if this policy applies to.
     * @returns True if the object is a BlockSvg.
     */
    isApplicable(current: any): current is T;
}
//# sourceMappingURL=flyout_navigation_policy.d.ts.map