/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceSvg } from '../workspace_svg.js';
/**
 * Four-way arrow indicator attached to a workspace element to indicate that it
 * is being moved.
 */
export declare class MoveIndicator {
    private workspace;
    /**
     * Root SVG element for the indicator.
     */
    svgRoot: SVGGElement;
    /**
     * Creates a new move indicator.
     *
     * @param workspace The workspace the indicator should be displayed on.
     */
    constructor(workspace: WorkspaceSvg);
    /**
     * Moves this indicator to the specified location.
     *
     * @param x The location on the X axis to move to.
     * @param y The location on the Y axis to move to.
     */
    moveTo(x: number, y: number): void;
    /**
     * Disposes of this move indicator.
     */
    dispose(): void;
}
//# sourceMappingURL=move_indicator.d.ts.map