/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Base class for all types of block events.
 *
 * @class
 */
import type { Block } from '../block.js';
import type { Workspace } from '../workspace.js';
import { Abstract as AbstractEvent, AbstractEventJson } from './events_abstract.js';
/**
 * Abstract class for any event related to blocks.
 */
export declare class BlockBase extends AbstractEvent {
    isBlank: boolean;
    /** The ID of the block associated with this event. */
    blockId?: string;
    /**
     * @param opt_block The block this event corresponds to.
     *     Undefined for a blank event.
     */
    constructor(opt_block?: Block);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): BlockBaseJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of BlockBase, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: BlockBaseJson, workspace: Workspace, event?: any): BlockBase;
}
export interface BlockBaseJson extends AbstractEventJson {
    blockId: string;
}
//# sourceMappingURL=events_block_base.d.ts.map