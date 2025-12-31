/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Workspace } from '../workspace.js';
import { Abstract as AbstractEvent } from './events_abstract.js';
import { EventType } from './type.js';
/**
 * Notifies listeners when the workspace has finished deserializing from
 * JSON/XML.
 */
export declare class FinishedLoading extends AbstractEvent {
    isBlank: boolean;
    recordUndo: boolean;
    type: EventType;
    /**
     * @param opt_workspace The workspace that has finished loading.  Undefined
     *     for a blank event.
     */
    constructor(opt_workspace?: Workspace);
}
//# sourceMappingURL=workspace_events.d.ts.map