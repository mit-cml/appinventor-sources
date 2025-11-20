/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { Coordinate } from '../utils/coordinate.js';
import { Size } from '../utils/size.js';
import { Workspace } from '../workspace.js';
export declare class WorkspaceComment {
    readonly workspace: Workspace;
    /** The unique identifier for this comment. */
    readonly id: string;
    /** The text of the comment. */
    private text;
    /** The size of the comment in workspace units. */
    private size;
    /** Whether the comment is collapsed or not. */
    private collapsed;
    /** Whether the comment is editable or not. */
    private editable;
    /** Whether the comment is movable or not. */
    private movable;
    /** Whether the comment is deletable or not. */
    private deletable;
    /** The location of the comment in workspace coordinates. */
    protected location: Coordinate;
    /** Whether this comment has been disposed or not. */
    protected disposed: boolean;
    /** Whether this comment is being disposed or not. */
    protected disposing: boolean;
    /**
     * Constructs the comment.
     *
     * @param workspace The workspace to construct the comment in.
     * @param id An optional ID to give to the comment. If not provided, one will
     *     be generated.
     */
    constructor(workspace: Workspace, id?: string);
    private fireCreateEvent;
    private fireDeleteEvent;
    /** Fires a comment change event. */
    private fireChangeEvent;
    /** Fires a comment collapse event. */
    private fireCollapseEvent;
    /** Sets the text of the comment. */
    setText(text: string): void;
    /** Returns the text of the comment. */
    getText(): string;
    /** Sets the comment's size in workspace units. */
    setSize(size: Size): void;
    /** Returns the comment's size in workspace units. */
    getSize(): Size;
    /** Sets whether the comment is collapsed or not. */
    setCollapsed(collapsed: boolean): void;
    /** Returns whether the comment is collapsed or not. */
    isCollapsed(): boolean;
    /** Sets whether the comment is editable or not. */
    setEditable(editable: boolean): void;
    /**
     * Returns whether the comment is editable or not, respecting whether the
     * workspace is read-only.
     */
    isEditable(): boolean;
    /**
     * Returns whether the comment is editable or not, only examining its own
     * state and ignoring the state of the workspace.
     */
    isOwnEditable(): boolean;
    /** Sets whether the comment is movable or not. */
    setMovable(movable: boolean): void;
    /**
     * Returns whether the comment is movable or not, respecting whether the
     * workspace is read-only.
     */
    isMovable(): boolean;
    /**
     * Returns whether the comment is movable or not, only examining its own
     * state and ignoring the state of the workspace.
     */
    isOwnMovable(): boolean;
    /** Sets whether the comment is deletable or not. */
    setDeletable(deletable: boolean): void;
    /**
     * Returns whether the comment is deletable or not, respecting whether the
     * workspace is read-only.
     */
    isDeletable(): boolean;
    /**
     * Returns whether the comment is deletable or not, only examining its own
     * state and ignoring the state of the workspace.
     */
    isOwnDeletable(): boolean;
    /** Moves the comment to the given location in workspace coordinates. */
    moveTo(location: Coordinate, reason?: string[] | undefined): void;
    /** Returns the position of the comment in workspace coordinates. */
    getRelativeToSurfaceXY(): Coordinate;
    /** Disposes of this comment. */
    dispose(): void;
    /** Returns whether the comment has been disposed or not. */
    isDisposed(): boolean;
    /**
     * Returns true if this comment view is currently being disposed or has
     * already been disposed.
     */
    isDeadOrDying(): boolean;
}
//# sourceMappingURL=workspace_comment.d.ts.map