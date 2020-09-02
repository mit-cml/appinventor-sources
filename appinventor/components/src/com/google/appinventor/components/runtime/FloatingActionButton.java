// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.core.widget.ImageViewCompat;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.io.IOException;

/**
 * Component for Floating Action Button. This is a circular
 * button that triggers the primary action in the app. The default icon is
 * a white plus which can be changed using {@link #Icon(String)} property.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */

@DesignerComponent(version = YaVersion.FLOATING_ACTION_BUTTON_COMPONENT_VERSION,
    category = ComponentCategory.USERINTERFACE,
description = "Component for Floating Action Button. This is a circular " +
    "button that triggers the primary action in the app. The default icon is " +
    "a white plus which can be changed using {@link #Icon(String)} property.")
@SimpleObject
public class FloatingActionButton extends AndroidViewComponent {
  private com.google.android.material.floatingactionbutton.FloatingActionButton floatingActionButton;
  private String iconPath = "";
  private Drawable icon = null;

  /**
   * Creates a new AndroidViewComponent.
   *
   * @param container container, component will be placed in
   */
  public FloatingActionButton(ComponentContainer container) {
    super(container);
    floatingActionButton = new com.google.android.material.floatingactionbutton.FloatingActionButton(container.$form());
    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    int px = (int) (24 * (Resources.getSystem().getDisplayMetrics().density));
    layoutParams.setMargins(0, 0, px, px);
    container.$form().frameWithFloatingActionButton.addView(floatingActionButton, layoutParams);
    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.rgb(255, 64, 129)));
    if (icon == null) {
      floatingActionButton.setImageResource(android.R.drawable.ic_input_add);
      ImageViewCompat.setImageTintList(floatingActionButton, ColorStateList.valueOf(Color.WHITE));
    }
    floatingActionButton.show();
    floatingActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Click();
      }
    });
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Icon(String path) {
    try {
      icon = MediaUtil.getBitmapDrawable(container.$form(), path);
      iconPath = path;
      floatingActionButton.setImageDrawable(icon);
    } catch (IOException e) {
      Log.d("FloatingActionButton", "Unable to load icon" + iconPath);
    }
  }

  @SimpleProperty
  public String Icon() {
    return iconPath;
  }

  @SimpleEvent(description = "Event raised when user clicks on Floating Action Button")
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }

  @Override
  public View getView() {
    return floatingActionButton;
  }
}
