/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Workspace } from '../workspace.js';
/**
 * The interface for an object that is selectable.
 */
export interface ISelectable {
    id: string;
    workspace: Workspace;
    /** Select this.  Highlight it visually. */
    select(): void;
    /** Unselect this.  Unhighlight it visually. */
    unselect(): void;
}
/** Checks whether the given object is an ISelectable. */
export declare function isSelectable(obj: object): obj is ISelectable;
//# sourceMappingURL=i_selectable.d.ts.map