/**
 * @license
 * Copyright 2023 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Legacy means of representing a procedure signature. The elements are
 * respectively: name, parameter names, and whether it has a return value.
 */
export type ProcedureTuple = [string, string[], boolean];
/**
 * Procedure block type.
 *
 * @internal
 */
export interface ProcedureBlock {
    getProcedureCall: () => string;
    renameProcedure: (p1: string, p2: string) => void;
    getProcedureDef: () => ProcedureTuple;
}
/** @internal */
export interface LegacyProcedureDefBlock {
    getProcedureDef: () => ProcedureTuple;
}
/** @internal */
export declare function isLegacyProcedureDefBlock(block: Object): block is LegacyProcedureDefBlock;
/** @internal */
export interface LegacyProcedureCallBlock {
    getProcedureCall: () => string;
    renameProcedure: (p1: string, p2: string) => void;
}
/** @internal */
export declare function isLegacyProcedureCallBlock(block: Object): block is LegacyProcedureCallBlock;
//# sourceMappingURL=i_legacy_procedure_blocks.d.ts.map