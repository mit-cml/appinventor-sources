/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
import { RenderedConnection } from './rendered_connection.js';
import { Size } from './utils/size.js';
/**
 * Visual representation of a mid-drag block if it were to be connected.
 */
export declare class InsertionMarker {
    /** The current size of the insertion marker, in workspace units. */
    private size;
    /** The DOM element representing the insertion marker. */
    private marker?;
    /** The static connection to which the insertion marker is attached. */
    private parentConnection?;
    /**
     * Returns the current size of the insertion marker in workspace units.
     */
    getHeightWidth(): Size;
    /**
     * Displays an insertion marker representing the block structure if
     * `draggingConnection` were to be connected to `staticConnection`.
     *
     * @param staticConnection The proposed connection on the stationary block.
     * @param draggingConnection The proposed connection on a block being dragged.
     */
    show(staticConnection: RenderedConnection, draggingConnection: RenderedConnection): void;
    /**
     * Hides the insertion marker.
     */
    hide(): void;
    /**
     * Creates a new insertion marker corresponding to the given block.
     *
     * @param block The block whose path will be used for the insertion marker.
     * @returns An SVG group representing an insertion marker.
     */
    private makeMarker;
    /**
     * Moves all connections on the given block adjacent to one another, excepting
     * a specified connection. Otherwise identical to
     * `BlockSvg.tightenChildrenEfficiently()`.
     *
     * @param block The block whose connections should be tightened.
     * @param skip A connection to avoid tightening; generally that to which the
     *     insertion marker is being connected to, which may need to have a gap
     *     between it and its partner connection which the insertion marker will
     *     be spliced into.
     */
    private partiallyTighten;
}
//# sourceMappingURL=insertion_marker.d.ts.map