/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { INavigationPolicy } from './interfaces/i_navigation_policy.js';
type RuleList<T> = INavigationPolicy<T>[];
/**
 * Class responsible for determining where focus should move in response to
 * keyboard navigation commands.
 */
export declare class Navigator {
    /**
     * Map from classes to a corresponding ruleset to handle navigation from
     * instances of that class.
     */
    protected rules: RuleList<any>;
    /**
     * Adds a navigation ruleset to this Navigator.
     *
     * @param policy A ruleset that determines where focus should move starting
     *     from an instance of its managed class.
     */
    addNavigationPolicy(policy: INavigationPolicy<any>): void;
    /**
     * Returns the navigation ruleset associated with the given object instance's
     * class.
     *
     * @param current An object to retrieve a navigation ruleset for.
     * @returns The navigation ruleset of objects of the given object's class, or
     *     undefined if no ruleset has been registered for the object's class.
     */
    private get;
    /**
     * Returns the first child of the given object instance, if any.
     *
     * @param current The object to retrieve the first child of.
     * @returns The first child node of the given object, if any.
     */
    getFirstChild(current: IFocusableNode): IFocusableNode | null;
    /**
     * Returns the parent of the given object instance, if any.
     *
     * @param current The object to retrieve the parent of.
     * @returns The parent node of the given object, if any.
     */
    getParent(current: IFocusableNode): IFocusableNode | null;
    /**
     * Returns the next sibling of the given object instance, if any.
     *
     * @param current The object to retrieve the next sibling node of.
     * @returns The next sibling node of the given object, if any.
     */
    getNextSibling(current: IFocusableNode): IFocusableNode | null;
    /**
     * Returns the previous sibling of the given object instance, if any.
     *
     * @param current The object to retrieve the previous sibling node of.
     * @returns The previous sibling node of the given object, if any.
     */
    getPreviousSibling(current: IFocusableNode): IFocusableNode | null;
}
export {};
//# sourceMappingURL=navigator.d.ts.map