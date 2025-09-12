/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { WorkspaceComment } from '../comments/workspace_comment.js';
import type { Workspace } from '../workspace.js';
import { CommentBase, CommentBaseJson } from './events_comment_base.js';
import { EventType } from './type.js';
export declare class CommentCollapse extends CommentBase {
    newCollapsed?: boolean | undefined;
    type: EventType;
    constructor(comment?: WorkspaceComment, newCollapsed?: boolean | undefined);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): CommentCollapseJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of CommentCollapse, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: CommentCollapseJson, workspace: Workspace, event?: any): CommentCollapse;
    /**
     * Run a collapse event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface CommentCollapseJson extends CommentBaseJson {
    newCollapsed: boolean;
}
//# sourceMappingURL=events_comment_collapse.d.ts.map