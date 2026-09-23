/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from './block.js';
import { IVariableModel, IVariableState } from './interfaces/i_variable_model.js';
import type { BlockInfo, FlyoutItemInfo } from './utils/toolbox.js';
import type { Workspace } from './workspace.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * String for use in the "custom" attribute of a category in toolbox XML.
 * This string indicates that the category should be dynamically populated with
 * variable blocks.
 * See also Blockly.Procedures.CATEGORY_NAME and
 * Blockly.VariablesDynamic.CATEGORY_NAME.
 */
export declare const CATEGORY_NAME = "VARIABLE";
/**
 * Find all user-created variables that are in use in the workspace.
 * For use by generators.
 *
 * To get a list of all variables on a workspace, including unused variables,
 * call getAllVariables.
 *
 * @param ws The workspace to search for variables.
 * @returns Array of variable models.
 */
export declare function allUsedVarModels(ws: Workspace): IVariableModel<IVariableState>[];
/**
 * Find all developer variables used by blocks in the workspace.
 *
 * Developer variables are never shown to the user, but are declared as global
 * variables in the generated code.
 * To declare developer variables, define the getDeveloperVariables function on
 * your block and return a list of variable names.
 * For use by generators.
 *
 * @param workspace The workspace to search.
 * @returns A list of non-duplicated variable names.
 */
export declare function allDeveloperVariables(workspace: Workspace): string[];
/**
 * Internal wrapper that returns the contents of the variables category.
 *
 * @internal
 * @param workspace The workspace to populate variable blocks for.
 */
export declare function internalFlyoutCategory(workspace: WorkspaceSvg): FlyoutItemInfo[];
export declare function flyoutCategory(workspace: WorkspaceSvg, useXml: true): Element[];
export declare function flyoutCategory(workspace: WorkspaceSvg, useXml: false): FlyoutItemInfo[];
/**
 * Construct the blocks required by the flyout for the variable category.
 *
 * @internal
 * @param workspace The workspace containing variables.
 * @param variables List of variables to create blocks for.
 * @param includeChangeBlocks True to include `change x by _` blocks.
 * @param getterType The type of the variable getter block to generate.
 * @param setterType The type of the variable setter block to generate.
 * @returns JSON list of blocks.
 */
export declare function jsonFlyoutCategoryBlocks(workspace: Workspace, variables: IVariableModel<IVariableState>[], includeChangeBlocks: boolean, getterType?: string, setterType?: string): BlockInfo[];
/**
 * Construct the blocks required by the flyout for the variable category.
 *
 * @param workspace The workspace containing variables.
 * @returns Array of XML block elements.
 */
export declare function flyoutCategoryBlocks(workspace: Workspace): Element[];
export declare const VAR_LETTER_OPTIONS = "ijkmnopqrstuvwxyzabcdefgh";
/**
 * Return a new variable name that is not yet being used. This will try to
 * generate single letter variable names in the range 'i' to 'z' to start with.
 * If no unique name is located it will try 'i' to 'z', 'a' to 'h',
 * then 'i2' to 'z2' etc.  Skip 'l'.
 *
 * @param workspace The workspace to be unique in.
 * @returns New variable name.
 */
export declare function generateUniqueName(workspace: Workspace): string;
/**
 * Private version of generateUniqueName for stubbing in tests.
 */
declare function generateUniqueNameInternal(workspace: Workspace): string;
/**
 * Returns a unique name that is not present in the usedNames array. This
 * will try to generate single letter names in the range a - z (skip l). It
 * will start with the character passed to startChar.
 *
 * @param startChar The character to start the search at.
 * @param usedNames A list of all of the used names.
 * @returns A unique name that is not present in the usedNames array.
 */
export declare function generateUniqueNameFromOptions(startChar: string, usedNames: string[]): string;
/**
 * Handles "Create Variable" button in the default variables toolbox category.
 * It will prompt the user for a variable name, including re-prompts if a name
 * is already in use among the workspace's variables.
 *
 * Custom button handlers can delegate to this function, allowing variables
 * types and after-creation processing. More complex customization (e.g.,
 * prompting for variable type) is beyond the scope of this function.
 *
 * @param workspace The workspace on which to create the variable.
 * @param opt_callback A callback. It will be passed an acceptable new variable
 *     name, or null if change is to be aborted (cancel button), or undefined if
 *     an existing variable was chosen.
 * @param opt_type The type of the variable like 'int', 'string', or ''. This
 *     will default to '', which is a specific type.
 */
export declare function createVariableButtonHandler(workspace: Workspace, opt_callback?: (p1?: string | null) => void, opt_type?: string): void;
/**
 * Opens a prompt that allows the user to enter a new name for a variable.
 * Triggers a rename if the new name is valid. Or re-prompts if there is a
 * collision.
 *
 * @param workspace The workspace on which to rename the variable.
 * @param variable Variable to rename.
 * @param opt_callback A callback. It will be passed an acceptable new variable
 *     name, or null if change is to be aborted (cancel button), or undefined if
 *     an existing variable was chosen.
 */
export declare function renameVariable(workspace: Workspace, variable: IVariableModel<IVariableState>, opt_callback?: (p1?: string | null) => void): void;
/**
 * Prompt the user for a new variable name.
 *
 * @param promptText The string of the prompt.
 * @param defaultText The default value to show in the prompt's field.
 * @param callback A callback. It will be passed the new variable name, or null
 *     if the user picked something illegal.
 */
export declare function promptName(promptText: string, defaultText: string, callback: (p1: string | null) => void): void;
/**
 * Check whether there exists a variable with the given name of any type.
 *
 * @param name The name to search for.
 * @param workspace The workspace to search for the variable.
 * @returns The variable with the given name, or null if none was found.
 */
export declare function nameUsedWithAnyType(name: string, workspace: Workspace): IVariableModel<IVariableState> | null;
/**
 * Returns the name of the procedure with a conflicting parameter name, or null
 * if one does not exist.
 *
 * This checks the procedure map if it contains models, and the legacy procedure
 * blocks otherwise.
 *
 * @param oldName The old name of the variable.
 * @param newName The proposed name of the variable.
 * @param workspace The workspace to search for conflicting parameters.
 * @internal
 */
export declare function nameUsedWithConflictingParam(oldName: string, newName: string, workspace: Workspace): string | null;
/**
 * Generate DOM objects representing a variable field.
 *
 * @param variableModel The variable model to represent.
 * @returns The generated DOM.
 */
export declare function generateVariableFieldDom(variableModel: IVariableModel<IVariableState>): Element;
/**
 * Helper function to look up or create a variable on the given workspace.
 * If no variable exists, creates and returns it.
 *
 * @param workspace The workspace to search for the variable.  It may be a
 *     flyout workspace or main workspace.
 * @param id The ID to use to look up or create the variable, or null.
 * @param opt_name The string to use to look up or create the variable.
 * @param opt_type The type to use to look up or create the variable.
 * @returns The variable corresponding to the given ID or name + type
 *     combination.
 */
export declare function getOrCreateVariablePackage(workspace: Workspace, id: string | null, opt_name?: string, opt_type?: string): IVariableModel<IVariableState>;
/**
 * Look up  a variable on the given workspace.
 * Always looks in the main workspace before looking in the flyout workspace.
 * Always prefers lookup by ID to lookup by name + type.
 *
 * @param workspace The workspace to search for the variable.  It may be a
 *     flyout workspace or main workspace.
 * @param id The ID to use to look up the variable, or null.
 * @param opt_name The string to use to look up the variable.
 *     Only used if lookup by ID fails.
 * @param opt_type The type to use to look up the variable.
 *     Only used if lookup by ID fails.
 * @returns The variable corresponding to the given ID or name + type
 *     combination, or null if not found.
 */
export declare function getVariable(workspace: Workspace, id: string | null, opt_name?: string, opt_type?: string): IVariableModel<IVariableState> | null;
/**
 * Helper function to get the list of variables that have been added to the
 * workspace after adding a new block, using the given list of variables that
 * were in the workspace before the new block was added.
 *
 * @param workspace The workspace to inspect.
 * @param originalVariables The array of variables that existed in the workspace
 *     before adding the new block.
 * @returns The new array of variables that were freshly added to the workspace
 *     after creating the new block, or [] if no new variables were added to the
 *     workspace.
 * @internal
 */
export declare function getAddedVariables(workspace: Workspace, originalVariables: IVariableModel<IVariableState>[]): IVariableModel<IVariableState>[];
/**
 * A custom compare function for the VariableModel objects.
 *
 * @param var1 First variable to compare.
 * @param var2 Second variable to compare.
 * @returns -1 if name of var1 is less than name of var2, 0 if equal, and 1 if
 *     greater.
 * @internal
 */
export declare function compareByName(var1: IVariableModel<IVariableState>, var2: IVariableModel<IVariableState>): number;
/**
 * Find all the uses of a named variable.
 *
 * @param workspace The workspace to search for the variable.
 * @param id ID of the variable to find.
 * @returns Array of block usages.
 */
export declare function getVariableUsesById(workspace: Workspace, id: string): Block[];
/**
 * Delete a variable and all of its uses from the given workspace. May prompt
 * the user for confirmation.
 *
 * @param workspace The workspace from which to delete the variable.
 * @param variable The variable to delete.
 * @param triggeringBlock The block from which this deletion was triggered, if
 *     any. Used to exclude it from checking and warning about blocks
 *     referencing the variable being deleted.
 */
export declare function deleteVariable(workspace: Workspace, variable: IVariableModel<IVariableState>, triggeringBlock?: Block): void;
export declare const TEST_ONLY: {
    generateUniqueNameInternal: typeof generateUniqueNameInternal;
};
export {};
//# sourceMappingURL=variables.d.ts.map