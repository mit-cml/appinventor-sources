/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IVariableModel, IVariableState } from '../interfaces/i_variable_model.js';
import type { Workspace } from '../workspace.js';
import { VarBase, VarBaseJson } from './events_var_base.js';
import { EventType } from './type.js';
/**
 * Notifies listeners that a variable model has been deleted.
 */
export declare class VarDelete extends VarBase {
    type: EventType;
    /** The type of the variable that was deleted. */
    varType?: string;
    /** The name of the variable that was deleted. */
    varName?: string;
    /**
     * @param opt_variable The deleted variable. Undefined for a blank event.
     */
    constructor(opt_variable?: IVariableModel<IVariableState>);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): VarDeleteJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of VarDelete, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: VarDeleteJson, workspace: Workspace, event?: any): VarDelete;
    /**
     * Run a variable deletion event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface VarDeleteJson extends VarBaseJson {
    varType: string;
    varName: string;
}
//# sourceMappingURL=events_var_delete.d.ts.map