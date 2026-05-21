/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from '../block_svg.js';
import { IDragStrategy } from '../interfaces/i_draggable.js';
import { Coordinate } from '../utils.js';
export declare class BlockDragStrategy implements IDragStrategy {
    private block;
    private workspace;
    /** The parent block at the start of the drag. */
    private startParentConn;
    /**
     * The child block at the start of the drag. Only gets set if
     * `healStack` is true.
     */
    private startChildConn;
    private startLoc;
    private connectionCandidate;
    private connectionPreviewer;
    private dragging;
    /**
     * If this is a shadow block, the offset between this block and the parent
     * block, to add to the drag location. In workspace units.
     */
    private dragOffset;
    /** Was there already an event group in progress when the drag started? */
    private inGroup;
    constructor(block: BlockSvg);
    /** Returns true if the block is currently movable. False otherwise. */
    isMovable(): boolean;
    /**
     * Handles any setup for starting the drag, including disconnecting the block
     * from any parent blocks.
     */
    startDrag(e?: PointerEvent): void;
    /** Starts a drag on a shadow, recording the drag offset. */
    private startDraggingShadow;
    /**
     * Whether or not we should disconnect the block when a drag is started.
     *
     * @param healStack Whether or not to heal the stack after disconnecting.
     * @returns True to disconnect the block, false otherwise.
     */
    private shouldDisconnect;
    /**
     * Disconnects the block from any parents. If `healStack` is true and this is
     * a stack block, we also disconnect from any next blocks and attempt to
     * attach them to any parent.
     *
     * @param healStack Whether or not to heal the stack after disconnecting.
     */
    private disconnectBlock;
    /** Fire a UI event at the start of a block drag. */
    private fireDragStartEvent;
    /** Fire a UI event at the end of a block drag. */
    private fireDragEndEvent;
    /** Fire a move event at the end of a block drag. */
    private fireMoveEvent;
    /** Moves the block and updates any connection previews. */
    drag(newLoc: Coordinate): void;
    /**
     * @param draggingBlock The block being dragged.
     * @param delta How far the pointer has moved from the position
     *     at the start of the drag, in workspace units.
     */
    private updateConnectionPreview;
    /**
     * Returns true if the given orphan block can connect at the end of the
     * top block's stack or row, false otherwise.
     */
    private orphanCanConnectAtEnd;
    /**
     * Returns true if the current candidate is better than the new candidate.
     *
     * We slightly prefer the current candidate even if it is farther away.
     */
    private currCandidateIsBetter;
    /**
     * Returns the closest valid candidate connection, if one can be found.
     *
     * Valid neighbour connections are within the configured start radius, with a
     * compatible type (input, output, etc) and connection check.
     */
    private getConnectionCandidate;
    /**
     * Returns all of the connections we might connect to blocks on the workspace.
     *
     * Includes any connections on the dragging block, and any last next
     * connection on the stack (if one exists).
     */
    private getLocalConnections;
    /**
     * Cleans up any state at the end of the drag. Applies any pending
     * connections.
     */
    endDrag(e?: PointerEvent): void;
    /** Disposes of any state at the end of the drag. */
    private disposeStep;
    /** Connects the given candidate connections. */
    private applyConnections;
    /**
     * Moves the block back to where it was at the beginning of the drag,
     * including reconnecting connections.
     */
    revertDrag(): void;
}
//# sourceMappingURL=block_drag_strategy.d.ts.map