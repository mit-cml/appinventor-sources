/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from '../block.js';
import { IconType } from './icon_types.js';
import { Coordinate } from '../utils.js';
import { Icon } from './icon.js';
import type { IHasBubble } from '../interfaces/i_has_bubble.js';
import type { ISerializable } from '../interfaces/i_serializable.js';
import { Size } from '../utils/size.js';
/**
 * An icon which allows the user to add comment text to a block.
 */
export declare class CommentIcon extends Icon implements IHasBubble, ISerializable {
    protected readonly sourceBlock: Block;
    /** The type string used to identify this icon. */
    static readonly TYPE: IconType<CommentIcon>;
    /**
     * The weight this icon has relative to other icons. Icons with more positive
     * weight values are rendered farther toward the end of the block.
     */
    static readonly WEIGHT = 3;
    /** The bubble used to show editable text to the user. */
    private textInputBubble;
    /** The bubble used to show non-editable text to the user. */
    private textBubble;
    /** The text of this comment. */
    private text;
    /** The size of this comment (which is applied to the editable bubble). */
    private bubbleSize;
    /**
     * The visibility of the bubble for this comment.
     *
     * This is used to track what the visibile state /should/ be, not necessarily
     * what it currently /is/. E.g. sometimes this will be true, but the block
     * hasn't been rendered yet, so the bubble will not currently be visible.
     */
    private bubbleVisiblity;
    constructor(sourceBlock: Block);
    getType(): IconType<CommentIcon>;
    initView(pointerdownListener: (e: PointerEvent) => void): void;
    dispose(): void;
    getWeight(): number;
    getSize(): Size;
    applyColour(): void;
    /**
     * Updates the state of the bubble (editable / noneditable) to reflect the
     * state of the bubble if the bubble is currently shown.
     */
    updateEditable(): void;
    onLocationChange(blockOrigin: Coordinate): void;
    /** Sets the text of this comment. Updates any bubbles if they are visible. */
    setText(text: string): void;
    /** Returns the text of this comment. */
    getText(): string;
    /**
     * Sets the size of the editable bubble for this comment. Resizes the
     * bubble if it is visible.
     */
    setBubbleSize(size: Size): void;
    /** @returns the size of the editable bubble for this comment. */
    getBubbleSize(): Size;
    /**
     * @returns the state of the comment as a JSON serializable value if the
     * comment has text. Otherwise returns null.
     */
    saveState(): CommentState | null;
    /** Applies the given state to this comment. */
    loadState(state: CommentState): void;
    onClick(): void;
    isClickableInFlyout(): boolean;
    /**
     * Updates the text of this comment in response to changes in the text of
     * the input bubble.
     */
    onTextChange(): void;
    /**
     * Updates the size of this icon in response to changes in the size of the
     * input bubble.
     */
    onSizeChange(): void;
    bubbleIsVisible(): boolean;
    setBubbleVisible(visible: boolean): void;
    /**
     * Shows the editable text bubble for this comment, and adds change listeners
     * to update the state of this icon in response to changes in the bubble.
     */
    private showEditableBubble;
    /** Shows the non editable text bubble for this comment. */
    private showNonEditableBubble;
    /** Hides any open bubbles owned by this comment. */
    private hideBubble;
    /**
     * @returns the location the bubble should be anchored to.
     *     I.E. the middle of this icon.
     */
    private getAnchorLocation;
    /**
     * @returns the rect the bubble should avoid overlapping.
     *     I.E. the block that owns this icon.
     */
    private getBubbleOwnerRect;
}
export interface CommentState {
    text?: string;
    pinned?: boolean;
    height?: number;
    width?: number;
}
//# sourceMappingURL=comment_icon.d.ts.map