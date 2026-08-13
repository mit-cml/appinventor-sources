/**
 * @license
 * Copyright 2026 Raspberry Pi Foundation
 * SPDX-License-Identifier: Apache-2.0
 */
import type { BlockSvg } from './block_svg.js';
import type { Input } from './inputs/input.js';
import { RenderedConnection } from './rendered_connection.js';
import { Verbosity } from './utils/aria.js';
/**
 * Prepositions to use when describing the relationship between two blocks based
 * on their connection types.
 */
export declare enum ConnectionPreposition {
    UNKNOWN = 0,
    BEFORE = 1,
    AFTER = 2,
    AROUND = 3,
    INSIDE = 4,
    TO = 5
}
/**
 * Returns an ARIA representation of the specified block.
 *
 * The returned label will contain a complete context of the block, including:
 * - Whether it begins a block stack or statement input stack.
 * - Its constituent editable and non-editable fields.
 * - Properties, including: disabled, collapsed, replaceable (a shadow), etc.
 * - Its parent toolbox category.
 * - Whether it has inputs.
 *
 * Beyond this, the returned label is specifically assembled with commas in
 * select locations with the intention of better 'prosody' in the screen reader
 * readouts since there's a lot of information being shared with the user. The
 * returned label also places more important information earlier in the label so
 * that the user gets the most important context as soon as possible in case
 * they wish to stop readout early.
 *
 * The returned label will be specialized based on whether the block is part of a
 * flyout.
 *
 * Custom input labels (from {@link Input.setAriaLabelProvider}) are not included
 * here; they are used only in move-mode disambiguation and parent-input context
 * via {@link Input.getAriaLabelText}.
 *
 * @internal
 * @param block The block for which an ARIA representation should be created.
 * @param verbosity How much detail to include in the description.
 * @param fullBlockFieldLabel An optional override for input labels for full-block fields
 * @returns The ARIA representation for the specified block.
 */
export declare function computeAriaLabel(block: BlockSvg, verbosity?: Verbosity, fullBlockFieldLabel?: string | undefined): string;
/**
 * Sets the ARIA role and role description for the specified block, accounting
 * for whether the block is part of a flyout.
 *
 * @internal
 * @param block The block to set ARIA role and roledescription attributes on.
 */
export declare function configureAriaRole(block: BlockSvg): void;
/**
 * Returns a list of ARIA labels for the 'field row' for the specified Input.
 *
 * 'Field row' essentially means the horizontal run of readable fields that
 * precede the Input. Together, these provide the domain context for the input,
 * particularly in the context of connections. In some cases, there may not be
 * any readable fields immediately prior to the Input. In that case, if the
 * `lookback` attribute is specified, all of the fields on the row immediately
 * above the Input will be used instead.
 *
 * If the input contains multiple adjacent FieldLabel fields, they will be
 * combined together into a singular label string so that screenreaders can
 * know to read them together as one piece of text.
 *
 * Empty field labels are excluded because they don't provide useful context.
 * Fields should generally have a helpful label, but there are exceptions, such
 * as when empty label fields are used to control the layout of a block.
 *
 * @internal
 * @param input The Input to compute a description/context label for.
 * @param lookback If true, will use labels for fields on the previous row if
 *     the given input's row has no fields itself.
 * @returns A list of labels for fields on the same row (or previous row, if
 *     lookback is specified) as the given input.
 */
export declare function computeFieldRowLabel(input: Input, lookback: boolean, verbosity?: Verbosity): string[];
/**
 * Returns text indicating that a block is the root block of a stack.
 *
 * @internal
 * @param block The block to retrieve a label for.
 * @returns Text indicating that the block begins a stack, or undefined if it
 *     does not.
 */
export declare function getBeginStackLabel(block: BlockSvg): string | undefined;
/**
 * Returns a list of accessibility labels for fields and inputs on a block.
 * Each entry in the returned array corresponds to one of: (a) a label for a
 * continuous run of non-interactable fields, (b) a label for an editable field,
 * (c) a label for an input. When an input contains nested blocks/fields/inputs,
 * their contents are returned as a single item in the array per top-level
 * input.
 *
 * Uses derived labels only (field row text and connected block content via
 * {@link Input.getLabel}). Custom input labels are not included; see
 * {@link Input.getAriaLabelText} for move-mode and parent-input usage.
 *
 * @internal
 * @param block The block to retrieve a list of field/input labels for.
 * @param verbosity How much detail to include in each input label.
 * @param fullBlockFieldLabel An optional override for full-block fields.
 * @returns A list of field/input labels for the given block.
 */
export declare function getInputLabels(block: BlockSvg, verbosity?: Verbosity, fullBlockFieldLabel?: string | undefined): string[];
/**
 * Returns a subset of derived labels for inputs on the given block, ending at
 * the specified input. Used to disambiguate move targets and connection
 * highlights when no custom label is set.
 *
 * The subset is determined based on the input type:
 * - For non-statement inputs, only the label for the given input is returned.
 * - For statement inputs, labels are collected from the start of the current
 *   statement section up to and including the given input. A statement section
 *   begins immediately after the previous statement input, or at the start of
 *   the block if none exists.
 *
 * Label resolution (see also {@link computeMoveConnectionLabel}):
 * 1. Custom labels ({@link Input.getAriaLabelText}) are handled by callers, not here.
 * 2. Derived labels from {@link Input.getLabel} (field row + child blocks).
 * 3. Numbered fallback ({@link Msg.INPUT_LABEL_INDEX}) when tier 2 is empty.
 *    For the statement target input, the fallback is omitted if any earlier
 *    input in the subset already produced a label.
 *
 * @internal
 * @param block The block to retrieve a list of field/input labels for.
 * @param endInput The input that defines the end of the subset.
 * @param includeEndInputChildren Whether to include labels for child blocks
 *    connected to the end input.
 * @returns A list of field/input labels for the given block.
 */
export declare function getInputLabelsSubset(block: BlockSvg, endInput: Input, includeEndInputChildren: boolean): string[];
/**
 * Returns a translated string describing an in-progress move of a block to a new
 * connection, suitable for announcement on the ARIA live region. The returned string
 * will be assembled based on the types of the local and neighbour connections and
 * the presence of any readable fields on the block's inputs. If multiple potential
 * candidate connections are present, additional context will be included in the
 * returned string to help disambiguate between them.
 *
 * @param local The moving side of the candidate connection pair
 * @param neighbour The target side of the candidate connection pair
 * @param disambiguationPolicy A function that determines whether it's useful to
 *     include parent input labels for disambiguation.
 * @param isMoveStart Whether this announcement is for the start of a move. If false,
 *     skip announcing the block label since it should have already been announced.
 */
export declare function computeMoveLabel(local: RenderedConnection, neighbour: RenderedConnection, disambiguationPolicy: (forLocal: boolean) => boolean, isMoveStart?: boolean): string;
//# sourceMappingURL=block_aria_composer.d.ts.map