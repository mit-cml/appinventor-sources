/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from '../../block_svg.js';
import { Renderer as BaseRenderer } from '../common/renderer.js';
import { RenderInfo } from './info.js';
/**
 * The thrasos renderer. This is a more modern take on the legacy geras
 * renderer.
 *
 * Thrasos is the ancient Greek spirit of boldness.
 */
export declare class Renderer extends BaseRenderer {
    /**
     * @param name The renderer name.
     */
    constructor(name: string);
    /**
     * Create a new instance of the renderer's render info object.
     *
     * @param block The block to measure.
     * @returns The render info object.
     */
    protected makeRenderInfo_(block: BlockSvg): RenderInfo;
}
//# sourceMappingURL=renderer.d.ts.map