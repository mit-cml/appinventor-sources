/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Non-editable, serializable text field. Behaves like a
 *    normal label but is serialized to XML. It may only be
 *    edited programmatically.
 *
 * @class
 */
import { FieldLabel, FieldLabelConfig, FieldLabelFromJsonConfig } from './field_label.js';
/**
 * Class for a non-editable, serializable text field.
 */
export declare class FieldLabelSerializable extends FieldLabel {
    /**
     * Editable fields usually show some sort of UI indicating they are
     * editable. This field should not.
     */
    EDITABLE: boolean;
    /**
     * Serializable fields are saved by the XML renderer, non-serializable
     * fields are not.  This field should be serialized, but only edited
     * programmatically.
     */
    SERIALIZABLE: boolean;
    /**
     * @param value The initial value of the field. Should cast to a string.
     *     Defaults to an empty string if null or undefined.
     * @param textClass Optional CSS class for the field's text.
     * @param config A map of options used to configure the field.
     *    See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/label-serializable#creation}
     * for a list of properties this parameter supports.
     */
    constructor(value?: string, textClass?: string, config?: FieldLabelConfig);
    /**
     * Construct a FieldLabelSerializable from a JSON arg object,
     * dereferencing any string table references.
     *
     * @param options A JSON object with options (text, and class).
     * @returns The new field instance.
     * @nocollapse
     * @internal
     */
    static fromJson(options: FieldLabelFromJsonConfig): FieldLabelSerializable;
}
//# sourceMappingURL=field_label_serializable.d.ts.map