/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { IPaster } from '../interfaces/i_paster.js';
import { ICopyData } from '../interfaces/i_copyable.js';
import { Coordinate } from '../utils/coordinate.js';
import { WorkspaceSvg } from '../workspace_svg.js';
import { WorkspaceCommentSvg } from '../workspace_comment_svg.js';
export declare class WorkspaceCommentPaster implements IPaster<WorkspaceCommentCopyData, WorkspaceCommentSvg> {
    static TYPE: string;
    paste(copyData: WorkspaceCommentCopyData, workspace: WorkspaceSvg, coordinate?: Coordinate): WorkspaceCommentSvg;
}
export interface WorkspaceCommentCopyData extends ICopyData {
    commentState: Element;
}
//# sourceMappingURL=workspace_comment_paster.d.ts.map