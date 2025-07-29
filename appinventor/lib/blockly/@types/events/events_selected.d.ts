/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Workspace } from '../workspace.js';
import { AbstractEventJson } from './events_abstract.js';
import { UiBase } from './events_ui_base.js';
import { EventType } from './type.js';
/**
 * Class for a selected event.
 * Notifies listeners that a new element has been selected.
 */
export declare class Selected extends UiBase {
    /** The id of the last selected selectable element. */
    oldElementId?: string;
    /**
     * The id of the newly selected selectable element,
     * or undefined if unselected.
     */
    newElementId?: string;
    type: EventType;
    /**
     * @param opt_oldElementId The ID of the previously selected element. Null if
     *     no element last selected. Undefined for a blank event.
     * @param opt_newElementId The ID of the selected element. Null if no element
     *     currently selected (deselect). Undefined for a blank event.
     * @param opt_workspaceId The workspace identifier for this event.
     *    Null if no element previously selected. Undefined for a blank event.
     */
    constructor(opt_oldElementId?: string | null, opt_newElementId?: string | null, opt_workspaceId?: string);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): SelectedJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of Selected, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: SelectedJson, workspace: Workspace, event?: any): Selected;
}
export interface SelectedJson extends AbstractEventJson {
    oldElementId?: string;
    newElementId?: string;
}
//# sourceMappingURL=events_selected.d.ts.map