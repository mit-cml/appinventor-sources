/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Events fired as a result of UI click in Blockly's editor.
 *
 * @class
 */
import type { Block } from '../block.js';
import { AbstractEventJson } from './events_abstract.js';
import { UiBase } from './events_ui_base.js';
import { Workspace } from '../workspace.js';
/**
 * Notifies listeners that ome blockly element was clicked.
 */
export declare class Click extends UiBase {
    /** The ID of the block that was clicked, if a block was clicked. */
    blockId?: string;
    /**
     * The type of element that was clicked; one of 'block', 'workspace',
     * or 'zoom_controls'.
     */
    targetType?: ClickTarget;
    type: string;
    /**
     * @param opt_block The affected block. Null for click events that do not have
     *     an associated block (i.e. workspace click). Undefined for a blank
     *     event.
     * @param opt_workspaceId The workspace identifier for this event.
     *    Not used if block is passed. Undefined for a blank event.
     * @param opt_targetType The type of element targeted by this click event.
     *     Undefined for a blank event.
     */
    constructor(opt_block?: Block | null, opt_workspaceId?: string | null, opt_targetType?: ClickTarget);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): ClickJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of Click, but we can't specify that due to the fact that parameters to
     *     static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: ClickJson, workspace: Workspace, event?: any): Click;
}
export declare enum ClickTarget {
    BLOCK = "block",
    WORKSPACE = "workspace",
    ZOOM_CONTROLS = "zoom_controls"
}
export interface ClickJson extends AbstractEventJson {
    targetType: ClickTarget;
    blockId?: string;
}
//# sourceMappingURL=events_click.d.ts.map