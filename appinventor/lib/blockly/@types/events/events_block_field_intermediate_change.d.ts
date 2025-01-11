/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for an event representing an intermediate change to a block's field's
 * value.
 *
 * @class
 */
import type { Block } from '../block.js';
import { Workspace } from '../workspace.js';
import { BlockBase, BlockBaseJson } from './events_block_base.js';
/**
 * Notifies listeners when the value of a block's field has changed but the
 * change is not yet complete, and is expected to be followed by a block change
 * event.
 */
export declare class BlockFieldIntermediateChange extends BlockBase {
    type: string;
    recordUndo: boolean;
    /** The name of the field that changed. */
    name?: string;
    /** The original value of the element. */
    oldValue: unknown;
    /** The new value of the element. */
    newValue: unknown;
    /**
     * @param opt_block The changed block. Undefined for a blank event.
     * @param opt_name Name of the field affected.
     * @param opt_oldValue Previous value of element.
     * @param opt_newValue New value of element.
     */
    constructor(opt_block?: Block, opt_name?: string, opt_oldValue?: unknown, opt_newValue?: unknown);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): BlockFieldIntermediateChangeJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of BlockFieldIntermediateChange, but we can't specify that due to the
     *     fact that parameters to static methods in subclasses must be supertypes
     *     of parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: BlockFieldIntermediateChangeJson, workspace: Workspace, event?: any): BlockFieldIntermediateChange;
    /**
     * Does this event record any change of state?
     *
     * @returns False if something changed.
     */
    isNull(): boolean;
    /**
     * Run a change event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface BlockFieldIntermediateChangeJson extends BlockBaseJson {
    name: string;
    newValue: unknown;
    oldValue: unknown;
}
//# sourceMappingURL=events_block_field_intermediate_change.d.ts.map