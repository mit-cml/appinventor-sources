/**
 * @license
 * Copyright 2013 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Rect } from './utils/rect.js';
import type { Size } from './utils/size.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Returns the HTML container for editor widgets.
 *
 * @returns The editor widget container.
 */
export declare function getDiv(): HTMLDivElement | null;
/**
 * Allows unit tests to reset the div. Do not use outside of tests.
 *
 * @param newDiv The new value for the DIV field.
 * @internal
 */
export declare function testOnly_setDiv(newDiv: HTMLDivElement | null): void;
/**
 * Create the widget div and inject it onto the page.
 */
export declare function createDom(): void;
/**
 * Initialize and display the widget div.  Close the old one if needed.
 *
 * @param newOwner The object that will be using this container.
 * @param rtl Right-to-left (true) or left-to-right (false).
 * @param newDispose Optional cleanup function to be run when the widget is
 *     closed.
 * @param workspace The workspace associated with the widget owner.
 */
export declare function show(newOwner: unknown, rtl: boolean, newDispose: () => void, workspace?: WorkspaceSvg | null): void;
/**
 * Destroy the widget and hide the div.
 */
export declare function hide(): void;
/**
 * Is the container visible?
 *
 * @returns True if visible.
 */
export declare function isVisible(): boolean;
/**
 * Destroy the widget and hide the div if it is being used by the specified
 * object.
 *
 * @param oldOwner The object that was using this container.
 */
export declare function hideIfOwner(oldOwner: unknown): void;
/**
 * Destroy the widget and hide the div if it is being used by an object in the
 * specified workspace, or if it is used by an unknown workspace.
 *
 * @param oldOwnerWorkspace The workspace that was using this container.
 */
export declare function hideIfOwnerIsInWorkspace(oldOwnerWorkspace: WorkspaceSvg): void;
/**
 * Position the widget div based on an anchor rectangle.
 * The widget should be placed adjacent to but not overlapping the anchor
 * rectangle.  The preferred position is directly below and aligned to the left
 * (LTR) or right (RTL) side of the anchor.
 *
 * @param viewportBBox The bounding rectangle of the current viewport, in window
 *     coordinates.
 * @param anchorBBox The bounding rectangle of the anchor, in window
 *     coordinates.
 * @param widgetSize The size of the widget that is inside the widget div, in
 *     window coordinates.
 * @param rtl Whether the workspace is in RTL mode.  This determines horizontal
 *     alignment.
 * @internal
 */
export declare function positionWithAnchor(viewportBBox: Rect, anchorBBox: Rect, widgetSize: Size, rtl: boolean): void;
/**
 * Reposition the widget div if the owner of it says to.
 * If the owner isn't a field, just give up and hide it.
 */
export declare function repositionForWindowResize(): void;
//# sourceMappingURL=widgetdiv.d.ts.map