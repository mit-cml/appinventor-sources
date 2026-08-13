/**
 * @license
 * Copyright 2011 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { DeleteArea } from './delete_area.js';
import './events/events_trashcan_open.js';
import type { IAutoHideable } from './interfaces/i_autohideable.js';
import type { IComponent } from './interfaces/i_component';
import type { IDraggable } from './interfaces/i_draggable.js';
import type { IFlyout } from './interfaces/i_flyout.js';
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { IPositionable } from './interfaces/i_positionable.js';
import type { UiMetrics } from './metrics_manager.js';
import { Rect } from './utils/rect.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for a trash can.
 */
export declare class Trashcan extends DeleteArea implements IAutoHideable, IPositionable, IFocusableNode, IComponent {
    private workspace;
    /**
     * The id for this component that is used to register with the
     * ComponentManager.
     */
    id: string;
    /**
     * A globally unique ID for this particular trashcan. Component Manager IDs
     * (the ID above) are 1:1 with classes, but if there are multiple workspaces
     * with trashcans on a page, each actual trashcan DOM element needs a unique
     * ID to support focusable node resolution. This ID is for that purpose.
     */
    private uniqueId;
    /**
     * A list of JSON (stored as strings) representing blocks in the trashcan.
     */
    private readonly contents;
    /**
     * The trashcan flyout.
     *
     * @internal
     */
    flyout: IFlyout | null;
    /** Current open/close state of the lid. */
    isLidOpen: boolean;
    /** The SVG group containing the trash can. */
    private svgGroup;
    /** Left coordinate of the trash can. */
    private left;
    /** Top coordinate of the trash can. */
    private top;
    /** Whether this trash can has been initialized. */
    private initialized;
    /** @param workspace The workspace to sit in. */
    constructor(workspace: WorkspaceSvg);
    /**
     * Create the trash can elements.
     *
     * @returns The trash can's SVG group.
     */
    createDom(): SVGElement;
    /** Initializes the trash can. */
    init(): void;
    /**
     * Dispose of this trash can.
     * Unlink from all DOM elements to prevent memory leaks.
     */
    dispose(): void;
    /**
     * Whether the trashcan has contents.
     *
     * @returns True if the trashcan has contents.
     */
    private hasContents;
    /**
     * Returns true if the trashcan contents-flyout is currently open.
     *
     * @returns True if the trashcan contents-flyout is currently open.
     */
    contentsIsOpen(): boolean;
    /** Opens the trashcan flyout. */
    openFlyout(): void;
    /** Closes the trashcan flyout. */
    closeFlyout(): void;
    /**
     * Hides the component. Called in WorkspaceSvg.hideChaff.
     *
     * @param onlyClosePopups Whether only popups should be closed.
     *     Flyouts should not be closed if this is true.
     */
    autoHide(onlyClosePopups: boolean): void;
    /**
     * Empties the trashcan's contents. If the contents-flyout is currently open
     * it will be closed.
     */
    emptyContents(): void;
    /**
     * Positions the trashcan.
     * It is positioned in the opposite corner to the corner the
     * categories/toolbox starts at.
     *
     * @param metrics The workspace metrics.
     * @param savedPositions List of rectangles that are already on the workspace.
     */
    position(metrics: UiMetrics, savedPositions: Rect[]): void;
    /**
     * Returns the bounding rectangle of the UI element in pixel units relative to
     * the Blockly injection div.
     *
     * @returns The UI elements's bounding box. Null if bounding box should be
     *     ignored by other UI elements.
     */
    getBoundingRectangle(): Rect | null;
    /**
     * Returns the bounding rectangle of the drag target area in pixel units
     * relative to viewport.
     *
     * @returns The component's bounding box. Null if drag target area should be
     *     ignored.
     */
    getClientRect(): Rect | null;
    /**
     * Handles when a cursor with a block or bubble is dragged over this drag
     * target.
     *
     * @param _dragElement The block or bubble currently being dragged.
     */
    onDragOver(_dragElement: IDraggable): void;
    /**
     * Handles when a cursor with a block or bubble exits this drag target.
     *
     * @param _dragElement The block or bubble currently being dragged.
     */
    onDragExit(_dragElement: IDraggable): void;
    /**
     * Handles when a block or bubble is dropped on this component.
     * Should not handle delete here.
     *
     * @param _dragElement The block or bubble currently being dragged.
     */
    onDrop(_dragElement: IDraggable): void;
    /**
     * Flip the lid open or shut.
     *
     * @param state True if open.
     * @internal
     */
    setLidOpen(state: boolean): void;
    /**
     * Flip the lid shut.
     * Called externally after a drag.
     */
    closeLid(): void;
    /** Inspect the contents of the trash. */
    click(): void;
    /**
     * Fires a UI event for trashcan flyout open or close.
     *
     * @param trashcanOpen Whether the flyout is opening.
     */
    private fireUiEvent;
    /**
     * Prevents a workspace scroll and click event if the trashcan has blocks.
     *
     * @param e A mouse down event.
     */
    private blockMouseDownWhenOpenable;
    /**
     * Handle a BLOCK_DELETE event. Adds deleted blocks oldXml to the content
     * array.
     *
     * @param event Workspace event.
     */
    private onDelete;
    /**
     * Converts JSON representing a block into text that can be stored in the
     * content array.
     *
     * @param json A JSON representation of a block's state.
     * @returns A BlockInfo object corresponding to the JSON, cleaned of all
     *     unnecessary attributes.
     */
    private cleanBlockJson;
    getFocusableElement(): SVGElement;
    getFocusableTree(): WorkspaceSvg;
    onNodeFocus(): void;
    onNodeBlur(): void;
    canBeFocused(): boolean;
    performAction(): void;
}
//# sourceMappingURL=trashcan.d.ts.map