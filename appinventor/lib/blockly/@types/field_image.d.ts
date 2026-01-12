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
import { Size } from './utils/size.js';
/**
 * Class for an image on a block.
 */
export declare class FieldImage extends Field<string> {
    /**
     * Vertical padding below the image, which is included in the reported height
     * of the field.
     */
    private static readonly Y_PADDING;
    protected size_: Size;
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
}
/**
 * Config options for the image field.
 */
export interface FieldImageConfig extends FieldConfig {
    flipRtl?: boolean;
    alt?: string;
}
/**
 * fromJson config options for the colour field.
 */
export interface FieldImageFromJsonConfig extends FieldImageConfig {
    src?: string;
    width?: number;
    height?: number;
}
//# sourceMappingURL=field_image.d.ts.map