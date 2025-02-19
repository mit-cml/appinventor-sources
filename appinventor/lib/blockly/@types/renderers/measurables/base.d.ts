/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { ConstantProvider } from '../common/constants.js';
/**
 * The base class to represent a part of a block that takes up space during
 * rendering.  The constructor for each non-spacer Measurable records the size
 * of the block element (e.g. field, statement input).
 */
export declare class Measurable {
    width: number;
    height: number;
    type: number;
    xPos: number;
    centerline: number;
    notchOffset: number;
    /** The renderer's constant provider. */
    protected readonly constants_: ConstantProvider;
    /**
     * @param constants The rendering constants provider.
     */
    constructor(constants: ConstantProvider);
}
//# sourceMappingURL=base.d.ts.map