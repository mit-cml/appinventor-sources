/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * The interface for an object that can be deleted.
 */
export interface IDeletable {
    /**
     * Get whether this object is deletable or not.
     *
     * @returns True if deletable.
     */
    isDeletable(): boolean;
    /** Disposes of this object, cleaning up any references or DOM elements. */
    dispose(): void;
    /** Visually indicates that the object is pending deletion. */
    setDeleteStyle(wouldDelete: boolean): void;
}
/** Returns whether the given object is an IDeletable. */
export declare function isDeletable(obj: any): obj is IDeletable;
//# sourceMappingURL=i_deletable.d.ts.map