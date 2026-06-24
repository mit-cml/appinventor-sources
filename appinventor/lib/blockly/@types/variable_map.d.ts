/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Object representing a map of variables and their types.
 *
 * @class
 */
import './events/events_var_delete.js';
import './events/events_var_rename.js';
import type { Block } from './block.js';
import type { IVariableMap } from './interfaces/i_variable_map.js';
import { IVariableModel, IVariableState } from './interfaces/i_variable_model.js';
import type { Workspace } from './workspace.js';
/**
 * Class for a variable map.  This contains a dictionary data structure with
 * variable types as keys and lists of variables as values.  The list of
 * variables are the type indicated by the key.
 */
export declare class VariableMap implements IVariableMap<IVariableModel<IVariableState>> {
    workspace: Workspace;
    potentialMap: boolean;
    /**
     * A map from variable type to map of IDs to variables. The maps contain
     * all of the named variables in the workspace, including variables that are
     * not currently in use.
     */
    private variableMap;
    /**
     * @param workspace The workspace this map belongs to.
     * @param potentialMap True if this holds variables that don't exist in the
     *  workspace yet.
     */
    constructor(workspace: Workspace, potentialMap?: boolean);
    /** Clear the variable map.  Fires events for every deletion. */
    clear(): void;
    /**
     * Rename the given variable by updating its name in the variable map.
     *
     * @param variable Variable to rename.
     * @param newName New variable name.
     * @returns The newly renamed variable.
     */
    renameVariable(variable: IVariableModel<IVariableState>, newName: string): IVariableModel<IVariableState>;
    changeVariableType(variable: IVariableModel<IVariableState>, newType: string): IVariableModel<IVariableState>;
    /**
     * Rename a variable by updating its name in the variable map. Identify the
     * variable to rename with the given ID.
     *
     * @deprecated v12: use VariableMap.renameVariable.
     * @param id ID of the variable to rename.
     * @param newName New variable name.
     */
    renameVariableById(id: string, newName: string): void;
    /**
     * Update the name of the given variable and refresh all references to it.
     * The new name must not conflict with any existing variable names.
     *
     * @param variable Variable to rename.
     * @param newName New variable name.
     * @param blocks The list of all blocks in the workspace.
     */
    private renameVariableAndUses;
    /**
     * Update the name of the given variable to the same name as an existing
     * variable.  The two variables are coalesced into a single variable with the
     * ID of the existing variable that was already using newName. Refresh all
     * references to the variable.
     *
     * @param variable Variable to rename.
     * @param newName New variable name.
     * @param conflictVar The variable that was already using newName.
     * @param blocks The list of all blocks in the workspace.
     */
    private renameVariableWithConflict;
    /**
     * Create a variable with a given name, optional type, and optional ID.
     *
     * @param name The name of the variable. This must be unique across variables
     *     and procedures.
     * @param opt_type The type of the variable like 'int' or 'string'.
     *     Does not need to be unique. Field_variable can filter variables based
     * on their type. This will default to '' which is a specific type.
     * @param opt_id The unique ID of the variable. This will default to a UUID.
     * @returns The newly created variable.
     */
    createVariable(name: string, opt_type?: string, opt_id?: string): IVariableModel<IVariableState>;
    /**
     * Adds the given variable to this variable map.
     *
     * @param variable The variable to add.
     */
    addVariable(variable: IVariableModel<IVariableState>): void;
    /**
     * Delete a variable and all of its uses without confirmation.
     *
     * @param variable Variable to delete.
     */
    deleteVariable(variable: IVariableModel<IVariableState>): void;
    /**
     * Delete a variables by the passed in ID and all of its uses from this
     * workspace. May prompt the user for confirmation.
     *
     * @deprecated v12: use Blockly.Variables.deleteVariable.
     * @param id ID of variable to delete.
     */
    deleteVariableById(id: string): void;
    /**
     * Find the variable by the given name and type and return it.  Return null if
     *     it is not found.
     *
     * @param name The name to check for.
     * @param opt_type The type of the variable.  If not provided it defaults to
     *     the empty string, which is a specific type.
     * @returns The variable with the given name, or null if it was not found.
     */
    getVariable(name: string, opt_type?: string): IVariableModel<IVariableState> | null;
    /**
     * Find the variable by the given ID and return it.  Return null if not found.
     *
     * @param id The ID to check for.
     * @returns The variable with the given ID.
     */
    getVariableById(id: string): IVariableModel<IVariableState> | null;
    /**
     * Get a list containing all of the variables of a specified type. If type is
     *     null, return list of variables with empty string type.
     *
     * @param type Type of the variables to find.
     * @returns The sought after variables of the passed in type. An empty array
     *     if none are found.
     */
    getVariablesOfType(type: string | null): IVariableModel<IVariableState>[];
    /**
     * Returns a list of unique types of variables in this variable map.
     *
     * @returns A list of unique types of variables in this variable map.
     */
    getTypes(): string[];
    /**
     * Return all variables of all types.
     *
     * @returns List of variable models.
     */
    getAllVariables(): IVariableModel<IVariableState>[];
    /**
     * Returns all of the variable names of all types.
     *
     * @deprecated v12: use Blockly.Variables.getAllVariables.
     * @returns All of the variable names of all types.
     */
    getAllVariableNames(): string[];
    /**
     * Find all the uses of a named variable.
     *
     * @deprecated v12: use Blockly.Variables.getVariableUsesById.
     * @param id ID of the variable to find.
     * @returns Array of block usages.
     */
    getVariableUsesById(id: string): Block[];
}
//# sourceMappingURL=variable_map.d.ts.map