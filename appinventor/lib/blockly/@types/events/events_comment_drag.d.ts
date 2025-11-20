/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Events fired when a workspace comment is dragged.
 */
import type { WorkspaceComment } from '../comments/workspace_comment.js';
import { Workspace } from '../workspace.js';
import { AbstractEventJson } from './events_abstract.js';
import { UiBase } from './events_ui_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners when a comment is being manually dragged/dropped.
 */
export declare class CommentDrag extends UiBase {
    /** The ID of the top-level comment being dragged. */
    commentId?: string;
    /** True if this is the start of a drag, false if this is the end of one. */
    isStart?: boolean;
    type: EventType;
    /**
     * @param opt_comment The comment that is being dragged.
     *     Undefined for a blank event.
     * @param opt_isStart Whether this is the start of a comment drag.
     *    Undefined for a blank event.
     */
    constructor(opt_comment?: WorkspaceComment, opt_isStart?: boolean);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentDragJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentDrag, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentDragJson, workspace: Workspace, event?: any): CommentDrag;
}
export interface CommentDragJson extends AbstractEventJson {
    isStart: boolean;
    commentId: string;
}
//# sourceMappingURL=events_comment_drag.d.ts.map