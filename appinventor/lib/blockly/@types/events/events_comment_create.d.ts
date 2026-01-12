/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceComment } from '../workspace_comment.js';
import { CommentBase, CommentBaseJson } from './events_comment_base.js';
import type { Workspace } from '../workspace.js';
/**
 * Notifies listeners that a workspace comment was created.
 */
export declare class CommentCreate extends CommentBase {
    type: string;
    /** The XML representation of the created workspace comment. */
    xml?: Element | DocumentFragment;
    /**
     * @param opt_comment The created comment.
     *     Undefined for a blank event.
     */
    constructor(opt_comment?: WorkspaceComment);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentCreateJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentCreate, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentCreateJson, workspace: Workspace, event?: any): CommentCreate;
    /**
     * Run a creation event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface CommentCreateJson extends CommentBaseJson {
    xml: string;
}
//# sourceMappingURL=events_comment_create.d.ts.map