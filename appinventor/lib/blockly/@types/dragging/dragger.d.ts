/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IDeletable } from '../interfaces/i_deletable.js';
import type { IDragTarget } from '../interfaces/i_drag_target.js';
import { type IDraggable } from '../interfaces/i_draggable.js';
import type { IDragger } from '../interfaces/i_dragger.js';
import { Coordinate } from '../utils/coordinate.js';
export declare class Dragger implements IDragger {
    protected draggable: IDraggable;
    protected startLoc: Coordinate;
    protected dragTarget: IDragTarget | null;
    constructor(draggable: IDraggable);
    /** Handles any drag startup. */
    onDragStart(e?: PointerEvent | KeyboardEvent): IDraggable;
    /**
     * Handles calculating where the element should actually be moved to.
     *
     * @param totalDelta The total amount in pixel coordinates the mouse has moved
     *     since the start of the drag.
     */
    onDrag(e: PointerEvent | KeyboardEvent | undefined, totalDelta: Coordinate): void;
    /** Updates the drag target under the pointer (if there is one). */
    protected updateDragTarget(coordinate: Coordinate): void;
    /**
     * Calculates the correct workspace coordinate for the movable and tells
     * the draggable to go to that location.
     */
    private moveDraggable;
    /**
     * Returns true if we would delete the draggable if it was dropped
     * at the current location.
     */
    protected wouldDeleteDraggable(coordinate: Coordinate, rootDraggable: IDraggable & IDeletable): boolean;
    /** Handles any drag cleanup. */
    onDragEnd(e?: PointerEvent | KeyboardEvent): void;
    /** Handles a drag being reverted. */
    onDragRevert(): void;
    /**
     * Returns true if we should return the draggable to its original location
     * at the end of the drag.
     */
    protected shouldReturnToStart(coordinate: Coordinate, rootDraggable: IDraggable): boolean;
    protected pixelsToWorkspaceUnits(pixelCoord: Coordinate): Coordinate;
}
//# sourceMappingURL=dragger.d.ts.map