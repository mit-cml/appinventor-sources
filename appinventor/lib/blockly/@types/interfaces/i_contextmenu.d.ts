/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
export interface IContextMenu {
    /**
     * Show the context menu for this object.
     *
     * @param e Mouse event.
     */
    showContextMenu(e: Event): void;
}
/** @returns true if the given object implements IContextMenu. */
export declare function hasContextMenu(obj: any): obj is IContextMenu;
//# sourceMappingURL=i_contextmenu.d.ts.map