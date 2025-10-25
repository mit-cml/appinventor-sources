/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { CommentState } from '../icons/comment_icon.js';
import { Size } from '../utils/size.js';
import { IHasBubble } from './i_has_bubble.js';
import { IIcon } from './i_icon.js';
import { ISerializable } from './i_serializable.js';
export interface ICommentIcon extends IIcon, IHasBubble, ISerializable {
    setText(text: string): void;
    getText(): string;
    setBubbleSize(size: Size): void;
    getBubbleSize(): Size;
    saveState(): CommentState;
    loadState(state: CommentState): void;
}
/** Checks whether the given object is an ICommentIcon. */
export declare function isCommentIcon(obj: object): obj is ICommentIcon;
//# sourceMappingURL=i_comment_icon.d.ts.map