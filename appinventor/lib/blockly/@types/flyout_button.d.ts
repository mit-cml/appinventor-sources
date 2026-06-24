/**
 * @license
 * Copyright 2016 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IBoundedElement } from './interfaces/i_bounded_element.js';
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { IFocusableTree } from './interfaces/i_focusable_tree.js';
import type { IRenderedElement } from './interfaces/i_rendered_element.js';
import { Coordinate } from './utils/coordinate.js';
import { Rect } from './utils/rect.js';
import type * as toolbox from './utils/toolbox.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for a button or label in the flyout.
 */
export declare class FlyoutButton implements IBoundedElement, IRenderedElement, IFocusableNode {
    private readonly workspace;
    private readonly targetWorkspace;
    private readonly isFlyoutLabel;
    /** The horizontal margin around the text in the button. */
    static TEXT_MARGIN_X: number;
    /** The vertical margin around the text in the button. */
    static TEXT_MARGIN_Y: number;
    /** The radius of the flyout button's borders. */
    static BORDER_RADIUS: number;
    /** The key to the function called when this button is activated. */
    readonly callbackKey: string;
    private readonly text;
    private readonly position;
    private readonly cssClass;
    /** Mouse up event data. */
    private onMouseDownWrapper;
    private onMouseUpWrapper;
    info: toolbox.ButtonOrLabelInfo;
    /** The width of the button's rect. */
    width: number;
    /** The height of the button's rect. */
    height: number;
    /** The root SVG group for the button or label. */
    private svgGroup;
    /** The SVG element with the text of the label or button. */
    private svgText;
    /**
     * Holds the cursors svg element when the cursor is attached to the button.
     * This is null if there is no cursor on the button.
     */
    cursorSvg: SVGElement | null;
    /** The unique ID for this FlyoutButton. */
    private id;
    /**
     * @param workspace The workspace in which to place this button.
     * @param targetWorkspace The flyout's target workspace.
     * @param json The JSON specifying the label/button.
     * @param isFlyoutLabel Whether this button should be styled as a label.
     * @internal
     */
    constructor(workspace: WorkspaceSvg, targetWorkspace: WorkspaceSvg, json: toolbox.ButtonOrLabelInfo, isFlyoutLabel: boolean);
    createDom(): SVGElement;
    /** Correctly position the flyout button and make it visible. */
    show(): void;
    /** Update SVG attributes to match internal state. */
    private updateTransform;
    /**
     * Move the button to the given x, y coordinates.
     *
     * @param x The new x coordinate.
     * @param y The new y coordinate.
     */
    moveTo(x: number, y: number): void;
    /**
     * Move the element by a relative offset.
     *
     * @param dx Horizontal offset in workspace units.
     * @param dy Vertical offset in workspace units.
     * @param _reason Why is this move happening?  'user', 'bump', 'snap'...
     */
    moveBy(dx: number, dy: number, _reason?: string[]): void;
    /** @returns Whether or not the button is a label. */
    isLabel(): boolean;
    /**
     * Location of the button.
     *
     * @returns x, y coordinates.
     * @internal
     */
    getPosition(): Coordinate;
    /**
     * Returns the coordinates of a bounded element describing the dimensions of
     * the element. Coordinate system: workspace coordinates.
     *
     * @returns Object with coordinates of the bounded element.
     */
    getBoundingRectangle(): Rect;
    /** @returns Text of the button. */
    getButtonText(): string;
    /**
     * Get the button's target workspace.
     *
     * @returns The target workspace of the flyout where this button resides.
     */
    getTargetWorkspace(): WorkspaceSvg;
    /**
     * Get the button's workspace.
     *
     * @returns The workspace in which to place this button.
     */
    getWorkspace(): WorkspaceSvg;
    /** Dispose of this button. */
    dispose(): void;
    /**
     * Add the cursor SVG to this buttons's SVG group.
     *
     * @param cursorSvg The SVG root of the cursor to be added to the button SVG
     *     group.
     */
    setCursorSvg(cursorSvg: SVGElement): void;
    /**
     * Do something when the button is clicked.
     *
     * @param e Pointer up event.
     */
    private onMouseUp;
    private onMouseDown;
    /**
     * @returns The root SVG element of this rendered element.
     */
    getSvgRoot(): SVGGElement;
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
//# sourceMappingURL=flyout_button.d.ts.map