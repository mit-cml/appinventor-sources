/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import './events/events_block_change.js';
import type { Block } from './block.js';
import type { Abstract } from './events/events_abstract.js';
import { Field } from './field.js';
import { ProcedureTuple } from './interfaces/i_legacy_procedure_blocks.js';
import { IParameterModel } from './interfaces/i_parameter_model.js';
import { IProcedureBlock, isProcedureBlock } from './interfaces/i_procedure_block.js';
import { IProcedureMap } from './interfaces/i_procedure_map.js';
import { IProcedureModel } from './interfaces/i_procedure_model.js';
import { ObservableProcedureMap } from './observable_procedure_map.js';
import type { Workspace } from './workspace.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * String for use in the "custom" attribute of a category in toolbox XML.
 * This string indicates that the category should be dynamically populated with
 * procedure blocks.
 * See also Blockly.Variables.CATEGORY_NAME and
 * Blockly.VariablesDynamic.CATEGORY_NAME.
 */
export declare const CATEGORY_NAME = "PROCEDURE";
/**
 * The default argument for a procedures_mutatorarg block.
 */
export declare const DEFAULT_ARG = "x";
/**
 * Find all user-created procedure definitions in a workspace.
 *
 * @param root Root workspace.
 * @returns Pair of arrays, the first contains procedures without return
 *     variables, the second with. Each procedure is defined by a three-element
 *     list of name, parameter list, and return value boolean.
 */
export declare function allProcedures(root: Workspace): [ProcedureTuple[], ProcedureTuple[]];
/**
 * Ensure two identically-named procedures don't exist.
 * Take the proposed procedure name, and return a legal name i.e. one that
 * is not empty and doesn't collide with other procedures.
 *
 * @param name Proposed procedure name.
 * @param block Block to disambiguate.
 * @returns Non-colliding name.
 */
export declare function findLegalName(name: string, block: Block): string;
/**
 * Return if the given name is already a procedure name.
 *
 * @param name The questionable name.
 * @param workspace The workspace to scan for collisions.
 * @param opt_exclude Optional block to exclude from comparisons (one doesn't
 *     want to collide with oneself).
 * @returns True if the name is used, otherwise return false.
 */
export declare function isNameUsed(name: string, workspace: Workspace, opt_exclude?: Block): boolean;
/**
 * Rename a procedure.  Called by the editable field.
 *
 * @param name The proposed new name.
 * @returns The accepted name.
 */
export declare function rename(this: Field, name: string): string;
/**
 * Construct the blocks required by the flyout for the procedure category.
 *
 * @param workspace The workspace containing procedures.
 * @returns Array of XML block elements.
 */
export declare function flyoutCategory(workspace: WorkspaceSvg): Element[];
/**
 * Listens for when a procedure mutator is opened. Then it triggers a flyout
 * update and adds a mutator change listener to the mutator workspace.
 *
 * @param e The event that triggered this listener.
 * @internal
 */
export declare function mutatorOpenListener(e: Abstract): void;
/**
 * Find all the callers of a named procedure.
 *
 * @param name Name of procedure.
 * @param workspace The workspace to find callers in.
 * @returns Array of caller blocks.
 */
export declare function getCallers(name: string, workspace: Workspace): Block[];
/**
 * When a procedure definition changes its parameters, find and edit all its
 * callers.
 *
 * @param defBlock Procedure definition block.
 */
export declare function mutateCallers(defBlock: Block): void;
/**
 * Find the definition block for the named procedure.
 *
 * @param name Name of procedure.
 * @param workspace The workspace to search.
 * @returns The procedure definition block, or null not found.
 */
export declare function getDefinition(name: string, workspace: Workspace): Block | null;
export { IParameterModel, IProcedureBlock, IProcedureMap, IProcedureModel, isProcedureBlock, ObservableProcedureMap, ProcedureTuple, };
//# sourceMappingURL=procedures.d.ts.map