/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { ICommentIcon } from '../interfaces/i_comment_icon.js';
import { IIcon } from '../interfaces/i_icon.js';
import { MutatorIcon } from './mutator_icon.js';
import { WarningIcon } from './warning_icon.js';
/**
 * Defines the type of an icon, so that it can be retrieved from block.getIcon
 */
export declare class IconType<_T extends IIcon> {
    private readonly name;
    /** @param name The name of the registry type. */
    constructor(name: string);
    /** @returns the name of the type. */
    toString(): string;
    /** @returns true if this icon type is equivalent to the given icon type. */
    equals(type: IconType<IIcon>): boolean;
    static MUTATOR: IconType<MutatorIcon>;
    static WARNING: IconType<WarningIcon>;
    static COMMENT: IconType<ICommentIcon>;
}
//# sourceMappingURL=icon_types.d.ts.map