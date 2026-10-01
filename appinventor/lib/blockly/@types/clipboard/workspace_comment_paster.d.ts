/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { RenderedWorkspaceComment } from '../comments/rendered_workspace_comment.js';
import type { ICopyData } from '../interfaces/i_copyable.js';
import type { IPaster } from '../interfaces/i_paster.js';
import * as commentSerialiation from '../serialization/workspace_comments.js';
import { Coordinate } from '../utils/coordinate.js';
import type { WorkspaceSvg } from '../workspace_svg.js';
export declare class WorkspaceCommentPaster implements IPaster<WorkspaceCommentCopyData, RenderedWorkspaceComment> {
    static TYPE: string;
    paste(copyData: WorkspaceCommentCopyData, workspace: WorkspaceSvg, coordinate?: Coordinate): RenderedWorkspaceComment | null;
}
export interface WorkspaceCommentCopyData extends ICopyData {
    commentState: commentSerialiation.State;
}
//# sourceMappingURL=workspace_comment_paster.d.ts.map