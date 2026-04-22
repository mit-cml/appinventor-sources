/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IFocusableNode } from './interfaces/i_focusable_node.js';
import { IRenderedElement } from './interfaces/i_rendered_element.js';
import { Coordinate } from './utils/coordinate.js';
import { WorkspaceSvg } from './workspace_svg.js';
/** @internal */
export declare class LayerManager {
    private workspace;
    /** The layer elements being dragged are appended to. */
    private dragLayer;
    /** The layer elements being animated are appended to. */
    private animationLayer;
    /** The layers elements not being dragged are appended to.  */
    private layers;
    /** @internal */
    constructor(workspace: WorkspaceSvg);
    private createDragLayer;
    private createAnimationLayer;
    /**
     * Appends the element to the animation layer. The animation layer doesn't
     * move when the workspace moves, so e.g. delete animations don't move
     * when a block delete triggers a workspace resize.
     *
     * @internal
     */
    appendToAnimationLayer(elem: IRenderedElement): void;
    /**
     * Translates layers when the workspace is dragged or zoomed.
     *
     * @internal
     */
    translateLayers(newCoord: Coordinate, newScale: number): void;
    /**
     * Moves the given element to the drag layer, which exists on top of all other
     * layers, and the drag surface.
     *
     * @param elem The element to move onto the drag layer.
     * @param focus Whether or not to focus the element post-move.
     *
     * @internal
     */
    moveToDragLayer(elem: IRenderedElement & IFocusableNode, focus?: boolean): void;
    /**
     * Moves the given element off of the drag layer.
     *
     * @param elem The element to move off of the drag layer.
     * @param layerNum The identifier of the layer to move the element onto.
     *     Should be a constant from layers.ts.
     * @param focus Whether or not the element should be focused once moved onto
     *     the destination layer.
     *
     * @internal
     */
    moveOffDragLayer(elem: IRenderedElement & IFocusableNode, layerNum: number, focus?: boolean): void;
    /**
     * Appends the given element to a layer. If the layer does not exist, it is
     * created.
     *
     * @internal
     */
    append(elem: IRenderedElement, layerNum: number): void;
    /**
     * Creates a layer and inserts it at the proper place given the layer number.
     *
     * More positive layers exist later in the dom and are rendered ontop of
     * less positive layers. Layers are added to the layer map as a side effect.
     */
    private createLayer;
    /**
     * Returns true if the given element is a layer managed by the layer manager.
     * False otherwise.
     *
     * @internal
     */
    hasLayer(elem: SVGElement): boolean;
    /**
     * We need to be able to access this layer explicitly for backwards
     * compatibility.
     *
     * @internal
     */
    getBlockLayer(): SVGGElement;
    /**
     * We need to be able to access this layer explicitly for backwards
     * compatibility.
     *
     * @internal
     */
    getBubbleLayer(): SVGGElement;
    /**
     * Returns the drag layer.
     *
     * @internal
     */
    getDragLayer(): SVGGElement | undefined;
}
//# sourceMappingURL=layer_manager.d.ts.map