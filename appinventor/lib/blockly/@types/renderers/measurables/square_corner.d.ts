/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { ConstantProvider } from '../common/constants.js';
import { Measurable } from './base.js';
/**
 * An object containing information about the space a square corner takes up
 * during rendering.
 */
export declare class SquareCorner extends Measurable {
    private squareCorner;
    /**
     * @param constants The rendering constants provider.
     * @param opt_position The position of this corner.
     */
    constructor(constants: ConstantProvider, opt_position?: string);
}
//# sourceMappingURL=square_corner.d.ts.map