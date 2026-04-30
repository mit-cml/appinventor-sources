/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Utility methods for rectangle manipulation.
 * These methods are not specific to Blockly, and could be factored out into
 * a JavaScript framework such as Closure.
 *
 * @class
 */
import { Coordinate } from './coordinate.js';
/**
 * Class for representing rectangular regions.
 */
export declare class Rect {
    top: number;
    bottom: number;
    left: number;
    right: number;
    /**
     * @param top Top.
     * @param bottom Bottom.
     * @param left Left.
     * @param right Right.
     */
    constructor(top: number, bottom: number, left: number, right: number);
    /**
     * Converts a DOM or SVG Rect to a Blockly Rect.
     *
     * @param rect The rectangle to convert.
     * @returns A representation of the same rectangle as a Blockly Rect.
     */
    static from(rect: DOMRect | SVGRect): Rect;
    /**
     * Creates a new copy of this rectangle.
     *
     * @returns A copy of this Rect.
     */
    clone(): Rect;
    /** Returns the height of this rectangle. */
    getHeight(): number;
    /** Returns the width of this rectangle. */
    getWidth(): number;
    /** Returns the top left coordinate of this rectangle. */
    getOrigin(): Coordinate;
    /**
     * Tests whether this rectangle contains a x/y coordinate.
     *
     * @param x The x coordinate to test for containment.
     * @param y The y coordinate to test for containment.
     * @returns Whether this rectangle contains given coordinate.
     */
    contains(x: number, y: number): boolean;
    /**
     * Tests whether this rectangle intersects the provided rectangle.
     * Assumes that the coordinate system increases going down and left.
     *
     * @param other The other rectangle to check for intersection with.
     * @returns Whether this rectangle intersects the provided rectangle.
     */
    intersects(other: Rect): boolean;
    /**
     * Compares bounding rectangles for equality.
     *
     * @param a A Rect.
     * @param b A Rect.
     * @returns True iff the bounding rectangles are equal, or if both are null.
     */
    static equals(a?: Rect | null, b?: Rect | null): boolean;
    /**
     * Creates a new Rect using a position and supplied dimensions.
     *
     * @param position The upper left coordinate of the new rectangle.
     * @param width The width of the rectangle, in pixels.
     * @param height The height of the rectangle, in pixels.
     * @returns A newly created Rect using the provided Coordinate and dimensions.
     */
    static createFromPoint(position: Coordinate, width: number, height: number): Rect;
}
//# sourceMappingURL=rect.d.ts.map