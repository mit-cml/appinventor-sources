/**
 * @license
 * Copyright 2011 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Flyout tray containing blocks which may be created.
 *
 * @class
 */
import { BlockSvg } from './block_svg.js';
import { DeleteArea } from './delete_area.js';
import { FlyoutItem } from './flyout_item.js';
import { IAutoHideable } from './interfaces/i_autohideable.js';
import type { IFlyout } from './interfaces/i_flyout.js';
import type { IFlyoutInflater } from './interfaces/i_flyout_inflater.js';
import { IFocusableNode } from './interfaces/i_focusable_node.js';
import type { IFocusableTree } from './interfaces/i_focusable_tree.js';
import type { Options } from './options.js';
import * as blocks from './serialization/blocks.js';
import { Coordinate } from './utils/coordinate.js';
import { Svg } from './utils/svg.js';
import * as toolbox from './utils/toolbox.js';
import { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for a flyout.
 */
export declare abstract class Flyout extends DeleteArea implements IAutoHideable, IFlyout, IFocusableNode {
    /**
     * Position the flyout.
     */
    abstract position(): void;
    /**
     * Determine if a drag delta is toward the workspace, based on the position
     * and orientation of the flyout. This is used in determineDragIntention_ to
     * determine if a new block should be created or if the flyout should scroll.
     *
     * @param currentDragDeltaXY How far the pointer has
     *     moved from the position at mouse down, in pixel units.
     * @returns True if the drag is toward the workspace.
     */
    abstract isDragTowardWorkspace(currentDragDeltaXY: Coordinate): boolean;
    /**
     * Sets the translation of the flyout to match the scrollbars.
     *
     * @param xyRatio Contains a y property which is a float
     *     between 0 and 1 specifying the degree of scrolling and a
     *     similar x property.
     */
    protected abstract setMetrics_(xyRatio: {
        x?: number;
        y?: number;
    }): void;
    /**
     * Lay out the elements in the flyout.
     *
     * @param contents The flyout elements to lay out.
     */
    protected abstract layout_(contents: FlyoutItem[]): void;
    /**
     * Scroll the flyout.
     *
     * @param e Mouse wheel scroll event.
     */
    protected abstract wheel_(e: WheelEvent): void;
    /**
     * Compute bounds of flyout.
     * For RTL: Lay out the elements right-aligned.
     */
    protected abstract reflowInternal_(): void;
    /**
     * Calculates the x coordinate for the flyout position.
     *
     * @returns X coordinate.
     */
    abstract getX(): number;
    /**
     * Calculates the y coordinate for the flyout position.
     *
     * @returns Y coordinate.
     */
    abstract getY(): number;
    /**
     * Scroll the flyout to the beginning of its contents.
     */
    abstract scrollToStart(): void;
    protected workspace_: WorkspaceSvg;
    RTL: boolean;
    /**
     * Whether the flyout should be laid out horizontally or not.
     *
     * @internal
     */
    horizontalLayout: boolean;
    protected toolboxPosition_: number;
    /**
     * Array holding info needed to unbind events.
     * Used for disposing.
     * Ex: [[node, name, func], [node, name, func]].
     */
    private boundEvents;
    /**
     * Function that will be registered as a change listener on the workspace
     * to reflow when elements in the flyout workspace change.
     */
    private reflowWrapper;
    /**
     * If true, prevents the reflow wrapper from running. Used to prevent infinite
     * recursion.
     */
    private inhibitReflowWrapper;
    /**
     * List of flyout elements.
     */
    protected contents: FlyoutItem[];
    protected readonly tabWidth_: number;
    /**
     * The target workspace.
     *
     * @internal
     */
    targetWorkspace: WorkspaceSvg;
    /**
     * Does the flyout automatically close when a block is created?
     */
    autoClose: boolean;
    /**
     * Whether the flyout is visible.
     */
    private visible;
    /**
     * Whether the workspace containing this flyout is visible.
     */
    private containerVisible;
    /**
     * Corner radius of the flyout background.
     */
    readonly CORNER_RADIUS: number;
    readonly MARGIN: number;
    readonly GAP_X: number;
    readonly GAP_Y: number;
    /**
     * Top/bottom padding between scrollbar and edge of flyout background.
     */
    readonly SCROLLBAR_MARGIN: number;
    /**
     * Width of flyout.
     */
    protected width_: number;
    /**
     * Height of flyout.
     */
    protected height_: number;
    /**
     * Range of a drag angle from a flyout considered "dragging toward
     * workspace". Drags that are within the bounds of this many degrees from
     * the orthogonal line to the flyout edge are considered to be "drags toward
     * the workspace".
     *
     * @example
     *
     * ```
     * Flyout                                                 Edge   Workspace
     * [block] /  <-within this angle, drags "toward workspace" |
     * [block] ---- orthogonal to flyout boundary ----          |
     * [block] \                                                |
     * ```
     *
     * The angle is given in degrees from the orthogonal.
     *
     * This is used to know when to create a new block and when to scroll the
     * flyout. Setting it to 360 means that all drags create a new block.
     */
    protected dragAngleRange_: number;
    /**
     * The path around the background of the flyout, which will be filled with a
     * background colour.
     */
    protected svgBackground_: SVGPathElement | null;
    /**
     * The root SVG group for the button or label.
     */
    protected svgGroup_: SVGGElement | null;
    /**
     * Map from flyout content type to the corresponding inflater class
     * responsible for creating concrete instances of the content type.
     */
    protected inflaters: Map<string, IFlyoutInflater>;
    /**
     * @param workspaceOptions Dictionary of options for the
     *     workspace.
     */
    constructor(workspaceOptions: Options);
    /**
     * Creates the flyout's DOM.  Only needs to be called once.  The flyout can
     * either exist as its own SVG element or be a g element nested inside a
     * separate SVG element.
     *
     * @param tagName The type of tag to
     *     put the flyout in. This should be <svg> or <g>.
     * @returns The flyout's SVG group.
     */
    createDom(tagName: string | Svg<SVGSVGElement> | Svg<SVGGElement>): SVGElement;
    /**
     * Initializes the flyout.
     *
     * @param targetWorkspace The workspace in which to
     *     create new blocks.
     */
    init(targetWorkspace: WorkspaceSvg): void;
    /**
     * Dispose of this flyout.
     * Unlink from all DOM elements to prevent memory leaks.
     */
    dispose(): void;
    /**
     * Get the width of the flyout.
     *
     * @returns The width of the flyout.
     */
    getWidth(): number;
    /**
     * Get the height of the flyout.
     *
     * @returns The width of the flyout.
     */
    getHeight(): number;
    /**
     * Get the scale (zoom level) of the flyout. By default,
     * this matches the target workspace scale, but this can be overridden.
     *
     * @returns Flyout workspace scale.
     */
    getFlyoutScale(): number;
    /**
     * Get the workspace inside the flyout.
     *
     * @returns The workspace inside the flyout.
     */
    getWorkspace(): WorkspaceSvg;
    /**
     * Sets whether this flyout automatically closes when blocks are dragged out,
     * the workspace is clicked, etc, or not.
     */
    setAutoClose(autoClose: boolean): void;
    /** Automatically hides the flyout if it is an autoclosing flyout. */
    autoHide(onlyClosePopups: boolean): void;
    /**
     * Get the target workspace inside the flyout.
     *
     * @returns The target workspace inside the flyout.
     */
    getTargetWorkspace(): WorkspaceSvg;
    /**
     * Is the flyout visible?
     *
     * @returns True if visible.
     */
    isVisible(): boolean;
    /**
     * Set whether the flyout is visible. A value of true does not necessarily
     * mean that the flyout is shown. It could be hidden because its container is
     * hidden.
     *
     * @param visible True if visible.
     */
    setVisible(visible: boolean): void;
    /**
     * Set whether this flyout's container is visible.
     *
     * @param visible Whether the container is visible.
     */
    setContainerVisible(visible: boolean): void;
    /**
     * Get the list of elements of the current flyout.
     *
     * @returns The array of flyout elements.
     */
    getContents(): FlyoutItem[];
    /**
     * Store the list of elements on the flyout.
     *
     * @param contents - The array of items for the flyout.
     */
    setContents(contents: FlyoutItem[]): void;
    /**
     * Update the display property of the flyout based whether it thinks it should
     * be visible and whether its containing workspace is visible.
     */
    private updateDisplay;
    /**
     * Update the view based on coordinates calculated in position().
     *
     * @param width The computed width of the flyout's SVG group
     * @param height The computed height of the flyout's SVG group.
     * @param x The computed x origin of the flyout's SVG group.
     * @param y The computed y origin of the flyout's SVG group.
     */
    protected positionAt_(width: number, height: number, x: number, y: number): void;
    /**
     * Hide and empty the flyout.
     */
    hide(): void;
    /**
     * Show and populate the flyout.
     *
     * @param flyoutDef Contents to display
     *     in the flyout. This is either an array of Nodes, a NodeList, a
     *     toolbox definition, or a string with the name of the dynamic category.
     */
    show(flyoutDef: toolbox.FlyoutDefinition | string): void;
    /**
     * Create the contents array and gaps array necessary to create the layout for
     * the flyout.
     *
     * @param parsedContent The array
     *     of objects to show in the flyout.
     * @returns The list of contents needed to lay out the flyout.
     */
    private createFlyoutInfo;
    /**
     * Updates and returns the provided list of flyout contents to flatten
     * separators as needed.
     *
     * When multiple separators occur one after another, the value of the last one
     * takes precedence and the earlier separators in the group are removed.
     *
     * @param contents The list of flyout contents to flatten separators in.
     * @returns An updated list of flyout contents with only one separator between
     *     each non-separator item.
     */
    protected normalizeSeparators(contents: FlyoutItem[]): FlyoutItem[];
    /**
     * Gets the flyout definition for the dynamic category.
     *
     * @param categoryName The name of the dynamic category.
     * @returns The definition of the
     *     flyout in one of its many forms.
     */
    private getDynamicCategoryContents;
    /**
     * Delete elements from a previous showing of the flyout.
     */
    private clearOldBlocks;
    /**
     * Pointer down on the flyout background.  Start a vertical scroll drag.
     *
     * @param e Pointer down event.
     */
    private onMouseDown;
    /**
     * Does this flyout allow you to create a new instance of the given block?
     * Used for deciding if a block can be "dragged out of" the flyout.
     *
     * @param block The block to copy from the flyout.
     * @returns True if you can create a new instance of the block, false
     *    otherwise.
     * @internal
     */
    isBlockCreatable(block: BlockSvg): boolean;
    /**
     * Create a copy of this block on the workspace.
     *
     * @param originalBlock The block to copy from the flyout.
     * @returns The newly created block.
     * @throws {Error} if something went wrong with deserialization.
     * @internal
     */
    createBlock(originalBlock: BlockSvg): BlockSvg;
    /**
     * Reflow flyout contents.
     */
    reflow(): void;
    /**
     * @returns True if this flyout may be scrolled with a scrollbar or
     *     by dragging.
     * @internal
     */
    isScrollable(): boolean;
    /**
     * Serialize a block to JSON.
     *
     * @param block The block to serialize.
     * @returns A serialized representation of the block.
     */
    protected serializeBlock(block: BlockSvg): blocks.State;
    /**
     * Positions a block on the target workspace.
     *
     * @param oldBlock The flyout block being copied.
     * @param block The block to posiiton.
     */
    private positionNewBlock;
    /**
     * Returns the inflater responsible for constructing items of the given type.
     *
     * @param type The type of flyout content item to provide an inflater for.
     * @returns An inflater object for the given type, or null if no inflater
     *     is registered for that type.
     */
    protected getInflaterForType(type: string): IFlyoutInflater | null;
    /**
     * See IFocusableNode.getFocusableElement.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    getFocusableElement(): HTMLElement | SVGElement;
    /**
     * See IFocusableNode.getFocusableTree.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    getFocusableTree(): IFocusableTree;
    /** See IFocusableNode.onNodeFocus. */
    onNodeFocus(): void;
    /** See IFocusableNode.onNodeBlur. */
    onNodeBlur(): void;
    /** See IFocusableNode.canBeFocused. */
    canBeFocused(): boolean;
    /**
     * See IFocusableNode.getRootFocusableNode.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    getRootFocusableNode(): IFocusableNode;
    /**
     * See IFocusableNode.getRestoredFocusableNode.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    getRestoredFocusableNode(_previousNode: IFocusableNode | null): IFocusableNode | null;
    /**
     * See IFocusableNode.getNestedTrees.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    getNestedTrees(): Array<IFocusableTree>;
    /**
     * See IFocusableNode.lookUpFocusableNode.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    lookUpFocusableNode(_id: string): IFocusableNode | null;
    /** See IFocusableTree.onTreeFocus. */
    onTreeFocus(_node: IFocusableNode, _previousTree: IFocusableTree | null): void;
    /**
     * See IFocusableNode.onTreeBlur.
     *
     * @deprecated v12: Use the Flyout's workspace for focus operations, instead.
     */
    onTreeBlur(_nextTree: IFocusableTree | null): void;
}
//# sourceMappingURL=flyout_base.d.ts.map