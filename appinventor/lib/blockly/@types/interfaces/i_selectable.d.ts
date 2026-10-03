/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Workspace } from '../workspace.js';
import { IFocusableNode } from './i_focusable_node.js';
/**
 * The interface for an object that is selectable.
 *
 * Implementations are generally expected to use their implementations of
 * onNodeFocus() and onNodeBlur() to call setSelected() with themselves and
 * null, respectively, in order to ensure that selections are correctly updated
 * and the selection change event is fired.
 */
export interface ISelectable extends IFocusableNode {
    id: string;
    workspace: Workspace;
    /** Select this.  Highlight it visually. */
    select(): void;
    /** Unselect this.  Unhighlight it visually. */
    unselect(): void;
}
/** Checks whether the given object is an ISelectable. */
export declare function isSelectable(obj: any): obj is ISelectable;
//# sourceMappingURL=i_selectable.d.ts.map