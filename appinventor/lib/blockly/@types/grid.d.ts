/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Object for configuring and updating a workspace grid in
 * Blockly.
 *
 * @class
 */
import { GridOptions } from './options.js';
import { Coordinate } from './utils/coordinate.js';
/**
 * Class for a workspace's grid.
 */
export declare class Grid {
    private pattern;
    private spacing;
    private length;
    private scale;
    private readonly line1;
    private readonly line2;
    private snapToGrid;
    /**
     * @param pattern The grid's SVG pattern, created during injection.
     * @param options A dictionary of normalized options for the grid.
     *     See grid documentation:
     *     https://developers.google.com/blockly/guides/configure/web/grid
     */
    constructor(pattern: SVGElement, options: GridOptions);
    /**
     * Sets the spacing between the centers of the grid lines.
     *
     * This does not trigger snapping to the newly spaced grid. If you want to
     * snap blocks to the grid programmatically that needs to be triggered
     * on individual top-level blocks. The next time a block is dragged and
     * dropped it will snap to the grid if snapping to the grid is enabled.
     */
    setSpacing(spacing: number): void;
    /**
     * Get the spacing of the grid points (in px).
     *
     * @returns The spacing of the grid points.
     */
    getSpacing(): number;
    /** Sets the length of the grid lines. */
    setLength(length: number): void;
    /** Get the length of the grid lines (in px). */
    getLength(): number;
    /**
     * Sets whether blocks should snap to the grid or not.
     *
     * Setting this to true does not trigger snapping. If you want to snap blocks
     * to the grid programmatically that needs to be triggered on individual
     * top-level blocks. The next time a block is dragged and dropped it will
     * snap to the grid.
     */
    setSnapToGrid(snap: boolean): void;
    /**
     * Whether blocks should snap to the grid.
     *
     * @returns True if blocks should snap, false otherwise.
     */
    shouldSnap(): boolean;
    /**
     * Get the ID of the pattern element, which should be randomized to avoid
     * conflicts with other Blockly instances on the page.
     *
     * @returns The pattern ID.
     * @internal
     */
    getPatternId(): string;
    /**
     * Update the grid with a new scale.
     *
     * @param scale The new workspace scale.
     * @internal
     */
    update(scale: number): void;
    /**
     * Set the attributes on one of the lines in the grid.  Use this to update the
     * length and stroke width of the grid lines.
     *
     * @param line Which line to update.
     * @param width The new stroke size (in px).
     * @param x1 The new x start position of the line (in px).
     * @param x2 The new x end position of the line (in px).
     * @param y1 The new y start position of the line (in px).
     * @param y2 The new y end position of the line (in px).
     */
    private setLineAttributes;
    /**
     * Move the grid to a new x and y position, and make sure that change is
     * visible.
     *
     * @param x The new x position of the grid (in px).
     * @param y The new y position of the grid (in px).
     * @internal
     */
    moveTo(x: number, y: number): void;
    /**
     * Given a coordinate, return the nearest coordinate aligned to the grid.
     *
     * @param xy A workspace coordinate.
     * @returns Workspace coordinate of nearest grid point.
     *   If there's no change, return the same coordinate object.
     */
    alignXY(xy: Coordinate): Coordinate;
    /**
     * Create the DOM for the grid described by options.
     *
     * @param rnd A random ID to append to the pattern's ID.
     * @param gridOptions The object containing grid configuration.
     * @param defs The root SVG element for this workspace's defs.
     * @returns The SVG element for the grid pattern.
     * @internal
     */
    static createDom(rnd: string, gridOptions: GridOptions, defs: SVGElement): SVGElement;
}
//# sourceMappingURL=grid.d.ts.map