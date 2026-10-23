/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { IBubble } from '../interfaces/i_bubble.js';
import type { IDragStrategy } from '../interfaces/i_draggable.js';
import type { Coordinate } from '../utils.js';
import type { WorkspaceSvg } from '../workspace_svg.js';
export declare class BubbleDragStrategy implements IDragStrategy {
    private bubble;
    private workspace;
    private startLoc;
    constructor(bubble: IBubble, workspace: WorkspaceSvg);
    isMovable(): boolean;
    startDrag(): IBubble;
    drag(newLoc: Coordinate): void;
    endDrag(): void;
    revertDrag(): void;
}
//# sourceMappingURL=bubble_drag_strategy.d.ts.map