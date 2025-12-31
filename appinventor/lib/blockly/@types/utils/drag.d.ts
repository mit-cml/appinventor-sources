/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceSvg } from '../workspace_svg.js';
import { Coordinate } from './coordinate.js';
/**
 * Start tracking a drag of an object on this workspace by recording the offset
 * between the pointer's current location and the object's starting location.
 *
 * Used for resizing block comments and workspace comments.
 *
 * @param workspace The workspace where the drag is occurring.
 * @param e Pointer down event.
 * @param xy Starting location of object.
 */
export declare function start(workspace: WorkspaceSvg, e: PointerEvent, xy: Coordinate): void;
/**
 * Compute the new position of a dragged object in this workspace based on the
 * current pointer position and the offset between the pointer's starting
 * location and the object's starting location.
 *
 * The start function should have be called previously, when the drag started.
 *
 * Used for resizing block comments and workspace comments.
 *
 * @param workspace The workspace where the drag is occurring.
 * @param e Pointer move event.
 * @returns New location of object.
 */
export declare function move(workspace: WorkspaceSvg, e: PointerEvent): Coordinate;
//# sourceMappingURL=drag.d.ts.map