/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { ICopyable, ICopyData } from '../interfaces/i_copyable.js';
import type { IPaster } from '../interfaces/i_paster.js';
/**
 * Registers the given paster so that it cna be used for pasting.
 *
 * @param type The type of the paster to register, e.g. 'block', 'comment', etc.
 * @param paster The paster to register.
 */
export declare function register<U extends ICopyData, T extends ICopyable<U>>(type: string, paster: IPaster<U, T>): void;
/**
 * Unregisters the paster associated with the given type.
 *
 * @param type The type of the paster to unregister.
 */
export declare function unregister(type: string): void;
//# sourceMappingURL=registry.d.ts.map