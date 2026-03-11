/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IBoundedElement } from './interfaces/i_bounded_element.js';
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { IFocusableTree } from './interfaces/i_focusable_tree.js';
import { Rect } from './utils/rect.js';
/**
 * Representation of a gap between elements in a flyout.
 */
export declare class FlyoutSeparator implements IBoundedElement, IFocusableNode {
    private gap;
    private axis;
    private x;
    private y;
    /**
     * Creates a new separator.
     *
     * @param gap The amount of space this separator should occupy.
     * @param axis The axis along which this separator occupies space.
     */
    constructor(gap: number, axis: SeparatorAxis);
    /**
     * Returns the bounding box of this separator.
     *
     * @returns The bounding box of this separator.
     */
    getBoundingRectangle(): Rect;
    /**
     * Repositions this separator.
     *
     * @param dx The distance to move this separator on the X axis.
     * @param dy The distance to move this separator on the Y axis.
     * @param _reason The reason this move was initiated.
     */
    moveBy(dx: number, dy: number, _reason?: string[]): void;
    /**
     * Returns false to prevent this separator from being navigated to by the
     * keyboard.
     *
     * @returns False.
     */
    isNavigable(): boolean;
    /** See IFocusableNode.getFocusableElement. */
    getFocusableElement(): HTMLElement | SVGElement;
    /** See IFocusableNode.getFocusableTree. */
    getFocusableTree(): IFocusableTree;
    /** See IFocusableNode.onNodeFocus. */
    onNodeFocus(): void;
    /** See IFocusableNode.onNodeBlur. */
    onNodeBlur(): void;
    /** See IFocusableNode.canBeFocused. */
    canBeFocused(): boolean;
}
/**
 * Representation of an axis along which a separator occupies space.
 */
export declare const enum SeparatorAxis {
    X = "x",
    Y = "y"
}
//# sourceMappingURL=flyout_separator.d.ts.map