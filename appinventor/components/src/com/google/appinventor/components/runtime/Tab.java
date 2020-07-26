package com.google.appinventor.components.runtime;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.MediaUtil;

import java.io.IOException;

@DesignerComponent(version = YaVersion.TAB_COMPONENT_VERSION,
    category = ComponentCategory.LAYOUT)
@SimpleObject
public class Tab extends HVArrangement<ViewGroup> implements Component, ComponentContainer {
  private static final String LOG_TAG = Tab.class.getSimpleName();
  private com.google.android.material.tabs.TabLayout.Tab tab;
  private String text = "";
  private boolean showText = true;
  private String iconPath = "";
  private Drawable icon = null;
  private boolean showIcon = true;
  public boolean isScrollable = false;
  
  public Tab (TabArrangement container) {
    super(container, HVArrangement.LAYOUT_ORIENTATION_VERTICAL, new FrameLayout(container.$context()));
    Log.d("tabarrangement" ,"Constructor for tab: " + this);
    container.addTab(this);
  }
  
  public TabLayout.Tab getTab() {
    return tab;
  }
  
  public void setTab (TabLayout.Tab tab) {
    this.tab = tab;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Text(String text) {
    this.text = text;
    tab.setText(showText ? text : "");
    Log.d("tabarrangement", "Setting text for tab: " + this + " " + tab + " to: " + tab.getText());
  }
  
  @SimpleProperty
  public String Text() {
    CharSequence text = tab.getText();
    return (text == null ? "" : text.toString());
  }
  
  @DesignerProperty(editorType =  PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShowText(boolean show) {
    showText = show;
    tab.setText(show ? text : "");
    Log.d("tabarrangement","Setting text on the basis of showText: " + showText + " " + (show ? text : ""));
  }
  
  @SimpleProperty
  public boolean ShowText() {
    return showText;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ASSET)
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Icon(String path) {
    try {
      icon = MediaUtil.getBitmapDrawable($form(), path);
      iconPath = path;
      tab.setIcon(showIcon ? icon : null);
    } catch (IOException e) {
      Log.d(LOG_TAG, "Unable to load icon " + iconPath);
    }
  }
  
  @SimpleProperty
  public String Icon() {
    return iconPath;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void ShowIcon(boolean show) {
    showIcon = show;
    tab.setIcon(show ? icon : null);
  }
  
  @SimpleProperty
  public boolean ShowIcon() {
    return showIcon;
  }
  
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(category = PropertyCategory.APPEARANCE)
  public void Scrollable(boolean isScrollable) {
    this.isScrollable = isScrollable;
  }
  
  @SimpleProperty
  public boolean Scrollable() {
    return isScrollable;
  }
  
  @SimpleEvent
  public void Click() {
    EventDispatcher.dispatchEvent(this, "Click");
  }
  
  @SimpleFunction
  public void Show() {
    tab.select();
  }
}