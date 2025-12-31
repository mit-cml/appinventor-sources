/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for a block move event.
 *
 * @class
 */
import type { Block } from '../block.js';
import { Coordinate } from '../utils/coordinate.js';
import type { Workspace } from '../workspace.js';
import { BlockBase, BlockBaseJson } from './events_block_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners when a block is moved. This could be from one
 * connection to another, or from one location on the workspace to another.
 */
export declare class BlockMove extends BlockBase {
    type: EventType;
    /** The ID of the old parent block. Undefined if it was a top-level block. */
    oldParentId?: string;
    /**
     * The name of the old input. Undefined if it was a top-level block or the
     * parent's next block.
     */
    oldInputName?: string;
    /**
     * The old X and Y workspace coordinates of the block if it was a top level
     * block. Undefined if it was not a top level block.
     */
    oldCoordinate?: Coordinate;
    /** The ID of the new parent block. Undefined if it is a top-level block. */
    newParentId?: string;
    /**
     * The name of the new input. Undefined if it is a top-level block or the
     * parent's next block.
     */
    newInputName?: string;
    /**
     * The new X and Y workspace coordinates of the block if it is a top-level
     * block. Undefined if it is not a top level block.
     */
    newCoordinate?: Coordinate;
    /**
     * An explanation of what this move is for.  Known values include:
     *  'drag' -- A drag operation completed.
     *  'bump' -- Block got bumped away from an invalid connection.
     *  'snap' -- Block got shifted to line up with the grid.
     *  'inbounds' -- Block got pushed back into a non-scrolling workspace.
     *  'connect' -- Block got connected to another block.
     *  'disconnect' -- Block got disconnected from another block.
     *  'create' -- Block created via XML.
     *  'cleanup' -- Workspace aligned top-level blocks.
     * Event merging may create multiple reasons: ['drag', 'bump', 'snap'].
     */
    reason?: string[];
    /** @param opt_block The moved block.  Undefined for a blank event. */
    constructor(opt_block?: Block);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): BlockMoveJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of BlockMove, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: BlockMoveJson, workspace: Workspace, event?: any): BlockMove;
    /** Record the block's new location.  Called after the move. */
    recordNew(): void;
    /**
     * Set the reason for a move event.
     *
     * @param reason Why is this move happening?  'drag', 'bump', 'snap', ...
     */
    setReason(reason: string[]): void;
    /**
     * Returns the parentId and input if the block is connected,
     *   or the XY location if disconnected.
     *
     * @returns Collection of location info.
     */
    private currentLocation;
    /**
     * Does this event record any change of state?
     *
     * @returns False if something changed.
     */
    isNull(): boolean;
    /**
     * Run a move event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface BlockMoveJson extends BlockBaseJson {
    oldParentId?: string;
    oldInputName?: string;
    oldCoordinate?: string;
    newParentId?: string;
    newInputName?: string;
    newCoordinate?: string;
    reason?: string[];
    recordUndo?: boolean;
}
//# sourceMappingURL=events_block_move.d.ts.map