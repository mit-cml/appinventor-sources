/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Utility methods for size calculation.
 * These methods are not specific to Blockly, and could be factored out into
 * a JavaScript framework such as Closure.
 *
 * @class
 */
/**
 * Class for representing sizes consisting of a width and height.
 */
export declare class Size {
    width: number;
    height: number;
    /**
     * @param width Width.
     * @param height Height.
     */
    constructor(width: number, height: number);
    /**
     * Compares sizes for equality.
     *
     * @param a A Size.
     * @param b A Size.
     * @returns True iff the sizes have equal widths and equal heights, or if both
     *     are null.
     */
    static equals(a?: Size | null, b?: Size | null): boolean;
    /**
     * Returns a new size with the maximum width and height values out of both
     * sizes.
     */
    static max(a: Size, b: Size): Size;
    /**
     * Returns a new size with the minimum width and height values out of both
     * sizes.
     */
    static min(a: Size, b: Size): Size;
}
//# sourceMappingURL=size.d.ts.map