/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * The KeyboardNavigationController handles coordinating Blockly-wide
 * keyboard navigation behavior, such as enabling/disabling full
 * cursor visualization.
 */
export declare class KeyboardNavigationController {
    /** Whether the user is actively using keyboard navigation. */
    private isActive;
    /** Css class name added to body if keyboard nav is active. */
    private activeClassName;
    /**
     * Sets whether a user is actively using keyboard navigation.
     *
     * If they are, apply a css class to the entire page so that
     * focused items can apply additional styling for keyboard users.
     *
     * Note that since enabling keyboard navigation presents significant UX changes
     * (such as cursor visualization and move mode), callers should take care to
     * only set active keyboard navigation when they have a high confidence in that
     * being the correct state. In general, in any given mouse or key input situation
     * callers can choose one of three paths:
     * 1. Do nothing. This should be the choice for neutral actions that don't
     *    predominantly imply keyboard or mouse usage (such as clicking to select a block).
     * 2. Disable keyboard navigation. This is the best choice when a user is definitely
     *    predominantly using the mouse (such as using a right click to open the context menu).
     * 3. Enable keyboard navigation. This is the best choice when there's high confidence
     *    a user actually intends to use it (such as attempting to use the arrow keys to move
     *    around).
     *
     * @param isUsing
     */
    setIsActive(isUsing?: boolean): void;
    /**
     * @returns true if the user is actively using keyboard navigation
     * (e.g., has recently taken some action that is only relevant to keyboard users)
     */
    getIsActive(): boolean;
    /** Adds or removes the css class that indicates keyboard navigation is active. */
    private updateActiveVisualization;
}
/** Singleton instance of the keyboard navigation controller. */
export declare const keyboardNavigationController: KeyboardNavigationController;
//# sourceMappingURL=keyboard_navigation_controller.d.ts.map