/**
 * @license
 * Copyright 2025 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceSvg } from '../workspace_svg.js';
import { CommentBarButton } from './comment_bar_button.js';
import type { CommentView } from './comment_view.js';
/**
 * Magic string appended to the comment ID to create a unique ID for this button.
 */
export declare const COMMENT_COLLAPSE_BAR_BUTTON_FOCUS_IDENTIFIER = "_collapse_bar_button";
/**
 * Button that toggles the collapsed state of a comment.
 */
export declare class CollapseCommentBarButton extends CommentBarButton {
    protected readonly id: string;
    protected readonly workspace: WorkspaceSvg;
    protected readonly container: SVGGElement;
    protected readonly commentView: CommentView;
    /**
     * Opaque ID used to unbind event handlers during disposal.
     */
    private readonly bindId;
    /**
     * SVG image displayed on this button.
     */
    protected readonly icon: SVGImageElement;
    /**
     * Creates a new CollapseCommentBarButton instance.
     *
     * @param id The ID of this button's parent comment.
     * @param workspace The workspace this button's parent comment is displayed on.
     * @param container An SVG group that this button should be a child of.
     */
    constructor(id: string, workspace: WorkspaceSvg, container: SVGGElement, commentView: CommentView);
    /**
     * Disposes of this button.
     */
    dispose(): void;
    /**
     * Adjusts the positioning of this button within its container.
     */
    reposition(): void;
    /**
     * Toggles the collapsed state of the parent comment.
     *
     * @param e The event that triggered this action.
     */
    performAction(e?: Event): void;
}
//# sourceMappingURL=collapse_comment_bar_button.d.ts.map