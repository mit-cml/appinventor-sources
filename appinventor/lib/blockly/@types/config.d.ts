/**
 * @license
 * Copyright 2022 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Object holding all the values on Blockly that we expect developers to be
 * able to change.
 */
export declare const config: {
    /**
     * Number of pixels the mouse must move before a drag starts.
     *
     */
    dragRadius: number;
    /**
     * Number of pixels the mouse must move before a drag/scroll starts from the
     * flyout.  Because the drag-intention is determined when this is reached, it
     * is larger than dragRadius so that the drag-direction is clearer.
     *
     */
    flyoutDragRadius: number;
    /**
     * Maximum misalignment between connections for them to snap together.
     * Default is 28.
     *
     */
    snapRadius: number;
    /**
     * Maximum misalignment between connections for them to snap together.
     * This should be the same as the snap radius.
     * Default is 28.
     *
     */
    connectingSnapRadius: number;
    /**
     * How much to prefer staying connected to the current connection over moving
     * to a new connection.  The current previewed connection is considered to be
     * this much closer to the matching connection on the block than it actually
     * is.
     *
     */
    currentConnectionPreference: number;
    /**
     * Delay in ms between trigger and bumping unconnected block out of alignment.
     *
     */
    bumpDelay: number;
};
//# sourceMappingURL=config.d.ts.map