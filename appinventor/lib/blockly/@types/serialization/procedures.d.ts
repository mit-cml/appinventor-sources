/**
 * @license
 * Copyright 2021 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IParameterModel } from '../interfaces/i_parameter_model.js';
import { IProcedureModel } from '../interfaces/i_procedure_model.js';
import type { ISerializer } from '../interfaces/i_serializer.js';
import type { Workspace } from '../workspace.js';
/**
 * Representation of a procedure data model.
 */
export interface State {
    id: string;
    name: string;
    returnTypes: string[] | null;
    parameters?: ParameterState[];
}
/**
 * Representation of a parameter data model.
 */
export interface ParameterState {
    id: string;
    name: string;
    types?: string[];
}
/**
 * A newable signature for an IProcedureModel.
 *
 * Refer to
 * https://www.typescriptlang.org/docs/handbook/2/generics.html#using-class-types-in-generics
 * for what is going on with this.
 */
type ProcedureModelConstructor<ProcedureModel extends IProcedureModel> = new (workspace: Workspace, name: string, id: string) => ProcedureModel;
/**
 * A newable signature for an IParameterModel.
 *
 * Refer to
 * https://www.typescriptlang.org/docs/handbook/2/generics.html#using-class-types-in-generics
 * for what is going on with this.
 */
type ParameterModelConstructor<ParameterModel extends IParameterModel> = new (workspace: Workspace, name: string, id: string) => ParameterModel;
/**
 * Serializes the given IProcedureModel to JSON.
 *
 * @internal
 */
export declare function saveProcedure(proc: IProcedureModel): State;
/**
 * Serializes the given IParameterModel to JSON.
 *
 * @internal
 */
export declare function saveParameter(param: IParameterModel): ParameterState;
/**
 * Deserializes the given procedure model State from JSON.
 *
 * @internal
 */
export declare function loadProcedure<ProcedureModel extends IProcedureModel, ParameterModel extends IParameterModel>(procedureModelClass: ProcedureModelConstructor<ProcedureModel>, parameterModelClass: ParameterModelConstructor<ParameterModel>, state: State, workspace: Workspace): ProcedureModel;
/**
 * Deserializes the given ParameterState from JSON.
 *
 * @internal
 */
export declare function loadParameter<ParameterModel extends IParameterModel>(parameterModelClass: ParameterModelConstructor<ParameterModel>, state: ParameterState, workspace: Workspace): ParameterModel;
/** Serializer for saving and loading procedure state. */
export declare class ProcedureSerializer<ProcedureModel extends IProcedureModel, ParameterModel extends IParameterModel> implements ISerializer {
    private readonly procedureModelClass;
    private readonly parameterModelClass;
    priority: number;
    /**
     * Constructs the procedure serializer.
     *
     * Example usage:
     *   new ProcedureSerializer(MyProcedureModelClass, MyParameterModelClass)
     *
     * @param procedureModelClass The class (implementing IProcedureModel) that
     *     you want this serializer to deserialize.
     * @param parameterModelClass The class (implementing IParameterModel) that
     *     you want this serializer to deserialize.
     */
    constructor(procedureModelClass: ProcedureModelConstructor<ProcedureModel>, parameterModelClass: ParameterModelConstructor<ParameterModel>);
    /** Serializes the procedure models of the given workspace. */
    save(workspace: Workspace): State[] | null;
    /**
     * Deserializes the procedures models defined by the given state into the
     * workspace.
     */
    load(state: State[], workspace: Workspace): void;
    /** Disposes of any procedure models that exist on the workspace. */
    clear(workspace: Workspace): void;
}
export {};
//# sourceMappingURL=procedures.d.ts.map