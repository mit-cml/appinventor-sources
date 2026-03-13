/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Methods for graphically rendering a block as SVG.
 *
 * @class
 */
import './events/events_selected.js';
import { Block } from './block.js';
import { IDeletable } from './blockly.js';
import { BlockCopyData } from './clipboard/block_paster.js';
import type { Connection } from './connection.js';
import { ConnectionType } from './connection_type.js';
import { ContextMenuOption, LegacyContextMenuOption } from './contextmenu_registry.js';
import type { Field } from './field.js';
import { IconType } from './icons/icon_types.js';
import { MutatorIcon } from './icons/mutator_icon.js';
import type { Input } from './inputs/input.js';
import type { IASTNodeLocationSvg } from './interfaces/i_ast_node_location_svg.js';
import type { IBoundedElement } from './interfaces/i_bounded_element.js';
import type { ICopyable } from './interfaces/i_copyable.js';
import type { IDragStrategy, IDraggable } from './interfaces/i_draggable.js';
import { IIcon } from './interfaces/i_icon.js';
import { RenderedConnection } from './rendered_connection.js';
import type { IPathObject } from './renderers/common/i_path_object.js';
import type { BlockStyle } from './theme.js';
import { Coordinate } from './utils/coordinate.js';
import { Rect } from './utils/rect.js';
import { FlyoutItemInfo } from './utils/toolbox.js';
import type { Workspace } from './workspace.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for a block's SVG representation.
 * Not normally called directly, workspace.newBlock() is preferred.
 */
export declare class BlockSvg extends Block implements IASTNodeLocationSvg, IBoundedElement, ICopyable<BlockCopyData>, IDraggable, IDeletable {
    /**
     * Constant for identifying rows that are to be rendered inline.
     * Don't collide with Blockly.inputTypes.
     */
    static readonly INLINE = -1;
    /**
     * ID to give the "collapsed warnings" warning. Allows us to remove the
     * "collapsed warnings" warning without removing any warnings that belong to
     * the block.
     */
    static readonly COLLAPSED_WARNING_ID = "TEMP_COLLAPSED_WARNING_";
    decompose?: (p1: Workspace) => BlockSvg;
    /**
     * An optional method which saves a record of blocks connected to
     * this block so they can be later restored after this block is
     * recoomposed (reconfigured).  Typically records the connected
     * blocks on properties on blocks in the mutator flyout, so that
     * rearranging those component blocks will automatically rearrange
     * the corresponding connected blocks on this block after this block
     * is recomposed.
     *
     * To keep the saved connection information up-to-date, MutatorIcon
     * arranges for an event listener to call this method any time the
     * mutator flyout is open and a change occurs on this block's
     * workspace.
     *
     * @param rootBlock The root block in the mutator flyout.
     */
    saveConnections?: (rootBlock: BlockSvg) => void;
    customContextMenu?: (p1: Array<ContextMenuOption | LegacyContextMenuOption>) => void;
    /**
     * Height of this block, not including any statement blocks above or below.
     * Height is in workspace units.
     */
    height: number;
    /**
     * Width of this block, including any connected value blocks.
     * Width is in workspace units.
     */
    width: number;
    /**
     * Width of this block, not including any connected value blocks.
     * Width is in workspace units.
     *
     * @internal
     */
    childlessWidth: number;
    /**
     * Map from IDs for warnings text to PIDs of functions to apply them.
     * Used to be able to maintain multiple warnings.
     */
    private warningTextDb;
    /** Block's mutator icon (if any). */
    mutator: MutatorIcon | null;
    private svgGroup;
    style: BlockStyle;
    /** @internal */
    pathObject: IPathObject;
    /** Is this block a BlockSVG? */
    readonly rendered = true;
    private visuallyDisabled;
    workspace: WorkspaceSvg;
    outputConnection: RenderedConnection;
    nextConnection: RenderedConnection;
    previousConnection: RenderedConnection;
    private translation;
    /** Whether this block is currently being dragged. */
    private dragging;
    /**
     * The location of the top left of this block (in workspace coordinates)
     * relative to either its parent block, or the workspace origin if it has no
     * parent.
     *
     * @internal
     */
    relativeCoords: Coordinate;
    private dragStrategy;
    /**
     * @param workspace The block's workspace.
     * @param prototypeName Name of the language object containing type-specific
     *     functions for this block.
     * @param opt_id Optional ID.  Use this ID if provided, otherwise create a new
     *     ID.
     */
    constructor(workspace: WorkspaceSvg, prototypeName: string, opt_id?: string);
    /**
     * Create and initialize the SVG representation of the block.
     * May be called more than once.
     */
    initSvg(): void;
    /**
     * Get the secondary colour of a block.
     *
     * @returns #RRGGBB string.
     */
    getColourSecondary(): string;
    /**
     * Get the tertiary colour of a block.
     *
     * @returns #RRGGBB string.
     */
    getColourTertiary(): string;
    /** Selects this block. Highlights the block visually. */
    select(): void;
    /** Unselects this block. Unhighlights the block visually. */
    unselect(): void;
    /**
     * Sets the parent of this block to be a new block or null.
     *
     * @param newParent New parent block.
     * @internal
     */
    setParent(newParent: this | null): void;
    /**
     * Return the coordinates of the top-left corner of this block relative to the
     * drawing surface's origin (0,0), in workspace units.
     * If the block is on the workspace, (0, 0) is the origin of the workspace
     * coordinate system.
     * This does not change with workspace scale.
     *
     * @returns Object with .x and .y properties in workspace coordinates.
     */
    getRelativeToSurfaceXY(): Coordinate;
    /**
     * Move a block by a relative offset.
     *
     * @param dx Horizontal offset in workspace units.
     * @param dy Vertical offset in workspace units.
     * @param reason Why is this move happening?  'drag', 'bump', 'snap', ...
     */
    moveBy(dx: number, dy: number, reason?: string[]): void;
    /**
     * Transforms a block by setting the translation on the transform attribute
     * of the block's SVG.
     *
     * @param x The x coordinate of the translation in workspace units.
     * @param y The y coordinate of the translation in workspace units.
     */
    translate(x: number, y: number): void;
    /**
     * Returns the SVG translation of this block.
     *
     * @internal
     */
    getTranslation(): string;
    /**
     * Move a block to a position.
     *
     * @param xy The position to move to in workspace units.
     * @param reason Why is this move happening?  'drag', 'bump', 'snap', ...
     */
    moveTo(xy: Coordinate, reason?: string[]): void;
    /**
     * Move this block during a drag.
     * This block must be a top-level block.
     *
     * @param newLoc The location to translate to, in workspace coordinates.
     * @internal
     */
    moveDuringDrag(newLoc: Coordinate): void;
    /** Snap this block to the nearest grid point. */
    snapToGrid(): void;
    /**
     * Returns the coordinates of a bounding box describing the dimensions of this
     * block and any blocks stacked below it.
     * Coordinate system: workspace coordinates.
     *
     * @returns Object with coordinates of the bounding box.
     */
    getBoundingRectangle(): Rect;
    /**
     * Returns the coordinates of a bounding box describing the dimensions of this
     * block alone.
     * Coordinate system: workspace coordinates.
     *
     * @returns Object with coordinates of the bounding box.
     */
    getBoundingRectangleWithoutChildren(): Rect;
    private getBoundingRectangleWithDimensions;
    /**
     * Notify every input on this block to mark its fields as dirty.
     * A dirty field is a field that needs to be re-rendered.
     */
    markDirty(): void;
    /**
     * Set whether the block is collapsed or not.
     *
     * @param collapsed True if collapsed.
     */
    setCollapsed(collapsed: boolean): void;
    /**
     * Makes sure that when the block is collapsed, it is rendered correctly
     * for that state.
     */
    private updateCollapsed;
    /**
     * Open the next (or previous) FieldTextInput.
     *
     * @param start Current field.
     * @param forward If true go forward, otherwise backward.
     */
    tab(start: Field, forward: boolean): void;
    /**
     * Handle a pointerdown on an SVG block.
     *
     * @param e Pointer down event.
     */
    private onMouseDown;
    /**
     * Load the block's help page in a new window.
     *
     * @internal
     */
    showHelp(): void;
    /**
     * Generate the context menu for this block.
     *
     * @returns Context menu options or null if no menu.
     */
    protected generateContextMenu(): Array<ContextMenuOption | LegacyContextMenuOption> | null;
    /**
     * Show the context menu for this block.
     *
     * @param e Mouse event.
     * @internal
     */
    showContextMenu(e: PointerEvent): void;
    /**
     * Updates the locations of any parts of the block that need to know where
     * they are (e.g. connections, icons).
     *
     * @param blockOrigin The top-left of this block in workspace coordinates.
     * @internal
     */
    updateComponentLocations(blockOrigin: Coordinate): void;
    private updateConnectionLocations;
    private updateIconLocations;
    private updateFieldLocations;
    /**
     * Recursively adds or removes the dragging class to this node and its
     * children.
     *
     * @param adding True if adding, false if removing.
     * @internal
     */
    setDragging(adding: boolean): void;
    /**
     * Set whether this block is movable or not.
     *
     * @param movable True if movable.
     */
    setMovable(movable: boolean): void;
    /**
     * Set whether this block is editable or not.
     *
     * @param editable True if editable.
     */
    setEditable(editable: boolean): void;
    /**
     * Sets whether this block is a shadow block or not.
     * This method is internal and should not be called by users of Blockly. To
     * create shadow blocks programmatically call connection.setShadowState
     *
     * @param shadow True if a shadow.
     * @internal
     */
    setShadow(shadow: boolean): void;
    /**
     * Set whether this block is an insertion marker block or not.
     * Once set this cannot be unset.
     *
     * @param insertionMarker True if an insertion marker.
     * @internal
     */
    setInsertionMarker(insertionMarker: boolean): void;
    /**
     * Return the root node of the SVG or null if none exists.
     *
     * @returns The root SVG node (probably a group).
     */
    getSvgRoot(): SVGGElement;
    /**
     * Dispose of this block.
     *
     * @param healStack If true, then try to heal any gap by connecting the next
     *     statement with the previous statement.  Otherwise, dispose of all
     *     children of this block.
     * @param animate If true, show a disposal animation and sound.
     */
    dispose(healStack?: boolean, animate?: boolean): void;
    /**
     * Disposes of this block without doing things required by the top block.
     * E.g. does trigger UI effects, remove nodes, etc.
     */
    disposeInternal(): void;
    /**
     * Delete a block and hide chaff when doing so. The block will not be deleted
     * if it's in a flyout. This is called from the context menu and keyboard
     * shortcuts as the full delete action. If you are disposing of a block from
     * the workspace and don't need to perform flyout checks, handle event
     * grouping, or hide chaff, then use `block.dispose()` directly.
     */
    checkAndDelete(): void;
    /**
     * Encode a block for copying.
     *
     * @returns Copy metadata, or null if the block is an insertion marker.
     */
    toCopyData(): BlockCopyData | null;
    /**
     * Updates the colour of the block to match the block's state.
     *
     * @internal
     */
    applyColour(): void;
    /**
     * Updates the colour of the block (and children) to match the current
     * disabled state.
     *
     * @internal
     */
    updateDisabled(): void;
    /**
     * Set this block's warning text.
     *
     * @param text The text, or null to delete.
     * @param id An optional ID for the warning text to be able to maintain
     *     multiple warnings.
     */
    setWarningText(text: string | null, id?: string): void;
    /**
     * Give this block a mutator dialog.
     *
     * @param mutator A mutator dialog instance or null to remove.
     */
    setMutator(mutator: MutatorIcon | null): void;
    addIcon<T extends IIcon>(icon: T): T;
    /**
     * Creates a pointer down event listener for the icon to append to its
     * root svg.
     */
    private createIconPointerDownListener;
    removeIcon(type: IconType<IIcon>): boolean;
    /**
     * @deprecated v11 - Set whether the block is manually enabled or disabled.
     * The user can toggle whether a block is disabled from a context menu
     * option. A block may still be disabled for other reasons even if the user
     * attempts to manually enable it, such as when the block is in an invalid
     * location. This method is deprecated and setDisabledReason should be used
     * instead.
     *
     * @param enabled True if enabled.
     */
    setEnabled(enabled: boolean): void;
    /**
     * Add or remove a reason why the block might be disabled. If a block has
     * any reasons to be disabled, then the block itself will be considered
     * disabled. A block could be disabled for multiple independent reasons
     * simultaneously, such as when the user manually disables it, or the block
     * is invalid.
     *
     * @param disabled If true, then the block should be considered disabled for
     *     at least the provided reason, otherwise the block is no longer disabled
     *     for that reason.
     * @param reason A language-neutral identifier for a reason why the block
     *     could be disabled. Call this method again with the same identifier to
     *     update whether the block is currently disabled for this reason.
     */
    setDisabledReason(disabled: boolean, reason: string): void;
    /**
     * Set whether the block is highlighted or not.  Block highlighting is
     * often used to visually mark blocks currently being executed.
     *
     * @param highlighted True if highlighted.
     */
    setHighlighted(highlighted: boolean): void;
    /**
     * Adds the visual "select" effect to the block, but does not actually select
     * it or fire an event.
     *
     * @see BlockSvg#select
     */
    addSelect(): void;
    /**
     * Removes the visual "select" effect from the block, but does not actually
     * unselect it or fire an event.
     *
     * @see BlockSvg#unselect
     */
    removeSelect(): void;
    /**
     * Update the cursor over this block by adding or removing a class.
     *
     * @param enable True if the delete cursor should be shown, false otherwise.
     * @internal
     */
    setDeleteStyle(enable: boolean): void;
    /**
     * Get the colour of a block.
     *
     * @returns #RRGGBB string.
     */
    getColour(): string;
    /**
     * Change the colour of a block.
     *
     * @param colour HSV hue value, or #RRGGBB string.
     */
    setColour(colour: number | string): void;
    /**
     * Set the style and colour values of a block.
     *
     * @param blockStyleName Name of the block style.
     * @throws {Error} if the block style does not exist.
     */
    setStyle(blockStyleName: string): void;
    /**
     * Returns the BlockStyle object used to style this block.
     *
     * @returns This block's style object.
     */
    getStyle(): BlockStyle;
    /**
     * Move this block to the front of the visible workspace.
     * <g> tags do not respect z-index so SVG renders them in the
     * order that they are in the DOM.  By placing this block first within the
     * block group's <g>, it will render on top of any other blocks.
     * Use sparingly, this method is expensive because it reorders the DOM
     * nodes.
     *
     * @param blockOnly True to only move this block to the front without
     *     adjusting its parents.
     */
    bringToFront(blockOnly?: boolean): void;
    /**
     * Set whether this block can chain onto the bottom of another block.
     *
     * @param newBoolean True if there can be a previous statement.
     * @param opt_check Statement type or list of statement types.  Null/undefined
     *     if any type could be connected.
     */
    setPreviousStatement(newBoolean: boolean, opt_check?: string | string[] | null): void;
    /**
     * Set whether another block can chain onto the bottom of this block.
     *
     * @param newBoolean True if there can be a next statement.
     * @param opt_check Statement type or list of statement types.  Null/undefined
     *     if any type could be connected.
     */
    setNextStatement(newBoolean: boolean, opt_check?: string | string[] | null): void;
    /**
     * Set whether this block returns a value.
     *
     * @param newBoolean True if there is an output.
     * @param opt_check Returned type or list of returned types.  Null or
     *     undefined if any type could be returned (e.g. variable get).
     */
    setOutput(newBoolean: boolean, opt_check?: string | string[] | null): void;
    /**
     * Set whether value inputs are arranged horizontally or vertically.
     *
     * @param newBoolean True if inputs are horizontal.
     */
    setInputsInline(newBoolean: boolean): void;
    /**
     * Remove an input from this block.
     *
     * @param name The name of the input.
     * @param opt_quiet True to prevent error if input is not present.
     * @returns True if operation succeeds, false if input is not present and
     *     opt_quiet is true
     * @throws {Error} if the input is not present and opt_quiet is not true.
     */
    removeInput(name: string, opt_quiet?: boolean): boolean;
    /**
     * Move a numbered input to a different location on this block.
     *
     * @param inputIndex Index of the input to move.
     * @param refIndex Index of input that should be after the moved input.
     */
    moveNumberedInputBefore(inputIndex: number, refIndex: number): void;
    /** @override */
    appendInput(input: Input): Input;
    /**
     * Sets whether this block's connections are tracked in the database or not.
     *
     * Used by the deserializer to be more efficient. Setting a connection's
     * tracked_ value to false keeps it from adding itself to the db when it
     * gets its first moveTo call, saving expensive ops for later.
     *
     * @param track If true, start tracking. If false, stop tracking.
     * @internal
     */
    setConnectionTracking(track: boolean): void;
    /**
     * Returns connections originating from this block.
     *
     * @param all If true, return all connections even hidden ones.
     *     Otherwise, for a collapsed block don't return inputs connections.
     * @returns Array of connections.
     * @internal
     */
    getConnections_(all: boolean): RenderedConnection[];
    /**
     * Walks down a stack of blocks and finds the last next connection on the
     * stack.
     *
     * @param ignoreShadows If true,the last connection on a non-shadow block will
     *     be returned. If false, this will follow shadows to find the last
     *     connection.
     * @returns The last next connection on the stack, or null.
     * @internal
     */
    lastConnectionInStack(ignoreShadows: boolean): RenderedConnection | null;
    /**
     * Find the connection on this block that corresponds to the given connection
     * on the other block.
     * Used to match connections between a block and its insertion marker.
     *
     * @param otherBlock The other block to match against.
     * @param conn The other connection to match.
     * @returns The matching connection on this block, or null.
     * @internal
     */
    getMatchingConnection(otherBlock: Block, conn: Connection): RenderedConnection | null;
    /**
     * Create a connection of the specified type.
     *
     * @param type The type of the connection to create.
     * @returns A new connection of the specified type.
     * @internal
     */
    makeConnection_(type: ConnectionType): RenderedConnection;
    /**
     * Return the next statement block directly connected to this block.
     *
     * @returns The next statement block or null.
     */
    getNextBlock(): BlockSvg | null;
    /**
     * Returns the block connected to the previous connection.
     *
     * @returns The previous statement block or null.
     */
    getPreviousBlock(): BlockSvg | null;
    /**
     * Bumps unconnected blocks out of alignment.
     *
     * Two blocks which aren't actually connected should not coincidentally line
     * up on screen, because that creates confusion for end-users.
     */
    bumpNeighbours(): void;
    /**
     * Snap to grid, and then bump neighbouring blocks away at the end of the next
     * render.
     */
    scheduleSnapAndBump(): void;
    /**
     * Position a block so that it doesn't move the target block when connected.
     * The block to position is usually either the first block in a dragged stack
     * or an insertion marker.
     *
     * @param sourceConnection The connection on the moving block's stack.
     * @param originalOffsetToTarget The connection original offset to the target connection
     * @param originalOffsetInBlock The connection original offset in its block
     * @internal
     */
    positionNearConnection(sourceConnection: RenderedConnection, originalOffsetToTarget: {
        x: number;
        y: number;
    }, originalOffsetInBlock: Coordinate): void;
    /**
     * Find all the blocks that are directly nested inside this one.
     * Includes value and statement inputs, as well as any following statement.
     * Excludes any connection on an output tab or any preceding statement.
     * Blocks are optionally sorted by position; top to bottom.
     *
     * @param ordered Sort the list if true.
     * @returns Array of blocks.
     */
    getChildren(ordered: boolean): BlockSvg[];
    /**
     * Triggers a rerender after a delay to allow for batching.
     *
     * @returns A promise that resolves after the currently queued renders have
     *     been completed. Used for triggering other behavior that relies on
     *     updated size/position location for the block.
     * @internal
     */
    queueRender(): Promise<void>;
    /**
     * Immediately lays out and reflows a block based on its contents and
     * settings.
     */
    render(): void;
    /**
     * Renders this block in a way that's compatible with the more efficient
     * render management system.
     *
     * @internal
     */
    renderEfficiently(): void;
    /**
     * Tightens all children of this block so they are snuggly rendered against
     * their parent connections.
     *
     * Does not update connection locations, so that they can be updated more
     * efficiently by the render management system.
     *
     * @internal
     */
    tightenChildrenEfficiently(): void;
    /** Redraw any attached marker or cursor svgs if needed. */
    protected updateMarkers_(): void;
    /**
     * Add the cursor SVG to this block's SVG group.
     *
     * @param cursorSvg The SVG root of the cursor to be added to the block SVG
     *     group.
     * @internal
     */
    setCursorSvg(cursorSvg: SVGElement): void;
    /**
     * Add the marker SVG to this block's SVG group.
     *
     * @param markerSvg The SVG root of the marker to be added to the block SVG
     *     group.
     * @internal
     */
    setMarkerSvg(markerSvg: SVGElement): void;
    /**
     * Returns a bounding box describing the dimensions of this block
     * and any blocks stacked below it.
     *
     * @returns Object with height and width properties in workspace units.
     * @internal
     */
    getHeightWidth(): {
        height: number;
        width: number;
    };
    /**
     * Visual effect to show that if the dragging block is dropped, this block
     * will be replaced.  If a shadow block, it will disappear.  Otherwise it will
     * bump.
     *
     * @param add True if highlighting should be added.
     * @internal
     */
    fadeForReplacement(add: boolean): void;
    /**
     * Visual effect to show that if the dragging block is dropped it will connect
     * to this input.
     *
     * @param conn The connection on the input to highlight.
     * @param add True if highlighting should be added.
     * @internal
     */
    highlightShapeForInput(conn: RenderedConnection, add: boolean): void;
    /** Sets the drag strategy for this block. */
    setDragStrategy(dragStrategy: IDragStrategy): void;
    /** Returns whether this block is movable or not. */
    isMovable(): boolean;
    /** Starts a drag on the block. */
    startDrag(e?: PointerEvent): void;
    /** Drags the block to the given location. */
    drag(newLoc: Coordinate, e?: PointerEvent): void;
    /** Ends the drag on the block. */
    endDrag(e?: PointerEvent): void;
    /** Moves the block back to where it was at the start of a drag. */
    revertDrag(): void;
    /**
     * Returns a representation of this block that can be displayed in a flyout.
     */
    toFlyoutInfo(): FlyoutItemInfo[];
}
//# sourceMappingURL=block_svg.d.ts.map