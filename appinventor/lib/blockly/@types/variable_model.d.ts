/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Components for the variable model.
 *
 * @class
 */
import './events/events_var_create.js';
import { IVariableModel, IVariableState } from './interfaces/i_variable_model.js';
import type { Workspace } from './workspace.js';
/**
 * Class for a variable model.
 * Holds information for the variable including name, ID, and type.
 *
 * @see {Blockly.FieldVariable}
 */
export declare class VariableModel implements IVariableModel<IVariableState> {
    private readonly workspace;
    private name;
    private type;
    private readonly id;
    /**
     * @param workspace The variable's workspace.
     * @param name The name of the variable.  This is the user-visible name (e.g.
     *     'my var' or '私の変数'), not the generated name.
     * @param opt_type The type of the variable like 'int' or 'string'.
     *     Does not need to be unique. Field_variable can filter variables based
     * on their type. This will default to '' which is a specific type.
     * @param opt_id The unique ID of the variable. This will default to a UUID.
     */
    constructor(workspace: Workspace, name: string, opt_type?: string, opt_id?: string);
    /** @returns The ID for the variable. */
    getId(): string;
    /** @returns The name of this variable. */
    getName(): string;
    /**
     * Updates the user-visible name of this variable.
     *
     * @returns The newly-updated variable.
     */
    setName(newName: string): this;
    /** @returns The type of this variable. */
    getType(): string;
    /**
     * Updates the type of this variable.
     *
     * @returns The newly-updated variable.
     */
    setType(newType: string): this;
    /**
     * Returns the workspace this VariableModel belongs to.
     *
     * @returns The workspace this VariableModel belongs to.
     */
    getWorkspace(): Workspace;
    /**
     * Serializes this VariableModel.
     *
     * @returns a JSON representation of this VariableModel.
     */
    save(): IVariableState;
    /**
     * Loads the persisted state into a new variable in the given workspace.
     *
     * @param state The serialized state of a variable model from save().
     * @param workspace The workspace to create the new variable in.
     */
    static load(state: IVariableState, workspace: Workspace): void;
}
//# sourceMappingURL=variable_model.d.ts.map