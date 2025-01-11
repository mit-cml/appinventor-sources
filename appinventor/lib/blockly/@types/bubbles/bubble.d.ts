/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IBubble } from '../interfaces/i_bubble.js';
import { Coordinate } from '../utils/coordinate.js';
import { Rect } from '../utils/rect.js';
import { Size } from '../utils/size.js';
import { WorkspaceSvg } from '../workspace_svg.js';
/**
 * The abstract pop-up bubble class. This creates a UI that looks like a speech
 * bubble, where it has a "tail" that points to the block, and a "head" that
 * displays arbitrary svg elements.
 */
export declare abstract class Bubble implements IBubble {
    protected readonly workspace: WorkspaceSvg;
    protected anchor: Coordinate;
    protected ownerRect?: Rect | undefined;
    /** The width of the border around the bubble. */
    static readonly BORDER_WIDTH = 6;
    /** Double the width of the border around the bubble. */
    static readonly DOUBLE_BORDER: number;
    /** The minimum size the bubble can have. */
    static readonly MIN_SIZE: number;
    /**
     * The thickness of the base of the tail in relation to the size of the
     * bubble. Higher numbers result in thinner tails.
     */
    static readonly TAIL_THICKNESS = 1;
    /** The number of degrees that the tail bends counter-clockwise. */
    static readonly TAIL_ANGLE = 20;
    /**
     * The sharpness of the tail's bend. Higher numbers result in smoother
     * tails.
     */
    static readonly TAIL_BEND = 4;
    /** Distance between arrow point and anchor point. */
    static readonly ANCHOR_RADIUS = 8;
    /** The SVG group containing all parts of the bubble. */
    protected svgRoot: SVGGElement;
    /** The SVG path for the arrow from the anchor to the bubble. */
    private tail;
    /** The SVG background rect for the main body of the bubble. */
    private background;
    /** The SVG group containing the contents of the bubble. */
    protected contentContainer: SVGGElement;
    /**
     * The size of the bubble (including background and contents but not tail).
     */
    private size;
    /** The colour of the background of the bubble. */
    private colour;
    /** True if the bubble has been disposed, false otherwise. */
    disposed: boolean;
    /** The position of the top of the bubble relative to its anchor. */
    private relativeTop;
    /** The position of the left of the bubble realtive to its anchor. */
    private relativeLeft;
    /**
     * @param workspace The workspace this bubble belongs to.
     * @param anchor The anchor location of the thing this bubble is attached to.
     *     The tail of the bubble will point to this location.
     * @param ownerRect An optional rect we don't want the bubble to overlap with
     *     when automatically positioning.
     */
    constructor(workspace: WorkspaceSvg, anchor: Coordinate, ownerRect?: Rect | undefined);
    /** Dispose of this bubble. */
    dispose(): void;
    /**
     * Set the location the tail of this bubble points to.
     *
     * @param anchor The location the tail of this bubble points to.
     * @param relayout If true, reposition the bubble from scratch so that it is
     *     optimally visible. If false, reposition it so it maintains the same
     *     position relative to the anchor.
     */
    setAnchorLocation(anchor: Coordinate, relayout?: boolean): void;
    /** Sets the position of this bubble relative to its anchor. */
    setPositionRelativeToAnchor(left: number, top: number): void;
    /** @returns the size of this bubble. */
    protected getSize(): Size;
    /**
     * Sets the size of this bubble, including the border.
     *
     * @param size Sets the size of this bubble, including the border.
     * @param relayout If true, reposition the bubble from scratch so that it is
     *     optimally visible. If false, reposition it so it maintains the same
     *     position relative to the anchor.
     */
    protected setSize(size: Size, relayout?: boolean): void;
    /** Returns the colour of the background and tail of this bubble. */
    protected getColour(): string;
    /** Sets the colour of the background and tail of this bubble. */
    setColour(colour: string): void;
    /** Passes the pointer event off to the gesture system. */
    private onMouseDown;
    /** Positions the bubble relative to its anchor. Does not render its tail. */
    protected positionRelativeToAnchor(): void;
    /**
     * Moves the bubble to the given coordinates.
     *
     * @internal
     */
    moveTo(x: number, y: number): void;
    /**
     * Positions the bubble "optimally" so that the most of it is visible and
     * it does not overlap the rect (if provided).
     */
    protected positionByRect(rect?: Rect): void;
    /**
     * Calculate the what percentage of the bubble overlaps with the visible
     * workspace (what percentage of the bubble is visible).
     *
     * @param relativeMin The position of the top-left corner of the bubble
     *     relative to the anchor point.
     * @param viewMetrics The view metrics of the workspace the bubble will appear
     *     in.
     * @returns The percentage of the bubble that is visible.
     */
    private getOverlap;
    /**
     * Calculate what the optimal horizontal position of the top-left corner of
     * the bubble is (relative to the anchor point) so that the most area of the
     * bubble is shown.
     *
     * @param viewMetrics The view metrics of the workspace the bubble will appear
     *     in.
     * @returns The optimal horizontal position of the top-left corner of the
     *     bubble.
     */
    private getOptimalRelativeLeft;
    /**
     * Calculate what the optimal vertical position of the top-left corner of
     * the bubble is (relative to the anchor point) so that the most area of the
     * bubble is shown.
     *
     * @param viewMetrics The view metrics of the workspace the bubble will appear
     *     in.
     * @returns The optimal vertical position of the top-left corner of the
     *     bubble.
     */
    private getOptimalRelativeTop;
    /**
     * @returns a rect defining the bounds of the workspace's view in workspace
     * coordinates.
     */
    private getWorkspaceViewRect;
    /** @returns the scrollbar thickness in workspace units. */
    private getScrollbarThickness;
    /** Draws the tail of the bubble. */
    private renderTail;
    /**
     * Move this bubble to the front of the visible workspace.
     *
     * @returns Whether or not the bubble has been moved.
     * @internal
     */
    bringToFront(): boolean;
    /** @internal */
    getRelativeToSurfaceXY(): Coordinate;
    /** @internal */
    getSvgRoot(): SVGElement;
    /**
     * Move this bubble during a drag.
     *
     * @param newLoc The location to translate to, in workspace coordinates.
     * @internal
     */
    moveDuringDrag(newLoc: Coordinate): void;
    setDragging(_start: boolean): void;
    /** @internal */
    setDeleteStyle(_enable: boolean): void;
    /** @internal */
    isDeletable(): boolean;
    /** @internal */
    showContextMenu(_e: Event): void;
}
//# sourceMappingURL=bubble.d.ts.map