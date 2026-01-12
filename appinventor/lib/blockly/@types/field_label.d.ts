/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { Field, FieldConfig } from './field.js';
/**
 * Class for a non-editable, non-serializable text field.
 */
export declare class FieldLabel extends Field<string> {
    /** The HTML class name to use for this field. */
    private class;
    /**
     * Editable fields usually show some sort of UI indicating they are
     * editable. This field should not.
     */
    EDITABLE: boolean;
    /** Text labels should not truncate. */
    maxDisplayLength: number;
    /**
     * @param value The initial value of the field. Should cast to a string.
     *     Defaults to an empty string if null or undefined. Also accepts
     *     Field.SKIP_SETUP if you wish to skip setup (only used by subclasses
     *     that want to handle configuration and setting the field value after
     *     their own constructors have run).
     * @param textClass Optional CSS class for the field's text.
     * @param config A map of options used to configure the field.
     *    See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/label#creation}
     * for a list of properties this parameter supports.
     */
    constructor(value?: string | typeof Field.SKIP_SETUP, textClass?: string, config?: FieldLabelConfig);
    protected configure_(config: FieldLabelConfig): void;
    /**
     * Create block UI for this label.
     */
    initView(): void;
    /**
     * Ensure that the input value casts to a valid string.
     *
     * @param newValue The input value.
     * @returns A valid string, or null if invalid.
     */
    protected doClassValidation_(newValue?: any): string | null;
    /**
     * Set the CSS class applied to the field's textElement_.
     *
     * @param cssClass The new CSS class name, or null to remove.
     */
    setClass(cssClass: string | null): void;
    /**
     * Construct a FieldLabel from a JSON arg object,
     * dereferencing any string table references.
     *
     * @param options A JSON object with options (text, and class).
     * @returns The new field instance.
     * @nocollapse
     * @internal
     */
    static fromJson(options: FieldLabelFromJsonConfig): FieldLabel;
}
/**
 * Config options for the label field.
 */
export interface FieldLabelConfig extends FieldConfig {
    class?: string;
}
/**
 * fromJson config options for the label field.
 */
export interface FieldLabelFromJsonConfig extends FieldLabelConfig {
    text?: string;
}
//# sourceMappingURL=field_label.d.ts.map