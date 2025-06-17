/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from '../block_svg.js';
import { ICopyData } from '../interfaces/i_copyable.js';
import { IPaster } from '../interfaces/i_paster.js';
import { State } from '../serialization/blocks.js';
import { Coordinate } from '../utils/coordinate.js';
import { WorkspaceSvg } from '../workspace_svg.js';
export declare class BlockPaster implements IPaster<BlockCopyData, BlockSvg> {
    static TYPE: string;
    paste(copyData: BlockCopyData, workspace: WorkspaceSvg, coordinate?: Coordinate): BlockSvg | null;
}
/**
 * Moves the given block to a location where it does not: (1) overlap exactly
 * with any other blocks, or (2) look like it is connected to any other blocks.
 *
 * Exported for testing.
 *
 * @param block The block to move to an unambiguous location.
 * @internal
 */
export declare function moveBlockToNotConflict(block: BlockSvg): void;
export interface BlockCopyData extends ICopyData {
    blockState: State;
    typeCounts: {
        [key: string]: number;
    };
}
//# sourceMappingURL=block_paster.d.ts.map