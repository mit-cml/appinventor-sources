/**
 * @license
 * Copyright 2011 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * A type which can define a tooltip.
 * Either a string, an object containing a tooltip property, or a function which
 * returns either a string, or another arbitrarily nested function which
 * eventually unwinds to a string.
 */
export type TipInfo = string | {
    tooltip: any;
} | (() => TipInfo | string);
/**
 * A function that renders custom tooltip UI.
 * 1st parameter: the div element to render content into.
 * 2nd parameter: the element being moused over (i.e., the element for which the
 * tooltip should be shown).
 */
export type CustomTooltip = (p1: Element, p2: Element) => any;
/**
 * Sets a custom function that will be called if present instead of the default
 * tooltip UI.
 *
 * @param customFn A custom tooltip used to render an alternate tooltip UI.
 */
export declare function setCustomTooltip(customFn: CustomTooltip): void;
/**
 * Gets the custom tooltip function.
 *
 * @returns The custom tooltip function, if defined.
 */
export declare function getCustomTooltip(): CustomTooltip | undefined;
/**
 * Returns whether or not a tooltip is showing
 *
 * @returns True if a tooltip is showing
 */
export declare function isVisible(): boolean;
/**
 * Maximum width (in characters) of a tooltip.
 */
export declare const LIMIT = 50;
/**
 * Horizontal offset between mouse cursor and tooltip.
 */
export declare const OFFSET_X = 0;
/**
 * Vertical offset between mouse cursor and tooltip.
 */
export declare const OFFSET_Y = 10;
/**
 * Radius mouse can move before killing tooltip.
 */
export declare const RADIUS_OK = 10;
/**
 * Delay before tooltip appears.
 */
export declare const HOVER_MS = 750;
/**
 * Horizontal padding between tooltip and screen edge.
 */
export declare const MARGINS = 5;
/**
 * Returns the HTML tooltip container.
 *
 * @returns The HTML tooltip container.
 */
export declare function getDiv(): HTMLDivElement | null;
/**
 * Returns the tooltip text for the given element.
 *
 * @param object The object to get the tooltip text of.
 * @returns The tooltip text of the element.
 */
export declare function getTooltipOfObject(object: any | null): string;
/**
 * Create the tooltip div and inject it onto the page.
 */
export declare function createDom(): void;
/**
 * Binds the required mouse events onto an SVG element.
 *
 * @param element SVG element onto which tooltip is to be bound.
 */
export declare function bindMouseEvents(element: Element): void;
/**
 * Unbinds tooltip mouse events from the SVG element.
 *
 * @param element SVG element onto which tooltip is bound.
 */
export declare function unbindMouseEvents(element: Element | null): void;
/**
 * Dispose of the tooltip.
 *
 * @internal
 */
export declare function dispose(): void;
/**
 * Hide the tooltip.
 */
export declare function hide(): void;
/**
 * Hide any in-progress tooltips and block showing new tooltips until the next
 * call to unblock().
 *
 * @internal
 */
export declare function block(): void;
/**
 * Unblock tooltips: allow them to be scheduled and shown according to their own
 * logic.
 *
 * @internal
 */
export declare function unblock(): void;
//# sourceMappingURL=tooltip.d.ts.map