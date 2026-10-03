/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for a variable type change event.
 *
 * @class
 */
import type { IVariableModel, IVariableState } from '../interfaces/i_variable_model.js';
import type { Workspace } from '../workspace.js';
import { VarBase, VarBaseJson } from './events_var_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners that a variable's type has changed.
 */
export declare class VarTypeChange extends VarBase {
    oldType?: string | undefined;
    newType?: string | undefined;
    type: EventType;
    /**
     * @param variable The variable whose type changed. Undefined for a blank event.
     * @param oldType The old type of the variable. Undefined for a blank event.
     * @param newType The new type of the variable. Undefined for a blank event.
     */
    constructor(variable?: IVariableModel<IVariableState>, oldType?: string | undefined, newType?: string | undefined);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): VarTypeChangeJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of VarTypeChange, but we can't specify that due to the fact that
     *     parameters to static methods in subclasses must be supertypes of
     *     parameters to static methods in superclasses.
     * @internal
     */
    static fromJson(json: VarTypeChangeJson, workspace: Workspace, event?: any): VarTypeChange;
    /**
     * Run a variable type change event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface VarTypeChangeJson extends VarBaseJson {
    oldType: string;
    newType: string;
}
//# sourceMappingURL=events_var_type_change.d.ts.map