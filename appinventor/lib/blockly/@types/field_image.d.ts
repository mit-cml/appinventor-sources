/**
 * @license
 * Copyright 2012 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Image field.  Used for pictures, icons, etc.
 *
 * @class
 */
import { Field, FieldConfig } from './field.js';
/**
 * Class for an image on a block.
 */
export declare class FieldImage extends Field<string> {
    /**
     * Vertical padding below the image, which is included in the reported height
     * of the field.
     */
    private static readonly Y_PADDING;
    protected readonly imageHeight: number;
    /** The function to be called when this field is clicked. */
    private clickHandler;
    /** The rendered field's image element. */
    protected imageElement: SVGImageElement | null;
    /**
     * Editable fields usually show some sort of UI indicating they are
     * editable. This field should not.
     */
    readonly EDITABLE = false;
    /**
     * Used to tell if the field needs to be rendered the next time the block is
     * rendered. Image fields are statically sized, and only need to be
     * rendered at initialization.
     */
    protected isDirty_: boolean;
    /** Whether to flip this image in RTL. */
    private flipRtl;
    /** Alt text of this image. */
    private altText;
    /**
     * @param src The URL of the image.
     *     Also accepts Field.SKIP_SETUP if you wish to skip setup (only used by
     * subclasses that want to handle configuration and setting the field value
     * after their own constructors have run).
     * @param width Width of the image.
     * @param height Height of the image.
     * @param alt Optional alt text for when block is collapsed.
     * @param onClick Optional function to be called when the image is
     *     clicked. If onClick is defined, alt must also be defined.
     * @param flipRtl Whether to flip the icon in RTL.
     * @param config A map of options used to configure the field.
     *     See the [field creation documentation]{@link
     * https://developers.google.com/blockly/guides/create-custom-blocks/fields/built-in-fields/image#creation}
     * for a list of properties this parameter supports.
     */
    constructor(src: string | typeof Field.SKIP_SETUP, width: string | number, height: string | number, alt?: string, onClick?: (p1: FieldImage) => void, flipRtl?: boolean, config?: FieldImageConfig);
    /**
     * Configure the field based on the given map of options.
     *
     * @param config A map of options to configure the field based on.
     */
    protected configure_(config: FieldImageConfig): void;
    /**
     * Create the block UI for this image.
     */
    initView(): void;
    updateSize_(): void;
    /**
     * Ensure that the input value (the source URL) is a string.
     *
     * @param newValue The input value.
     * @returns A string, or null if invalid.
     */
    protected doClassValidation_(newValue?: any): string | null;
    /**
     * Update the value of this image field, and update the displayed image.
     *
     * @param newValue The value to be saved. The default validator guarantees
     *     that this is a string.
     */
    protected doValueUpdate_(newValue: string): void;
    /**
     * Get whether to flip this image in RTL
     *
     * @returns True if we should flip in RTL.
     */
    getFlipRtl(): boolean;
    /**
     * Set the alt text of this image.
     *
     * @param alt New alt text.
     */
    setAlt(alt: string | null): void;
    /**
     * Check whether this field should be clickable.
     *
     * @returns Whether this field is clickable.
     */
    isClickable(): boolean;
    /**
     * If field click is called, and click handler defined,
     * call the handler.
     */
    protected showEditor_(): void;
    /**
     * Set the function that is called when this image  is clicked.
     *
     * @param func The function that is called when the image is clicked, or null
     *     to remove.
     */
    setOnClickHandler(func: ((p1: FieldImage) => void) | null): void;
    /**
     * Use the `getText_` developer hook to override the field's text
     * representation.
     * Return the image alt text instead.
     *
     * @returns The image alt text.
     */
    protected getText_(): string | null;
    /**
     * Construct a FieldImage from a JSON arg object,
     * dereferencing any string table references.
     *
     * @param options A JSON object with options (src, width, height, alt, and
     *     flipRtl).
     * @returns The new field instance.
     * @nocollapse
     * @internal
     */
    static fromJson(options: FieldImageFromJsonConfig): FieldImage;
    /**
     * Gets an ARIA-friendly label representation of this field's type.
     *
     * Implementations are responsible for, and encouraged to, return a localized
     * version of the ARIA representation of the field's type.
     *
     * @returns An ARIA representation of the field's type or a default if it is
     *     unspecified.
     */
    getAriaTypeName(): string | null;
    /**
     * Gets an ARIA-friendly label representation of this field's value.
     *
     * Implementations are responsible for, and encouraged to, return a localized
     * version of the ARIA representation of the field's value.
     *
     * @returns An ARIA representation of the field's text, or null if no text is
     *     currently defined or known for the field.
     */
    getAriaValue(): string | null;
    /**
     * Computes a descriptive ARIA label to represent this field with configurable
     * verbosity.
     *
     * A 'verbose' label includes type information, if available, whereas a
     * non-verbose label only contains the field's value.
     *
     * Note that this will always return the latest representation of the field's
     * label which may differ from any previously set ARIA label for the field
     * itself. Implementations are largely responsible for ensuring that the
     * field's ARIA label is set correctly at relevant moments in the field's
     * lifecycle (such as when its value changes).
     *
     * Finally, it is never guaranteed that implementations use the label returned
     * by this method for their actual ARIA label. Some implementations may rely
     * on other contexts to convey information like the field's value. Example:
     * checkboxes represent their checked/non-checked status (i.e. value) through
     * a separate ARIA property.
     *
     * Returns an empty string on clickable images (buttons), as we do not want to
     * include image buttons on the block-level ARIA label. When the button is
     * focused the label is set in recomputeAriaContext below.
     *
     * @param includeTypeInfo Whether to include the field's type information in
     *     the returned label, if available.
     */
    computeAriaLabel(includeTypeInfo: boolean): string;
    /**
     * Customizes label and sets additional aria state.
     */
    recomputeAriaContext(): boolean;
}
/**
 * Config options for the image field.
 */
export interface FieldImageConfig extends FieldConfig {
    flipRtl?: boolean;
    alt?: string;
}
/**
 * fromJson config options for the image field.
 */
export interface FieldImageFromJsonConfig extends FieldImageConfig {
    src?: string;
    width?: number;
    height?: number;
}
//# sourceMappingURL=field_image.d.ts.map