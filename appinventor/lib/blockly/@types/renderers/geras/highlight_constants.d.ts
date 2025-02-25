/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import type { ConstantProvider } from '../common/constants.js';
/** An object containing sizing and path information about an outside corner. */
export interface OutsideCorner {
    height: number;
    topLeft: (p1: boolean) => string;
    bottomLeft: () => string;
}
/** An object containing sizing and path information about an inside corner. */
export interface InsideCorner {
    width: number;
    height: number;
    pathTop: (p1: boolean) => string;
    pathBottom: (p1: boolean) => string;
}
/** An object containing sizing and path information about a start hat. */
export interface StartHat {
    path: (p1: boolean) => string;
}
/** An object containing sizing and path information about a notch. */
export interface Notch {
    pathLeft: string;
}
/** An object containing sizing and path information about a puzzle tab. */
export interface PuzzleTab {
    width: number;
    height: number;
    pathDown: (p1: boolean) => string;
    pathUp: (p1: boolean) => string;
}
/**
 * An object containing sizing and path information about collapsed block
 * indicators.
 */
export interface JaggedTeeth {
    height: number;
    width: number;
    pathLeft: string;
}
/**
 * An object that provides constants for rendering highlights on blocks.
 * Some highlights are simple offsets of the parent paths and can be generated
 * programmatically.  Others, especially on curves, are just made out of piles
 * of constants and are hard to tweak.
 */
export declare class HighlightConstantProvider {
    constantProvider: ConstantProvider;
    /** The offset between the block's main path and highlight path. */
    OFFSET: number;
    START_POINT: string;
    INSIDE_CORNER: InsideCorner;
    OUTSIDE_CORNER: OutsideCorner;
    PUZZLE_TAB: PuzzleTab;
    NOTCH: Notch;
    JAGGED_TEETH: JaggedTeeth;
    START_HAT: StartHat;
    /**
     * @param constants The rendering constants provider.
     */
    constructor(constants: ConstantProvider);
    /**
     * Initialize shape objects based on the constants set in the constructor.
     */
    init(): void;
    /**
     * @returns An object containing sizing and path information about inside
     *     corner highlights.
     */
    protected makeInsideCorner(): InsideCorner;
    /**
     * @returns An object containing sizing and path information about outside
     *     corner highlights.
     */
    protected makeOutsideCorner(): OutsideCorner;
    /**
     * @returns An object containing sizing and path information about puzzle tab
     *     highlights.
     */
    protected makePuzzleTab(): PuzzleTab;
    /**
     * @returns An object containing sizing and path information about notch
     *     highlights.
     */
    protected makeNotch(): Notch;
    /**
     * @returns An object containing sizing and path information about collapsed
     *     block edge highlights.
     */
    protected makeJaggedTeeth(): JaggedTeeth;
    /**
     * @returns An object containing sizing and path information about start
     *     highlights.
     */
    protected makeStartHat(): StartHat;
}
//# sourceMappingURL=highlight_constants.d.ts.map