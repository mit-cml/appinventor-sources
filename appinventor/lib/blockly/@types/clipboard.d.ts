/**
 * @license
 * Copyright 2021 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockPaster } from './clipboard/block_paster.js';
import * as registry from './clipboard/registry.js';
import type { ICopyData, ICopyable } from './interfaces/i_copyable.js';
import { Coordinate } from './utils/coordinate.js';
import { WorkspaceSvg } from './workspace_svg.js';
/**
 * Private version of copy for stubbing in tests.
 */
declare function copyInternal<T extends ICopyData>(toCopy: ICopyable<T>): T | null;
/**
 * Paste a pasteable element into the workspace.
 *
 * @param copyData The data to paste into the workspace.
 * @param workspace The workspace to paste the data into.
 * @param coordinate The location to paste the thing at.
 * @returns The pasted thing if the paste was successful, null otherwise.
 */
export declare function paste<T extends ICopyData>(copyData: T, workspace: WorkspaceSvg, coordinate?: Coordinate): ICopyable<T> | null;
/**
 * Pastes the last copied ICopyable into the workspace.
 *
 * @returns the pasted thing if the paste was successful, null otherwise.
 */
export declare function paste(): ICopyable<ICopyData> | null;
/**
 * Private version of duplicate for stubbing in tests.
 */
declare function duplicateInternal<U extends ICopyData, T extends ICopyable<U> & IHasWorkspace>(toDuplicate: T): T | null;
interface IHasWorkspace {
    workspace: WorkspaceSvg;
}
export declare const TEST_ONLY: {
    duplicateInternal: typeof duplicateInternal;
    copyInternal: typeof copyInternal;
};
export { BlockPaster, registry };
//# sourceMappingURL=clipboard.d.ts.map