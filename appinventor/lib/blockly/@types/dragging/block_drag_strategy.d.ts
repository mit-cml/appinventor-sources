/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from '../block_svg.js';
import type { IDragStrategy } from '../interfaces/i_draggable.js';
import { DragDisposition } from '../interfaces/i_draggable.js';
import type { RenderedConnection } from '../rendered_connection.js';
import { Coordinate } from '../utils.js';
/** Represents a valid pair of connections between the dragging block and a block on the workspace. */
interface ConnectionPair {
    /** A connection on the dragging stack that is compatible with neighbour. */
    local: RenderedConnection;
    /** A nearby connection that is compatible with local. */
    neighbour: RenderedConnection;
}
/** Represents a nearby valid connection. */
interface ConnectionCandidate extends ConnectionPair {
    /** The distance between the local connection and the neighbour connection. */
    distance: number;
}
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
    /** List of all connections available on the workspace. */
    private allConnectionPairs;
    /** The current movement mode. */
    private moveMode;
    /** Used to persist an event group when snapping is done async. */
    private originalEventGroup;
    /**
     * Map from block IDs to reason(s) why it was disabled, used to restore
     * disabled state post-drag.
     */
    private lastBlockDisabledReasons;
    protected readonly BLOCK_CONNECTION_OFFSET = 10;
    /**
     * How far in from the edges of the workspace to position newly placed blocks.
     */
    protected readonly WORKSPACE_MARGIN = 10;
    constructor(block: BlockSvg);
    /** Returns true if the block is currently movable. False otherwise. */
    isMovable(): boolean;
    /**
     * Positions a cloned block on its new workspace.
     *
     * @param oldBlock The flyout block that was cloned.
     * @param newBlock The new block to position.
     */
    private positionNewBlock;
    /**
     * Returns the block to use for the current drag operation. This may create
     * and return a newly instantiated block when e.g. dragging from a flyout.
     */
    protected getTargetBlock(): BlockSvg;
    /**
     * Announces a move on the ARIA live region for assistive technologies.
     *
     * @param isMoveStart Whether this announcement is for the start of a move. If false,
     * skip announcing the block label since it should have already been announced at the
     * start of the move.
     */
    private announceMove;
    /**
     * Checks if there are multiple compatible connections for the specified side of the pair.
     *
     * @param forLocal Whether we are considering the local or neighbour side of the pair
     * @returns True if there are multiple compatible connections, false otherwise
     */
    private hasMultipleCompatibleConnections;
    /**
     * Handles any setup for starting the drag, including disconnecting the block
     * from any parent blocks.
     */
    startDrag(e?: PointerEvent | KeyboardEvent): import("../blockly.js").IDraggable;
    /**
     * Caches a list of all valid connection pairs between the dragging block
     * and any other blocks on the workspace. This is used to determine the
     * closest valid connection during keyboard-driven moves and to determine
     * the need to disambiguate between multiple compatible connections when
     * announcing moves to assistive technologies.
     */
    cacheAllConnectionPairs(): void;
    /**
     * Returns an array of visible bubbles attached to the given block or its
     * descendants.
     *
     * @param block The block to identify open bubbles on.
     * @returns An array of all currently visible bubbles on the given block or
     *    its descendants.
     */
    private getVisibleBubbles;
    /**
     * Get whether the drag should act on a single block or a block stack.
     *
     * @param e The instigating pointer or keyboard event, if any.
     * @returns True if just the initial block should be dragged out, false
     *     if all following blocks should also be dragged.
     */
    protected shouldHealStack(e: PointerEvent | KeyboardEvent | undefined): boolean;
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
    /**
     * Stores the dragging block's current parent or child connection before
     * unplugging. This allows us to revert the drag cleanly. In keyboard move mode,
     * the initial connection pair is also used as the first connection candidate.
     */
    private storeInitialConnections;
    /** Fire a UI event at the start of a block drag. */
    private fireDragStartEvent;
    /** Fire a UI event at the end of a block drag. */
    private fireDragEndEvent;
    /** Fire a move event at the end of a block drag. */
    private fireMoveEvent;
    /** Moves the block and updates any connection previews. */
    drag(newLoc: Coordinate, e?: PointerEvent | KeyboardEvent): void;
    /**
     * Determines the offset to apply to the dragged block's position
     * based on the current connection candidate.
     *
     * @returns coordinates representing the offset
     */
    private determineConnectionOffset;
    /**
     * Renders the connection preview indicator.
     *
     * @param draggingBlock The block being dragged.
     * @param delta How far the pointer has moved from the position
     *     at the start of the drag, in workspace units.
     * @param initialCandidate If provided, a connection candidate that the
     *     connection preview indicator will be attached to.
     * @returns The neighbouring connection to which the connection preview will
     *     be attached.
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
     * Returns the closest connection candidate for the given block.
     *
     * @param block The block to find a connection for.
     * @param delta The distance the block has traveled since dragging began.
     * @returns The closest available connection candidate, if any.
     */
    private getClosestCandidate;
    /**
     * Get the radius to use when searching for a nearby valid connection.
     */
    protected getSearchRadius(): number;
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
    endDrag(_e: PointerEvent | KeyboardEvent | undefined, disposition: DragDisposition): void;
    /** Disposes of any state at the end of the drag. */
    private disposeStep;
    /** Connects the given candidate connections. */
    private applyConnections;
    /**
     * Moves the block back to where it was at the beginning of the drag,
     * including reconnecting connections.
     */
    revertDrag(): void;
    /**
     * Get the nearest valid candidate connection in traversal order.
     *
     * @param delta The distance the block has moved since this drag began.
     * @returns A candidate connection and radius, or null if none was found.
     */
    findTraversalCandidate(delta: Coordinate): ConnectionCandidate | null;
    /**
     * Returns whether or not the given block is at a terminal position (start or
     * end) of the blocks on the workspace. This helps distinguish between a block
     * that is at the end of the line because all valid connections have been
     * visited and the proposed constrained move destination is now to drop it on
     * the workspace as a top-level block (in which case it will be in a terminal
     * position), and a block that just entered move mode as a top-level block,
     * and should therefore still be able to move to another connection point
     * even if looping is disabled.
     *
     * @param block The block to check.
     * @param direction The current dragging direction.
     * @returns True if the block is at the start or end of its possible positions
     *     on the workspace.
     */
    private isInTerminalPosition;
    /**
     * Converts a connection pair to a connection candidate with a default
     * distance of 0.
     */
    private pairToCandidate;
    /**
     * Returns the cardinal direction that the block being dragged would have to
     * move in to reach the given location.
     * The given coordinate should differ from the current location on only one
     * axis.
     *
     * @param newLocation The intended destination for the block.
     * @returns The direction the block would need to travel to reach the new
     *     location.
     */
    private getDirectionToNewLocation;
    /**
     * Returns all navigable connections on the given block and its children.
     * Omits connections on shadow blocks, collapsed blocks, or those that are
     * associated with a hidden input.
     *
     * @param block The block to use as a starting point for retrieving
     *     connections.
     * @returns All connections on the block and its children.
     */
    private getAllConnections;
    /**
     * Returns a connection candidate to move the dragged block to at the start of
     * a drag. If the passively focused node is a connection and the dragged block
     * can connect to it, the connection will be returned. Otherwise, the first
     * compatible connection on the passively focused node's block, if any, will
     * be returned. Returns null if the workspace does not have passive focus.
     */
    private getInitialCandidate;
    /**
     * Updates the current move mode based on the most recent drag-related event.
     */
    private updateMoveMode;
    /**
     * Enables the given block and its children.
     * Stores the reasons each block was disabled so they can be restored.
     *
     * @param block The block to enable.
     */
    private enableAllDraggedBlocks;
    /**
     * Re-disables the given block and its children using their original
     * disabled reasons.
     *
     * @param block The block to re-disable, if applicable.
     */
    private redisableAllDraggedBlocks;
}
export {};
//# sourceMappingURL=block_drag_strategy.d.ts.map