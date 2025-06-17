/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { ISelectable } from './i_selectable.js';
export interface ICopyable<T extends ICopyData> extends ISelectable {
    /**
     * Encode for copying.
     *
     * @returns Copy metadata.
     */
    toCopyData(): T | null;
}
export declare namespace ICopyable {
    interface ICopyData {
        paster: string;
    }
}
export type ICopyData = ICopyable.ICopyData;
/** @returns true if the given object is copyable. */
export declare function isCopyable(obj: any): obj is ICopyable<ICopyData>;
//# sourceMappingURL=i_copyable.d.ts.map