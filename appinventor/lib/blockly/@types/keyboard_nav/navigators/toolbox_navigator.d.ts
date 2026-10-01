/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from '../../interfaces/i_focusable_node.js';
import type { IToolbox } from '../../interfaces/i_toolbox.js';
import { Position } from '../../utils/toolbox.js';
import type { WorkspaceSvg } from '../../workspace_svg.js';
import { Navigator } from './navigator.js';
/**
 * Navigator that handles keyboard navigation within a toolbox.
 */
export declare class ToolboxNavigator extends Navigator {
    protected toolbox: IToolbox;
    constructor(toolbox: IToolbox);
    /**
     * Returns the flyout's first item (if any) or next toolbox item when
     * navigating in (right arrow) from a toolbox.
     *
     * @param node The toolbox item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     toolbox's layout; false (default) to take it into account.
     */
    getInNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns the flyout's first item (if any) or previous toolbox item when
     * navigating out (left arrow) from a toolbox.
     *
     * @param node The toolbox item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     toolbox's layout; false (default) to take it into account.
     */
    getOutNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns the flyout's first item (if any) or next toolbox item when
     * navigating next (down arrow) from a toolbox.
     *
     * @param node The toolbox item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     toolbox's layout; false (default) to take it into account.
     */
    getNextNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns the flyout's first item (if any) or previous toolbox item when
     * navigating previous (up arrow) from a toolbox.
     *
     * @param node The toolbox item to navigate relative to.
     * @param bypassAdjustments True to skip adjusting navigation based on the
     *     toolbox's layout; false (default) to take it into account.
     */
    getPreviousNode(node?: IFocusableNode | null, bypassAdjustments?: boolean): IFocusableNode | null;
    /**
     * Returns a list of all toolbox items.
     */
    protected getTopLevelItems(): IFocusableNode[];
}
/**
 * Although developers specify the toolbox position as "start" or "end", this
 * gets normalized by the injection options parser based on RTL, such that "end"
 * in RTL means the left. When dealing with arrow keys, we want the actual/
 * physical position on screen, not the logical position. This function converts
 * the stored logical position to the physical position.
 *
 * @internal
 * @param workspace The workspace to use injection options from.
 * @returns The physical location of the toolbox/flyout on screen.
 */
export declare function getPhysicalToolboxPosition(workspace: WorkspaceSvg): Position;
//# sourceMappingURL=toolbox_navigator.d.ts.map