/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { Icon } from '../icons/icon.js';
import type { IFocusableNode } from '../interfaces/i_focusable_node.js';
import type { INavigationPolicy } from '../interfaces/i_navigation_policy.js';
/**
 * Set of rules controlling keyboard navigation from an icon.
 */
export declare class IconNavigationPolicy implements INavigationPolicy<Icon> {
    /**
     * Returns the first child of the given icon.
     *
     * @param current The icon to return the first child of.
     * @returns Null.
     */
    getFirstChild(current: Icon): IFocusableNode | null;
    /**
     * Returns the parent of the given icon.
     *
     * @param current The icon to return the parent of.
     * @returns The source block of the given icon.
     */
    getParent(current: Icon): IFocusableNode | null;
    /**
     * Returns the next peer node of the given icon.
     *
     * @param current The icon to find the following element of.
     * @returns The next icon, field or input following this icon, if any.
     */
    getNextSibling(current: Icon): IFocusableNode | null;
    /**
     * Returns the previous peer node of the given icon.
     *
     * @param current The icon to find the preceding element of.
     * @returns The icon's previous icon, if any.
     */
    getPreviousSibling(current: Icon): IFocusableNode | null;
    /**
     * Returns whether or not the given icon can be navigated to.
     *
     * @param current The instance to check for navigability.
     * @returns True if the given icon can be focused.
     */
    isNavigable(current: Icon): boolean;
    /**
     * Returns whether the given object can be navigated from by this policy.
     *
     * @param current The object to check if this policy applies to.
     * @returns True if the object is an Icon.
     */
    isApplicable(current: any): current is Icon;
}
//# sourceMappingURL=icon_navigation_policy.d.ts.map