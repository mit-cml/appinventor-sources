/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Field.  Used for editable titles, variables, etc.
 * This is an abstract class that defines the UI on the block.  Actual
 * instances would be FieldTextInput, FieldDropdown, etc.
 *
 * @class
 */
import './events/events_block_change.js';
import type { Block } from './block.js';
import type { Input } from './inputs/input.js';
import type { IASTNodeLocationSvg } from './interfaces/i_ast_node_location_svg.js';
import type { IASTNodeLocationWithBlock } from './interfaces/i_ast_node_location_with_block.js';
import type { IKeyboardAccessible } from './interfaces/i_keyboard_accessible.js';
import type { IRegistrable } from './interfaces/i_registrable.js';
import type { ConstantProvider } from './renderers/common/constants.js';
import type { KeyboardShortcut } from './shortcut_registry.js';
import * as Tooltip from './tooltip.js';
import type { Coordinate } from './utils/coordinate.js';
import { Rect } from './utils/rect.js';
import { Size } from './utils/size.js';
import { ISerializable } from './interfaces/i_serializable.js';
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
export type FieldValidator<T = any> = (newValue: T) => T | null | undefined;
/**
 * Abstract class for an editable field.
 *
 * @typeParam T - The value stored on the field.
 */
export declare abstract class Field<T = any> implements IASTNodeLocationSvg, IASTNodeLocationWithBlock, IKeyboardAccessible, IRegistrable, ISerializable {
    /**
     * To overwrite the default value which is set in **Field**, directly update
     * the prototype.
     *
     * Example:
     * `FieldImage.prototype.DEFAULT_VALUE = null;`
     */
    DEFAULT_VALUE: T | null;
    /** Non-breaking space. */
    static readonly NBSP = "\u00A0";
    /**
     * A value used to signal when a field's constructor should *not* set the
     * field's value or run configure_, and should allow a subclass to do that
     * instead.
     */
    static readonly SKIP_SETUP: unique symbol;
    /**
     * Name of field.  Unique within each block.
     * Static labels are usually unnamed.
     */
    name?: string;
    protected value_: T | null;
    /** Validation function called when user edits an editable field. */
    protected validator_: FieldValidator<T> | null;
    /**
     * Used to cache the field's tooltip value if setTooltip is called when the
     * field is not yet initialized. Is *not* guaranteed to be accurate.
     */
    private tooltip_;
    protected size_: Size;
    /**
     * Holds the cursors svg element when the cursor is attached to the field.
     * This is null if there is no cursor on the field.
     */
    private cursorSvg_;
    /**
     * Holds the markers svg element when the marker is attached to the field.
     * This is null if there is no marker on the field.
     */
    private markerSvg_;
    /** The rendered field's SVG group element. */
    protected fieldGroup_: SVGGElement | null;
    /** The rendered field's SVG border element. */
    protected borderRect_: SVGRectElement | null;
    /** The rendered field's SVG text element. */
    protected textElement_: SVGTextElement | null;
    /** The rendered field's text content element. */
    protected textContent_: Text | null;
    /** Mouse down event listener data. */
    private mouseDownWrapper_;
    /** Constants associated with the source block's renderer. */
    protected constants_: ConstantProvider | null;
    /**
     * Has this field been disposed of?
     *
     * @internal
     */
    disposed: boolean;
    /** Maximum characters of text to display before adding an ellipsis. */
    maxDisplayLength: number;
    /** Block this field is attached to.  Starts as null, then set in init. */
    protected sourceBlock_: Block | null;
    /** Does this block need to be re-rendered? */
    protected isDirty_: boolean;
    /** Is the field visible, or hidden due to the block being collapsed? */
    protected visible_: boolean;
    /**
     * Can the field value be changed using the editor on an editable block?
     */
    protected enabled_: boolean;
    /** The element the click handler is bound to. */
    protected clickTarget_: Element | null;
    /**
     * The prefix field.
     *
     * @internal
     */
    prefixField: string | null;
    /**
     * The suffix field.
     *
     * @internal
     */
    suffixField: string | null;
    /**
     * Editable fields usually show some sort of UI indicating they are
     * editable. They will also be saved by the serializer.
     */
    EDITABLE: boolean;
    /**
     * Serializable fields are saved by the serializer, non-serializable fields
     * are not. Editable fields should also be serializable. This is not the
     * case by default so that SERIALIZABLE is backwards compatible.
     */
    SERIALIZABLE: boolean;
    /** Mouse cursor style when over the hotspot that initiates the editor. */
    CURSOR: string;
    /**
     * @param value The initial value of the field.
     *     Also accepts Field.SKIP_SETUP if you wish to skip setup (only used by
     * subclasses that want to handle configuration and setting the field value
     * after their own constructors have run).
     * @param validator  A function that is called to validate changes to the
     *     field's value. Takes in a value & returns a validated value, or null to
     *     abort the change.
     * @param config A map of options used to configure the field.
     *    Refer to the individual field's documentation for a list of properties
     * this parameter supports.
     */
    constructor(value: T | typeof Field.SKIP_SETUP, validator?: FieldValidator<T> | null, config?: FieldConfig);
    /**
     * Process the configuration map passed to the field.
     *
     * @param config A map of options used to configure the field. See the
     *     individual field's documentation for a list of properties this
     *     parameter supports.
     */
    protected configure_(config: FieldConfig): void;
    /**
     * Attach this field to a block.
     *
     * @param block The block containing this field.
     */
    setSourceBlock(block: Block): void;
    /**
     * Get the renderer constant provider.
     *
     * @returns The renderer constant provider.
     */
    getConstants(): ConstantProvider | null;
    /**
     * Get the block this field is attached to.
     *
     * @returns The block containing this field.
     * @throws An error if the source block is not defined.
     */
    getSourceBlock(): Block | null;
    /**
     * Initialize everything to render this field. Override
     * methods initModel and initView rather than this method.
     *
     * @sealed
     * @internal
     */
    init(): void;
    /**
     * Create the block UI for this field.
     */
    protected initView(): void;
    /**
     * Initializes the model of the field after it has been installed on a block.
     * No-op by default.
     */
    initModel(): void;
    /**
     * Defines whether this field should take up the full block or not.
     *
     * Be cautious when overriding this function. It may not work as you expect /
     * intend because the behavior was kind of hacked in. If you are thinking
     * about overriding this function, post on the forum with your intended
     * behavior to see if there's another approach.
     */
    protected isFullBlockField(): boolean;
    /**
     * Create a field border rect element. Not to be overridden by subclasses.
     * Instead modify the result of the function inside initView, or create a
     * separate function to call.
     */
    protected createBorderRect_(): void;
    /**
     * Create a field text element. Not to be overridden by subclasses. Instead
     * modify the result of the function inside initView, or create a separate
     * function to call.
     */
    protected createTextElement_(): void;
    /**
     * Bind events to the field. Can be overridden by subclasses if they need to
     * do custom input handling.
     */
    protected bindEvents_(): void;
    /**
     * Sets the field's value based on the given XML element. Should only be
     * called by Blockly.Xml.
     *
     * @param fieldElement The element containing info about the field's state.
     * @internal
     */
    fromXml(fieldElement: Element): void;
    /**
     * Serializes this field's value to XML. Should only be called by Blockly.Xml.
     *
     * @param fieldElement The element to populate with info about the field's
     *     state.
     * @returns The element containing info about the field's state.
     * @internal
     */
    toXml(fieldElement: Element): Element;
    /**
     * Saves this fields value as something which can be serialized to JSON.
     * Should only be called by the serialization system.
     *
     * @param _doFullSerialization If true, this signals to the field that if it
     *     normally just saves a reference to some state (eg variable fields) it
     *     should instead serialize the full state of the thing being referenced.
     *     See the
     *     {@link https://developers.devsite.google.com/blockly/guides/create-custom-blocks/fields/customizing-fields/creating#full_serialization_and_backing_data | field serialization docs}
     *     for more information.
     * @returns JSON serializable state.
     * @internal
     */
    saveState(_doFullSerialization?: boolean): any;
    /**
     * Sets the field's state based on the given state value. Should only be
     * called by the serialization system.
     *
     * @param state The state we want to apply to the field.
     * @internal
     */
    loadState(state: any): void;
    /**
     * Returns a stringified version of the XML state, if it should be used.
     * Otherwise this returns null, to signal the field should use its own
     * serialization.
     *
     * @param callingClass The class calling this method.
     *     Used to see if `this` has overridden any relevant hooks.
     * @returns The stringified version of the XML state, or null.
     */
    protected saveLegacyState(callingClass: FieldProto): string | null;
    /**
     * Loads the given state using either the old XML hooks, if they should be
     * used. Returns true to indicate loading has been handled, false otherwise.
     *
     * @param callingClass The class calling this method.
     *     Used to see if `this` has overridden any relevant hooks.
     * @param state The state to apply to the field.
     * @returns Whether the state was applied or not.
     */
    loadLegacyState(callingClass: FieldProto, state: any): boolean;
    /**
     * Dispose of all DOM objects and events belonging to this editable field.
     *
     * @internal
     */
    dispose(): void;
    /** Add or remove the UI indicating if this field is editable or not. */
    updateEditable(): void;
    /**
     * Set whether this field's value can be changed using the editor when the
     *     source block is editable.
     *
     * @param enabled True if enabled.
     */
    setEnabled(enabled: boolean): void;
    /**
     * Check whether this field's value can be changed using the editor when the
     *     source block is editable.
     *
     * @returns Whether this field is enabled.
     */
    isEnabled(): boolean;
    /**
     * Check whether this field defines the showEditor_ function.
     *
     * @returns Whether this field is clickable.
     */
    isClickable(): boolean;
    /**
     * Check whether the field should be clickable while the block is in a flyout.
     * The default is that fields are clickable in always-open flyouts such as the
     * simple toolbox, but not in autoclosing flyouts such as the category toolbox.
     * Subclasses may override this function to change this behavior. Note that
     * `isClickable` must also return true for this to have any effect.
     *
     * @param autoClosingFlyout true if the containing flyout is an auto-closing one.
     * @returns Whether the field should be clickable while the block is in a flyout.
     */
    isClickableInFlyout(autoClosingFlyout: boolean): boolean;
    /**
     * Check whether this field is currently editable.  Some fields are never
     * EDITABLE (e.g. text labels). Other fields may be EDITABLE but may exist on
     * non-editable blocks or be currently disabled.
     *
     * @returns Whether this field is currently enabled, editable and on an
     *     editable block.
     */
    isCurrentlyEditable(): boolean;
    /**
     * Check whether this field should be serialized by the XML renderer.
     * Handles the logic for backwards compatibility and incongruous states.
     *
     * @returns Whether this field should be serialized or not.
     */
    isSerializable(): boolean;
    /**
     * Gets whether this editable field is visible or not.
     *
     * @returns True if visible.
     */
    isVisible(): boolean;
    /**
     * Sets whether this editable field is visible or not. Should only be called
     * by input.setVisible.
     *
     * @param visible True if visible.
     * @internal
     */
    setVisible(visible: boolean): void;
    /**
     * Sets a new validation function for editable fields, or clears a previously
     * set validator.
     *
     * The validator function takes in the new field value, and returns
     * validated value. The validated value could be the input value, a modified
     * version of the input value, or null to abort the change.
     *
     * If the function does not return anything (or returns undefined) the new
     * value is accepted as valid. This is to allow for fields using the
     * validated function as a field-level change event notification.
     *
     * @param handler The validator function or null to clear a previous
     *     validator.
     */
    setValidator(handler: FieldValidator<T>): void;
    /**
     * Gets the validation function for editable fields, or null if not set.
     *
     * @returns Validation function, or null.
     */
    getValidator(): FieldValidator<T> | null;
    /**
     * Gets the group element for this editable field.
     * Used for measuring the size and for positioning.
     *
     * @returns The group element.
     */
    getSvgRoot(): SVGGElement | null;
    /**
     * Gets the border rectangle element.
     *
     * @returns The border rectangle element.
     * @throws An error if the border rectangle element is not defined.
     */
    protected getBorderRect(): SVGRectElement;
    /**
     * Gets the text element.
     *
     * @returns The text element.
     * @throws An error if the text element is not defined.
     */
    protected getTextElement(): SVGTextElement;
    /**
     * Gets the text content.
     *
     * @returns The text content.
     * @throws An error if the text content is not defined.
     */
    protected getTextContent(): Text;
    /**
     * Updates the field to match the colour/style of the block.
     *
     * Non-abstract sub-classes may wish to implement this if the colour of the
     * field depends on the colour of the block. It will automatically be called
     * at relevant times, such as when the parent block or renderer changes.
     *
     * See {@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/customizing-fields/creating#matching_block_colours
     * | the field documentation} for more information, or FieldDropdown for an
     * example.
     */
    applyColour(): void;
    /**
     * Used by getSize() to move/resize any DOM elements, and get the new size.
     *
     * All rendering that has an effect on the size/shape of the block should be
     * done here, and should be triggered by getSize().
     */
    protected render_(): void;
    /**
     * Calls showEditor_ when the field is clicked if the field is clickable.
     * Do not override.
     *
     * @param e Optional mouse event that triggered the field to open, or
     *     undefined if triggered programmatically.
     * @sealed
     * @internal
     */
    showEditor(e?: Event): void;
    /**
     * A developer hook to create an editor for the field. This is no-op by
     * default, and must be overriden to create an editor.
     *
     * @param _e Optional mouse event that triggered the field to open, or
     *     undefined if triggered programmatically.
     */
    protected showEditor_(_e?: Event): void;
    /**
     * A developer hook to reposition the WidgetDiv during a window resize. You
     * need to define this hook if your field has a WidgetDiv that needs to
     * reposition itself when the window is resized. For example, text input
     * fields define this hook so that the input WidgetDiv can reposition itself
     * on a window resize event. This is especially important when modal inputs
     * have been disabled, as Android devices will fire a window resize event when
     * the soft keyboard opens.
     *
     * If you want the WidgetDiv to hide itself instead of repositioning, return
     * false. This is the default behavior.
     *
     * DropdownDivs already handle their own positioning logic, so you do not need
     * to override this function if your field only has a DropdownDiv.
     *
     * @returns True if the field should be repositioned,
     *    false if the WidgetDiv should hide itself instead.
     */
    repositionForWindowResize(): boolean;
    /**
     * Updates the size of the field based on the text.
     *
     * @param margin margin to use when positioning the text element.
     */
    protected updateSize_(margin?: number): void;
    /**
     * Position a field's text element after a size change.  This handles both LTR
     * and RTL positioning.
     *
     * @param xOffset x offset to use when positioning the text element.
     * @param contentWidth The content width.
     */
    protected positionTextElement_(xOffset: number, contentWidth: number): void;
    /** Position a field's border rect after a size change. */
    protected positionBorderRect_(): void;
    /**
     * Returns the height and width of the field.
     *
     * This should *in general* be the only place render_ gets called from.
     *
     * @returns Height and width.
     */
    getSize(): Size;
    /**
     * Returns the bounding box of the rendered field, accounting for workspace
     * scaling.
     *
     * @returns An object with top, bottom, left, and right in pixels relative to
     *     the top left corner of the page (window coordinates).
     * @internal
     */
    getScaledBBox(): Rect;
    /**
     * Notifies the field that it has changed locations.
     *
     * @param _ The location of this field's block's top-start corner
     *     in workspace coordinates.
     */
    onLocationChange(_: Coordinate): void;
    /**
     * Get the text from this field to display on the block. May differ from
     * `getText` due to ellipsis, and other formatting.
     *
     * @returns Text to display.
     */
    protected getDisplayText_(): string;
    /**
     * Get the text from this field.
     * Override getText_ to provide a different behavior than simply casting the
     * value to a string.
     *
     * @returns Current text.
     * @sealed
     */
    getText(): string;
    /**
     * A developer hook to override the returned text of this field.
     * Override if the text representation of the value of this field
     * is not just a string cast of its value.
     * Return null to resort to a string cast.
     *
     * @returns Current text or null.
     */
    protected getText_(): string | null;
    /**
     * Force a rerender of the block that this field is installed on, which will
     * rerender this field and adjust for any sizing changes.
     * Other fields on the same block will not rerender, because their sizes have
     * already been recorded.
     *
     * @internal
     */
    markDirty(): void;
    /**
     * Force a rerender of the block that this field is installed on, which will
     * rerender this field and adjust for any sizing changes.
     * Other fields on the same block will not rerender, because their sizes have
     * already been recorded.
     *
     * @internal
     */
    forceRerender(): void;
    /**
     * Used to change the value of the field. Handles validation and events.
     * Subclasses should override doClassValidation_ and doValueUpdate_ rather
     * than this method.
     *
     * @param newValue New value.
     * @param fireChangeEvent Whether to fire a change event. Defaults to true.
     *     Should usually be true unless the change will be reported some other
     *     way, e.g. an intermediate field change event.
     * @sealed
     */
    setValue(newValue: any, fireChangeEvent?: boolean): void;
    /**
     * Process the result of validation.
     *
     * @param newValue New value.
     * @param validatedValue Validated value.
     * @returns New value, or an Error object.
     */
    private processValidation_;
    /**
     * Get the current value of the field.
     *
     * @returns Current value.
     */
    getValue(): T | null;
    /**
     * Validate the changes to a field's value before they are set. See
     * **FieldDropdown** for an example of subclass implementation.
     *
     * **NOTE:** Validation returns one option between `T`, `null`, and
     * `undefined`. **Field**'s implementation will never return `undefined`, but
     * it is valid for a subclass to return `undefined` if the new value is
     * compatible with `T`.
     *
     * @see {@link https://developers.google.com/blockly/guides/create-custom-blocks/fields/validators#return_values}
     * @param newValue - The value to be validated.
     * @returns One of three instructions for setting the new value: `T`, `null`,
     * or `undefined`.
     *
     * - `T` to set this function's returned value instead of `newValue`.
     *
     * - `null` to invoke `doValueInvalid_` and not set a value.
     *
     * - `undefined` to set `newValue` as is.
     */
    protected doClassValidation_(newValue: T): T | null | undefined;
    protected doClassValidation_(newValue?: any): T | null;
    /**
     * Used to update the value of a field. Can be overridden by subclasses to do
     * custom storage of values/updating of external things.
     *
     * @param newValue The value to be saved.
     */
    protected doValueUpdate_(newValue: T): void;
    /**
     * Used to notify the field an invalid value was input. Can be overridden by
     * subclasses, see FieldTextInput.
     * No-op by default.
     *
     * @param _invalidValue The input value that was determined to be invalid.
     */
    protected doValueInvalid_(_invalidValue: any): void;
    /**
     * Handle a pointerdown event on a field.
     *
     * @param e Pointer down event.
     */
    protected onMouseDown_(e: PointerEvent): void;
    /**
     * Sets the tooltip for this field.
     *
     * @param newTip The text for the tooltip, a function that returns the text
     *     for the tooltip, a parent object whose tooltip will be used, or null to
     *     display the tooltip of the parent block. To not display a tooltip pass
     *     the empty string.
     */
    setTooltip(newTip: Tooltip.TipInfo | null): void;
    /**
     * Returns the tooltip text for this field.
     *
     * @returns The tooltip text for this field.
     */
    getTooltip(): string;
    /**
     * The element to bind the click handler to. If not set explicitly, defaults
     * to the SVG root of the field. When this element is
     * clicked on an editable field, the editor will open.
     *
     * @returns Element to bind click handler to.
     */
    protected getClickTarget_(): Element | null;
    /**
     * Return the absolute coordinates of the top-left corner of this field.
     * The origin (0,0) is the top-left corner of the page body.
     *
     * @returns Object with .x and .y properties.
     */
    protected getAbsoluteXY_(): Coordinate;
    /**
     * Whether this field references any Blockly variables.  If true it may need
     * to be handled differently during serialization and deserialization.
     * Subclasses may override this.
     *
     * @returns True if this field has any variable references.
     * @internal
     */
    referencesVariables(): boolean;
    /**
     * Refresh the variable name referenced by this field if this field references
     * variables.
     *
     * @internal
     */
    refreshVariableName(): void;
    /**
     * Search through the list of inputs and their fields in order to find the
     * parent input of a field.
     *
     * @returns The input that the field belongs to.
     * @internal
     */
    getParentInput(): Input;
    /**
     * Returns whether or not we should flip the field in RTL.
     *
     * @returns True if we should flip in RTL.
     */
    getFlipRtl(): boolean;
    /**
     * Returns whether or not the field is tab navigable.
     *
     * @returns True if the field is tab navigable.
     */
    isTabNavigable(): boolean;
    /**
     * Handles the given keyboard shortcut.
     *
     * @param _shortcut The shortcut to be handled.
     * @returns True if the shortcut has been handled, false otherwise.
     */
    onShortcut(_shortcut: KeyboardShortcut): boolean;
    /**
     * Add the cursor SVG to this fields SVG group.
     *
     * @param cursorSvg The SVG root of the cursor to be added to the field group.
     * @internal
     */
    setCursorSvg(cursorSvg: SVGElement): void;
    /**
     * Add the marker SVG to this fields SVG group.
     *
     * @param markerSvg The SVG root of the marker to be added to the field group.
     * @internal
     */
    setMarkerSvg(markerSvg: SVGElement): void;
    /**
     * Redraw any attached marker or cursor svgs if needed.
     *
     * @internal
     */
    updateMarkers_(): void;
}
/**
 * Extra configuration options for the base field.
 */
export interface FieldConfig {
    tooltip?: string;
}
/**
 * For use by Field and descendants of Field. Constructors can change
 * in descendants, though they should contain all of Field's prototype methods.
 *
 * @internal
 */
export type FieldProto = Pick<typeof Field, 'prototype'>;
/**
 * Represents an error where the field is trying to access its block or
 * information about its block before it has actually been attached to said
 * block.
 */
export declare class UnattachedFieldError extends Error {
    /** @internal */
    constructor();
}
//# sourceMappingURL=field.d.ts.map