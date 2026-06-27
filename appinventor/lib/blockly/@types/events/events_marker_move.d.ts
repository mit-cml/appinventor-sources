/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Events fired as a result of a marker move.
 *
 * @class
 */
import type { Block } from '../block.js';
import { ASTNode } from '../keyboard_nav/ast_node.js';
import type { Workspace } from '../workspace.js';
import { AbstractEventJson } from './events_abstract.js';
import { UiBase } from './events_ui_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners that a marker (used for keyboard navigation) has
 * moved.
 */
export declare class MarkerMove extends UiBase {
    /** The ID of the block the marker is now on, if any. */
    blockId?: string;
    /** The old node the marker used to be on, if any. */
    oldNode?: ASTNode;
    /** The new node the marker is now on. */
    newNode?: ASTNode;
    /**
     * True if this is a cursor event, false otherwise.
     * For information about cursors vs markers see {@link
     * https://blocklycodelabs.dev/codelabs/keyboard-navigation/index.html?index=..%2F..index#1}.
     */
    isCursor?: boolean;
    type: EventType;
    /**
     * @param opt_block The affected block. Null if current node is of type
     *     workspace. Undefined for a blank event.
     * @param isCursor Whether this is a cursor event. Undefined for a blank
     *     event.
     * @param opt_oldNode The old node the marker used to be on.
     *    Undefined for a blank event.
     * @param opt_newNode The new node the marker is now on.
     *    Undefined for a blank event.
     */
    constructor(opt_block?: Block | null, isCursor?: boolean, opt_oldNode?: ASTNode | null, opt_newNode?: ASTNode);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): MarkerMoveJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of MarkerMove, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: MarkerMoveJson, workspace: Workspace, event?: any): MarkerMove;
}
export interface MarkerMoveJson extends AbstractEventJson {
    isCursor: boolean;
    blockId?: string;
    oldNode?: ASTNode;
    newNode: ASTNode;
}
//# sourceMappingURL=events_marker_move.d.ts.map