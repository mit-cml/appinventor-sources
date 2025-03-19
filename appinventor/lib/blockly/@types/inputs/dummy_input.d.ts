/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from '../block.js';
import { Input } from './input.js';
import { inputTypes } from './input_types.js';
/** Represents an input on a block with no connection. */
export declare class DummyInput extends Input {
    name: string;
    readonly type = inputTypes.DUMMY;
    /**
     * @param name Language-neutral identifier which may used to find this input
     *     again.
     * @param block The block containing this input.
     */
    constructor(name: string, block: Block);
}
//# sourceMappingURL=dummy_input.d.ts.map