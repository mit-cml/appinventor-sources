/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { WorkspaceCommentCopyData } from '../clipboard/workspace_comment_paster.js';
import { IBoundedElement } from '../interfaces/i_bounded_element.js';
import { IContextMenu } from '../interfaces/i_contextmenu.js';
import { ICopyable } from '../interfaces/i_copyable.js';
import { IDeletable } from '../interfaces/i_deletable.js';
import { IDraggable } from '../interfaces/i_draggable.js';
import { IRenderedElement } from '../interfaces/i_rendered_element.js';
import { ISelectable } from '../interfaces/i_selectable.js';
import { Coordinate } from '../utils/coordinate.js';
import { Rect } from '../utils/rect.js';
import { Size } from '../utils/size.js';
import { WorkspaceSvg } from '../workspace_svg.js';
import { WorkspaceComment } from './workspace_comment.js';
export declare class RenderedWorkspaceComment extends WorkspaceComment implements IBoundedElement, IRenderedElement, IDraggable, ISelectable, IDeletable, ICopyable<WorkspaceCommentCopyData>, IContextMenu {
    /** The class encompassing the svg elements making up the workspace comment. */
    private view;
    readonly workspace: WorkspaceSvg;
    private dragStrategy;
    /** Constructs the workspace comment, including the view. */
    constructor(workspace: WorkspaceSvg, id?: string);
    /**
     * Adds listeners to the view that updates the model (i.e. the superclass)
     * when changes are made to the view.
     */
    private addModelUpdateBindings;
    /** Sets the text of the comment. */
    setText(text: string): void;
    /** Sets the size of the comment. */
    setSize(size: Size): void;
    /** Sets whether the comment is collapsed or not. */
    setCollapsed(collapsed: boolean): void;
    /** Sets whether the comment is editable or not. */
    setEditable(editable: boolean): void;
    /** Returns the root SVG element of this comment. */
    getSvgRoot(): SVGElement;
    /**
     * Returns the comment's size in workspace units.
     * Does not respect collapsing.
     */
    getSize(): Size;
    /**
     * Returns the bounding rectangle of this comment in workspace coordinates.
     * Respects collapsing.
     */
    getBoundingRectangle(): Rect;
    /** Move the comment by the given amounts in workspace coordinates. */
    moveBy(dx: number, dy: number, reason?: string[] | undefined): void;
    /** Moves the comment to the given location in workspace coordinates. */
    moveTo(location: Coordinate, reason?: string[] | undefined): void;
    /**
     * Moves the comment during a drag. Doesn't fire move events.
     *
     * @internal
     */
    moveDuringDrag(location: Coordinate): void;
    /**
     * Adds the dragging CSS class to this comment.
     *
     * @internal
     */
    setDragging(dragging: boolean): void;
    /** Disposes of the view. */
    dispose(): void;
    /**
     * Starts a gesture because we detected a pointer down on the comment
     * (that wasn't otherwise gobbled up, e.g. by resizing).
     */
    private startGesture;
    /** Visually indicates that this comment would be deleted if dropped. */
    setDeleteStyle(wouldDelete: boolean): void;
    /** Returns whether this comment is movable or not. */
    isMovable(): boolean;
    /** Starts a drag on the comment. */
    startDrag(): void;
    /** Drags the comment to the given location. */
    drag(newLoc: Coordinate): void;
    /** Ends the drag on the comment. */
    endDrag(): void;
    /** Moves the comment back to where it was at the start of a drag. */
    revertDrag(): void;
    /** Visually highlights the comment. */
    select(): void;
    /** Visually unhighlights the comment. */
    unselect(): void;
    /**
     * Returns a JSON serializable representation of this comment's state that
     * can be used for pasting.
     */
    toCopyData(): WorkspaceCommentCopyData | null;
    /** Show a context menu for this comment. */
    showContextMenu(e: PointerEvent): void;
    /** Snap this comment to the nearest grid point. */
    snapToGrid(): void;
}
//# sourceMappingURL=rendered_workspace_comment.d.ts.map