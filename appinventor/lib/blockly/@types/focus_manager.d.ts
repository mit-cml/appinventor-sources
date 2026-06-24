/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { IFocusableTree } from './interfaces/i_focusable_tree.js';
/**
 * Type declaration for returning focus to FocusManager upon completing an
 * ephemeral UI flow (such as a dialog).
 *
 * See FocusManager.takeEphemeralFocus for more details.
 */
export type ReturnEphemeralFocus = () => void;
/**
 * A per-page singleton that manages Blockly focus across one or more
 * IFocusableTrees, and bidirectionally synchronizes this focus with the DOM.
 *
 * Callers that wish to explicitly change input focus for select Blockly
 * components on the page should use the focus functions in this manager.
 *
 * The manager is responsible for handling focus events from the DOM (which may
 * may arise from users clicking on page elements) and ensuring that
 * corresponding IFocusableNodes are clearly marked as actively/passively
 * highlighted in the same way that this would be represented with calls to
 * focusNode().
 */
export declare class FocusManager {
    /**
     * The CSS class assigned to IFocusableNode elements that presently have
     * active DOM and Blockly focus.
     *
     * This should never be used directly. Instead, rely on FocusManager to ensure
     * nodes have active focus (either automatically through DOM focus or manually
     * through the various focus* methods provided by this class).
     *
     * It's recommended to not query using this class name, either. Instead, use
     * FocusableTreeTraverser or IFocusableTree's methods to find a specific node.
     */
    static readonly ACTIVE_FOCUS_NODE_CSS_CLASS_NAME = "blocklyActiveFocus";
    /**
     * The CSS class assigned to IFocusableNode elements that presently have
     * passive focus (that is, they were the most recent node in their relative
     * tree to have active focus--see ACTIVE_FOCUS_NODE_CSS_CLASS_NAME--and will
     * receive active focus again if their surrounding tree is requested to become
     * focused, i.e. using focusTree below).
     *
     * See ACTIVE_FOCUS_NODE_CSS_CLASS_NAME for caveats and limitations around
     * using this constant directly (generally it never should need to be used).
     */
    static readonly PASSIVE_FOCUS_NODE_CSS_CLASS_NAME = "blocklyPassiveFocus";
    private focusedNode;
    private previouslyFocusedNode;
    private registeredTrees;
    private currentlyHoldsEphemeralFocus;
    private lockFocusStateChanges;
    private recentlyLostAllFocus;
    private isUpdatingFocusedNode;
    constructor(addGlobalEventListener: (type: string, listener: EventListener) => void);
    /**
     * Registers a new IFocusableTree for automatic focus management.
     *
     * If the tree currently has an element with DOM focus, it will not affect the
     * internal state in this manager until the focus changes to a new,
     * now-monitored element/node.
     *
     * This function throws if the provided tree is already currently registered
     * in this manager. Use isRegistered to check in cases when it can't be
     * certain whether the tree has been registered.
     *
     * The tree's registration can be customized to configure automatic tab stops.
     * This specifically provides capability for the user to be able to tab
     * navigate to the root of the tree but only when the tree doesn't hold active
     * focus. If this functionality is disabled then the tree's root will
     * automatically be made focusable (but not tabbable) when it is first focused
     * in the same way as any other focusable node.
     *
     * @param tree The IFocusableTree to register.
     * @param rootShouldBeAutoTabbable Whether the root of this tree should be
     *     added as a top-level page tab stop when it doesn't hold active focus.
     */
    registerTree(tree: IFocusableTree, rootShouldBeAutoTabbable?: boolean): void;
    /**
     * Returns whether the specified tree has already been registered in this
     * manager using registerTree and hasn't yet been unregistered using
     * unregisterTree.
     */
    isRegistered(tree: IFocusableTree): boolean;
    /**
     * Returns the TreeRegistration for the specified tree, or null if the tree is
     * not currently registered.
     */
    private lookUpRegistration;
    /**
     * Unregisters a IFocusableTree from automatic focus management.
     *
     * If the tree had a previous focused node, it will have its highlight
     * removed. This function does NOT change DOM focus.
     *
     * This function throws if the provided tree is not currently registered in
     * this manager.
     *
     * This function will reset the tree's root element tabindex if the tree was
     * registered with automatic tab management.
     */
    unregisterTree(tree: IFocusableTree): void;
    /**
     * Returns the current IFocusableTree that has focus, or null if none
     * currently do.
     *
     * Note also that if ephemeral focus is currently captured (e.g. using
     * takeEphemeralFocus) then the returned tree here may not currently have DOM
     * focus.
     */
    getFocusedTree(): IFocusableTree | null;
    /**
     * Returns the current IFocusableNode with focus (which is always tied to a
     * focused IFocusableTree), or null if there isn't one.
     *
     * Note that this function will maintain parity with
     * IFocusableTree.getFocusedNode(). That is, if a tree itself has focus but
     * none of its non-root children do, this will return null but
     * getFocusedTree() will not.
     *
     * Note also that if ephemeral focus is currently captured (e.g. using
     * takeEphemeralFocus) then the returned node here may not currently have DOM
     * focus.
     */
    getFocusedNode(): IFocusableNode | null;
    /**
     * Focuses the specific IFocusableTree. This either means restoring active
     * focus to the tree's passively focused node, or focusing the tree's root
     * node.
     *
     * Note that if the specified tree already has a focused node then this will
     * not change any existing focus (unless that node has passive focus, then it
     * will be restored to active focus).
     *
     * See getFocusedNode for details on how other nodes are affected.
     *
     * @param focusableTree The tree that should receive active
     *     focus.
     */
    focusTree(focusableTree: IFocusableTree): void;
    /**
     * Focuses DOM input on the specified node, and marks it as actively focused.
     *
     * Any previously focused node will be updated to be passively highlighted (if
     * it's in a different focusable tree) or blurred (if it's in the same one).
     *
     * **Important**: If the provided node is not able to be focused (e.g. its
     * canBeFocused() method returns false), it will be ignored and any existing
     * focus state will remain unchanged.
     *
     * Note that this may update the specified node's element's tabindex to ensure
     * that it can be properly read out by screenreaders while focused.
     *
     * The focused node will not be automatically scrolled into view.
     *
     * @param focusableNode The node that should receive active focus.
     */
    focusNode(focusableNode: IFocusableNode): void;
    /**
     * Ephemerally captures focus for a specific element until the returned lambda
     * is called. This is expected to be especially useful for ephemeral UI flows
     * like dialogs.
     *
     * IMPORTANT: the returned lambda *must* be called, otherwise automatic focus
     * will no longer work anywhere on the page. It is highly recommended to tie
     * the lambda call to the closure of the corresponding UI so that if input is
     * manually changed to an element outside of the ephemeral UI, the UI should
     * close and automatic input restored. Note that this lambda must be called
     * exactly once and that subsequent calls will throw an error.
     *
     * Note that the manager will continue to track DOM input signals even when
     * ephemeral focus is active, but it won't actually change node state until
     * the returned lambda is called. Additionally, only 1 ephemeral focus context
     * can be active at any given time (attempting to activate more than one
     * simultaneously will result in an error being thrown).
     *
     * This method does not scroll the ephemerally focused element into view.
     */
    takeEphemeralFocus(focusableElement: HTMLElement | SVGElement): ReturnEphemeralFocus;
    /**
     * @returns whether something is currently holding ephemeral focus
     */
    ephemeralFocusTaken(): boolean;
    /**
     * Ensures that the manager is currently allowing operations that change its
     * internal focus state (such as via focusNode()).
     *
     * If the manager is currently not allowing state changes, an exception is
     * thrown.
     */
    private ensureManagerIsUnlocked;
    /**
     * Updates the internally tracked focused node to the specified node, or null
     * if focus is being lost. This also updates previous focus tracking.
     *
     * @param newFocusedNode The new node to set as focused.
     */
    private updateFocusedNode;
    /**
     * Defocuses the current actively focused node tracked by the manager, iff
     * there's a node being tracked and the manager doesn't have ephemeral focus.
     */
    private defocusCurrentFocusedNode;
    /**
     * Marks the specified node as actively focused, also calling related
     * lifecycle callback methods for both the node and its parent tree. This
     * ensures that the node is properly styled to indicate its active focus.
     *
     * This does not change the manager's currently tracked node, nor does it
     * change any other nodes.
     *
     * @param node The node to be actively focused.
     * @param prevTree The tree of the previously actively focused node, or null
     *     if there wasn't a previously actively focused node.
     */
    private activelyFocusNode;
    /**
     * Marks the specified node as passively focused, also calling related
     * lifecycle callback methods for both the node and its parent tree. This
     * ensures that the node is properly styled to indicate its passive focus.
     *
     * This does not change the manager's currently tracked node, nor does it
     * change any other nodes.
     *
     * @param node The node to be passively focused.
     * @param nextTree The tree of the node receiving active focus, or null if no
     *     node will be actively focused.
     */
    private passivelyFocusNode;
    /**
     * Updates the node's styling to indicate that it should have an active focus
     * indicator.
     *
     * @param node The node to be styled for active focus.
     */
    private setNodeToVisualActiveFocus;
    /**
     * Updates the node's styling to indicate that it should have a passive focus
     * indicator.
     *
     * @param node The node to be styled for passive focus.
     */
    private setNodeToVisualPassiveFocus;
    /**
     * Removes any active/passive indicators for the specified node.
     *
     * @param node The node which should have neither passive nor active focus
     *     indication.
     */
    private removeHighlight;
    private static focusManager;
    /**
     * Returns the page-global FocusManager.
     *
     * The returned instance is guaranteed to not change across function calls,
     * but may change across page loads.
     */
    static getFocusManager(): FocusManager;
}
/** Convenience function for FocusManager.getFocusManager. */
export declare function getFocusManager(): FocusManager;
//# sourceMappingURL=focus_manager.d.ts.map