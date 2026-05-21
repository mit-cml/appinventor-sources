/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Block } from '../block.js';
import type { IIcon } from '../interfaces/i_icon.js';
import * as tooltip from '../tooltip.js';
import { Coordinate } from '../utils/coordinate.js';
import { Size } from '../utils/size.js';
import type { IconType } from './icon_types.js';
/**
 * The abstract icon class. Icons are visual elements that live in the top-start
 * corner of the block. Usually they provide more "meta" information about a
 * block (such as warnings or comments) as opposed to fields, which provide
 * "actual" information, related to how a block functions.
 */
export declare abstract class Icon implements IIcon {
    protected sourceBlock: Block;
    /**
     * The position of this icon relative to its blocks top-start,
     * in workspace units.
     */
    protected offsetInBlock: Coordinate;
    /** The position of this icon in workspace coordinates. */
    protected workspaceLocation: Coordinate;
    /** The root svg element visually representing this icon. */
    protected svgRoot: SVGGElement | null;
    /** The tooltip for this icon. */
    protected tooltip: tooltip.TipInfo;
    constructor(sourceBlock: Block);
    getType(): IconType<IIcon>;
    initView(pointerdownListener: (e: PointerEvent) => void): void;
    dispose(): void;
    getWeight(): number;
    getSize(): Size;
    /**
     * Sets the tooltip for this icon to the given value. Null to show the
     * tooltip of the block.
     */
    setTooltip(tip: tooltip.TipInfo | null): void;
    /** Returns the tooltip for this icon. */
    getTooltip(): tooltip.TipInfo;
    applyColour(): void;
    updateEditable(): void;
    updateCollapsed(): void;
    hideForInsertionMarker(): void;
    isShownWhenCollapsed(): boolean;
    setOffsetInBlock(offset: Coordinate): void;
    private updateSvgRootOffset;
    onLocationChange(blockOrigin: Coordinate): void;
    onClick(): void;
    /**
     * Check whether the icon should be clickable while the block is in a flyout.
     * The default is that icons are clickable in all flyouts (auto-closing or not).
     * Subclasses may override this function to change this behavior.
     *
     * @param autoClosingFlyout true if the containing flyout is an auto-closing one.
     * @returns Whether the icon should be clickable while the block is in a flyout.
     */
    isClickableInFlyout(autoClosingFlyout: boolean): boolean;
}
//# sourceMappingURL=icon.d.ts.map