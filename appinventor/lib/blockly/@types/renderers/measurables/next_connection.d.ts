/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { RenderedConnection } from '../../rendered_connection.js';
import type { ConstantProvider } from '../common/constants.js';
import { Connection } from './connection.js';
/**
 * An object containing information about the space a next connection takes
 * up during rendering.
 */
export declare class NextConnection extends Connection {
    private nextConnection;
    /**
     * @param constants The rendering constants provider.
     * @param connectionModel The connection object on the block that this
     *     represents.
     */
    constructor(constants: ConstantProvider, connectionModel: RenderedConnection);
}
//# sourceMappingURL=next_connection.d.ts.map