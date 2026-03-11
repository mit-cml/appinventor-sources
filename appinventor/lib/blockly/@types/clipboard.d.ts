/**
 * @license
 * Copyright 2021 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockCopyData, BlockPaster } from './clipboard/block_paster.js';
import * as registry from './clipboard/registry.js';
import type { ICopyData, ICopyable } from './interfaces/i_copyable.js';
import { Coordinate } from './utils/coordinate.js';
import { WorkspaceSvg } from './workspace_svg.js';
/**
 * Copy a copyable item, and record its data and the workspace it was
 * copied from.
 *
 * This function does not perform any checks to ensure the copy
 * should be allowed, e.g. to ensure the block is deletable. Such
 * checks should be done before calling this function.
 *
 * Note that if the copyable item is not an `ISelectable` or its
 * `workspace` property is not a `WorkspaceSvg`, the copy will be
 * successful, but there will be no saved workspace data. This will
 * impact the ability to paste the data unless you explictily pass
 * a workspace into the paste method.
 *
 * @param toCopy item to copy.
 * @param location location to save as a potential paste location.
 * @returns the copied data if copy was successful, otherwise null.
 */
export declare function copy<T extends ICopyData>(toCopy: ICopyable<T>, location?: Coordinate): T | null;
/**
 * Gets the copy data for the last item copied. This is useful if you
 * are implementing custom copy/paste behavior. If you want the default
 * behavior, just use the copy and paste methods directly.
 *
 * @returns copy data for the last item copied, or null if none set.
 */
export declare function getLastCopiedData(): ICopyable.ICopyData | null;
/**
 * Sets the last copied item. You should call this method if you implement
 * custom copy behavior, so that other callers are working with the correct
 * data. This method is called automatically if you use the built-in copy
 * method.
 *
 * @param copyData copy data for the last item copied.
 */
export declare function setLastCopiedData(copyData: ICopyData): void;
/**
 * Gets the workspace that was last copied from. This is useful if you
 * are implementing custom copy/paste behavior and want to paste on the
 * same workspace that was copied from. If you want the default behavior,
 * just use the copy and paste methods directly.
 *
 * @returns workspace that was last copied from, or null if none set.
 */
export declare function getLastCopiedWorkspace(): WorkspaceSvg | null;
/**
 * Sets the workspace that was last copied from. You should call this method
 * if you implement custom copy behavior, so that other callers are working
 * with the correct data. This method is called automatically if you use the
 * built-in copy method.
 *
 * @param workspace workspace that was last copied from.
 */
export declare function setLastCopiedWorkspace(workspace: WorkspaceSvg): void;
/**
 * Gets the location that was last copied from. This is useful if you
 * are implementing custom copy/paste behavior. If you want the
 * default behavior, just use the copy and paste methods directly.
 *
 * @returns last saved location, or null if none set.
 */
export declare function getLastCopiedLocation(): Coordinate | undefined;
/**
 * Sets the location that was last copied from. You should call this method
 * if you implement custom copy behavior, so that other callers are working
 * with the correct data. This method is called automatically if you use the
 * built-in copy method.
 *
 * @param location last saved location, which can be used to paste at.
 */
export declare function setLastCopiedLocation(location: Coordinate): void;
/**
 * Paste a pasteable element into the given workspace.
 *
 * This function does not perform any checks to ensure the paste
 * is allowed, e.g. that the workspace is rendered or the block
 * is pasteable. Such checks should be done before calling this
 * function.
 *
 * @param copyData The data to paste into the workspace.
 * @param workspace The workspace to paste the data into.
 * @param coordinate The location to paste the thing at.
 * @returns The pasted thing if the paste was successful, null otherwise.
 */
export declare function paste<T extends ICopyData>(copyData: T, workspace: WorkspaceSvg, coordinate?: Coordinate): ICopyable<T> | null;
/**
 * Pastes the last copied ICopyable into the last copied-from workspace.
 *
 * @returns the pasted thing if the paste was successful, null otherwise.
 */
export declare function paste(): ICopyable<ICopyData> | null;
export { BlockCopyData, BlockPaster, registry };
//# sourceMappingURL=clipboard.d.ts.map