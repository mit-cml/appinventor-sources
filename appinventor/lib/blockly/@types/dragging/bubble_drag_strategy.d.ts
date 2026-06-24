/**
 * @license
 * Copyright 2024 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IBubble, WorkspaceSvg } from '../blockly.js';
import { IDragStrategy } from '../interfaces/i_draggable.js';
import { Coordinate } from '../utils.js';
export declare class BubbleDragStrategy implements IDragStrategy {
    private bubble;
    private workspace;
    private startLoc;
    constructor(bubble: IBubble, workspace: WorkspaceSvg);
    isMovable(): boolean;
    startDrag(): void;
    drag(newLoc: Coordinate): void;
    endDrag(): void;
    revertDrag(): void;
}
//# sourceMappingURL=bubble_drag_strategy.d.ts.map