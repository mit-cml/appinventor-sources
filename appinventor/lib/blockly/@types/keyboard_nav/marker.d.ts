/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * The class representing a marker.
 * Used primarily for keyboard navigation to show a marked location.
 *
 * @class
 */
import { BlockSvg } from '../block_svg.js';
import type { IFocusableNode } from '../interfaces/i_focusable_node.js';
/**
 * Class for a marker.
 * This is used in keyboard navigation to save a location in the Blockly AST.
 */
export declare class Marker {
    /** The colour of the marker. */
    colour: string | null;
    /** The current location of the marker. */
    protected curNode: IFocusableNode | null;
    /** The type of the marker. */
    type: string;
    /**
     * Gets the current location of the marker.
     *
     * @returns The current field, connection, or block the marker is on.
     */
    getCurNode(): IFocusableNode | null;
    /**
     * Set the location of the marker and call the update method.
     *
     * @param newNode The new location of the marker, or null to remove it.
     */
    setCurNode(newNode: IFocusableNode | null): void;
    /** Dispose of this marker. */
    dispose(): void;
    /**
     * Returns the block that the given node is a child of.
     *
     * @returns The parent block of the node if any, otherwise null.
     */
    getSourceBlockFromNode(node: IFocusableNode | null): BlockSvg | null;
    /**
     * Returns the block that this marker's current node is a child of.
     *
     * @returns The parent block of the marker's current node if any, otherwise
     *     null.
     */
    getSourceBlock(): BlockSvg | null;
}
//# sourceMappingURL=marker.d.ts.map