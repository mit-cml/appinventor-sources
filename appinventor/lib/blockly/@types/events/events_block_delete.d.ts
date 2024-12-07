/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for a block delete event.
 *
 * @class
 */
import type { Block } from '../block.js';
import * as blocks from '../serialization/blocks.js';
import { BlockBase, BlockBaseJson } from './events_block_base.js';
import { Workspace } from '../workspace.js';
/**
 * Notifies listeners when a block (or connected stack of blocks) is
 * deleted.
 */
export declare class BlockDelete extends BlockBase {
    /** The XML representation of the deleted block(s). */
    oldXml?: Element | DocumentFragment;
    /** The JSON respresentation of the deleted block(s). */
    oldJson?: blocks.State;
    /** All of the IDs of deleted blocks. */
    ids?: string[];
    /** True if the deleted block was a shadow block, false otherwise. */
    wasShadow?: boolean;
    type: string;
    /** @param opt_block The deleted block.  Undefined for a blank event. */
    constructor(opt_block?: Block);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): BlockDeleteJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of BlockDelete, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: BlockDeleteJson, workspace: Workspace, event?: any): BlockDelete;
    /**
     * Run a deletion event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface BlockDeleteJson extends BlockBaseJson {
    oldXml: string;
    ids: string[];
    wasShadow: boolean;
    oldJson: blocks.State;
    recordUndo?: boolean;
}
//# sourceMappingURL=events_block_delete.d.ts.map