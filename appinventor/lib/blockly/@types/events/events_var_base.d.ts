/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Abstract class for a variable event.
 *
 * @class
 */
import type { IVariableModel, IVariableState } from '../interfaces/i_variable_model.js';
import type { Workspace } from '../workspace.js';
import { Abstract as AbstractEvent, AbstractEventJson } from './events_abstract.js';
/**
 * Abstract class for a variable event.
 */
export declare class VarBase extends AbstractEvent {
    isBlank: boolean;
    /** The ID of the variable this event references. */
    varId?: string;
    /**
     * @param opt_variable The variable this event corresponds to.  Undefined for
     *     a blank event.
     */
    constructor(opt_variable?: IVariableModel<IVariableState>);
    /**
     * Encode the event as JSON.
     *
     * @returns JSON representation.
     */
    toJson(): VarBaseJson;
    /**
     * Deserializes the JSON event.
     *
     * @param event The event to append new properties to. Should be a subclass
     *     of VarBase, but we can't specify that due to the fact that parameters
     *     to static methods in subclasses must be supertypes of parameters to
     *     static methods in superclasses.
     * @internal
     */
    static fromJson(json: VarBaseJson, workspace: Workspace, event?: any): VarBase;
}
export interface VarBaseJson extends AbstractEventJson {
    varId: string;
}
//# sourceMappingURL=events_var_base.d.ts.map