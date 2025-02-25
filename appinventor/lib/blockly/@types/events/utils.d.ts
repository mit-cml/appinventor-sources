/**
 * @license
 * Copyright 2021 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from '../block.js';
import type { Workspace } from '../workspace.js';
import type { Abstract } from './events_abstract.js';
import type { BlockCreate } from './events_block_create.js';
import type { BlockMove } from './events_block_move.js';
import type { CommentCreate } from './events_comment_create.js';
import type { CommentMove } from './events_comment_move.js';
/**
 * Sets whether events should be added to the undo stack.
 *
 * @param newValue True if events should be added to the undo stack.
 */
export declare function setRecordUndo(newValue: boolean): void;
/**
 * Returns whether or not events will be added to the undo stack.
 *
 * @returns True if events will be added to the undo stack.
 */
export declare function getRecordUndo(): boolean;
/**
 * Name of event that creates a block. Will be deprecated for BLOCK_CREATE.
 */
export declare const CREATE = "create";
/**
 * Name of event that creates a block.
 */
export declare const BLOCK_CREATE = "create";
/**
 * Name of event that deletes a block. Will be deprecated for BLOCK_DELETE.
 */
export declare const DELETE = "delete";
/**
 * Name of event that deletes a block.
 */
export declare const BLOCK_DELETE = "delete";
/**
 * Name of event that changes a block. Will be deprecated for BLOCK_CHANGE.
 */
export declare const CHANGE = "change";
/**
 * Name of event that changes a block.
 */
export declare const BLOCK_CHANGE = "change";
/**
 * Name of event representing an in-progress change to a field of a block, which
 * is expected to be followed by a block change event.
 */
export declare const BLOCK_FIELD_INTERMEDIATE_CHANGE = "block_field_intermediate_change";
/**
 * Name of event that moves a block. Will be deprecated for BLOCK_MOVE.
 */
export declare const MOVE = "move";
/**
 * Name of event that moves a block.
 */
export declare const BLOCK_MOVE = "move";
/**
 * Name of event that creates a variable.
 */
export declare const VAR_CREATE = "var_create";
/**
 * Name of event that deletes a variable.
 */
export declare const VAR_DELETE = "var_delete";
/**
 * Name of event that renames a variable.
 */
export declare const VAR_RENAME = "var_rename";
/**
 * Name of generic event that records a UI change.
 */
export declare const UI = "ui";
/**
 * Name of event that record a block drags a block.
 */
export declare const BLOCK_DRAG = "drag";
/**
 * Name of event that records a change in selected element.
 */
export declare const SELECTED = "selected";
/**
 * Name of event that records a click.
 */
export declare const CLICK = "click";
/**
 * Name of event that records a marker move.
 */
export declare const MARKER_MOVE = "marker_move";
/**
 * Name of event that records a bubble open.
 */
export declare const BUBBLE_OPEN = "bubble_open";
/**
 * Name of event that records a trashcan open.
 */
export declare const TRASHCAN_OPEN = "trashcan_open";
/**
 * Name of event that records a toolbox item select.
 */
export declare const TOOLBOX_ITEM_SELECT = "toolbox_item_select";
/**
 * Name of event that records a theme change.
 */
export declare const THEME_CHANGE = "theme_change";
/**
 * Name of event that records a viewport change.
 */
export declare const VIEWPORT_CHANGE = "viewport_change";
/**
 * Name of event that creates a comment.
 */
export declare const COMMENT_CREATE = "comment_create";
/**
 * Name of event that deletes a comment.
 */
export declare const COMMENT_DELETE = "comment_delete";
/**
 * Name of event that changes a comment.
 */
export declare const COMMENT_CHANGE = "comment_change";
/**
 * Name of event that moves a comment.
 */
export declare const COMMENT_MOVE = "comment_move";
/**
 * Name of event that records a workspace load.
 */
export declare const FINISHED_LOADING = "finished_loading";
/**
 * Type of events that cause objects to be bumped back into the visible
 * portion of the workspace.
 *
 * Not to be confused with bumping so that disconnected connections do not
 * appear connected.
 */
export type BumpEvent = BlockCreate | BlockMove | CommentCreate | CommentMove;
/**
 * List of events that cause objects to be bumped back into the visible
 * portion of the workspace.
 *
 * Not to be confused with bumping so that disconnected connections do not
 * appear connected.
 */
export declare const BUMP_EVENTS: string[];
/**
 * Create a custom event and fire it.
 *
 * @param event Custom data for event.
 */
export declare function fire(event: Abstract): void;
/**
 * Private version of fireInternal for stubbing in tests.
 */
declare function fireInternal(event: Abstract): void;
/** Fire all queued events. */
declare function fireNow(): void;
/**
 * Filter the queued events and merge duplicates.
 *
 * @param queueIn Array of events.
 * @param forward True if forward (redo), false if backward (undo).
 * @returns Array of filtered events.
 */
export declare function filter(queueIn: Abstract[], forward: boolean): Abstract[];
/**
 * Modify pending undo events so that when they are fired they don't land
 * in the undo stack.  Called by Workspace.clearUndo.
 */
export declare function clearPendingUndo(): void;
/**
 * Stop sending events.  Every call to this function MUST also call enable.
 */
export declare function disable(): void;
/**
 * Start sending events.  Unless events were already disabled when the
 * corresponding call to disable was made.
 */
export declare function enable(): void;
/**
 * Returns whether events may be fired or not.
 *
 * @returns True if enabled.
 */
export declare function isEnabled(): boolean;
/**
 * Current group.
 *
 * @returns ID string.
 */
export declare function getGroup(): string;
/**
 * Start or stop a group.
 *
 * @param state True to start new group, false to end group.
 *   String to set group explicitly.
 */
export declare function setGroup(state: boolean | string): void;
/**
 * Private version of setGroup for stubbing in tests.
 */
declare function setGroupInternal(state: boolean | string): void;
/**
 * Compute a list of the IDs of the specified block and all its descendants.
 *
 * @param block The root block.
 * @returns List of block IDs.
 * @internal
 */
export declare function getDescendantIds(block: Block): string[];
/**
 * Decode the JSON into an event.
 *
 * @param json JSON representation.
 * @param workspace Target workspace for event.
 * @returns The event represented by the JSON.
 * @throws {Error} if an event type is not found in the registry.
 */
export declare function fromJson(json: any, workspace: Workspace): Abstract;
/**
 * Gets the class for a specific event type from the registry.
 *
 * @param eventType The type of the event to get.
 * @returns The event class with the given type.
 */
export declare function get(eventType: string): new (...p1: any[]) => Abstract;
/**
 * Enable/disable a block depending on whether it is properly connected.
 * Use this on applications where all blocks should be connected to a top block.
 * Recommend setting the 'disable' option to 'false' in the config so that
 * users don't try to re-enable disabled orphan blocks.
 *
 * @param event Custom data for event.
 */
export declare function disableOrphans(event: Abstract): void;
export declare const TEST_ONLY: {
    FIRE_QUEUE: Abstract[];
    fireNow: typeof fireNow;
    fireInternal: typeof fireInternal;
    setGroupInternal: typeof setGroupInternal;
};
export {};
//# sourceMappingURL=utils.d.ts.map