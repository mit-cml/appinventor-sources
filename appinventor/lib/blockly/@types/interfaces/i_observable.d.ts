/**
 * @license
 * Copyright 2022 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * An object that fires events optionally.
 *
 * @internal
 */
export interface IObservable {
    startPublishing(): void;
    stopPublishing(): void;
}
/**
 * Type guard for checking if an object fulfills IObservable.
 *
 * @internal
 */
export declare function isObservable(obj: any): obj is IObservable;
//# sourceMappingURL=i_observable.d.ts.map