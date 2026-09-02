/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from '../interfaces/i_focusable_node.js';
import { Rect } from '../utils/rect.js';
import type { WorkspaceSvg } from '../workspace_svg.js';
import type { CommentView } from './comment_view.js';
/**
 * Button displayed on a comment's top bar.
 */
export declare abstract class CommentBarButton implements IFocusableNode {
    protected readonly id: string;
    protected readonly workspace: WorkspaceSvg;
    protected readonly container: SVGGElement;
    protected readonly commentView: CommentView;
    /**
     * SVG image displayed on this button.
     */
    protected abstract readonly icon: SVGImageElement;
    /**
     * Creates a new CommentBarButton instance.
     *
     * @param id The ID of this button's parent comment.
     * @param workspace The workspace this button's parent comment is on.
     * @param container An SVG group that this button should be a child of.
     */
    constructor(id: string, workspace: WorkspaceSvg, container: SVGGElement, commentView: CommentView);
    /**
     * Returns whether or not this button is currently visible.
     */
    isVisible(): boolean;
    /**
     * Returns the parent comment view of this comment bar button.
     */
    getCommentView(): CommentView;
    /** Adjusts the position of this button within its parent container. */
    abstract reposition(): void;
    /** Perform the action this button should take when it is acted on. */
    abstract performAction(e?: Event): void;
    /**
     * Returns the dimensions of this button in workspace coordinates.
     *
     * @param includeMargin True to include the margin when calculating the size.
     * @returns The size of this button.
     */
    getSize(includeMargin?: boolean): Rect;
    /** Returns the margin in workspace coordinates surrounding this button. */
    getMargin(): number;
    /** Returns a DOM element representing this button that can receive focus. */
    getFocusableElement(): SVGImageElement;
    /** Returns the workspace this button is a child of. */
    getFocusableTree(): WorkspaceSvg;
    /** Called when this button's focusable DOM element gains focus. */
    onNodeFocus(): void;
    /** Called when this button's focusable DOM element loses focus. */
    onNodeBlur(): void;
    /** Returns whether this button can be focused. True if it is visible. */
    canBeFocused(): boolean;
    /**
     * Recomputes the ARIA label and role for this button. Note that this is not
     * automatically called during initialization and must be called once a button's
     * focusable element (icon) is initialized. Implementations may also find it useful
     * to call this if the button's label should be changed.
     */
    protected recomputeAriaContext(): void;
    /**
     * Returns the ARIA label to use for this button (defaults to null). Note that this
     * method will only be called and apply when recomputeAriaContext is called.
     *
     * @returns The ARIA label to use for this button, or null to use a default.
     */
    protected getAriaLabel(): string | null;
}
//# sourceMappingURL=comment_bar_button.d.ts.map