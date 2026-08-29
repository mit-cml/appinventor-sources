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
 * Notifies listeners that a variable model was renamed.
 */
export declare class VarRename extends VarBase {
    type: EventType;
    /** The previous name of the variable. */
    oldName?: string;
    /** The new name of the variable. */
    newName?: string;
    /**
     * @param opt_variable The renamed variable. Undefined for a blank event.
     * @param newName The new name the variable will be changed to.
     */
    constructor(opt_variable?: IVariableModel<IVariableState>, newName?: string);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): VarRenameJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of VarRename, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: VarRenameJson, workspace: Workspace, event?: any): VarRename;
    /**
     * Run a variable rename event.
     *
     * @param forward True if run forward, false if run backward (undo).
     */
    run(forward: boolean): void;
}
export interface VarRenameJson extends VarBaseJson {
    oldName: string;
    newName: string;
}
//# sourceMappingURL=events_var_rename.d.ts.map