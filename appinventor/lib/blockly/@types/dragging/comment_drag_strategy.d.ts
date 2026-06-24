/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { RenderedWorkspaceComment } from '../comments.js';
import { IDragStrategy } from '../interfaces/i_draggable.js';
import { Coordinate } from '../utils.js';
export declare class CommentDragStrategy implements IDragStrategy {
    private comment;
    private startLoc;
    private workspace;
    constructor(comment: RenderedWorkspaceComment);
    isMovable(): boolean;
    startDrag(): void;
    drag(newLoc: Coordinate): void;
    endDrag(): void;
    /** Fire a UI event at the start of a comment drag. */
    private fireDragStartEvent;
    /** Fire a UI event at the end of a comment drag. */
    private fireDragEndEvent;
    /** Fire a move event at the end of a comment drag. */
    private fireMoveEvent;
    revertDrag(): void;
}
//# sourceMappingURL=comment_drag_strategy.d.ts.map