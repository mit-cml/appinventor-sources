/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * The class representing an in-progress gesture, e.g. a drag,
 * tap, or pinch to zoom.
 *
 * @class
 */
import './events/events_click.js';
import type { BlockSvg } from './block_svg.js';
import { RenderedWorkspaceComment } from './comments.js';
import type { Field } from './field.js';
import type { IBubble } from './interfaces/i_bubble.js';
import { IDragger } from './interfaces/i_dragger.js';
import type { IFlyout } from './interfaces/i_flyout.js';
import type { IIcon } from './interfaces/i_icon.js';
import { Coordinate } from './utils/coordinate.js';
import { WorkspaceDragger } from './workspace_dragger.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for one gesture.
 */
export declare class Gesture {
    private readonly creatorWorkspace;
    /**
     * The position of the pointer when the gesture started.  Units are CSS
     * pixels, with (0, 0) at the top left of the browser window (pointer event
     * clientX/Y).
     */
    private mouseDownXY;
    private currentDragDeltaXY;
    /**
     * The bubble that the gesture started on, or null if it did not start on a
     * bubble.
     */
    private startBubble;
    /**
     * The field that the gesture started on, or null if it did not start on a
     * field.
     */
    private startField;
    /**
     * The icon that the gesture started on, or null if it did not start on an
     * icon.
     */
    private startIcon;
    /**
     * The block that the gesture started on, or null if it did not start on a
     * block.
     */
    private startBlock;
    /**
     * The comment that the gesture started on, or null if it did not start on a
     * comment.
     */
    private startComment;
    /**
     * The block that this gesture targets.  If the gesture started on a
     * shadow block, this is the first non-shadow parent of the block.  If the
     * gesture started in the flyout, this is the root block of the block group
     * that was clicked or dragged.
     */
    private targetBlock;
    /**
     * The workspace that the gesture started on.  There may be multiple
     * workspaces on a page; this is more accurate than using
     * Blockly.common.getMainWorkspace().
     */
    protected startWorkspace_: WorkspaceSvg | null;
    /**
     * Whether the pointer has at any point moved out of the drag radius.
     * A gesture that exceeds the drag radius is a drag even if it ends exactly
     * at its start point.
     */
    private hasExceededDragRadius;
    /**
     * Array holding info needed to unbind events.
     * Used for disposing.
     * Ex: [[node, name, func], [node, name, func]].
     */
    private boundEvents;
    private dragger;
    /**
     * The object tracking a workspace or flyout workspace drag, or null if none
     * is in progress.
     */
    private workspaceDragger;
    /** Whether the gesture is dragging or not. */
    private dragging;
    /** The flyout a gesture started in, if any. */
    private flyout;
    /** Boolean for sanity-checking that some code is only called once. */
    private calledUpdateIsDragging;
    /** Boolean for sanity-checking that some code is only called once. */
    private gestureHasStarted;
    /** Boolean used internally to break a cycle in disposal. */
    protected isEnding_: boolean;
    /** The event that most recently updated this gesture. */
    private mostRecentEvent;
    /** Boolean for whether or not this gesture is a multi-touch gesture. */
    private multiTouch;
    /** A map of cached points used for tracking multi-touch gestures. */
    private cachedPoints;
    /**
     * This is the ratio between the starting distance between the touch points
     * and the most recent distance between the touch points.
     * Scales between 0 and 1 mean the most recent zoom was a zoom out.
     * Scales above 1.0 mean the most recent zoom was a zoom in.
     */
    private previousScale;
    /** The starting distance between two touch points. */
    private startDistance;
    /** Boolean for whether or not the workspace supports pinch-zoom. */
    private isPinchZoomEnabled;
    /**
     * The owner of the dropdownDiv when this gesture first starts.
     * Needed because we'll close the dropdown before fields get to
     * act on their events, and some fields care about who owns
     * the dropdown.
     */
    currentDropdownOwner: Field | null;
    /**
     * @param e The event that kicked off this gesture.
     * @param creatorWorkspace The workspace that created this gesture and has a
     *     reference to it.
     */
    constructor(e: PointerEvent, creatorWorkspace: WorkspaceSvg);
    /**
     * Sever all links from this object.
     *
     * @internal
     */
    dispose(): void;
    /**
     * Update internal state based on an event.
     *
     * @param e The most recent pointer event.
     */
    private updateFromEvent;
    /**
     * DO MATH to set currentDragDeltaXY_ based on the most recent pointer
     * position.
     *
     * @param currentXY The most recent pointer position, in pixel units,
     *     with (0, 0) at the window's top left corner.
     * @returns True if the drag just exceeded the drag radius for the first time.
     */
    private updateDragDelta;
    /**
     * Update this gesture to record whether a block is being dragged from the
     * flyout.
     * This function should be called on a pointermove event the first time
     * the drag radius is exceeded.  It should be called no more than once per
     * gesture. If a block should be dragged from the flyout this function creates
     * the new block on the main workspace and updates targetBlock_ and
     * startWorkspace_.
     *
     * @returns True if a block is being dragged from the flyout.
     */
    private updateIsDraggingFromFlyout;
    /**
     * Check whether to start a workspace drag. If a workspace is being dragged,
     * create the necessary WorkspaceDragger and start the drag.
     *
     * This function should be called on a pointermove event the first time
     * the drag radius is exceeded.  It should be called no more than once per
     * gesture. If a workspace is being dragged this function creates the
     * necessary WorkspaceDragger and starts the drag.
     */
    private updateIsDraggingWorkspace;
    /**
     * Update this gesture to record whether anything is being dragged.
     * This function should be called on a pointermove event the first time
     * the drag radius is exceeded.  It should be called no more than once per
     * gesture.
     */
    private updateIsDragging;
    private createDragger;
    /**
     * Start a gesture: update the workspace to indicate that a gesture is in
     * progress and bind pointermove and pointerup handlers.
     *
     * @param e A pointerdown event.
     * @internal
     */
    doStart(e: PointerEvent): void;
    /**
     * Bind gesture events.
     *
     * @param e A pointerdown event.
     * @internal
     */
    bindMouseEvents(e: PointerEvent): void;
    /**
     * Handle a pointerdown event.
     *
     * @param e A pointerdown event.
     * @internal
     */
    handleStart(e: PointerEvent): void;
    /**
     * Handle a pointermove event.
     *
     * @param e A pointermove event.
     * @internal
     */
    handleMove(e: PointerEvent): void;
    /**
     * Handle a pointerup event.
     *
     * @param e A pointerup event.
     * @internal
     */
    handleUp(e: PointerEvent): void;
    /**
     * Handle a pointerdown event and keep track of current
     * pointers.
     *
     * @param e A pointerdown event.
     * @internal
     */
    handleTouchStart(e: PointerEvent): void;
    /**
     * Handle a pointermove event and zoom in/out if two pointers
     * are on the screen.
     *
     * @param e A pointermove event.
     * @internal
     */
    handleTouchMove(e: PointerEvent): void;
    /**
     * Handle pinch zoom gesture.
     *
     * @param e A pointermove event.
     */
    private handlePinch;
    /**
     * Handle a pointerup event and end the gesture.
     *
     * @param e A pointerup event.
     * @internal
     */
    handleTouchEnd(e: PointerEvent): void;
    /**
     * Helper function returning the current touch point coordinate.
     *
     * @param e A pointer event.
     * @returns The current touch point coordinate
     * @internal
     */
    getTouchPoint(e: PointerEvent): Coordinate | null;
    /**
     * Whether this gesture is part of a multi-touch gesture.
     *
     * @returns Whether this gesture is part of a multi-touch gesture.
     * @internal
     */
    isMultiTouch(): boolean;
    /**
     * Cancel an in-progress gesture.  If a workspace or block drag is in
     * progress, end the drag at the most recent location.
     *
     * @internal
     */
    cancel(): void;
    /**
     * Handle a real or faked right-click event by showing a context menu.
     *
     * @param e A pointerdown event.
     * @internal
     */
    handleRightClick(e: PointerEvent): void;
    /**
     * Handle a pointerdown event on a workspace.
     *
     * @param e A pointerdown event.
     * @param ws The workspace the event hit.
     * @internal
     */
    handleWsStart(e: PointerEvent, ws: WorkspaceSvg): void;
    /**
     * Fires a workspace click event.
     *
     * @param ws The workspace that a user clicks on.
     */
    private fireWorkspaceClick;
    /**
     * Handle a pointerdown event on a flyout.
     *
     * @param e A pointerdown event.
     * @param flyout The flyout the event hit.
     * @internal
     */
    handleFlyoutStart(e: PointerEvent, flyout: IFlyout): void;
    /**
     * Handle a pointerdown event on a block.
     *
     * @param e A pointerdown event.
     * @param block The block the event hit.
     * @internal
     */
    handleBlockStart(e: PointerEvent, block: BlockSvg): void;
    /**
     * Handle a pointerdown event on a bubble.
     *
     * @param e A pointerdown event.
     * @param bubble The bubble the event hit.
     * @internal
     */
    handleBubbleStart(e: PointerEvent, bubble: IBubble): void;
    /**
     * Handle a pointerdown event on a workspace comment.
     *
     * @param e A pointerdown event.
     * @param comment The comment the event hit.
     * @internal
     */
    handleCommentStart(e: PointerEvent, comment: RenderedWorkspaceComment): void;
    /** Execute a field click. */
    private doFieldClick;
    /** Execute an icon click. */
    private doIconClick;
    /** Execute a block click. */
    private doBlockClick;
    /**
     * Execute a workspace click. When in accessibility mode shift clicking will
     * move the cursor.
     *
     * @param _e A pointerup event.
     */
    private doWorkspaceClick;
    /**
     * Move the dragged/clicked block to the front of the workspace so that it is
     * not occluded by other blocks.
     */
    private bringBlockToFront;
    /**
     * Record the field that a gesture started on.
     *
     * @param field The field the gesture started on.
     * @internal
     */
    setStartField<T>(field: Field<T>): void;
    /**
     * Record the icon that a gesture started on.
     *
     * @param icon The icon the gesture started on.
     * @internal
     */
    setStartIcon(icon: IIcon): void;
    /**
     * Record the bubble that a gesture started on
     *
     * @param bubble The bubble the gesture started on.
     * @internal
     */
    setStartBubble(bubble: IBubble): void;
    /**
     * Record the comment that a gesture started on
     *
     * @param comment The comment the gesture started on.
     * @internal
     */
    setStartComment(comment: RenderedWorkspaceComment): void;
    /**
     * Record the block that a gesture started on, and set the target block
     * appropriately.
     *
     * @param block The block the gesture started on.
     * @internal
     */
    setStartBlock(block: BlockSvg): void;
    /**
     * Record the block that a gesture targets, meaning the block that will be
     * dragged if this turns into a drag.  If this block is a shadow, that will be
     * its first non-shadow parent.
     *
     * @param block The block the gesture targets.
     */
    private setTargetBlock;
    /**
     * Record the workspace that a gesture started on.
     *
     * @param ws The workspace the gesture started on.
     */
    private setStartWorkspace;
    /**
     * Record the flyout that a gesture started on.
     *
     * @param flyout The flyout the gesture started on.
     */
    private setStartFlyout;
    /**
     * Whether this gesture is a click on a bubble.  This should only be called
     * when ending a gesture (pointerup).
     *
     * @returns Whether this gesture was a click on a bubble.
     */
    private isBubbleClick;
    private isCommentClick;
    /**
     * Whether this gesture is a click on a block.  This should only be called
     * when ending a gesture (pointerup).
     *
     * @returns Whether this gesture was a click on a block.
     */
    private isBlockClick;
    /**
     * Whether this gesture is a click on a field that should be handled.  This should only be called
     * when ending a gesture (pointerup).
     *
     * @returns Whether this gesture was a click on a field.
     */
    private isFieldClick;
    /** @returns Whether this gesture is a click on an icon that should be handled. */
    private isIconClick;
    /**
     * Whether this gesture is a click on a workspace.  This should only be called
     * when ending a gesture (pointerup).
     *
     * @returns Whether this gesture was a click on a workspace.
     */
    private isWorkspaceClick;
    /** Returns the current dragger if the gesture is a drag. */
    getCurrentDragger(): WorkspaceDragger | IDragger | null;
    /**
     * Whether this gesture is a drag of either a workspace or block.
     * This function is called externally to block actions that cannot be taken
     * mid-drag (e.g. using the keyboard to delete the selected blocks).
     *
     * @returns True if this gesture is a drag of a workspace or block.
     * @internal
     */
    isDragging(): boolean;
    /**
     * Whether this gesture has already been started.  In theory every pointerdown
     * has a corresponding pointerup, but in reality it is possible to lose a
     * pointerup, leaving an in-process gesture hanging.
     *
     * @returns Whether this gesture was a click on a workspace.
     * @internal
     */
    hasStarted(): boolean;
    /**
     * Is a drag or other gesture currently in progress on any workspace?
     *
     * @returns True if gesture is occurring.
     */
    static inProgress(): boolean;
}
//# sourceMappingURL=gesture.d.ts.map