/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from '../interfaces/i_focusable_node.js';
import type { IFocusableTree } from '../interfaces/i_focusable_tree.js';
/**
 * A helper utility for IFocusableTree implementations to aid with common
 * tree traversals.
 */
export declare class FocusableTreeTraverser {
    private static readonly ACTIVE_CLASS_NAME;
    private static readonly PASSIVE_CSS_CLASS_NAME;
    private static readonly ACTIVE_FOCUS_NODE_CSS_SELECTOR;
    private static readonly PASSIVE_FOCUS_NODE_CSS_SELECTOR;
    /**
     * Returns the current IFocusableNode that is styled (and thus represented) as
     * having either passive or active focus, only considering HTML and SVG
     * elements.
     *
     * This can match against the tree's root.
     *
     * Note that this will never return a node from a nested sub-tree as that tree
     * should specifically be used to retrieve its focused node.
     *
     * @param tree The IFocusableTree in which to search for a focused node.
     * @returns The IFocusableNode currently with focus, or null if none.
     */
    static findFocusedNode(tree: IFocusableTree): IFocusableNode | null;
    /**
     * Returns the IFocusableNode corresponding to the specified HTML or SVG
     * element iff it's the root element or a descendent of the root element of
     * the specified IFocusableTree.
     *
     * If the element exists within the specified tree's DOM structure but does
     * not directly correspond to a node, the nearest parent node (or the tree's
     * root) will be returned to represent the provided element.
     *
     * If the tree contains another nested IFocusableTree, the nested tree may be
     * traversed but its nodes will never be returned here per the contract of
     * IFocusableTree.lookUpFocusableNode.
     *
     * The provided element must have a non-null, non-empty ID that conforms to
     * the contract mentioned in IFocusableNode.
     *
     * @param element The HTML or SVG element being sought.
     * @param tree The tree under which the provided element may be a descendant.
     * @returns The matching IFocusableNode, or null if there is no match.
     */
    static findFocusableNodeFor(element: HTMLElement | SVGElement, tree: IFocusableTree): IFocusableNode | null;
}
//# sourceMappingURL=focusable_tree_traverser.d.ts.map