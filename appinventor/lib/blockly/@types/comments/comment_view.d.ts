/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IRenderedElement } from '../interfaces/i_rendered_element.js';
import { Coordinate } from '../utils/coordinate.js';
import { Size } from '../utils/size.js';
import { WorkspaceSvg } from '../workspace_svg.js';
export declare class CommentView implements IRenderedElement {
    private readonly workspace;
    /** The root group element of the comment view. */
    private svgRoot;
    /**
     * The svg rect element that we use to create a hightlight around the comment.
     */
    private highlightRect;
    /** The group containing all of the top bar elements. */
    private topBarGroup;
    /** The rect background for the top bar. */
    private topBarBackground;
    /** The delete icon that goes in the top bar. */
    private deleteIcon;
    /** The foldout icon that goes in the top bar. */
    private foldoutIcon;
    /** The text element that goes in the top bar. */
    private textPreview;
    /** The actual text node in the text preview. */
    private textPreviewNode;
    /** The resize handle element. */
    private resizeHandle;
    /** The foreignObject containing the HTML text area. */
    private foreignObject;
    /** The text area where the user can type. */
    private textArea;
    /** The current size of the comment in workspace units. */
    private size;
    /** Whether the comment is collapsed or not. */
    private collapsed;
    /** Whether the comment is editable or not. */
    private editable;
    /** The current location of the comment in workspace coordinates. */
    private location;
    /** The current text of the comment. Updates on  text area change. */
    private text;
    /** Listeners for changes to text. */
    private textChangeListeners;
    /** Listeners for changes to size. */
    private sizeChangeListeners;
    /** Listeners for disposal. */
    private disposeListeners;
    /** Listeners for collapsing. */
    private collapseChangeListeners;
    /**
     * Event data for the pointer up event on the resize handle. Used to
     * unregister the listener.
     */
    private resizePointerUpListener;
    /**
     * Event data for the pointer move event on the resize handle. Used to
     * unregister the listener.
     */
    private resizePointerMoveListener;
    /** Whether this comment view is currently being disposed or not. */
    private disposing;
    /** Whether this comment view has been disposed or not. */
    private disposed;
    /** Size of this comment when the resize drag was initiated. */
    private preResizeSize?;
    constructor(workspace: WorkspaceSvg);
    /**
     * Creates the rect we use for highlighting the comment when it's selected.
     */
    private createHighlightRect;
    /**
     * Creates the top bar and the elements visually within it.
     * Registers event listeners.
     */
    private createTopBar;
    /**
     * Creates the text area where users can type. Registers event listeners.
     */
    private createTextArea;
    /** Creates the DOM elements for the comment resize handle. */
    private createResizeHandle;
    /** Returns the root SVG group element of the comment view. */
    getSvgRoot(): SVGGElement;
    /**
     * Returns the current size of the comment in workspace units.
     * Respects collapsing.
     */
    getSize(): Size;
    /**
     * Sets the size of the comment in workspace units, and updates the view
     * elements to reflect the new size.
     */
    setSizeWithoutFiringEvents(size: Size): void;
    /**
     * Sets the size of the comment in workspace units, updates the view
     * elements to reflect the new size, and triggers size change listeners.
     */
    setSize(size: Size): void;
    /**
     * Calculates the minimum size for the uncollapsed comment based on text
     * size and visible icons.
     *
     * The minimum width is based on the width of the truncated preview text.
     *
     * The minimum height is based on the height of the top bar.
     */
    private calcMinSize;
    /** Calculates the margin that should exist around the delete icon. */
    private calcDeleteMargin;
    /** Calculates the margin that should exist around the foldout icon. */
    private calcFoldoutMargin;
    /** Updates the size of the highlight rect to reflect the new size. */
    private updateHighlightRect;
    /** Updates the size of the top bar to reflect the new size. */
    private updateTopBarSize;
    /** Updates the size of the text area elements to reflect the new size. */
    private updateTextAreaSize;
    /**
     * Updates the position of the delete icon elements to reflect the new size.
     */
    private updateDeleteIconPosition;
    /**
     * Updates the position of the foldout icon elements to reflect the new size.
     */
    private updateFoldoutIconPosition;
    /**
     * Updates the size and position of the text preview elements to reflect the new size.
     */
    private updateTextPreviewSize;
    /** Updates the position of the resize handle to reflect the new size. */
    private updateResizeHandlePosition;
    /**
     * Triggers listeners when the size of the comment changes, either
     * programmatically or manually by the user.
     */
    private onSizeChange;
    /**
     * Registers a callback that listens for size changes.
     *
     * @param listener Receives callbacks when the size of the comment changes.
     *     The new and old size are in workspace units.
     */
    addSizeChangeListener(listener: (oldSize: Size, newSize: Size) => void): void;
    /** Removes the given listener from the list of size change listeners. */
    removeSizeChangeListener(listener: () => void): void;
    /**
     * Handles starting an interaction with the resize handle to resize the
     * comment.
     */
    private onResizePointerDown;
    /** Ends an interaction with the resize handle. */
    private onResizePointerUp;
    /** Resizes the comment in response to a drag on the resize handle. */
    private onResizePointerMove;
    /** Returns true if the comment is currently collapsed. */
    isCollapsed(): boolean;
    /** Sets whether the comment is currently collapsed or not. */
    setCollapsed(collapsed: boolean): void;
    /**
     * Triggers listeners when the collapsed-ness of the comment changes, either
     * progrmatically or manually by the user.
     */
    private onCollapse;
    /** Registers a callback that listens for collapsed-ness changes. */
    addOnCollapseListener(listener: (newCollapse: boolean) => void): void;
    /** Removes the given listener from the list of on collapse listeners. */
    removeOnCollapseListener(listener: () => void): void;
    /**
     * Toggles the collapsedness of the block when we receive a pointer down
     * event on the foldout icon.
     */
    private onFoldoutDown;
    /** Returns true if the comment is currently editable. */
    isEditable(): boolean;
    /** Sets the editability of the comment. */
    setEditable(editable: boolean): void;
    /** Returns the current location of the comment in workspace coordinates. */
    getRelativeToSurfaceXY(): Coordinate;
    /**
     * Moves the comment view to the given location.
     *
     * @param location The location to move to in workspace coordinates.
     */
    moveTo(location: Coordinate): void;
    /** Retursn the current text of the comment. */
    getText(): string;
    /** Sets the current text of the comment. */
    setText(text: string): void;
    /** Registers a callback that listens for text changes. */
    addTextChangeListener(listener: (oldText: string, newText: string) => void): void;
    /** Removes the given listener from the list of text change listeners. */
    removeTextChangeListener(listener: () => void): void;
    /**
     * Triggers listeners when the text of the comment changes, either
     * programmatically or manually by the user.
     */
    private onTextChange;
    /** Updates the preview text element to reflect the given text. */
    private updateTextPreview;
    /** Truncates the text to fit within the top view. */
    private truncateText;
    /** Brings the workspace comment to the front of its layer. */
    private bringToFront;
    /**
     * Handles disposing of the comment when we get a pointer down event on the
     * delete icon.
     */
    private onDeleteDown;
    /** Disposes of this comment view. */
    dispose(): void;
    /** Returns whether this comment view has been disposed or not. */
    isDisposed(): boolean;
    /**
     * Returns true if this comment view is currently being disposed or has
     * already been disposed.
     */
    isDeadOrDying(): boolean;
    /** Registers a callback that listens for disposal of this view. */
    addDisposeListener(listener: () => void): void;
    /** Removes the given listener from the list of disposal listeners. */
    removeDisposeListener(listener: () => void): void;
}
//# sourceMappingURL=comment_view.d.ts.map