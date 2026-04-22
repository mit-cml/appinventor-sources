/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for comment deletion event.
 *
 * @class
 */
import type { WorkspaceComment } from '../comments/workspace_comment.js';
import * as comments from '../serialization/workspace_comments.js';
import type { Workspace } from '../workspace.js';
import { CommentBase, CommentBaseJson } from './events_comment_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners that a workspace comment has been deleted.
 */
export declare class CommentDelete extends CommentBase {
    type: EventType;
    /** The XML representation of the deleted workspace comment. */
    xml?: Element;
    /** The JSON representation of the created workspace comment. */
    json?: comments.State;
    /**
     * @param opt_comment The deleted comment.
     *     Undefined for a blank event.
     */
    constructor(opt_comment?: WorkspaceComment);
    /**
     * Run a creation event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentDeleteJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentDelete, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentDeleteJson, workspace: Workspace, event?: any): CommentDelete;
}
export interface CommentDeleteJson extends CommentBaseJson {
    xml: string;
    json: object;
}
//# sourceMappingURL=events_comment_delete.d.ts.map