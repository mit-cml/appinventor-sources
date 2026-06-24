/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IFocusableNode } from '../interfaces/i_focusable_node.js';
import { IFocusableTree } from '../interfaces/i_focusable_tree.js';
import { Size } from '../utils/size.js';
import { WorkspaceSvg } from '../workspace_svg.js';
/**
 * String added to the ID of a workspace comment to identify
 * the focusable node for the comment editor.
 */
export declare const COMMENT_EDITOR_FOCUS_IDENTIFIER = "_comment_textarea_";
/** The part of a comment that can be typed into. */
export declare class CommentEditor implements IFocusableNode {
    workspace: WorkspaceSvg;
    private onFinishEditing?;
    id?: string;
    /** The foreignObject containing the HTML text area. */
    private foreignObject;
    /** The text area where the user can type. */
    private textArea;
    /** Listeners for changes to text. */
    private textChangeListeners;
    /** The current text of the comment. Updates on text area change. */
    private text;
    constructor(workspace: WorkspaceSvg, commentId?: string, onFinishEditing?: (() => void) | undefined);
    /** Gets the dom structure for this comment editor. */
    getDom(): SVGForeignObjectElement;
    /** Gets the current text of the comment. */
    getText(): string;
    /** Sets the current text of the comment and fires change listeners. */
    setText(text: string): void;
    /**
     * Triggers listeners when the text of the comment changes, either
     * programmatically or manually by the user.
     */
    private onTextChange;
    /**
     * Do something when the user indicates they've finished editing.
     *
     * @param e Keyboard event.
     */
    private handleKeyDown;
    /** Registers a callback that listens for text changes. */
    addTextChangeListener(listener: (oldText: string, newText: string) => void): void;
    /** Removes the given listener from the list of text change listeners. */
    removeTextChangeListener(listener: () => void): void;
    /** Sets the placeholder text displayed for an empty comment. */
    setPlaceholderText(text: string): void;
    /** Sets whether the textarea is editable. If not, the textarea will be readonly. */
    setEditable(isEditable: boolean): void;
    /** Update the size of the comment editor element. */
    updateSize(size: Size, topBarSize: Size): void;
    getFocusableElement(): HTMLElement | SVGElement;
    getFocusableTree(): IFocusableTree;
    onNodeFocus(): void;
    onNodeBlur(): void;
    canBeFocused(): boolean;
}
//# sourceMappingURL=comment_editor.d.ts.map