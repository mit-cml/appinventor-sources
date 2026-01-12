/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { AbstractEventJson } from './events_abstract.js';
import { UiBase } from './events_ui_base.js';
import type { Workspace } from '../workspace.js';
/**
 * Notifies listeners that the workspace theme has changed.
 */
export declare class ThemeChange extends UiBase {
    /** The name of the new theme that has been set. */
    themeName?: string;
    type: string;
    /**
     * @param opt_themeName The theme name. Undefined for a blank event.
     * @param opt_workspaceId The workspace identifier for this event.
     *    event. Undefined for a blank event.
     */
    constructor(opt_themeName?: string, opt_workspaceId?: string);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): ThemeChangeJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of ThemeChange, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: ThemeChangeJson, workspace: Workspace, event?: any): ThemeChange;
}
export interface ThemeChangeJson extends AbstractEventJson {
    themeName: string;
}
//# sourceMappingURL=events_theme_change.d.ts.map