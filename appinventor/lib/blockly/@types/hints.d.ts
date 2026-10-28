/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Nudge the user to use unconstrained movement.
 *
 * @param workspace Workspace.
 * @param force Set to show it even if previously shown.
 */
export declare function showUnconstrainedMoveHint(workspace: WorkspaceSvg, force?: boolean): void;
/**
 * Nudge the user to move a block that's in move mode.
 *
 * @param workspace Workspace.
 */
export declare function showConstrainedMovementHint(workspace: WorkspaceSvg): void;
/**
 * Clear active move-related hints, if any.
 *
 * @param workspace The workspace.
 */
export declare function clearMoveHints(workspace: WorkspaceSvg): void;
/**
 * Nudge the user to open the help.
 *
 * @param workspace The workspace.
 */
export declare function showHelpHint(workspace: WorkspaceSvg): void;
/**
 * Tell the user how to navigate inside blocks.
 *
 * @param workspace The workspace.
 */
export declare function showBlockNavigationHint(workspace: WorkspaceSvg): void;
/**
 * Tell the user how to navigate inside the workspace.
 *
 * @param workspace The workspace.
 */
export declare function showWorkspaceNavigationHint(workspace: WorkspaceSvg): void;
/**
 * Tell the user how to navigate away from a flyout label (heading) when they
 * try to act on it. Labels are not interactive, so direct them to use the
 * arrow keys to reach a block or the next-heading shortcut to skip ahead.
 *
 * @param workspace The workspace.
 */
export declare function showFlyoutLabelActionHint(workspace: WorkspaceSvg): void;
/**
 * Nudge the user to paste after a copy.
 *
 * @param workspace Workspace.
 */
export declare function showCopiedHint(workspace: WorkspaceSvg): void;
/**
 * Nudge the user to paste after a cut.
 *
 * @param workspace Workspace.
 */
export declare function showCutHint(workspace: WorkspaceSvg): void;
/**
 * Clear active paste-related hints, if any.
 *
 * @param workspace The workspace.
 */
export declare function clearPasteHints(workspace: WorkspaceSvg): void;
/**
 * Inform the user about screenreader optimization mode being toggled, and how
 * to undo it.
 *
 * @param workspace The workspace where screenreader mode was toggled.
 * @param enabled True if screenreader mode is now enabled, otherwise false.
 */
export declare function showScreenreaderModeHint(workspace: WorkspaceSvg, enabled: boolean): void;
//# sourceMappingURL=hints.d.ts.map