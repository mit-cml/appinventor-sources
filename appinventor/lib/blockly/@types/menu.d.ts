/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { MenuSeparator } from './menu_separator.js';
import { MenuItem } from './menuitem.js';
import * as aria from './utils/aria.js';
import { Coordinate } from './utils/coordinate.js';
import type { Size } from './utils/size.js';
/**
 * A basic menu class.
 */
export declare class Menu {
    /**
     * Array of menu items and separators.
     */
    private readonly menuItems;
    /**
     * Coordinates of the pointerdown event that caused this menu to open. Used to
     * prevent the consequent pointerup event due to a simple click from
     * activating a menu item immediately.
     */
    openingCoords: Coordinate | null;
    /**
     * This is the element that we will listen to the real focus events on.
     * A value of null means no menu item is highlighted.
     */
    private highlightedItem;
    /** Pointer over event data. */
    private pointerMoveHandler;
    /** Click event data. */
    private clickHandler;
    /** Pointer enter event data. */
    private pointerEnterHandler;
    /** Pointer leave event data. */
    private pointerLeaveHandler;
    /** Key down event data. */
    private onKeyDownHandler;
    /** The menu's root DOM element. */
    private element;
    /** ARIA name for this menu. */
    private roleName;
    /** Constructs a new Menu instance. */
    constructor();
    /**
     * Add a new menu item to the bottom of this menu.
     *
     * @param menuItem Menu item or separator to append.
     * @internal
     */
    addChild(menuItem: MenuItem | MenuSeparator): void;
    /**
     * Creates the menu DOM.
     *
     * @param container Element upon which to append this menu.
     * @returns The menu's root DOM element.
     */
    render(container: Element): HTMLDivElement;
    /**
     * Gets the menu's element.
     *
     * @returns The DOM element.
     * @internal
     */
    getElement(): HTMLDivElement | null;
    /**
     * Focus the menu element.
     *
     * @internal
     */
    focus(): void;
    /** Blur the menu element. */
    private blur;
    /**
     * Set the menu accessibility role.
     *
     * @param roleName role name.
     * @internal
     */
    setRole(roleName: aria.Role): void;
    /** Dispose of this menu. */
    dispose(): void;
    /**
     * Returns the child menu item that owns the given DOM element,
     * or null if no such menu item is found.
     *
     * @param elem DOM element whose owner is to be returned.
     * @returns Menu item for which the DOM element belongs to.
     */
    private getMenuItem;
    /**
     * Highlights the given menu item, or clears highlighting if null.
     *
     * @param item Item to highlight, or null.
     * @internal
     */
    setHighlighted(item: MenuItem | null): void;
    /**
     * Highlights the next highlightable item (or the first if nothing is
     * currently highlighted).
     *
     * @internal
     */
    highlightNext(): void;
    /**
     * Highlights the previous highlightable item (or the last if nothing is
     * currently highlighted).
     *
     * @internal
     */
    highlightPrevious(): void;
    /** Highlights the first highlightable item. */
    private highlightFirst;
    /** Highlights the last highlightable item. */
    private highlightLast;
    /**
     * Helper function that manages the details of moving the highlight among
     * child menuitems in response to keyboard events.
     *
     * @param startIndex Start index.
     * @param delta Step direction: 1 to go down, -1 to go up.
     */
    private highlightHelper;
    /**
     * Handles pointermove events. Highlight menu items as the user hovers over
     * them.
     *
     * @param e Pointer event to handle.
     */
    private handlePointerMove;
    /**
     * Handles click events. Pass the event onto the child menuitem to handle.
     *
     * @param e Click event to handle.
     */
    private handleClick;
    /**
     * Handles pointer enter events. Focus the element.
     *
     * @param _e Pointer event to handle.
     */
    private handlePointerEnter;
    /**
     * Handles pointer leave events by clearing the active highlight.
     *
     * @param _e Pointer event to handle.
     */
    private handlePointerLeave;
    /**
     * Attempts to handle a keyboard event.
     *
     * @param e Key event to handle.
     */
    private handleKeyEvent;
    /**
     * Get the size of a rendered menu.
     *
     * @returns Object with width and height properties.
     * @internal
     */
    getSize(): Size;
    /**
     * Returns the action menu items (omitting separators) in this menu.
     *
     * @returns The MenuItem objects displayed in this menu.
     */
    private getMenuItems;
}
//# sourceMappingURL=menu.d.ts.map