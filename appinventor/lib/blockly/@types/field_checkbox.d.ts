/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Checkbox field.  Checked or not checked.
 *
 * @class
 */
import './events/events_block_change.js';
import { Field, FieldConfig, FieldValidator } from './field.js';
type BoolString = 'TRUE' | 'FALSE';
type CheckboxBool = BoolString | boolean;
/**
 * Class for a checkbox field.
 */
export declare class FieldCheckbox extends Field<CheckboxBool> {
    /** Default character for the checkmark. */
    static readonly CHECK_CHAR = "\u2713";
    private checkChar;
    /**
     * Serializable fields are saved by the serializer, non-serializable fields
     * are not. Editable fields should also be serializable.
     */
    SERIALIZABLE: boolean;
    /**
     * Mouse cursor style when over the hotspot that initiates editability.
     */
    CURSOR: string;
    /**
     * NOTE: The default value is set in `Field`, so maintain that value instead
     * of overwriting it here or in the constructor.
     */
    value_: boolean | null;
    /**
     * @param value The initial value of the field. Should either be 'TRUE',
     *     'FALSE' or a boolean. Defaults to 'FALSE'. Also accepts
     *     Field.SKIP_SETUP if you wish to skip setup (only used by subclasses
     *     that want to handle configuration and setting the field value after
     *     their own constructors have run).
     * @param validator  A function that is called to validate changes to the
     *     field's value. Takes in a value ('TRUE' or 'FALSE') & returns a
     *     validated value ('TRUE' or 'FALSE'), or null to abort the change.
     * @param config A map of options used to configure the field.
     *     See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/checkbox#creation}
     * for a list of properties this parameter supports.
     */
    constructor(value?: CheckboxBool | typeof Field.SKIP_SETUP, validator?: FieldCheckboxValidator, config?: FieldCheckboxConfig);
    /**
     * Configure the field based on the given map of options.
     *
     * @param config A map of options to configure the field based on.
     */
    protected configure_(config: FieldCheckboxConfig): void;
    /**
     * Saves this field's value.
     *
     * @returns The boolean value held by this field.
     * @internal
     */
    saveState(): any;
    /**
     * Create the block UI for this checkbox.
     */
    initView(): void;
    render_(): void;
    getDisplayText_(): string;
    /**
     * Set the character used for the check mark.
     *
     * @param character The character to use for the check mark, or null to use
     *     the default.
     */
    setCheckCharacter(character: string | null): void;
    /** Toggle the state of the checkbox on click. */
    protected showEditor_(): void;
    /**
     * Ensure that the input value is valid ('TRUE' or 'FALSE').
     *
     * @param newValue The input value.
     * @returns A valid value ('TRUE' or 'FALSE), or null if invalid.
     */
    protected doClassValidation_(newValue?: any): BoolString | null;
    /**
     * Update the value of the field, and update the checkElement.
     *
     * @param newValue The value to be saved. The default validator guarantees
     *     that this is a either 'TRUE' or 'FALSE'.
     */
    protected doValueUpdate_(newValue: BoolString): void;
    /**
     * Get the value of this field, either 'TRUE' or 'FALSE'.
     *
     * @returns The value of this field.
     */
    getValue(): BoolString;
    /**
     * Get the boolean value of this field.
     *
     * @returns The boolean value of this field.
     */
    getValueBoolean(): boolean | null;
    /**
     * Get the text of this field. Used when the block is collapsed.
     *
     * @returns Text representing the value of this field ('true' or 'false').
     */
    getText(): string;
    /**
     * Convert a value into a pure boolean.
     *
     * Converts 'TRUE' to true and 'FALSE' to false correctly, everything else
     * is cast to a boolean.
     *
     * @param value The value to convert.
     * @returns The converted value.
     */
    private convertValueToBool_;
    /**
     * Construct a FieldCheckbox from a JSON arg object.
     *
     * @param options A JSON object with options (checked).
     * @returns The new field instance.
     * @nocollapse
     * @internal
     */
    static fromJson(options: FieldCheckboxFromJsonConfig): FieldCheckbox;
}
/**
 * Config options for the checkbox field.
 */
export interface FieldCheckboxConfig extends FieldConfig {
    checkCharacter?: string;
}
/**
 * fromJson config options for the checkbox field.
 */
export interface FieldCheckboxFromJsonConfig extends FieldCheckboxConfig {
    checked?: boolean;
}
/**
 * A function that is called to validate changes to the field's value before
 * they are set.
 *
 * @see {@link https://developers.google.com/blockly/guides/create-custom-blocks/fields/validators#return_values}
 * @param newValue The value to be validated.
 * @returns One of three instructions for setting the new value: `T`, `null`,
 * or `undefined`.
 *
 * - `T` to set this function's returned value instead of `newValue`.
 *
 * - `null` to invoke `doValueInvalid_` and not set a value.
 *
 * - `undefined` to set `newValue` as is.
 */
export type FieldCheckboxValidator = FieldValidator<CheckboxBool>;
export {};
//# sourceMappingURL=field_checkbox.d.ts.map