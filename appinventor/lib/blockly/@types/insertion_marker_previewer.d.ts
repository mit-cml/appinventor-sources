/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from './block_svg.js';
import { IConnectionPreviewer } from './interfaces/i_connection_previewer.js';
import { RenderedConnection } from './rendered_connection.js';
export declare class InsertionMarkerPreviewer implements IConnectionPreviewer {
    private readonly workspace;
    private fadedBlock;
    private markerConn;
    private draggedConn;
    private staticConn;
    constructor(draggedBlock: BlockSvg);
    /**
     * Display a connection preview where the draggedCon connects to the
     * staticCon, replacing the replacedBlock (currently connected to the
     * staticCon).
     *
     * @param draggedConn The connection on the block stack being dragged.
     * @param staticConn The connection not being dragged that we are
     *     connecting to.
     * @param replacedBlock The block currently connected to the staticCon that
     *     is being replaced.
     */
    previewReplacement(draggedConn: RenderedConnection, staticConn: RenderedConnection, replacedBlock: BlockSvg): void;
    /**
     * Display a connection preview where the draggedCon connects to the
     * staticCon, and no block is being relaced.
     *
     * @param draggedConn The connection on the block stack being dragged.
     * @param staticConn The connection not being dragged that we are
     *     connecting to.
     */
    previewConnection(draggedConn: RenderedConnection, staticConn: RenderedConnection): void;
    private shouldUseMarkerPreview;
    private previewMarker;
    private createInsertionMarker;
    /**
     * Gets the connection on the marker block that matches the original
     * connection on the original block.
     *
     * @param orig The original block.
     * @param marker The marker block (where we want to find the matching
     *     connection).
     * @param origConn The original connection.
     */
    private getMatchingConnection;
    /** Hide any previews that are currently displayed. */
    hidePreview(): void;
    private hideInsertionMarker;
    /** Dispose of any references held by this connection previewer. */
    dispose(): void;
}
//# sourceMappingURL=insertion_marker_previewer.d.ts.map