/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from '../block.js';
import type { Connection } from '../connection.js';
import { Input } from './input.js';
import { inputTypes } from './input_types.js';
/** Represents an input on a block with a statement connection. */
export declare class StatementInput extends Input {
    name: string;
    readonly type = inputTypes.STATEMENT;
    connection: Connection;
    /**
     * @param name Language-neutral identifier which may used to find this input
     *     again.
     * @param block The block containing this input.
     */
    constructor(name: string, block: Block);
}
//# sourceMappingURL=statement_input.d.ts.map