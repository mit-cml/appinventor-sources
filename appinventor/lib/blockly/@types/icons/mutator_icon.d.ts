/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from '../block_svg.js';
import type { Abstract } from '../events/events_abstract.js';
import type { IHasBubble } from '../interfaces/i_has_bubble.js';
import { Coordinate } from '../utils/coordinate.js';
import { Size } from '../utils/size.js';
import type { WorkspaceSvg } from '../workspace_svg.js';
import { Icon } from './icon.js';
import { IconType } from './icon_types.js';
/**
 * An icon that allows the user to change the shape of the block.
 *
 * For example, it could be used to add additional fields or inputs to
 * the block.
 */
export declare class MutatorIcon extends Icon implements IHasBubble {
    private readonly flyoutBlockTypes;
    protected readonly sourceBlock: BlockSvg;
    /** The type string used to identify this icon. */
    static readonly TYPE: IconType<MutatorIcon>;
    /**
     * The weight this icon has relative to other icons. Icons with more positive
     * weight values are rendered farther toward the end of the block.
     */
    static readonly WEIGHT = 1;
    /** The bubble used to show the mini workspace to the user. */
    private miniWorkspaceBubble;
    /** The root block in the mini workspace. */
    private rootBlock;
    /** The PID tracking updating the workkspace in response to user events. */
    private updateWorkspacePid;
    /**
     * The change listener in the main workspace that triggers the saveConnections
     * method when anything in the main workspace changes.
     *
     * Only actually registered to listen for events while the mutator bubble is
     * open.
     */
    private saveConnectionsListener;
    constructor(flyoutBlockTypes: string[], sourceBlock: BlockSvg);
    getType(): IconType<MutatorIcon>;
    initView(pointerdownListener: (e: PointerEvent) => void): void;
    dispose(): void;
    getWeight(): number;
    getSize(): Size;
    applyColour(): void;
    updateCollapsed(): void;
    onLocationChange(blockOrigin: Coordinate): void;
    onClick(): void;
    isClickableInFlyout(): boolean;
    bubbleIsVisible(): boolean;
    setBubbleVisible(visible: boolean): Promise<void>;
    /** @returns the configuration the mini workspace should have. */
    private getMiniWorkspaceConfig;
    /**
     * @returns the location the bubble should be anchored to.
     *     I.E. the middle of this icon.
     */
    private getAnchorLocation;
    /**
     * @returns the rect the bubble should avoid overlapping.
     *     I.E. the block that owns this icon.
     */
    private getBubbleOwnerRect;
    /** Decomposes the source block to create blocks in the mini workspace. */
    private createRootBlock;
    /** Adds a listen to the source block that triggers saving connections. */
    private addSaveConnectionsListener;
    /**
     * Creates a change listener to add to the mini workspace which recomposes
     * the block.
     */
    private createMiniWorkspaceChangeListener;
    /**
     * Returns true if the given event is not one the mutator needs to
     * care about.
     *
     * @internal
     */
    static isIgnorableMutatorEvent(e: Abstract): boolean;
    /** Recomposes the source block based on changes to the mini workspace. */
    private recomposeSourceBlock;
    /**
     * @returns The workspace of the mini workspace bubble, if the bubble is
     *     currently open.
     */
    getWorkspace(): WorkspaceSvg | undefined;
}
//# sourceMappingURL=mutator_icon.d.ts.map