/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { CommentState } from '../icons/comment_icon.js';
import type { Coordinate } from '../utils/coordinate.js';
import type { Size } from '../utils/size.js';
import type { IHasBubble } from './i_has_bubble.js';
import type { IIcon } from './i_icon.js';
import type { ISerializable } from './i_serializable.js';
export interface ICommentIcon extends IIcon, IHasBubble, ISerializable {
    setText(text: string): void;
    getText(): string;
    setBubbleSize(size: Size): void;
    getBubbleSize(): Size;
    setBubbleLocation(location: Coordinate): void;
    getBubbleLocation(): Coordinate | undefined;
    saveState(): CommentState;
    loadState(state: CommentState): void;
}
/** Checks whether the given object is an ICommentIcon. */
export declare function isCommentIcon(obj: any): obj is ICommentIcon;
//# sourceMappingURL=i_comment_icon.d.ts.map