/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { AbstractEventJson } from './events_abstract.js';
import { UiBase } from './events_ui_base.js';
import type { Workspace } from '../workspace.js';
/**
 * Notifies listeners that a toolbox item has been selected.
 */
export declare class ToolboxItemSelect extends UiBase {
    /** The previously selected toolbox item. */
    oldItem?: string;
    /** The newly selected toolbox item. */
    newItem?: string;
    type: string;
    /**
     * @param opt_oldItem The previously selected toolbox item.
     *     Undefined for a blank event.
     * @param opt_newItem The newly selected toolbox item. Undefined for a blank
     *     event.
     * @param opt_workspaceId The workspace identifier for this event.
     *    Undefined for a blank event.
     */
    constructor(opt_oldItem?: string | null, opt_newItem?: string | null, opt_workspaceId?: string);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): ToolboxItemSelectJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of ToolboxItemSelect, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: ToolboxItemSelectJson, workspace: Workspace, event?: any): ToolboxItemSelect;
}
export interface ToolboxItemSelectJson extends AbstractEventJson {
    oldItem?: string;
    newItem?: string;
}
//# sourceMappingURL=events_toolbox_item_select.d.ts.map