/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Input } from '../../../inputs/input.js';
import type { ConstantProvider as BaseConstantProvider } from '../../../renderers/common/constants.js';
import { StatementInput as BaseStatementInput } from '../../../renderers/measurables/statement_input.js';
import type { ConstantProvider as GerasConstantProvider } from '../constants.js';
/**
 * An object containing information about the space a statement input takes up
 * during rendering.
 */
export declare class StatementInput extends BaseStatementInput {
    constants_: GerasConstantProvider;
    /**
     * @param constants The rendering constants provider.
     * @param input The statement input to measure and store information for.
     */
    constructor(constants: BaseConstantProvider, input: Input);
}
//# sourceMappingURL=statement_input.d.ts.map