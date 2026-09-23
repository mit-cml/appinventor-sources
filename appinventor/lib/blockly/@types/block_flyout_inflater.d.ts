/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { BlockSvg } from './block_svg.js';
import * as browserEvents from './browser_events.js';
import { FlyoutItem } from './flyout_item.js';
import type { IFlyout } from './interfaces/i_flyout.js';
import type { IFlyoutInflater } from './interfaces/i_flyout_inflater.js';
import type { BlockInfo } from './utils/toolbox.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class responsible for creating blocks for flyouts.
 */
export declare class BlockFlyoutInflater implements IFlyoutInflater {
    protected permanentlyDisabledBlocks: Set<BlockSvg>;
    protected listeners: Map<string, browserEvents.Data[]>;
    protected flyout?: IFlyout;
    private capacityWrapper;
    /**
     * Creates a new BlockFlyoutInflater instance.
     */
    constructor();
    /**
     * Inflates a flyout block from the given state and adds it to the flyout.
     *
     * @param state A JSON representation of a flyout block.
     * @param flyout The flyout to create the block on.
     * @returns A newly created block.
     */
    load(state: object, flyout: IFlyout): FlyoutItem;
    /**
     * Creates a block on the given workspace.
     *
     * @param blockDefinition A JSON representation of the block to create.
     * @param workspace The workspace to create the block on.
     * @returns The newly created block.
     */
    createBlock(blockDefinition: BlockInfo, workspace: WorkspaceSvg): BlockSvg;
    /**
     * Returns the amount of space that should follow this block.
     *
     * @param state A JSON representation of a flyout block.
     * @param defaultGap The default spacing for flyout items.
     * @returns The amount of space that should follow this block.
     */
    gapForItem(state: object, defaultGap: number): number;
    /**
     * Disposes of the given block.
     *
     * @param item The flyout block to dispose of.
     */
    disposeItem(item: FlyoutItem): void;
    /**
     * Removes event listeners for the block with the given ID.
     *
     * @param blockId The ID of the block to remove event listeners from.
     */
    protected removeListeners(blockId: string): void;
    /**
     * Updates this inflater's flyout.
     *
     * @param flyout The flyout that owns this inflater.
     */
    protected setFlyout(flyout: IFlyout): void;
    /**
     * Updates the enabled state of the given block based on the capacity of the
     * workspace.
     *
     * @param block The block to update the enabled/disabled state of.
     */
    private updateStateBasedOnCapacity;
    /**
     * Add listeners to a block that has been added to the flyout.
     *
     * @param block The block to add listeners for.
     */
    protected addBlockListeners(block: BlockSvg): void;
    /**
     * Updates the state of blocks in our owning flyout to be disabled/enabled
     * based on the capacity of the workspace for more blocks of that type.
     *
     * @param event The event that triggered this update.
     */
    private filterFlyoutBasedOnCapacity;
    /**
     * Returns the type of items this inflater is responsible for creating.
     *
     * @returns An identifier for the type of items this inflater creates.
     */
    getType(): string;
}
//# sourceMappingURL=block_flyout_inflater.d.ts.map