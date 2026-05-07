/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Text input field.
 *
 * @class
 */
import './events/events_block_change.js';
import { Field, FieldConfig, FieldValidator } from './field.js';
import { Size } from './utils/size.js';
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Supported types for FieldInput subclasses.
 *
 * @internal
 */
type InputTypes = string | number;
/**
 * Abstract class for an editable input field.
 *
 * @typeParam T - The value stored on the field.
 * @internal
 */
export declare abstract class FieldInput<T extends InputTypes> extends Field<string | T> {
    /**
     * Pixel size of input border radius.
     * Should match blocklyText's border-radius in CSS.
     */
    static BORDERRADIUS: number;
    /** Allow browser to spellcheck this field. */
    protected spellcheck_: boolean;
    /** The HTML input element. */
    protected htmlInput_: HTMLInputElement | null;
    /** True if the field's value is currently being edited via the UI. */
    protected isBeingEdited_: boolean;
    /**
     * True if the value currently displayed in the field's editory UI is valid.
     */
    protected isTextValid_: boolean;
    /**
     * The intial value of the field when the user opened an editor to change its
     * value. When the editor is disposed, an event will be fired that uses this
     * as the event's oldValue.
     */
    protected valueWhenEditorWasOpened_: string | T | null;
    /** Key down event data. */
    private onKeyDownWrapper;
    /** Key input event data. */
    private onKeyInputWrapper;
    /**
     * Whether the field should consider the whole parent block to be its click
     * target.
     */
    fullBlockClickTarget_: boolean;
    /** The workspace that this field belongs to. */
    protected workspace_: WorkspaceSvg | null;
    /**
     * Serializable fields are saved by the serializer, non-serializable fields
     * are not. Editable fields should also be serializable.
     */
    SERIALIZABLE: boolean;
    /** Mouse cursor style when over the hotspot that initiates the editor. */
    CURSOR: string;
    /**
     * @param value The initial value of the field. Should cast to a string.
     *     Defaults to an empty string if null or undefined. Also accepts
     *     Field.SKIP_SETUP if you wish to skip setup (only used by subclasses
     *     that want to handle configuration and setting the field value after
     *     their own constructors have run).
     * @param validator A function that is called to validate changes to the
     *     field's value. Takes in a string & returns a validated string, or null
     *     to abort the change.
     * @param config A map of options used to configure the field.
     *     See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/text-input#creation}
     * for a list of properties this parameter supports.
     */
    constructor(value?: string | typeof Field.SKIP_SETUP, validator?: FieldInputValidator<T> | null, config?: FieldInputConfig);
    protected configure_(config: FieldInputConfig): void;
    initView(): void;
    protected isFullBlockField(): boolean;
    /**
     * Called by setValue if the text input is not valid. If the field is
     * currently being edited it reverts value of the field to the previous
     * value while allowing the display text to be handled by the htmlInput_.
     *
     * @param _invalidValue The input value that was determined to be invalid.
     *     This is not used by the text input because its display value is stored
     *     on the htmlInput_.
     * @param fireChangeEvent Whether to fire a change event if the value changes.
     */
    protected doValueInvalid_(_invalidValue: any, fireChangeEvent?: boolean): void;
    /**
     * Called by setValue if the text input is valid. Updates the value of the
     * field, and updates the text of the field if it is not currently being
     * edited (i.e. handled by the htmlInput_).
     *
     * @param newValue The value to be saved. The default validator guarantees
     *     that this is a string.
     */
    protected doValueUpdate_(newValue: string | T): void;
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
     * Notifies the field that it has changed locations. Moves the widget div to
     * be in the correct place if it is open.
     */
    onLocationChange(): void;
    /**
     * Updates the colour of the htmlInput given the current validity of the
     * field's value.
     *
     * Also updates the colour of the block to reflect whether this is a full
     * block field or not.
     */
    protected render_(): void;
    /**
     * Set whether this field is spellchecked by the browser.
     *
     * @param check True if checked.
     */
    setSpellcheck(check: boolean): void;
    /**
     * Show an editor for the field.
     * Shows the inline free-text editor on top of the text by default.
     * Shows a prompt editor for mobile browsers if the modalInputs option is
     * enabled.
     *
     * @param _e Optional mouse event that triggered the field to open, or
     *     undefined if triggered programmatically.
     * @param quietInput True if editor should be created without focus.
     *     Defaults to false.
     */
    protected showEditor_(_e?: Event, quietInput?: boolean): void;
    /**
     * Create and show a text input editor that is a prompt (usually a popup).
     * Mobile browsers may have issues with in-line textareas (focus and
     * keyboards).
     */
    private showPromptEditor;
    /**
     * Create and show a text input editor that sits directly over the text input.
     *
     * @param quietInput True if editor should be created without focus.
     */
    private showInlineEditor;
    /**
     * Create the text input editor widget.
     *
     * @returns The newly created text input editor.
     */
    protected widgetCreate_(): HTMLInputElement | HTMLTextAreaElement;
    /**
     * Closes the editor, saves the results, and disposes of any events or
     * DOM-references belonging to the editor.
     */
    protected widgetDispose_(): void;
    /**
     * A callback triggered when the user is done editing the field via the UI.
     *
     * @param _value The new value of the field.
     */
    onFinishEditing_(_value: any): void;
    /**
     * Bind handlers for user input on the text input field's editor.
     *
     * @param htmlInput The htmlInput to which event handlers will be bound.
     */
    protected bindInputEvents_(htmlInput: HTMLElement): void;
    /** Unbind handlers for user input and workspace size changes. */
    protected unbindInputEvents_(): void;
    /**
     * Handle key down to the editor.
     *
     * @param e Keyboard event.
     */
    protected onHtmlInputKeyDown_(e: KeyboardEvent): void;
    /**
     * Handle a change to the editor.
     *
     * @param _e Keyboard event.
     */
    private onHtmlInputChange;
    /**
     * Set the HTML input value and the field's internal value. The difference
     * between this and `setValue` is that this also updates the HTML input
     * value whilst editing.
     *
     * @param newValue New value.
     * @param fireChangeEvent Whether to fire a change event. Defaults to true.
     *     Should usually be true unless the change will be reported some other
     *     way, e.g. an intermediate field change event.
     */
    protected setEditorValue_(newValue: any, fireChangeEvent?: boolean): void;
    /** Resize the editor to fit the text. */
    protected resizeEditor_(): void;
    /**
     * Handles repositioning the WidgetDiv used for input fields when the
     * workspace is resized. Will bump the block into the viewport and update the
     * position of the text input if necessary.
     *
     * @returns True for rendered workspaces, as we never want to hide the widget
     *     div.
     */
    repositionForWindowResize(): boolean;
    /**
     * Returns whether or not the field is tab navigable.
     *
     * @returns True if the field is tab navigable.
     */
    isTabNavigable(): boolean;
    /**
     * Use the `getText_` developer hook to override the field's text
     * representation. When we're currently editing, return the current HTML value
     * instead. Otherwise, return null which tells the field to use the default
     * behaviour (which is a string cast of the field's value).
     *
     * @returns The HTML value if we're editing, otherwise null.
     */
    protected getText_(): string | null;
    /**
     * Transform the provided value into a text to show in the HTML input.
     * Override this method if the field's HTML input representation is different
     * than the field's value. This should be coupled with an override of
     * `getValueFromEditorText_`.
     *
     * @param value The value stored in this field.
     * @returns The text to show on the HTML input.
     */
    protected getEditorText_(value: any): string;
    /**
     * Transform the text received from the HTML input into a value to store
     * in this field.
     * Override this method if the field's HTML input representation is different
     * than the field's value. This should be coupled with an override of
     * `getEditorText_`.
     *
     * @param text Text received from the HTML input.
     * @returns The value to store.
     */
    protected getValueFromEditorText_(text: string): any;
}
/**
 * Config options for the input field.
 *
 * @internal
 */
export interface FieldInputConfig extends FieldConfig {
    spellcheck?: boolean;
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
 * @internal
 */
export type FieldInputValidator<T extends InputTypes> = FieldValidator<string | T>;
export {};
//# sourceMappingURL=field_input.d.ts.map