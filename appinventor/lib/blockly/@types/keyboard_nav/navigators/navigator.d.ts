/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from '../../block_svg.js';
import type { IFocusableNode } from '../../interfaces/i_focusable_node.js';
import type { INavigationPolicy } from '../../interfaces/i_navigation_policy.js';
type RuleList<T> = INavigationPolicy<T>[];
/**
 * Representation of the direction of travel within a navigation context.
 */
export declare enum NavigationDirection {
    NEXT = 0,
    PREVIOUS = 1,
    IN = 2,
    OUT = 3
}
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
    /** Whether or not navigation loops around when reaching the end. */
    protected navigationLoops: boolean;
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
    /**
     * Returns the previous node relative to the given node.
     *
     * @param node The node to navigate relative to, defaults to the currently
     *     focused node.
     * @returns The previous node, generally on the "row" visually above the
     *     specified node, or null if there is none.
     */
    getPreviousNode(node?: IFocusableNode | null): IFocusableNode | null;
    /**
     * Returns the node to the left of the given node.
     *
     * @param node The node to navigate relative to, defaults to the currently
     *     focused node.
     * @returns The node to the left of the given node, within the same visual
     *     "row" as the given node, or null if there is none.
     */
    getOutNode(node?: IFocusableNode | null): IFocusableNode | null;
    /**
     * Returns next node relative to the given node.
     *
     * @param node The node to navigate relative to, defaults to the currently
     *     focused node.
     * @returns The next node, generally on the "row" visually below the
     *     specified node, or null if there is none.
     */
    getNextNode(node?: IFocusableNode | null): IFocusableNode | null;
    /**
     * Returns the node to the right of the given node.
     *
     * @param node The node to navigate relative to, defaults to the currently
     *     focused node.
     * @returns The node to the right of the given node, within the same visual
     *     "row" as the given node, or null if there is none.
     */
    getInNode(node?: IFocusableNode | null): IFocusableNode | null;
    /**
     * Returns the previous sibling/parent node relative to the given node.
     *
     * @param startNode The node that navigation is starting from.
     * @param node The node to navigate relative to.
     * @param direction The direction to navigate, either OUT or PREVIOUS.
     * @param visitedNodes Set of already-visited nodes used to avoid cycles,
     *     should not be specified by the caller.
     * @returns The previous sibling/parent node, or null if there is none or a
     *     node was not provided.
     */
    private getPreviousNodeImpl;
    /**
     * Returns the next sibling/child node relative to the given node.
     *
     * @param startNode The node that navigation is starting from.
     * @param node The node to navigate relative to.
     * @param direction The direction to navigate, either IN or NEXT.
     * @param visitedNodes Set of already-visited nodes used to avoid cycles,
     *     should not be specified by the caller.
     * @returns The next sibling/child node, or null if there is none or a
     *     node was not provided.
     */
    private getNextNodeImpl;
    private getRightMostChild;
    /**
     * Sets whether or not navigation should loop around when reaching the end
     * of the workspace.
     *
     * @param loops True if navigation should loop around, otherwise false.
     */
    setNavigationLoops(loops: boolean): void;
    /**
     * Returns whether or not navigation loops around when reaching the end of
     * the workspace.
     */
    getNavigationLoops(): boolean;
    /**
     * Get the first navigable node on the workspace, or null if none exist.
     *
     * @returns The first navigable node on the workspace, or null.
     */
    getFirstNode(): IFocusableNode | null;
    /**
     * Get the last navigable node on the workspace, or null if none exist.
     *
     * @returns The last navigable node on the workspace, or null.
     */
    getLastNode(): IFocusableNode | null;
    /**
     * Determines whether navigation is allowed between two nodes.
     *
     * @param current The starting node for proposed navigation.
     * @param candidate The proposed destination node.
     * @param direction The direction in which the user is navigating.
     * @returns True if navigation should be allowed to proceed, or false to find
     *     a different candidate.
     */
    protected transitionAllowed(current: IFocusableNode, candidate: IFocusableNode, direction: NavigationDirection): boolean;
    /**
     * Returns the leftmost node in the same row as the given node.
     *
     * @param node The node to find the leftmost sibling of.
     * @returns The leftmost sibling of the given node in the same row.
     */
    private getLeftmostSibling;
    /**
     * Returns the last node in a stack of blocks or other top-level workspace
     * entity.
     *
     * @param stackRoot A top-level item to get the last node of.
     * @param stopIfFound A sentinel node that terminates traversal if
     *     encountered; typically the root node of the next stack.
     * @returns The last node in the given stack.
     */
    private getLastNodeInStack;
    private getRowId;
    /**
     * Returns the next/previous stack relative to the given element's stack.
     *
     * @internal
     * @param current The element whose stack will be navigated relative to.
     * @param delta The difference in index to navigate; positive values navigate
     *     to the nth next stack, while negative values navigate to the nth
     *     previous stack.
     * @returns The first element in the stack offset by `delta` relative to the
     *     current element's stack, or the last element in the stack offset by
     * `delta` relative to the current element's stack when navigating backwards.
     */
    navigateStacks(current: IFocusableNode, delta: number): IFocusableNode | null;
    /**
     * Returns a list of all top-level focusable items on the given node's
     * focusable tree.
     *
     * @param current The node whose root focusable tree to retrieve the top-level
     *     items of.
     * @returns A list of all top-level items on the given node's parent tree.
     */
    protected getTopLevelItems(current: IFocusableNode): IFocusableNode[];
    /**
     * Returns whether or not the given node is navigable.
     *
     * @param node A focusable node to check the navigability of.
     * @returns True if the node is navigable, otherwise false.
     */
    protected isNavigable(node: IFocusableNode): boolean | undefined;
    /**
     * Returns the block that the given node is a child of.
     *
     * @returns The parent block of the node if any, otherwise null.
     */
    getSourceBlockFromNode(node: IFocusableNode | null): BlockSvg | null;
}
export {};
//# sourceMappingURL=navigator.d.ts.map