/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Field as BlocklyField } from '../../field.js';
import type { Input } from '../../inputs/input.js';
import type { ConstantProvider } from '../common/constants.js';
import { Measurable } from './base.js';
/**
 * An object containing information about the space a field takes up during
 * rendering
 */
export declare class Field extends Measurable {
    field: BlocklyField;
    parentInput: Input;
    isEditable: boolean;
    flipRtl: boolean;
    height: number;
    width: number;
    /**
     * @param constants The rendering constants provider.
     * @param field The field to measure and store information for.
     * @param parentInput The parent input for the field.
     */
    constructor(constants: ConstantProvider, field: BlocklyField, parentInput: Input);
}
//# sourceMappingURL=field.d.ts.map