/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for a block change event.
 *
 * @class
 */
import type { Block } from '../block.js';
import type { BlockSvg } from '../block_svg.js';
import { Workspace } from '../workspace.js';
import { BlockBase, BlockBaseJson } from './events_block_base.js';
/**
 * Notifies listeners when some element of a block has changed (e.g.
 * field values, comments, etc).
 */
export declare class BlockChange extends BlockBase {
    type: string;
    /**
     * The element that changed; one of 'field', 'comment', 'collapsed',
     * 'disabled', 'inline', or 'mutation'
     */
    element?: string;
    /** The name of the field that changed, if this is a change to a field. */
    name?: string;
    /** The original value of the element. */
    oldValue: unknown;
    /** The new value of the element. */
    newValue: unknown;
    /**
     * @param opt_block The changed block.  Undefined for a blank event.
     * @param opt_element One of 'field', 'comment', 'disabled', etc.
     * @param opt_name Name of input or field affected, or null.
     * @param opt_oldValue Previous value of element.
     * @param opt_newValue New value of element.
     */
    constructor(opt_block?: Block, opt_element?: string, opt_name?: string | null, opt_oldValue?: unknown, opt_newValue?: unknown);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): BlockChangeJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of BlockChange, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: BlockChangeJson, workspace: Workspace, event?: any): BlockChange;
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
    /**
     * Returns the extra state of the given block (either as XML or a JSO,
     * depending on the block's definition).
     *
     * @param block The block to get the extra state of.
     * @returns A stringified version of the extra state of the given block.
     * @internal
     */
    static getExtraBlockState_(block: BlockSvg): string;
}
export interface BlockChangeJson extends BlockBaseJson {
    element: string;
    name?: string;
    newValue: unknown;
    oldValue: unknown;
}
//# sourceMappingURL=events_block_change.d.ts.map