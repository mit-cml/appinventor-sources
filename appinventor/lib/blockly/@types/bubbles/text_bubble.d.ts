/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { Coordinate } from '../utils/coordinate.js';
import { Rect } from '../utils/rect.js';
import { WorkspaceSvg } from '../workspace_svg.js';
import { Bubble } from './bubble.js';
/**
 * A bubble that displays non-editable text. Used by the warning icon.
 */
export declare class TextBubble extends Bubble {
    private text;
    readonly workspace: WorkspaceSvg;
    protected anchor: Coordinate;
    protected ownerRect?: Rect | undefined;
    private paragraph;
    constructor(text: string, workspace: WorkspaceSvg, anchor: Coordinate, ownerRect?: Rect | undefined);
    /** @returns the current text of this text bubble. */
    getText(): string;
    /** Sets the current text of this text bubble, and updates the display. */
    setText(text: string): void;
    /**
     * Converts the given string into an svg containing that string,
     * broken up by newlines.
     */
    private stringToSvg;
    /** Creates the paragraph container for this bubble's view's text fragments. */
    private createParagraph;
    /** Creates the text fragments visualizing the text of this bubble. */
    private createTextFragments;
    /** Right aligns the given text fragments. */
    private rightAlignTextFragments;
    /** Updates the size of this bubble to account for the size of the text. */
    private updateBubbleSize;
}
//# sourceMappingURL=text_bubble.d.ts.map