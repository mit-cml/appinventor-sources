/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IVariableModel, IVariableState } from './i_variable_model.js';
/**
 * Variable maps are container objects responsible for storing and managing the
 * set of variables referenced on a workspace.
 *
 * Any of these methods may define invariants about which names and types are
 * legal, and throw if they are not met.
 */
export interface IVariableMap<T extends IVariableModel<IVariableState>> {
    getVariableById(id: string): T | null;
    /**
     * Returns the variable with the given name, or null if not found. If `type`
     * is provided, the variable's type must also match, or null should be
     * returned.
     */
    getVariable(name: string, type?: string): T | null;
    getAllVariables(): T[];
    /**
     * Returns a list of all of the variables of the given type managed by this
     * variable map.
     */
    getVariablesOfType(type: string): T[];
    /**
     * Returns a list of the set of types of the variables managed by this
     * variable map.
     */
    getTypes(): string[];
    /**
     * Creates a new variable with the given name. If ID is not specified, the
     * variable map should create one. Returns the new variable.
     */
    createVariable(name: string, type?: string, id?: string | null): T;
    addVariable(variable: T): void;
    /**
     * Changes the name of the given variable to the name provided and returns the
     * renamed variable.
     */
    renameVariable(variable: T, newName: string): T;
    changeVariableType(variable: T, newType: string): T;
    deleteVariable(variable: T): void;
    clear(): void;
}
//# sourceMappingURL=i_variable_map.d.ts.map