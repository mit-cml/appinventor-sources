/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from './block.js';
export declare const TEST_ONLY: {
    allExtensions: any;
};
/**
 * Registers a new extension function. Extensions are functions that help
 * initialize blocks, usually adding dynamic behavior such as onchange
 * handlers and mutators. These are applied using Block.applyExtension(), or
 * the JSON "extensions" array attribute.
 *
 * @param name The name of this extension.
 * @param initFn The function to initialize an extended block.
 * @throws {Error} if the extension name is empty, the extension is already
 *     registered, or extensionFn is not a function.
 */
export declare function register<T extends Block>(name: string, initFn: (this: T) => void): void;
/**
 * Registers a new extension function that adds all key/value of mixinObj.
 *
 * @param name The name of this extension.
 * @param mixinObj The values to mix in.
 * @throws {Error} if the extension name is empty or the extension is already
 *     registered.
 */
export declare function registerMixin(name: string, mixinObj: any): void;
/**
 * Registers a new extension function that adds a mutator to the block.
 * At register time this performs some basic sanity checks on the mutator.
 * The wrapper may also add a mutator dialog to the block, if both compose and
 * decompose are defined on the mixin.
 *
 * @param name The name of this mutator extension.
 * @param mixinObj The values to mix in.
 * @param opt_helperFn An optional function to apply after mixing in the object.
 * @param opt_blockList A list of blocks to appear in the flyout of the mutator
 *     dialog.
 * @throws {Error} if the mutation is invalid or can't be applied to the block.
 */
export declare function registerMutator(name: string, mixinObj: any, opt_helperFn?: () => any, opt_blockList?: string[]): void;
/**
 * Unregisters the extension registered with the given name.
 *
 * @param name The name of the extension to unregister.
 */
export declare function unregister(name: string): void;
/**
 * Returns whether an extension is registered with the given name.
 *
 * @param name The name of the extension to check for.
 * @returns True if the extension is registered.  False if it is not registered.
 */
export declare function isRegistered(name: string): boolean;
/**
 * Applies an extension method to a block. This should only be called during
 * block construction.
 *
 * @param name The name of the extension.
 * @param block The block to apply the named extension to.
 * @param isMutator True if this extension defines a mutator.
 * @throws {Error} if the extension is not found.
 */
export declare function apply(name: string, block: Block, isMutator: boolean): void;
/**
 * Calls a function after the page has loaded, possibly immediately.
 *
 * @param fn Function to run.
 * @throws Error Will throw if no global document can be found (e.g., Node.js).
 * @internal
 */
export declare function runAfterPageLoad(fn: () => void): void;
/**
 * Builds an extension function that will map a dropdown value to a tooltip
 * string.
 *
 * @param dropdownName The name of the field whose value is the key to the
 *     lookup table.
 * @param lookupTable The table of field values to tooltip text.
 * @returns The extension function.
 */
export declare function buildTooltipForDropdown(dropdownName: string, lookupTable: {
    [key: string]: string;
}): (this: Block) => void;
/**
 * Builds an extension function that will install a dynamic tooltip. The
 * tooltip message should include the string '%1' and that string will be
 * replaced with the text of the named field.
 *
 * @param msgTemplate The template form to of the message text, with %1
 *     placeholder.
 * @param fieldName The field with the replacement text.
 * @returns The extension function.
 */
export declare function buildTooltipWithFieldText(msgTemplate: string, fieldName: string): (this: Block) => void;
//# sourceMappingURL=extensions.d.ts.map