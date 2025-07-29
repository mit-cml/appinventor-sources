/**
 * @license
 * Copyright 2016 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Components for creating connections between blocks.
 *
 * @class
 */
import type { Block } from './block.js';
import type { BlockSvg } from './block_svg.js';
import { Connection } from './connection.js';
import { Coordinate } from './utils/coordinate.js';
/**
 * Class for a connection between blocks that may be rendered on screen.
 */
export declare class RenderedConnection extends Connection {
    sourceBlock_: BlockSvg;
    private readonly db;
    private readonly dbOpposite;
    private readonly offsetInBlock;
    private trackedState;
    private highlighted;
    /** Connection this connection connects to.  Null if not connected. */
    targetConnection: RenderedConnection | null;
    /**
     * @param source The block establishing this connection.
     * @param type The type of the connection.
     */
    constructor(source: BlockSvg, type: number);
    /**
     * Dispose of this connection. Remove it from the database (if it is
     * tracked) and call the super-function to deal with connected blocks.
     *
     * @internal
     */
    dispose(): void;
    /**
     * Get the source block for this connection.
     *
     * @returns The source block.
     */
    getSourceBlock(): BlockSvg;
    /**
     * Returns the block that this connection connects to.
     *
     * @returns The connected block or null if none is connected.
     */
    targetBlock(): BlockSvg | null;
    /**
     * Returns the distance between this connection and another connection in
     * workspace units.
     *
     * @param otherConnection The other connection to measure the distance to.
     * @returns The distance between connections, in workspace units.
     */
    distanceFrom(otherConnection: Connection): number;
    /**
     * Move the block(s) belonging to the connection to a point where they don't
     * visually interfere with the specified connection.
     *
     * @param superiorConnection The connection to move away from. The provided
     *     connection should be the superior connection and should not be
     *     connected to this connection.
     * @param initiatedByThis Whether or not the block group that was manipulated
     *     recently causing bump checks is associated with the inferior
     *     connection. Defaults to false.
     * @internal
     */
    bumpAwayFrom(superiorConnection: RenderedConnection, initiatedByThis?: boolean): void;
    /**
     * Change the connection's coordinates.
     *
     * @param x New absolute x coordinate, in workspace coordinates.
     * @param y New absolute y coordinate, in workspace coordinates.
     * @returns True if the position of the connection in the connection db
     *     was updated.
     */
    moveTo(x: number, y: number): boolean;
    /**
     * Change the connection's coordinates.
     *
     * @param dx Change to x coordinate, in workspace units.
     * @param dy Change to y coordinate, in workspace units.
     * @returns True if the position of the connection in the connection db
     *     was updated.
     */
    moveBy(dx: number, dy: number): boolean;
    /**
     * Move this connection to the location given by its offset within the block
     * and the location of the block's top left corner.
     *
     * @param blockTL The location of the top left corner of the block, in
     *     workspace coordinates.
     * @returns True if the position of the connection in the connection db
     *     was updated.
     */
    moveToOffset(blockTL: Coordinate): boolean;
    /**
     * Set the offset of this connection relative to the top left of its block.
     *
     * @param x The new relative x, in workspace units.
     * @param y The new relative y, in workspace units.
     */
    setOffsetInBlock(x: number, y: number): void;
    /**
     * Get the offset of this connection relative to the top left of its block.
     *
     * @returns The offset of the connection.
     */
    getOffsetInBlock(): Coordinate;
    /**
     * Moves the blocks on either side of this connection right next to
     * each other, based on their local offsets, not global positions.
     *
     * @internal
     */
    tightenEfficiently(): void;
    /**
     * Find the closest compatible connection to this connection.
     * All parameters are in workspace units.
     *
     * @param maxLimit The maximum radius to another connection.
     * @param dxy Offset between this connection's location in the database and
     *     the current location (as a result of dragging).
     * @returns Contains two properties: 'connection' which is either another
     *     connection or null, and 'radius' which is the distance.
     */
    closest(maxLimit: number, dxy: Coordinate): {
        connection: RenderedConnection | null;
        radius: number;
    };
    /** Add highlighting around this connection. */
    highlight(): void;
    /** Remove the highlighting around this connection. */
    unhighlight(): void;
    /** Returns true if this connection is highlighted, false otherwise. */
    isHighlighted(): boolean;
    /**
     * Set whether this connections is tracked in the database or not.
     *
     * @param doTracking If true, start tracking. If false, stop tracking.
     * @internal
     */
    setTracking(doTracking: boolean): void;
    /**
     * Stop tracking this connection, as well as all down-stream connections on
     * any block attached to this connection. This happens when a block is
     * collapsed.
     *
     * Also closes down-stream icons/bubbles.
     *
     * @internal
     */
    stopTrackingAll(): void;
    /**
     * Start tracking this connection, as well as all down-stream connections on
     * any block attached to this connection. This happens when a block is
     * expanded.
     *
     * @returns List of blocks to render.
     */
    startTrackingAll(): Block[];
    /**
     * Behaviour after a connection attempt fails.
     * Bumps this connection away from the other connection. Called when an
     * attempted connection fails.
     *
     * @param superiorConnection Connection that this connection failed to connect
     *     to. The provided connection should be the superior connection.
     * @internal
     */
    onFailedConnect(superiorConnection: Connection): void;
    /**
     * Disconnect two blocks that are connected by this connection.
     *
     * @param setParent Whether to set the parent of the disconnected block or
     *     not, defaults to true.
     *     If you do not set the parent, ensure that a subsequent action does,
     *     otherwise the view and model will be out of sync.
     */
    disconnectInternal(setParent?: boolean): void;
    /**
     * Respawn the shadow block if there was one connected to the this connection.
     * Render/rerender blocks as needed.
     */
    protected respawnShadow_(): void;
    /**
     * Find all nearby compatible connections to this connection.
     * Type checking does not apply, since this function is used for bumping.
     *
     * @param maxLimit The maximum radius to another connection, in workspace
     *     units.
     * @returns List of connections.
     * @internal
     */
    neighbours(maxLimit: number): RenderedConnection[];
    /**
     * Connect two connections together.  This is the connection on the superior
     * block.  Rerender blocks as needed.
     *
     * @param childConnection Connection on inferior block.
     */
    protected connect_(childConnection: Connection): void;
    /**
     * Function to be called when this connection's compatible types have changed.
     */
    protected onCheckChanged_(): void;
    /**
     * Change a connection's compatibility.
     * Rerender blocks as needed.
     *
     * @param check Compatible value type or list of value types. Null if all
     *     types are compatible.
     * @returns The connection being modified (to allow chaining).
     */
    setCheck(check: string | string[] | null): RenderedConnection;
}
export declare namespace RenderedConnection {
    /**
     * Enum for different kinds of tracked states.
     *
     * WILL_TRACK means that this connection will add itself to
     * the db on the next moveTo call it receives.
     *
     * UNTRACKED means that this connection will not add
     * itself to the database until setTracking(true) is explicitly called.
     *
     * TRACKED means that this connection is currently being tracked.
     */
    enum TrackedState {
        WILL_TRACK = -1,
        UNTRACKED = 0,
        TRACKED = 1
    }
}
export type TrackedState = RenderedConnection.TrackedState;
export declare const TrackedState: typeof RenderedConnection.TrackedState;
//# sourceMappingURL=rendered_connection.d.ts.map