/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { ConstantProvider } from '../common/constants.js';
import { Measurable } from './base.js';
/**
 * An object containing information about a spacer between two elements on a
 * row.
 */
export declare class InRowSpacer extends Measurable {
    private inRowSpacer;
    /**
     * @param constants The rendering constants provider.
     * @param width The width of the spacer.
     */
    constructor(constants: ConstantProvider, width: number);
}
//# sourceMappingURL=in_row_spacer.d.ts.map