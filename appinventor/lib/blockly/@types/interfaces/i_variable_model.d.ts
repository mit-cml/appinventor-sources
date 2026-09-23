/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Workspace } from '../workspace.js';
export interface IVariableModel<T extends IVariableState> {
    getId(): string;
    getName(): string;
    /**
     * Returns the type of the variable like 'int' or 'string'.  Does not need to be
     * unique. This will default to '' which is a specific type.
     */
    getType(): string;
    setName(name: string): this;
    setType(type: string): this;
    getWorkspace(): Workspace;
    save(): T;
}
export interface IVariableModelStatic<T extends IVariableState> {
    new (workspace: Workspace, name: string, type?: string, id?: string): IVariableModel<T>;
    /**
     * Creates a new IVariableModel corresponding to the given state on the
     * specified workspace. This method must be static in your implementation.
     */
    load(state: T, workspace: Workspace): IVariableModel<T>;
}
/**
 * Represents the state of a given variable.
 */
export interface IVariableState {
    name: string;
    id: string;
    type?: string;
}
//# sourceMappingURL=i_variable_model.d.ts.map