/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IIcon as BlocklyIcon } from '../../interfaces/i_icon.js';
import type { ConstantProvider } from '../common/constants.js';
import { Measurable } from './base.js';
/**
 * An object containing information about the space an icon takes up during
 * rendering.
 */
export declare class Icon extends Measurable {
    icon: BlocklyIcon;
    /**
     * @deprecated Will be removed in v11. Create a subclass of the Icon
     *     measurable if this data is necessary for you.
     */
    isVisible: boolean;
    flipRtl: boolean;
    /**
     * An object containing information about the space an icon takes up during
     * rendering.
     *
     * @param constants The rendering constants provider.
     * @param icon The icon to measure and store information for.
     */
    constructor(constants: ConstantProvider, icon: BlocklyIcon);
}
//# sourceMappingURL=icon.d.ts.map