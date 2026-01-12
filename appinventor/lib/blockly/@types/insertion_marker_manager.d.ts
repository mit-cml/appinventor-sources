/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from './block_svg.js';
import type { IDragTarget } from './interfaces/i_drag_target.js';
import type { Coordinate } from './utils/coordinate.js';
/**
 * Class that controls updates to connections during drags.  It is primarily
 * responsible for finding the closest eligible connection and highlighting or
 * unhighlighting it as needed during a drag.
 */
export declare class InsertionMarkerManager {
    /**
     * The top block in the stack being dragged.
     * Does not change during a drag.
     */
    private readonly topBlock;
    /**
     * The workspace on which these connections are being dragged.
     * Does not change during a drag.
     */
    private readonly workspace;
    /**
     * The last connection on the stack, if it's not the last connection on the
     * first block.
     * Set in initAvailableConnections, if at all.
     */
    private lastOnStack;
    /**
     * The insertion marker corresponding to the last block in the stack, if
     * that's not the same as the first block in the stack.
     * Set in initAvailableConnections, if at all
     */
    private lastMarker;
    /**
     * The insertion marker that shows up between blocks to show where a block
     * would go if dropped immediately.
     */
    private firstMarker;
    /**
     * Information about the connection that would be made if the dragging block
     * were released immediately. Updated on every mouse move.
     */
    private activeCandidate;
    /**
     * Whether the block would be deleted if it were dropped immediately.
     * Updated on every mouse move.
     *
     * @internal
     */
    wouldDeleteBlock: boolean;
    /**
     * Connection on the insertion marker block that corresponds to
     * the active candidate's local connection on the currently dragged block.
     */
    private markerConnection;
    /** The block that currently has an input being highlighted, or null. */
    private highlightedBlock;
    /** The block being faded to indicate replacement, or null. */
    private fadedBlock;
    /**
     * The connections on the dragging blocks that are available to connect to
     * other blocks.  This includes all open connections on the top block, as
     * well as the last connection on the block stack.
     */
    private availableConnections;
    /** @param block The top block in the stack being dragged. */
    constructor(block: BlockSvg);
    /**
     * Sever all links from this object.
     *
     * @internal
     */
    dispose(): void;
    /**
     * Update the available connections for the top block. These connections can
     * change if a block is unplugged and the stack is healed.
     *
     * @internal
     */
    updateAvailableConnections(): void;
    /**
     * Return whether the block would be connected if dropped immediately, based
     * on information from the most recent move event.
     *
     * @returns True if the block would be connected if dropped immediately.
     * @internal
     */
    wouldConnectBlock(): boolean;
    /**
     * Connect to the closest connection and render the results.
     * This should be called at the end of a drag.
     *
     * @internal
     */
    applyConnections(): void;
    /**
     * Update connections based on the most recent move location.
     *
     * @param dxy Position relative to drag start, in workspace units.
     * @param dragTarget The drag target that the block is currently over.
     * @internal
     */
    update(dxy: Coordinate, dragTarget: IDragTarget | null): void;
    /**
     * Create an insertion marker that represents the given block.
     *
     * @param sourceBlock The block that the insertion marker will represent.
     * @returns The insertion marker that represents the given block.
     */
    private createMarkerBlock;
    /**
     * Populate the list of available connections on this block stack. If the
     * stack has more than one block, this function will also update lastOnStack.
     *
     * @returns A list of available connections.
     */
    private initAvailableConnections;
    /**
     * Whether the previews (insertion marker and replacement marker) should be
     * updated based on the closest candidate and the current drag distance.
     *
     * @param newCandidate A new candidate connection that may replace the current
     *     best candidate.
     * @param dxy Position relative to drag start, in workspace units.
     * @returns Whether the preview should be updated.
     */
    private shouldUpdatePreviews;
    /**
     * Find the nearest valid connection, which may be the same as the current
     * closest connection.
     *
     * @param dxy Position relative to drag start, in workspace units.
     * @returns An object containing a local connection, a closest connection, and
     *     a radius.
     */
    private getCandidate;
    /**
     * Decide the radius at which to start searching for the closest connection.
     *
     * @returns The radius at which to start the search for the closest
     *     connection.
     */
    private getStartRadius;
    /**
     * Whether ending the drag would delete the block.
     *
     * @param newCandidate Whether there is a candidate connection that the
     *     block could connect to if the drag ended immediately.
     * @param dragTarget The drag target that the block is currently over.
     * @returns Whether dropping the block immediately would delete the block.
     */
    private shouldDelete;
    /**
     * Show an insertion marker or replacement highlighting during a drag, if
     * needed.
     * At the beginning of this function, this.activeConnection should be null.
     *
     * @param newCandidate A new candidate connection that may replace the current
     *     best candidate.
     */
    private maybeShowPreview;
    /**
     * A preview should be shown.  This function figures out if it should be a
     * block highlight or an insertion marker, and shows the appropriate one.
     *
     * @param activeCandidate The connection that will be made if the drag ends
     *     immediately.
     */
    private showPreview;
    /**
     * Hide an insertion marker or replacement highlighting during a drag, if
     * needed.
     * At the end of this function, this.activeCandidate will be null.
     *
     * @param newCandidate A new candidate connection that may replace the current
     *     best candidate.
     */
    private maybeHidePreview;
    /**
     * A preview should be hidden. Loop through all possible preview modes
     * and hide everything.
     */
    private hidePreview;
    /**
     * Shows an insertion marker connected to the appropriate blocks (based on
     * manager state).
     *
     * @param activeCandidate The connection that will be made if the drag ends
     *     immediately.
     */
    private showInsertionMarker;
    /**
     * Disconnects and hides the current insertion marker. Should return the
     * blocks to their original state.
     */
    private hideInsertionMarker;
    /**
     * Shows an outline around the input the closest connection belongs to.
     *
     * @param activeCandidate The connection that will be made if the drag ends
     *     immediately.
     */
    private showInsertionInputOutline;
    /** Hides any visible input outlines. */
    private hideInsertionInputOutline;
    /**
     * Shows a replacement fade affect on the closest connection's target block
     * (the block that is currently connected to it).
     *
     * @param activeCandidate The connection that will be made if the drag ends
     *     immediately.
     */
    private showReplacementFade;
    /**
     * Hides/Removes any visible fade affects.
     */
    private hideReplacementFade;
    /**
     * Get a list of the insertion markers that currently exist.  Drags have 0, 1,
     * or 2 insertion markers.
     *
     * @returns A possibly empty list of insertion marker blocks.
     * @internal
     */
    getInsertionMarkers(): BlockSvg[];
    /**
     * Safely disposes of an insertion marker.
     */
    private disposeInsertionMarker;
}
export declare namespace InsertionMarkerManager {
    /**
     * An enum describing different kinds of previews the InsertionMarkerManager
     * could display.
     */
    enum PREVIEW_TYPE {
        INSERTION_MARKER = 0,
        INPUT_OUTLINE = 1,
        REPLACEMENT_FADE = 2
    }
}
export type PreviewType = InsertionMarkerManager.PREVIEW_TYPE;
export declare const PreviewType: typeof InsertionMarkerManager.PREVIEW_TYPE;
//# sourceMappingURL=insertion_marker_manager.d.ts.map