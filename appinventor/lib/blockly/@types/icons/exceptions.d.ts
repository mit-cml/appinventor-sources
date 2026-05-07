/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IIcon } from '../interfaces/i_icon.js';
/**
 * Thrown when you add more than one icon of the same type to a block.
 */
export declare class DuplicateIconType extends Error {
    icon: IIcon;
    /**
     * @internal
     */
    constructor(icon: IIcon);
}
//# sourceMappingURL=exceptions.d.ts.map