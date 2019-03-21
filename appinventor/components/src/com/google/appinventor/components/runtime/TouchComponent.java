// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.TextViewUtil;
import com.google.appinventor.components.runtime.util.ViewUtil;

import java.io.IOException;

/**
 * Underlying base class for click-based components, not directly accessible to Simple programmers.
 */
@SimpleObject
public abstract class TouchComponent<T extends View> extends AndroidViewComponent
        implements View.OnTouchListener, View.OnFocusChangeListener {

    protected T view;

    // Used for determining if visual feedback should be provided for components that have images
    private boolean showFeedback = true;

    // Image path
    private String imagePath = "";

    // Backing for background color
    protected int backgroundColor;

    // This is the Drawable corresponding to the Image property.
    // If an Image has never been set or if the most recent Image
    // could not be loaded, this is null.
    protected Drawable backgroundImageDrawable;

    // Stores default background of the component
    private Drawable defaultDrawable;

    /**
     * Creates a new TouchComponent component.
     *
     * @param container container, component will be placed in
     */
    public TouchComponent(ComponentContainer container) {
        super(container);
    }

    protected void initToggle() {
        // Adds the component to its designated container
        container.$add(this);

        view.setOnTouchListener(this);
        view.setOnFocusChangeListener(this);

        defaultDrawable = view.getBackground();

        Enabled(true);
        // BackgroundColor and Image are dangerous properties:
        // Once either of them is set, the 3D bevel effect for the button is
        // irretrievable, except by reloading defaultButtonDrawable, defined above.
        BackgroundColor(Component.COLOR_DEFAULT);
        Image("");
    }

    @Override
    public View getView() {
        return view;
    }

    /**
     * Indicates when a component is touch down
     */
    @SimpleEvent(description = "Indicates that the button was pressed down.")
    public void TouchDown() {
        EventDispatcher.dispatchEvent(this, "TouchDown");
    }

    /**
     * Indicates when a component touch ends
     */
    @SimpleEvent(description = "Indicates that a button has been released.")
    public void TouchUp() {
        EventDispatcher.dispatchEvent(this, "TouchUp");
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


    /**
     * If a custom background images is specified for the button, then it will lose the pressed
     * and disabled image effects; no visual feedback.
     * The approach below is to provide a visual feedback if and only if an image is assigned
     * to the button. In this situation, we overlay a gray background when pressed and
     * release when not-pressed.
     */
    @Override
    public boolean onTouch(View view, MotionEvent me) {
        //NOTE: We ALWAYS return false because we want to indicate that this listener has not
        //been consumed. Using this approach, other listeners (e.g. OnClick) can process as normal.
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            //button pressed, provide visual feedback AND return false
            if (ShowFeedback() && (AppInventorCompatActivity.isClassicMode() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
                view.getBackground().setAlpha(70); // translucent
                view.invalidate();
            }
            TouchDown();
        } else if (me.getAction() == MotionEvent.ACTION_UP ||
                me.getAction() == MotionEvent.ACTION_CANCEL) {
            //button released, set button back to normal AND return false
            if (ShowFeedback() && (AppInventorCompatActivity.isClassicMode() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
                view.getBackground().setAlpha(255); // opaque
                view.invalidate();
            }
            TouchUp();
        }

        return false;
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

    /**
     * Returns true if the component is active and interacatable.
     *
     * @return {@code true} indicates enabled, {@code false} disabled
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR,
            description = "If set, user can tap check box to cause action.")
    public boolean Enabled() {
        return view.isEnabled();
    }

    /**
     * Specifies whether the component should be active and interactable.
     *
     * @param enabled {@code true} for enabled, {@code false} disabled
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        view.setEnabled(enabled);
        view.invalidate();
    }


    /**
     * Specifies if a visual feedback should be shown when a component with an assigned image
     * is pressed.
     *
     * @param showFeedback {@code true} enables showing feedback,
     *                     {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty(description = "Specifies if a visual feedback should be shown " +
            " for a button that as an image as background.")

    public void ShowFeedback(boolean showFeedback) {
        this.showFeedback = showFeedback;
    }

    /**
     * Returns true if the component should provide visual feedwback when it is pressed
     * and there is an image assigned.
     *
     * @return {@code true} indicates visual feedback will be shown,
     * {@code false} visual feedback will not be shown
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Returns the button's visual feedback state")
    public boolean ShowFeedback() {
        return showFeedback;
    }

    /**
     * Returns the button's background color as an alpha-red-green-blue
     * integer.
     *
     * @return background RGB color with alpha
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Returns the button's background color")
    public int BackgroundColor() {
        return backgroundColor;
    }

    /**
     * Specifies the button's background color as an alpha-red-green-blue
     * integer.  If the parameter is {@link Component#COLOR_DEFAULT}, the
     * original beveling is restored.  If an Image has been set, the color
     * change will not be visible until the Image is removed.
     *
     * @param argb background RGB color with alpha
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_COLOR,
            defaultValue = Component.DEFAULT_VALUE_COLOR_DEFAULT)
    @SimpleProperty(description = "Specifies the button's background color. " +
            "The background color will not be visible if an Image is being displayed.")
    public void BackgroundColor(int argb) {
        backgroundColor = argb;
        updateAppearance();
    }

    /**
     * Returns the path of the button's image.
     *
     * @return the path of the button's image
     */
    @SimpleProperty(
            category = PropertyCategory.APPEARANCE,
            description = "Image to display on button.")
    public String Image() {
        return imagePath;
    }

    /**
     * Specifies the path of the button's image.
     * <p>
     * <p/>See {@link MediaUtil#determineMediaSource} for information about what
     * a path can be.
     *
     * @param path the path of the button's image
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET,
            defaultValue = "")
    @SimpleProperty(description = "Specifies the path of the button's image.  " +
            "If there is both an Image and a BackgroundColor, only the Image will be " +
            "visible.")
    public void Image(String path) {
        // If it's the same as on the prior call and the prior load was successful,
        // do nothing.
        if (path.equals(imagePath) && backgroundImageDrawable != null) {
            return;
        }

        imagePath = (path == null) ? "" : path;

        // Clear the prior background image.
        backgroundImageDrawable = null;

        // Load image from file.
        if (imagePath.length() > 0) {
            try {
                backgroundImageDrawable = MediaUtil.getBitmapDrawable(container.$form(), imagePath);
            } catch (IOException ioe) {
                // TODO(user): Maybe raise Form.ErrorOccurred.
                //Log.e(LOG_TAG, "Unable to load " + imagePath);
                // Fall through with a value of null for backgroundImageDrawable.
            }
        }

        // Update the appearance based on the new value of backgroundImageDrawable.
        updateAppearance();
    }

    protected void updateAppearance() {
        // If there is no background image,
        // the appearance depends solely on the background color and shape.
        if (backgroundImageDrawable == null) {
            if (backgroundColor == Component.COLOR_DEFAULT) {
                // If there is no background image and color is default,
                // restore original 3D bevel appearance.
                ViewUtil.setBackgroundDrawable(view, defaultDrawable);
            } else if (backgroundColor == Component.COLOR_NONE) {
                // Clear the background image.
                ViewUtil.setBackgroundDrawable(view, null);
                //Now we set again the default drawable
                ViewUtil.setBackgroundDrawable(view, defaultDrawable);
                view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.CLEAR);
            } else {
                // Clear the background image.
                ViewUtil.setBackgroundDrawable(view, null);
                //Now we set again the default drawable
                ViewUtil.setBackgroundDrawable(view, defaultDrawable);
                //@Author NMD (Next Mobile Development) [nmdofficialhelp@gmail.com]
                view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP);
            }
        } else {
            // If there is a background image
            ViewUtil.setBackgroundImage(view, backgroundImageDrawable);
        }
    }
}