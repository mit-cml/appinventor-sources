import type { IDraggable } from '../interfaces/i_draggable.js';
/**
 * Cardinal directions in which a move can proceed.
 */
export declare enum Direction {
    NONE = 0,
    UP = 1,
    DOWN = 2,
    LEFT = 3,
    RIGHT = 4
}
/**
 * Class responsible for coordinating keyboard-driven moves with the workspace
 * and dragging system.
 */
export declare class KeyboardMover {
    /**
     * Object responsible for dragging workspace elements in response to move
     * commands.
     */
    private dragger?;
    /**
     * The object that is currently being moved.
     */
    private draggable?;
    /**
     * Workspace coordinate that the current move started from.
     */
    private startLocation?;
    /**
     * The total distance, in workspace coordinates, that the element being moved
     * has been moved since the movement process started.
     */
    private totalDelta;
    /**
     * The distance to move an item in workspace coordinates.
     */
    private stepDistance;
    /**
     * Symbol attached to the item being moved to indicate it is in move mode.
     */
    private moveIndicator?;
    private allowedShortcuts;
    private readonly blurListener;
    static mover: KeyboardMover;
    private constructor();
    /**
     * Returns true iff the given draggable is allowed to be moved.
     *
     * @param draggable The draggable element to try to move.
     * @returns True iff movement is allowed.
     */
    canMove(draggable: IDraggable): boolean;
    /**
     * Returns true iff this Mover is currently moving an element.
     *
     * @returns True iff a workspace element is being moved.
     */
    isMoving(): boolean;
    /**
     * Start moving the currently-focused item on workspace, if possible.
     *
     * @param draggable The element to start moving.
     * @param event The keyboard event that triggered this move.
     * @returns True iff a move has successfully begun.
     */
    startMove(draggable: IDraggable, event?: KeyboardEvent): boolean;
    /**
     * Moves the current element in the given direction.
     *
     * @param direction The direction to move the currently-moving element.
     * @param event The event that triggered this move, if any.
     * @returns True iff this action applies and has been performed.
     */
    move(direction: Direction, event?: KeyboardEvent | PointerEvent): boolean;
    /**
     * Finish moving the item that is currently being moved.
     *
     * @param event The event that triggered the end of the move, if any.
     * @returns True iff move successfully finished.
     */
    finishMove(event?: KeyboardEvent | PointerEvent): boolean;
    /**
     * Abort moving the currently-focused item on workspace.
     *
     * @param event The event that triggered the end of the move, if any.
     * @returns True iff move successfully aborted.
     */
    abortMove(event?: KeyboardEvent | PointerEvent): boolean;
    /**
     * Sets the distance by which an object will be moved.
     *
     * @param stepDistance The distance in workspace coordinates that each move
     *     should move elements on the workspace by.
     */
    setMoveDistance(stepDistance: number): void;
    /**
     * Returns a list of the names of shortcuts that are allowed to be run while
     * a keyboard-driven move is in progress.
     */
    getAllowedShortcuts(): string[];
    /**
     * Adds shortcuts with the given names to the list of shortcuts that are
     * allowed to be run while a keyboard-driven move is in progress.
     */
    setAllowedShortcuts(shortcutNames: string[]): void;
    /**
     * Repositions the move indicator to the corner of the item being moved.
     */
    private repositionMoveIndicator;
    /**
     * Returns a bounding box used for positioning the move indicator on a block.
     * Blocks require special treatment because `BlockSvg.getBoundingRectangle()`
     * includes the bounds of nested and subsequent blocks. Since the move
     * indicator is positioned at the top right corner of the bounds, this can
     * result in it appearing to float in empty space when e.g. a small block has
     * a much wider block nested inside a statement input. BlockSvg also provides
     * `getBoundingRectangleWithoutChildren()`, which addresses that case, but is
     * insufficient because in the case of nested *value* blocks in a row, the
     * child blocks' bounds should contribute to the bounding box.
     *
     * @param block The block to retrieve an adjusted bounding box for.
     * @returns A bounding box for the given block whose top-right corner
     *     corresponds to the maximum visual extent of the given block's row.
     */
    private positionForBlockMoveIndicator;
    /**
     * Common clean-up for finish/abort run before terminating the move.
     */
    private preDragEndCleanup;
    /**
     * Common clean-up for finish/abort run after terminating the move.
     */
    private postDragEndCleanup;
    /**
     * Returns the total distance current element has moved in pixels.
     */
    private totalPixelDelta;
    /**
     * Scrolls the current element into view.
     */
    private scrollCurrentElementIntoView;
    /**
     * Recalculates the total movement delta from the starting location and the
     * current position of the item being moved.
     */
    private updateTotalDelta;
}
//# sourceMappingURL=keyboard_mover.d.ts.map