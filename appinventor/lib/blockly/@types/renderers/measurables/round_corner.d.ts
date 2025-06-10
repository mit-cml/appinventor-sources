/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { ConstantProvider } from '../common/constants.js';
import { Measurable } from './base.js';
/**
 * An object containing information about the space a rounded corner takes up
 * during rendering.
 */
export declare class RoundCorner extends Measurable {
    /**
     * @param constants The rendering constants provider.
     * @param opt_position The position of this corner.
     */
    constructor(constants: ConstantProvider, opt_position?: string);
}
//# sourceMappingURL=round_corner.d.ts.map