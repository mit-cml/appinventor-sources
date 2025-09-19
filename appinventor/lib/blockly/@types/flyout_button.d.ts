/**
 * @license
 * Copyright 2016 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Class for a button in the flyout.
 *
 * @class
 */
import type { IASTNodeLocationSvg } from './blockly.js';
import { Coordinate } from './utils/coordinate.js';
import type * as toolbox from './utils/toolbox.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for a button or label in the flyout.
 */
export declare class FlyoutButton implements IASTNodeLocationSvg {
    private readonly workspace;
    private readonly targetWorkspace;
    private readonly isFlyoutLabel;
    /** The horizontal margin around the text in the button. */
    static TEXT_MARGIN_X: number;
    /** The vertical margin around the text in the button. */
    static TEXT_MARGIN_Y: number;
    /** The radius of the flyout button's borders. */
    static BORDER_RADIUS: number;
    private readonly text;
    private readonly position;
    private readonly callbackKey;
    private readonly cssClass;
    /** Mouse up event data. */
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
    /**
     * @param workspace The workspace in which to place this button.
     * @param targetWorkspace The flyout's target workspace.
     * @param json The JSON specifying the label/button.
     * @param isFlyoutLabel Whether this button should be styled as a label.
     * @internal
     */
    constructor(workspace: WorkspaceSvg, targetWorkspace: WorkspaceSvg, json: toolbox.ButtonOrLabelInfo, isFlyoutLabel: boolean);
    /**
     * Create the button elements.
     *
     * @returns The button's SVG group.
     */
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
    /** @returns Whether or not the button is a label. */
    isLabel(): boolean;
    /**
     * Location of the button.
     *
     * @returns x, y coordinates.
     * @internal
     */
    getPosition(): Coordinate;
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
     * Required by IASTNodeLocationSvg, but not used. A marker cannot be set on a
     * button. If the 'mark' shortcut is used on a button, its associated callback
     * function is triggered.
     */
    setMarkerSvg(): void;
    /**
     * Do something when the button is clicked.
     *
     * @param e Pointer up event.
     */
    private onMouseUp;
}
//# sourceMappingURL=flyout_button.d.ts.map