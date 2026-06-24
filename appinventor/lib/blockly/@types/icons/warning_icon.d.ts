/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from '../block_svg.js';
import type { IBubble } from '../interfaces/i_bubble.js';
import type { IHasBubble } from '../interfaces/i_has_bubble.js';
import { Size } from '../utils.js';
import { Coordinate } from '../utils/coordinate.js';
import { Icon } from './icon.js';
import { IconType } from './icon_types.js';
/**
 * An icon that warns the user that something is wrong with their block.
 *
 * For example, this could be used to warn them about incorrect field values,
 * or incorrect placement of the block (putting it somewhere it doesn't belong).
 */
export declare class WarningIcon extends Icon implements IHasBubble {
    protected readonly sourceBlock: BlockSvg;
    /** The type string used to identify this icon. */
    static readonly TYPE: IconType<WarningIcon>;
    /**
     * The weight this icon has relative to other icons. Icons with more positive
     * weight values are rendered farther toward the end of the block.
     */
    static readonly WEIGHT = 2;
    /** A map of warning IDs to warning text. */
    private textMap;
    /** The bubble used to display the warnings to the user. */
    private textBubble;
    /** @internal */
    constructor(sourceBlock: BlockSvg);
    getType(): IconType<WarningIcon>;
    initView(pointerdownListener: (e: PointerEvent) => void): void;
    dispose(): void;
    getWeight(): number;
    getSize(): Size;
    applyColour(): void;
    updateCollapsed(): void;
    /** Tells blockly that this icon is shown when the block is collapsed. */
    isShownWhenCollapsed(): boolean;
    /** Updates the location of the icon's bubble if it is open. */
    onLocationChange(blockOrigin: Coordinate): void;
    /**
     * Adds a warning message to this warning icon.
     *
     * @param text The text of the message to add.
     * @param id The id of the message to add.
     * @internal
     */
    addMessage(text: string, id: string): this;
    /**
     * @returns the display text for this icon. Includes all warning messages
     *     concatenated together with newlines.
     * @internal
     */
    getText(): string;
    /** Toggles the visibility of the bubble. */
    onClick(): void;
    isClickableInFlyout(): boolean;
    bubbleIsVisible(): boolean;
    setBubbleVisible(visible: boolean): Promise<void>;
    /** See IHasBubble.getBubble. */
    getBubble(): IBubble | null;
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
//# sourceMappingURL=warning_icon.d.ts.map