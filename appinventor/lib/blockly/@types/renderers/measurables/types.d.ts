/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { Measurable } from './base.js';
import type { Row } from './row.js';
/**
 * Types of rendering elements.
 */
declare class TypesContainer {
    [index: string]: number | Function;
    NONE: number;
    FIELD: number;
    HAT: number;
    ICON: number;
    SPACER: number;
    BETWEEN_ROW_SPACER: number;
    IN_ROW_SPACER: number;
    EXTERNAL_VALUE_INPUT: number;
    INPUT: number;
    INLINE_INPUT: number;
    STATEMENT_INPUT: number;
    CONNECTION: number;
    PREVIOUS_CONNECTION: number;
    NEXT_CONNECTION: number;
    OUTPUT_CONNECTION: number;
    CORNER: number;
    LEFT_SQUARE_CORNER: number;
    LEFT_ROUND_CORNER: number;
    RIGHT_SQUARE_CORNER: number;
    RIGHT_ROUND_CORNER: number;
    JAGGED_EDGE: number;
    ROW: number;
    TOP_ROW: number;
    BOTTOM_ROW: number;
    INPUT_ROW: number;
    /**
     * A Left Corner Union Type.
     */
    LEFT_CORNER: number;
    /**
     * A Right Corner Union Type.
     */
    RIGHT_CORNER: number;
    /**
     * Next flag value to use for custom rendering element types.
     * This must be updated to reflect the next enum flag value
     * to use if additional elements are added to
     * `Types`.
     */
    nextTypeValue_: number;
    /**
     * Get the enum flag value of an existing type or register a new type.
     *
     * @param type The name of the type.
     * @returns The enum flag value associated with that type.
     */
    getType(type: string): number;
    /**
     * Whether a measurable stores information about a field.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a field.
     */
    isField(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a hat.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a hat.
     */
    isHat(elem: Measurable): number;
    /**
     * Whether a measurable stores information about an icon.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about an icon.
     */
    isIcon(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a spacer.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a spacer.
     */
    isSpacer(elem: Measurable | Row): number;
    /**
     * Whether a measurable stores information about an in-row spacer.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about an in-row spacer.
     */
    isInRowSpacer(elem: Measurable): number;
    /**
     * Whether a measurable stores information about an input.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about an input.
     */
    isInput(elem: Measurable): number;
    /**
     * Whether a measurable stores information about an external input.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about an external input.
     */
    isExternalInput(elem: Measurable): number;
    /**
     * Whether a measurable stores information about an inline input.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about an inline input.
     */
    isInlineInput(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a statement input.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a statement input.
     */
    isStatementInput(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a previous connection.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a previous connection.
     */
    isPreviousConnection(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a next connection.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a next connection.
     */
    isNextConnection(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a previous or next
     * connection.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a previous or next
     *     connection.
     */
    isPreviousOrNextConnection(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a left round corner.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a left round corner.
     */
    isLeftRoundedCorner(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a right round corner.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a right round corner.
     */
    isRightRoundedCorner(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a left square corner.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a left square corner.
     */
    isLeftSquareCorner(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a right square corner.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a right square corner.
     */
    isRightSquareCorner(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a corner.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a corner.
     */
    isCorner(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a jagged edge.
     *
     * @param elem The element to check.
     * @returns 1 if the object stores information about a jagged edge.
     */
    isJaggedEdge(elem: Measurable): number;
    /**
     * Whether a measurable stores information about a row.
     *
     * @param row The row to check.
     * @returns 1 if the object stores information about a row.
     */
    isRow(row: Row): number;
    /**
     * Whether a measurable stores information about a between-row spacer.
     *
     * @param row The row to check.
     * @returns 1 if the object stores information about a between-row spacer.
     */
    isBetweenRowSpacer(row: Row): number;
    /**
     * Whether a measurable stores information about a top row.
     *
     * @param row The row to check.
     * @returns 1 if the object stores information about a top row.
     */
    isTopRow(row: Row): number;
    /**
     * Whether a measurable stores information about a bottom row.
     *
     * @param row The row to check.
     * @returns 1 if the object stores information about a bottom row.
     */
    isBottomRow(row: Row): number;
    /**
     * Whether a measurable stores information about a top or bottom row.
     *
     * @param row The row to check.
     * @returns 1 if the object stores information about a top or bottom row.
     */
    isTopOrBottomRow(row: Row): number;
    /**
     * Whether a measurable stores information about an input row.
     *
     * @param row The row to check.
     * @returns 1 if the object stores information about an input row.
     */
    isInputRow(row: Row): number;
}
export declare const Types: TypesContainer;
export {};
//# sourceMappingURL=types.d.ts.map