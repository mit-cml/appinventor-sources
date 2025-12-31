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
 * Notifies listeners that the workspace surface's position or scale has
 * changed.
 *
 * Does not notify when the workspace itself resizes.
 */
export declare class ViewportChange extends UiBase {
    /**
     * Top edge of the visible portion of the workspace, relative to the
     * workspace origin.
     */
    viewTop?: number;
    /**
     * The left edge of the visible portion of the workspace, relative to
     * the workspace origin.
     */
    viewLeft?: number;
    /** The scale of the workpace. */
    scale?: number;
    /** The previous scale of the workspace. */
    oldScale?: number;
    type: EventType;
    /**
     * @param opt_top Top-edge of the visible portion of the workspace, relative
     *     to the workspace origin. Undefined for a blank event.
     * @param opt_left Left-edge of the visible portion of the workspace relative
     *     to the workspace origin. Undefined for a blank event.
     * @param opt_scale The scale of the workspace. Undefined for a blank event.
     * @param opt_workspaceId The workspace identifier for this event.
     *    Undefined for a blank event.
     * @param opt_oldScale The old scale of the workspace. Undefined for a blank
     *     event.
     */
    constructor(opt_top?: number, opt_left?: number, opt_scale?: number, opt_workspaceId?: string, opt_oldScale?: number);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): ViewportChangeJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of Viewport, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: ViewportChangeJson, workspace: Workspace, event?: any): ViewportChange;
}
export interface ViewportChangeJson extends AbstractEventJson {
    viewTop: number;
    viewLeft: number;
    scale: number;
    oldScale: number;
}
//# sourceMappingURL=events_viewport.d.ts.map