/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from '../../block_svg.js';
import { RenderInfo as BaseRenderInfo } from '../common/info.js';
import type { Renderer } from './renderer.js';
/**
 * An object containing all sizing information needed to draw this block.
 *
 * This measure pass does not propagate changes to the block (although fields
 * may choose to rerender when getSize() is called).  However, calling it
 * repeatedly may be expensive.
 *
 * @deprecated Use Blockly.blockRendering.RenderInfo instead. To be removed
 *     in v11.
 */
export declare class RenderInfo extends BaseRenderInfo {
    protected renderer_: Renderer;
    /**
     * @param renderer The renderer in use.
     * @param block The block to measure.
     * @deprecated Use Blockly.blockRendering.RenderInfo instead. To be removed
     *     in v11.
     */
    constructor(renderer: Renderer, block: BlockSvg);
    /**
     * Get the block renderer in use.
     *
     * @returns The block renderer in use.
     */
    getRenderer(): Renderer;
}
//# sourceMappingURL=info.d.ts.map