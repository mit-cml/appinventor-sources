/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlocklyOptions } from '../blockly_options.js';
import { Abstract as AbstractEvent } from '../events/events_abstract.js';
import { Options } from '../options.js';
import { Coordinate } from '../utils/coordinate.js';
import type { Rect } from '../utils/rect.js';
import type { WorkspaceSvg } from '../workspace_svg.js';
import { Bubble } from './bubble.js';
/**
 * A bubble that contains a mini-workspace which can hold arbitrary blocks.
 * Used by the mutator icon.
 */
export declare class MiniWorkspaceBubble extends Bubble {
    readonly workspace: WorkspaceSvg;
    protected anchor: Coordinate;
    protected ownerRect?: Rect | undefined;
    /**
     * The minimum amount of change to the mini workspace view to trigger
     * resizing the bubble.
     */
    private static readonly MINIMUM_VIEW_CHANGE;
    /**
     * An arbitrary margin of whitespace to put around the blocks in the
     * workspace.
     */
    private static readonly MARGIN;
    /** The root svg element containing the workspace. */
    private svgDialog;
    /** The workspace that gets shown within this bubble. */
    private miniWorkspace;
    /**
     * Should this bubble automatically reposition itself when it resizes?
     * Becomes false after this bubble is first dragged.
     */
    private autoLayout;
    /** @internal */
    constructor(workspaceOptions: BlocklyOptions, workspace: WorkspaceSvg, anchor: Coordinate, ownerRect?: Rect | undefined);
    dispose(): void;
    /** @internal */
    getWorkspace(): WorkspaceSvg;
    /** Adds a change listener to the mini workspace. */
    addWorkspaceChangeListener(listener: (e: AbstractEvent) => void): void;
    /**
     * Validates the workspace options to make sure folks aren't trying to
     * enable things the miniworkspace doesn't support.
     */
    private validateWorkspaceOptions;
    private onWorkspaceChange;
    /**
     * Bumps blocks that are above the top or outside the start-side of the
     * workspace back within the workspace.
     *
     * Blocks that are below the bottom or outside the end-side of the workspace
     * are dealt with by resizing the workspace to show them.
     */
    private bumpBlocksIntoBounds;
    /**
     * Updates the size of this bubble to account for the size of the
     * mini workspace.
     */
    private updateBubbleSize;
    /**
     * Calculates the size of the mini workspace for use in resizing the bubble.
     */
    private calculateWorkspaceSize;
    /** Reapplies styles to all of the blocks in the mini workspace. */
    updateBlockStyles(): void;
    /**
     * Move this bubble during a drag.
     *
     * @param newLoc The location to translate to, in workspace coordinates.
     * @internal
     */
    moveDuringDrag(newLoc: Coordinate): void;
    /** @internal */
    moveTo(x: number, y: number): void;
    /** @internal */
    newWorkspaceSvg(options: Options): WorkspaceSvg;
}
//# sourceMappingURL=mini_workspace_bubble.d.ts.map