/**
 * @license
 * Copyright 2013 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
import { Field } from './field.js';
import { FieldInput, FieldInputConfig, FieldInputValidator } from './field_input.js';
/**
 * Class for an editable angle field.
 */
export declare class FieldAngle extends FieldInput<number> {
    /** Half the width of protractor image. */
    static readonly HALF: number;
    /**
     * Radius of protractor circle.  Slightly smaller than protractor size since
     * otherwise SVG crops off half the border at the edges.
     */
    static readonly RADIUS: number;
    /**
     * Default property describing which direction makes an angle field's value
     * increase. Angle increases clockwise (true) or counterclockwise (false).
     */
    static readonly CLOCKWISE = false;
    /**
     * The default offset of 0 degrees (and all angles). Always offsets in the
     * counterclockwise direction, regardless of the field's clockwise property.
     * Usually either 0 (0 = right) or 90 (0 = up).
     */
    static readonly OFFSET = 0;
    /**
     * The default maximum angle to allow before wrapping.
     * Usually either 360 (for 0 to 359.9) or 180 (for -179.9 to 180).
     */
    static readonly WRAP = 360;
    /**
     * The default amount to round angles to when using a mouse or keyboard nav
     * input. Must be a positive integer to support keyboard navigation.
     */
    static readonly ROUND = 15;
    /**
     * Whether the angle should increase as the angle picker is moved clockwise
     * (true) or counterclockwise (false).
     */
    private clockwise;
    /**
     * The offset of zero degrees (and all other angles).
     */
    private offset;
    /**
     * The maximum angle to allow before wrapping.
     */
    private wrap;
    /**
     * The amount to round angles to when using a mouse or keyboard nav input.
     */
    private round;
    /**
     * Array holding info needed to unbind events.
     * Used for disposing.
     * Ex: [[node, name, func], [node, name, func]].
     */
    private boundEvents;
    /** Dynamic red line pointing at the value's angle. */
    private line;
    /** Dynamic pink area extending from 0 to the value's angle. */
    private gauge;
    /** The degree symbol for this field. */
    protected symbol_: SVGTSpanElement | null;
    /**
     * @param value The initial value of the field. Should cast to a number.
     *     Defaults to 0. Also accepts Field.SKIP_SETUP if you wish to skip setup
     *     (only used by subclasses that want to handle configuration and setting
     *     the field value after their own constructors have run).
     * @param validator A function that is called to validate changes to the
     *     field's value. Takes in a number & returns a validated number, or null
     *     to abort the change.
     * @param config A map of options used to configure the field.
     *     See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/angle#creation}
     * for a list of properties this parameter supports.
     */
    constructor(value?: string | number | typeof Field.SKIP_SETUP, validator?: FieldAngleValidator, config?: FieldAngleConfig);
    /**
     * Configure the field based on the given map of options.
     *
     * @param config A map of options to configure the field based on.
     */
    protected configure_(config: FieldAngleConfig): void;
    /**
     * Create the block UI for this field.
     */
    initView(): void;
    /** Updates the angle when the field rerenders. */
    protected render_(): void;
    /**
     * Create and show the angle field's editor.
     *
     * @param e Optional mouse event that triggered the field to open,
     *     or undefined if triggered programmatically.
     */
    protected showEditor_(e?: Event): void;
    /**
     * Creates the angle dropdown editor.
     *
     * @returns The newly created slider.
     */
    private dropdownCreate;
    /** Disposes of events and DOM-references belonging to the angle editor. */
    private dropdownDispose;
    /** Hide the editor. */
    private hide;
    /**
     * Set the angle to match the mouse's position.
     *
     * @param e Mouse move event.
     */
    protected onMouseMove_(e: PointerEvent): void;
    /**
     * Handles and displays values that are input via mouse or arrow key input.
     * These values need to be rounded and wrapped before being displayed so
     * that the text input's value is appropriate.
     *
     * @param angle New angle.
     */
    private displayMouseOrKeyboardValue;
    /** Redraw the graph with the current angle. */
    private updateGraph;
    /**
     * Handle key down to the editor.
     *
     * @param e Keyboard event.
     */
    protected onHtmlInputKeyDown_(e: KeyboardEvent): void;
    /**
     * Ensure that the input value is a valid angle.
     *
     * @param newValue The input value.
     * @returns A valid angle, or null if invalid.
     */
    protected doClassValidation_(newValue?: any): number | null;
    /**
     * Wraps the value so that it is in the range (-360 + wrap, wrap).
     *
     * @param value The value to wrap.
     * @returns The wrapped value.
     */
    private wrapValue;
    /**
     * Construct a FieldAngle from a JSON arg object.
     *
     * @param options A JSON object with options (angle).
     * @returns The new field instance.
     * @nocollapse
     * @internal
     */
    static fromJson(options: FieldAngleFromJsonConfig): FieldAngle;
}
/**
 * The two main modes of the angle field.
 * Compass specifies:
 *   - clockwise: true
 *   - offset: 90
 *   - wrap: 0
 *   - round: 15
 *
 * Protractor specifies:
 *   - clockwise: false
 *   - offset: 0
 *   - wrap: 0
 *   - round: 15
 */
export declare enum Mode {
    COMPASS = "compass",
    PROTRACTOR = "protractor"
}
/**
 * Extra configuration options for the angle field.
 */
export interface FieldAngleConfig extends FieldInputConfig {
    mode?: Mode;
    clockwise?: boolean;
    offset?: number;
    wrap?: number;
    round?: number;
}
/**
 * fromJson configuration options for the angle field.
 */
export interface FieldAngleFromJsonConfig extends FieldAngleConfig {
    angle?: number;
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
export type FieldAngleValidator = FieldInputValidator<number>;
//# sourceMappingURL=field_angle.d.ts.map