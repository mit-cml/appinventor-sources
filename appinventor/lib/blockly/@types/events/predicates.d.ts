/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * @file Predicates for testing Abstract event subclasses based on
 * their .type properties.  These are useful because there are places
 * where it is not possible to use instanceof <ClassConstructor> tests
 * for type narrowing due to load ordering issues that would be caused
 * by the need to import (rather than just import type) the class
 * constructors in question.
 */
import type { Abstract } from './events_abstract.js';
import type { BlockChange } from './events_block_change.js';
import type { BlockCreate } from './events_block_create.js';
import type { BlockDelete } from './events_block_delete.js';
import type { BlockDrag } from './events_block_drag.js';
import type { BlockFieldIntermediateChange } from './events_block_field_intermediate_change.js';
import type { BlockMove } from './events_block_move.js';
import type { BubbleOpen } from './events_bubble_open.js';
import type { Click } from './events_click.js';
import type { CommentChange } from './events_comment_change.js';
import type { CommentCollapse } from './events_comment_collapse.js';
import type { CommentCreate } from './events_comment_create.js';
import type { CommentDelete } from './events_comment_delete.js';
import type { CommentDrag } from './events_comment_drag.js';
import type { CommentMove } from './events_comment_move.js';
import type { CommentResize } from './events_comment_resize.js';
import type { Selected } from './events_selected.js';
import type { ThemeChange } from './events_theme_change.js';
import type { ToolboxItemSelect } from './events_toolbox_item_select.js';
import type { TrashcanOpen } from './events_trashcan_open.js';
import type { VarCreate } from './events_var_create.js';
import type { VarDelete } from './events_var_delete.js';
import type { VarRename } from './events_var_rename.js';
import type { ViewportChange } from './events_viewport.js';
import type { FinishedLoading } from './workspace_events.js';
/** @returns true iff event.type is EventType.BLOCK_CREATE */
export declare function isBlockCreate(event: Abstract): event is BlockCreate;
/** @returns true iff event.type is EventType.BLOCK_DELETE */
export declare function isBlockDelete(event: Abstract): event is BlockDelete;
/** @returns true iff event.type is EventType.BLOCK_CHANGE */
export declare function isBlockChange(event: Abstract): event is BlockChange;
/** @returns true iff event.type is EventType.BLOCK_FIELD_INTERMEDIATE_CHANGE */
export declare function isBlockFieldIntermediateChange(event: Abstract): event is BlockFieldIntermediateChange;
/** @returns true iff event.type is EventType.BLOCK_MOVE */
export declare function isBlockMove(event: Abstract): event is BlockMove;
/** @returns true iff event.type is EventType.VAR_CREATE */
export declare function isVarCreate(event: Abstract): event is VarCreate;
/** @returns true iff event.type is EventType.VAR_DELETE */
export declare function isVarDelete(event: Abstract): event is VarDelete;
/** @returns true iff event.type is EventType.VAR_RENAME */
export declare function isVarRename(event: Abstract): event is VarRename;
/** @returns true iff event.type is EventType.BLOCK_DRAG */
export declare function isBlockDrag(event: Abstract): event is BlockDrag;
/** @returns true iff event.type is EventType.SELECTED */
export declare function isSelected(event: Abstract): event is Selected;
/** @returns true iff event.type is EventType.CLICK */
export declare function isClick(event: Abstract): event is Click;
/** @returns true iff event.type is EventType.BUBBLE_OPEN */
export declare function isBubbleOpen(event: Abstract): event is BubbleOpen;
/** @returns true iff event.type is EventType.TRASHCAN_OPEN */
export declare function isTrashcanOpen(event: Abstract): event is TrashcanOpen;
/** @returns true iff event.type is EventType.TOOLBOX_ITEM_SELECT */
export declare function isToolboxItemSelect(event: Abstract): event is ToolboxItemSelect;
/** @returns true iff event.type is EventType.THEME_CHANGE */
export declare function isThemeChange(event: Abstract): event is ThemeChange;
/** @returns true iff event.type is EventType.VIEWPORT_CHANGE */
export declare function isViewportChange(event: Abstract): event is ViewportChange;
/** @returns true iff event.type is EventType.COMMENT_CREATE */
export declare function isCommentCreate(event: Abstract): event is CommentCreate;
/** @returns true iff event.type is EventType.COMMENT_DELETE */
export declare function isCommentDelete(event: Abstract): event is CommentDelete;
/** @returns true iff event.type is EventType.COMMENT_CHANGE */
export declare function isCommentChange(event: Abstract): event is CommentChange;
/** @returns true iff event.type is EventType.COMMENT_MOVE */
export declare function isCommentMove(event: Abstract): event is CommentMove;
/** @returns true iff event.type is EventType.COMMENT_RESIZE */
export declare function isCommentResize(event: Abstract): event is CommentResize;
/** @returns true iff event.type is EventType.COMMENT_DRAG */
export declare function isCommentDrag(event: Abstract): event is CommentDrag;
/** @returns true iff event.type is EventType.COMMENT_COLLAPSE */
export declare function isCommentCollapse(event: Abstract): event is CommentCollapse;
/** @returns true iff event.type is EventType.FINISHED_LOADING */
export declare function isFinishedLoading(event: Abstract): event is FinishedLoading;
//# sourceMappingURL=predicates.d.ts.map