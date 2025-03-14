/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Colour input field.
 *
 * @class
 */
import './events/events_block_change.js';
import { Field, FieldConfig, FieldValidator } from './field.js';
import { Size } from './utils/size.js';
/**
 * Class for a colour input field.
 */
export declare class FieldColour extends Field<string> {
    /**
     * An array of colour strings for the palette.
     * Copied from goog.ui.ColorPicker.SIMPLE_GRID_COLORS
     * All colour pickers use this unless overridden with setColours.
     */
    static COLOURS: string[];
    /**
     * An array of tooltip strings for the palette.  If not the same length as
     * COLOURS, the colour's hex code will be used for any missing titles.
     * All colour pickers use this unless overridden with setColours.
     */
    static TITLES: string[];
    /**
     * Number of columns in the palette.
     * All colour pickers use this unless overridden with setColumns.
     */
    static COLUMNS: number;
    /** The field's colour picker element. */
    private picker;
    /** Index of the currently highlighted element. */
    private highlightedIndex;
    /**
     * Array holding info needed to unbind events.
     * Used for disposing.
     * Ex: [[node, name, func], [node, name, func]].
     */
    private boundEvents;
    /**
     * Serializable fields are saved by the serializer, non-serializable fields
     * are not. Editable fields should also be serializable.
     */
    SERIALIZABLE: boolean;
    /** Mouse cursor style when over the hotspot that initiates the editor. */
    CURSOR: string;
    /**
     * Used to tell if the field needs to be rendered the next time the block is
     * rendered. Colour fields are statically sized, and only need to be
     * rendered at initialization.
     */
    protected isDirty_: boolean;
    /** Array of colours used by this field.  If null, use the global list. */
    private colours;
    /**
     * Array of colour tooltips used by this field.  If null, use the global
     * list.
     */
    private titles;
    /**
     * Number of colour columns used by this field.  If 0, use the global
     * setting. By default use the global constants for columns.
     */
    private columns;
    /**
     * @param value The initial value of the field. Should be in '#rrggbb'
     *     format. Defaults to the first value in the default colour array. Also
     *     accepts Field.SKIP_SETUP if you wish to skip setup (only used by
     *     subclasses that want to handle configuration and setting the field
     *     value after their own constructors have run).
     * @param validator A function that is called to validate changes to the
     *     field's value. Takes in a colour string & returns a validated colour
     *     string ('#rrggbb' format), or null to abort the change.
     * @param config A map of options used to configure the field.
     *     See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/colour}
     * for a list of properties this parameter supports.
     */
    constructor(value?: string | typeof Field.SKIP_SETUP, validator?: FieldColourValidator, config?: FieldColourConfig);
    /**
     * Configure the field based on the given map of options.
     *
     * @param config A map of options to configure the field based on.
     */
    protected configure_(config: FieldColourConfig): void;
    /**
     * Create the block UI for this colour field.
     */
    initView(): void;
    protected isFullBlockField(): boolean;
    /**
     * Updates text field to match the colour/style of the block.
     */
    applyColour(): void;
    /**
     * Returns the height and width of the field.
     *
     * This should *in general* be the only place render_ gets called from.
     *
     * @returns Height and width.
     */
    getSize(): Size;
    /**
     * Updates the colour of the block to reflect whether this is a full
     * block field or not.
     */
    protected render_(): void;
    /**
     * Updates the size of the field based on whether it is a full block field
     * or not.
     *
     * @param margin margin to use when positioning the field.
     */
    protected updateSize_(margin?: number): void;
    /**
     * Ensure that the input value is a valid colour.
     *
     * @param newValue The input value.
     * @returns A valid colour, or null if invalid.
     */
    protected doClassValidation_(newValue?: any): string | null;
    /**
     * Get the text for this field.  Used when the block is collapsed.
     *
     * @returns Text representing the value of this field.
     */
    getText(): string;
    /**
     * Set a custom colour grid for this field.
     *
     * @param colours Array of colours for this block, or null to use default
     *     (FieldColour.COLOURS).
     * @param titles Optional array of colour tooltips, or null to use default
     *     (FieldColour.TITLES).
     * @returns Returns itself (for method chaining).
     */
    setColours(colours: string[], titles?: string[]): FieldColour;
    /**
     * Set a custom grid size for this field.
     *
     * @param columns Number of columns for this block, or 0 to use default
     *     (FieldColour.COLUMNS).
     * @returns Returns itself (for method chaining).
     */
    setColumns(columns: number): FieldColour;
    /** Create and show the colour field's editor. */
    protected showEditor_(): void;
    /**
     * Handle a click on a colour cell.
     *
     * @param e Mouse event.
     */
    private onClick;
    /**
     * Handle a key down event. Navigate around the grid with the
     * arrow keys. Enter selects the highlighted colour.
     *
     * @param e Keyboard event.
     */
    private onKeyDown;
    /**
     * Move the currently highlighted position by dx and dy.
     *
     * @param dx Change of x.
     * @param dy Change of y.
     */
    private moveHighlightBy;
    /**
     * Handle a mouse move event. Highlight the hovered colour.
     *
     * @param e Mouse event.
     */
    private onMouseMove;
    /** Handle a mouse enter event. Focus the picker. */
    private onMouseEnter;
    /**
     * Handle a mouse leave event. Blur the picker and unhighlight
     * the currently highlighted colour.
     */
    private onMouseLeave;
    /**
     * Returns the currently highlighted item (if any).
     *
     * @returns Highlighted item (null if none).
     */
    private getHighlighted;
    /**
     * Update the currently highlighted cell.
     *
     * @param cell The new cell to highlight.
     * @param index The index of the new cell.
     */
    private setHighlightedCell;
    /** Create a colour picker dropdown editor. */
    private dropdownCreate;
    /** Disposes of events and DOM-references belonging to the colour editor. */
    private dropdownDispose;
    /**
     * Construct a FieldColour from a JSON arg object.
     *
     * @param options A JSON object with options (colour).
     * @returns The new field instance.
     * @nocollapse
     * @internal
     */
    static fromJson(options: FieldColourFromJsonConfig): FieldColour;
}
/**
 * Config options for the colour field.
 */
export interface FieldColourConfig extends FieldConfig {
    colourOptions?: string[];
    colourTitles?: string[];
    columns?: number;
}
/**
 * fromJson config options for the colour field.
 */
export interface FieldColourFromJsonConfig extends FieldColourConfig {
    colour?: string;
}
/**
 * A function that is called to validate changes to the field's value before
 * they are set.
 *
 * @see {@link https://developers.google.com/blockly/guides/create-custom-blocks/fields/validators#return_values}
 * @param newValue The value to be validated.
 * @returns One of three instructions for setting the new value: `T`, `null`,
 * or `undefined`.
 *
 * - `T` to set this function's returned value instead of `newValue`.
 *
 * - `null` to invoke `doValueInvalid_` and not set a value.
 *
 * - `undefined` to set `newValue` as is.
 */
export type FieldColourValidator = FieldValidator<string>;
//# sourceMappingURL=field_colour.d.ts.map