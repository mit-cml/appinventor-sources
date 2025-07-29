/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceComment } from '../comments/workspace_comment.js';
import { ISerializer } from '../interfaces/i_serializer.js';
import { Workspace } from '../workspace.js';
export interface State {
    id?: string;
    text?: string;
    x?: number;
    y?: number;
    width?: number;
    height?: number;
    collapsed?: boolean;
    editable?: boolean;
    movable?: boolean;
    deletable?: boolean;
}
/** Serializes the state of the given comment to JSON. */
export declare function save(comment: WorkspaceComment, { addCoordinates, saveIds, }?: {
    addCoordinates?: boolean;
    saveIds?: boolean;
}): State;
/** Appends the comment defined by the given state to the given workspace. */
export declare function append(state: State, workspace: Workspace, { recordUndo }?: {
    recordUndo?: boolean;
}): WorkspaceComment;
/** Serializer for saving and loading workspace comment state. */
export declare class WorkspaceCommentSerializer implements ISerializer {
    priority: number;
    /**
     * Returns the state of all workspace comments in the given workspace.
     */
    save(workspace: Workspace): State[] | null;
    /**
     * Deserializes the comments defined by the given state into the given
     * workspace.
     */
    load(state: State[], workspace: Workspace): void;
    /** Disposes of any comments that exist on the given workspace. */
    clear(workspace: Workspace): void;
}
//# sourceMappingURL=workspace_comments.d.ts.map