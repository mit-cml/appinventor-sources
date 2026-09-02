/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * ARIA state values for LivePriority.
 * Copied from Closure's goog.a11y.aria.LivePriority
 */
export declare enum LiveRegionAssertiveness {
    ASSERTIVE = "assertive",
    OFF = "off",
    POLITE = "polite"
}
/**
 * Customization options that can be passed when using `announceDynamicAriaState`.
 */
export interface DynamicAnnouncementOptions {
    /** The custom ARIA `Role` that should be used for the announcement container. */
    role?: Role;
    /**
     * How assertive the announcement should be.
     *
     * Important*: It was found through testing that `ASSERTIVE` announcements are
     * often outright ignored by some screen readers, so it's generally recommended
     * to always use `POLITE` unless specifically tested across supported readers.
     */
    assertiveness?: LiveRegionAssertiveness;
}
/**
 * A valid ARIA role for a Blockly DOM element. See also setRole() and getRole().
 *
 * This should be used instead of directly setting an element's role attribute.
 */
export declare enum Role {
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/application_role. */
    APPLICATION = "application",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/button_role. */
    BUTTON = "button",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/checkbox_role. */
    CHECKBOX = "checkbox",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/dialog_role. */
    DIALOG = "dialog",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/figure_role. */
    FIGURE = "figure",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/generic_role. */
    GENERIC = "generic",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/grid_role. */
    GRID = "grid",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/gridcell_role. */
    GRIDCELL = "gridcell",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/group_role. */
    GROUP = "group",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/list_role. */
    LIST = "list",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/listbox_role. */
    LISTBOX = "listbox",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/listitem_role. */
    LISTITEM = "listitem",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/menu_role. */
    MENU = "menu",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/menuitem_role. */
    MENUITEM = "menuitem",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/none_role. */
    NONE = "none",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/option_role. */
    OPTION = "option",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/region_role. */
    REGION = "region",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/row_role. */
    ROW = "row",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/separator_role. */
    SEPARATOR = "separator",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/status_role. */
    STATUS = "status",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/textbox_role. */
    TEXTBOX = "textbox",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/tree_role. */
    TREE = "tree",
    /** See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Roles/treeitem_role. */
    TREEITEM = "treeitem"
}
/**
 * A possible ARIA attribute state for a Blockly DOM element. See also setState() and getState().
 *
 * This should be used instead of directly setting aria-* attributes on elements.
 */
export declare enum State {
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-activedescendant.
     *
     * Value: ID of a DOM element.
     */
    ACTIVEDESCENDANT = "activedescendant",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-atomic.
     *
     * Value: one of {true, false}.
     */
    ATOMIC = "atomic",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-checked.
     *
     * Value: one of {true, false, mixed, undefined}.
     */
    CHECKED = "checked",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-controls.
     *
     * Value: an array of element IDs.
     */
    CONTROLS = "controls",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-disabled.
     *
     * Value: one of {true, false}.
     */
    DISABLED = "disabled",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-expanded.
     *
     * Value: one of {true, false, undefined}.
     */
    EXPANDED = "expanded",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-haspopup.
     *
     * Value: one of {true, false, menu, listbox, tree, grid, dialog}.
     */
    HASPOPUP = "haspopup",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-hidden.
     *
     * Value: one of {true, false,undefined}.
     */
    HIDDEN = "hidden",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-invalid.
     *
     * Value: one of {true, false, grammar, spelling}.
     */
    INVALID = "invalid",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-label.
     *
     * Value: a string.
     */
    LABEL = "label",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-labelledby.
     *
     * Value: an array of element IDs.
     */
    LABELLEDBY = "labelledby",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-level.
     *
     * Value: an integer.
     */
    LEVEL = "level",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-live.
     *
     * Value: one of {polite, assertive, off}.
     */
    LIVE = "live",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-pressed.
     *
     * Value: one of {true, false, mixed, undefined}.
     */
    PRESSED = "pressed",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-roledescription.
     *
     * Value: a string.
     */
    ROLEDESCRIPTION = "roledescription",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-selected.
     *
     * Value:one of {true, false, undefined}.
     */
    SELECTED = "selected",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-valuemax.
     *
     * Value: a number representing the maximum allowed value for a range widget.
     */
    VALUEMAX = "valuemax",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-valuemin.
     *
     * Value: a number representing the minimum allowed value for a range widget.
     */
    VALUEMIN = "valuemin",
    /**
     * See https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/Reference/Attributes/aria-owns
     *
     * Value: a space-separated list of element IDs that are owned by the current element.
     */
    OWNS = "owns"
}
/**
 * Used to control how verbose generated a11y labels are.
 */
export declare enum Verbosity {
    TERSE = 0,
    STANDARD = 1,
    LOQUACIOUS = 2
}
/**
 * Removes the ARIA role from an element.
 *
 * Similar to Closure's goog.a11y.aria.removeRole
 *
 * @param element DOM element to remove the role from.
 */
export declare function removeRole(element: Element): void;
/**
 * Updates the specific role for the specified element.
 *
 * @param element The element whose ARIA role should be changed.
 * @param roleName The new role for the specified element, or null if its role
 *     should be cleared.
 */
export declare function setRole(element: Element, roleName: Role | null): void;
/**
 * Returns the ARIA role of the specified element, or null if it either doesn't
 * have a designated role or if that role is unknown.
 *
 * @param element The element from which to retrieve its ARIA role.
 * @returns The ARIA role of the element, or null if undefined or unknown.
 */
export declare function getRole(element: Element): Role | null;
/**
 * Sets the specified ARIA state by its name and value for the specified
 * element.
 *
 * Note that the type of value is not validated against the specific type of
 * state being changed, so it's up to callers to ensure the correct value is
 * used for the given state.
 *
 * @param element The element whose ARIA state may be changed.
 * @param stateName The state to change.
 * @param value The new value to specify for the provided state.
 */
export declare function setState(element: Element, stateName: State, value: string | boolean | number | string[]): void;
/**
 * Clears the specified ARIA state by removing any related attributes from the
 * specified element that have been set using setState().
 *
 * @param element The element whose ARIA state may be changed.
 * @param stateName The state to clear from the provided element.
 */
export declare function clearState(element: Element, stateName: State): void;
/**
 * Returns a string representation of the specified state for the specified
 * element, or null if it's not defined or specified.
 *
 * Note that an explicit set state of 'null' will return the 'null' string, not
 * the value null.
 *
 * @param element The element whose state is being retrieved.
 * @param stateName The state to retrieve.
 * @returns The string representation of the requested state for the specified
 *     element, or null if not defined.
 */
export declare function getState(element: Element, stateName: State): string | null;
/**
 * Creates an ARIA live region under the specified parent Element to be used
 * for all dynamic announcements via `announceDynamicAriaState`. This must be
 * called only once and before any dynamic announcements can be made.
 *
 * @param parent The container element to which the live region will be appended.
 */
export declare function initializeGlobalAriaLiveRegion(parent: HTMLDivElement): void;
/**
 * Requests that the specified text be read to the user if a screen reader is
 * currently active.
 *
 * This relies on a centrally managed ARIA live region that is hidden from the
 * visual DOM. This live region is designed to try and ensure the text is read,
 * including if the same text is issued multiple times consecutively. Note that
 * `initializeGlobalAriaLiveRegion` must be called before this can be used.
 *
 * Callers should use this judiciously. It's often considered bad practice to
 * over-announce information that can be inferred from other sources on the page,
 * so this ought to be used only when certain context cannot be easily determined
 * (such as dynamic states that may not have perfect ARIA representations or
 * indications).
 *
 * @param text The text to read to the user.
 * @param options Custom options to configure the announcement. This defaults to
 *    the status role and polite assertiveness.
 */
export declare function announceDynamicAriaState(text: string, options?: DynamicAnnouncementOptions): void;
//# sourceMappingURL=aria.d.ts.map