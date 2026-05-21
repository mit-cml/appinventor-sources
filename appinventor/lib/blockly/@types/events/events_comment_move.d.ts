/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for comment move event.
 *
 * @class
 */
import type { WorkspaceComment } from '../comments/workspace_comment.js';
import { Coordinate } from '../utils/coordinate.js';
import type { Workspace } from '../workspace.js';
import { CommentBase, CommentBaseJson } from './events_comment_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners that a workspace comment has moved.
 */
export declare class CommentMove extends CommentBase {
    type: EventType;
    /** The comment that is being moved. */
    comment_?: WorkspaceComment;
    /** The location of the comment before the move, in workspace coordinates. */
    oldCoordinate_?: Coordinate;
    /** The location of the comment after the move, in workspace coordinates. */
    newCoordinate_?: Coordinate;
    /**
     * An explanation of what this move is for.  Known values include:
     *  'drag' -- A drag operation completed.
     *  'snap' -- Comment got shifted to line up with the grid.
     *  'inbounds' -- Block got pushed back into a non-scrolling workspace.
     *  'create' -- Block created via deserialization.
     *  'cleanup' -- Workspace aligned top-level blocks.
     * Event merging may create multiple reasons: ['drag', 'inbounds', 'snap'].
     */
    reason?: string[];
    /**
     * @param opt_comment The comment that is being moved.  Undefined for a blank
     *     event.
     */
    constructor(opt_comment?: WorkspaceComment);
    /**
     * Record the comment's new location.  Called after the move.  Can only be
     * called once.
     */
    recordNew(): void;
    /**
     * Sets the reason for a move event.
     *
     * @param reason Why is this move happening?  'drag', 'bump', 'snap', ...
     */
    setReason(reason: string[]): void;
    /**
     * Override the location before the move.  Use this if you don't create the
     * event until the end of the move, but you know the original location.
     *
     * @param xy The location before the move, in workspace coordinates.
     */
    setOldCoordinate(xy: Coordinate): void;
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentMoveJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentMove, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentMoveJson, workspace: Workspace, event?: any): CommentMove;
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
export interface CommentMoveJson extends CommentBaseJson {
    oldCoordinate: string;
    newCoordinate: string;
}
//# sourceMappingURL=events_comment_move.d.ts.map