// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.IceCreamSandwichUtil;
import com.google.appinventor.components.runtime.util.KitkatUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import java.io.IOException;

/**
 * Underlying base class for click-based components, not directly accessible to Simple programmers.
 */
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public abstract class ButtonBase extends TouchComponent<android.widget.Button>
        implements OnClickListener, OnLongClickListener, View.OnFocusChangeListener {

    // Constant for shape
    // 10px is the radius of the rounded corners.
    // 10px was chosen for esthetic reasons.
    private static final float ROUNDED_CORNERS_RADIUS = 10f;
    private static final float[] ROUNDED_CORNERS_ARRAY = new float[]{ROUNDED_CORNERS_RADIUS,
            ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS,
            ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS, ROUNDED_CORNERS_RADIUS,
            ROUNDED_CORNERS_RADIUS};

    // Constant background color for buttons with a Shape other than default
    private static final int SHAPED_DEFAULT_BACKGROUND_COLOR = Color.LTGRAY;

    // Backing for text alignment
    private int textAlignment;

    // Backing for font typeface
    private int fontTypeface;

    // Backing for font bold
    private boolean bold;

    // Backing for font italic
    private boolean italic;

    // Backing for text color
    private int textColor;

    // Backing for button shape
    private int shape;

    // This is our handle in Android's default button color states;
    private ColorStateList defaultColorStateList;

    /**
     * The minimum width of a button for the current theme.
     * <p>
     * We store this statically because it should be constant across all buttons in the app.
     */
    private static int defaultButtonMinWidth = 0;

    /**
     * The minimum height of a button for the current theme.
     * <p>
     * We store this statically because it should be constant across all buttons in the app.
     */
    private static int defaultButtonMinHeight = 0;

    /**
     * Creates a new ButtonBase component.
     *
     * @param container container, component will be placed in
     */
    public ButtonBase(ComponentContainer container) {
        super(container);
        view = new android.widget.Button(container.$context());

        // Save the default values in case the user wants them back later.
        defaultColorStateList = view.getTextColors();
        defaultButtonMinWidth = KitkatUtil.getMinWidth(view);
        defaultButtonMinHeight = KitkatUtil.getMinHeight(view);

        // Initialize TouchComponent attributes
        initToggle();

        // Listen to clicks & focus change
        view.setOnFocusChangeListener(this);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        IceCreamSandwichUtil.setAllCaps(view, false);

        // Default property values
        TextAlignment(Component.ALIGNMENT_CENTER);
        fontTypeface = Component.TYPEFACE_DEFAULT;
        TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
        FontSize(Component.FONT_DEFAULT_SIZE);
        Text("");
        TextColor(Component.COLOR_DEFAULT);
        Shape(Component.BUTTON_SHAPE_DEFAULT);
    }

    /**
     * Returns the alignment of the button's text: center, normal
     * (e.g., left-justified if text is written left to right), or
     * opposite (e.g., right-justified if text is written left to right).
     *
     * @return one of {@link Component#ALIGNMENT_NORMAL},
     * {@link Component#ALIGNMENT_CENTER} or
     * {@link Component#ALIGNMENT_OPPOSITE}
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Left, center, or right.",
            userVisible = false)
    public int TextAlignment() {
        return textAlignment;
    }

    /**
     * Specifies the alignment of the button's text: center, normal
     * (e.g., left-justified if text is written left to right), or
     * opposite (e.g., right-justified if text is written left to right).
     *
     * @param alignment one of {@link Component#ALIGNMENT_NORMAL},
     *                  {@link Component#ALIGNMENT_CENTER} or
     *                  {@link Component#ALIGNMENT_OPPOSITE}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTALIGNMENT,
            defaultValue = Component.ALIGNMENT_CENTER + "")
    @SimpleProperty(userVisible = false)
    public void TextAlignment(int alignment) {
        this.textAlignment = alignment;
        TextViewUtil.setAlignment(view, alignment, true);
    }

    /**
     * Returns the style of the button.
     *
     * @return one of {@link Component#BUTTON_SHAPE_DEFAULT},
     * {@link Component#BUTTON_SHAPE_ROUNDED},
     * {@link Component#BUTTON_SHAPE_RECT} or
     * {@link Component#BUTTON_SHAPE_OVAL}
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            userVisible = false)
    public int Shape() {
        return shape;
    }

    /**
     * Specifies the style the button. This does not check that the argument is a legal value.
     *
     * @param shape one of {@link Component#BUTTON_SHAPE_DEFAULT},
     *              {@link Component#BUTTON_SHAPE_ROUNDED},
     *              {@link Component#BUTTON_SHAPE_RECT} or
     *              {@link Component#BUTTON_SHAPE_OVAL}
     * @throws IllegalArgumentException if shape is not a legal value.
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BUTTON_SHAPE,
            defaultValue = Component.BUTTON_SHAPE_DEFAULT + "")
    @SimpleProperty(description = "Specifies the button's shape (default, rounded," +
            " rectangular, oval). The shape will not be visible if an Image is being displayed.",
            userVisible = false)
    public void Shape(int shape) {
        this.shape = shape;
        updateAppearance();
    }

    // Update appearance based on values of backgroundImageDrawable, backgroundColor and shape.
    // Images take precedence over background colors.
    @Override
    protected void updateAppearance() {
        // If there is no background image,
        // the appearance depends solely on the background color and shape.
        if (backgroundImageDrawable == null) {
            if (shape == BUTTON_SHAPE_DEFAULT) {
                super.updateAppearance();
            } else {
                // If there is no background image and the shape is something other than default,
                // create a drawable with the appropriate shape and color.
                setShape();
            }
            TextViewUtil.setMinSize(view, defaultButtonMinWidth, defaultButtonMinHeight);
        } else {
            // If there is a background image
            super.updateAppearance();
            TextViewUtil.setMinSize(view, 0, 0);
        }
    }

    private ColorStateList createRippleState() {

        int[][] states = new int[][]{new int[]{android.R.attr.state_enabled}};
        int enabled_color = defaultColorStateList.getColorForState(view.getDrawableState(), android.R.attr.state_enabled);
        int[] colors = new int[]{Color.argb(70, Color.red(enabled_color), Color.green(enabled_color),
                Color.blue(enabled_color))};

        return new ColorStateList(states, colors);
    }

    // Throw IllegalArgumentException if shape has illegal value.
    private void setShape() {
        ShapeDrawable drawable = new ShapeDrawable();

        // Set shape of drawable.
        switch (shape) {
            case Component.BUTTON_SHAPE_ROUNDED:
                drawable.setShape(new RoundRectShape(ROUNDED_CORNERS_ARRAY, null, null));
                break;
            case Component.BUTTON_SHAPE_RECT:
                drawable.setShape(new RectShape());
                break;
            case Component.BUTTON_SHAPE_OVAL:
                drawable.setShape(new OvalShape());
                break;
            default:
                throw new IllegalArgumentException();
        }

        // Set drawable to the background of the button.
        if (!AppInventorCompatActivity.isClassicMode() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewUtil.setBackgroundDrawable(view, new RippleDrawable(createRippleState(), drawable, drawable));
        } else {
            ViewUtil.setBackgroundDrawable(view, drawable);
        }

        if (backgroundColor == Component.COLOR_NONE) {
            view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.CLEAR);
        } else if (backgroundColor == Component.COLOR_DEFAULT) {
            view.getBackground().setColorFilter(SHAPED_DEFAULT_BACKGROUND_COLOR, PorterDuff.Mode.SRC_ATOP);
        } else {
            view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
        }

        view.invalidate();
    }

    /**
     * Returns true if the button's text should be bold.
     * If bold has been requested, this property will return true, even if the
     * font does not support bold.
     *
     * @return {@code true} indicates bold, {@code false} normal
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "If set, button text is displayed in bold.")
    public boolean FontBold() {
        return bold;
    }

    /**
     * Specifies whether the button's text should be bold.
     * Some fonts do not support bold.
     *
     * @param bold {@code true} indicates bold, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontBold(boolean bold) {
        this.bold = bold;
        TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    }

    /**
     * Returns true if the button's text should be italic.
     * If italic has been requested, this property will return true, even if the
     * font does not support italic.
     *
     * @return {@code true} indicates italic, {@code false} normal
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "If set, button text is displayed in italics.")
    public boolean FontItalic() {
        return italic;
    }

    /**
     * Specifies whether the button's text should be italic.
     * Some fonts do not support italic.
     *
     * @param italic {@code true} indicates italic, {@code false} normal
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "False")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontItalic(boolean italic) {
        this.italic = italic;
        TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    }

    /**
     * Returns the button's text's font size, measured in sp(scale-independent pixels).
     *
     * @return font size in sp(scale-independent pixels).
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Point size for button text.")
    public float FontSize() {
        return TextViewUtil.getFontSize(view, container.$context());
    }

    /**
     * Specifies the button's text's font size, measured in sp(scale-independent pixels).
     *
     * @param size font size in sp(scale-independent pixels)
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_FLOAT,
            defaultValue = Component.FONT_DEFAULT_SIZE + "")
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE)
    public void FontSize(float size) {
        TextViewUtil.setFontSize(view, size);
    }

    /**
     * Returns the button's text's font face as default, serif, sans
     * serif, or monospace.
     *
     * @return one of {@link Component#TYPEFACE_DEFAULT},
     * {@link Component#TYPEFACE_SERIF},
     * {@link Component#TYPEFACE_SANSSERIF} or
     * {@link Component#TYPEFACE_MONOSPACE}
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Font family for button text.",
            userVisible = false)
    public int FontTypeface() {
        return fontTypeface;
    }

    /**
     * Specifies the button's text's font face as default, serif, sans
     * serif, or monospace.
     *
     * @param typeface one of {@link Component#TYPEFACE_DEFAULT},
     *                 {@link Component#TYPEFACE_SERIF},
     *                 {@link Component#TYPEFACE_SANSSERIF} or
     *                 {@link Component#TYPEFACE_MONOSPACE}
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TYPEFACE,
            defaultValue = Component.TYPEFACE_DEFAULT + "")
    @SimpleProperty(
            userVisible = false)
    public void FontTypeface(int typeface) {
        fontTypeface = typeface;
        TextViewUtil.setFontTypeface(view, fontTypeface, bold, italic);
    }

    /**
     * Returns the text displayed by the button.
     *
     * @return button caption
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Text to display on button.")
    public String Text() {
        return TextViewUtil.getText(view);
    }

    /**
     * Specifies the text displayed by the button.
     *
     * @param text new caption for button
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
            defaultValue = "")
    @SimpleProperty
    public void Text(String text) {
        TextViewUtil.setText(view, text);
    }

    /**
     * Returns the button's text color as an alpha-red-green-blue
     * integer.
     *
     * @return text RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Color for button text.")
    public int TextColor() {
        return textColor;
    }

    /**
     * Specifies the button's text color as an alpha-red-green-blue
     * integer.
     *
     * @param argb text RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty
    public void TextColor(int argb) {
        // TODO(user): I think there is a way of only setting the color for the enabled state
        textColor = argb;
        if (argb != Component.COLOR_DEFAULT) {
            TextViewUtil.setTextColor(view, argb);
        } else {
            TextViewUtil.setTextColors(view, defaultColorStateList);
        }
    }

    /**
     * Indicates the cursor moved over the button so it is now possible
     * to click it.
     */
    @SimpleEvent(description = "Indicates the cursor moved over the button so " +
            "it is now possible to click it.")
    public void GotFocus() {
        EventDispatcher.dispatchEvent(this, "GotFocus");
    }

    /**
     * Indicates the cursor moved away from the button so it is now no
     * longer possible to click it.
     */
    @SimpleEvent(description = "Indicates the cursor moved away from " +
            "the button so it is now no longer possible to click it.")
    public void LostFocus() {
        EventDispatcher.dispatchEvent(this, "LostFocus");
    }

    // OnFocusChangeListener implementation

    @Override
    public void onFocusChange(View previouslyFocused, boolean gainFocus) {
        if (gainFocus) {
            GotFocus();
        } else {
            LostFocus();
        }
    }

    public abstract void click();

    // Override this if your component actually will consume a long
    // click.  A 'false' returned from this function will cause a long
    // click to be interpreted as a click (and the click function will
    // be called).
    public boolean longClick() {
        return false;
    }

    // OnClickListener implementation

    @Override
    public void onClick(View view) {
        click();
    }

    // OnLongClickListener implementation

    @Override
    public boolean onLongClick(View view) {
        return longClick();
    }

}
