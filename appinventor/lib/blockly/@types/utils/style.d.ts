/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { Coordinate } from './coordinate.js';
import { Rect } from './rect.js';
import { Size } from './size.js';
/**
 * Gets the height and width of an element.
 * Similar to Closure's goog.style.getSize
 *
 * @param element Element to get size of.
 * @returns Object with width/height properties.
 */
export declare function getSize(element: Element): Size;
/**
 * Private version of getSize for stubbing in tests.
 */
declare function getSizeInternal(element: Element): Size;
/**
 * Retrieves a computed style value of a node. It returns empty string
 * if the property requested is an SVG one and it has not been
 * explicitly set (firefox and webkit).
 *
 * Copied from Closure's goog.style.getComputedStyle
 *
 * @param element Element to get style of.
 * @param property Property to get (camel-case).
 * @returns Style value.
 */
export declare function getComputedStyle(element: Element, property: string): string;
/**
 * Returns a Coordinate object relative to the top-left of the HTML document.
 * Similar to Closure's goog.style.getPageOffset
 *
 * @param el Element to get the page offset for.
 * @returns The page offset.
 */
export declare function getPageOffset(el: Element): Coordinate;
/**
 * Calculates the viewport coordinates relative to the document.
 * Similar to Closure's goog.style.getViewportPageOffset
 *
 * @returns The page offset of the viewport.
 */
export declare function getViewportPageOffset(): Coordinate;
/**
 * Gets the computed border widths (on all sides) in pixels
 * Copied from Closure's goog.style.getBorderBox
 *
 * @param element  The element to get the border widths for.
 * @returns The computed border widths.
 */
export declare function getBorderBox(element: Element): Rect;
/**
 * Converts a point in page coordinates into the equivalent CSS `left`/`top`
 * for an absolutely positioned child of `container`.
 *
 * Those properties are resolved against the container's padding box, so the
 * container's border widths have to be subtracted along with its position.
 *
 * @param x Horizontal page coordinate.
 * @param y Vertical page coordinate.
 * @param container The relatively positioned parent element.
 * @returns The equivalent point relative to the container's padding box.
 * @internal
 */
export declare function pageToContainerOffset(x: number, y: number, container: Element): Coordinate;
/**
 * Changes the scroll position of `container` with the minimum amount so
 * that the content and the borders of the given `element` become visible.
 * If the element is bigger than the container, its top left corner will be
 * aligned as close to the container's top left corner as possible.
 * Copied from Closure's goog.style.scrollIntoContainerView
 *
 * @param element The element to make visible.
 * @param container The container to scroll. If not set, then the document
 *     scroll element will be used.
 * @param opt_center Whether to center the element in the container.
 *     Defaults to false.
 */
export declare function scrollIntoContainerView(element: Element, container: Element, opt_center?: boolean): void;
/**
 * Calculate the scroll position of `container` with the minimum amount so
 * that the content and the borders of the given `element` become visible.
 * If the element is bigger than the container, its top left corner will be
 * aligned as close to the container's top left corner as possible.
 * Copied from Closure's goog.style.getContainerOffsetToScrollInto
 *
 * @param element The element to make visible.
 * @param container The container to scroll. If not set, then the document
 *     scroll element will be used.
 * @param opt_center Whether to center the element in the container.
 *     Defaults to false.
 * @returns The new scroll position of the container.
 */
export declare function getContainerOffsetToScrollInto(element: Element, container: Element, opt_center?: boolean): Coordinate;
export declare const TEST_ONLY: {
    getSizeInternal: typeof getSizeInternal;
};
export {};
//# sourceMappingURL=style.d.ts.map