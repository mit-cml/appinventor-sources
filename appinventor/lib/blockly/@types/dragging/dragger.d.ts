/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IDeletable } from '../interfaces/i_deletable.js';
import { IDragTarget } from '../interfaces/i_drag_target.js';
import { IDraggable } from '../interfaces/i_draggable.js';
import { IDragger } from '../interfaces/i_dragger.js';
import { Coordinate } from '../utils/coordinate.js';
import { WorkspaceSvg } from '../workspace_svg.js';
export declare class Dragger implements IDragger {
    protected draggable: IDraggable;
    protected workspace: WorkspaceSvg;
    protected startLoc: Coordinate;
    protected dragTarget: IDragTarget | null;
    constructor(draggable: IDraggable, workspace: WorkspaceSvg);
    /** Handles any drag startup. */
    onDragStart(e: PointerEvent): void;
    /**
     * Handles calculating where the element should actually be moved to.
     *
     * @param totalDelta The total amount in pixel coordinates the mouse has moved
     *     since the start of the drag.
     */
    onDrag(e: PointerEvent, totalDelta: Coordinate): void;
    /** Updates the drag target under the pointer (if there is one). */
    protected updateDragTarget(e: PointerEvent): void;
    /**
     * Calculates the correct workspace coordinate for the movable and tells
     * the draggable to go to that location.
     */
    private moveDraggable;
    /**
     * Returns true if we would delete the draggable if it was dropped
     * at the current location.
     */
    protected wouldDeleteDraggable(e: PointerEvent, rootDraggable: IDraggable & IDeletable): boolean;
    /** Handles any drag cleanup. */
    onDragEnd(e: PointerEvent): void;
    private getRoot;
    /**
     * Returns true if we should return the draggable to its original location
     * at the end of the drag.
     */
    protected shouldReturnToStart(e: PointerEvent, rootDraggable: IDraggable): boolean;
    protected pixelsToWorkspaceUnits(pixelCoord: Coordinate): Coordinate;
}
//# sourceMappingURL=dragger.d.ts.map