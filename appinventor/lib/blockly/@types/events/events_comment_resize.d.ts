/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for comment resize event.
 */
import type { WorkspaceComment } from '../comments/workspace_comment.js';
import { Size } from '../utils/size.js';
import type { Workspace } from '../workspace.js';
import { CommentBase, CommentBaseJson } from './events_comment_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners that a workspace comment has resized.
 */
export declare class CommentResize extends CommentBase {
    type: EventType;
    /** The size of the comment before the resize. */
    oldSize?: Size;
    /** The size of the comment after the resize. */
    newSize?: Size;
    /**
     * @param opt_comment The comment that is being resized. Undefined for a blank
     *     event.
     */
    constructor(opt_comment?: WorkspaceComment);
    /**
     * Record the comment's new size. Called after the resize. Can only be
     * called once.
     */
    recordCurrentSizeAsNewSize(): void;
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentResizeJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentResize, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentResizeJson, workspace: Workspace, event?: any): CommentResize;
    /**
     * Does this event record any change of state?
     *
     * @returns False if something changed.
     */
    isNull(): boolean;
    /**
     * Run a resize event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface CommentResizeJson extends CommentBaseJson {
    oldWidth: number;
    oldHeight: number;
    newWidth: number;
    newHeight: number;
}
//# sourceMappingURL=events_comment_resize.d.ts.map