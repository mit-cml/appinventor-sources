/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceComment } from '../workspace_comment.js';
import { Abstract as AbstractEvent, AbstractEventJson } from './events_abstract.js';
import type { CommentCreate } from './events_comment_create.js';
import type { CommentDelete } from './events_comment_delete.js';
import type { Workspace } from '../workspace.js';
/**
 * Abstract class for a comment event.
 */
export declare class CommentBase extends AbstractEvent {
    isBlank: boolean;
    /** The ID of the comment that this event references. */
    commentId?: string;
    /**
     * @param opt_comment The comment this event corresponds to.  Undefined for a
     *     blank event.
     */
    constructor(opt_comment?: WorkspaceComment);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentBaseJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentBase, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentBaseJson, workspace: Workspace, event?: any): CommentBase;
    /**
     * Helper function for Comment[Create|Delete]
     *
     * @param event The event to run.
     * @param create if True then Create, if False then Delete
     */
    static CommentCreateDeleteHelper(event: CommentCreate | CommentDelete, create: boolean): void;
}
export interface CommentBaseJson extends AbstractEventJson {
    commentId: string;
}
//# sourceMappingURL=events_comment_base.d.ts.map